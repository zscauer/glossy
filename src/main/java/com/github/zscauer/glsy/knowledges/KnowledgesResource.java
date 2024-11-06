package com.github.zscauer.glsy.knowledges;

import com.github.zscauer.glsy.MainResource;
import com.github.zscauer.glsy.common.RequestParams;
import com.github.zscauer.glsy.common.TemplateDataContainer;
import com.github.zscauer.glsy.security.SecurityProvider;
import com.github.zscauer.glsy.tools.Templating;
import io.quarkus.qute.TemplateEnum;
import io.quarkus.qute.TemplateInstance;
import jakarta.inject.Singleton;
import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.RestResponse.ResponseBuilder;
import org.jboss.resteasy.reactive.RestResponse.Status;

import static com.github.zscauer.glsy.common.ResourcePaths.PATH_COMPONENT_SEARCH_PANEL;
import static com.github.zscauer.glsy.common.ResourcePaths.PATH_RESOURCE_KNOWLEDGES;

@Slf4j
@Singleton
@Path(PATH_RESOURCE_KNOWLEDGES)
@Produces(MediaType.TEXT_HTML)
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@SuppressWarnings("unused")
final class KnowledgesResource {

    SecurityProvider securityProvider;

    @TemplateEnum
    protected enum SearchPanelSource {
        INFROMATION_NOTES, TAGS
    }

    @GET
    @Path(PATH_COMPONENT_SEARCH_PANEL)
    public RestResponse<TemplateInstance> searchPanel(@BeanParam final RequestParams requestParams,
                                                      @QueryParam("searchSource") final SearchPanelSource searchSource) {
        final ResponseBuilder<TemplateInstance> responseBuilder = MainResource.createResponseBuilder(requestParams);

        TemplateInstance result;
        if (!requestParams.hxRequest()) {
            result = MainResource.spaTemplateLocalized(TemplateDataContainer.getEmpty(), requestParams);
        } else {
            switch (searchSource) {
                case TAGS -> result = Templating.localized(TagsResource.searchPanel(), requestParams.userLanguage());
                case null, default ->
                        result = Templating.localized(InformationNotesResource.searchPanel(), requestParams.userLanguage());
            }
        }

        return responseBuilder.entity(result).status(Status.OK).build();
    }

}
