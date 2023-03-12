package io.project.bluetoothchatapp.data

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import io.project.bluetoothchatapp.domain.chat.BluetoothDeviceDomain

@SuppressLint("MissingPermission")
fun BluetoothDevice.toBluetoothDeviceDomain():BluetoothDeviceDomain {
    return BluetoothDeviceDomain(
        name = name,
        address = address
    )
}