## Architecture

### Overview

Our project consists of five modules:

1. Main
1. The `messaging` package, which contains a package for API- and one for Peer-To-Peer- (p2p) messaging.
1. The `api` package, which is responsible for the communication to the other modules.
1. The `p2p` package, which maintains the neighbourhood of the peer and spreads knowledge across the network.
1. Networking

### The `messaging` package

The `messaging` package consists of the `api` and `p2p` *sub*packages (not to be confused with the *main* `api`
and `p2p` packages). They each contain:

- Classes representing the message types of the API or P2P protocol, respectively. For the concrete message types of the
  API or P2P protocol, we refer to the *specification* paper of this class. We outline the message types in the *`p2p`
  package* section.
- A superclass from which the message classes inherit.
- An interface providing a method `receive` to receive API or P2P messages, respectively. The `communicator` (**
  specify!!!**) package calls this method to pass the appropriate incoming messages.
  
Additionally, the `p2p` package contains the `Peer` class, representing a peer in the network. Beside other members,
this class contains the peer's address as well as whether the peer is online or not.

### The `api` package

The main logic of the `api` package is in the `APIMessagesManager`. It implements the API communication as specified in
the *specification* paper.  
To receive messaging coming from other modules or peers, the manager implements the listener interface from
the `messaging.api` or `messaging.p2p` package. To send messaging to other modules, the manager also uses
the `communication` module (**specify!!!**).

### The `p2p` package

The `p2p` package consists of the `SpreadManager` class, as well as the `brahms` package. The `SpreadManager` class is
responsible for sending and receiving *spread message*s to or from other peers. Our module uses them to spread knowledge
in the network.

The `brahms` subpackage maintains the peer's neighbourhood by implementing a simplified version of the Brahms algorithm
specified in [*Brahms: byzantine resilient random membership sampling*](https://dl.acm.org/doi/10.1145/1400751.1400772)
by Edward Bortnikov et al.. The Brahms algorithm calls the neighbourhood of a peer its *view*. The algorithm frequently
updates this view from three sources:

1. It queries its neighbours about their view with so called *pull request* messages. Honest neighbours then answer with
   a *pull response* message, containing their view.
2. Additionally, the algorithm listens to other peer's *push request* messages. Each peer uses these requests to ask
   another peer to include them in their view.
3. Finally, each update contains a random subset of the *history* of the peer. This history consists of all the peers
   contained in the push requests or pull responses the peer has ever received. Of course, an implementation never
   maintains the whole history but only the current random subset. Additionally, Brahms regularly ensures that the peers
   in the random subset are still online by sending them *probe request* messages. If there is no *probe response*
   message answering the request, the algorithm removes this peer from the current random subset.

In our implementation, the `View` class maintains the peer's current view. At the startup of our module,
the `Bootstrapper` class initializes the view by asking for pull responses from hardcoded peers. Afterwards, the `View`
class frequently updates its view from three sources:

1. `vPush`. This is a set containing the peers from all the received *push request*s since the last update round.
1. `vPull`. Another set, consisting of the peers from all the *pull response*s since the last update round.
1. The current random subset of the history, provided by the `History` class.

The `History` class holds a list of `Sampler` objects. Each `Sampler` instance is responsible for selecting one of the
elements in the current random subset. It is implemented similar to the pseudocode in the aforementioned paper. However,
we validate the availability of a `Sampler`'s peer differently:
Whenever the `History` asks a `Sampler` for the peer the `Sampler` currently holds, the `Sampler` only returns the peer
if its `online` variable is `true`. To update this variable, our `Sampler` instance is furthermore responsible for
regularly sending *probe request*s to this peer. However, it does not send them directly but rather instructs
the `ProbeManager` to do so.

The remaining classes in the `brahms` package are the `Probe`-, `Pull`- and `PushManager`. They are responsible for
handling the sending and/or receiving of probe, pull or push messages, respectively. They all implement the listener
interface from `messaging.p2p`.

Whenever the `ProbeManager` sends a *probe request* and does not receive a corresponding answer, it sets the appropriate
peer as offline and removes them from the current view.  
The `PullManager` frequently sends *pull request*s to the peer's neighbours. When it does not receive an answer after a
certain timeout, it sends the peer as offline and removes it from the current view. Otherwise, it adds the peers
contained in the *pull response* to the `View.vPull` set.  
To send a *push request*, the sender must always proof some work. This prevents a malicious peer from flooding the
network with *push responses*. Every time, the `PushManager` receives a *push request*, it validates whether the sender
did the obligatory work for the request. Only then it updates the `View.vPush` set.

## P2P Protocol

Our module represents every message of the P2P protocol in two formats: As the instance of a message class (see
section *`messaging` package*) or as a Json bytecode object. The module operates with the first format for passing a
message internally while it uses the second format on the network layer. The P2P protocol consists of all the message
types needed for the Brahms algorithm as well as another message type for spreading knowledge across the network. We
have already described their usage in the section above. In this section, we provide a formal definition of each message
type on the network layer. Each Json message consists of a message type and a body (see figure ...). The message type is
a byte representing the type. When receiving a message, the `networking` package must map the body to an instance to the
correct message class before passing it to other modules. The message type byte tells the package the correct class.

### Message Types

## Spread Message

Message byte type: 0 Json format:

{"message type": 0,
"body": <nested Json object>}


