package com.github.zscauer.glsy;

import com.github.zscauer.glsy.common.CommonTemplates;
import com.github.zscauer.glsy.common.ErrorPageTemplateDataContainer;
import io.quarkus.qute.TemplateInstance;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Provider
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@SuppressWarnings("unused")
public class GlobalResourceExceptionMapper implements ExceptionMapper<Exception> {

    jakarta.inject.Provider<ContainerRequestContext> containerRequestContextProvider;

    @Override
    @Produces(MediaType.TEXT_HTML)
    public final Response toResponse(final Exception exception) {
        logException(exception);

        final ContainerRequestContext containerRequestContext = containerRequestContextProvider.get();
        ErrorPageTemplateDataContainer<?> dataContainer = new ErrorPageTemplateDataContainer<>().errorCode(404);
        TemplateInstance templateInstance;

        if (containerRequestContext.getHeaderString("HX-Request") != null) {
            // processing only fragment for HTMX requests
            templateInstance = CommonTemplates.error(dataContainer);
        } else {
            // processing full page for direct requests
            templateInstance = MainResource.spaTemplateLocalized(dataContainer, null);
        }

        return Response
                .status(Status.OK) // HTMX needs OK status to process received fragment
                .header("Vary", "HX-Request")
                .entity(templateInstance)
                .build();
    }

    private void logException(final Exception exception) {
        // TODO: handle exception messages by exception type
        log.error(exception.getMessage());
    }

}
