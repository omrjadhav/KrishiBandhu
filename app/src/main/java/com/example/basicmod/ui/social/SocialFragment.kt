package com.example.basicmod.ui.social

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.basicmod.R
import com.example.basicmod.data.model.Post
import com.example.basicmod.data.repository.PostRepository
import com.example.basicmod.SupabaseApplication
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch
import androidx.navigation.fragment.findNavController
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.storage.storage

class SocialFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var fabCreatePost: FloatingActionButton
    private val posts = mutableListOf<Post>()
    private val postRepository by lazy {
        val supabase = (requireActivity().application as SupabaseApplication).supabase
        PostRepository(supabase)
    }
    private lateinit var postAdapter: PostAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_social, container, false)
        
        recyclerView = view.findViewById(R.id.recyclerView)
        fabCreatePost = view.findViewById(R.id.fabCreatePost)
        
        setupRecyclerView()
        setupFab()
        fetchPosts()
        
        return view
    }

    private fun setupRecyclerView() {
        postAdapter = PostAdapter(posts)
        recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = postAdapter
        }
    }

    private fun setupFab() {
        fabCreatePost.setOnClickListener {
            findNavController().navigate(R.id.action_socialFragment_to_createPostFragment)
        }
    }

    private fun fetchPosts() {
        lifecycleScope.launch {
            try {
                val fetchedPosts = postRepository.getPosts()
                posts.clear()
                posts.addAll(fetchedPosts)
                postAdapter.notifyDataSetChanged()
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    override fun onResume() {
        super.onResume()
        fetchPosts() // Refresh posts when returning to this fragment
    }
} 