package com.example.basicmod.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.basicmod.R
import com.example.basicmod.data.model.Plant
import com.example.basicmod.databinding.ItemPlantBinding
import java.text.NumberFormat
import java.util.Locale

class PlantAdapter(
    private val onEditClick: (Plant) -> Unit,
    private val onDeleteClick: (Plant) -> Unit
) : ListAdapter<Plant, PlantAdapter.PlantViewHolder>(PlantDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlantViewHolder {
        val binding = ItemPlantBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PlantViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PlantViewHolder, position: Int) {
        val plant = getItem(position)
        holder.bind(plant)
    }

    inner class PlantViewHolder(private val binding: ItemPlantBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(plant: Plant) {
            binding.apply {
                plantName.text = plant.name
                plantDescription.text = plant.description
                plantPrice.text = formatPrice(plant.price)
                
                // Load plant image using Glide
                Glide.with(plantImage)
                    .load(plant.imageUrl)
                    .placeholder(R.drawable.ic_plant_placeholder)
                    .error(R.drawable.ic_plant_placeholder)
                    .centerCrop()
                    .into(plantImage)

                // Set up edit and delete buttons
                addToCartButton.text = "Edit"
                addToCartButton.setOnClickListener { onEditClick(plant) }
                
                // Add a delete button if needed
                // You might need to add this to your layout
            }
        }
        
        private fun formatPrice(price: Double): String {
            val format = NumberFormat.getCurrencyInstance(Locale.US)
            return format.format(price)
        }
    }

    class PlantDiffCallback : DiffUtil.ItemCallback<Plant>() {
        override fun areItemsTheSame(oldItem: Plant, newItem: Plant): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Plant, newItem: Plant): Boolean {
            return oldItem == newItem
        }
    }
} 