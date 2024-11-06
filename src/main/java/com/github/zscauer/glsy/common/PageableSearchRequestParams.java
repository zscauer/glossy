package com.github.zscauer.glsy.common;

import jakarta.ws.rs.QueryParam;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

/**
 * Parameters for requests, that needs to represent filtered content.
 */
@Getter
@Setter
@Accessors(fluent = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public final class PageableSearchRequestParams extends PageableRequestParams {

    @QueryParam("inputed-value")
    String searchString;

    private static final String PATH_PARAMETER_SEARCH_STRING = "%ssearchString=%s&";

    @Override
    public void validate() {
        super.validate();
        if (searchString != null && searchString.isBlank()) {
            searchString = null;
        }
    }

    @Override
    public String parameterizedPath(final String currentPath) {
        if (searchString != null) {
            return PATH_PARAMETER_SEARCH_STRING.formatted(super.parameterizedPath(currentPath), searchString);
        }
        return super.parameterizedPath(currentPath);
    }

}
