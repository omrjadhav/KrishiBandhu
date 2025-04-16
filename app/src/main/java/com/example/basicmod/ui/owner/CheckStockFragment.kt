package com.example.basicmod.ui.owner

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.basicmod.R
import com.example.basicmod.data.model.Plant
import com.example.basicmod.databinding.FragmentCheckStockBinding
import com.example.basicmod.ui.adapters.PlantAdapter
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class CheckStockFragment : Fragment() {
    private var _binding: FragmentCheckStockBinding? = null
    private val binding get() = _binding!!
    private val viewModel: PlantViewModel by viewModels { PlantViewModel.Factory() }
    private lateinit var plantAdapter: PlantAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCheckStockBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        setupRecyclerView()
        observeViewModel()
        loadPlants()
    }

    private fun setupRecyclerView() {
        plantAdapter = PlantAdapter(
            onEditClick = { plant: Plant ->
                val action = CheckStockFragmentDirections.actionCheckStockFragmentToEditPlantFragment(plant.id)
                findNavController().navigate(action)
            },
            onDeleteClick = { plant: Plant ->
                showDeleteConfirmationDialog(plant)
            }
        )

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = plantAdapter
        }
    }

    private fun observeViewModel() {
        viewModel.plantsState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is PlantsState.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.recyclerView.visibility = View.GONE
                    binding.tvNoStock.visibility = View.GONE
                }
                is PlantsState.Success -> {
                    binding.progressBar.visibility = View.GONE
                    if (state.plants.isEmpty()) {
                        binding.recyclerView.visibility = View.GONE
                        binding.tvNoStock.visibility = View.VISIBLE
                    } else {
                        binding.recyclerView.visibility = View.VISIBLE
                        binding.tvNoStock.visibility = View.GONE
                        plantAdapter.submitList(state.plants)
                    }
                }
                is PlantsState.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.recyclerView.visibility = View.GONE
                    binding.tvNoStock.visibility = View.VISIBLE
                    binding.tvNoStock.text = state.message
                }
                is PlantsState.Initial -> { 
                    binding.progressBar.visibility = View.GONE
                    binding.recyclerView.visibility = View.GONE
                    binding.tvNoStock.visibility = View.GONE
                }
            }
        }

        viewModel.plantActionState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is PlantActionState.Success -> {
                    loadPlants()
                }
                else -> {}
            }
        }
    }

    private fun loadPlants() {
        viewModel.loadPlants()
    }
    
    private fun showDeleteConfirmationDialog(plant: Plant) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.delete_stock)
            .setMessage(R.string.confirm_delete_stock)
            .setPositiveButton(R.string.yes) { _, _ ->
                viewModel.deletePlant(plant.id)
            }
            .setNegativeButton(R.string.no, null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 