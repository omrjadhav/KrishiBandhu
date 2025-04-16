package com.example.basicmod

import android.app.Application
import android.util.Log
import android.widget.Toast
import com.example.basicmod.data.api.SupabaseClient
import com.example.basicmod.data.repository.AuthRepository
import com.example.basicmod.data.repository.PlantRepository
import com.example.basicmod.util.JsonConfig
import com.example.basicmod.util.SupabaseConnectionTest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

class MyApplication : Application() {
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val mainScope = MainScope()
    private val TAG = "MyApplication"
    
    override fun onCreate() {
        super.onCreate()
        
        // Configure global JSON settings
        val json = Json { 
            ignoreUnknownKeys = true
            coerceInputValues = true
            encodeDefaults = true 
            isLenient = true
        }
        
        // Test Supabase connection and setup test data
        applicationScope.launch {
            // Add a small delay to ensure the application is fully initialized
            delay(2000)
            
            // Test Supabase connection
            val isConnected = SupabaseConnectionTest.isConnected()
            Log.d(TAG, "Supabase connection test result: $isConnected")
            
            if (!isConnected) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MyApplication, 
                        "Could not connect to Supabase. Check logs for details.", 
                        Toast.LENGTH_LONG).show()
                }
                return@launch
            }
            
            // Setup test account and data
            setupTestAccountAndData()
        }
    }
    
    private suspend fun setupTestAccountAndData() {
        try {
            val authRepository = AuthRepository.getInstance()
            val plantRepository = PlantRepository.getInstance()
            
            // Create a test owner account if it doesn't exist
            val testOwnerCreated = authRepository.createTestOwner()
            
            if (testOwnerCreated) {
                Log.d(TAG, "Test owner account created successfully")
                
                // Login with the test account
                val loginResult = authRepository.login("test@example.com", "password123")
                
                if (loginResult.isSuccess) {
                    Log.d(TAG, "Logged in with test account")
                    
                    // Get the current user ID
                    val userId = authRepository.getCurrentUser().getOrNull()?.id
                    if (userId != null) {
                        // Create sample plants for this owner
                        plantRepository.createSamplePlants(userId)
                        Log.d(TAG, "Created sample plants for test account")
                    }
                } else {
                    Log.e(TAG, "Failed to login with test account: ${loginResult.exceptionOrNull()?.message}")
                }
            } else {
                Log.d(TAG, "Test owner account already exists or could not be created")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up test account and data: ${e.message}", e)
        }
    }
} 