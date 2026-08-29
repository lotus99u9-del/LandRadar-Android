# LandRadar Android authentication

## Status

This repository contains the Android authentication boundary and secure token store.
The UI is a scaffold only. No production authentication provider or backend endpoint is connected yet.

## Components

- `SignInScreen`: collects an email address or phone number and starts OTP login.
- `AuthApi`: provider-neutral contract for request, verify, refresh, and revoke operations.
- `SecureTokenStore`: encrypts access and refresh tokens using a key protected by Android Keystore.
- Backend (not in this repository): verifies OTP, creates the user session, rotates refresh tokens, and authorizes API requests.

## Request flow

1. App sends the identifier to `POST /auth/otp/request` over HTTPS.
2. Backend returns an opaque, short-lived `challengeId`. It must not reveal whether an account already exists.
3. App sends `challengeId + otp` to `POST /auth/otp/verify`.
4. Backend returns a short-lived access token and a rotating refresh token.
5. App stores both through `SecureTokenStore`; OTP is never persisted.
6. Protected API calls use `Authorization: Bearer <access token>`.
7. On HTTP 401 caused by expiry, one synchronized refresh request rotates the refresh token, then the original request is retried once.
8. Sign-out calls the revoke endpoint and clears local encrypted storage even if the network call fails.

## Credential and token rules

- No server secrets, service-role keys, passwords, or signing keys belong in the Android app.
- Public API base URLs may be supplied through build configuration; secrets must remain server-side.
- Never place tokens in URLs, analytics, crash reports, source control, or application logs.
- Access tokens should be short-lived (recommended 5–15 minutes).
- Refresh tokens should be opaque, single-use/rotating, revocable, and bound to the device/session.
- The server must store refresh-token hashes, enforce expiry, rate-limit OTP attempts, and keep an audit trail.
- Use HTTPS only; Android cleartext traffic is disabled in the manifest.
- Production should add Play Integrity/risk checks without treating device attestation as user identity.

## Backend work still required

- Choose the identity provider and supported identifier (phone, email, LINE, or another provider).
- Implement OTP delivery and the four `AuthApi` operations.
- Define account recovery and session/device management.
- Add an authenticated HTTP client with refresh synchronization.
- Add unit, integration, and device tests before enabling real login.
