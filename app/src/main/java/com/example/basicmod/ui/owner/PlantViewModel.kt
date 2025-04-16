package com.example.basicmod.ui.owner

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.basicmod.data.model.Plant
import com.example.basicmod.data.repository.AuthRepository
import com.example.basicmod.data.repository.PlantRepository
import kotlinx.coroutines.launch
import java.io.File

sealed class PlantActionState {
    object Initial : PlantActionState()
    object Loading : PlantActionState()
    data class Success(val message: String) : PlantActionState()
    data class Error(val message: String) : PlantActionState()
}

sealed class PlantsState {
    object Initial : PlantsState()
    object Loading : PlantsState()
    data class Success(val plants: List<Plant>) : PlantsState()
    data class Error(val message: String) : PlantsState()
}

class PlantViewModel : ViewModel() {
    private val plantRepository = PlantRepository.getInstance()
    private val authRepository = AuthRepository.getInstance()

    private val _plantActionState = MutableLiveData<PlantActionState>(PlantActionState.Initial)
    val plantActionState: LiveData<PlantActionState> = _plantActionState

    private val _plantsState = MutableLiveData<PlantsState>(PlantsState.Initial)
    val plantsState: LiveData<PlantsState> = _plantsState

    init {
        viewModelScope.launch {
            // Create test owner account
            authRepository.createTestOwner()
            
            // Create test plants
            authRepository.getCurrentUser().getOrNull()?.let { user ->
                plantRepository.createSamplePlants(user.id)
            }
        }
    }

    fun addPlant(plant: Plant, imageFile: File? = null) {
        viewModelScope.launch {
            _plantActionState.value = PlantActionState.Loading
            try {
                val result = if (imageFile != null) {
                    plantRepository.addPlant(plant, imageFile)
                } else {
                    plantRepository.addPlant(plant)
                }
                result.fold(
                    onSuccess = {
                        _plantActionState.value = PlantActionState.Success("Plant added successfully")
                    },
                    onFailure = {
                        _plantActionState.value = PlantActionState.Error(it.message ?: "Failed to add plant")
                    }
                )
            } catch (e: Exception) {
                _plantActionState.value = PlantActionState.Error(e.message ?: "Failed to add plant")
            }
        }
    }

    fun updatePlant(plant: Plant, imageFile: File? = null) {
        viewModelScope.launch {
            _plantActionState.value = PlantActionState.Loading
            try {
                val result = plantRepository.updatePlant(plant, imageFile)
                result.fold(
                    onSuccess = {
                        _plantActionState.value = PlantActionState.Success("Plant updated successfully")
                    },
                    onFailure = {
                        _plantActionState.value = PlantActionState.Error(it.message ?: "Failed to update plant")
                    }
                )
            } catch (e: Exception) {
                _plantActionState.value = PlantActionState.Error(e.message ?: "Failed to update plant")
            }
        }
    }

    fun deletePlant(plantId: String) {
        viewModelScope.launch {
            _plantActionState.value = PlantActionState.Loading
            try {
                val result = plantRepository.deletePlant(plantId)
                result.fold(
                    onSuccess = {
                        _plantActionState.value = PlantActionState.Success("Plant deleted successfully")
                    },
                    onFailure = {
                        _plantActionState.value = PlantActionState.Error(it.message ?: "Failed to delete plant")
                    }
                )
            } catch (e: Exception) {
                _plantActionState.value = PlantActionState.Error(e.message ?: "Failed to delete plant")
            }
        }
    }

    fun loadPlants() {
        viewModelScope.launch {
            _plantsState.value = PlantsState.Loading
            try {
                val result = plantRepository.getPlants()
                result.fold(
                    onSuccess = {
                        _plantsState.value = PlantsState.Success(it)
                    },
                    onFailure = {
                        _plantsState.value = PlantsState.Error(it.message ?: "Failed to load plants")
                    }
                )
            } catch (e: Exception) {
                _plantsState.value = PlantsState.Error(e.message ?: "Failed to load plants")
            }
        }
    }

    fun getCurrentUserId(callback: (String?) -> Unit) {
        viewModelScope.launch {
            try {
                val result = authRepository.getCurrentUser()
                callback(result.getOrNull()?.id)
            } catch (e: Exception) {
                callback(null)
            }
        }
    }

    fun resetActionState() {
        _plantActionState.value = PlantActionState.Initial
    }

    class Factory : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(PlantViewModel::class.java)) {
                return PlantViewModel() as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
} 