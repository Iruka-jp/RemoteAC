package com.example.remote_ac

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecondScreen(
    macAddress: String,
    viewModel: ConfigViewModel,
    onConfigSaved: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    val context = LocalContext.current
    val makers = context.resources.getStringArray(R.array.ac_makers)
    var expanded by remember { mutableStateOf(false) }
    var selectedMaker by remember { mutableStateOf(makers[0]) }
    var nameError by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        OutlinedTextField(
            value = name,
            onValueChange = { 
                name = it
                nameError = null
            },
            label = { Text("Name") },
            modifier = Modifier.fillMaxWidth(),
            isError = nameError != null,
            supportingText = { nameError?.let { Text(it) } }
        )

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = selectedMaker,
                onValueChange = {},
                readOnly = true,
                label = { Text("Maker") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier.menuAnchor(),
                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                makers.forEach { maker ->
                    DropdownMenuItem(
                        text = { Text(maker) },
                        onClick = {
                            selectedMaker = maker
                            expanded = false
                        }
                    )
                }
            }
        }

        Text(text = "MAC Address: $macAddress", style = MaterialTheme.typography.bodyMedium)

        Button(
            onClick = {
                if (name.isNotBlank()) {
                    viewModel.addConfig(Config(name, selectedMaker, macAddress))
                    onConfigSaved()
                } else {
                    nameError = "Name cannot be empty"
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save Configuration")
        }
    }
}
