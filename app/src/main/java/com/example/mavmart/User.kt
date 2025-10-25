package com.example.mavmart

data class User(
    val id: Long = 0L,
    val first: String,
    val last: String,
    val email: String,
    val password: String,
    val role: Role = Role.User
)