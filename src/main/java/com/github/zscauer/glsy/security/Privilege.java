package com.github.zscauer.glsy.security;

import io.quarkus.qute.TemplateEnum;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

/**
 * Type of executed operation.
 * <p>
 * Each upper privilege including lower privilges automatically (by priority):
 * {@linkplain #DELETE} -> {@linkplain #CREATE} -> {@linkplain #UPDATE} -> {@linkplain #READ}.
 */
@TemplateEnum
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@SuppressWarnings("unused")
public enum Privilege {

    READ((short) 0),
    UPDATE((short) 1),
    CREATE((short) 2),
    DELETE((short) 3);

    short priority;

    public final boolean enough(final Privilege requiredPrivilege) {
        return priority >= requiredPrivilege.priority;
    }

}
