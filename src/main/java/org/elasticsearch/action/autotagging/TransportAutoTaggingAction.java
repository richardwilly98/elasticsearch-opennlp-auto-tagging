package org.elasticsearch.action.autotagging;

import java.util.Set;

import org.elasticsearch.ElasticSearchException;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.single.custom.TransportSingleCustomOperationAction;
import org.elasticsearch.client.Client;
import org.elasticsearch.cluster.ClusterService;
import org.elasticsearch.cluster.ClusterState;
import org.elasticsearch.cluster.block.ClusterBlockException;
import org.elasticsearch.cluster.block.ClusterBlockLevel;
import org.elasticsearch.cluster.routing.ShardsIterator;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.service.autotagging.DocumentTaggerService;
import org.elasticsearch.threadpool.ThreadPool;
import org.elasticsearch.transport.TransportService;

public class TransportAutoTaggingAction extends TransportSingleCustomOperationAction<AutoTaggingRequest, AutoTaggingResponse> {

    private final DocumentTaggerService documentTaggerService;
    private final Client client;

    @Inject
    protected TransportAutoTaggingAction(Settings settings, ThreadPool threadPool, ClusterService clusterService,
            TransportService transportService, DocumentTaggerService documentTaggerService, Client client) {
        super(settings, threadPool, clusterService, transportService);
        this.documentTaggerService = documentTaggerService;
        this.client = client;
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
            logger.debug("shardOperation - {} - {} - {} - {}", request.getIndex(), request.getType(), request.getId(),
                    request.getContent(), request.getField());
            SearchResponse searchResponse = client.prepareSearch(request.getIndex()).setTypes(request.getType())
                    .addField(request.getContent()).setQuery(QueryBuilders.idsQuery(request.getType()).ids(request.getId())).get();
            if (searchResponse.getHits().getTotalHits() > 0) {

                String text = searchResponse.getHits().getAt(0).getFields().get(request.getContent()).getValue();
                if (text == null) {
                    throw new ElasticSearchException("No value found for field " + request.getContent());
                }
                Set<String> keywords = documentTaggerService.extractKeywords(text.toString());
                if (keywords.size() > 0) {
                    client.prepareUpdate(request.getIndex(), request.getType(), request.getId()).setDoc(request.getField(), keywords).get();
                }
                return new AutoTaggingResponse(keywords);
            }
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
