package com.github.zscauer.glsy.security;

import com.github.zscauer.glsy.common.RequestParams;
import com.github.zscauer.glsy.i18n.MessagesProvider;
import io.quarkus.qute.TemplateData;
import jakarta.annotation.Nullable;
import jakarta.inject.Singleton;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.resteasy.reactive.RestResponse.ResponseBuilder;

import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@TemplateData
@Getter
@Singleton
@Accessors(fluent = true)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public final class AdminProfile extends AccessProfile {

    AccessMode accessMode = AccessMode.ADMIN;
    byte[] salt = new SecureRandom().generateSeed(16);

    private static final Map<AccessibleContent, Privilege> PRIVILEGES = new ConcurrentHashMap<>(2);
    private static String SECRET;

    public AdminProfile(@ConfigProperty(name = "configuration.security.access-mode.admin.secret") final String secret) throws InvalidKeySpecException {
        SECRET = generateEncryptedSecret(secret, salt);
    }

    @Override
    public boolean authorized(@Nullable final RequestParams requestParams) {
        if (requestParams != null) {
            return secretsAreIdentical(requestParams.accessModeSecret());
        }
        return false;
    }

    @Override
    public boolean havePrivilegesToContent(final AccessibleContent requestedContent, final Privilege requestedPrivilege) {
        if (PRIVILEGES.isEmpty()) {
            fillPrivileges(PRIVILEGES, accessMode);
        }

        final Privilege contentPrivilege = PRIVILEGES.get(requestedContent);
        return contentPrivilege != null && contentPrivilege.enough(requestedPrivilege);
    }

    @Override
    public void grant(final String secret,
                      final RequestParams requestParams,
                      final ResponseBuilder<?> responseBuilder) throws GrantAccessException {
        if (secret == null || secret.isBlank()) {
            throw new GrantAccessException(MessagesProvider.security(requestParams.userLanguage()).secret_not_provided_error());
        }

        try {
            if (!inputedSecretIsCorrect(createNewPBEKeySpec(secret, salt), SECRET)) {
                throw new GrantAccessException(MessagesProvider.security(requestParams.userLanguage()).secret_not_matched_error());
            }
        } catch (final InvalidKeySpecException e) {
            log.error(e.getMessage());
            throw new GrantAccessException(MessagesProvider.security(requestParams.userLanguage()).unable_to_grant_privileges());
        }

        addAccessCookie(SECRET, responseBuilder);
    }

    @Override
    public void revoke(final ResponseBuilder<?> responseBuilder) {
        addAccessCookie(null, responseBuilder);
    }

    private static boolean secretsAreIdentical(@Nullable final String inputedSecret) {
        if (inputedSecret == null) {
            return false;
        }

        return inputedSecret.equals(SECRET);
    }

}
