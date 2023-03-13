package io.project.bluetoothchatapp.domain.chat

import io.project.bluetoothchatapp.data.chat.ConnectionResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

interface BluetoothController {

    val isConnected: StateFlow<Boolean>
    val scannedDevices: StateFlow<List<BluetoothDevice>>
    val pairedDevices: StateFlow<List<BluetoothDevice>>
    val errors: SharedFlow<String>

    fun startDiscovery()
    fun stopDiscovery()

    //flow is a reactive data structure
    //if we start a server, this will be a blocking action, as long as(ala) we wait for a device to connect, and after connection
    //we want to keep on listening to these events, because a.l.a we have the connection, a msg could come,
    //so there should be some of background thread that keeps on checking and notifies our ui when there is a new msg
    //or when device is disconnected, for that we'll create a flow
    //these flow will give us connection result, which will tell if there is any change in our connection
    //this function will launch the server, which device A will have to do
    fun startBluetoothServer(): Flow<ConnectionResult>

    //this function will be executed by device b, which will connect to device that has launched the server
    //both these devices will receive the connection result(when the connection was established, when there
    //was an error, when there was a msg received from the other party and so on
    fun connectToDevice(device: BluetoothDevice): Flow<ConnectionResult>

    //when we want device to disconnect
    fun closeConnection()
    //then we need to implement all these 3 fun. in AndroidBluetoothController

    fun release()
}