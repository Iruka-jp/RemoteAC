package com.example.remote_ac

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.remote_ac.databinding.FragmentSecondBinding
import kotlin.text.lowercase

/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class SecondFragment : Fragment() {

    private var _binding: FragmentSecondBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ConfigViewModel by activityViewModels()
    private val args: SecondFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSecondBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.ac_makers,
            android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerMaker.adapter = adapter

        binding.buttonSave.setOnClickListener {
            val name = binding.editTextName.text.toString()
            val maker = binding.spinnerMaker.selectedItem.toString()
            
            if (name.isNotBlank()) {
                viewModel.addConfig(Config(name, maker, args.macAddress))
                findNavController().navigate(R.id.action_SecondFragment_to_FirstFragment)
            } else {
                binding.nameLayout.error = "Name cannot be empty"
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}