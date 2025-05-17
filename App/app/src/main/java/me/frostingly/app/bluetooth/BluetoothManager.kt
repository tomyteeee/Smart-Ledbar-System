import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.pm.PackageManager
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import me.frostingly.app.bluetooth.BluetoothManagerSingleton
import java.io.IOException
import java.util.UUID

class BluetoothManager(private val context: Context) {

    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private var bluetoothSocket: BluetoothSocket? = null
    private var device: BluetoothDevice? = null
    private val MY_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB") // SPP UUID

    // New variables for retry logic
    private var currentRetryCount = 1
    private val maxRetries = 3

    init {
        if (bluetoothAdapter == null) {
            Log.e("PROJEKTAS", "Bluetooth is not supported on this device.")
        }
    }

    fun isBluetoothEnabled(): Boolean {
        return bluetoothAdapter?.isEnabled == true
    }

    fun isConnected(): Boolean {
        return bluetoothSocket?.isConnected == true
    }

    fun getCurrentRetryCount(): Int {
        return currentRetryCount
    }

    @SuppressLint("MissingPermission")
    fun connectToDevice(macAddress: String, onConnected: () -> Unit, onError: (String) -> Unit) {
        val pairedDevices: Set<BluetoothDevice> = bluetoothAdapter?.bondedDevices ?: emptySet()
        device = pairedDevices.find { it.address == macAddress }

        if (device == null) {
            onError("Device not found")
            return
        }

        val handler = Handler(Looper.getMainLooper())
        val timeoutRunnable = Runnable {
            bluetoothSocket?.let {
                if (!it.isConnected) {
                    it.close()
                    onError("Connection timed out")
                }
            }
        }
        handler.postDelayed(timeoutRunnable, 20000) // Timeout after 20 seconds

        // Retry connection logic
        attemptConnection(macAddress, onConnected, onError, handler, timeoutRunnable)
    }

    private fun attemptConnection(macAddress: String, onConnected: () -> Unit, onError: (String) -> Unit,
                                  handler: Handler, timeoutRunnable: Runnable) {
        // Start a new thread for connection
        Thread {
            try {
                if (ActivityCompat.checkSelfPermission(
                        context,
                        Manifest.permission.BLUETOOTH_CONNECT
                    ) != PackageManager.PERMISSION_GRANTED ||
                    ActivityCompat.checkSelfPermission(
                        context,
                        Manifest.permission.BLUETOOTH
                    ) != PackageManager.PERMISSION_GRANTED ||
                    ActivityCompat.checkSelfPermission(
                        context,
                        Manifest.permission.BLUETOOTH_ADMIN
                    ) != PackageManager.PERMISSION_GRANTED) {
                    onError("Bluetooth permissions not granted")
                    return@Thread
                }

                bluetoothSocket = device?.createRfcommSocketToServiceRecord(MY_UUID)

                // Attempt connection with retries
                while (currentRetryCount <= maxRetries) {
                    try {
                        bluetoothSocket?.connect()
                        handler.removeCallbacks(timeoutRunnable) // Connection successful, remove timeout
                        handler.post {
                            onConnected() // Notify on successful connection
                        }
                        return@Thread // If successful, exit the function
                    } catch (e: IOException) {
                        currentRetryCount++
                        Log.e("PROJEKTAS", "Connection attempt $currentRetryCount failed: ${e.message}")
                        if (currentRetryCount >= maxRetries) {
                            handler.removeCallbacks(timeoutRunnable)
                            handler.post {
                                onError("Failed to connect after $maxRetries attempts: ${e.message}")
                            }
                            return@Thread // After max retries, report the error
                        }
                        // If not the last attempt, try again after a short delay
                        Thread.sleep(2000) // Wait for 2 seconds before retrying
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
                handler.removeCallbacks(timeoutRunnable)
                handler.post {
                    onError("Failed to connect: ${e.message}") // Notify on error
                }
            }
        }.start()
    }

    fun sendData(data: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        try {
            bluetoothSocket?.outputStream?.write(data.toByteArray())
            onSuccess()
        } catch (e: IOException) {
            e.printStackTrace()
            onError("Failed to send data: ${e.message}")
        }
    }

    @SuppressLint("MissingPermission")
    fun closeBluetoothSocket() {
        try {
            if (isSocketConnected()) {
                // Ensure to close the socket and the streams
                bluetoothSocket?.inputStream?.close()
                bluetoothSocket?.outputStream?.close()
                bluetoothSocket?.close()

                bluetoothSocket = null // Clear the socket reference
                Log.d("PROJEKTAS", "Bluetooth connection closed successfully.")
            } else {
                Log.d("PROJEKTAS", "Bluetooth socket is not connected or already closed.")
            }
        } catch (e: IOException) {
            Log.e("PROJEKTAS", "Error closing Bluetooth connection: ${e.message}")
        }
    }

    // Helper method to check if the Bluetooth socket is still connected
    fun isSocketConnected(): Boolean {
        return try {
            bluetoothSocket?.outputStream?.write(byteArrayOf(0))  // Try writing a small byte to check
            true
        } catch (e: IOException) {
            false  // If writing throws an exception, the socket is disconnected
        }
    }
}

fun sendBluetoothCommand(context: Context, cmd: String, successCommand: () -> Unit) {
    val bluetoothManager = BluetoothManagerSingleton.getInstance(context)
    val chunkSize = 1
    val totalLength = cmd.length
    val numberOfChunks =
        (totalLength + chunkSize - 1) / chunkSize  // Calculate number of chunks
    // Iterate through each chunk
    for (i in 0 until numberOfChunks) {
        val startIndex = i * chunkSize
        val endIndex = minOf(startIndex + chunkSize, totalLength)
        val chunk = cmd.substring(startIndex, endIndex)

        // Send the chunk
        bluetoothManager.sendData(
            chunk,
            onSuccess = {
                Log.d(
                    "PROJEKTAS",
                    "Successfully sent chunk: $chunk"
                )
            },
            onError = { error ->
                Log.e(
                    "PROJEKTAS",
                    "Error sending chunk: $chunk. Error: $error"
                )
            }
        )
    }
    successCommand()
}
