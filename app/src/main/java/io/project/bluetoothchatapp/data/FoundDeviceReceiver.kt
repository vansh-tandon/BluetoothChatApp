package io.project.bluetoothchatapp.data

import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build

//Broadcast Receiver
//which will provide call back onDeviceFound
//asa it finds a device, it will fire a broadcast and to retrieve that
//broadcast can use this broadcast receiver
class FoundDeviceReceiver(private var onDeviceFound:(BluetoothDevice)-> Unit):BroadcastReceiver() {
    //code in this block will be executed when some action is performed on device
    //such as turning on and off airplane mode, in this case-> Device found
    override fun onReceive(context: Context?, intent: Intent?) {
        when(intent?.action){
            BluetoothDevice.ACTION_FOUND ->{
                //to retrieve device info
                val device = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    intent.getParcelableExtra(
                        BluetoothDevice.EXTRA_NAME,
                        BluetoothDevice::class.java
                    )
                } else {
                    intent.getParcelableExtra(BluetoothDevice.EXTRA_NAME)
                }
                //if device exists we called onDeviceFound
                device?.let(onDeviceFound)
            }
        }
    }

}