package org.elasticsearch.service.autotagging;

import java.util.Set;

import org.elasticsearch.ElasticSearchException;
import org.elasticsearch.action.autotagging.AutoTaggingRequest;
import org.elasticsearch.action.autotagging.AutoTaggingResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.QueryBuilders;

import akka.actor.UntypedActor;

public class AutoTaggingWorker extends UntypedActor {

    private final DocumentTaggerService service;
    private final Client client;

    public AutoTaggingWorker(final DocumentTaggerService service, final Client client) {
        this.service = service;
        this.client = client;
    }

    @Override
    public void onReceive(Object message) throws Exception {
        if (message instanceof AutoTaggingRequest) {
            AutoTaggingRequest request = (AutoTaggingRequest) message;
            AutoTaggingResponse response;
            SearchResponse searchResponse = client.prepareSearch(request.getIndex()).setTypes(request.getType())
                    .addField(request.getContent()).setQuery(QueryBuilders.idsQuery(request.getType()).ids(request.getId())).get();
            if (searchResponse.getHits().getTotalHits() > 0) {
                Object text = searchResponse.getHits().getAt(0).getFields().get(request.getContent()).getValue();
                if (text == null) {
                    throw new ElasticSearchException("No value found for field " + request.getContent());
                }
                Set<String> keywords = service.extractKeywords(text.toString(), request.getMax());
                if (keywords.size() > 0) {
                    client.prepareUpdate(request.getIndex(), request.getType(), request.getId()).setDoc(request.getField(), keywords).get();
                }
                response = new AutoTaggingResponse(true);
            }
            response = new AutoTaggingResponse();
            getSender().tell(response, getSelf());

        } else {
            unhandled(message);
        }

    }

}
