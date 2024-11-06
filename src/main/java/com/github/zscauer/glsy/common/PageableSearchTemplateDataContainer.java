package com.github.zscauer.glsy.common;

import com.github.zscauer.glsy.security.AccessProfile;
import io.quarkus.qute.TemplateData;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

import java.util.List;

/**
 * Container of content for templates, those supports filtration / pageable representation of content.
 */
@TemplateData
@Getter
@Setter
@Accessors(fluent = true, chain = true)
@FieldDefaults(level = AccessLevel.PROTECTED, makeFinal = true)
public final class PageableSearchTemplateDataContainer<T> extends PageableTemplateDataContainer<T> {

    String searchString;

    private PageableSearchTemplateDataContainer(final String searchString,
                                                final List<T> content,
                                                final short currentPage,
                                                final String changePageEndpoint,
                                                final short pageSize,
                                                final AccessProfile accessProfile) {
        super(content, currentPage, changePageEndpoint, pageSize, accessProfile);
        this.searchString = searchString;
    }

    public static <T> PageableSearchTemplateDataContainer<T> wrapContent(final String searchString,
                                                                         final List<T> content,
                                                                         final short currentPage,
                                                                         final String changePageEndpoint,
                                                                         final short pageSize,
                                                                         final AccessProfile accessProfile) {
        return new PageableSearchTemplateDataContainer<>(searchString, content, currentPage, changePageEndpoint, pageSize, accessProfile);
    }

}
