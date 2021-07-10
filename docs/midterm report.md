## Architecture

### Overview

Our project consists of five modules:

1. Main
1. The `messaging` package, which contains a package for API- and one for Peer-To-Peer- (p2p) messaging.
1. Communicators
1. The `api` package, which is responsible for the communication to the other modules.
1. The `p2p` package, which maintains the neighbourhood of the peer and spreads knowledge across the network.

### The `messaging` package

The `messaging` package consists of the `api` and `p2p` *sub*packages (not to be confused with the *main* `api`
and `p2p` packages). They each contain:

- classes representing the messages of the API or P2P protocol, respectively. For the concrete messages, we refer to
  the *specification* paper of this class, and the *P2P protocol* section below.
- a superclass from which the message classes inherit.
- an interface providing a method `receive` to receive API or P2P messages, respectively. The `communicator` (**
  specify!!!**) package calls this method to pass the appropriate incoming messages.

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