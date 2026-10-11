#!/usr/bin/env python3
"""Boot a relocated java -jar distribution, then restart its saved world."""
import argparse
import difflib
import hashlib
import io
import json
import os
from pathlib import Path
import re
import shutil
import socket
import struct
import subprocess
import tempfile
import threading
import time
import uuid
import zlib

ROOT = Path(__file__).resolve().parent.parent


def meldrite_log_event(line):
    """Read lifecycle/facts from Minecraft logs, retaining older distribution support."""
    line = re.sub(r"\x1b\[[0-9;]*m", "", line).strip()
    match = re.fullmatch(r"\[\d{2}:\d{2}:\d{2}\] \[[^\]\r\n]+/(?:INFO|WARN|ERROR|DEBUG|TRACE)\]: Meldrite (status|runtime): (.+)", line)
    if match:
        kind, value = match.groups()
        if kind == "status":
            return {"meldrite": kind, "value": value}
        event = {"meldrite": kind}
        for field in value.split():
            key, separator, value = field.partition("=")
            if not separator:
                continue
            if key == "javaFeature":
                try:
                    value = int(value)
                except ValueError:
                    return None
            event[key] = value
        return event
    marker = line.find('{"meldrite":')
    if marker >= 0:
        try:
            event = json.loads(line[marker:])
            if event.get("meldrite") in ("status", "runtime"):
                return event
        except (ValueError, AttributeError):
            pass
    return None


def varint(value):
    result = bytearray()
    while True:
        byte = value & 127
        value >>= 7
        result.append(byte | (128 if value else 0))
        if not value:
            return bytes(result)


def read_exact(stream, length):
    result = bytearray()
    while len(result) < length:
        chunk = stream.read(length - len(result))
        if not chunk:
            raise EOFError("Server closed the Minecraft connection")
        result.extend(chunk)
    return bytes(result)


def read_varint(stream):
    value = 0
    for shift in range(0, 35, 7):
        byte = read_exact(stream, 1)[0]
        value |= (byte & 127) << shift
        if not byte & 128:
            return value
    raise ValueError("Invalid Minecraft VarInt")


def read_frame(stream):
    length = read_varint(stream)
    if not 0 < length <= 2 * 1024 * 1024:
        raise ValueError(f"Invalid Minecraft frame length: {length}")
    return read_exact(stream, length)


def query_status(host, port, timeout=5):
    """Exercise the native handshake, status request and ping on a fresh channel."""
    started = time.monotonic()
    address = host.encode("utf-8")
    handshake = b"\x00" + varint(763) + varint(len(address)) + address + struct.pack(">H", port) + b"\x01"
    with socket.create_connection((host, port), timeout=timeout) as connection:
        connection.settimeout(timeout)
        connection.sendall(varint(len(handshake)) + handshake + b"\x01\x00")
        with connection.makefile("rb") as stream:
            response = io.BytesIO(read_frame(stream))
            if read_varint(response) != 0:
                raise AssertionError("Expected a Minecraft status response")
            status = json.loads(read_exact(response, read_varint(response)).decode("utf-8"))
            if response.read() or status.get("version", {}).get("protocol") != 763:
                raise AssertionError(f"Invalid Minecraft 1.20.1 status: {status}")
            status_ms = round((time.monotonic() - started) * 1000, 3)
            ping = b"\x01" + struct.pack(">q", time.monotonic_ns())
            connection.sendall(varint(len(ping)) + ping)
            if read_frame(stream) != ping:
                raise AssertionError("Minecraft ping nonce was not echoed")
    return {"statusMs": status_ms, "totalMs": round((time.monotonic() - started) * 1000, 3),
            "version": status["version"], "players": status.get("players"), "pong": True}


def await_first_status(host, port, timeout):
    started = time.monotonic()
    deadline = started + timeout
    early_closes = 0
    while True:
        remaining = deadline - time.monotonic()
        if remaining <= 0:
            raise TimeoutError("Minecraft status did not become available after READY")
        try:
            result = query_status(host, port, remaining)
            result["readyToStatusMs"] = round((time.monotonic() - started) * 1000, 3)
            result["earlyStatusCloses"] = early_closes
            return result
        except EOFError:
            # SERVER_STARTED callbacks run before vanilla builds its first
            # cached status. The native handshake closes status requests while
            # that cache is null. Retry only that startup window, never a timeout.
            early_closes += 1
            time.sleep(min(0.05, max(0, deadline - time.monotonic())))


def read_string(stream):
    return read_exact(stream, read_varint(stream)).decode("utf-8")


def read_command_roots(data):
    """Decode the vanilla 1.20.1 command packet used by the isolated fixtures."""
    nodes = []
    count = read_varint(data)
    if count > 100000:
        raise ValueError("Unreasonable command tree node count")
    for _ in range(count):
        flags = read_exact(data, 1)[0]
        children = [read_varint(data) for _ in range(read_varint(data))]
        if flags & 8:
            read_varint(data)  # Redirect node.
        kind = flags & 3
        name = read_string(data) if kind in (1, 2) else ""
        if kind == 2:
            parser = read_varint(data)
            if parser in (1, 2, 3, 4):  # Brigadier numeric bounds.
                bounds = read_exact(data, 1)[0]
                width = 8 if parser in (2, 4) else 4
                read_exact(data, width * (bool(bounds & 1) + bool(bounds & 2)))
            elif parser == 5:  # brigadier:string mode.
                read_varint(data)
            elif parser in (6, 29):  # entity / score_holder flags.
                read_exact(data, 1)
            elif parser == 40:  # time minimum.
                read_exact(data, 4)
            elif parser in (41, 42, 43, 44):  # Registry key.
                read_string(data)
            elif parser > 50:
                raise ValueError(f"The isolated command fixture has a non-vanilla argument serializer: {parser}")
            if flags & 16:
                read_string(data)  # Server suggestion provider ID.
        nodes.append({"name": name, "children": children, "kind": kind})
    root = read_varint(data)
    if data.read() or root >= count or nodes[root]["kind"] != 0:
        raise AssertionError("Invalid Minecraft command tree root")
    return sorted(nodes[child]["name"] for child in nodes[root]["children"])


def query_offline_login(host, port, timeout=5, commands=False):
    """Reach Login Success and Game Join in the disposable offline test world."""
    started = time.monotonic()
    address = host.encode("utf-8")
    handshake = b"\x00" + varint(763) + varint(len(address)) + address + struct.pack(">H", port) + b"\x02"
    username = b"MeldriteProbe"
    login = b"\x00" + varint(len(username)) + username + b"\x00"
    compressed = False
    logged_in = False
    result = {"queryCount": 0}
    command_tree = False
    completion = 0
    command_executed = False
    completions = [("/probealias ", ["alpha", "beta"]),
                   ("/probealias alpha ", ["alpha", "beta"]), ("/probealias a", ["alpha"])]
    with socket.create_connection((host, port), timeout=timeout) as connection:
        connection.settimeout(timeout)
        connection.sendall(varint(len(handshake)) + handshake + varint(len(login)) + login)

        def send_play(payload):
            wire = b"\x00" + payload if compressed else payload
            connection.sendall(varint(len(wire)) + wire)

        def request_completion(index):
            partial = completions[index][0].encode("utf-8")
            send_play(b"\x09" + varint(index + 1) + varint(len(partial)) + partial)

        with connection.makefile("rb") as stream:
            for _ in range(2000):
                remaining = timeout - (time.monotonic() - started)
                if remaining <= 0:
                    raise TimeoutError("Minecraft login did not reach Game Join")
                connection.settimeout(remaining)
                frame = read_frame(stream)
                if compressed:
                    wrapped = io.BytesIO(frame)
                    size = read_varint(wrapped)
                    frame = zlib.decompress(wrapped.read()) if size else wrapped.read()
                    if size and len(frame) != size:
                        raise AssertionError("Invalid compressed Minecraft frame")
                data = io.BytesIO(frame)
                packet = read_varint(data)
                if logged_in:
                    if packet == 0x28:  # Native 1.20.1 GameJoinS2CPacket.
                        result.update({"joined": True, "entityId": struct.unpack(">i", read_exact(data, 4))[0],
                                       "totalMs": round((time.monotonic() - started) * 1000, 3)})
                        if not commands:
                            return result
                    elif commands and packet == 0x10:  # CommandTreeS2CPacket.
                        roots = read_command_roots(data)
                        expected = {"declaredprobe", "implicitprobe", "probealias", "commandfixture:probealias", "fabricprobe"}
                        if not expected.issubset(roots) or "secureprobe" in roots or "commandfixture:secureprobe" in roots:
                            raise AssertionError(f"Wrong command visibility for non-op client: {roots}")
                        result["commandRoots"] = roots
                        command_tree = True
                        request_completion(0)
                        command = b"probealias from-wire"
                        send_play(b"\x04" + varint(len(command)) + command
                                  + struct.pack(">qq", int(time.time() * 1000), 0)
                                  + b"\x00\x00\x00\x00\x00")  # No signatures or last-seen messages.
                    elif commands and packet == 0x0F:  # CommandSuggestionsS2CPacket.
                        transaction, start, length = read_varint(data), read_varint(data), read_varint(data)
                        values = []
                        for _ in range(read_varint(data)):
                            values.append(read_string(data))
                            if read_exact(data, 1) != b"\x00":
                                read_string(data)  # Optional tooltip component.
                        partial, expected = completions[completion]
                        if transaction != completion + 1 or values != expected or start != partial.rfind(" ") + 1:
                            raise AssertionError(f"Wrong wire command completion: {transaction}, {start}, {length}, {values}")
                        result.setdefault("completions", []).append({"input": partial, "start": start, "length": length, "values": values})
                        completion += 1
                        if completion < len(completions):
                            request_completion(completion)
                    elif commands and packet == 0x64:  # GameMessageS2CPacket.
                        message = read_string(data)
                        if "command-fixture-ok:probealias:from-wire" in message:
                            command_executed = True
                            result["commandExecuted"] = True
                    elif commands and packet == 0x23:
                        send_play(b"\x12" + read_exact(data, 8))  # Native keep-alive reply.
                    elif commands and packet == 0x3C:
                        read_exact(data, 33)  # Position, rotation and relative-coordinate flags.
                        send_play(b"\x00" + varint(read_varint(data)))  # Teleport confirmation.
                    if packet == 0x1A:
                        raise AssertionError("Disconnected before Game Join: " + data.read().decode("utf-8", "replace"))
                    if commands and command_tree and completion == len(completions) and command_executed:
                        result["commandTotalMs"] = round((time.monotonic() - started) * 1000, 3)
                        return result
                elif packet == 3:
                    result["compressionThreshold"] = read_varint(data)
                    compressed = True
                elif packet == 4:
                    transaction = read_varint(data)
                    result["queryCount"] += 1
                    # An unrecognised login query receives vanilla's negative
                    # response. The probe contains no mod-specific channels.
                    response = b"\x02" + varint(transaction) + b"\x00"
                    wire = b"\x00" + response if compressed else response
                    connection.sendall(varint(len(wire)) + wire)
                elif packet == 2:
                    result["uuid"] = str(uuid.UUID(bytes=read_exact(data, 16)))
                    returned_name = read_exact(data, read_varint(data))
                    if returned_name != username:
                        raise AssertionError("Login returned an unexpected player name")
                    logged_in = True
                elif packet == 0:
                    raise AssertionError("Minecraft login rejected: " + data.read().decode("utf-8", "replace"))
                else:
                    raise AssertionError(f"Unexpected Minecraft login packet: {packet}")
    raise AssertionError("Minecraft login did not reach Game Join")


def boot(args, directory, phase):
    command_report = directory / "command-probe.json"
    if args.command_probe:
        command_report.unlink(missing_ok=True)
    with socket.socket() as sock:
        sock.bind(("127.0.0.1", 0))
        port = sock.getsockname()[1]
    command = [str(args.java), "-Xms1G", "-Xmx6G", "-XX:ActiveProcessorCount=2",
               "-Djava.awt.headless=true", "-jar", "server test.jar", "nogui", "--port", str(port)]
    (directory / f"{phase}-command.json").write_text(json.dumps(command, indent=2))
    ready = threading.Event()
    observations = []
    environment = os.environ.copy()
    # The relocated artifact must not need this checkout or a build-time cache.
    environment.pop("CLASSPATH", None)
    environment.pop("MELDRITE_RUNTIME_NAMESPACE", None)
    process = subprocess.Popen(command, cwd=directory, env=environment, stdin=subprocess.PIPE,
                               stdout=subprocess.PIPE, stderr=subprocess.STDOUT, text=True,
                               encoding="utf-8", errors="replace", start_new_session=True)

    def collect():
        with (directory / f"{phase}-stdout.log").open("w", encoding="utf-8") as output:
            for line in process.stdout:
                output.write(line)
                output.flush()
                observations.append(line)
                event = meldrite_log_event(line)
                if event and event.get("meldrite") == "status" and event.get("value") == "READY":
                    ready.set()

    reader = threading.Thread(target=collect)
    reader.start()
    stopped = False
    network = []
    login = None
    commands = None
    network_error = None
    deadline = time.monotonic() + args.timeout
    try:
        while process.poll() is None and time.monotonic() < deadline:
            if ready.wait(0.1):
                try:
                    if args.command_probe:
                        if not command_report.is_file():
                            continue
                        commands = json.loads(command_report.read_text())
                        if commands.get("status") != "PASS":
                            raise AssertionError(f"Native command checks failed: {commands}")
                    network.append(await_first_status("127.0.0.1", port, args.network_timeout))
                    network.append(query_status("127.0.0.1", port, args.network_timeout))
                    if args.login_probe:
                        login = query_offline_login("127.0.0.1", port, args.network_timeout, args.command_probe)
                except Exception as failure:
                    network_error = f"{type(failure).__name__}: {failure}"
                process.stdin.write("save-all flush\nstop\n")
                process.stdin.flush()
                stopped = True
                break
        if not stopped and process.poll() is None:
            process.terminate()
        try:
            code = process.wait(timeout=60)
        except subprocess.TimeoutExpired:
            os.killpg(process.pid, 9)
            code = process.wait(timeout=10)
    finally:
        if process.poll() is None:
            os.killpg(process.pid, 9)
            process.wait()
        reader.join(timeout=10)
    output = "".join(observations)
    result = {"ready": ready.is_set(), "stopSent": stopped, "exitCode": code,
              "stopped": any((event := meldrite_log_event(line)) and event.get("meldrite") == "status"
                             and event.get("value") == "STOPPED" for line in observations),
              "eulaRequested": "You need to agree to the EULA" in output,
              "runtime": None, "network": network, "login": login, "networkError": network_error}
    if args.command_probe:
        if commands is None and command_report.is_file():
            commands = json.loads(command_report.read_text())
        result["commands"] = commands
    for line in observations:
        event = meldrite_log_event(line)
        if event and event.get("meldrite") == "runtime":
            result["runtime"] = event
    (directory / f"{phase}-result.json").write_text(json.dumps(result, indent=2))
    print(phase, result, flush=True)
    if args.eula is None:
        if code != 0 or result["ready"] or not result["eulaRequested"]:
            raise AssertionError(f"Expected vanilla EULA request; see {directory}")
        if not re.search(r"^eula=false$", (directory / "eula.txt").read_text(), re.MULTILINE):
            raise AssertionError("Launcher must not accept the EULA")
    elif code != 0 or not result["ready"] or not result["stopped"] or len(network) != 2 or network_error:
        raise AssertionError(f"Executable server failed; see {directory}")
    elif args.command_probe and (commands is None or commands.get("status") != "PASS"):
        raise AssertionError(f"Native command checks failed; see {directory}")
    return result


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--jar", type=Path, required=True)
    parser.add_argument("--java", type=Path, required=True)
    parser.add_argument("--eula", type=Path)
    parser.add_argument("--mods", type=Path)
    parser.add_argument("--plugins", type=Path)
    parser.add_argument("--timeout", type=int, default=300)
    parser.add_argument("--network-timeout", type=float, default=5)
    parser.add_argument("--login-probe", action="store_true",
                        help="Verify offline Login Success and Game Join in the copied test world")
    parser.add_argument("--command-probe", action="store_true",
                        help="Wait for the test-only Bukkit/native command fixtures and verify their report")
    parser.add_argument("--report", type=Path, default=ROOT / "build/reports/meldrite/executable-server")
    args = parser.parse_args()
    for name in ("jar", "java", "eula", "mods", "plugins", "report"):
        value = getattr(args, name)
        if value is not None:
            setattr(args, name, value.resolve())
    args.report.mkdir(parents=True, exist_ok=True)
    directory = Path(tempfile.mkdtemp(prefix="relocated server ", dir=args.report))
    print("Test directory:", directory, flush=True)
    shutil.copy2(args.jar, directory / "server test.jar")
    for name in ("mods", "plugins"):
        source = getattr(args, name)
        if source is not None:
            shutil.copytree(source, directory / name)
    if args.eula is not None:
        if not re.search(r"^\s*eula\s*=\s*true\s*$", args.eula.read_text(), re.MULTILINE):
            raise ValueError("Provide an existing accepted EULA")
        shutil.copy2(args.eula, directory / "eula.txt")
    (directory / "server.properties").write_text(
        "online-mode=false\nview-distance=2\nsimulation-distance=2\nlevel-seed=0\n"
        "level-type=minecraft:flat\ngenerate-structures=false\nmotd=Executable server test\n")
    results = {"initial": boot(args, directory, "initial")}
    if args.eula is not None:
        level = directory / "world/level.dat"
        if not level.is_file():
            raise AssertionError("Server did not save the test world")
        before = (directory / "server.properties").read_text().splitlines()
        (directory / "before-restart-server.properties").write_text("\n".join(before) + "\n")
        results["restart"] = boot(args, directory, "restart")
        after = (directory / "server.properties").read_text().splitlines()
        # java.util.Properties.store updates the date comment on every vanilla startup.
        configuration_before = sorted(line for line in before if line and not line.startswith(("#", "!")))
        configuration_after = sorted(line for line in after if line and not line.startswith(("#", "!")))
        if configuration_after != configuration_before:
            raise AssertionError("Restart changed the server configuration:\n" + "\n".join(
                difflib.unified_diff(configuration_before, configuration_after)))
    report = {"jar": str(args.jar), "sha256": hashlib.sha256(args.jar.read_bytes()).hexdigest(),
              "serverDirectory": str(directory), "phases": results}
    (directory / "result.json").write_text(json.dumps(report, indent=2))
    print("Verified:", directory / "result.json", flush=True)


if __name__ == "__main__":
    main()
