package me.frostingly.app.bluetooth

import BluetoothManager
import android.content.Context

object BluetoothManagerSingleton {
    private var bluetoothManager: BluetoothManager? = null

    fun getInstance(context: Context): BluetoothManager {
        if (bluetoothManager == null) {
            bluetoothManager = BluetoothManager(context)
        }
        return bluetoothManager!!
    }
}