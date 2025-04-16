package com.example.basicmod.data.repository

import android.util.Log
import com.example.basicmod.data.model.Post
import com.example.basicmod.data.model.Like
import com.example.basicmod.data.model.Comment
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.storage.storage
import io.github.jan.supabase.postgrest.query.Order
import java.io.File
import java.util.UUID

class PostRepository(
    private val supabaseClient: SupabaseClient
) {
    private val postgrest = supabaseClient.postgrest
    private val storage = supabaseClient.storage
    
    private val TAG = "PostRepository"
    private val BUCKET_NAME = "plant-images"
    private val POSTS_TABLE = "posts"
    private val LIKES_TABLE = "likes"
    private val COMMENTS_TABLE = "comments"

    // Function to upload image to Supabase Storage
    suspend fun uploadImage(file: File): String {
        try {
            val fileName = "${UUID.randomUUID()}.jpg"
            val bytes = file.readBytes()
            Log.d(TAG, "Uploading image to bucket: $BUCKET_NAME with filename: $fileName")
            
            storage.from(BUCKET_NAME).upload(
                path = fileName,
                data = bytes,
                upsert = false
            )
            
            val publicUrl = storage.from(BUCKET_NAME).publicUrl(fileName)
            Log.d(TAG, "Image uploaded successfully. Public URL: $publicUrl")
            return publicUrl
        } catch (e: Exception) {
            Log.e(TAG, "Error uploading image: ${e.message}", e)
            throw e
        }
    }

    // Function to create a new post
    suspend fun createPost(userId: String?, imageUrl: String?, description: String?): Post {
        try {
            // Generate a random UUID if userId is not provided or invalid
            val actualUserId = try {
                if (userId.isNullOrBlank()) UUID.randomUUID().toString()
                else UUID.fromString(userId).toString()
            } catch (e: IllegalArgumentException) {
                UUID.randomUUID().toString()
            }

            val post = Post(
                id = UUID.randomUUID().toString(),
                userId = actualUserId,
                imageUrl = imageUrl,
                description = description
            )
            Log.d(TAG, "Creating post: $post")
            
            postgrest[POSTS_TABLE].insert(post)
            
            Log.d(TAG, "Post created successfully")
            return post
        } catch (e: Exception) {
            Log.e(TAG, "Error creating post: ${e.message}", e)
            throw e
        }
    }

    // Function to get all posts
    suspend fun getPosts(): List<Post> {
        try {
            Log.d(TAG, "Fetching all posts")
            val posts = postgrest[POSTS_TABLE]
                .select {
                    order("created_at", Order.DESCENDING)
                }
                .decodeList<Post>()
            Log.d(TAG, "Fetched ${posts.size} posts")
            return posts
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching posts: ${e.message}", e)
            throw e
        }
    }

    // Function to like a post
    suspend fun likePost(userId: String, postId: String): Like {
        try {
            val like = Like(
                id = UUID.randomUUID().toString(),
                userId = userId,
                postId = postId
            )
            Log.d(TAG, "Creating like: $like")
            postgrest[LIKES_TABLE].insert(like)
            return like
        } catch (e: Exception) {
            Log.e(TAG, "Error liking post: ${e.message}", e)
            throw e
        }
    }

    // Function to unlike a post
    suspend fun unlikePost(userId: String, postId: String) {
        try {
            Log.d(TAG, "Removing like for userId: $userId, postId: $postId")
            postgrest[LIKES_TABLE]
                .delete {
                    eq("user_id", userId)
                    eq("post_id", postId)
                }
        } catch (e: Exception) {
            Log.e(TAG, "Error unliking post: ${e.message}", e)
            throw e
        }
    }

    // Function to comment on a post
    suspend fun commentOnPost(userId: String, postId: String, content: String): Comment {
        try {
            val comment = Comment(
                id = UUID.randomUUID().toString(),
                userId = userId,
                postId = postId,
                content = content
            )
            Log.d(TAG, "Creating comment: $comment")
            postgrest[COMMENTS_TABLE].insert(comment)
            return comment
        } catch (e: Exception) {
            Log.e(TAG, "Error creating comment: ${e.message}", e)
            throw e
        }
    }

    // Function to get comments for a post
    suspend fun getComments(postId: String): List<Comment> {
        try {
            Log.d(TAG, "Fetching comments for postId: $postId")
            return postgrest[COMMENTS_TABLE]
                .select {
                    eq("post_id", postId)
                    order("created_at", Order.DESCENDING)
                }
                .decodeList()
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching comments: ${e.message}", e)
            throw e
        }
    }

    // Function to get likes for a post
    suspend fun getLikes(postId: String): List<Like> {
        try {
            Log.d(TAG, "Fetching likes for postId: $postId")
            return postgrest[LIKES_TABLE]
                .select {
                    eq("post_id", postId)
                }
                .decodeList()
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching likes: ${e.message}", e)
            throw e
        }
    }
} 