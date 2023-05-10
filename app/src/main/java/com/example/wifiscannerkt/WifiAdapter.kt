package com.example.wifiscannerkt

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class WifiAdapter(private val wifiList: MutableList<ScanResults>) :
    RecyclerView.Adapter<WifiAdapter.WifiViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WifiViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.wifi_item, parent, false)
        return WifiViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: WifiViewHolder, position: Int) {
        val currentItem = wifiList[position]
        holder.ssidTextView.text = currentItem.Ssid
        holder.bssidTextView.text = currentItem.Bssid
        holder.frequencyTextView.text = currentItem.Frequency.toString()
        holder.levelTextView.text = currentItem.Level.toString()
        holder.isOpenTextView.text = if (currentItem.isOpen)   "Secured" else "Open"
    }

    override fun getItemCount() = wifiList.size

    class WifiViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ssidTextView: TextView = itemView.findViewById(R.id.ssid_text_view)
        val bssidTextView: TextView = itemView.findViewById(R.id.bssid_text_view)
        val frequencyTextView: TextView = itemView.findViewById(R.id.frequency_text_view)
        val levelTextView: TextView = itemView.findViewById(R.id.level_text_view)
        val isOpenTextView: TextView = itemView.findViewById(R.id.open_text_view)
    }
}