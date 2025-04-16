package com.example.basicmod.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.basicmod.R
import com.example.basicmod.databinding.FragmentProfileBinding
import com.example.basicmod.ui.auth.AuthViewModel
import com.example.basicmod.ui.auth.AuthState

class ProfileFragment : Fragment() {
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private val authViewModel: AuthViewModel by viewModels { AuthViewModel.Factory() }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set up logout button click listener
        binding.btnLogout.setOnClickListener {
            authViewModel.logout()
        }

        // Observe auth state changes
        authViewModel.authState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is AuthState.LoggedOut -> {
                    // Navigate back to user type selection
                    findNavController().navigate(R.id.action_navigation_profile_to_userTypeSelectionFragment)
                }
                is AuthState.Error -> {
                    // Handle error if needed
                }
                else -> {
                    // Handle other states if needed
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 