package akka.cluster.sharding.example.app;

import akka.actor.ActorRef;
import akka.actor.OneForOneStrategy;
import akka.actor.SupervisorStrategy;
import akka.actor.UntypedActor;
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

    private SupervisorStrategy strategy = new OneForOneStrategy(-1, Duration.create("5 seconds"), new Function<Throwable, SupervisorStrategy.Directive>() {
        @Override
        public SupervisorStrategy.Directive apply(Throwable t) {
            System.out.println("**********In Subscriber supervisorStrategy SupervisorStrategy.Directive apply " +
                    "*******************");
            if (t instanceof NullPointerException) {
                System.out.println("oneToOne Subscriber: restartOrEsclate strategy, restarting the actor");
                return restart();
            } else {
                System.out.println("$$$$$$$$$$$$$$$$$ Subscriber oneToOne: final else called escalating to oneToAll");
                return escalate();
            }
        }
    });

    @Override
    public SupervisorStrategy supervisorStrategy() {
        System.out.println("**********In Subscriber supervisorStrategy *******************");
        return strategy;
    }
    //get supervisor actor from shard
    ActorRef myEntitySupervisor = ClusterSharding.get(getContext().system()).shardRegion("MyEntitySupervisor");

    public void onReceive(Object msg) {
        if (msg instanceof MyCounter) {
            myEntitySupervisor.tell(msg,getSelf());
        } else if (msg instanceof DistributedPubSubMediator.Subscribe)
            log.info("subscribe started !!!!!!!!!!!!");
        else if (msg instanceof DistributedPubSubMediator.SubscribeAck)
            log.info("subscribing");
        else
            unhandled(msg);
    }
}