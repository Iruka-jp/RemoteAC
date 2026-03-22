package com.example.remote_ac

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.WifiNetworkSpecifier
import android.os.Build
import androidx.core.content.ContextCompat

@Composable
fun WifiPairingScreen(onWifiConnected: (String) -> Unit) {
    val context = LocalContext.current
    val wifiManager = remember { context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager }
    var wifiNetworks by remember { mutableStateOf(emptyList<ScanResult>()) }
    var scanning by remember { mutableStateOf(false) }
    var connecting by remember { mutableStateOf(false) }

    val permissionsToRequest = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_WIFI_STATE,
        Manifest.permission.CHANGE_WIFI_STATE
    )

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.values.all { it }) {
            // Permissions granted
        }
    }

    LaunchedEffect(Unit) {
        val allGranted = permissionsToRequest.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
        if (!allGranted) {
            permissionLauncher.launch(permissionsToRequest)
        }
    }

    fun startScan() {
        val allGranted = permissionsToRequest.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }

        if (!allGranted) {
            permissionLauncher.launch(permissionsToRequest)
            return
        }

        scanning = true
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    try {
                        val results = wifiManager.scanResults
                        wifiNetworks = results.filter { it.SSID?.startsWith("RemoteAC_") == true }
                    } catch (e: SecurityException) {
                        // Handle case where permission was revoked
                    }
                }
                scanning = false
                try {
                    context.unregisterReceiver(this)
                } catch (e: Exception) {
                    // Already unregistered
                }
            }
        }
        context.registerReceiver(receiver, IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION))
        
        try {
            @Suppress("DEPRECATION")
            wifiManager.startScan()
        } catch (e: SecurityException) {
            scanning = false
        }
    }

    fun connectToWifi(scanResult: ScanResult) {
        val ssid = scanResult.SSID ?: return
        val macAddress = ssid.substringAfter("RemoteAC_")
        connecting = true

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val specifier = WifiNetworkSpecifier.Builder()
                .setSsid(ssid)
                .setWpa2Passphrase("remote123")
                .build()

            val request = NetworkRequest.Builder()
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                .removeCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .setNetworkSpecifier(specifier)
                .build()

            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            connectivityManager.requestNetwork(request, object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    connectivityManager.bindProcessToNetwork(network)
                    onWifiConnected(macAddress)
                }

                override fun onUnavailable() {
                    connecting = false
                }
            })
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Button(onClick = { startScan() }, enabled = !scanning) {
            Text(if (scanning) "Scanning..." else "Scan for AC")
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (connecting) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            Text("Connecting...", modifier = Modifier.align(Alignment.CenterHorizontally))
        } else {
            LazyColumn {
                items(wifiNetworks) { network ->
                    ListItem(
                        headlineContent = { Text(network.SSID ?: "Unknown") },
                        modifier = Modifier.clickable { connectToWifi(network) }
                    )
                }
            }
        }
    }
}
