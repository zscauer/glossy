package com.github.zscauer.glsy.tools;

import com.github.zscauer.glsy.i18n.Language;
import io.quarkus.qute.TemplateEnum;
import io.quarkus.qute.TemplateInstance;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.util.Objects;

/**
 * Common methods for template processing.
 */
public final class Templating {

    @TemplateEnum
    public enum TemplateDestination {
        ASIDE,
        INFORMATION_NOTE
    }

    public static TemplateInstance localized(final @Nonnull TemplateInstance template,
                                             final @Nullable Language userLanguage) {
        return Objects.requireNonNull(template)
                .setLocale(Objects.requireNonNullElseGet(userLanguage, Language::defaultLanguage).toLocale());
    }

}