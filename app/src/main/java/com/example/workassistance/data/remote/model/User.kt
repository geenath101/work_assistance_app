package com.example.workassistance.data.remote.model


import com.google.gson.annotations.SerializedName

// Sample response:
// {
//   "id": "259b7b98",
//   "username": "259b7b98",
//   "role": "ADMIN",
//   "created_at": "2026-05-27T04:09:54.626539Z",
//   "updated_at": "2026-05-27T04:09:54.626539Z"
// }
data class UserResponse(
    @SerializedName("id") val id: String,
    @SerializedName("username") val username: String,
    @SerializedName("role") val role: String? = null,
    @SerializedName("created_at") val createdAt: String? = null,
    @SerializedName("updated_at") val updatedAt: String? = null,
)


data class UserRequest(
    @SerializedName("id") val id: String,
)

// Intentionally no "User" type here: app uses com.example.workassistance.data.model.User.
