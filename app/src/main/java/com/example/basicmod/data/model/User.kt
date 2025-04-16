package com.example.basicmod.data.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class User(
    val id: String,
    val email: String,
    @SerialName("user_type")
    val userType: UserType,
    val name: String,
    @SerialName("nursery_name")
    val nurseryName: String? = null,
    @SerialName("owner_name")
    val ownerName: String? = null,
    val created_at: String? = null
) 