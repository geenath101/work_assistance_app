package com.example.workassistance.data.remote.model

import com.google.gson.annotations.SerializedName

data class Site(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("address") val address: String,
    @SerializedName("latitude") val latitude: Double,
    @SerializedName("longitude") val longitude: Double,
    @SerializedName("proximity_radius_m") val radiusMeters: Double
)
