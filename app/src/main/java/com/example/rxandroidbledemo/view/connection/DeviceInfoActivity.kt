package com.example.rxandroidbledemo.view.connection

import android.annotation.TargetApi
import android.content.Context
import android.content.Intent
import android.view.View
import androidx.core.view.isVisible
import com.example.rxandroidbledemo.MyBLEApplication
import com.example.rxandroidbledemo.R
import com.example.rxandroidbledemo.base.BaseActivity
import com.example.rxandroidbledemo.databinding.ActivityDeviceInfoBinding
import com.example.rxandroidbledemo.utils.isConnected
import com.example.rxandroidbledemo.utils.isConnectionPermissionGranted
import com.example.rxandroidbledemo.utils.requestConnectionPermission
import com.example.rxandroidbledemo.utils.showSnackbarShort
import com.polidea.rxandroidble2.RxBleConnection
import com.polidea.rxandroidble2.RxBleDevice
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

private const val EXTRA_MAC_ADDRESS = "extra_mac_address"
private const val DEVICE_NAME = "device_name"
private const val DEVICE_RSSI = "rssi"

class DeviceInfoActivity : BaseActivity<ActivityDeviceInfoBinding>(R.layout.activity_device_info) {

    companion object {
        fun newInstance(
            context: Context,
            macAddress: String,
            deviceName: String,
            rssi: String
        ): Intent =
            Intent(context, DeviceInfoActivity::class.java).apply {
                putExtra(
                    EXTRA_MAC_ADDRESS,
                    macAddress
                )
                putExtra(
                    DEVICE_NAME,
                    deviceName
                )
                putExtra(
                    DEVICE_RSSI,
                    rssi
                )
            }
    }

    private lateinit var macAddress: String
    private lateinit var deviceName: String
    private lateinit var rssi: String

    private lateinit var bleDevice: RxBleDevice

    private var connectionDisposable: Disposable? = null

    private var stateDisposable: Disposable? = null

    private val mtuDisposable = CompositeDisposable()

    private var hasClickedConnect = false

    override fun init() {
        macAddress = intent.getStringExtra(EXTRA_MAC_ADDRESS)!!
        deviceName = intent.getStringExtra(DEVICE_NAME)!!
        rssi = intent.getStringExtra(DEVICE_RSSI)!!

        bleDevice = MyBLEApplication.rxBleClient.getBleDevice(macAddress)
        binding.connectToggle.setOnClickListener { connectDevice() }


        binding.deviceName = deviceName
        binding.deviceRssi = ("RSSI: $rssi")

        binding.btnDisconnect.setOnClickListener {
            triggerDisconnect()
            finish()
        }


        // How to listen for connection state changes
        // Note: it is meant for UI updates only — one should not observeConnectionStateChanges() with BLE connection logic
        bleDevice.observeConnectionStateChanges()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { onConnectionStateChange(it) }
            .let { stateDisposable = it }


        android.os.Handler().postDelayed({
            connectDevice()
        }, 1000)
    }

    private fun connectDevice() {
        if (bleDevice.isConnected) {
            triggerDisconnect()
        } else {
            if (MyBLEApplication.rxBleClient.isConnectRuntimePermissionGranted) {
                connect()
            } else {
                hasClickedConnect = true
                requestConnectionPermission(MyBLEApplication.rxBleClient)
            }
        }
    }

    private fun connect() {
        bleDevice.establishConnection(binding.autoconnect.isChecked)
            .observeOn(AndroidSchedulers.mainThread())
            .doFinally { dispose() }
            .subscribe({ onConnectionReceived() }, { onConnectionFailure(it) })
            .let { connectionDisposable = it }
    }

    @TargetApi(21 /* Build.VERSION_CODES.LOLLIPOP */)
    private fun onSetMtu() {
        binding.newMtu.text.toString().toIntOrNull()?.let { mtu ->
            bleDevice.establishConnection(false)
                .flatMapSingle { rxBleConnection -> rxBleConnection.requestMtu(mtu) }
                .take(1) // Disconnect automatically after discovery
                .observeOn(AndroidSchedulers.mainThread())
                .doFinally { updateUI() }
                .subscribe({ onMtuReceived(it) }, { onConnectionFailure(it) })
                .let { mtuDisposable.add(it) }
        }
    }

    private fun onConnectionFailure(throwable: Throwable) {
        binding.bgConnected.visibility = View.INVISIBLE
        binding.progressWheel.visibility = View.INVISIBLE
        binding.tvStatus.text = getString(R.string.failed_to_connect)
        showSnackbarShort("Connection error: $throwable")
    }

    private fun onConnectionReceived() {
        binding.bgConnected.visibility = View.VISIBLE
        binding.progressWheel.visibility = View.INVISIBLE
        binding.tvStatus.text = ""
        binding.btnDisconnect.visibility = View.VISIBLE
    }

    private fun onConnectionStateChange(newState: RxBleConnection.RxBleConnectionState) {
        binding.connectionState.text = newState.toString()
        updateUI()
    }

    private fun onMtuReceived(mtu: Int) = showSnackbarShort("MTU received: $mtu")

    private fun dispose() {
        connectionDisposable = null
        updateUI()
    }

    private fun triggerDisconnect() = connectionDisposable?.dispose()

    private fun updateUI() {
        binding.connectToggle.setText(if (bleDevice.isConnected) R.string.button_disconnect else R.string.button_connect)
        binding.autoconnect.isEnabled = !bleDevice.isConnected
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (isConnectionPermissionGranted(requestCode, grantResults) && hasClickedConnect) {
            hasClickedConnect = false
            connect()
        }
    }

    override fun onPause() {
        super.onPause()
        triggerDisconnect()
        mtuDisposable.clear()
    }

    override fun onDestroy() {
        super.onDestroy()
        stateDisposable?.dispose()
    }
}