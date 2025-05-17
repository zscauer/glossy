package com.github.zscauer.glsy.common.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

/**
 * Represents result of API operation.
 *
 * @param error  if operation have error, it will be the only content of response
 * @param result can be returned only if operation completes without errors
 * @param <T>    type of response
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(
        @Nullable T result,
        @Nullable ApiError error
) {

    private static final ApiError UNKNOWN_ERROR = new ApiError(ApiErrorCategory.UNKNOWN_ERROR);

    public ApiResponse(@Nonnull T result) {
        this(result, null);
    }

    public ApiResponse(@Nonnull ApiError error) {
        this(null, error);
    }

    public ApiResponse(@Nullable T result, @Nullable ApiError error) {
        if ((result == null && error == null) || (result != null && error != null)) {
            this.result = null;
            this.error = UNKNOWN_ERROR;
        } else {
            this.result = result;
            this.error = error;
        }
    }

}
