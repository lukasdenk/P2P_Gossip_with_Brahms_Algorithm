# Midterm report

## Changes to our assumptions from Initial Report

### Defined libraries in use

We found it useful to use [Kotlin Coroutines](https://kotlinlang.org/docs/coroutines-overview.html) 
for networking connections management, 
and [Ini4j](http://ini4j.sourceforge.net) for Windows INI files reading.


## Architecture

### Overview

Our project consists of five packages:

1. The `main` module serves as setup function for our service and reads specified console and ini file parameters.
1. The `messaging` package, which contains a package for API- and one for Peer-To-Peer- (p2p) messaging.
1. The `networking` module serves as transport functionality for module-to-module and gossip-to-gossip communication.
1. The `api` package, which is responsible for the communication to the other modules.
1. The `p2p` package, which maintains the neighbourhood of the peer and spreads knowledge across the network.

### The `main` module

The `main` module serves as setup function for our service and consists of `CLI Reader` and `INI Reader` classes.
Specifically, it does following:

- Reads command line arguments
- Reads INI file parameters
- Initializes our communication modules with specified parameters.

### The `messaging` package

The `messaging` package consists of the `api` and `p2p` *sub*packages (not to be confused with the `api`
and `p2p` *main* packages). They each contain:

- Classes representing the message types of the API or P2P protocol, respectively. For the concrete message types of the
  API protocol, we refer to the *specification* paper of this class. For the message types of the P2P protocol, we refer
  to the *`p2p`
  package* section.
- The superclass `APIMessage` or `P2PMessage` from which the API or P2P message classes, respectively, inherit.
- The interface `APIMessageListener` or `P2PMessageListener`, providing a method `receive` to receive API or P2P
  messages, respectively. The `networking` package calls this method to pass incoming API or P2P messages, respectively.

The reason for separating the messages into an own package is that our module uses them across multiple packages.

The `p2p` package additionally contains the `Peer` class, representing a peer in the network. Beside other members, this
class contains the peer's address as well as whether the peer is online or not.

### The `networking` package

`Networking` package serves as transport functionality for module-to-module and gossip-to-gossip communication.
Currently, it supports socket communication (read/write) and connections management 
(restricting amount of incoming connections to api service).

To manage socket communication we have used Kotlin coroutines and Java Non-blocking IO.
These technologies will are explained in paragraph below.

#### Coroutines

We do networking with the help of coroutines. 
Coroutines can be thought of as light-weight threads with a number of differences.
As coroutines is such a broad topic, there will be explained only features we used in current project.
(See [Kotlin Team documentation](https://kotlinlang.org/docs/coroutines-basics.html#your-first-coroutine) 
for detailed explanation).

##### Coroutine Scope
We make use of a structured concurrency approach, making use of Coroutine Scope. 
Coroutine Scope is a parent scope for all coroutines created with its help, 
if we create 5 coroutines for module-to-module connections inside Communication Scope. 
We can easily stop all the connections by simply stopping Communication Scope.
In this way we do not have to keep track of created jobs.

##### How we use coroutine scope
In the current state of development, 
we create one coroutine per module-to-module connection and keep it in Service Scope.
In addition, we are planning to add functionality for one-message-connections, 
to get or initiate a connection, receive or send a message, close the socket, and finish the coroutine.

##### Asynchronous Socket Communication
For socket communication use standard Java Non-blocking I/O library, 
which is the most efficient choice for multi-service communication. 
Instead of writing code for waiting for a message and blocking threads, 
we define handlers that will start their work as soon as action 
(Message receiving, connection establishment) happens. 
In this way, we delegate message receiving and sending to the java library.
We take care of the most important part - writing, reading, reacting to failure events.

### The `api` package

The main logic of the `api` package is in the `APIMessagesManager`. It implements the API communication as specified in
the *specification* paper.  
To receive messaging coming from other modules, the manager implements the `APIMessageListener` interface.  
The manager is also responsible for forwarding incoming knowledge to the modules which have subscribed for it.
Therefore, it also implements the `P2PMessageListener` interface. The `networking` package is also liable for calling
the `APIMessagesManager`'s `channelClosed` method whenever the connection to another module breaks. If necessary,
the `APIMessagesManager` then unsubscribes the corresponding module.  
To send messages to other modules, the manager uses the `networking`
package. Furthermore, it forwards validated knowledge of *Gossip Notification*s via the `SpreadManager` with a so
called *spread message* (see section `p2p` package).

### The `p2p` package

The `p2p` package consists of the `SpreadManager` class, as well as the `brahms` package. The `SpreadManager` class is
responsible for sending and receiving *spread message*s of the P2P protocol to or from other peers. Our module uses them
to spread knowledge in the network. The `brahms` subpackage maintains the peer's neighbourhood by implementing a
simplified version of the Brahms algorithm specified in [*Brahms: byzantine resilient random membership
sampling*](https://dl.acm.org/doi/10.1145/1400751.1400772)
by Edward Bortnikov et al..

#### The Brahms algorithm

As in the Brahms paper, we also refer to a peer's neighbourhood as its *view*. The Brahms algorithm frequently updates
this view from three sources:

1. It queries its neighbours about their view with so called *pull request* messages. Honest neighbours then answer with
   a *pull response* message, containing their view.
2. Additionally, the algorithm listens to other peer's *push request* messages. Each peer uses these requests to ask
   another peer to include it in their view.
3. Finally, each update contains a random subset of the *history* of the peer. This history consists of all the peers
   contained in the push requests or pull responses the peer has ever received. Of course, an implementation never
   maintains the whole history but only the current random subset. Additionally, Brahms regularly ensures that the peers
   in the random subset are still online by sending them *probe request* messages. If there is no *probe response*
   message answering the request, the algorithm removes this peer from the current random subset.

#### Message types of the P2P protocol

Our module represents every message of the P2P protocol in two formats: As the instance of a message class or as a Json
object. The module operates with the first format for passing a message internally while it uses the second format on
the network layer. We describe the latter format in the *P2P protocol* section. When we mention a message in the *
Architecture* section, we always talk about the instance of a message class. The message classes are:

- `SpreadMsg`
- `PullRequest` and `PullResponse`
- `PushRequest`
- `ProbeRequest` and `ProbeResponse`

As already mentioned, our module uses the `SpreadMsg` to spread knowledge across the network. A peer needs the other
messages to maintain its view with the Brahms algorithm (see section *The Brahms algorithm*).

#### Implementation of the Brahms algorithm

In our implementation, the `View` class maintains the peer's current view. At the startup of our module,
the `Bootstrapper` class initializes the view by asking for pull responses from hardcoded peers. Afterwards, the `View`
class frequently updates its view from three sources:

1. `vPush`. This is a set containing the peers from all the received push requests since the last update round.
1. `vPull`. A set consisting of the peers from all the pull responses since the last update round.
1. The current random subset of the history, provided by the `History` class.

The `History` class holds a list of `Sampler` objects. Each `Sampler` instance is responsible for selecting one of the
elements in the current random subset. It is implemented similar to the pseudocode in the aforementioned paper. However,
we validate the online status of a `Sampler`'s peer differently:
Whenever the `History` asks a `Sampler` for the peer the `Sampler` currently holds, the `Sampler` only returns the peer
if its `online` variable is `true`. To update this variable, our `Sampler` instance is furthermore responsible for
regularly sending probe requests to this peer. However, it does not send them directly but rather instructs
the `ProbeManager` to do so.

The remaining classes in the `brahms` package are the `Probe`-, `Pull`- and `PushManager`. They are responsible for
handling the sending and/or receiving of probe, pull or push messages, respectively. They all implement
the `P2PMessageListener` interface.  
Whenever the `ProbeManager` sends a probe request and does not receive a corresponding answer before a timeout, it sets
the appropriate peer as offline and removes it from the current view.  
The `PullManager` frequently sends pull requests to the peer's neighbours. If it does not receive an answer after a
timeout, it sends the peer as offline and removes it from the current view. Otherwise, it adds the peers contained in
the pull responseto the `View.vPull` set.  
The `PushManager` regularly sends push requests to the peer's neighbourhood. To send a push request, the sender must
always proof some work. This prevents a malicious peer from flooding the network with push responses. Therefore, before
sending such a request, the manager must first hash the sender and receiver address, the current date and hour, and a
nonce. The nonce must be chosen so that the resulting hash starts with a certain number of leading 0 bits. Every time
the `PushManager` receives a push request, it validates whether hashing the mentioned values results in a correct hash.
Only then it updates the `View.vPush` set.

## P2P Protocol

In this section, we provide a precise definition of the message formats on the network layer. The peers transport them
as Json objects. The `networking` package maps an incoming Json message to an instance of the message class it is
associated with. It then forwards the instance to each `APIMessageListener`. On the other hand, when another class
instructs the `networking` package to send a message instance, the mapping proceeds in the other direction. In this
case, it maps the instance to a Json object before sending it to the message's destination.

### Core Message

When the `networking` package receives a Json message, it needs to know to which message class it should map the message
to. Therefore, each Json message starts with the message type. The *body* field then contains the actual data of the
message. Hence, each message is in the following format:

| Field name   | data type   | meaning                                            |
|--------------|-------------|----------------------------------------------------|
| message type | integer     | integer, indicating the type of the message        |
| body         | Json object | described in the next subsection |

### Body types of the core message

In this section, we outline the different body formats, representing the different message types.

#### Spread Message

Message type integer: 0

| Field name | data type  | meaning                                             |
|------------|------------|-----------------------------------------------------|
| dataType   | integer    | the data type field, as specified in *GossipAnnounce* |
| ttl        | integer    | TTL, as specified in *GossipAnnounce*                 |
| data       | byte array | data, as specified in *GossipAnnounce*                |

#### Pull Request

Message type integer: 1

| Field name | data type     | meaning                                                                    |
|------------|---------------|----------------------------------------------------------------------------|
| limit       | integer | the maximum number of peers to send with the corresponding *pull response* |

#### Pull Response

Message type integer: 2

| Field name | data type     | meaning                                                                    |
|------------|---------------|----------------------------------------------------------------------------|
| view       | list of peers | the peer's view, with a maximum size of the limit of the corresponding *pull request*|

Where a peer has the following format:

| Field name | data type | meaning                    |
|------------|-----------|----------------------------|
| ip address | string    | the IP address of the peer |
| port       | integer   | port number of the peer    |

#### Push Request

Message type integer: 3

| Field name | data type  | meaning                             |
|------------|------------|-------------------------------------|
| nonce      | byte array | the nonce proofing the sender's work |

#### Probe Request

Message type integer: 4

Empty body

#### Probe Response

Message type integer: 5

Empty body 

## Future Work. Features we could not finish so far.

- We are planning to add functionality for one-message-connections, to get or initiate a connection, 
  receive or send a message, close the socket, and finish the coroutine in our communication module.
- After that, we combine communication and peer-to-peer protocol modules.

## Workload Distribution

- Kyrylo Vasylenko: Main Module and Communication Service

## Effort spent for the project

- Kyrylo Vasylenko
    - It took me up to two weeks to create the communication module in its current state.
1/3 of the time was allocated for investigation of Java Non-Blocking I/O and coroutines approach.
    - Almost 2/3 of the time was implementation (1/3) and debugging (1/3). I was setting up a python voip environment and communicating with a gossip client and gossip mockup, handling message\connection failures in the Communication Module.
    - And I needed up to a day to create command line and INI file parsing modules.
- Before starting to implement the Communication Module, I studied the following resources, which helped me to completely understand Async IO in Java and Coroutines approach in Kotlin.
    - Non-blocking IO
        - https://www.baeldung.com/java-io-vs-nio
    - Why use coroutines
        - https://elizarov.medium.com/blocking-threads-suspending-coroutines-d33e11bf4761
    - Coroutine Scope
        - https://elizarov.medium.com/structured-concurrency-722d765aa952
        - https://elizarov.medium.com/the-reason-to-avoid-globalscope-835337445abc
