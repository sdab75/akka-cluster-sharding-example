package akka.cluster.sharding.example.app;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.cluster.sharding.ClusterSharding;
import akka.cluster.sharding.ClusterShardingSettings;
import akka.cluster.sharding.ShardRegion;
import akka.japi.Option;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

public class SubClusterApp2 {

    public static void main(String[] args) {
        if (args.length == 0)
            startup(new String[]{"2551"});
        else
            startup(args);
    }

    public static void startup(String[] ports) {
        for (String port : ports) {
            // Override the configuration of the port
            Config config = ConfigFactory.parseString("akka.remote.netty.tcp.port=" + port).withFallback(ConfigFactory.load());

            ShardRegion.MessageExtractor messageExtractor = new ShardRegion.MessageExtractor() {

                @Override
                public String entityId(Object message) {
                    MyCounter counter=(MyCounter) message;
                    return String.valueOf(counter.getEntityId());
                }

                @Override
                public Object entityMessage(Object message) {
                    return message;
                }

                @Override
                public String shardId(Object message) {
                    int numberOfShards = 100;
                    if (message instanceof MyCounter) {
                        MyCounter counter=(MyCounter) message;
                        return String.valueOf(counter.getEntityId().length() % numberOfShards);
                    } else {
                        return null;
                    }
                }
            };

            // Create an Akka system
            ActorSystem system = ActorSystem.create("ClusterSystem", config);

            Option<String> roleOption = Option.none();
            ClusterShardingSettings settings = ClusterShardingSettings.create(system);
            ClusterSharding.get(system).start("MyEntity", Props.create(MyEntity.class), settings, messageExtractor);
            ActorRef subscriber1 = system.actorOf(Props.create(Subscriber.class), "subscriber");
            ClusterSharding.get(system).start("Subscriber", Props.create(Subscriber.class), settings, messageExtractor);
        }
    }
}
