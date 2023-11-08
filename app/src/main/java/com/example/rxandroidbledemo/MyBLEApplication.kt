package com.example.rxandroidbledemo

import android.app.Application
import com.polidea.rxandroidble2.LogConstants
import com.polidea.rxandroidble2.LogOptions
import com.polidea.rxandroidble2.RxBleClient

class MyBLEApplication : Application() {
    companion object {
        lateinit var rxBleClient: RxBleClient
            private set
    }

    override fun onCreate() {
        super.onCreate()
        rxBleClient = RxBleClient.create(this)
        RxBleClient.updateLogOptions(LogOptions.Builder()
            .setLogLevel(LogConstants.INFO)
            .setMacAddressLogSetting(LogConstants.MAC_ADDRESS_FULL)
            .setUuidsLogSetting(LogConstants.UUIDS_FULL)
            .setShouldLogAttributeValues(true)
            .build()
        )
    }
}