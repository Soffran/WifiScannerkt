@file:Suppress("DEPRECATION")

package com.example.wifiscannerkt

import android.content.Context
import android.content.Intent
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.documentfile.provider.DocumentFile
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.gson.Gson
import kotlinx.coroutines.*
import java.io.IOException

class MainActivity : AppCompatActivity() {
    private lateinit var wifiAdapter: WifiAdapter
    private lateinit var recyclerView: RecyclerView
    private var json: String? = null
    private var passwords: Array<String> = arrayOf()
    private val REQUEST_CODE_SAVE = 77
    private var wifiList:MutableList<ScanResults> = arrayListOf()


    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        recyclerView = findViewById(R.id.wifiRecycler)
        recyclerView.layoutManager = LinearLayoutManager(this)
        wifiAdapter = WifiAdapter(arrayListOf())
        passwords = resources.getStringArray(R.array.wifi_passwords)
        val btnScan = findViewById<FloatingActionButton>(R.id.scanBtn)
        val wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager



        btnScan.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                wifiList = WifiReceiver(wifiManager).scanWifi(this@MainActivity,wifiManager)
                withContext(Dispatchers.Main){
                    if (wifiList.isEmpty()){
                    Toast.makeText(this@MainActivity,"No Wi-Fi networks found",Toast.LENGTH_SHORT).show()
                } else{
                    wifiAdapter = WifiAdapter(wifiList)
                    recyclerView.adapter = wifiAdapter
                }}
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {

        menuInflater.inflate(R.menu.wifi_menu,menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.save ->{
                saveWifiListToExternalStorage()
            }
            R.id.export ->{
                exportWifiList()
            }
            R.id.unlock ->{
                CoroutineScope(Dispatchers.IO).launch{
                    unlockWifiList(wifiList, passwords,applicationContext)
            }

            }

        }
        return super.onOptionsItemSelected(item)
    }
    private fun saveWifiListToExternalStorage() {
        val gson = Gson()
        json = gson.toJson(wifiList)

        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
        startActivityForResult(intent, REQUEST_CODE_SAVE)

    }
    private fun exportWifiList() {
        val gson = Gson()
        json = gson.toJson(wifiList)

        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "text/plain"
        intent.putExtra(Intent.EXTRA_TEXT, json)
        startActivity(Intent.createChooser(intent, "Share Wi-Fi networks"))
    }

    private fun connectToWifiWithKey(ssid: String, key: String): Boolean {
        val wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val wifiConfig = WifiConfiguration()
        wifiConfig.SSID = String.format("\"%s\"", ssid)
        wifiConfig.preSharedKey = String.format("\"%s\"", key)
        val networkId = wifiManager.addNetwork(wifiConfig)
        return if (networkId != -1) {
            wifiManager.enableNetwork(networkId, true)
            wifiManager.reconnect()
            true
        } else {
            false
        }
    }

    private fun unlockWifiList( wifiList: MutableList<ScanResults>, passwords: Array<String>, context: Context ) {

        val scope = CoroutineScope(Dispatchers.IO)
        val wifiJobs = wifiList.flatMap { wifi ->
            passwords.map { password ->
                scope.async {
                    try {
                        val connected = connectToWifiWithKey( wifi.Ssid, password)
                        if (connected) {
                            withContext(Dispatchers.Main) {
                                Toast.makeText(
                                    context,
                                    "Подключено к сети: ${wifi.Ssid}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                context,
                                "Не удалось подключиться к сети: ${wifi.Ssid}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }
        }

    }






    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        @Suppress("DEPRECATION")
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE_SAVE && resultCode == RESULT_OK) {
            val uri = data?.data

            if (uri != null) {
                val document = DocumentFile.fromTreeUri(applicationContext, uri)
                val file = document?.createFile("application/json", "wifi_networks.json")

                if (file != null) {
                    try {
                        val outputStream = contentResolver.openOutputStream(file.uri)
                        outputStream?.write(json?.toByteArray())
                        outputStream?.close()

                        Toast.makeText(this, "Wi-Fi networks saved to ${file.uri}", Toast.LENGTH_SHORT).show()
                    } catch (e: IOException) {
                        e.printStackTrace()
                        Toast.makeText(this, "Error saving Wi-Fi networks", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Error creating file", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Invalid uri", Toast.LENGTH_SHORT).show()
            }
        }
    }

}