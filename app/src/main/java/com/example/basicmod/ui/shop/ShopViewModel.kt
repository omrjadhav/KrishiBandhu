package com.example.basicmod.ui.shop

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.basicmod.data.model.Plant
import com.example.basicmod.data.repository.PlantRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ShopViewModel : ViewModel() {
    private val plantRepository = PlantRepository.getInstance()

    private val _plants = MutableStateFlow<List<Plant>>(emptyList())
    val plants: StateFlow<List<Plant>> = _plants.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private var allPlants: List<Plant> = emptyList()

    init {
        loadPlants()
    }

    fun loadPlants() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = plantRepository.getPlants()
                result.onSuccess { plants ->
                    allPlants = plants
                    _plants.value = plants
                }.onFailure { exception ->
                    _error.value = exception.message ?: "Failed to load plants"
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun searchPlants(query: String) {
        if (query.isEmpty()) {
            _plants.value = allPlants
            return
        }

        val filteredPlants = allPlants.filter { plant ->
            plant.name.contains(query, ignoreCase = true) ||
            plant.description.contains(query, ignoreCase = true)
        }
        _plants.value = filteredPlants
    }

    class Factory : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ShopViewModel::class.java)) {
                return ShopViewModel() as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
} 