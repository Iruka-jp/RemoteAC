package com.example.myapplication

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.databinding.ConfigItemBinding

class ConfigAdapter(private val onClick: (Config) -> Unit) :
    ListAdapter<Config, ConfigAdapter.ConfigViewHolder>(ConfigDiffCallback) {

    class ConfigViewHolder(private val binding: ConfigItemBinding, val onClick: (Config) -> Unit) :
        RecyclerView.ViewHolder(binding.root) {
        private var currentConfig: Config? = null

        init {
            binding.root.setOnClickListener {
                currentConfig?.let {
                    onClick(it)
                }
            }
        }

        fun bind(config: Config) {
            currentConfig = config
            binding.configName.text = config.name

            val resourceId = config.iconRes
            binding.makerIcon.setBackgroundResource(R.drawable.white_circle_bg)
            if (resourceId != 0) {
                binding.makerIcon.setImageResource(resourceId)
            } else {
                // Fallback to a default icon if the specific one is not found
                binding.makerIcon.setImageResource(android.R.drawable.ic_menu_preferences)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConfigViewHolder {
        val binding = ConfigItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ConfigViewHolder(binding, onClick)
    }

    override fun onBindViewHolder(holder: ConfigViewHolder, position: Int) {
        val config = getItem(position)
        holder.bind(config)
    }

    object ConfigDiffCallback : DiffUtil.ItemCallback<Config>() {
        override fun areItemsTheSame(oldItem: Config, newItem: Config): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: Config, newItem: Config): Boolean {
            return oldItem.name == newItem.name && oldItem.maker == newItem.maker
        }
    }
}