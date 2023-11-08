package com.example.rxandroidbledemo.utils

import android.bluetooth.BluetoothGattCharacteristic

fun BluetoothGattCharacteristic.hasProperty(property: Int): Boolean = (properties and property) > 0