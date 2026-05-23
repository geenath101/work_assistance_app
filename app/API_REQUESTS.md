### GET /sites/employee/{employeeId} — List all sites assigned to an employee

**Request**
```http
GET {{BASE_URL}}/sites/employee/c3d4e5f6-a7b8-9012-cdef-123456789012
```

**Response `200 OK`**
```json
[
  {
    "id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
    "name": "City Tower Office Block",
    "address": "36 Union Place, Colombo 02",
    "latitude": 6.9147,
    "longitude": 79.8554,
    "proximity_radius_m": 150,
    "created_at": "2026-04-28T08:00:00Z",
    "updated_at": "2026-04-28T08:00:00Z"
  }
]
```
