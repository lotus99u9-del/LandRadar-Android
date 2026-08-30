# LandRadar property API contract

The Android app now has a provider-neutral connector at
`data/remote/LandRadarApiClient.kt`.

## Endpoint

`GET /v1/properties`

Response body:

```json
[
  {
    "id": "LED-123",
    "title": "ที่ดินพร้อมสิ่งปลูกสร้าง",
    "district": "เมืองเชียงใหม่",
    "province": "เชียงใหม่",
    "priceBaht": 1850000,
    "areaRai": 0.75,
    "auctionDate": "2026-09-15",
    "latitude": 18.7883,
    "longitude": 98.9853
  }
]
```

## Security rules

- The Android client accepts HTTPS only.
- Provider/service-role credentials must remain in the LandRadar backend.
- The app may send only a short-lived member access token.
- Do not expose credentials belonging to the Legal Execution Department in APK resources, BuildConfig, logs, or source control.
- The backend must normalize and validate coordinates before returning them.

## Activation

Set `LANDRADAR_API_BASE_URL` only to the LandRadar-owned backend gateway.
It is intentionally empty until the real backend is deployed. The current UI
continues using isolated preview data and cannot accidentally call an unknown server.
