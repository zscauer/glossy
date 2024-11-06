package com.github.zscauer.glsy.security;

import com.github.zscauer.glsy.MainResource;
import com.github.zscauer.glsy.common.RequestParams;
import com.github.zscauer.glsy.common.TemplateDataContainer;
import com.github.zscauer.glsy.tools.Templating;
import io.quarkus.qute.CheckedTemplate;
import io.quarkus.qute.TemplateInstance;
import io.vertx.core.http.HttpServerRequest;
import jakarta.annotation.Nullable;
import jakarta.inject.Singleton;
import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.RestResponse.ResponseBuilder;

import static com.github.zscauer.glsy.common.ResourcePaths.PATH_RESOURCE_SECURITY;

@Slf4j
@Singleton
@Path(PATH_RESOURCE_SECURITY)
@Produces(MediaType.TEXT_HTML)
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@SuppressWarnings("unused")
final class SecurityResource {

    SecurityProvider securityProvider;

    @Context
    HttpServerRequest request;

    private static final String PATH_CHANGE_ACCESS_MODE_REQUEST = "/changeAccessModeRequest";
    private static final String PATH_CHANGE_ACCESS_MODE = "/changeAccessMode";

    @CheckedTemplate(basePath = "fragments/security", defaultName = CheckedTemplate.HYPHENATED_ELEMENT_NAME)
    private static class Templates {
        private static native TemplateInstance requestAccessModal(@Nullable TemplateDataContainer<?> dataContainer);
    }

    @GET
    @Path(PATH_CHANGE_ACCESS_MODE_REQUEST)
    public RestResponse<TemplateInstance> changeAccessModeRequest(@BeanParam final RequestParams requestParams) {
        final ResponseBuilder<TemplateInstance> responseBuilder = MainResource.createResponseBuilder(requestParams);
        final AccessProfile userAccess = securityProvider.define(requestParams);

        TemplateInstance result;
        if (!requestParams.hxRequest()) {
            final var dataContainer = new TemplateDataContainer<>(null, userAccess);
            result = MainResource.spaTemplateLocalized(dataContainer, requestParams);
        } else {
            result = Templating.localized(Templates.requestAccessModal(
                    new TemplateDataContainer<>(null, userAccess)), requestParams.userLanguage());
        }

        return responseBuilder.entity(result).status(RestResponse.Status.OK).build();
    }

    @PUT
    @Path(PATH_CHANGE_ACCESS_MODE)
    public RestResponse<TemplateInstance> changeAccessMode(@BeanParam final RequestParams requestParams,
                                                           @FormParam("access-mode-secret-key") final String inputedSecret,
                                                           @FormParam("selected-access-mode") final AccessMode selectedAccessMode) {
        final ResponseBuilder<TemplateInstance> responseBuilder = MainResource.createResponseBuilder(requestParams);

        TemplateInstance result;
        TemplateDataContainer<?> container;
        if (!requestParams.hxRequest()) {
            container = new TemplateDataContainer<>(null, securityProvider.define(requestParams));
            result = MainResource.spaTemplateLocalized(container, requestParams);
        } else {
            try {
                securityProvider.grant(selectedAccessMode, inputedSecret, requestParams, responseBuilder);
                responseBuilder.header("HX-Refresh", "true");
                container = new TemplateDataContainer<>(null, securityProvider.define(requestParams));
            } catch (final GrantAccessException grantAccessException) {
                container = new TemplateDataContainer<>(null, securityProvider.define(requestParams));
                container.addErrorMessage(grantAccessException.getMessage());
            }

            result = Templating.localized(Templates.requestAccessModal(container), requestParams.userLanguage());
        }

        return responseBuilder.entity(result).status(RestResponse.Status.OK).build();
    }

}
