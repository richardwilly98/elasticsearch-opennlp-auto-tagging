package org.elasticsearch.action.autotagging;

import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.support.single.custom.SingleCustomOperationRequestBuilder;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.internal.InternalClient;
import org.elasticsearch.common.Nullable;

public class AutoTaggingRequestBuilder extends
        SingleCustomOperationRequestBuilder<AutoTaggingRequest, AutoTaggingResponse, AutoTaggingRequestBuilder> {

    protected AutoTaggingRequestBuilder(Client client) {
        super((InternalClient) client, new AutoTaggingRequest());
    }

    public AutoTaggingRequestBuilder(Client client, @Nullable String index) {
        super((InternalClient) client, new AutoTaggingRequest(index));
    }

    /**
     * Sets the type to index the document to.
     */
    public AutoTaggingRequestBuilder setIndex(String index) {
        request.index(index);
        return this;
    }

    /**
     * Sets the type to index the document to.
     */
    public AutoTaggingRequestBuilder setType(String type) {
        request.type(type);
        return this;
    }

    /**
     * Sets the id to index the document under. Optional, and if not set, one
     * will be automatically generated.
     */
    public AutoTaggingRequestBuilder setId(String id) {
        request.id(id);
        return this;
    }

    public AutoTaggingRequestBuilder setField(String field) {
        request.field(field);
        return this;
    }

    public AutoTaggingRequestBuilder setContent(String content) {
        request.content(content);
        return this;
    }

    public AutoTaggingRequestBuilder setMax(Integer max) {
        request.max(max);
        return this;
    }

    @Override
    protected void doExecute(ActionListener<AutoTaggingResponse> listener) {
        ((Client) client).execute(AutoTaggingAction.INSTANCE, request, listener);
    }
}
