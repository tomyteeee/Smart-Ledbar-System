package me.frostingly.app

import BluetoothManager
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import me.frostingly.app.room.ConfigurationDB.ConfigurationRepository
import me.frostingly.app.room.LedbarDB.LedbarRepository
import kotlin.system.exitProcess

class MainActivity : ComponentActivity() {

    private val bluetoothManager: BluetoothManager = BluetoothManager(this)

    private val ledbarRepository by lazy {
        LedbarRepository(applicationContext)
    }

    private val configurationRepository by lazy {
        ConfigurationRepository(applicationContext)
    }

    @SuppressLint("NewApi")
    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Check if Bluetooth permissions are granted at runtime

        checkAndLoadPerms()

        val sharedPreferences = SharedPreferences(this)
        enableEdgeToEdge()

        setContent {
            Navigation(sharedPreferences, this, ledbarRepository, configurationRepository, lifecycleScope)
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    fun checkAndLoadPerms() {
        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.BLUETOOTH_CONNECT
            ) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.BLUETOOTH_SCAN
            ) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.BLUETOOTH_ADMIN
            ) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.BLUETOOTH_ADVERTISE
            ) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            Log.d("Bluetooth", "All Bluetooth permissions already granted")
            // Proceed with Bluetooth operations
        } else {
            // Request additional Bluetooth permissions
            bluetoothPermissionRequest.launch(
                arrayOf(
                    android.Manifest.permission.BLUETOOTH,
                    android.Manifest.permission.BLUETOOTH_CONNECT,
                    android.Manifest.permission.BLUETOOTH_SCAN,
                    android.Manifest.permission.BLUETOOTH_ADMIN,
                    android.Manifest.permission.BLUETOOTH_ADVERTISE,
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onStop() {
        super.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    private fun closeBluetoothSocket() {
        try {
            bluetoothManager.closeBluetoothSocket()  // Calls the closeBluetoothSocket method in BluetoothManager
        } catch (e: Exception) {
            Log.e("PROJEKTAS", "Error closing Bluetooth connection: ${e.message}")
        }
    }

    // Register for the permission request result
    private val bluetoothPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
        ActivityResultCallback { permissions ->
            // Check if all Bluetooth permissions are granted
            if (permissions[android.Manifest.permission.BLUETOOTH_CONNECT] == true &&
                permissions[android.Manifest.permission.BLUETOOTH_SCAN] == true) {
                Log.d("Bluetooth", "Bluetooth permissions granted")
                // Proceed with Bluetooth operations
            } else {
                Log.d("Bluetooth", "Bluetooth permissions denied")
                exitProcess(0)
                // Handle the case when permissions are denied
            }
        }
    )
}