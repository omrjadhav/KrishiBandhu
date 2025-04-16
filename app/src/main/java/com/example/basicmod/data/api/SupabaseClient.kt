package com.example.basicmod.data.api

import android.os.Looper
import android.util.Log
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.gotrue.GoTrue
import io.github.jan.supabase.gotrue.gotrue
import io.github.jan.supabase.storage.Storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

/**
 * Provides access to the Supabase client instance.
 * The client is lazily initialized to ensure it's created on the main thread.
 * 
 * To use Supabase in your project:
 * 1. Make sure you've created tables 'user_profiles' and 'plants' in your Supabase project
 * 2. Create a storage bucket named 'plant-images' for storing plant images
 * 3. Disable email confirmation in Authentication settings if using for development
 */
object SupabaseClient {
    private const val TAG = "SupabaseClient"
    private const val SUPABASE_URL = "https://kuxowxevkobfiociyclm.supabase.co"
    private const val SUPABASE_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Imt1eG93eGV2a29iZmlvY2l5Y2xtIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDM5MzM5ODgsImV4cCI6MjA1OTUwOTk4OH0.-7vdjDyDM9seQZOg4bL2kynNgS_U3v74bgfliEIkH7w"
    
    // Lazy initialization to ensure client is created on main thread
    val client: SupabaseClient by lazy {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            Log.w(TAG, "Initializing Supabase client from background thread; this may cause issues")
        }
        
        Log.d(TAG, "Creating Supabase client with URL: $SUPABASE_URL")
        
        createSupabaseClient(
            supabaseUrl = SUPABASE_URL,
            supabaseKey = SUPABASE_KEY
        ) {
            install(Postgrest)
            install(GoTrue) {
                // Configure GoTrue for production use
                // Note: Make sure to disable email confirmation in Supabase dashboard for development
            }
            install(Storage)
        }
    }
}

// Access plugins via extension functions like client.gotrue, client.postgrest, etc.
// No need for explicit getters here. 