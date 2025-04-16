package com.example.basicmod.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class Post(
    val id: String = UUID.randomUUID().toString(),
    @SerialName("user_id")
    val userId: String,
    @SerialName("image_url")
    val imageUrl: String?,
    val description: String?,
    @SerialName("created_at")
    val createdAt: String? = null,
    val likes: Int = 0,
    val comments: Int = 0
) 