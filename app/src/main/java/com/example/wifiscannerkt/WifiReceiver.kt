package com.example.wifiscannerkt


import android.app.Activity
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
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiManager
import android.net.wifi.WifiNetworkSuggestion
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat

class WifiReceiver(private var wifiManager: WifiManager):BroadcastReceiver() {
    private val REQUEST_CODE_PERMISSIONS= 100



    override fun onReceive(context: Context?, intent: Intent?) {


        }
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun scanWifi(context: Context, wifiManager: WifiManager): MutableList<ScanResults> {
        val wifiReceiver = WifiReceiver(wifiManager)
        val intentFilter = IntentFilter()
        var scanResults:List<ScanResult> = ArrayList()
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
        context.registerReceiver(wifiReceiver, intentFilter)
        val wifiList: MutableList<ScanResults> = mutableListOf()
        if (context.checkSelfPermission(android.Manifest.permission.ACCESS_WIFI_STATE) ==PackageManager.PERMISSION_GRANTED&&
            context.checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_GRANTED){
            scanResults = wifiManager.scanResults
        } else{
            ActivityCompat.requestPermissions(context as AppCompatActivity, arrayOf(android.Manifest.permission.ACCESS_WIFI_STATE,android.Manifest.permission.ACCESS_FINE_LOCATION),REQUEST_CODE_PERMISSIONS)
        }
        for (result in scanResults) {
            val isOpen = result.capabilities.contains("WEP") || result.capabilities.contains("PSK") || result.capabilities.contains("EAP")
            wifiList.add(
                ScanResults(
                    result.SSID,
                    result.BSSID,
                    result.frequency,
                    result.level,
                    isOpen,
                    false,
                    false
                )
            )
        }
        context.unregisterReceiver(wifiReceiver)
        return wifiList
    }
    @RequiresApi(Build.VERSION_CODES.Q)
    fun connectToWifi(networkSSID: String, networkPasskey: String, context: Context) {
        val wifiConfig = WifiNetworkSuggestion.Builder()
            .setSsid(networkSSID)
            .setWpa2Passphrase(networkPasskey)
            .build()

        val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val suggestionsList = listOf(wifiConfig)
        val status = wifiManager.addNetworkSuggestions(suggestionsList)

        if (status != WifiManager.STATUS_NETWORK_SUGGESTIONS_SUCCESS) {
            // Обработка ошибки
            return
        }

        val networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                // Подключение выполнено успешно
            }

            override fun onUnavailable() {
                super.onUnavailable()
                // Обработка ошибки
            }
        }

        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val requestBuilder = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .removeCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)

        connectivityManager.requestNetwork(requestBuilder.build(), networkCallback)
    }

}