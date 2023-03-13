package io.project.bluetoothchatapp.domain.chat

import java.io.IOException

class TransferFailedException: IOException("Reading incoming data failed")