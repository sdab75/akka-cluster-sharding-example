package akka.cluster.sharding.example.app;

import akka.actor.*;
import akka.cluster.client.ClusterClientReceptionist;
import akka.cluster.pubsub.DistributedPubSub;
import akka.cluster.pubsub.DistributedPubSubMediator;
import akka.cluster.sharding.ClusterSharding;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.japi.Function;
import scala.concurrent.duration.Duration;

import static akka.actor.SupervisorStrategy.escalate;
import static akka.actor.SupervisorStrategy.restart;

/**
 * Created by davenkat on 9/28/2015.
 */
public class Subscriber extends UntypedActor {
    LoggingAdapter log = Logging.getLogger(getContext().system(), this);

    public Subscriber() {
        ActorRef mediator = DistributedPubSub.get(getContext().system()).mediator();
        // subscribe to the topic named "content"
        mediator.tell(new DistributedPubSubMediator.Subscribe("content", "grp1", getSelf()), getSelf());
        ClusterClientReceptionist.get(getContext().system()).registerService(getSelf());

    }

    //If use this approach in multi node situation then sharding gets co
    ActorRef myEntity = getContext().actorOf(Props.create(MyEntity.class), "myEntity");


    //If use the below commented lookup way i can see the sharding working as expected but the supervisor doesn't work.
    //ActorRef myEntity = ClusterSharding.get(getContext().system()).shardRegion("MyEntity");


    private SupervisorStrategy strategy = new OneForOneStrategy(-1, Duration.create("5 seconds"), new Function<Throwable, SupervisorStrategy.Directive>() {
        @Override
        public SupervisorStrategy.Directive apply(Throwable t) {
            if (t instanceof NullPointerException) {
                System.out.println("oneToOne: restartOrEsclate strategy, restarting the actor");
                return restart();
            } else {
                System.out.println("oneToOne: final else called escalating to oneToAll");
                return escalate();
            }
        }
    });

    @Override
    public SupervisorStrategy supervisorStrategy() {
        return strategy;
    }

    public void onReceive(Object msg) {
        if (msg instanceof MyCounter) {
            log.info("Got: {}", msg);

            myEntity.forward(msg, getContext());

        } else if (msg instanceof DistributedPubSubMediator.Subscribe)
            log.info("subscribe started !!!!!!!!!!!!");
        else if (msg instanceof DistributedPubSubMediator.SubscribeAck)
            log.info("subscribing");
        else
            unhandled(msg);
    }
}