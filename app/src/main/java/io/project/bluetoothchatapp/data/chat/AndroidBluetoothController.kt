package io.project.bluetoothchatapp.data.chat

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.content.Context
//import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
//import io.project.bluetoothchatapp.Manifest
import io.project.bluetoothchatapp.data.FoundDeviceReceiver
import io.project.bluetoothchatapp.data.toBluetoothDeviceDomain
import io.project.bluetoothchatapp.domain.chat.BluetoothController
import io.project.bluetoothchatapp.domain.chat.BluetoothDeviceDomain
import kotlinx.coroutines.flow.*
import java.io.IOException
import java.util.*

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

    private var currentServerSocket: BluetoothServerSocket? = null
    private var currentClientSocket: BluetoothSocket? = null

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

    override fun startBluetoothServer(): Flow<ConnectionResult> {
        return flow {
            //it will emit a value and we'll be notified about that later on in our viewModel
            if (!hasPermission(Manifest.permission.BLUETOOTH_CONNECT)){
                throw SecurityException("No BLUETOOTH_CONNECT permission")
            }
            //here we need to pass name of that bluetooth service that accepts connection and a uuid(unique identifier)
            //both the devices should have sane uuid, so we want we using a random uuid, we have created a const hardcoded uuid.
            currentServerSocket = bluetoothAdapter?.listenUsingInsecureRfcommWithServiceRecord("chat_service",
                UUID.fromString(SERVICE_UUID))
            //nxt step is to listen to another connection, now we want to block this background thread ala we r open to accept connection
            var shouldLoop = true
            while (shouldLoop){
                currentClientSocket = try {
                    //accept-> here it is a blocking action, it'll block the thread ala current server socket is active, it'll
                    //be blocked until we call currentServerSocket?.close()
                    //currentServerSocket?.accept()-> will return a Bluetooth socket, and this is now the socket from
                    //the client that connected, as soon as we have a connected client this fun will return and loop will go on
                    currentServerSocket?.accept()
                }
                catch(e: IOException){
                    shouldLoop = false
                    null
                }
                //if that exists
                currentClientSocket?.let {
                    currentServerSocket?.close()
                }

                //server socket is only for accepting connections
                    //this client socket which we haven't closed, will be used to keep those connected instance
                //this active connection will be able to always send data between our client and server
            }
        }
    }
    //we do care about the return value of this func which will be bluetooth server socket, we'll save it in public field
    //for that we'll create currentServerSocket

    override fun connectToDevice(device: BluetoothDeviceDomain): Flow<ConnectionResult> {
        TODO("Not yet implemented")
    }

    override fun closeConnection() {
        TODO("Not yet implemented")
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

    companion object{
        const val SERVICE_UUID = "0000-1111-2222-0000"
    }


}


