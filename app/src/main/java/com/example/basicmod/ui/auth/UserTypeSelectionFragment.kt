package com.example.basicmod.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.basicmod.R
import com.example.basicmod.databinding.FragmentUserTypeSelectionBinding

class UserTypeSelectionFragment : Fragment() {
    private var _binding: FragmentUserTypeSelectionBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUserTypeSelectionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buyerCard.setOnClickListener {
            findNavController().navigate(R.id.action_userTypeSelectionFragment_to_loginFragment)
        }

        binding.ownerCard.setOnClickListener {
            findNavController().navigate(R.id.action_userTypeSelectionFragment_to_ownerLoginFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 