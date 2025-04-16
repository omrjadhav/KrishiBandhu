package com.example.basicmod.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Comment(
    val id: String,
    @SerialName("user_id")
    val userId: String,
    @SerialName("post_id")
    val postId: String,
    val content: String,
    @SerialName("created_at")
    val createdAt: String? = null
) 