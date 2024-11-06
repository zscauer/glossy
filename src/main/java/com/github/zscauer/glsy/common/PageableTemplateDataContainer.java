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
 * Container of content for templates, those supports pageable representation of content.
 */
@TemplateData
@Getter
@Setter
@Accessors(fluent = true, chain = true)
@FieldDefaults(level = AccessLevel.PROTECTED, makeFinal = true)
public sealed class PageableTemplateDataContainer<T> extends TemplateDataContainer<List<T>> permits PageableSearchTemplateDataContainer {

    int pageSize;
    int previousPage;
    int currentPage;
    int nextPage;
    boolean lastPage;
    String changePageEndpoint;

    protected PageableTemplateDataContainer(final List<T> iterableContent,
                                            final short currentPage,
                                            final String changePageEndpoint,
                                            final short pageSize,
                                            final AccessProfile accessProfile) {
        super(iterableContent, accessProfile);
        if (content != null) {
            this.lastPage = content.size() <= pageSize;
            while (content.size() > pageSize) {
                content.remove(content.getLast());
            }
        } else {
            this.lastPage = true;
        }

        this.pageSize = pageSize;
        this.previousPage = currentPage > 0 ? currentPage - 1 : 0;
        this.currentPage = currentPage;
        this.nextPage = currentPage + 1;
        this.changePageEndpoint = changePageEndpoint;
    }

    public static <T> PageableTemplateDataContainer<T> wrapContent(final List<T> content,
                                                                   final short currentPage,
                                                                   final String changePageEndpoint,
                                                                   final short pageSize,
                                                                   final AccessProfile accessProfile) {
        return new PageableTemplateDataContainer<>(content, currentPage, changePageEndpoint, pageSize, accessProfile);
    }

}
