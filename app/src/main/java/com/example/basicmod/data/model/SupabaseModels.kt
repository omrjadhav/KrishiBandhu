package com.example.basicmod.data.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class SupabaseUser(
    val id: String,
    val name: String,
    val email: String,
    @SerialName("user_type")
    val userType: String,
    @SerialName("nursery_name")
    val nurseryName: String? = null,
    @SerialName("owner_name")
    val ownerName: String? = null,
    val created_at: String? = null
)

@Serializable
data class Plant(
    val id: String = "",
    @SerialName("owner_id")
    val ownerId: String = "",
    val name: String = "",
    val description: String = "",
    val price: Double = 0.0,
    val quantity: Int = 0,
    @SerialName("care_instructions")
    val careInstructions: String? = null,
    @SerialName("image_url")
    val imageUrl: String? = null,
    val created_at: String? = null
)

@Serializable
data class Order(
    val id: String = "",
    @SerialName("user_id")
    val userId: String = "",
    @SerialName("total_amount")
    val totalAmount: Double = 0.0,
    val status: String = "pending",
    val created_at: String? = null
)

@Serializable
data class OrderItem(
    val id: String = "",
    @SerialName("order_id")
    val orderId: String = "",
    @SerialName("plant_id")
    val plantId: String = "",
    val quantity: Int = 0,
    val price: Double = 0.0,
    val created_at: String? = null
)

@Serializable
data class Revenue(
    val id: String = "",
    @SerialName("owner_id")
    val ownerId: String = "",
    @SerialName("total_amount")
    val totalAmount: Double = 0.0,
    @SerialName("order_count")
    val orderCount: Int = 0,
    val date: String = "",
    val created_at: String? = null
) 