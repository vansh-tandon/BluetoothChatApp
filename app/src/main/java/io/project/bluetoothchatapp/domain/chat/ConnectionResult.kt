package io.project.bluetoothchatapp.domain.chat

sealed interface ConnectionResult{
    //will define different types of connection result
    object ConnectionEstablished: ConnectionResult
    data class TransferSucceeded(val message: BluetoothMessage): ConnectionResult
    data class Error(val message: String): ConnectionResult

}