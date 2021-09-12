## TO-DO:

## Lukas

- receive msgs as Thread or Coroutine?
- pull request with challenge
- set def charset
- thread safe
- synchronize
- encrypt netw traffic
- read parameters from config
- useful difficulty

### final report

- process architecture
- security
  - incoming API connections only own pc
- reread

### For testing:

- break API connection
- PoW invalid
- invalid
- peer goes offline -> does it disappear from view?

## Kyrylo

- check if APIMessage is from own computer (for security reasons). If not, remove. (+)
- log when incoming P2P msg cannot be converted to a P2PMessage obj. Don't crash but ignore the msg then. (for sec
  reasons) (+)
- call channel broken (+)
- write tests for networking

### final report:

- state that networking module calls APICommunicator and P2PCommunicator's receive method when receiving a valid msg (
  check with my explanation to avoid explaining the same twice)
- explain how msgs are send and received (mapping them from object to bytearray and vice versa (e.g. with toByteArray
  fct or JsonMapper))
- include the libraries u used in library table
- describe how to set JAVA_HOME or link to an article about it.
- specify the needed JDK version and describe how to install it
- describe how to only run the project

### For testing:

- make it possible to use only client to listen to incoming p2p msgs