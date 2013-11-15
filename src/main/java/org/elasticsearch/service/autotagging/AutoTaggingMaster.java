package org.elasticsearch.service.autotagging;

import org.elasticsearch.action.autotagging.AutoTaggingRequest;
import org.elasticsearch.client.Client;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.actor.UntypedActorFactory;

public class AutoTaggingMaster extends UntypedActor {

    public static class Process {
        private final AutoTaggingRequest request;

        public Process(AutoTaggingRequest request) {
            this.request = request;
        }

        public AutoTaggingRequest getRequest() {
            return request;
        }
    }

    private final ActorRef workerRouter;

    public AutoTaggingMaster(final DocumentTaggerService service, final Client client) {
        workerRouter = this.getContext().actorOf(new Props(new UntypedActorFactory() {
            private static final long serialVersionUID = 1L;

            public UntypedActor create() {
                return new AutoTaggingWorker(service, client);
            }
        }), "master");
    }

    @Override
    public void onReceive(Object message) throws Exception {
        if (message instanceof Process) {
            Process process = (Process) message;
            workerRouter.tell(process.getRequest(), getSelf());
        }
    }

}
