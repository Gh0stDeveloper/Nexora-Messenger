# Nexora Messenger — implemented feature batch

This batch adds the production-facing structure for the next large Messenger phase.

## Included

- Mexico phone normalization: accepts `+52`, `52`, `+521`, `521` and 10-digit local numbers, then normalizes to `+52XXXXXXXXXX`.
- Debug/local preview session for UI testing while the VPS and Firebase project are not fully active.
- Messenger-style Android UI: phone screen, chat list tabs, cleaner chat detail, rounded bubbles, reduced launcher foreground icon.
- Contacts data model and VPS contacts endpoints.
- Groups data model and VPS group endpoints.
- Status data model and VPS status endpoints.
- Encrypted media upload endpoint on the VPS.
- Signed release workflow with APK signing inputs.
- Nexora emoji-pack foundation with original text/sticker style entries.

## Important security note

The current implementation includes a ratchet foundation and local AES-GCM encryption, but it is not an audited Signal Protocol implementation yet. A production-grade Signal-style design still requires:

- identity key verification,
- signed prekeys,
- one-time prekeys,
- X3DH session setup,
- Double Ratchet message chains,
- skipped-message key cache,
- multi-device sessions,
- sender keys for groups,
- independent security review.

## Emoji assets

Official third-party emoji or sticker artwork is not bundled. Nexora includes its own emoji-pack foundation. If an external pack is used later, only add assets that are licensed for redistribution inside this app.

## Release signing secrets

The signed release workflow expects:

- `GOOGLE_SERVICES_JSON_BASE64`
- `NEXORA_KEYSTORE_BASE64`
- `NEXORA_KEYSTORE_PASSWORD`
- `NEXORA_KEY_ALIAS`
- `NEXORA_KEY_PASSWORD`

Run workflow: `Actions -> Android Release`.
