package org.elasticsearch.action.autotagging;

import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.support.single.custom.SingleCustomOperationRequestBuilder;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.internal.InternalClient;

public class AutoTaggingRequestBuilder extends SingleCustomOperationRequestBuilder<AutoTaggingRequest, AutoTaggingResponse, AutoTaggingRequestBuilder> {


    protected AutoTaggingRequestBuilder(Client client) {
        super((InternalClient) client, new AutoTaggingRequest());
    }

    @Override
    protected void doExecute(ActionListener<AutoTaggingResponse> listener) {
        ((Client)client).execute(AutoTaggingAction.INSTANCE, request, listener);
    }
 }
