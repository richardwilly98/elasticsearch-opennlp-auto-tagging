package org.elasticsearch.action.autotagging;

import org.elasticsearch.ElasticSearchException;
import org.elasticsearch.action.support.single.custom.TransportSingleCustomOperationAction;
import org.elasticsearch.client.Client;
import org.elasticsearch.cluster.ClusterService;
import org.elasticsearch.cluster.ClusterState;
import org.elasticsearch.cluster.block.ClusterBlockException;
import org.elasticsearch.cluster.block.ClusterBlockLevel;
import org.elasticsearch.cluster.routing.ShardsIterator;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.service.autotagging.DocumentTaggerService;
import org.elasticsearch.threadpool.ThreadPool;
import org.elasticsearch.transport.TransportService;

public class TransportAutoTaggingAction extends TransportSingleCustomOperationAction<AutoTaggingRequest, AutoTaggingResponse> {

    private final DocumentTaggerService documentTaggerService;

    @Inject
    protected TransportAutoTaggingAction(Settings settings, ThreadPool threadPool, ClusterService clusterService,
            TransportService transportService, final DocumentTaggerService documentTaggerService, final Client client) {
        super(settings, threadPool, clusterService, transportService);
        this.documentTaggerService = documentTaggerService;
    }


    @Override
    protected String transportAction() {
        return AutoTaggingAction.NAME;
    }

    @Override
    protected String executor() {
        return ThreadPool.Names.GENERIC;
    }

    // execute always locally
    @Override
    protected ShardsIterator shards(ClusterState state, AutoTaggingRequest request) {
        return null;
    }

    @Override
    protected AutoTaggingResponse shardOperation(AutoTaggingRequest request, int shardId) throws ElasticSearchException {
        try {
            documentTaggerService.detectTags(request);
            return new AutoTaggingResponse();
        } catch (Throwable t) {
            throw new ElasticSearchException(t.getMessage(), t);
        }
    }

    @Override
    protected AutoTaggingRequest newRequest() {
        return new AutoTaggingRequest();
    }

    @Override
    protected AutoTaggingResponse newResponse() {
        return new AutoTaggingResponse();
    }

    @Override
    protected ClusterBlockException checkGlobalBlock(ClusterState state, AutoTaggingRequest request) {
        return state.blocks().globalBlockedException(ClusterBlockLevel.READ);
    }

    @Override
    protected ClusterBlockException checkRequestBlock(ClusterState state, AutoTaggingRequest request) {
        return null;
    }

}
