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

### For testing:

- break API connection
- PoW invalid
- invalid
- peer goes offline -> does it disappear from view?
-

## Kyrylo

- check if APIMessage is from own computer (for security reasons). If not, remove.
- log when incoming P2P msg cannot be converted to a P2PMessage obj. Don't crash but ignore the msg then. (for sec
  reasons) (+)
- call channel broken

### For testing:

- make it possible to use only client to listen to incoming p2p msgs