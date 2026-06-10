package com.example.workassistance.data.remote.model

import com.google.gson.annotations.SerializedName

data class LoginRequest(
    @SerializedName("username") val username: String,
    @SerializedName("password") val password: String,
)

// Sample response:
// {
//   "access_token": "OSw0wqAjVemX4IltZZG12jvWAjzRRZ5QpxIk5CEcX7g",
//   "token_type": "Bearer",
//   "expires_at": "2026-05-28T04:55:12.681078037Z",
//   "user": {
//     "id": "259b7b98",
//     "username": "259b7b98",
//     "role": "ADMIN",
//     "created_at": "2026-05-27T04:09:54.626539Z",
//     "updated_at": "2026-05-27T04:09:54.626539Z"
//   }
// }
data class LoginResponse(
    @SerializedName("access_token") val accessToken: String? = null,
    @SerializedName("token_type") val tokenType: String? = null,
    @SerializedName("expires_at") val expiresAt: String? = null,
    @SerializedName("user") val user: UserResponse? = null,
)
