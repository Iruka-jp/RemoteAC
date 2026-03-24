package com.example.remote_ac

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun FirstScreen(viewModel: ConfigViewModel) {
    val configs by viewModel.configs.observeAsState(emptyList())

    if (configs.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("No configurations found. Add one!")
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(configs) { config ->
                ConfigItem(config = config)
            }
        }
    }
}

@Composable
fun ConfigItem(config: Config) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* Handle click */ },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(text = config.name, style = MaterialTheme.typography.titleLarge)
            Text(text = "Maker: ${config.maker}", style = MaterialTheme.typography.bodyMedium)
            Text(text = "MAC: ${config.macAddress}", style = MaterialTheme.typography.bodySmall)
        }
    }
}
