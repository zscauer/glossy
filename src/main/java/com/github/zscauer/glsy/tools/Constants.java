package com.github.zscauer.glsy.tools;

import com.github.zscauer.glsy.common.TemplateUsage;
import io.quarkus.qute.TemplateGlobal;
import io.quarkus.runtime.Startup;
import jakarta.inject.Singleton;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Getter
@Startup
@Singleton
@Accessors(fluent = true)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class Constants {

    public static final boolean IS_NATIVE_EXECUTION = System.getProperty("org.graalvm.nativeimage.imagecode") != null;

    private static String SERVICE_VERSION;
    private static short MAX_PAGE_SIZE;
    private static boolean INFORMATION_NOTES_HISTORY_TRACKING_ENABLED;

    public Constants(@ConfigProperty(name = "quarkus.application.version") final String version,
                     @ConfigProperty(name = "configuration.limits.max-page-size", defaultValue = "40") final short maxPageSize,
                     @ConfigProperty(name = "configuration.history.information-notes.enabled", defaultValue = "true") final boolean informationNotesHistoryTrackingEnabled) {
        SERVICE_VERSION = version;
        MAX_PAGE_SIZE = maxPageSize;
        INFORMATION_NOTES_HISTORY_TRACKING_ENABLED = informationNotesHistoryTrackingEnabled;
    }

    public static short maxPageSize() {
        return MAX_PAGE_SIZE;
    }

    public static boolean informationNotesHistoryTrackingEnabled() {
        return INFORMATION_NOTES_HISTORY_TRACKING_ENABLED;
    }

    @TemplateUsage
    @SuppressWarnings("unused")
    @TemplateGlobal(name = "getServiceVersion")
    public static String serviceVersion() {
        return SERVICE_VERSION;
    }

    @TemplateUsage
    @TemplateGlobal(name = "getCurrentTimeInUTC")
    public static LocalDateTime getCurrentTimeInUTC() {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(System.currentTimeMillis()), ZoneId.of("UTC"));
    }

}
