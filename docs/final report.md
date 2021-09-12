# Final report

## Implementation

### Overview

Our project consists of five packages:

1. The `main` package serves as setup function for our service and reads specified console and ini file parameters.
1. The `networking` package serves as transport functionality for API and Peer-To-Peer (P2P) communication.
1. The `messaging` package, which contains a package for API- messaging as well as one for P2P messaging.
1. The `api` package, which is responsible for the communication to the other modules.
1. The `p2p` package, which maintains the neighbourhood of the peer and spreads knowledge across the network.

### The Main Package

The `main` package serves as setup function for our service and consists of `CLI Reader` and `INI Reader` classes.
Specifically, it does following:

- Reads command line arguments
- Reads INI file parameters
- Initializes our communication modules with specified parameters.

### The Networking Package

`Networking` package serves as transport functionality for module-to-module and P2P communication. Currently, it
supports socket communication (read/write) and connections management
(restricting amount of incoming connections to api service).

To manage socket communication we have used Kotlin coroutines and Java Non-blocking IO. These technologies will are
explained in paragraph below.

#### Coroutines

We do networking with the help of coroutines. Coroutines can be thought of as light-weight threads with a number of
differences. As coroutines is such a broad topic, there will be explained only features we used in current project.
(See [Kotlin Team documentation](https://kotlinlang.org/docs/coroutines-basics.html#your-first-coroutine)
for detailed explanation).

##### Coroutine Scope

We make use of a structured concurrency approach, making use of Coroutine Scope. Coroutine Scope is a parent scope for
all coroutines created with its help, if we create 5 coroutines for module-to-module connections inside Communication
Scope. We can easily stop all the connections by simply stopping Communication Scope. In this way we do not have to keep
track of created jobs.

##### How We Use Coroutine Scope

In the current state of development, we create one coroutine per module-to-module connection and keep it in Service
Scope. In addition, we are planning to add functionality for one-message-connections, to get or initiate a connection,
receive or send a message, close the socket, and finish the coroutine.

##### Asynchronous Socket Communication

For socket communication use standard Java Non-blocking I/O library, which is the most efficient choice for
multi-service communication. Instead of writing code for waiting for a message and blocking threads, we define handlers
that will start their work as soon as action
(Message receiving, connection establishment) happens. In this way, we delegate message receiving and sending to the
java library. We take care of the most important part - writing, reading, reacting to failure events.

#### Communication with API and P2P packages

Every time networking module receives a valid message, it converts it into Kotlin object: `messaging.p2p.P2PMgs` or `messaging.api.APIMsg`.
The api or p2p message then passed to the APICommunicator or P2PCommunicator respectively. Invalid messages are ignored.
If API channel is closed `api.manager.GossipManager` gets notified about it with `channelClosed` function.

#### Process architecture

We have two processes running: P2PService and APIService. Their addresses are defined as api_address and
p2p_address accordingly. They are started as two different coroutines.

In addition, if we need to send a P2P message, we create a process `OneWayMessageClient` to do so.
It opens a socket connection with stated peer, sends message to it then closes the connection and stops the process.
`OneWayMessageClient` is also starts as coroutine.

##### Messages mapping from byte array to objects and to byte array from objects

##### API messages

Socket connections not from our own machine are refused. So attackers cannot fake our own modules.

To map API messages into objects we manually parse ByteBuffer's content into APIMsg object with `fromByteBuffer` static
function that implemented in every APIMsg class.

To map API messages to byte array, we manually create byte array from objects according to the protocol specification 
with `toByteArray()` function that implemented in every APIMsg object.

##### P2P messages

To map P2P messages into objects we use `json.JsonMapper` that makes use of `kotlinx.serialization` library.
We convert our objects into json, which is string format, then it is converted into byte array and sent over 
the network.

To map P2P messages to byte array, we use `json.JsonMapper` that makes use of `kotlinx.serialization` library.
We convert our byte arrays to string, then we convert resulting json into P2PMsg with `Json.mapFromJson` function. 

### The Messaging Package

The `messaging` package consists of the `api` and `p2p` *sub*packages (not to be confused with the `api`
and `p2p` *main* packages). They each contain:

- Classes representing the message types of the API or P2P protocol, respectively. For the concrete message types of the
  API protocol, we refer to the *specification* paper of this class. For the message types of the P2P protocol, we refer
  to the *`p2p` package* section.
- The superclass `APIMsg` or `P2PMsg` from which the API or P2P message classes, respectively, inherit.
- The interface `APIMsgListener` or `P2PMsgListener`, providing a method `receive` to receive API or P2P messages,
  respectively. The `APICommunicator` (see subsection _The API Package_) or `P2PCommunicator` (see subsection _The P2P Package_) calls this function to pass an incoming API or P2P
  message, whenever it receives one from the `networking` package.
- The singletons `APICommunicator` and `P2PCommunicator`. They each serve as an abstraction layer between the `networking` and the `api` or `p2p` package, respectively. Other classes can use their `send` function to send an `APIMsg` or `P2PMsg`, respectively. Whenever the `networking` package receives an API or P2P message, it forwards them to the `APICommunicator` or `P2PCommunicator`, respectively. They then forward the message to all instances implementing the `APIMsgListener` or `P2PMsgListener`, respectively.

The reason for separating these classes into an own package is that our module uses them across multiple packages.

The `p2p` package additionally contains the `Peer` class. An object of this class represents a peer in the network. It
contains the address of the socket the peer is listening on.


### The API Package

The main logic of the `api` package is in the `GossipManager`. It implements the API communication as specified in the *specification* paper. It has the following responsibilities:
- Receiving messages coming from other modules. For this reason, the manager implements the `APIMsgListener` interface.
- Keeping track of the data types the other modules have notified for. The manager does so by mapping each module to the data types it has subscribed for. Also, it implements the `channelClosed` method. The `networking` package calls is whenever the connection to another module breaks. The `GossipManager` then unsubscribes the corresponding module.
- Passing knowledge coming from other peers to the modules which have subscribed for it. To receive messages from other peers, it implements the `P2PMsgListener` interface.  
- Forwarding data to other peers. To do so, the manager stores incoming *spread messages* with a unique ID. It then sends a `GOSSIP NOTIFICATION` message to the modules which have notified for the corresponding data type. If a module sends a `GOSSIP VALIDATION` with the valid flag set to `true`, the manager spreads this message to our peers.


### The P2P Package

The `p2p` package maintains the peer's neighbourhood by implementing a simplified version of the Brahms algorithm
specified in [*Brahms: byzantine resilient random membership sampling*](https://dl.acm.org/doi/10.1145/1400751.1400772)
by Edward Bortnikov et al.. We chose this approach since the Brahms algorithm provides a solid resilience against Byzantine and Crash faults (see the aforementioned paper).

#### The Brahms Algorithm

As in the Brahms paper, we also refer to a peer's neighbourhood as its *view*. The Brahms algorithm frequently updates
this view from three sources:

- It queries its neighbours about their view with so called *pull request* messages. Honest neighbours then answer with
  a *pull response* message, containing their view.
- Additionally, the algorithm listens to other peer's *push request* messages. Each peer uses these requests to ask
  another peer to include it in their view.
- Finally, each update contains a random subset of the *history* of the peer. This history consists of all the peers
  contained in the push requests or pull responses the peer has ever received. Of course, an implementation never
  maintains the whole history but only the current random subset. Additionally, Brahms regularly ensures that the peers
  in the random subset are still online by sending them *probe request* messages. If there is no *probe response*
  message answering the request, the algorithm removes this peer from the current random subset.

#### Message Types of the P2P Protocol

Our module represents every message of the P2P protocol in two formats: As the instance of a message class or as a Json
object. The module operates with the first format for passing a message internally while it uses the second format on
the network layer. We describe the latter format in the *The P2P Protocol* section. When we mention a message in the *Architecture* section, we always talk about the instance of a message class. The message classes are:

- `SpreadMsg`
- `PullRequest` and `PullResponse`
- `PushRequest`

As already mentioned, our module uses the `SpreadMsg` to spread knowledge across the network. A peer needs the other
messages to maintain its view with the Brahms algorithm (see section *The Brahms algorithm*). Our P2P protocol does not
contain a *probe request* or *probe response* message. Instead, we check the availability of a peer directly on the
network level by trying to connect to the peer's socket.

#### Implementation of the Brahms Algorithm

In our implementation, the `View` singleton maintains the peer's current view. At the startup of our module, we
initialize the view with the bootstrapping peers provided in the INI file. A coroutine frequently updates the `View`
singleton from three sources:

- `PushManager.push`. This is a set containing the peers from all the received push requests since the last update
  round.
- `PullManager.pull`. A set consisting of the peers from all the pull responses since the last update round.
- The current random subset of the history, provided by the `History` singleton.

The `History` singleton holds a list of `Sampler` objects. Each `Sampler` instance is responsible for selecting one of
the elements in the current random subset. The `Sampler` class is implemented similar to the pseudocode in the
Brahms paper. It holds the peer it is currently selecting as well as a random number.   
For each peer received in a *push message* or *pull response*, the `History` singleton calls each `Sampler`'s `next` function. As a function parameter, we hand over the received peer. If the `Sampler` is currently not
selecting a peer, the received peer becomes the selected peer. Otherwise, the function hashes the peer's address as well
as the random number with the SHA256 algorithm. If this hash is smaller than the hash of the currently selected peer,
the received peer becomes the new selected peer. With this strategy, a `Sampler` always holds a peer selected uniformly
at random from all the peers it has received so far. The random number ensures that the different `Sampler`s choose
different peers.  
Additionally, a `Sampler` must check the availability status of the peer it holds. This is done with a coroutine which
periodically tries to connect to the peer. The `Sampler`'s construction phase launches this coroutine. Whenever the
associated peer is offline, the `Sampler` resets itself. It removes the peer and creates a new random number.

The remaining classes in the `brahms` package are the aforementioned `Pull`- and `PushManager`. They are responsible for
handling the sending and/or receiving of pull or push messages, respectively. They both implement the `P2PMsgListener`
interface. The `PullManager` frequently sends pull requests to the peer's neighbours. If it does not receive an answer
after a timeout, it removes it from the current view. Otherwise, it adds the peers
contained in the pull response to the `View.vPull` set.  
The `PushManager` regularly sends push requests to the peer's neighbourhood. To send a push request, the sender must
always proof some work. This prevents a malicious peer from flooding the network with push responses. Therefore, before
sending such a request, the manager must first hash the sender and receiver address, the current time, in meticulous
precision, as well as a nonce. The nonce must be chosen so that the resulting hash starts with a certain number of
leading 0 bits. Every time the `PushManager` receives a push request, it validates whether hashing the mentioned values
results in a correct hash. It tries the last few minutes as the time parameter. Only then it updates the `View.vPush` set.

### The P2P Protocol

In this section, we provide a definition of the message formats on the network layer. The peers transport them
as Json objects. The `networking` package maps an incoming Json message to an instance of the message class the Json
message is associated with. It then forwards the instance to the `P2PCommunicator`. When an entity instructs
the `networking` package to send a message instance, the mapping proceeds in the other direction. In this case, it maps
the instance to a Json object before sending it to the message's destination.

#### Core Message

When the `networking` package receives a Json message, it needs to know to which message class it should map the message
to. Therefore, each Json message starts with the message type. Furthermore, it contains the address on which the sender
listens to incoming messages. Hence, each message is in the following format:

| Field Name   | Data Type   | Meaning                                            |
|--------------|-------------|----------------------------------------------------|
| type | string     | The type of the message.        |
|sender|Peer|As descibed in section *The Messaging Package*.
| remaining fields         | | Described in the next subsections. |

A peer has the following format:

| Field Name | Data Type | Meaning                    |
|------------|-----------|----------------------------|
| ip address | string    | The IP address of the peer. |
| port       | integer   | The port number of the peer.    |

#### Body Types of the Core Message

In this section, we outline the different body formats, representing the different message types.

**_Spread Message:_**

Message type string: *SpreadMsg*

| Field Name | Data Type  | Meaning                                             |
|------------|------------|-----------------------------------------------------|
| dataType   | integer    | The data type field, as specified in *GOSSIP ANNOUNCE*. |
| ttl        | integer    | The TTL field, as specified in *GOSSIP ANNOUNCE*.                 |
| data       | byte array | The data field, as specified in *GOSSIP ANNOUNCE*.                |

**_Pull Request:_**

Message type string: *PullRequest*

| Field Name | Data Type     | Meaning                                                                    |
|------------|---------------|----------------------------------------------------------------------------|
| limit       | integer | The maximum number of peers to send with the corresponding *pull response*. |

**_Pull Response:_**

Message type string: *PullResponse*

| Field Name | Data Type     | Meaning                                                                    |
|------------|---------------|----------------------------------------------------------------------------|
| view       | list of peers | The peer's view, with a maximum size of the limit of the corresponding *pull request*.|

**_Push Message:_**

Message type string: *PushMsg*

| Field Name | Data Type  | Meaning                             |
|------------|------------|-------------------------------------|
| nonce      | integer | The nonce proofing the sender's work. |


### Used libraries

| Library                                                                                                                                     | Usage                                                        |
|---------------------------------------------------------------------------------------------------------------------------------------------|--------------------------------------------------------------|
| [org.jetbrains.kotlin.jvm](https://plugins.gradle.org/plugin/org.jetbrains.kotlin.jvm)                                                      | Compiles Kotlin code to the JVM.                             |
| [org.jetbrains.kotlin.plugin.serialization' version  '1.5.30](https://plugins.gradle.org/plugin/org.jetbrains.kotlin.plugin.serialization) | Maps P2P messages to kotlin objects  and vice versa.          |
| [org.jetbrains.kotlinx:kotlinx-serialization-json:1.2.2](https://github.com/Kotlin/kotlinx.serialization)                                   | See above.                                                   |
| [org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.1-native-mt](https://github.com/Kotlin/kotlinx.coroutines)                               | Provides coroutines, as described in section **Coroutines**
architecture*. |
| [com.github.johnrengelman.shadow](https://github.com/johnrengelman/shadow)                                                                  | Plugin to build jar file.                                    |



### Changes to our assumptions in the midterm report

None.

### Future Work

Several configuration parameters were only tested in a small environment. A deeper evaluation could improve the performance and robustness of our module.   
This applies to the following parameters:
- Difficulty: The number of leading 0 bits the hash of a `PushMsg` must have.
- Update Interval: The frequency with which our module updates its view.
- Probe Interval: The regularity in which a `Sampler` probes the peer it is currently selecting. 
- Push Limit: The maximal number of push messages we accept in an update round. If the incoming push messages exceed this limit, the view does not update in this round.

### Disclaimer
Our module sends network traffic as plain text. For this reason, message encryption and authentication must be done on a lower layer.



## Deployment

### How to install java on your machine to follow section "How to build and run project"

1. Download java version 16 following instructions on
   1. https://java.tutorials24x7.com/blog/how-to-install-java-16-on-windows for Windows
   2. https://docs.oracle.com/en/java/javase/16/install/installation-jdk-macos.html#GUID-E8A251B6-D9A9-4276-ABC8-CC0DAD62EA33 for Mac OS
   3. https://docs.oracle.com/en/java/javase/16/install/installation-jdk-linux-platforms.html#GUID-19D58769-FD72-4353-A935-40FCD82A7A81 for Linux

### How to build and run project

1. To build the project:
   1. For MacOS Terminal
      1. Run from the project root folder `sh deployment/mac/build.sh`
   2. For Windows PowerShell
      1. Run from the project root folder `.\deployment\windows\build.ps1`
   3. For Windows CMD
      1. Run from root project folder `.\deployment\windows\build.cmd`
2. Then, if you need to provide your own `.ini` settings, you have two options
   1. Change existing `service.ini` file
      1. For MacOS Terminal
         1. in `deployment/mac/service.ini`
      2. For Windows
          1. in `deployment\windows\service.ini`
   2. Create your own `ini` settings file
3. Run jar file
    1. For MacOS Terminal
        1. `java -jar deployment/mac/Gossip-1.0-SNAPSHOT-all.jar -c deployment/mac/service.ini`
            1. If you use your own `.ini` file, put it in the command above instead of `deployment/mac/service.ini`.
    2. For Windows
        1. `java -jar deployment\windows\Gossip-1.0-SNAPSHOT-all.jar -c deployment\windows\service.ini`
            1. If you use your own `.ini` file, put it in the command above instead of `deployment\windows\service.ini`.


## Testing

### API Protocol

**Disclaimer:** Only works on Windows 10.

1. Build the project as described in section *How to build and run project*.
2. Install Python3 and the latest Pip on your machine. 
3. Open a terminal with administrator rights and go to the project root.
4. Install all dependencies with `pip install -r .\api_testing\requirements.txt`.
5. Make sure that port 7000-7002 and 7050-7052 are available. Now, start 3 instances of our module by executing `.\api_testing\test_servers.cmd`. Wait for about 10 seconds so that the modules can find each other.
6. Go to the project root and run `.\api_testing\gossip_client.py --help`. Read the instructions to test the functionality you want to test.

### Networking part

The `networking` part was tested manually by using test class `ClientMain.kt`.
`ClientMain.kt` was sending one way messages to `APIService` and `P2PService` and we ensured they
were encrypted.

## Workload Distribution

### Kyrylo Vasylenko

Kyrylo Vasylenko implements `main` and `networking` packages.

### Lukas Denk

Lukas Denk implements the `messaging`, `p2p` and `api` packages.

## Effort Spent for the Project

### Kyrylo Vasylenko

Kyrylo Vasylenko spent up to 19 hours for `networking` package implementation. About 6 hours were used for studying Java
Non-Blocking I/O and coroutines approach. About 8 hours were spent on `networking` module manual testing with use of
python gossip client and gossip mockup. And up to 5 hours were put into command line and windows INI file parsing
modules.

- Resources that were studied about Async IO in Java and Coroutines approach in Kotlin.
    - [Java Non-blocking IO](https://www.baeldung.com/java-io-vs-nio)
    - [Why use coroutines](https://elizarov.medium.com/blocking-threads-suspending-coroutines-d33e11bf4761)
    - [Structured concurrency](https://elizarov.medium.com/structured-concurrency-722d765aa952)
    - [How to properly use Coroutine Scope](https://elizarov.medium.com/the-reason-to-avoid-globalscope-835337445abc)

After midterm report Kyrylo Vasylenko spent up to 20 hours on `networking` package further implementation, bug fixes and
jar file building pipeline. Approximately 17 out of 20 hours went on functionality and scripts creation, and about 3
hours were invested into documenting everything for internal use.

### Lukas Denk

*Before Midterm:* About 3 hours for research and design, about 12 hours for the implementation, about 8 hours for
documentation.

*After Midterm:* About 8 hours for the implementation, about 8 hours for documentation and about 8 hours for testing
and debugging.