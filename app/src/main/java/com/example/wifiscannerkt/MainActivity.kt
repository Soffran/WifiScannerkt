package com.example.wifiscannerkt

import android.content.Context
import android.content.Intent
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException

class MainActivity : AppCompatActivity() {
    private lateinit var wifiAdapter: WifiAdapter
    private lateinit var recyclerView: RecyclerView
    private var json: String? = null
    private val REQUEST_CODE_SAVE = 77
    private var wifiList:MutableList<ScanResults> = arrayListOf()


    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        recyclerView = findViewById(R.id.wifiRecycler)
        recyclerView.layoutManager = LinearLayoutManager(this)
        wifiAdapter = WifiAdapter(arrayListOf())
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
                Toast.makeText(this, "В разработке", Toast.LENGTH_SHORT).show()
            }

        }
        return super.onOptionsItemSelected(item)
    }
    private fun saveWifiListToExternalStorage() {
        val gson = Gson()
        json = gson.toJson(wifiList)

        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
        @Suppress("DEPRECATION")
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