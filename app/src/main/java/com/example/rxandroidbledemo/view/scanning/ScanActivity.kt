package com.example.rxandroidbledemo.view.scanning

import android.util.Log
import com.example.rxandroidbledemo.MyBLEApplication
import com.example.rxandroidbledemo.R
import com.example.rxandroidbledemo.databinding.ActivityScanBinding
import com.example.rxandroidbledemo.base.BaseActivity
import com.example.rxandroidbledemo.utils.ItemOffsetDecoration
import com.example.rxandroidbledemo.utils.VERTICAL
import com.example.rxandroidbledemo.utils.isScanPermissionGranted
import com.example.rxandroidbledemo.utils.requestScanPermission
import com.example.rxandroidbledemo.utils.showError
import com.example.rxandroidbledemo.view.connection.DeviceInfoActivity
import com.polidea.rxandroidble2.exceptions.BleScanException
import com.polidea.rxandroidble2.scan.ScanFilter
import com.polidea.rxandroidble2.scan.ScanSettings
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable

class ScanActivity : BaseActivity<ActivityScanBinding>(R.layout.activity_scan) {

    private val rxBleClient = MyBLEApplication.rxBleClient

    private var scanDisposable: Disposable? = null

    private var hasClickedScan = false

    private val resultsAdapter =
        ScanResultsAdapter {
            startActivity(
                DeviceInfoActivity.newInstance(
                    this,
                    it.bleDevice.macAddress,
                    it.scanRecord.deviceName ?: it.bleDevice.macAddress,
                    it.rssi.toString(),
                )
            )
        }

    private val isScanning: Boolean
        get() = scanDisposable != null

    override fun init() {
        configureResultList()
        onScanToggleClick()
    }

    private fun configureResultList() {
        with(binding.scanResults) {
            setHasFixedSize(true)
            itemAnimator = null
            addItemDecoration(
                ItemOffsetDecoration(
                    resources.getDimensionPixelSize(R.dimen.gutter_margin),
                    VERTICAL
                )
            )
            adapter = resultsAdapter
        }
    }

    private fun onScanToggleClick() {
        if (isScanning) {
            scanDisposable?.dispose()
        } else {
            if (rxBleClient.isScanRuntimePermissionGranted) {
                scanBleDevices()
            } else {
                hasClickedScan = true
                requestScanPermission(rxBleClient)
            }
        }
//        updateButtonUIState()
    }

    private fun scanBleDevices() {
        val scanSettings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
            .build()

        val scanFilter = ScanFilter.Builder()
//            .setDeviceAddress("B4:99:4C:34:DC:8B")
            // add custom filters if needed
            .build()

        rxBleClient.scanBleDevices(scanSettings, scanFilter)
            .observeOn(AndroidSchedulers.mainThread())
            .doFinally { dispose() }
            .subscribe({ resultsAdapter.addScanResult(it) }, { onScanFailure(it) })
            .let { scanDisposable = it }
    }

    private fun dispose() {
        scanDisposable = null
//        resultsAdapter.clearScanResults()
//        updateButtonUIState()
    }

    private fun onScanFailure(throwable: Throwable) {
        if (throwable is BleScanException) showError(throwable)
        else Log.w("ScanActivity", "Scan failed", throwable)
    }

    /*private fun updateButtonUIState() =
        scan_toggle_btn.setText(if (isScanning) R.string.button_stop_scan else R.string.button_start_scan)*/

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (isScanPermissionGranted(requestCode, grantResults) && hasClickedScan) {
            hasClickedScan = false
            scanBleDevices()
        }
    }

    public override fun onPause() {
        super.onPause()
        // Stop scanning in onPause callback.
        if (isScanning) scanDisposable?.dispose()
    }

}