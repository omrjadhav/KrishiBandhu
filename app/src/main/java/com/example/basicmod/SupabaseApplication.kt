package com.example.basicmod

import android.app.Application
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.storage.storage

class SupabaseApplication : Application() {
    lateinit var supabase: SupabaseClient
        private set

    override fun onCreate() {
        super.onCreate()
        
        supabase = createSupabaseClient(
            supabaseUrl = BuildConfig.SUPABASE_URL,
            supabaseKey = BuildConfig.SUPABASE_KEY
        ) {
            install(Postgrest)
            install(Storage)
        }
    }
}

// Extension properties to access Postgrest and Storage
val SupabaseClient.postgrest get() = this.postgrest
val SupabaseClient.storage get() = this.storage 