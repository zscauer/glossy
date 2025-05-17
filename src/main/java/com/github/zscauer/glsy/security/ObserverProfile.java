package com.github.zscauer.glsy.security;

import com.github.zscauer.glsy.common.RequestParams;
import jakarta.annotation.Nullable;
import jakarta.inject.Singleton;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import org.jboss.resteasy.reactive.RestResponse.ResponseBuilder;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Getter
@Singleton
@Accessors(fluent = true)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@SuppressWarnings("unused")
public final class ObserverProfile extends AccessProfile {

    AccessMode accessMode = AccessMode.OBSERVER;

    private static final Map<AccessibleContent, Privilege> PRIVILEGES = new ConcurrentHashMap<>(2);

    ObserverProfile() {}

    @Override
    public boolean authorized(@Nullable final RequestParams requestParams) {
        return true;
    }

    @Override
    public boolean havePrivilegesToContent(AccessibleContent requestedContent, Privilege requestedPrivilege) {
        if (PRIVILEGES.isEmpty()) {
            fillPrivileges(PRIVILEGES, accessMode);
        }

        final Privilege contentPrivilege = PRIVILEGES.get(requestedContent);
        return contentPrivilege != null && contentPrivilege.enough(requestedPrivilege);
    }

    @Override
    public void grant(final String secret,
                      final RequestParams requestParams,
                      final ResponseBuilder<?> responseBuilder) {
        // base access mode, nothing to grant.
    }

    @Override
    public void revoke(final ResponseBuilder<?> responseBuilder) {
        // base access mode, nothing to revoke.
    }

}
