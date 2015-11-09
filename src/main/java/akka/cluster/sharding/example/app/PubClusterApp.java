package akka.cluster.sharding.example.app;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.routing.RoundRobinPool;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

public class PubClusterApp {

  public static void main(String[] args) {
    if (args.length == 0)
      startup(new String[] { "2552" });
    else
      startup(args);
  }

  public static void startup(String[] ports) {
    for (String port : ports) {
      // Override the configuration of the port
      Config config = ConfigFactory.parseString("akka.remote.netty.tcp.port=" + port).withFallback(ConfigFactory.load());
      // Create an Akka system
      ActorSystem system = ActorSystem.create("ClusterSystem", config);
     ActorRef pub = system.actorOf(Props.create(Publisher.class).withRouter(new RoundRobinPool(5)), "publisher");
      try {
        Thread.sleep(20000);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
        String enityId="MyEntity123";
      for(int i=0;i<200;i++){
        System.out.println("Debug..."+i);
        MyCounter myCounter =new MyCounter(enityId,i,"Counter -->"+i);
        pub.tell(myCounter,null);
      }
    }
  }
}
