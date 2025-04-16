package com.example.basicmod.ui.owner

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.example.basicmod.R
import com.example.basicmod.data.model.Plant
import com.example.basicmod.databinding.FragmentEditPlantBinding
import com.google.android.material.snackbar.Snackbar
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class EditPlantFragment : Fragment() {
    private var _binding: FragmentEditPlantBinding? = null
    private val binding get() = _binding!!
    private val viewModel: PlantViewModel by viewModels { PlantViewModel.Factory() }
    private val args: EditPlantFragmentArgs by navArgs()
    private var currentPlant: Plant? = null
    private var selectedImageUri: Uri? = null
    private var tempImageFile: File? = null

    private val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            selectedImageUri = uri
            displaySelectedImage(uri)
            tempImageFile = createTempFileFromUri(uri)
        } 
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditPlantBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.title = getString(R.string.edit_plant)
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        binding.btnAddImage.setOnClickListener {
            openImagePicker()
        }

        binding.btnUpdateStock.setOnClickListener {
            validateAndUpdatePlant()
        }

        observeViewModel()
        loadPlantData(args.plantId)
    }

    private fun loadPlantData(plantId: String) {
        viewModel.loadPlants()
    }

    private fun observeViewModel() {
        viewModel.plantActionState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is PlantActionState.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.btnUpdateStock.isEnabled = false
                }
                is PlantActionState.Success -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnUpdateStock.isEnabled = true
                    Snackbar.make(binding.root, state.message, Snackbar.LENGTH_SHORT).show()
                    findNavController().navigateUp()
                }
                is PlantActionState.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnUpdateStock.isEnabled = true
                    Snackbar.make(binding.root, state.message, Snackbar.LENGTH_SHORT).show()
                }
                is PlantActionState.Initial -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnUpdateStock.isEnabled = true
                }
            }
        }

        viewModel.plantsState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is PlantsState.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.btnUpdateStock.visibility = View.GONE
                }
                is PlantsState.Success -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnUpdateStock.visibility = View.VISIBLE
                    val plant = state.plants.find { it.id == args.plantId }
                    plant?.let {
                        currentPlant = it
                        populateFields(it)
                    }
                }
                is PlantsState.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnUpdateStock.visibility = View.VISIBLE
                    Snackbar.make(binding.root, state.message, Snackbar.LENGTH_SHORT).show()
                }
                is PlantsState.Initial -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnUpdateStock.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun populateFields(plant: Plant) {
        binding.etPlantName.setText(plant.name)
        binding.etDescription.setText(plant.description)
        binding.etPrice.setText(plant.price.toString())
        binding.etQuantity.setText(plant.quantity.toString())
        if (!plant.imageUrl.isNullOrEmpty()) {
            Glide.with(this).load(plant.imageUrl).into(binding.ivPlantImage)
            binding.ivPlantImage.visibility = View.VISIBLE
        }
    }

    private fun openImagePicker() {
        pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    private fun displaySelectedImage(uri: Uri) {
        Glide.with(this).load(uri).into(binding.ivPlantImage)
        binding.ivPlantImage.visibility = View.VISIBLE
    }

    private fun createTempFileFromUri(uri: Uri): File? {
        return try {
            val inputStream = requireContext().contentResolver.openInputStream(uri)
            val file = File.createTempFile("plant_image", ".jpg", requireContext().cacheDir)
            val outputStream = FileOutputStream(file)
            inputStream?.copyTo(outputStream)
            inputStream?.close()
            outputStream.close()
            file
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    private fun validateAndUpdatePlant() {
        val name = binding.etPlantName.text.toString().trim()
        val description = binding.etDescription.text.toString().trim()
        val priceStr = binding.etPrice.text.toString().trim()
        val quantityStr = binding.etQuantity.text.toString().trim()
        val careInstructions = currentPlant?.careInstructions ?: ""

        if (name.isBlank() || description.isBlank() || priceStr.isBlank() || quantityStr.isBlank()) {
            Snackbar.make(binding.root, "Please fill all required fields", Snackbar.LENGTH_SHORT).show()
            return
        }
        
        if (currentPlant == null) {
            Snackbar.make(binding.root, "Plant data not available", Snackbar.LENGTH_SHORT).show()
            return
        }

        try {
            val price = priceStr.toDouble()
            val quantity = quantityStr.toInt()

            val updatedPlant = currentPlant!!.copy(
                name = name,
                description = description,
                price = price,
                quantity = quantity,
                careInstructions = careInstructions
            )

            viewModel.updatePlant(updatedPlant, tempImageFile)
        } catch (e: NumberFormatException) {
            Snackbar.make(binding.root, "Please enter valid number values", Snackbar.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 