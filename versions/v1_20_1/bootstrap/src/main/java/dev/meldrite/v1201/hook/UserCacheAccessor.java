package dev.meldrite.v1201.hook;

import java.util.Map;
public interface UserCacheAccessor {
    Map<String, ?> meldrite$profilesByName();
    long meldrite$nextProfileAccess();
}
