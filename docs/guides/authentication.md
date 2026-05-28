# Authentication Guide

```kotlin
val auth = createAuthClient(pb.client)
```

## Password Auth

```kotlin
auth.authWithPassword("users", "email@example.com", "password")
```

## Refresh

```kotlin
auth.refresh("users")
```

## Common Flows

- OAuth2 code auth: `authWithOAuth2Code(...)`
- OTP request/auth: `requestOtp(...)`, `authWithOtp(...)`
- Password reset: `requestPasswordReset(...)`, `confirmPasswordReset(...)`
- Verification and email change confirmations
- Impersonation: `impersonate(...)`
