package com.example.basicmod.ui.owner

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.basicmod.R
import com.example.basicmod.databinding.FragmentOwnerDashboardBinding
import com.example.basicmod.ui.auth.AuthViewModel

class OwnerDashboardFragment : Fragment() {
    private var _binding: FragmentOwnerDashboardBinding? = null
    private val binding get() = _binding!!
    private val authViewModel: AuthViewModel by viewModels { AuthViewModel.Factory() }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOwnerDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set up click listeners for the dashboard cards
        binding.cardCheckStock.setOnClickListener {
            findNavController().navigate(R.id.checkStockFragment)
        }

        binding.cardAddStock.setOnClickListener {
            findNavController().navigate(R.id.addStockFragment)
        }

        binding.cardRevenue.setOnClickListener {
            findNavController().navigate(R.id.revenueFragment)
        }
        
        // Set up logout button
        binding.btnLogout.setOnClickListener {
            authViewModel.logout()
            findNavController().navigate(R.id.action_ownerDashboardFragment_to_userTypeSelectionFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 