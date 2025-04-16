package com.example.basicmod.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.basicmod.R
import com.example.basicmod.databinding.FragmentRegisterBinding
import kotlinx.coroutines.launch

class RegisterFragment : Fragment() {
    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AuthViewModel by viewModels { AuthViewModel.Factory() }
    private var isLogoutInitiated = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Clear errors when the user starts typing
        setupErrorClearance()

        binding.registerButton.setOnClickListener {
            val name = binding.nameInput.text.toString()
            val email = binding.emailInput.text.toString()
            val password = binding.passwordInput.text.toString()
            val confirmPassword = binding.confirmPasswordInput.text.toString()

            if (validateInputs(name, email, password, confirmPassword)) {
                binding.progressBar.visibility = View.VISIBLE
                viewModel.registerCustomer(email, password, name)
            }
        }

        binding.loginButton.setOnClickListener {
            findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.authState.observe(viewLifecycleOwner) { state ->
                when (state) {
                    is AuthState.Success -> {
                        if (!isLogoutInitiated) {
                            binding.progressBar.visibility = View.GONE
                            isLogoutInitiated = true
                            Toast.makeText(requireContext(), "Registration successful! Please login with your credentials.", Toast.LENGTH_LONG).show()
                            // Start the logout process
                            viewModel.logout()
                        }
                    }
                    is AuthState.Error -> {
                        binding.progressBar.visibility = View.GONE
                        isLogoutInitiated = false
                        Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                    }
                    is AuthState.Loading -> {
                        binding.progressBar.visibility = View.VISIBLE
                    }
                    is AuthState.LoggedOut -> {
                        binding.progressBar.visibility = View.GONE
                        // Only navigate if we initiated the logout after registration
                        if (isLogoutInitiated) {
                            isLogoutInitiated = false
                            // Use postDelayed to ensure UI updates before navigation
                            view.post {
                                if (isAdded()) { // Check if fragment is still attached
                                    findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun validateInputs(
        name: String,
        email: String,
        password: String,
        confirmPassword: String
    ): Boolean {
        var isValid = true

        if (name.isEmpty()) {
            binding.nameLayout.error = "Name is required"
            isValid = false
        }

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.emailLayout.error = getString(R.string.invalid_email)
            isValid = false
        }

        if (password.isEmpty() || password.length < 6) {
            binding.passwordLayout.error = getString(R.string.invalid_password)
            isValid = false
        }

        if (password != confirmPassword) {
            binding.confirmPasswordLayout.error = getString(R.string.passwords_dont_match)
            isValid = false
        }

        return isValid
    }

    private fun setupErrorClearance() {
        binding.nameInput.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) binding.nameLayout.error = null
        }
        
        binding.emailInput.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) binding.emailLayout.error = null
        }
        
        binding.passwordInput.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) binding.passwordLayout.error = null
        }
        
        binding.confirmPasswordInput.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) binding.confirmPasswordLayout.error = null
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 