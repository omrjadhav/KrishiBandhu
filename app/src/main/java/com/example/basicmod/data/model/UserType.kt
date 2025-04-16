package com.example.basicmod.data.model

import kotlinx.serialization.Serializable

@Serializable
enum class UserType {
    CUSTOMER,
    OWNER
} 