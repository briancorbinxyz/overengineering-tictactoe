# Contract: TCP Protocol v2

**Scope**: Network message format between GameServer and GameClient

## Version Negotiation

- Protocol version bumped from 1 to 2
- v2 clients receiving v1 messages: default chainLength to board dimension
- v1 clients receiving v2 messages: reject with graceful error (unrecognized version)

## Message Formats

### Start Message (v2)

```json
{
  "version": 2,
  "message": "start",
  "assignedPlayerMarker": "X",
  "chainLength": 3
}
```

**New field**: `chainLength` — the winning chain length for this game session.

### NextMove Message (v2)

```json
{
  "version": 2,
  "message": "nextMove",
  "state": {
    "board": {
      "dimension": 5,
      "chainLength": 3,
      "content": ["X", null, "O", ...]
    },
    "playerMarkers": ["X", "O"],
    "currentPlayerIndex": 0
  }
}
```

**New field in board**: `chainLength` — included in board serialization.

### Exit Message (unchanged)

```json
{}
```

## Backward Compatibility

| Sender | Receiver | Behavior |
|--------|----------|----------|
| v2 server | v2 client | Full support — chainLength transmitted and used |
| v2 server | v1 client | Client rejects v2 start message (unknown version) |
| v1 server | v2 client | Client defaults chainLength to board dimension |
