package com.github.zscauer.glsy.knowledges;

import com.github.zscauer.glsy.MainResource;
import com.github.zscauer.glsy.common.IncompleteRepositoryOperationException;
import com.github.zscauer.glsy.common.PageableRequestParams;
import com.github.zscauer.glsy.common.PageableSearchRequestParams;
import com.github.zscauer.glsy.common.PageableSearchTemplateDataContainer;
import com.github.zscauer.glsy.common.PageableTemplateDataContainer;
import com.github.zscauer.glsy.common.RequestParams;
import com.github.zscauer.glsy.common.TemplateDataContainer;
import com.github.zscauer.glsy.i18n.MessagesProvider;
import com.github.zscauer.glsy.security.AccessibleContent;
import com.github.zscauer.glsy.security.AccessProfile;
import com.github.zscauer.glsy.security.Privilege;
import com.github.zscauer.glsy.security.SecurityProvider;
import com.github.zscauer.glsy.tools.Templating;
import io.quarkus.qute.CheckedTemplate;
import io.quarkus.qute.TemplateInstance;
import io.vertx.core.http.HttpServerRequest;
import jakarta.annotation.Nullable;
import jakarta.inject.Singleton;
import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
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

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static com.github.zscauer.glsy.common.ResourcePaths.PATH_KEY_HISTORY;
import static com.github.zscauer.glsy.common.ResourcePaths.PATH_KEY_TAG;
import static com.github.zscauer.glsy.common.ResourcePaths.PATH_OPERATION_UPSERT;
import static com.github.zscauer.glsy.common.ResourcePaths.PATH_RESOURCE_INFORMATION_NOTES;

@Slf4j
@Singleton
@Path(PATH_RESOURCE_INFORMATION_NOTES)
@Produces(MediaType.TEXT_HTML)
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@SuppressWarnings("unused")
final class InformationNotesResource {

    SecurityProvider securityProvider;

    @Context
    HttpServerRequest request;

    @CheckedTemplate(basePath = "fragments/knowledges/information_notes", defaultName = CheckedTemplate.HYPHENATED_ELEMENT_NAME)
    private static class Templates {
        private static native TemplateInstance searchPanel();

        private static native TemplateInstance list(PageableTemplateDataContainer<?> dataContainer);

        private static native TemplateInstance upsertModal(TemplateDataContainer<?> dataContainer);

        private static native TemplateInstance view(TemplateDataContainer<?> dataContainer);

        private static native TemplateInstance viewHistory(PageableTemplateDataContainer<?> dataContainer);

        private static native TemplateInstance viewHistoryRecordModal(TemplateDataContainer<?> dataContainer);
    }

    public static TemplateInstance searchPanel() {
        return Templates.searchPanel();
    }

    @GET
    public RestResponse<TemplateInstance> informationNotes(@BeanParam final PageableSearchRequestParams requestParams) {
        final ResponseBuilder<TemplateInstance> responseBuilder = MainResource.createResponseBuilder(requestParams);
        final AccessProfile userAccess = securityProvider.define(requestParams);

        TemplateInstance result;
        if (!requestParams.hxRequest()) {
            final var dataContainer = new TemplateDataContainer<>(null, userAccess);
            dataContainer.redirectContent(requestParams.parameterizedPath(PATH_RESOURCE_INFORMATION_NOTES));
            result = MainResource.spaTemplateLocalized(dataContainer, requestParams);
        } else {
            final List<InformationNote> found = InformationNote.findAll(requestParams);
            final var dataContainer = PageableSearchTemplateDataContainer.wrapContent(requestParams.searchString(),
                    found, requestParams.page(), PATH_RESOURCE_INFORMATION_NOTES, requestParams.pageSize(), userAccess);
            responseBuilder.header("HX-Push-Url", requestParams.parameterizedPath(PATH_RESOURCE_INFORMATION_NOTES));

            result = Templating.localized(Templates.list(dataContainer), requestParams.userLanguage());
        }

        return responseBuilder.entity(result).status(RestResponse.Status.OK).build();
    }

    @GET
    @Path(PATH_OPERATION_UPSERT)
    public RestResponse<TemplateInstance> upsertInformationNoteForm(@BeanParam final RequestParams requestParams,
                                                                    @Nullable @QueryParam("information-note-id") final UUID informationNoteId) {
        final ResponseBuilder<TemplateInstance> responseBuilder = MainResource.createResponseBuilder(requestParams);
        final AccessProfile userAccess = securityProvider.define(requestParams);

        TemplateInstance result;
        if (!requestParams.hxRequest()) {
            final var dataContainer = new TemplateDataContainer<>(null, userAccess);
            result = MainResource.spaTemplateLocalized(dataContainer, requestParams);
        } else {
            final InformationNote found = InformationNote.find(informationNoteId);
            final var dataContainer = new TemplateDataContainer<>(found, userAccess);

            result = Templating.localized(Templates.upsertModal(dataContainer), requestParams.userLanguage());
        }

        return responseBuilder.entity(result).status(RestResponse.Status.OK).build();
    }

    @PUT
    @Path(PATH_OPERATION_UPSERT)
    public RestResponse<TemplateInstance> upsertInformationNote(@BeanParam final RequestParams requestParams,
                                                                @BeanParam final InformationNote informationNote) {
        final ResponseBuilder<TemplateInstance> responseBuilder = MainResource.createResponseBuilder(requestParams);
        final AccessProfile userAccess = securityProvider.define(requestParams);

        TemplateInstance result;
        if (!requestParams.hxRequest()) {
            final var dataContainer = new TemplateDataContainer<>(null, userAccess);
            result = MainResource.spaTemplateLocalized(dataContainer, requestParams);
        } else {
            final var dataContainer = new TemplateDataContainer<>(informationNote, userAccess);
            if (informationNote.isNew()) {
                if (!userAccess.havePrivilegesToContent(AccessibleContent.INFORMATION_NOTE, Privilege.CREATE)) {
                    dataContainer.addErrorMessage(MessagesProvider.security(requestParams.userLanguage()).no_privilege_to_create_information_notes());
                    result = Templating.localized(Templates.upsertModal(dataContainer), requestParams.userLanguage());
                    return responseBuilder.entity(result).status(RestResponse.Status.OK).build();
                }
            } else {
                if (!userAccess.havePrivilegesToContent(AccessibleContent.INFORMATION_NOTE, Privilege.UPDATE)) {
                    dataContainer.addErrorMessage(MessagesProvider.security(requestParams.userLanguage()).no_privilege_to_update_information_notes());
                    result = Templating.localized(Templates.upsertModal(dataContainer), requestParams.userLanguage());
                    return responseBuilder.entity(result).status(RestResponse.Status.OK).build();
                }
            }
            final boolean isNew = informationNote.isNew();
            try {
                informationNote.save();
                final InformationNote found = InformationNote.find(informationNote.id());
                InformationNoteHistory.addHistory(found, isNew ? LifecycleEvent.CREATED : LifecycleEvent.UPDATED, request.remoteAddress());
                final String endpointPath = "%s/%s".formatted(PATH_RESOURCE_INFORMATION_NOTES, informationNote.id());
                responseBuilder.header("HX-Redirect", endpointPath);
                responseBuilder.header("HX-Push-Url", endpointPath);
                result = Templating.localized(Templates.view(dataContainer), requestParams.userLanguage());
            } catch (final Exception e) {
                if (e instanceof IncompleteRepositoryOperationException) {
                    dataContainer.addErrorMessage(MessagesProvider.global(requestParams.userLanguage()).error_unique_violation_constraint());
                } else {
                    dataContainer.addErrorMessage(MessagesProvider.global(requestParams.userLanguage()).error_internal());
                }
                result = Templating.localized(Templates.upsertModal(dataContainer), requestParams.userLanguage());
            }
        }

        return responseBuilder.entity(result).status(RestResponse.Status.OK).build();
    }

    @GET
    @Path("/{informationNoteId}")
    public RestResponse<TemplateInstance> informationNote(@PathParam("informationNoteId") final UUID informationNoteId,
                                                          @BeanParam final PageableSearchRequestParams requestParams) {
        final ResponseBuilder<TemplateInstance> responseBuilder = MainResource.createResponseBuilder(requestParams);
        final String endpointPath = "%s/%s".formatted(PATH_RESOURCE_INFORMATION_NOTES, informationNoteId);
        final AccessProfile userAccess = securityProvider.define(requestParams);

        TemplateInstance result;
        if (!requestParams.hxRequest()) {
            final var dataContainer = new TemplateDataContainer<>(null, userAccess);
            dataContainer.redirectContent(requestParams.parameterizedPath(endpointPath));
            result = MainResource.spaTemplateLocalized(dataContainer, requestParams);
        } else {
            final InformationNote found = InformationNote.find(informationNoteId);
            final var dataContainer = new TemplateDataContainer<>(found, userAccess);
            responseBuilder.header("HX-Push-Url", endpointPath);

            result = Templating.localized(Templates.view(dataContainer), requestParams.userLanguage());
        }

        return responseBuilder.entity(result).status(RestResponse.Status.OK).build();
    }

    @GET
    @Path("/{informationNoteId}" + PATH_KEY_HISTORY)
    public RestResponse<TemplateInstance> informationNoteHistory(@PathParam("informationNoteId") final UUID informationNoteId,
                                                                 @BeanParam final PageableRequestParams requestParams) {
        final ResponseBuilder<TemplateInstance> responseBuilder = MainResource.createResponseBuilder(requestParams);
        final String endpointPath = "%s/%s%s".formatted(PATH_RESOURCE_INFORMATION_NOTES, informationNoteId, PATH_KEY_HISTORY);
        final AccessProfile userAccess = securityProvider.define(requestParams);

        TemplateInstance result;
        if (!requestParams.hxRequest()) {
            final var dataContainer = new TemplateDataContainer<>(null, userAccess);
            dataContainer.redirectContent("%s/%s".formatted(PATH_RESOURCE_INFORMATION_NOTES, informationNoteId));
            result = MainResource.spaTemplateLocalized(dataContainer, requestParams);
        } else {
            final boolean haveAccess = userAccess.havePrivilegesToContent(AccessibleContent.INFORMATION_NOTE_HISTORY, Privilege.READ);
            final List<InformationNoteHistory> found = haveAccess ?
                    InformationNoteHistory.findForInformationNote(informationNoteId, requestParams) :
                    Collections.emptyList();
            final var dataContainer = PageableTemplateDataContainer.wrapContent(
                    found, requestParams.page(), endpointPath, requestParams.pageSize(), userAccess);
            if (!haveAccess) {
                dataContainer.addErrorMessage(MessagesProvider.security(requestParams.userLanguage()).no_privilege_to_view_information_note_history());
            }
            responseBuilder.header("HX-Push-Url", requestParams.parameterizedPath(endpointPath));

            result = Templating.localized(Templates.viewHistory(dataContainer), requestParams.userLanguage());
        }

        return responseBuilder.entity(result).status(RestResponse.Status.OK).build();
    }

    @GET
    @Path("/{informationNoteId}" + PATH_KEY_HISTORY + "/{historyId}")
    public RestResponse<TemplateInstance> informationNoteHistoryRecord(@PathParam("informationNoteId") final UUID informationNoteId,
                                                                       @PathParam("historyId") final long historyId,
                                                                       @BeanParam final RequestParams requestParams) {
        final ResponseBuilder<TemplateInstance> responseBuilder = MainResource.createResponseBuilder(requestParams);
        final String endpointPath = "%s/%s%s".formatted(PATH_RESOURCE_INFORMATION_NOTES, informationNoteId, PATH_KEY_HISTORY);
        final AccessProfile userAccess = securityProvider.define(requestParams);

        TemplateInstance result;
        if (!requestParams.hxRequest()) {
            final var dataContainer = new TemplateDataContainer<>(null, userAccess);
            dataContainer.redirectContent("%s/%s".formatted(PATH_RESOURCE_INFORMATION_NOTES, informationNoteId));
            result = MainResource.spaTemplateLocalized(dataContainer, requestParams);
        } else {
            TemplateDataContainer<InformationNoteHistory> dataContainer;
            if (userAccess.havePrivilegesToContent(AccessibleContent.INFORMATION_NOTE_HISTORY, Privilege.READ)) {
                InformationNoteHistory found = InformationNoteHistory.find(historyId);
                dataContainer = new TemplateDataContainer<>(found, userAccess);
            } else {
                dataContainer = new TemplateDataContainer<InformationNoteHistory>(null, userAccess)
                        .addErrorMessage(MessagesProvider.security(requestParams.userLanguage()).no_privilege_to_view_information_note_history());
            }

            result = Templating.localized(Templates.viewHistoryRecordModal(dataContainer), requestParams.userLanguage());
        }

        return responseBuilder.entity(result).status(RestResponse.Status.OK).build();
    }

    @PATCH
    @Path("/{informationNoteId}/tags/add")
    public RestResponse<TemplateInstance> addToInformationNote(@PathParam("informationNoteId") final UUID informationNoteId,
                                                               @BeanParam final RequestParams requestParams,
                                                               @FormParam("inputed-value") final String tagName) {
        final ResponseBuilder<TemplateInstance> responseBuilder = MainResource.createResponseBuilder(requestParams);
        final AccessProfile userAccess = securityProvider.define(requestParams);

        TemplateInstance result;
        if (!requestParams.hxRequest()) {
            final var dataContainer = new TemplateDataContainer<>(null, userAccess);
            result = MainResource.spaTemplateLocalized(dataContainer, requestParams);
        } else {
            final InformationNote foundNote = InformationNote.find(informationNoteId);
            if (foundNote != null) {
                TemplateDataContainer<InformationNote> dataContainer;
                if (!userAccess.havePrivilegesToContent(AccessibleContent.INFORMATION_NOTE, Privilege.UPDATE)) {
                    dataContainer = new TemplateDataContainer<>(foundNote, userAccess)
                            .addErrorMessage(MessagesProvider.security(requestParams.userLanguage()).no_privilege_to_update_information_notes());
                    result = Templating.localized(Templates.view(dataContainer), requestParams.userLanguage());
                    return responseBuilder.entity(result).status(RestResponse.Status.OK).build();
                }
                if (tagName == null || tagName.isBlank()) {
                    dataContainer = new TemplateDataContainer<>(foundNote, userAccess)
                            .addErrorMessage(MessagesProvider.knowledges(requestParams.userLanguage()).tag_name_empty_error());
                } else {
                    if (!Tag.exists(tagName) && !userAccess.havePrivilegesToContent(AccessibleContent.TAG, Privilege.CREATE)) {
                        dataContainer = new TemplateDataContainer<>(foundNote, userAccess)
                                .addErrorMessage(MessagesProvider.security(requestParams.userLanguage()).no_privilege_to_create_tags());
                        result = Templating.localized(Templates.view(dataContainer), requestParams.userLanguage());
                        return responseBuilder.entity(result).status(RestResponse.Status.OK).build();
                    }

                    final InformationNote updatedNote = foundNote.addTag(Tag.getOfName(tagName));
                    InformationNoteHistory.addHistory(updatedNote, LifecycleEvent.UPDATED, request.remoteAddress());
                    dataContainer = new TemplateDataContainer<>(updatedNote, userAccess);
                }
                result = Templates.view(dataContainer);
            } else {
                final var dataContainer = new TemplateDataContainer<>(null, userAccess);
                result = MainResource.spaTemplateLocalized(dataContainer, requestParams);
            }
        }

        return responseBuilder.entity(result).status(RestResponse.Status.OK).build();
    }

    @PATCH
    @Path("/{informationNoteId}/tags/{tagId}/delete")
    public RestResponse<TemplateInstance> deleteTagFromInformationNote(@PathParam("informationNoteId") final UUID informationNoteId,
                                                                       @PathParam("tagId") final UUID tagId,
                                                                       @BeanParam final RequestParams requestParams) {
        final ResponseBuilder<TemplateInstance> responseBuilder = MainResource.createResponseBuilder(requestParams);
        final AccessProfile userAccess = securityProvider.define(requestParams);

        TemplateInstance result;
        if (!requestParams.hxRequest()) {
            final var dataContainer = new TemplateDataContainer<>(null, userAccess);
            result = MainResource.spaTemplateLocalized(dataContainer, requestParams);
        } else {
            if (InformationNote.exists(informationNoteId)) {
                TemplateDataContainer<InformationNote> dataContainer;
                if (!Tag.exists(tagId)) {
                    dataContainer = new TemplateDataContainer<>(InformationNote.find(informationNoteId), userAccess)
                            .addErrorMessage(MessagesProvider.knowledges(requestParams.userLanguage()).tag_not_found_error());
                } else if (!userAccess.havePrivilegesToContent(AccessibleContent.INFORMATION_NOTE, Privilege.UPDATE)) {
                    dataContainer = new TemplateDataContainer<>(InformationNote.find(informationNoteId), userAccess)
                            .addErrorMessage(MessagesProvider.security(requestParams.userLanguage()).no_privilege_to_update_information_notes());
                } else {
                    final InformationNote updatedNote = InformationNote.deleteTag(informationNoteId, tagId);
                    InformationNoteHistory.addHistory(updatedNote, LifecycleEvent.UPDATED, request.remoteAddress());
                    dataContainer = new TemplateDataContainer<>(updatedNote, userAccess);
                }
                result = Templates.view(dataContainer);
            } else {
                final var dataContainer = new TemplateDataContainer<>(null, userAccess);
                result = MainResource.spaTemplateLocalized(dataContainer, requestParams);
            }
        }

        return responseBuilder.entity(result).status(RestResponse.Status.OK).build();
    }

    @GET
    @Path(PATH_KEY_TAG + "/{tag}")
    public RestResponse<TemplateInstance> informationNotesWithTag(@PathParam("tag") final UUID tag,
                                                                  @BeanParam final PageableRequestParams requestParams) {
        final ResponseBuilder<TemplateInstance> responseBuilder = MainResource.createResponseBuilder(requestParams);
        final String endpointPath = "%s%s/%s".formatted(PATH_RESOURCE_INFORMATION_NOTES, PATH_KEY_TAG, tag);
        final AccessProfile userAccess = securityProvider.define(requestParams);

        TemplateInstance result;
        if (!requestParams.hxRequest()) {
            final var dataContainer = new TemplateDataContainer<>(null, userAccess);
            dataContainer.redirectContent(requestParams.parameterizedPath(endpointPath));
            result = MainResource.spaTemplateLocalized(dataContainer, requestParams);
        } else {
            final List<InformationNote> found = InformationNote.findAllWithTag(tag, requestParams);
            final var dataContainer = PageableTemplateDataContainer.wrapContent(
                    found, requestParams.page(), endpointPath, requestParams.pageSize(), userAccess);
            responseBuilder.header("HX-Push-Url", requestParams.parameterizedPath(endpointPath));

            result = Templating.localized(Templates.list(dataContainer), requestParams.userLanguage());
        }

        return responseBuilder.entity(result).status(RestResponse.Status.OK).build();
    }

}
