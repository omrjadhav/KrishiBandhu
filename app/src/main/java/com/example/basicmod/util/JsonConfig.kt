package com.example.basicmod.util

import kotlinx.serialization.json.Json

/**
 * Provides JSON serialization configuration for the app.
 */
object JsonConfig {
    /**
     * Custom JSON configuration that can handle null values by coercing them to defaults.
     * Use this when deserializing data from Supabase to prevent null-related errors.
     */
    val custom = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true  // This allows null values to be coerced to default values
        encodeDefaults = true
        isLenient = true
    }
} 