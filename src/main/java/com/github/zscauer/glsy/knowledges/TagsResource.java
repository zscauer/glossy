package com.github.zscauer.glsy.knowledges;

import com.github.zscauer.glsy.MainResource;
import com.github.zscauer.glsy.common.PageableRequestParams;
import com.github.zscauer.glsy.common.PageableSearchRequestParams;
import com.github.zscauer.glsy.common.PageableSearchTemplateDataContainer;
import com.github.zscauer.glsy.common.PageableTemplateDataContainer;
import com.github.zscauer.glsy.common.ResourcePaths;
import com.github.zscauer.glsy.common.TemplateDataContainer;
import com.github.zscauer.glsy.security.AccessProfile;
import com.github.zscauer.glsy.security.SecurityProvider;
import com.github.zscauer.glsy.tools.Templating;
import com.github.zscauer.glsy.tools.Templating.TemplateDestination;
import io.quarkus.qute.CheckedTemplate;
import io.quarkus.qute.TemplateInstance;
import io.vertx.core.http.HttpServerRequest;
import jakarta.inject.Singleton;
import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.RestResponse.ResponseBuilder;

import java.util.List;

import static com.github.zscauer.glsy.common.ResourcePaths.PATH_RESOURCE_TAGS;

@Slf4j
@Singleton
@Path(PATH_RESOURCE_TAGS)
@Produces(MediaType.TEXT_HTML)
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@SuppressWarnings("unused")
final class TagsResource {

    SecurityProvider securityProvider;

    @Context
    HttpServerRequest request;

    private static final String PATH_TAGS_MOST_USED = "/mostUsed";

    private static final String ENDPOINT_TAGS_MOST_USED = "%s%s".formatted(ResourcePaths.PATH_RESOURCE_TAGS, PATH_TAGS_MOST_USED);

    @CheckedTemplate(basePath = "fragments/knowledges/tags", defaultName = CheckedTemplate.HYPHENATED_ELEMENT_NAME)
    private static class Templates {
        private static native TemplateInstance searchPanel();

        private static native TemplateInstance list(PageableTemplateDataContainer<?> dataContainer);

        private static native TemplateInstance addToInformationNoteDatalist(TemplateDataContainer<?> dataContainer);

        private static native TemplateInstance mostUsedForAside(PageableTemplateDataContainer<?> dataContainer);
    }

    public static TemplateInstance searchPanel() {
        return Templates.searchPanel();
    }

    @GET
    public RestResponse<TemplateInstance> tags(@BeanParam final PageableSearchRequestParams requestParams,
                                                     @QueryParam("destination") final TemplateDestination destination) {
        final ResponseBuilder<TemplateInstance> responseBuilder = MainResource.createResponseBuilder(requestParams);
        final AccessProfile userAccess = securityProvider.define(requestParams);

        TemplateInstance result;
        if (!requestParams.hxRequest()) {
            final var dataContainer = new TemplateDataContainer<>(null, userAccess);
            dataContainer.redirectContent(requestParams.parameterizedPath(PATH_RESOURCE_TAGS));
            result = MainResource.spaTemplateLocalized(dataContainer, requestParams);
        } else {
            switch (destination) {
                case INFORMATION_NOTE -> {
                    final List<Tag> found = Tag.findAll(requestParams);
                    final var dataContainer = new TemplateDataContainer<>(found, userAccess);
                    result = Templating.localized(Templates.addToInformationNoteDatalist(dataContainer), requestParams.userLanguage());
                }
                case null, default -> {
                    final List<TagStatistics> found = TagStatistics.findAll(requestParams);
                    final var dataContainer = PageableSearchTemplateDataContainer.wrapContent(requestParams.searchString(),
                            found, requestParams.page(), PATH_RESOURCE_TAGS, requestParams.pageSize(), userAccess);
                    responseBuilder.header("HX-Push-Url", requestParams.parameterizedPath(PATH_RESOURCE_TAGS));

                    result = Templating.localized(Templates.list(dataContainer), requestParams.userLanguage());
                }
            }
        }

        return responseBuilder.entity(result).status(RestResponse.Status.OK).build();
    }

    @GET
    @Path(PATH_TAGS_MOST_USED)
    public RestResponse<TemplateInstance> mostUsedTags(@BeanParam final PageableRequestParams requestParams,
                                                             @QueryParam("destination") final TemplateDestination destination) {
        final ResponseBuilder<TemplateInstance> responseBuilder = MainResource.createResponseBuilder(requestParams);

        TemplateInstance result;
        if (!requestParams.hxRequest() || destination == null) {
            result = MainResource.spaTemplateLocalized(TemplateDataContainer.getEmpty(), requestParams);
        } else {
            final List<TagStatistics> found = TagStatistics.findAllPopular(requestParams);
            final var dataContainer = PageableTemplateDataContainer.wrapContent(
                    found, requestParams.page(), ENDPOINT_TAGS_MOST_USED, requestParams.pageSize(),
                    securityProvider.define(requestParams));
            switch (destination) {
                case ASIDE ->
                        result = Templating.localized(Templates.mostUsedForAside(dataContainer), requestParams.userLanguage());
                default -> result = MainResource.spaTemplateLocalized(TemplateDataContainer.getEmpty(), requestParams);
            }
        }

        return responseBuilder.entity(result).status(RestResponse.Status.OK).build();
    }

}
