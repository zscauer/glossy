package com.github.zscauer.glsy.security;

import com.github.zscauer.glsy.common.RequestParams;
import com.github.zscauer.glsy.common.TemplateDataContainer;
import io.quarkus.arc.All;
import io.quarkus.qute.TemplateExtension;
import jakarta.annotation.Nullable;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.jboss.resteasy.reactive.RestResponse.ResponseBuilder;

import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

@Singleton
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public final class SecurityProvider {

    TreeMap<AccessMode, AccessProfile> orderedModes = new TreeMap<>((mode1, mode2) -> mode2.getPriority() - mode1.getPriority());

    private static final AccessMode DEFAULT_ACCESS_MODE = AccessMode.DEFAULT_MODE;
    private static final Set<AccessMode> SELECTABLE_ACCESS_MODES = new TreeSet<>();

    @Inject
    @SuppressWarnings("unused")
    public SecurityProvider(@All final List<AccessProfile> accesses) {
        SELECTABLE_ACCESS_MODES.addAll(AccessMode.selectableAccessModes());
        accesses.forEach(access -> orderedModes.put(access.accessMode(), access));
        if (!orderedModes.containsKey(DEFAULT_ACCESS_MODE)) {
            throw new IllegalStateException("Default access mode not found.");
        }
    }

    @TemplateExtension(namespace = "security")
    @SuppressWarnings("unused")
    public static Set<AccessMode> selectableAccessModes() {
        return SELECTABLE_ACCESS_MODES;
    }

    @TemplateExtension(namespace = "security")
    @SuppressWarnings("unused")
    public static boolean havePrivileges(@SuppressWarnings("rawtypes") @Nullable final TemplateDataContainer dataContainer,
                                         final AccessibleContent requestedContent,
                                         final Privilege requestedPrivilege) {
        return dataContainer != null && dataContainer.accessProfile() != null
                && dataContainer.accessProfile().havePrivilegesToContent(requestedContent, requestedPrivilege);
    }

    public AccessProfile define(final RequestParams requestParams) {
        AccessProfile result = orderedModes.get(DEFAULT_ACCESS_MODE);
        for (final AccessProfile access : orderedModes.sequencedValues()) {
            if (access.authorized(requestParams)) {
                result = access;
                break;
            }
        }

        return result;
    }

    public void grant(final AccessMode accessMode,
                      final String inputedSecret,
                      final RequestParams requestParams,
                      final ResponseBuilder<?> responseBuilder) {
        if (accessMode == null || DEFAULT_ACCESS_MODE.equals(accessMode)) {
            revoke(responseBuilder);
        }
        orderedModes.getOrDefault(accessMode, orderedModes.get(DEFAULT_ACCESS_MODE)).grant(inputedSecret, requestParams, responseBuilder);
    }

    public void revoke(final ResponseBuilder<?> responseBuilder) {
        orderedModes.values().forEach(contentAccess -> contentAccess.revoke(responseBuilder));
    }

}
