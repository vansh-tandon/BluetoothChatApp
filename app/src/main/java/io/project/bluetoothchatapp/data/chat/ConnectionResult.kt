package io.project.bluetoothchatapp.data.chat

import androidx.compose.ui.graphics.drawscope.Stroke

sealed interface ConnectionResult{
    //will define different types of connection result
    object ConnectionEstablished: ConnectionResult
    data class Error(val message: String): ConnectionResult

}