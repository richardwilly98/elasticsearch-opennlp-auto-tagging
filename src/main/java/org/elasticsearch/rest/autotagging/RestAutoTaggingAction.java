package org.elasticsearch.rest.autotagging;

import static org.elasticsearch.rest.RestRequest.Method.POST;
import static org.elasticsearch.rest.RestStatus.OK;

import java.io.IOException;

import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.autotagging.AutoTaggingAction;
import org.elasticsearch.action.autotagging.AutoTaggingRequest;
import org.elasticsearch.action.autotagging.AutoTaggingRequest.Builder;
import org.elasticsearch.action.autotagging.AutoTaggingResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.logging.ESLogger;
import org.elasticsearch.common.logging.ESLoggerFactory;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.rest.BaseRestHandler;
import org.elasticsearch.rest.RestChannel;
import org.elasticsearch.rest.RestController;
import org.elasticsearch.rest.RestRequest;
import org.elasticsearch.rest.XContentRestResponse;
import org.elasticsearch.rest.XContentThrowableRestResponse;
import org.elasticsearch.rest.action.support.RestXContentBuilder;

public class RestAutoTaggingAction extends BaseRestHandler {

    private static ESLogger logger = ESLoggerFactory.getLogger(RestAutoTaggingAction.class.getName());

    @Inject
    protected RestAutoTaggingAction(Settings settings, Client client, RestController controller) {
        super(settings, client);
        controller.registerHandler(POST, "/{index}/{type}/{id}/_autoTagging", this);
    }

    @Override
    public void handleRequest(final RestRequest request, final RestChannel channel) {
        AutoTaggingRequest.Builder builder = new Builder(request.param("index")).type(request.param("type")).id(request.param("id"))
                .field(request.param("t", "tags")).content(request.param("f", "content"));
        AutoTaggingRequest autoTaggingRequest = builder.build();
        client.execute(AutoTaggingAction.INSTANCE, autoTaggingRequest, new ActionListener<AutoTaggingResponse>() {

            @Override
            public void onResponse(AutoTaggingResponse response) {
                try {
                    XContentBuilder builder = RestXContentBuilder.restContentBuilder(request);
                    builder.startObject();
                    builder.field("ok", true);
                    builder.startArray("tags");
                    for (String tag : response.getTags()) {
                        builder.startObject().field("tag", tag).endObject();
                        // .field("probability",
                        // lang.getProbability()).endObject();
                    }
                    builder.endArray();
                    builder.endObject();
                    channel.sendResponse(new XContentRestResponse(request, OK, builder));
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

            @Override
            public void onFailure(Throwable e) {
                try {
                    channel.sendResponse(new XContentThrowableRestResponse(request, e));
                } catch (IOException e1) {
                    logger.error("Failed to send failure response", e1);
                }
            }
        });
    }

}
