package akka.cluster.sharding.example.app;

import akka.actor.PoisonPill;
import akka.actor.ReceiveTimeout;
import akka.cluster.pubsub.DistributedPubSubMediator;
import akka.cluster.sharding.ShardRegion;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.japi.Procedure;
import akka.persistence.UntypedPersistentActor;

/**
 * Created by davenkat on 9/28/2015.
 */
public class MyEntity extends UntypedPersistentActor {
    LoggingAdapter log = Logging.getLogger(getContext().system(), this);

/*
    int counter;

    int cmdCount;

    int recoveredCount;
*/

    @Override
    public void preStart() throws Exception {
        System.out.println("Worker Startup ###########################");
        super.preStart();
    }

    @Override
    public String persistenceId() {
        return "counter-worker-" + getSelf().path().name();
    }

    @Override
    public void onReceiveRecover(Object msg) {
        if (msg instanceof MyCounter) {
            System.out.println("Worker :  Recovered Event -->" + ((MyCounter) msg).getMsg());
            processEvent(((MyCounter) msg));
        } else {
            unhandled(msg);
        }
    }

    @Override
    public boolean recoveryRunning() {
        log.info("Worker: Recovery running @@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
        return super.recoveryRunning();
    }

    @Override
    public boolean recoveryFinished() {
        System.out.println("Worker: Recovery finished @@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
        return super.recoveryFinished();
    }

    @Override
    public void onRecoveryFailure(Throwable cause, scala.Option<Object> event) {
        System.out.println("Worker: Recovery failed @@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
        super.onRecoveryFailure(cause, event);
    }

    private void processEvent(MyCounter evt){
        if(evt.getCount()==190){
            throw  new RuntimeException("Failed processing counter --->"+evt.toString());
        }
        System.out.println("Worker: Successfully processed persisted event-->" + evt.getMsg());
        saveSnapshot(evt);
    }
    @Override
    public void onReceiveCommand(Object msg) {
        if (msg instanceof MyCounter) {
            log.info("Worker: onReceiveCommand #######", ((MyCounter) msg).getMsg());
            MyCounter evt = ((MyCounter) msg);
            evt.setMsg(evt.getMsg() + "-->Validated");
            persistAsync(evt, new Procedure<MyCounter>() {
                public void apply(MyCounter evt) throws Exception {
                    processEvent(evt);
                }
            });

        } else if (msg instanceof DistributedPubSubMediator.SubscribeAck) {
            log.info("worker subscribing");
        } else if (msg.equals(ReceiveTimeout.getInstance())) {
            getContext().parent().tell(new ShardRegion.Passivate(PoisonPill.getInstance()), getSelf());
        } else
            unhandled(msg);
    }
}
