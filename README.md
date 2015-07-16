Toy app to demonstrate that multiple clients taking part in a
zookeeper leader election recipe can think they are leader at any one
time.

# Building

To build:
```
$ ../gradlew jar
```

This will create hanging-chad.jar in build/libs. This is a runnable
jar containing all necessary dependencies.

# Demonstrating the phenomena

Start a zookeeper server. The easiest way is to use docker to start
one in a containers on your local machine.
```
$ docker run -p 2181:2181 jplock/zookeeper
```

Now open multiple terminals and start the hanging-chad program. One
they should all connect to zookeeper and one of them will become
leader. You will know which one is leader, because it will tell you so
in a loop.

```
$ java -jar build/libs/hanging-chad.jar 
log4j:WARN No appenders could be found for logger (org.apache.curator.framework.imps.CuratorFrameworkImpl).
log4j:WARN Please initialize the log4j system properly.
log4j:WARN See http://logging.apache.org/log4j/1.2/faq.html#noconfig for more info.
Leader selector started
I am leader
I am leader
I am leader
I am leader
I am leader
I am leader
...
```

Pause the leader process, using Ctrl-Z. The one of the other processes
should take over leadership after the session timeout (30 seconds),
and start printing out "I am leader". Now unpause the first process
using fg. It will still print out "I am leader" until the zk thread
can tell it otherwise.


```
I am leader
I am leader
^Z
[1]+  Stopped                 java -jar build/libs/hanging-chad.jar
$
$ fg
java -jar build/libs/hanging-chad.jar
I am leader
I am leader
I am leader
I am leader
I am leader
I am leader
I am leader
I am leader
I am leader
I am leader
Ooops, I'm no longer the leader

```
