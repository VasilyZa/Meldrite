#!/usr/bin/env python3
"""Restore pinned file dependencies needed before Gradle can configure this build."""

import argparse
from concurrent.futures import ThreadPoolExecutor
import hashlib
import io
import json
from pathlib import Path
import re
import urllib.request
import xml.etree.ElementTree as ET
import zipfile


ROOT = Path(__file__).resolve().parents[2]
GRADLE_CACHE = Path.home() / ".gradle/caches/modules-2/files-2.1"


def sha256(data):
    return hashlib.sha256(data).hexdigest()


def source_urls(url):
    if not url.startswith("https://"):
        raise ValueError(f"Dependency source must use HTTPS: {url}")
    urls = [url]
    paper = "https://repo.papermc.io/repository/maven-public/"
    if url.startswith(paper):
        # The old Maven endpoint can redirect to an HTML UI. Keep the lock's
        # exact artifact and checksum, using Paper's current raw endpoint.
        urls.insert(0, "https://artifactory.papermc.io/artifactory/universe/" + url[len(paper):])
    central = "https://repo.maven.apache.org/maven2/"
    if url.startswith(central):
        urls.append("https://repo1.maven.org/maven2/" + url[len(central):])
    return urls


def download(url, expected):
    failures = []
    for source in source_urls(url):
        for _ in range(2):
            try:
                request = urllib.request.Request(source, headers={"User-Agent": "Meldrite-CI"})
                with urllib.request.urlopen(request, timeout=30) as response:
                    data = response.read()
                actual = sha256(data)
                if actual != expected:
                    raise ValueError(f"SHA-256 mismatch: expected {expected}, got {actual}")
                return data
            except (OSError, ValueError) as error:
                failures.append(f"{source}: {error}")
                if isinstance(error, ValueError):
                    break
    raise RuntimeError("Unable to fetch pinned dependency:\n" + "\n".join(failures))


def project_cache_path(relative):
    base = (ROOT / ".gradle/meldrite/artifacts").resolve()
    destination = (ROOT / relative).resolve()
    if not destination.is_relative_to(base):
        raise ValueError(f"Dependency cache path escapes project cache: {relative}")
    return destination


def dependencies():
    files = {}

    def add(path, digest, url):
        if not re.fullmatch(r"[0-9a-f]{64}", digest):
            raise ValueError(f"Invalid SHA-256 for {path}")
        entry = (path, digest, url)
        if path in files and files[path] != entry:
            raise ValueError(f"Conflicting dependency locks for {path}")
        files[path] = entry

    def visit(node):
        add(project_cache_path(node["cachePath"]), node["sha256"], node["sourceUrl"])
        add(project_cache_path(node["pomCachePath"]), node["pomHash"], node["pomUrl"])
        for child in node["dependencies"]:
            visit(child)

    for name in ["paper-1.20.1.lock.json", "tools.lock.json"]:
        document = json.loads((ROOT / "contracts/locks" / name).read_text())
        for root in document["roots"]:
            visit(root)

    extra = json.loads((ROOT / ".github/build-dependencies.lock.json").read_text())["artifacts"]
    console = json.loads((ROOT / "contracts/locks/console-3.25.1.lock.json").read_text())
    for artifact in console["artifacts"]:
        module = artifact["module"]
        version = console["version"]
        extra.append({
            "coordinate": f"{console['group']}:{module}:{version}",
            "sha256": artifact["sha256"],
            "sourceUrl": f"https://repo.maven.apache.org/maven2/org/jline/{module}/{version}/{module}-{version}.jar",
        })
    return list(files.values()), extra


def ensure_file(entry, check_only):
    path, expected, url = entry
    if path.is_file():
        if sha256(path.read_bytes()) != expected:
            raise ValueError(f"Cached dependency checksum mismatch: {path}")
        return
    if check_only:
        raise FileNotFoundError(path)
    data = download(url, expected)
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_bytes(data)


def release_archive(entry):
    if entry.get("archiveRelease") != "latest":
        return download(entry["sourceUrl"], entry["archiveSha256"])
    request = urllib.request.Request(entry["sourceUrl"], headers={
        "User-Agent": "Meldrite-CI", "Accept": "application/vnd.github+json"})
    with urllib.request.urlopen(request, timeout=30) as response:
        release = json.load(response)
    assets = [asset for asset in release["assets"]
              if re.fullmatch(r"meldrite-.+-1\.20\.1-release\.jar", asset["name"])]
    if len(assets) != 1:
        raise ValueError("Latest release must contain exactly one Minecraft 1.20.1 release JAR")
    asset = assets[0]
    digest = asset.get("digest") or ""
    if not re.fullmatch(r"sha256:[0-9a-f]{64}", digest):
        raise ValueError("Latest release JAR has no valid SHA-256 asset digest")
    print(f"Fetching dependency archive from latest release: {release['tag_name']}")
    return download(asset["browser_download_url"], digest.removeprefix("sha256:"))


def ensure_extra(entry, check_only):
    group, module, version = entry["coordinate"].split(":")
    classifier = "-" + entry["classifier"] if "classifier" in entry else ""
    filename = f"{module}-{version}{classifier}.jar"
    if entry.get("cache") == "project":
        ensure_file((project_cache_path(f".gradle/meldrite/artifacts/{group}/{module}/{version}/{filename}"),
                     entry["sha256"], entry["sourceUrl"]), check_only)
        return
    directory = GRADLE_CACHE / group / module / version
    existing = list(directory.glob(f"*/{filename}"))
    if existing:
        if len(existing) != 1 or sha256(existing[0].read_bytes()) != entry["sha256"]:
            raise ValueError(f"Unexpected cached file dependency: {directory}/{filename}")
        return
    if check_only:
        raise FileNotFoundError(directory / filename)
    if "archiveEntry" in entry:
        # This vendor dependency is already embedded in a pinned release.
        # Check the archive as well as the extracted dependency, never run it.
        archive = release_archive(entry)
        with zipfile.ZipFile(io.BytesIO(archive)) as bundle:
            data = bundle.read(entry["archiveEntry"])
        if sha256(data) != entry["sha256"]:
            raise ValueError(f"Embedded dependency checksum mismatch: {filename}")
    else:
        data = download(entry["sourceUrl"], entry["sha256"])
    # Match Gradle's content-addressed directory so later resolution does not
    # create duplicate files for the build's singleFile lookups.
    path = directory / hashlib.sha1(data).hexdigest() / filename
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_bytes(data)


def trust_loom_outputs():
    path = ROOT / "gradle/verification-metadata.xml"
    text = path.read_text()
    ns = {"v": "https://schema.gradle.org/dependency-verification"}
    config = ET.fromstring(text).find("v:configuration", ns)
    if config.find("v:trusted-artifacts", ns) is not None:
        raise ValueError("Review existing trusted artifacts before changing CI verification")
    # Locally transformed jars have nondeterministic ZIP bytes. The original
    # Minecraft/Mod inputs remain subject to the committed hash checks.
    marker = "   </configuration>"
    if text.count(marker) != 1:
        raise ValueError("Cannot locate dependency verification configuration")
    trusted = '''      <trusted-artifacts>
         <trust group="net[.]minecraft" name="minecraft-merged-.*" regex="true" reason="Local Loom output from verified Minecraft inputs"/>
         <trust group="remapped[.].+" regex="true" reason="Local Loom remapping of separately verified Mod inputs"/>
      </trusted-artifacts>
'''
    path.write_text(text.replace(marker, trusted + marker))


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--check-cache", action="store_true", help="Verify existing files without downloading or modifying them")
    parser.add_argument("--trust-loom-outputs", action="store_true", help="Prepare generated-output verification in the CI checkout")
    args = parser.parse_args()
    if args.check_cache and args.trust_loom_outputs:
        parser.error("--check-cache must not modify verification metadata")
    files, extra = dependencies()
    with ThreadPoolExecutor(max_workers=4) as workers:
        list(workers.map(lambda entry: ensure_file(entry, args.check_cache), files))
        list(workers.map(lambda entry: ensure_extra(entry, args.check_cache), extra))
    if args.trust_loom_outputs:
        trust_loom_outputs()
    print(f"Verified {len(files)} locked contract files and {len(extra)} build dependencies")


if __name__ == "__main__":
    main()
