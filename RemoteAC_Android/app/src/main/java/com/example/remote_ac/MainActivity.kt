package com.example.remote_ac

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.remote_ac.ui.theme.RemoteACTheme

class MainActivity : ComponentActivity() {
    private val viewModel: ConfigViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RemoteACTheme {
                RemoteACApp(viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemoteACApp(viewModel: ConfigViewModel) {
    val navController = rememberNavController()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Remote AC") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate("wifi_pairing") }) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "first",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("first") {
                FirstScreen(viewModel = viewModel)
            }
            composable("wifi_pairing") {
                WifiPairingScreen(
                    onWifiConnected = { macAddress ->
                        navController.navigate("second/$macAddress")
                    }
                )
            }
            composable(
                route = "second/{macAddress}",
                arguments = listOf(navArgument("macAddress") { defaultValue = "" })
            ) { backStackEntry ->
                val macAddress = backStackEntry.arguments?.getString("macAddress") ?: ""
                SecondScreen(
                    macAddress = macAddress,
                    viewModel = viewModel,
                    onConfigSaved = {
                        navController.popBackStack("first", inclusive = false)
                    }
                )
            }
        }
    }
}
