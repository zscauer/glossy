package com.github.zscauer.glsy.security;

import io.quarkus.qute.TemplateEnum;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

@TemplateEnum
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@SuppressWarnings("unused")
public enum AccessibleContent {

    INFORMATION_NOTE,
    INFORMATION_NOTE_HISTORY,
    TAG

}
