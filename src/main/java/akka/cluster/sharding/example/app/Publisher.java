package akka.cluster.sharding.example.app;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import akka.cluster.pubsub.DistributedPubSub;
import akka.cluster.pubsub.DistributedPubSubMediator;
import akka.event.Logging;
import akka.event.LoggingAdapter;

/**
 * Created by davenkat on 9/28/2015.
 */
public class Publisher extends UntypedActor {
    LoggingAdapter log = Logging.getLogger(getContext().system(), this);

    // activate the extension
    ActorRef mediator =DistributedPubSub.get(getContext().system()).mediator();
    public void onReceive(Object msg) {
        if (msg instanceof MyCounter) {
            MyCounter myCounter =(MyCounter) msg;
            //Following is for broadcasting
            mediator.tell(new DistributedPubSubMediator.Publish("content", myCounter,true),getSelf());

        }
        else if (msg instanceof DistributedPubSubMediator.SubscribeAck)
            log.info("publisher subscribing");
        else {
            unhandled(msg);
        }
    }
}