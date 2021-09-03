## TO-DO:

# Lukas

- receive msgs as Thread or Coroutine?
- pull request with challenge
- set def charset
- make P2PMessage an Interface
- thread safe

# Kyrylo

- check if APIMessage is from own computer (for security reasons). If not, remove.
- log when incoming P2P msg cannot be converted to a P2PMessage obj. Don't crash but ignore the msg then. (for sec
  reasons)