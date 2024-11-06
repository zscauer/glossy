package com.github.zscauer.glsy.common;

import com.github.zscauer.glsy.i18n.Language;
import jakarta.ws.rs.CookieParam;
import jakarta.ws.rs.HeaderParam;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

import static com.github.zscauer.glsy.i18n.Language.COOKIE_USER_LANGUAGE_KEY;
import static com.github.zscauer.glsy.security.AccessProfile.COOKIE_ACCESS_MODE_SECRET;

/**
 * Parameters for all requests.
 */
@Getter
@Setter
@Accessors(fluent = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public sealed class RequestParams permits PageableRequestParams {

    @HeaderParam("Accept-Language")
    String acceptedLang;
    @HeaderParam("HX-Request")
    boolean hxRequest;
    @CookieParam(COOKIE_USER_LANGUAGE_KEY)
    Language userLanguage;
    @CookieParam(COOKIE_ACCESS_MODE_SECRET)
    String accessModeSecret;

    public void validate() {
        if (userLanguage == null) {
            userLanguage = Language.fromHeader(acceptedLang);
        }
    }

    public String parameterizedPath(final String currentPath) {
        return "%s?".formatted(currentPath);
    }

}
