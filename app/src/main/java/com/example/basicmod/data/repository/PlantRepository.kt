package com.example.basicmod.data.repository

import android.util.Log
import com.example.basicmod.data.api.SupabaseClient as AppSupabaseClient
import com.example.basicmod.data.model.Plant
import com.example.basicmod.util.JsonConfig
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import java.io.File
import java.util.UUID
import java.util.NoSuchElementException

class PlantRepository private constructor(private val supabase: SupabaseClient) {
    private val TAG = "PlantRepository"

    // Flag to use mock implementation while Supabase is being set up
    private val useMockForDevelopment = false

    companion object {
        @Volatile
        private var instance: PlantRepository? = null

        fun getInstance(): PlantRepository {
            return instance ?: synchronized(this) {
                instance ?: PlantRepository(AppSupabaseClient.client).also { instance = it }
            }
        }
    }

    suspend fun getPlants(ownerId: String? = null): Result<List<Plant>> = withContext(Dispatchers.IO) {
        try {
            // Just use the standard decode method which handles JSON properly
            val plants = if (ownerId != null) {
                supabase.postgrest
                    .from("plants")
                    .select {
                        eq("owner_id", ownerId)
                    }
                    .decodeList<Plant>()
            } else {
                supabase.postgrest
                    .from("plants")
                    .select()
                    .decodeList<Plant>()
            }
            
            Result.success(plants)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting plants: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun getPlant(id: String): Result<Plant> = withContext(Dispatchers.IO) {
        try {
            // Use standard decode method which handles the JSON conversion
            val plant = supabase.postgrest
                .from("plants")
                .select {
                    eq("id", id)
                }
                .decodeSingleOrNull<Plant>()
                ?: throw NoSuchElementException("Plant with id $id not found")
            
            Result.success(plant)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting plant $id: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun addPlant(plant: Plant): Result<Plant> {
        return try {
            // Ensure the plant has a valid ID
            val newPlant = if (plant.id.isBlank()) {
                plant.copy(id = UUID.randomUUID().toString())
            } else {
                plant
            }
            
            supabase.postgrest
                .from("plants")
                .insert(newPlant)
            
            Result.success(newPlant)
        } catch (e: Exception) {
            Log.e(TAG, "Error adding plant: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun addPlant(plant: Plant, imageFile: File?): Result<Plant> {
        return try {
            // Ensure the plant has a valid ID
            val plantId = if (plant.id.isBlank()) UUID.randomUUID().toString() else plant.id
            var updatedPlant = plant.copy(id = plantId)
            
            // Upload image to storage if provided
            if (imageFile != null) {
                val path = "plants/$plantId/${imageFile.name}"
                
                supabase.storage
                    .from("plant-images")
                    .upload(path, imageFile.readBytes())
                
                // Get the public URL for the uploaded image
                val imageUrl = supabase.storage
                    .from("plant-images")
                    .publicUrl(path)
                
                updatedPlant = updatedPlant.copy(imageUrl = imageUrl)
            }
            
            // Insert the plant record with image URL
            supabase.postgrest
                .from("plants")
                .insert(updatedPlant)
            
            Result.success(updatedPlant)
        } catch (e: Exception) {
            Log.e(TAG, "Error adding plant with image: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun updatePlant(plant: Plant, imageFile: File?): Result<Plant> {
        return try {
            var updatedPlant = plant
            
            // Upload new image if provided
            if (imageFile != null) {
                val path = "plants/${plant.id}/${imageFile.name}"
                
                supabase.storage
                    .from("plant-images")
                    .upload(path, imageFile.readBytes(), upsert = true)
                
                // Get the public URL for the uploaded image
                val imageUrl = supabase.storage
                    .from("plant-images")
                    .publicUrl(path)
                
                updatedPlant = plant.copy(imageUrl = imageUrl)
            }
            
            // Update the plant record
            supabase.postgrest
                .from("plants")
                .update(updatedPlant) {
                    eq("id", plant.id)
                }
            
            Result.success(updatedPlant)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating plant ${plant.id}: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun deletePlant(id: String): Result<Unit> {
        return try {
            // Delete the plant record
            supabase.postgrest
                .from("plants")
                .delete {
                    eq("id", id)
                }
            
            // Try to delete associated images
            try {
                // First list all files in the plant's directory
                val files = supabase.storage
                    .from("plant-images")
                    .list("plants/$id")
                
                // Delete each file
                for (file in files) {
                    supabase.storage
                        .from("plant-images")
                        .delete("plants/$id/${file.name}")
                }
            } catch (e: Exception) {
                // Log but continue if image deletion fails
                Log.e(TAG, "Error deleting plant images: ${e.message}", e)
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting plant $id: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun getPlantsByNursery(nurseryId: String): Result<List<Plant>> {
        return try {
            val plants = supabase.postgrest
                .from("plants")
                .select {
                    eq("nursery_id", nurseryId)
                }
                .decodeList<Plant>()
            
            Result.success(plants)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting plants for nursery $nurseryId: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    // Create sample plants for development testing if none exist
    suspend fun createSamplePlants(ownerId: String) {
        try {
            // Check if plants already exist
            val existingPlants = getPlants(ownerId).getOrNull()
            if (!existingPlants.isNullOrEmpty()) {
                // Plants already exist for this owner
                return
            }
            
            // Sample plants data
            val plants = listOf(
                Plant(
                    id = UUID.randomUUID().toString(),
                    ownerId = ownerId,
                    name = "Monstera Deliciosa",
                    description = "A beautiful tropical plant with distinctive split leaves",
                    price = 1299.99,
                    quantity = 10,
                    careInstructions = "Water once a week, keep in indirect light"
                ),
                Plant(
                    id = UUID.randomUUID().toString(),
                    ownerId = ownerId,
                    name = "Snake Plant",
                    description = "A hardy plant perfect for beginners",
                    price = 899.99,
                    quantity = 15,
                    careInstructions = "Water sparingly, tolerates low light"
                ),
                Plant(
                    id = UUID.randomUUID().toString(),
                    ownerId = ownerId,
                    name = "Fiddle Leaf Fig",
                    description = "Popular indoor tree with large, violin-shaped leaves",
                    price = 1899.99,
                    quantity = 5,
                    careInstructions = "Consistent watering, bright indirect light"
                )
            )
            
            // Add each plant to the database
            plants.forEach { plant ->
                addPlant(plant)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error creating sample plants: ${e.message}", e)
        }
    }
} 