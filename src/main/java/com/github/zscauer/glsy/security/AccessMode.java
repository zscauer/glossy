package com.github.zscauer.glsy.security;

import com.github.zscauer.glsy.common.TemplateUsage;
import io.quarkus.qute.TemplateEnum;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.EnumSet;
import java.util.Set;

/**
 * Role of user.
 */
@Getter
@TemplateEnum
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public enum AccessMode {

    OBSERVER((short) 0),
    MODERATOR((short) 1),
    ADMIN((short) 2);

    short priority;

    public static final AccessMode DEFAULT_MODE = OBSERVER;

    /**
     * @return all access modes, that could be selected by user to activate
     */
    static Set<AccessMode> selectableAccessModes() {
        return EnumSet.of(MODERATOR, ADMIN);
    }

    @TemplateUsage
    @SuppressWarnings("unused")
    public final boolean isDefault() {
        return this.equals(DEFAULT_MODE);
    }

}
