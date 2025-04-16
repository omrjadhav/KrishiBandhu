package com.example.basicmod.ui.social

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.basicmod.R
import com.example.basicmod.data.repository.PostRepository
import com.example.basicmod.SupabaseApplication
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

class CreatePostFragment : Fragment() {
    private lateinit var imageView: ImageView
    private lateinit var selectImageButton: Button
    private lateinit var descriptionEditText: EditText
    private lateinit var createPostButton: Button
    private lateinit var progressBar: ProgressBar
    private var selectedImageUri: Uri? = null
    private val supabase by lazy {
        (requireActivity().application as SupabaseApplication).supabase
    }
    private val postRepository by lazy {
        PostRepository(supabase)
    }

    companion object {
        private const val PICK_IMAGE_REQUEST = 1
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_create_post, container, false)
        
        imageView = view.findViewById(R.id.imageView)
        selectImageButton = view.findViewById(R.id.selectImageButton)
        descriptionEditText = view.findViewById(R.id.descriptionEditText)
        createPostButton = view.findViewById(R.id.createPostButton)
        progressBar = view.findViewById(R.id.progressBar)

        setupImageSelection()
        setupCreatePost()
        
        return view
    }

    private fun setupImageSelection() {
        selectImageButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, PICK_IMAGE_REQUEST)
        }
    }

    private suspend fun createUserProfile(): String {
        val userId = UUID.randomUUID().toString()
        val userProfile = mapOf(
            "id" to userId,
            "email" to "anonymous${userId.take(8)}@example.com",
            "name" to "Anonymous User",
            "user_type" to "user"
        )
        
        supabase.postgrest["user_profiles"].insert(userProfile)
        return userId
    }

    private fun setupCreatePost() {
        createPostButton.setOnClickListener {
            val description = descriptionEditText.text.toString()
            if (description.isBlank()) {
                Toast.makeText(context, "Please enter a description", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (selectedImageUri == null) {
                Toast.makeText(context, "Please select an image", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            progressBar.visibility = View.VISIBLE
            createPostButton.isEnabled = false
            selectImageButton.isEnabled = false
            descriptionEditText.isEnabled = false

            lifecycleScope.launch {
                try {
                    // First create a user profile
                    val userId = createUserProfile()
                    
                    // Convert Uri to File
                    val imageFile = createTempFileFromUri(selectedImageUri!!)
                    
                    // Upload to Supabase Storage and get URL
                    val imageUrl = postRepository.uploadImage(imageFile)
                    
                    // Create post with image URL and the new user ID
                    val post = postRepository.createPost(
                        userId = userId,
                        imageUrl = imageUrl,
                        description = description
                    )

                    activity?.runOnUiThread {
                        Toast.makeText(context, "Post created successfully!", Toast.LENGTH_SHORT).show()
                        // Delete the temporary file
                        imageFile.delete()
                        // Navigate back
                        findNavController().navigateUp()
                    }
                } catch (e: Exception) {
                    activity?.runOnUiThread {
                        Toast.makeText(context, "Error creating post: ${e.message}", Toast.LENGTH_LONG).show()
                        progressBar.visibility = View.GONE
                        createPostButton.isEnabled = true
                        selectImageButton.isEnabled = true
                        descriptionEditText.isEnabled = true
                    }
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            selectedImageUri = data.data
            Glide.with(this)
                .load(selectedImageUri)
                .centerCrop()
                .into(imageView)
            imageView.visibility = View.VISIBLE
        }
    }

    private fun createTempFileFromUri(uri: Uri): File {
        val inputStream = requireContext().contentResolver.openInputStream(uri)
        val tempFile = File.createTempFile("upload", ".jpg", requireContext().cacheDir)
        FileOutputStream(tempFile).use { outputStream ->
            inputStream?.copyTo(outputStream)
        }
        return tempFile
    }
} 