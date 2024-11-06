package com.github.zscauer.glsy.common;

import com.github.zscauer.glsy.security.AccessMode;
import com.github.zscauer.glsy.security.AccessProfile;
import io.quarkus.qute.TemplateData;
import jakarta.annotation.Nullable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Container of content for templates.
 */
@TemplateData
@Getter
@Accessors(fluent = true, chain = true)
@FieldDefaults(level = AccessLevel.PROTECTED)
public sealed class TemplateDataContainer<T> permits
        TemplateDataContainer.EmptyTemplateDataContainer,
        PageableTemplateDataContainer,
        ErrorPageTemplateDataContainer {

    @Setter
    @Nullable
    String redirectContent;
    @Nullable
    final T content;
    AccessProfile accessProfile;

    final List<String> errorMessages = new ArrayList<>();
    final List<String> successMessages = new ArrayList<>();

    private static final TemplateDataContainer<?> EMPTY_CONTAINER = new TemplateDataContainer<>(null, null).createEmpty();

    public TemplateDataContainer(@Nullable T content, AccessProfile accessProfile) {
        this.content = content;
        this.accessProfile = accessProfile;
    }

    public static TemplateDataContainer<?> getEmpty() {
        return EMPTY_CONTAINER;
    }

    @TemplateUsage
    @SuppressWarnings("unused")
    public final boolean isEmpty() {
        return content == null;
    }

    @TemplateUsage
    @SuppressWarnings("unused")
    public final AccessMode accessMode() {
        if (accessProfile != null) {
            return accessProfile.accessMode();
        }
        return AccessMode.DEFAULT_MODE;
    }

    @TemplateUsage
    @SuppressWarnings("unused")
    public boolean redirected() {
        return redirectContent != null && !redirectContent.isBlank();
    }

    @TemplateUsage
    @SuppressWarnings("unused")
    public boolean errorPageContainer() {
        return false;
    }

    public final TemplateDataContainer<T> addErrorMessage(final String message) {
        if (message != null) {
            errorMessages.add(message);
        }
        return this;
    }

    public final TemplateDataContainer<T> addSuccessMessage(final String message) {
        if (message != null) {
            successMessages.add(message);
        }
        return this;
    }

    @TemplateUsage
    @SuppressWarnings("unused")
    public final boolean haveMessages() {
        return !(errorMessages.isEmpty() && successMessages.isEmpty());
    }

    private TemplateDataContainer<T> createEmpty() {
        return new EmptyTemplateDataContainer<>();
    }

    public static final class EmptyTemplateDataContainer<T> extends TemplateDataContainer<T> {

        public EmptyTemplateDataContainer() {
            super(null, null);
        }

        @Override
        public List<String> errorMessages() {
            return Collections.emptyList();
        }

        @Override
        public List<String> successMessages() {
            return Collections.emptyList();
        }

        @Override
        public boolean redirected() {
            return false;
        }

        @Override
        public TemplateDataContainer<T> redirectContent(final String redirectContent) {
            return this;
        }

        @Override
        public String redirectContent() {
            return null;
        }

        @Nullable
        @Override
        public T content() {
            return null;
        }
    }

}
