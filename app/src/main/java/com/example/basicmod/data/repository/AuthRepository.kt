package com.example.basicmod.data.repository

import android.util.Log
import com.example.basicmod.data.api.SupabaseClient as AppSupabaseClient
import com.example.basicmod.data.model.User
import com.example.basicmod.data.model.UserType
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.gotrue.gotrue
import io.github.jan.supabase.gotrue.providers.builtin.Email
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AuthRepository private constructor(private val supabase: SupabaseClient) {
    private val TAG = "AuthRepository"

    // Flag to use mock implementation while Supabase email confirmation is being fixed
    private val useMockForDevelopment = false

    companion object {
        @Volatile
        private var instance: AuthRepository? = null

        fun getInstance(): AuthRepository {
            return instance ?: synchronized(this) {
                instance ?: AuthRepository(AppSupabaseClient.client).also { instance = it }
            }
        }
    }

    suspend fun login(email: String, password: String): Result<User> {
        return try {
            // Sign in with email and password
            supabase.gotrue.loginWith(Email) {
                this.email = email
                this.password = password
            }
            
            // Get current user ID
            val userId = supabase.gotrue.currentUserOrNull()?.id 
                ?: throw IllegalStateException("User not logged in after sign in")
            
            // Fetch user profile from database
            val userProfile = supabase.postgrest
                .from("user_profiles")
                .select {
                    eq("id", userId)
                }
                .decodeSingle<User>()

            Result.success(userProfile)
        } catch (e: Exception) {
            Log.e(TAG, "Login error: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun registerCustomer(email: String, password: String, name: String): Result<User> {
        // Use real Supabase implementation
        try {
            // Sign up with email and password
            supabase.gotrue.signUpWith(Email) {
                this.email = email
                this.password = password
            }
            
            // To get the user ID, we need to login after signup
            supabase.gotrue.loginWith(Email) {
                this.email = email
                this.password = password
            }
            
            val userId = supabase.gotrue.currentUserOrNull()?.id 
                ?: throw IllegalStateException("User ID not found after sign up")

            // Create user profile with CUSTOMER role
            val user = User(
                id = userId,
                email = email,
                name = name,
                userType = UserType.CUSTOMER
            )

            // Insert user profile into database
            Log.d(TAG, "Inserting customer profile with ID: $userId")
            supabase.postgrest
                .from("user_profiles")
                .insert(mapOf(
                    "id" to userId,
                    "email" to email,
                    "name" to name,
                    "user_type" to UserType.CUSTOMER.toString()
                ))

            return Result.success(user)
        } catch (e: Exception) {
            Log.e(TAG, "Customer registration error: ${e.message}", e)
            return Result.failure(e)
        }
    }

    suspend fun registerOwner(nurseryName: String, email: String, password: String, ownerName: String): Result<User> {
        // Use real Supabase implementation
        try {
            // Sign up with email and password
            supabase.gotrue.signUpWith(Email) {
                this.email = email
                this.password = password
            }
            
            // To get the user ID, we need to login after signup
            supabase.gotrue.loginWith(Email) {
                this.email = email
                this.password = password
            }
            
            val userId = supabase.gotrue.currentUserOrNull()?.id 
                ?: throw IllegalStateException("User ID not found after sign up")

            // Create user profile with OWNER role
            val user = User(
                id = userId,
                email = email,
                name = ownerName,
                userType = UserType.OWNER,
                nurseryName = nurseryName
            )

            // Insert user profile into database
            Log.d(TAG, "Inserting owner profile with ID: $userId")
            supabase.postgrest
                .from("user_profiles")
                .insert(mapOf(
                    "id" to userId,
                    "email" to email,
                    "name" to ownerName,
                    "user_type" to UserType.OWNER.toString(),
                    "nursery_name" to nurseryName
                ))

            return Result.success(user)
        } catch (e: Exception) {
            Log.e(TAG, "Owner registration error: ${e.message}", e)
            return Result.failure(e)
        }
    }

    suspend fun logout() {
        // Sign out current user
        supabase.gotrue.logout()
    }

    suspend fun getCurrentUser(): Result<User?> = withContext(Dispatchers.IO) {
        try {
            // Check if user is logged in
            val session = supabase.gotrue.currentSessionOrNull()
            if (session != null) {
                val userId = session.user?.id ?: return@withContext Result.success(null)
                
                // Fetch user profile from database
                val userProfile = supabase.postgrest
                    .from("user_profiles")
                    .select {
                        eq("id", userId)
                    }
                    .decodeSingle<User>()
                    
                Result.success(userProfile)
            } else {
                Result.success(null)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Get current user error: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    // For development testing only - create a test owner account if needed
    // Returns true if created, false if already existed or failed
    suspend fun createTestOwner(): Boolean {
        try {
            val email = "test@example.com"  // Make sure this matches the email in MyApplication.kt
            val password = "password123"
            
            Log.d(TAG, "Creating/verifying test owner account with email: $email")
            
            // Check if user already exists by trying to log in
            try {
                val loginResult = login(email, password)
                loginResult.fold(
                    onSuccess = {
                        Log.d(TAG, "Test owner account exists and login successful")
                        return false  // Account already exists
                    },
                    onFailure = { error ->
                        if (error.message?.contains("Email not confirmed") == true) {
                            Log.d(TAG, "Account exists but email not confirmed, will try setting up profile anyway")
                            // Continue with the rest of the process to ensure profile exists
                        } else {
                            Log.d(TAG, "Login failed, will create new account: ${error.message}")
                            // Continue with account creation
                        }
                    }
                )
            } catch (e: Exception) {
                Log.d(TAG, "Login check failed, will create new account: ${e.message}")
            }
            
            try {
                // Try to create the account first
                try {
                    Log.d(TAG, "Attempting to create test user")
                    val signUpResponse = supabase.gotrue.signUpWith(Email) {
                        this.email = email
                        this.password = password
                    }
                    
                    Log.d(TAG, "Signup response: $signUpResponse")
                } catch (e: Exception) {
                    // Ignore errors here as the user might already exist
                    Log.d(TAG, "Signup attempt resulted in: ${e.message}")
                }
                
                // Try to log in regardless of signup success (user might already exist)
                var userId: String? = null
                try {
                    supabase.gotrue.loginWith(Email) {
                        this.email = email
                        this.password = password
                    }
                    userId = supabase.gotrue.currentUserOrNull()?.id
                    Log.d(TAG, "Successfully logged in, user ID: $userId")
                } catch (e: Exception) {
                    Log.e(TAG, "Login failed: ${e.message}")
                    
                    // If we can't log in, we can't create the profile
                    if (e.message?.contains("Email not confirmed") == true) {
                        Log.e(TAG, "Email confirmation required. For development, consider enabling 'validateEmailOnSignUp = false' in SupabaseClient.kt")
                    }
                    return false  // Failed to create/login
                }
                
                if (userId == null) {
                    Log.e(TAG, "Failed to get user ID after login")
                    return false  // Failed to get user ID
                }
                
                // Check if user profile already exists
                try {
                    val existingProfiles = supabase.postgrest
                        .from("user_profiles")
                        .select {
                            eq("id", userId)
                        }
                        .decodeList<Map<String, Any>>()
                    
                    if (existingProfiles.isNotEmpty()) {
                        Log.d(TAG, "User profile already exists, no need to create")
                        return false  // Profile already exists
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Error checking for existing profile: ${e.message}")
                }
                
                // Create the user profile record
                val user = User(
                    id = userId,
                    email = email,
                    name = "Test Owner",
                    userType = UserType.OWNER,
                    nurseryName = "Test Nursery"
                )
                
                // Insert user profile into database
                try {
                    Log.d(TAG, "Inserting user profile with ID: $userId")
                    val insertResponse = supabase.postgrest
                        .from("user_profiles")
                        .insert(mapOf(
                            "id" to userId,
                            "email" to email,
                            "name" to "Test Owner",
                            "user_type" to UserType.OWNER.toString(),
                            "nursery_name" to "Test Nursery"
                        ))
                    
                    Log.d(TAG, "Successfully created test owner profile")
                    return true  // Successfully created profile
                } catch (e: Exception) {
                    Log.e(TAG, "Error creating user profile: ${e.message}")
                    return false  // Failed to create profile
                }
                
            } catch (e: Exception) {
                Log.e(TAG, "Error creating test owner account: ${e.message}", e)
                return false  // Exception occurred
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in createTestOwner: ${e.message}", e)
            return false  // Exception occurred
        }
    }
} 