package org.elasticsearch.action.autotagging;

import org.elasticsearch.action.Action;
import org.elasticsearch.client.Client;

public class AutoTaggingAction extends Action<AutoTaggingRequest, AutoTaggingResponse, AutoTaggingRequestBuilder> {

    public static final AutoTaggingAction INSTANCE = new AutoTaggingAction();
    public static final String NAME = "autotagging";

    private AutoTaggingAction() {
        super(NAME);
    }

    @Override
    public AutoTaggingRequestBuilder newRequestBuilder(Client client) {
        return new AutoTaggingRequestBuilder(client);
    }

    @Override
    public AutoTaggingResponse newResponse() {
        return new AutoTaggingResponse();
    }

}
