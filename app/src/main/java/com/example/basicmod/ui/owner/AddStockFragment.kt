package com.example.basicmod.ui.owner

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.basicmod.BuildConfig
import com.example.basicmod.R
import com.example.basicmod.data.model.Plant
import com.example.basicmod.databinding.FragmentAddStockBinding
import com.example.basicmod.ui.auth.AuthState
import com.example.basicmod.ui.auth.AuthViewModel
import com.google.android.material.snackbar.Snackbar
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class AddStockFragment : Fragment() {
    private var _binding: FragmentAddStockBinding? = null
    private val binding get() = _binding!!
    private val viewModel: PlantViewModel by viewModels { PlantViewModel.Factory() }
    private val authViewModel: AuthViewModel by viewModels { AuthViewModel.Factory() }
    
    private var selectedImageUri: Uri? = null
    private var tempImageFile: File? = null
    
    // Image picker result launcher
    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                selectedImageUri = uri
                displaySelectedImage(uri)
                
                // Create a temp file from the uri
                createTempFileFromUri(uri)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddStockBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
        
        // Set up image picker button
        binding.btnAddImage.setOnClickListener {
            openImagePicker()
        }

        // Set up add stock button
        binding.btnAddStock.setOnClickListener {
            validateAndAddPlant()
        }
        
        // Observe plant action state
        viewModel.plantActionState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is PlantActionState.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.btnAddStock.isEnabled = false
                }
                is PlantActionState.Success -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnAddStock.isEnabled = true
                    Snackbar.make(view, "Stock added successfully", Snackbar.LENGTH_SHORT).show()
                    clearFields()
                    findNavController().navigateUp()
                    viewModel.resetActionState()
                }
                is PlantActionState.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnAddStock.isEnabled = true
                    Snackbar.make(view, state.message, Snackbar.LENGTH_SHORT).show()
                    viewModel.resetActionState()
                }
                else -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnAddStock.isEnabled = true
                }
            }
        }
    }
    
    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        pickImageLauncher.launch(intent)
    }
    
    private fun displaySelectedImage(uri: Uri) {
        binding.ivPlantImage.visibility = View.VISIBLE
        Glide.with(requireContext())
            .load(uri)
            .centerCrop()
            .into(binding.ivPlantImage)
    }
    
    private fun createTempFileFromUri(uri: Uri) {
        try {
            val inputStream = requireContext().contentResolver.openInputStream(uri)
            tempImageFile = File.createTempFile("plant_", ".jpg", requireContext().cacheDir)
            
            inputStream?.use { input ->
                FileOutputStream(tempImageFile).use { output ->
                    input.copyTo(output)
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(requireContext(), "Failed to process image", Toast.LENGTH_SHORT).show()
        }
    }

    private fun validateAndAddPlant() {
        val name = binding.etPlantName.text.toString().trim()
        val description = binding.etDescription.text.toString().trim()
        val priceStr = binding.etPrice.text.toString().trim()
        val quantityStr = binding.etQuantity.text.toString().trim()
        val careInstructions = ""

        if (name.isBlank() || description.isBlank() || priceStr.isBlank() || quantityStr.isBlank()) {
            Snackbar.make(binding.root, "Please fill in all fields", Snackbar.LENGTH_SHORT).show()
            return
        }

        val price = priceStr.toDoubleOrNull()
        val quantity = quantityStr.toIntOrNull()

        if (price == null || quantity == null) {
            Snackbar.make(binding.root, "Invalid price or quantity", Snackbar.LENGTH_SHORT).show()
            return
        }

        viewModel.getCurrentUserId { userId ->
            if (userId == null) {
                Snackbar.make(binding.root, "You must be logged in to add plants", Snackbar.LENGTH_SHORT).show()
                return@getCurrentUserId
            }

            val plant = Plant(
                id = UUID.randomUUID().toString(),
                name = name,
                description = description,
                price = price,
                quantity = quantity,
                careInstructions = careInstructions,
                ownerId = userId
            )

            viewModel.addPlant(plant, tempImageFile)
        }
    }
    
    private fun clearFields() {
        binding.etPlantName.text?.clear()
        binding.etDescription.text?.clear()
        binding.etPrice.text?.clear()
        binding.etQuantity.text?.clear()
        binding.ivPlantImage.visibility = View.GONE
        selectedImageUri = null
        tempImageFile = null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 