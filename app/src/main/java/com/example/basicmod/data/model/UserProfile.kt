package com.example.basicmod.data.model

data class UserProfile(
    val id: String,
    val userId: String,
    val name: String,
    val userType: UserType,
    val nurseryName: String? = null
) 