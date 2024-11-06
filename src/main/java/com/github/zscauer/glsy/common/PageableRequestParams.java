package com.github.zscauer.glsy.common;

import com.github.zscauer.glsy.tools.Constants;
import jakarta.ws.rs.QueryParam;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

/**
 * Parameters for requests, that uses pages for represent content.
 */
@Getter
@Setter
@Accessors(fluent = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public sealed class PageableRequestParams extends RequestParams permits PageableSearchRequestParams {

    @QueryParam("pageSize")
    short pageSize = DEFAULT_PAGE_SIZE;
    @QueryParam("page")
    short page = DEFAULT_PAGE;

    private static short DEFAULT_PAGE = 1;
    private static short DEFAULT_PAGE_SIZE = 10;

    private static final String PATH_PARAMETER_PAGE = "%spage=%s&";
    private static final String PATH_PARAMETER_PAGE_SIZE = "%spageSize=%s&";

    @Override
    public void validate() {
        super.validate();
        if (page < 1) {
            this.page = 1;
        }
        if (pageSize > Constants.maxPageSize()) {
            pageSize = Constants.maxPageSize();
        }
    }

    @Override
    public String parameterizedPath(final String currentPath) {
        String parameterizedPath = super.parameterizedPath(currentPath);
        if (page > DEFAULT_PAGE) {
            parameterizedPath = PATH_PARAMETER_PAGE.formatted(parameterizedPath, page);
        }
        if (pageSize != DEFAULT_PAGE_SIZE) {
            parameterizedPath = PATH_PARAMETER_PAGE_SIZE.formatted(parameterizedPath, pageSize);

        }
        return parameterizedPath;
    }

}
