package com.github.zscauer.glsy.i18n;

import com.github.zscauer.glsy.knowledges.AppMessagesKnowledges;
import com.github.zscauer.glsy.security.AppMessagesSecurity;
import io.quarkus.qute.i18n.Localized.Literal;
import io.quarkus.qute.i18n.MessageBundles;

import java.util.HashMap;
import java.util.Map;

/**
 * Contains all available localized message sources.
 */
public final class MessagesProvider {

    private static final Map<Language, Literal> LITERALS_CACHE = new HashMap<>(Language.values().length);

    static {
        for (final Language language : Language.values()) {
            LITERALS_CACHE.put(language, Literal.of(language.name()));
        }
    }

    public static AppMessagesGlobal global(final Language language) {
        return MessageBundles.get(AppMessagesGlobal.class, LITERALS_CACHE.getOrDefault(language, Literal.of(language.name())));
    }

    public static AppMessagesKnowledges knowledges(final Language language) {
        return MessageBundles.get(AppMessagesKnowledges.class, LITERALS_CACHE.getOrDefault(language, Literal.of(language.name())));
    }

    public static AppMessagesSecurity security(final Language language) {
        return MessageBundles.get(AppMessagesSecurity.class, LITERALS_CACHE.getOrDefault(language, Literal.of(language.name())));
    }

}
