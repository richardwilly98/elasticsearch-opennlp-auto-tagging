package org.elasticsearch.service.autotagging;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.Set;

import opennlp.tools.lemmatizer.SimpleLemmatizer;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSSample;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.tokenize.SimpleTokenizer;
import opennlp.tools.util.ObjectStream;
import opennlp.tools.util.PlainTextByLineStream;

import org.elasticsearch.ElasticSearchException;
import org.elasticsearch.action.autotagging.AutoTaggingRequest;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.Nullable;
import org.elasticsearch.common.base.Stopwatch;
import org.elasticsearch.common.collect.HashMultiset;
import org.elasticsearch.common.collect.Multiset;
import org.elasticsearch.common.collect.Multiset.Entry;
import org.elasticsearch.common.collect.Multisets;
import org.elasticsearch.common.collect.Sets;
import org.elasticsearch.common.component.AbstractLifecycleComponent;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.logging.ESLogger;
import org.elasticsearch.common.logging.ESLoggerFactory;
import org.elasticsearch.common.settings.Settings;

import akka.actor.Actor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.actor.UntypedActorFactory;

public class DocumentTaggerService extends AbstractLifecycleComponent<DocumentTaggerService> {

    private static ESLogger logger = ESLoggerFactory.getLogger(DocumentTaggerService.class.getName());
    private static POSTaggerME tagger;
    private final Client client;
    private static ActorSystem system;
    private static ActorRef master;

    @Inject
    public DocumentTaggerService(Settings settings, final Client client) {
        super(settings);
        this.client = client;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.elasticsearch.service.autotagging.DocumentTagger#detectTags(org.
     * elasticsearch.action.autotagging.AutoTaggingRequest)
     */
    public void detectTags(AutoTaggingRequest request) {
        try {
            logger.debug("shardOperation - {} - {} - {} - {}", request.getIndex(), request.getType(), request.getId(),
                    request.getContent(), request.getField());
            master.tell(new AutoTaggingMaster.Process(request));
        } catch (Throwable t) {
            throw new ElasticSearchException(t.getMessage(), t);
        }

    }

    public Set<String> extractKeywords(String text, @Nullable Integer max) {
        Stopwatch watcher = Stopwatch.createStarted();
        Set<String> keywords = Sets.newHashSet();
        try {
            BufferedReader r = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(text.getBytes("UTF-8"))));
            Multiset<String> nounSet = HashMultiset.create();
            ObjectStream<String> lineStream = new PlainTextByLineStream(r);

            String line;

            while ((line = lineStream.read()) != null) {

                String[] tokens = SimpleTokenizer.INSTANCE.tokenize(line);
                String[] tags = tagger.tag(tokens);

                POSSample sample = new POSSample(tokens, tags);
                for (int i = 0; i < sample.getSentence().length; i++) {
                    String word = sample.getSentence()[i].toLowerCase();
                    String tag = sample.getTags()[i];
                    word = lemmatize(word, tag);
                    if (logger.isTraceEnabled()) {
                        logger.trace(word + " - " + tag);
                    }
                    if (word.length() > 2 && tag.matches("NN|NNP|NNS|NNPS")) {
                        nounSet.add(word);
                    }
                }
            }

            if (max == null || max == 0) {
                max = settings.getAsInt("opennlp-auto-tagging.max", new Integer(10));
            }
            int count = 0;
            Multiset<String> sortedSet = Multisets.copyHighestCountFirst(nounSet);
            Iterator<Entry<String>> iterator = sortedSet.entrySet().iterator();
            while (count < max && iterator.hasNext()) {
                Entry<String> item = iterator.next();
                if (logger.isDebugEnabled()) {
                    logger.debug("Add tag: {} - {}", item.getElement(), item.getCount());
                }
                keywords.add(item.getElement());
                count++;
            }
            logger.info("Elapsed time to extract keywords: {}", watcher.toString());
        } catch (Throwable t) {
            logger.error("failed extractKeywords", t);
        }
        return keywords;

    }

    private static SimpleLemmatizer lemmatizer;

    private String lemmatize(String word, String postag) throws IOException {
        if (lemmatizer == null) {
            InputStream is = getClass().getResourceAsStream("/models/en-lemmatizer.dict");
            lemmatizer = new SimpleLemmatizer(is);
            is.close();
        }
        String lemma = lemmatizer.lemmatize(word, postag);
        return lemma;
    }

    @Override
    protected void doStart() throws ElasticSearchException {
        try {
            system = ActorSystem.create("OpenNlpAutoTaggingSystem");
            master = system.actorOf(new Props(new UntypedActorFactory() {
                private static final long serialVersionUID = 1L;

                @Override
                public Actor create() {
                    return new AutoTaggingMaster(DocumentTaggerService.this, client);
                }
            }), "master");
            InputStream is = getClass().getResourceAsStream("/models/en-pos-maxent.bin");
            tagger = new POSTaggerME(new POSModel(is));
            is.close();
        } catch (Throwable t) {
            throw new ElasticSearchException(t.getMessage(), t);
        }
    }

    @Override
    protected void doStop() throws ElasticSearchException {
        system.shutdown();
    }

    @Override
    protected void doClose() throws ElasticSearchException {
    }
}
