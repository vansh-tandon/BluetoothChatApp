package io.project.bluetoothchatapp.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.project.bluetoothchatapp.domain.chat.BluetoothController
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

//to interact with the bluetooth controller and then map the result
//to ui state
@HiltViewModel
class BluetoothViewModel @Inject constructor(private val bluetoothController:BluetoothController): ViewModel() {

    private val _state = MutableStateFlow(BluetoothUiState())
    //public exposed version of the state
    //combine-> to combine multiple stateFlows
    //which is done when either of the stateflow emits the value
    val state = combine(
        bluetoothController.scannedDevices,
        bluetoothController.pairedDevices,
        _state
    ){//either of these values changes, we'll get the new values here
        scannedDevices, pairedDevices, state ->
        state.copy(
            scannedDevices = scannedDevices,
            pairedDevices = pairedDevices
        )
            //stateIn-> converts this normal flow into stateflow
        //so it caches the latest value-> result of copy operation
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), _state.value)

    fun startScan(){
        bluetoothController.startDiscovery()
    }
    fun stopScan(){
        bluetoothController.stopDiscovery()
    }


}