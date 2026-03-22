package com.example.remote_ac

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import android.net.wifi.WifiNetworkSpecifier
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.remote_ac.databinding.FragmentWifiPairingBinding

class WifiPairingFragment : Fragment() {

    private var _binding: FragmentWifiPairingBinding? = null
    private val binding get() = _binding!!

    private lateinit var wifiManager: WifiManager
    private val wifiNetworks = mutableListOf<ScanResult>()
    private lateinit var adapter: WifiAdapter

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.all { it.value }) {
            startWifiScan()
        } else {
            Toast.makeText(context, "Permissions required to scan WiFi. Closing app.", Toast.LENGTH_SHORT).show()
            requireActivity().finish()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWifiPairingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        wifiManager = requireContext().applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

        adapter = WifiAdapter(wifiNetworks) { scanResult ->
            connectToWifi(scanResult)
        }
        binding.wifiList.layoutManager = LinearLayoutManager(context)
        binding.wifiList.adapter = adapter

        checkPermissionsAndScan()
    }

    private fun checkPermissionsAndScan() {
        val permissions = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.CHANGE_WIFI_STATE
        )
        if (permissions.all { ContextCompat.checkSelfPermission(requireContext(), it) == PackageManager.PERMISSION_GRANTED }) {
            startWifiScan()
        } else {
            requestPermissionLauncher.launch(permissions)
        }
    }

    private fun startWifiScan() {
        binding.progressBar.visibility = View.VISIBLE
        val wifiScanReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val success = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false)
                if (success) {
                    scanSuccess()
                }
                requireContext().unregisterReceiver(this)
                binding.progressBar.visibility = View.GONE
            }
        }

        val intentFilter = IntentFilter()
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
        requireContext().registerReceiver(wifiScanReceiver, intentFilter)

        val success = wifiManager.startScan()
        if (!success) {
            scanSuccess()
        }
    }

    private fun scanSuccess() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            checkPermissionsAndScan()
            return
        }
        val results = wifiManager.scanResults
        wifiNetworks.clear()
        wifiNetworks.addAll(results.filter { it.SSID.startsWith("RemoteAC_") })
        adapter.notifyDataSetChanged()
    }

    private fun connectToWifi(scanResult: ScanResult) {
        val ssid = scanResult.SSID
        val macAddress = ssid.substringAfter("RemoteAC_")
        
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

            val connectivityManager = requireContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            
            binding.progressBar.visibility = View.VISIBLE
            connectivityManager.requestNetwork(request, object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    super.onAvailable(network)
                    connectivityManager.bindProcessToNetwork(network)
                    activity?.runOnUiThread {
                        binding.progressBar.visibility = View.GONE
                        val action = WifiPairingFragmentDirections.actionWifiPairingFragmentToIotWebViewFragment(macAddress)
                        findNavController().navigate(action)
                    }
                }

                override fun onUnavailable() {
                    super.onUnavailable()
                    activity?.runOnUiThread {
                        binding.progressBar.visibility = View.GONE
                        Toast.makeText(context, "Connection failed", Toast.LENGTH_SHORT).show()
                    }
                }
            })
        } else {
            // Older versions handled differently, but for this exercise we focus on modern Android
            Toast.makeText(context, "OS version not supported for auto-connect in this demo", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private class WifiAdapter(private val networks: List<ScanResult>, private val onClick: (ScanResult) -> Unit) :
        RecyclerView.Adapter<WifiAdapter.ViewHolder>() {

        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val textView: TextView = view.findViewById(android.R.id.text1)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(android.R.layout.simple_list_item_1, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val network = networks[position]
            holder.textView.text = network.SSID
            holder.itemView.setOnClickListener { onClick(network) }
        }

        override fun getItemCount() = networks.size
    }
}