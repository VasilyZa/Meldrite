package dev.meldrite.remap;

import java.util.Set;

public final class InternalSymbolVerifier {
    public enum Status { API_CONTRACT, MISSING_TARGET_MEMBER }

    public Status verify(String owner, String member, Set<String> availableMembers) {
        if (owner == null || member == null || availableMembers == null) return Status.MISSING_TARGET_MEMBER;
        return availableMembers.contains(owner + '#' + member) ? Status.API_CONTRACT : Status.MISSING_TARGET_MEMBER;
    }
}
