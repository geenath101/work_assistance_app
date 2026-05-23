package com.example.workassistance.data.model

data class User(
    val id: String,
    val employeeId: String,
    val displayName: String,
    val email: String,
    val photoUrl: String? = null,
    val companyName: String = "WorkAssist Co."
)
