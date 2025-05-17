package com.github.zscauer.glsy.common.api;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import lombok.NonNull;

import java.util.Objects;

public record ApiError(
        @NonNull ApiErrorCategory category,
        @Nonnull String message
) {

    public ApiError(@Nullable final ApiErrorCategory category) {
        this(category, null);
    }

    public ApiError(@Nullable final ApiErrorCategory category, @Nullable final String message) {
        this.category = Objects.requireNonNullElse(category, ApiErrorCategory.UNKNOWN_ERROR);
        this.message = Objects.requireNonNullElse(message, "");
    }

}
