package com.github.zscauer.glsy.i18n;

import jakarta.ws.rs.core.NewCookie;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Representation of user language for localization.
 */
@Getter
@Accessors(fluent = true)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public enum Language {

    en,
    ru;

    NewCookie languageCookie = new NewCookie.Builder(COOKIE_USER_LANGUAGE_KEY).value(name()).path("/").build();

    private static final int DEFAULT_LANGUAGE_CACHE_SIZE = 50;
    private static final Language DEFAULT_LANGUAGE = Language.en;
    private static final Map<String, Language> LANGUAGE_CACHE = new ConcurrentHashMap<>(DEFAULT_LANGUAGE_CACHE_SIZE);
    public static final String COOKIE_USER_LANGUAGE_KEY = "GLSY-USER-LANGUAGE";

    public final boolean is(final String lang) {
        if (lang == null) {
            return false;
        }
        return this.name().equalsIgnoreCase(lang);
    }

    public final Locale toLocale() {
        return Locale.of(name());
    }

    public static Language defaultLanguage() {
        return DEFAULT_LANGUAGE;
    }

    public static Language fromLocale(final Locale locale) {
        if (locale != null && locale.getLanguage().equalsIgnoreCase(Language.ru.name())) {
            return Language.ru;
        } else {
            return DEFAULT_LANGUAGE;
        }
    }

    public static Language fromHeader(final String acceptedLang) {
        if (acceptedLang == null || acceptedLang.isBlank() || acceptedLang.length() < 2) {
            return DEFAULT_LANGUAGE;
        }

        if (LANGUAGE_CACHE.containsKey(acceptedLang)) {
            return LANGUAGE_CACHE.get(acceptedLang);
        }

        refreshCache();
        try {
            return LANGUAGE_CACHE.computeIfAbsent(acceptedLang, land -> fromLocale(Locale.of(acceptedLang.substring(0, 2))));
        } catch (Exception e) {
            return DEFAULT_LANGUAGE;
        }
    }

    private static void refreshCache() {
        if (LANGUAGE_CACHE.size() >= DEFAULT_LANGUAGE_CACHE_SIZE) {
            LANGUAGE_CACHE.clear();
        }
    }

}
