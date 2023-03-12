package io.project.bluetoothchatapp.domain.chat

//to avoid any name complications
typealias BluetoothDeviceDomain = BluetoothDevice

data class BluetoothDevice(
    val name: String?,
    val address: String
)