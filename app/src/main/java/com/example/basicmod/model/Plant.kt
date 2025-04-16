package com.example.basicmod.model

data class Plant(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val price: Double = 0.0,
    val quantity: Int = 0,
    val careInstructions: String = ""
) 