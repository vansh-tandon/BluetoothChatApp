package io.project.bluetoothchatapp.data.chat

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import io.project.bluetoothchatapp.Manifest
import io.project.bluetoothchatapp.data.FoundDeviceReceiver
import io.project.bluetoothchatapp.data.toBluetoothDeviceDomain
import io.project.bluetoothchatapp.domain.chat.BluetoothController
import io.project.bluetoothchatapp.domain.chat.BluetoothDeviceDomain
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

@SuppressLint("MissingPermission")
class AndroidBluetoothController(private val context: Context): BluetoothController {

    private val bluetoothManager by lazy {
        context.getSystemService(BluetoothManager::class.java)
    }
    private val bluetoothAdapter by lazy {
        //this adapter could be null, if your device doesn't support bluetooth so we used ?
        bluetoothManager?.adapter

    }

    //we do this just to expose the immutable version
    //since another class should not be directly able to modify our stateflow in this class
    //this way we just have single source of changes
    private val _scannedDevices= MutableStateFlow<List<BluetoothDeviceDomain>>(emptyList())
    override val scannedDevices: StateFlow<List<BluetoothDeviceDomain>>
        get() = _scannedDevices.asStateFlow()
    private val _pairedDevices= MutableStateFlow<List<BluetoothDeviceDomain>>(emptyList())
    override val pairedDevices: StateFlow<List<BluetoothDeviceDomain>>
        get() = _pairedDevices.asStateFlow()


    //creating a receiver for broadcast receiver
    private val foundDeviceReceiver= FoundDeviceReceiver{device->
        //here we'll get the call back with the device that was found
        //devices->existing devices
        //and we only want to add new devices to our list,
        _scannedDevices.update {devices->
            val newDevice = device.toBluetoothDeviceDomain()
            if(newDevice in devices) devices else devices+newDevice
        }
    }
    //to get the result we'll have to register the receiver first and we'll do that in start
    //discovery func
    init {
        updatePairedDevices()
    }

    override fun startDiscovery() {
        if(!hasPermission(android.Manifest.permission.BLUETOOTH_SCAN)) return

        //registering the receiver
        context.registerReceiver(
            foundDeviceReceiver,
            //intent filter for specifying the actions for which we want to register this receiver
            IntentFilter(BluetoothDevice.ACTION_FOUND)
        )

        updatePairedDevices()
        bluetoothAdapter?.startDiscovery()
        //to get the callback that the device is found with all the device information
        //for that we need broadcast receiver

    }

    override fun stopDiscovery() {
        if(!hasPermission(android.Manifest.permission.BLUETOOTH_SCAN)) return
        bluetoothAdapter?.cancelDiscovery()
    }

    override fun release() {
        //to clear our bluetooth controller
        context.unregisterReceiver(foundDeviceReceiver)
    }

    //to get the list of paired devices
    @SuppressLint("MissingPermission")
    private fun updatePairedDevices(){
        if(!hasPermission(android.Manifest.permission.BLUETOOTH_CONNECT)) return

        bluetoothAdapter
                //bounded devices-> set of bluetooth devices not from android
            ?.bondedDevices
                //map-> to map it to our own bt devices,
            // for that we use mapper(BluetoothDeviceMapper)
            ?.map {it.toBluetoothDeviceDomain()}
            ?.also {devices->
                _pairedDevices.update { devices }

            }

            }
    private fun hasPermission(permission: String): Boolean {
        return context.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED
    }


}


