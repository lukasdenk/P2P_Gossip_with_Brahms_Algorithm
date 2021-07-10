## Changes to our assumptions from Initial Report

### Defined libraries in use

We found it useful to use [Kotlin Coroutines](https://kotlinlang.org/docs/coroutines-overview.html) 
for networking connections management, 
and [Ini4j](http://ini4j.sourceforge.net) for Windows INI files reading.


## Architecture

### Overview

Our project consists of five modules:

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

The `messaging` package consists of the `api` and `p2p` *sub*packages (not to be confused with the *main* `api`
and `p2p` packages). They each contain:

- classes representing the messages of the API or P2P protocol, respectively. For the concrete messages, we refer to
  the *specification* paper of this class, and the *P2P protocol* section below.
- a superclass from which the message classes inherit.
- an interface providing a method `receive` to receive API or P2P messages, respectively. The `communicator` (**
  specify!!!**) package calls this method to pass the appropriate incoming messages.

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
To receive messaging coming from other modules, the manager implements the listener interface from the `messaging.api`
package. To send messaging to other modules, the manager also uses the `communication` module (**specify!!!**).

### The `p2p` package

The `p2p` package mainly consists of the `P2PStarter` class as well as the packages `brahms` and `manager`.

The `brahms` package maintains the peer's neighbourhood by implementing a simplified version of the Brahms algorithm
specified in [*Brahms: byzantine resilient random membership sampling*](https://dl.acm.org/doi/10.1145/1400751.1400772)
by Edward Bortnikov et al.. The Brahms algorithm calls the neighbourhood of a peer its *view*. The algorithm frequently
updates this view from three sources:

1. It queries its neighbours about their view with so called *pull request*s. Honest neighbours then answer with a *pull
   response*, containing their view.
2. Additionally, the algorithm listens to other peer's *push request*s. Each peer uses these requests to ask another
   peer to include them in their view.
3. Finally, each update contains a random subset of the *history* of the peer. This history consists of all the peers
   contained in the push requests or pull responses the peer has ever received. Of course, an implementation never
   maintains the whole history but only the current random subset. Additionally, Brahms regularly ensures that the peers
   in the random subset are still online by sending them *probe requests*. If there is no *probe response* answering the
   request, the algorithm removes this peer from the current random subset.

It contains a `Probe`-, `Pull`- and `PushManager` to handle the sending and/or receiving of probe, pull or push
messages, respectively. They all implement the listener interface from `messaging.p2p`.

The `View` class maintains the peer's current view. At the startup of our module, the `Bootstrapper` class initializes
the view by asking for pull responses from hardcoded peers. Afterwards, the `View` class frequently updates its view
from three sources:

1. Incoming push requests, provided and verified by the `PushManager`.
1. 


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
