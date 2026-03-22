package com.example.remote_ac

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.remote_ac.databinding.FragmentIotWebViewBinding

class IotWebViewFragment : Fragment() {

    private var _binding: FragmentIotWebViewBinding? = null
    private val binding get() = _binding!!
    private val args: IotWebViewFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentIotWebViewBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.iotWebView.settings.javaScriptEnabled = true
        binding.iotWebView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                binding.webProgressBar.visibility = View.GONE
            }

            override fun onReceivedError(
                view: WebView?,
                request: WebResourceRequest?,
                error: WebResourceError?
            ) {
                super.onReceivedError(view, request, error)
                // If the connection is lost, it likely means the IoT device 
                // switched from AP mode to Station mode (connecting to home WiFi).
                // In a real scenario, we might want a more robust check.
                if (request?.url?.toString()?.contains("192.168.4.1") == true) {
                    navigateToFinalConfig()
                }
            }
        }

        // Standard IP for IoT device Access Point configuration page
        binding.iotWebView.loadUrl("http://192.168.4.1")

        // Also add a manual "Next" button in case the auto-detection fails
        // Or handle it via a specific URL redirect from the IoT device
    }

    private fun navigateToFinalConfig() {
        val action = IotWebViewFragmentDirections.actionIotWebViewFragmentToSecondFragment(args.macAddress)
        findNavController().navigate(action)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}