package com.github.zscauer.glsy;

import com.github.zscauer.glsy.common.CommonTemplates;
import com.github.zscauer.glsy.common.RequestParams;
import com.github.zscauer.glsy.common.ResourcePaths;
import com.github.zscauer.glsy.common.TemplateDataContainer;
import com.github.zscauer.glsy.i18n.Language;
import com.github.zscauer.glsy.security.SecurityProvider;
import com.github.zscauer.glsy.tools.Templating;
import io.quarkus.qute.CheckedTemplate;
import io.quarkus.qute.TemplateInstance;
import io.vertx.core.http.HttpServerRequest;
import jakarta.annotation.Nullable;
import jakarta.inject.Singleton;
import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.RestResponse.ResponseBuilder;
import org.jboss.resteasy.reactive.RestResponse.Status;

import java.util.Locale;

import static com.github.zscauer.glsy.common.ResourcePaths.PATH_RESOURCE_MAIN;

@Slf4j
@Singleton
@Path(PATH_RESOURCE_MAIN)
@Produces(MediaType.TEXT_HTML)
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@SuppressWarnings("unused")
public final class MainResource {

    SecurityProvider securityProvider;

    @Context
    HttpServerRequest request;

    private static final String PATH_CHANGE_LANGUAGE = "/language";
    private static final String PATH_NAVIGATION_MOBILE = "/navigation/mobile";
    private static final String PATH_BLANK = "/blank";
    private static final String PATH_CONSTRUCTION = "/construction";
    private static final String PATH_NOT_IMPLEMENTED = "/not_implemented";

    @CheckedTemplate(basePath = "", defaultName = CheckedTemplate.HYPHENATED_ELEMENT_NAME)
    private static class Templates {
        private static native TemplateInstance SPA(TemplateDataContainer<?> dataContainer);
    }

    public static TemplateInstance spaTemplateLocalized(@Nullable final TemplateDataContainer<?> dataContainer,
                                                        @Nullable final RequestParams requestParams) {
        return Templating.localized(Templates.SPA(dataContainer), requestParams == null ? Language.defaultLanguage() : requestParams.userLanguage());
    }

    @GET
    public RestResponse<TemplateInstance> index(@BeanParam final RequestParams requestParams) {
        final ResponseBuilder<TemplateInstance> responseBuilder = createResponseBuilder(requestParams);
        final var dataContainer = new TemplateDataContainer<>(null, securityProvider.define(requestParams));

        return responseBuilder.entity(spaTemplateLocalized(dataContainer, requestParams)).status(RestResponse.Status.OK).build();
    }

    @PUT
    @Path(PATH_CHANGE_LANGUAGE + "/{language}")
    public RestResponse<TemplateInstance> changeLanguage(@PathParam("language") String language,
                                                         @BeanParam final RequestParams requestParams) {
        final Language selectedLanguage = Language.fromLocale(Locale.of(language));
        requestParams.userLanguage(selectedLanguage);
        final ResponseBuilder<TemplateInstance> responseBuilder = createResponseBuilder(requestParams);
        responseBuilder.header("HX-Refresh", "true");
        TemplateInstance result = CommonTemplates.blank();

        return responseBuilder.entity(result).status(RestResponse.Status.OK).build();
    }

    @GET
    @Path(PATH_NAVIGATION_MOBILE)
    public RestResponse<TemplateInstance> mobileNavigation(@BeanParam final RequestParams requestParams) {
        final ResponseBuilder<TemplateInstance> responseBuilder = createResponseBuilder(requestParams);
        TemplateInstance result;

        final var dataContainer = new TemplateDataContainer<>(null, securityProvider.define(requestParams));
        if (!requestParams.hxRequest()) {
            dataContainer.redirectContent("%s%s".formatted(ResourcePaths.PATH_RESOURCE_MAIN, PATH_NAVIGATION_MOBILE));
            result = spaTemplateLocalized(dataContainer, requestParams);
        } else {
            result = Templating.localized(CommonTemplates.navigationMobile(dataContainer), requestParams.userLanguage());
        }

        return responseBuilder.entity(result).status(RestResponse.Status.OK).build();
    }

    @GET
    @Path(PATH_BLANK)
    public RestResponse<TemplateInstance> blank(@BeanParam final RequestParams requestParams) {
        final ResponseBuilder<TemplateInstance> responseBuilder = createResponseBuilder(requestParams);
        TemplateInstance result;

        if (!requestParams.hxRequest()) {
            final var dataContainer = new TemplateDataContainer<>(null, securityProvider.define(requestParams));
            dataContainer.redirectContent("%s%s".formatted(ResourcePaths.PATH_RESOURCE_MAIN, PATH_BLANK));
            result = spaTemplateLocalized(dataContainer, requestParams);
        } else {
            result = CommonTemplates.blank();
        }

        return responseBuilder.entity(result).status(RestResponse.Status.OK).build();
    }

    @GET
    @Path(PATH_CONSTRUCTION)
    public RestResponse<TemplateInstance> construction(@BeanParam final RequestParams requestParams) {
        final ResponseBuilder<TemplateInstance> responseBuilder = createResponseBuilder(requestParams);
        TemplateInstance result;

        if (!requestParams.hxRequest()) {
            final var dataContainer = new TemplateDataContainer<>(null, securityProvider.define(requestParams));
            dataContainer.redirectContent("%s%s".formatted(ResourcePaths.PATH_RESOURCE_MAIN, PATH_CONSTRUCTION));
            result = MainResource.Templates.SPA(dataContainer);
        } else {
            result = Templating.localized(CommonTemplates.construction(), requestParams.userLanguage());
        }

        return responseBuilder.entity(result).status(RestResponse.Status.OK).build();
    }

    @GET
    @Path(PATH_NOT_IMPLEMENTED)
    public RestResponse<TemplateInstance> notImplemented(@BeanParam final RequestParams requestParams) {
        final ResponseBuilder<TemplateInstance> responseBuilder = createResponseBuilder(requestParams);
        TemplateInstance result;

        if (!requestParams.hxRequest()) {
            final var dataContainer = new TemplateDataContainer<>(null, securityProvider.define(requestParams));
            dataContainer.redirectContent("%s%s".formatted(ResourcePaths.PATH_RESOURCE_MAIN, PATH_NOT_IMPLEMENTED));
            result = MainResource.Templates.SPA(dataContainer);
        } else {
            result = Templating.localized(CommonTemplates.notImplemented(), requestParams.userLanguage());
        }

        return responseBuilder.entity(result).status(RestResponse.Status.OK).build();
    }

    public static <T> ResponseBuilder<T> createResponseBuilder(final RequestParams requestParams) {
        final ResponseBuilder<T> responseBuilder = ResponseBuilder.create(Status.ACCEPTED);

        if (requestParams == null) {
            responseBuilder.cookie(Language.defaultLanguage().languageCookie());
        } else {
            requestParams.validate();
            responseBuilder.cookie(requestParams.userLanguage().languageCookie());
        }
        responseBuilder.header("Vary", "HX-Request");

        return responseBuilder;
    }

}
