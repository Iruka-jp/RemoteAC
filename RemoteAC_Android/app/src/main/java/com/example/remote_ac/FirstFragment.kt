package com.example.remoteAC

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.remoteAC.databinding.FragmentFirstBinding

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FirstFragment : Fragment() {

    private var _binding: FragmentFirstBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ConfigViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = ConfigAdapter { config ->
            Toast.makeText(context, "Clicked: ${config.name}", Toast.LENGTH_SHORT).show()
        }
        
        binding.configList.adapter = adapter
        binding.configList.layoutManager = LinearLayoutManager(context)

        viewModel.configs.observe(viewLifecycleOwner) { configs ->
            if (configs.isEmpty()) {
                binding.configList.visibility = View.GONE
                binding.emptyView.visibility = View.VISIBLE
            } else {
                binding.configList.visibility = View.VISIBLE
                binding.emptyView.visibility = View.GONE
                adapter.submitList(configs)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}