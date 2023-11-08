package com.example.rxandroidbledemo.view.scanning

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.rxandroidbledemo.R
import com.example.rxandroidbledemo.databinding.ScannedDeviceAdapterBinding
import com.polidea.rxandroidble2.scan.ScanResult

internal class ScanResultsAdapter(
    private val onClickListener: (ScanResult) -> Unit
) : RecyclerView.Adapter<ScanResultsAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ScannedDeviceAdapterBinding) :
        RecyclerView.ViewHolder(binding.root) {

    }

    private val data = mutableListOf<ScanResult>()

    fun addScanResult(bleScanResult: ScanResult) {
        // Not the best way to ensure distinct devices, just for the sake of the demo.
        data.withIndex()
            .firstOrNull { it.value.bleDevice == bleScanResult.bleDevice }
            ?.let {
                // device already in data list => update
                data[it.index] = bleScanResult
                notifyItemChanged(it.index)
            }
            ?: run {
                // new device => add to data list
                with(data) {
                    add(bleScanResult)
                    sortBy { it.bleDevice.macAddress }
                }
                notifyDataSetChanged()
            }
    }

    fun clearScanResults() {
        data.clear()
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = data.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(data[position]) {
            holder.binding.device.text = String.format(
                "%s",
                if (scanRecord.deviceName != null) {
                    scanRecord.deviceName
                } else {
                    bleDevice.macAddress
                },
            )
            holder.binding.rssi.text = String.format("RSSI: %d", rssi)
            holder.binding.root.setOnClickListener { onClickListener(this) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.scanned_device_adapter,
                parent,
                false
            )
        )
    }


}
