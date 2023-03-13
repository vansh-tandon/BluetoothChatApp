package io.project.bluetoothchatapp.data

import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build

//we created as we need to listen to state changes in the end
//to receive a boolean when we have successfully connected and when the connection was actually interrupted

//Broadcast Receiver
//which will provide call back onDeviceFound
//asa it finds a device, it will fire a broadcast and to retrieve that
//broadcast can use this broadcast receiver
class BluetoothStateReceiver(private var onStateChanged:(isConnected :Boolean, BluetoothDevice)-> Unit):BroadcastReceiver() {
    //code in this block will be executed when some action is performed on device
    //such as turning on and off airplane mode, in this case-> Device found
    override fun onReceive(context: Context?, intent: Intent?) {
        //to retrieve device info
        val device = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent?.getParcelableExtra(
                BluetoothDevice.EXTRA_DEVICE,
                BluetoothDevice::class.java
            )
        } else {
            intent?.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
        }
        //if device exists we called onDeviceFound
//        device?.let(OnStateChanged)


        when (intent?.action) {
            BluetoothDevice.ACTION_ACL_CONNECTED -> {
                onStateChanged(true, device?: return)
            }
            BluetoothDevice.ACTION_ACL_DISCONNECTED ->{
                onStateChanged(true, device?: return)
            }
        }
    }
}
