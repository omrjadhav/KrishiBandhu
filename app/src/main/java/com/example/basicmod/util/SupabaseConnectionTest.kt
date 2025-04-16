package com.example.basicmod.util

import android.os.Handler
import android.os.Looper
import android.util.Log
import com.example.basicmod.data.api.SupabaseClient as AppSupabaseClient
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.gotrue.gotrue
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume

/**
 * Utility class to test Supabase connection and functionality
 */
object SupabaseConnectionTest {
    private const val TAG = "SupabaseConnectionTest"
    
    // Get client reference safely, ensuring initialization happens on main thread
    private suspend fun getClientSafely(): SupabaseClient {
        // If we're already on the main thread, just return the client
        if (Looper.myLooper() == Looper.getMainLooper()) {
            return AppSupabaseClient.client
        }
        
        // Otherwise, ensure client is initialized on main thread
        return suspendCancellableCoroutine { continuation ->
            Handler(Looper.getMainLooper()).post {
                val client = AppSupabaseClient.client
                continuation.resume(client)
            }
        }
    }
    
    /**
     * Tests the Supabase connection by performing basic operations with each component
     * @return a map of test results for each component with diagnostic information
     */
    suspend fun testConnection(): Map<String, Pair<Boolean, String>> = withContext(Dispatchers.IO) {
        val results = mutableMapOf<String, Pair<Boolean, String>>()
        
        try {
            // Get client on main thread
            val supabase = getClientSafely()
            
            // Verify the Supabase URL and API key configuration
            try {
                val url = AppSupabaseClient::class.java.getDeclaredField("SUPABASE_URL")
                url.isAccessible = true
                val key = AppSupabaseClient::class.java.getDeclaredField("SUPABASE_KEY")
                key.isAccessible = true
                
                val supabaseUrl = url.get(null) as String
                val supabaseKey = key.get(null) as String
                
                Log.d(TAG, "Supabase URL: $supabaseUrl")
                Log.d(TAG, "Supabase Key (first 10 chars): ${supabaseKey.take(10)}...")
                
                if (supabaseUrl.isBlank() || supabaseKey.isBlank()) {
                    results["config"] = Pair(false, "URL or API key is empty")
                } else {
                    results["config"] = Pair(true, "URL and key found")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to verify Supabase config: ${e.message}", e)
                results["config"] = Pair(false, "Error: ${e.message}")
            }
            
            // Test Auth connection
            try {
                val session = supabase.gotrue.currentSessionOrNull()
                val message = if (session != null) {
                    "User is logged in as: ${session.user?.email}"
                } else {
                    "No active session (normal if no user is logged in)"
                }
                Log.d(TAG, "Auth connection test: $message")
                results["auth"] = Pair(true, message)
            } catch (e: Exception) {
                Log.e(TAG, "Auth connection test failed: ${e.message}", e)
                results["auth"] = Pair(false, "Error: ${e.message}")
            }
            
            // Test Database connection and check table access
            testDatabaseConnection(supabase, results)
            
            // Test Storage connection
            testStorageConnection(supabase, results)
            
        } catch (e: Exception) {
            Log.e(TAG, "General Supabase connection test failed: ${e.message}", e)
            results["general"] = Pair(false, "General error: ${e.message}")
        }
        
        results
    }
    
    private suspend fun testDatabaseConnection(supabase: SupabaseClient, results: MutableMap<String, Pair<Boolean, String>>) {
        try {
            // Check for required tables directly by trying to access them
            val requiredTables = listOf("user_profiles", "plants")
            val existingTables = mutableListOf<String>()
            val missingTables = mutableListOf<String>()
            
            // Check each table individually
            for (table in requiredTables) {
                try {
                    // Try to access the table
                    try {
                        // Just try to get data to see if table exists and we have access
                        val response = supabase.postgrest
                            .from(table)
                            .select()
                            .decodeList<Map<String, Any>>()
                        
                        existingTables.add(table)
                        Log.d(TAG, "Table $table exists with ${response.size} records")
                    } catch (e: Exception) {
                        Log.w(TAG, "Table $table select failed: ${e.message}")
                        
                        // Try a different approach to check if table exists
                        try {
                            // Try a simple query just to see if the table exists
                            supabase.postgrest
                                .from(table)
                                .select()
                                .decodeList<Any>()
                            
                            existingTables.add(table)
                            Log.d(TAG, "Table $table exists but might have no records or restricted access")
                        } catch (e2: Exception) {
                            Log.w(TAG, "Table $table likely doesn't exist: ${e2.message}")
                            missingTables.add(table)
                        }
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Table $table check failed: ${e.message}")
                    missingTables.add(table)
                }
            }
            
            if (missingTables.isEmpty()) {
                Log.d(TAG, "Database connection test: Success (found all required tables)")
                results["database"] = Pair(true, "Found tables: $existingTables")
            } else {
                Log.w(TAG, "Missing required tables: $missingTables")
                results["database"] = Pair(false, "Missing tables: $missingTables. Please run the SQL setup script.")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Database connection test failed: ${e.message}", e)
            results["database"] = Pair(false, "Error: ${e.message}")
        }
    }
    
    private suspend fun testStorageConnection(supabase: SupabaseClient, results: MutableMap<String, Pair<Boolean, String>>) {
        try {
            // Check for the required bucket directly
            try {
                // Try direct access to the bucket since listBuckets may not be available in this version
                val files = supabase.storage.from("plant-images").list()
                Log.d(TAG, "Storage connection test: Success (found ${files.size} files)")
                results["storage"] = Pair(true, "Found plant-images bucket with ${files.size} files")
            } catch (e: Exception) {
                Log.w(TAG, "Missing required bucket plant-images: ${e.message}")
                results["storage"] = Pair(false, "Missing 'plant-images' bucket or no access. Please create it in Supabase.")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Storage connection test failed: ${e.message}", e)
            results["storage"] = Pair(false, "Error: ${e.message}")
        }
    }
    
    /**
     * Get a detailed diagnostic report of the Supabase connection
     */
    suspend fun getDiagnosticReport(): String {
        val results = testConnection()
        val sb = StringBuilder()
        
        sb.appendLine("=== Supabase Connection Diagnostic Report ===")
        
        results.forEach { (component, result) ->
            val (success, message) = result
            val status = if (success) "✅ CONNECTED" else "❌ FAILED"
            sb.appendLine("$component: $status")
            sb.appendLine("  $message")
        }
        
        // Add some troubleshooting help for common issues
        if (results.any { !it.value.first }) {
            sb.appendLine("\n=== Troubleshooting Tips ===")
            sb.appendLine("1. Check that you've set up your Supabase project correctly")
            sb.appendLine("2. Verify that your SUPABASE_URL and SUPABASE_KEY are correct")
            sb.appendLine("3. Make sure you've created the required tables and bucket:")
            sb.appendLine("   - Tables: user_profiles, plants")
            sb.appendLine("   - Storage bucket: plant-images")
            sb.appendLine("4. Check network connectivity")
            sb.appendLine("5. Review the SQL setup script and run it in the Supabase console")
        }
        
        return sb.toString()
    }
    
    /**
     * Get the diagnostic results as a map for UI consumption
     */
    suspend fun getDiagnosticResultsMap(): Map<String, Pair<Boolean, String>> {
        return testConnection()
    }
    
    /**
     * @return true if all components are connected successfully
     */
    suspend fun isConnected(): Boolean {
        val results = testConnection()
        return results.values.all { it.first }
    }
} 