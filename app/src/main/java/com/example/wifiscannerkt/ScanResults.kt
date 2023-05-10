package com.example.wifiscannerkt

data class ScanResults (
    var Ssid:String,
    var Bssid:String,
    var Frequency: Int,
    var Level: Int,
    var isOpen: Boolean,
    var isConnected: Boolean,
    var isSaved:Boolean
        )