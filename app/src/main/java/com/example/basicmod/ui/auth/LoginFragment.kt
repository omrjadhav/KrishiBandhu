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
import com.example.basicmod.databinding.FragmentLoginBinding
import kotlinx.coroutines.launch

class LoginFragment : Fragment() {
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AuthViewModel by viewModels { AuthViewModel.Factory() }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Clear errors when the user starts typing
        setupErrorClearance()

        binding.loginButton.setOnClickListener {
            val email = binding.emailInput.text.toString()
            val password = binding.passwordInput.text.toString()

            if (validateInputs(email, password)) {
                binding.progressBar.visibility = View.VISIBLE
                viewModel.login(email, password)
            }
        }

        binding.registerButton.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.authState.observe(viewLifecycleOwner) { state ->
                when (state) {
                    is AuthState.Success -> {
                        binding.progressBar.visibility = View.GONE
                        findNavController().navigate(R.id.action_loginFragment_to_navigation_shop)
                    }
                    is AuthState.Error -> {
                        binding.progressBar.visibility = View.GONE
                        // Clear password field on error for security
                        binding.passwordInput.text?.clear()
                        // Show more detailed error message
                        val errorMessage = when {
                            state.message.contains("credentials", ignoreCase = true) -> "Invalid email or password. Please try again."
                            state.message.contains("network", ignoreCase = true) -> "Network error. Please check your connection."
                            else -> state.message
                        }
                        Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show()
                    }
                    is AuthState.Loading -> {
                        binding.progressBar.visibility = View.VISIBLE
                    }
                    is AuthState.LoggedOut -> {
                        binding.progressBar.visibility = View.GONE
                    }
                }
            }
        }
    }

    private fun validateInputs(email: String, password: String): Boolean {
        var isValid = true

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.emailLayout.error = getString(R.string.invalid_email)
            isValid = false
        }

        if (password.isEmpty() || password.length < 6) {
            binding.passwordLayout.error = getString(R.string.invalid_password)
            isValid = false
        }

        return isValid
    }

    private fun setupErrorClearance() {
        binding.emailInput.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) binding.emailLayout.error = null
        }
        
        binding.passwordInput.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) binding.passwordLayout.error = null
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 