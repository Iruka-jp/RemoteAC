package com.example.remoteAC

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.remoteAC.databinding.ConfigItemBinding

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

            val context = binding.root.context
            // Match icon based on config name (lowercase, spaces replaced by underscores)
            val iconName = config.name.lowercase().replace(" ", "_")
            val resourceId = context.resources.getIdentifier(iconName, "drawable", context.packageName)

            val icon = if (resourceId != 0) {
                ContextCompat.getDrawable(context, resourceId)
            } else {
                // Fallback icon
                ContextCompat.getDrawable(context, android.R.drawable.ic_menu_preferences)
            }
            
            binding.configName.setCompoundDrawablesWithIntrinsicBounds(icon, null, null, null)
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
            return oldItem.macAddress == newItem.macAddress
        }

        override fun areContentsTheSame(oldItem: Config, newItem: Config): Boolean {
            return oldItem == newItem
        }
    }
}