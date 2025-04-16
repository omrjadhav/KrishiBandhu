package com.example.basicmod.ui.owner

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.basicmod.databinding.ItemStockBinding
import com.example.basicmod.data.model.Plant
import java.text.NumberFormat
import java.util.Locale

class StockAdapter(
    private val onEditClick: (Plant) -> Unit,
    private val onDeleteClick: (Plant) -> Unit
) : ListAdapter<Plant, StockAdapter.StockViewHolder>(StockDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StockViewHolder {
        val binding = ItemStockBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return StockViewHolder(binding)
    }

    override fun onBindViewHolder(holder: StockViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class StockViewHolder(
        private val binding: ItemStockBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.btnEdit.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onEditClick(getItem(position))
                }
            }

            binding.btnDelete.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onDeleteClick(getItem(position))
                }
            }
        }

        fun bind(plant: Plant) {
            binding.apply {
                tvPlantName.text = plant.name
                tvDescription.text = plant.description
                tvPrice.text = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
                    .format(plant.price)
                tvQuantity.text = "Quantity: ${plant.quantity}"
                
                // Load image if available
                if (!plant.imageUrl.isNullOrEmpty()) {
                    Glide.with(binding.root.context)
                        .load(plant.imageUrl)
                        .centerCrop()
                        .into(binding.ivPlantImage)
                }
            }
        }
    }

    private class StockDiffCallback : DiffUtil.ItemCallback<Plant>() {
        override fun areItemsTheSame(oldItem: Plant, newItem: Plant): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Plant, newItem: Plant): Boolean {
            return oldItem == newItem
        }
    }
} 