package org.elasticsearch.rest.autotagging;

import static org.elasticsearch.rest.RestRequest.Method.POST;

import org.elasticsearch.action.autotagging.AutoTaggingAction;
import org.elasticsearch.action.autotagging.AutoTaggingRequest;
import org.elasticsearch.action.autotagging.AutoTaggingResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.logging.ESLogger;
import org.elasticsearch.common.logging.ESLoggerFactory;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.rest.AcknowledgedRestResponseActionListener;
import org.elasticsearch.rest.BaseRestHandler;
import org.elasticsearch.rest.RestChannel;
import org.elasticsearch.rest.RestController;
import org.elasticsearch.rest.RestRequest;

public class RestAutoTaggingAction extends BaseRestHandler {

    private static ESLogger logger = ESLoggerFactory.getLogger(RestAutoTaggingAction.class.getName());

    @Inject
    protected RestAutoTaggingAction(Settings settings, Client client, RestController controller) {
        super(settings, client);
        controller.registerHandler(POST, "/{index}/{type}/{id}/_autoTagging", this);
    }

    @Override
    public void handleRequest(final RestRequest request, final RestChannel channel) {
        AutoTaggingRequest autoTaggingRequest = new AutoTaggingRequest(request.param("index")).type(request.param("type"))
                .id(request.param("id")).field(request.param("t", "tags")).content(request.param("f", "content")).max(request.paramAsInt("m", 0));
        client.execute(AutoTaggingAction.INSTANCE, autoTaggingRequest, new AcknowledgedRestResponseActionListener<AutoTaggingResponse>(
                request, channel, logger));
    }

}
