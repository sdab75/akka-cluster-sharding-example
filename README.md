# akka-cluster-sharding-example

I am struggling to get the persistent actor and akka cluster sharding “supervisor” combination work. 

The cluster sharding ‘without’ akka supervisor works fine i.e. in my Subscriber if I get the sharded region entity via “ActorRef myEntity = ClusterSharding.get(getContext().system()).shardRegion("MyEntity");” and use the myEntity reference to forward the received messages to my persistent actor entity “MyEntity.class”. 

However, this approach doesn’t fire the parent (‘Subscriber’ actor) supervisor strategy.

According to the Akka documentation (http://doc.akka.io/docs/akka/2.4.0/java/cluster-sharding.html). I am supposed to forward received message via parent actor “ActorRef myEntity = getContext().actorOf(Props.create(MyEntity.class), "myEntity");”. This works  as long as I have ‘one Subscriber node’ in the cluster (i.e. on exception, parent actor supervisor strategy gets invoked), the minute we add an additional ‘Subscriber’ node the persistence goes havoc and does only partial persistence i.e. if I publish 10 messages I only 6 or 7 messages getting persisted. 

I have checked in the code in my git hub. Please let me know if If the cluster sharding supervisor is fully tested in akka distribute pub/sub scenario for akka 2.4.0.I followed the documentation from this URL (http://doc.akka.io/docs/akka/2.4.0/java/cluster-sharding.html). 

**Example setup**

1.	The PubCluster publishes a topic named ‘content’ that has myCounter instance. Currently PubClusterApp uses pub actor with round robbin pool of 5 ( ‘ActorRef pub = system.actorOf(Props.create(Publisher.class).withRouter(new RoundRobinPool(5)), "publisher");’.).
2.	There are 2 subscriber apps (SubClusterApp1, SubClusterApp2)both uses the same Subscriber uses the same group ‘grp1’. 

**Sucessful scenario with one ‘Subscriber’ node in the cluster.**

1.	Start SubClusterApp1 (akka 2550)
2.	Start PubClusterApp (akka 2552)

This will perfectly save all the messages and and also notifies the supervisor on the exception.

**Failure scenario** 

when I start 2 subcriber nodes I see both nodes recieveing messages but only few messages are getting saved.

1.	Start SubClusterApp1 (akka 2550)
2.	Start SubClusterApp1 (akka 2551)
3.	Start PubClusterApp (akka 2552)

If the cluster sharding based fault tolerance doesn’t work in a multi node scenarios then I am not sure how far we can use akka for production scenarios. Any help in clarifying the issue appreciated.
