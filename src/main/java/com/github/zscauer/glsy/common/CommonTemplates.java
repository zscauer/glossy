package com.github.zscauer.glsy.common;

import io.quarkus.qute.CheckedTemplate;
import io.quarkus.qute.TemplateInstance;

/**
 * Contains templates, that could be used from different places.
 */
@CheckedTemplate(basePath = "fragments/common", defaultName = CheckedTemplate.HYPHENATED_ELEMENT_NAME)
public final class CommonTemplates {
    public static native TemplateInstance navigationMobile(TemplateDataContainer<?> dataContainer);

    public static native TemplateInstance blank();

    public static native TemplateInstance construction();

    public static native TemplateInstance notImplemented();

    public static native TemplateInstance notImplementedModal();

    public static native TemplateInstance error(ErrorPageTemplateDataContainer<?> dataContainer);

}
