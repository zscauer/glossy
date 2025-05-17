package com.github.zscauer.glsy.knowledges;

import com.github.zscauer.glsy.common.api.ApiResponse;
import io.vertx.core.http.HttpServerRequest;
import jakarta.inject.Singleton;
import jakarta.ws.rs.GET;
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

import static com.github.zscauer.glsy.common.ResourcePaths.PATH_API_V1;
import static com.github.zscauer.glsy.common.ResourcePaths.PATH_RESOURCE_INFORMATION_NOTES;

@Slf4j
@Singleton
@Path(PATH_RESOURCE_INFORMATION_NOTES)
@Produces(MediaType.APPLICATION_JSON)
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@SuppressWarnings("unused")
final class InformationNotesResource {

    @Context
    HttpServerRequest request;

    @GET
    @Path(PATH_API_V1)
    public RestResponse<ApiResponse<InformationNote>> getInformationNote() {
        final ResponseBuilder<ApiResponse<InformationNote>> responseBuilder = ResponseBuilder.create(RestResponse.Status.ACCEPTED);

        return responseBuilder.entity(new ApiResponse<>(null)).build();
    }


}
