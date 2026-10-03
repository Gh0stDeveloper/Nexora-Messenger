# Nexora Messenger Architecture

## Overview

Nexora Messenger is split into two deployable units:

1. **Android app**: identity, profile, local cache, local encryption and UI.
2. **VPS backend**: authenticated relay, PostgreSQL persistence, avatar storage and backups.

```text
Android App ──Firebase ID Token──► VPS Relay API ──► PostgreSQL
     │                                │
     ├─ Firebase Auth OTP             ├─ uploads/avatars
     ├─ Firebase Messaging            ├─ Nginx/TLS
     ├─ Room                          └─ Docker Compose
     └─ Android Keystore
```

## Android layers

```text
ui/screens      Compose surfaces
ui/viewmodel    screen state and async work
data/repository business operations
data/remote     relay HTTP client
data/local      Room cache
data/crypto     local identity/encryption primitives
data/messaging  FCM service
```

## Backend layers

```text
Express middleware  Firebase ID token verification
Routes              profile/avatar/message/chat endpoints
PostgreSQL          users/chats/messages
Uploads volume      avatars now, encrypted media later
Scripts             deploy/backup/restore
```

## Message flow

1. User writes plaintext inside `ChatDetailScreen`.
2. `ChatDetailViewModel` calls `ChatRepository.sendTextMessage`.
3. `ChatRepository` encrypts locally with AES-GCM.
4. Android sends `encryptedPayload + iv` to the VPS relay.
5. Backend verifies Firebase ID token.
6. Backend stores ciphertext in PostgreSQL.
7. Receiver syncs chats/messages and receives FCM notification.

## Current limitations

- AES-GCM payload encryption is local-first protection against server plaintext.
- Full multi-device E2EE is still pending.
- Contact discovery by phone is pending.
- Media messages are pending.
