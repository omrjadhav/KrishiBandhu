package com.example.basicmod.ui.social

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.basicmod.R
import com.example.basicmod.data.model.Post

class PostAdapter(private val posts: List<Post>) : RecyclerView.Adapter<PostAdapter.PostViewHolder>() {
    class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.postImageView)
        val descriptionTextView: TextView = itemView.findViewById(R.id.postDescriptionTextView)
        val likeButton: Button = itemView.findViewById(R.id.likeButton)
        val commentButton: Button = itemView.findViewById(R.id.commentButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_post, parent, false)
        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = posts[position]
        holder.descriptionTextView.text = post.description
        // Load image using a library like Glide or Picasso
        // holder.imageView.setImageResource(post.imageUrl)
        holder.likeButton.setOnClickListener {
            // Logic to like the post
        }
        holder.commentButton.setOnClickListener {
            // Logic to open comment section
        }
    }

    override fun getItemCount() = posts.size
} 