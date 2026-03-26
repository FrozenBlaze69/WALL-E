package fr.isen.valentin.wall_e.ble

import android.annotation.SuppressLint
import android.bluetooth.*
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import java.util.*

@SuppressLint("MissingPermission")
class BleManager(private val context: Context) {
    private val bluetoothAdapter: BluetoothAdapter? = context.getSystemService(BluetoothManager::class.java).adapter
    private var bluetoothGatt: BluetoothGatt? = null
    
    private val SERVICE_UUID = UUID.fromString("0000180c-0000-1000-8000-00805f9b34fb")
    private val CHAR_UUID = UUID.fromString("0000fe41-0000-1000-8000-00805f9b34fb")

    var onConnectionStateChange: ((Int) -> Unit)? = null
    var onWriteSuccess: (() -> Unit)? = null

    fun startScanning() {
        val scanner = bluetoothAdapter?.bluetoothLeScanner
        scanner?.startScan(object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                if (result.device.name == "WALL-E") {
                    scanner.stopScan(this)
                    connectToDevice(result.device)
                }
            }
        })
    }

    private fun connectToDevice(device: BluetoothDevice) {
        bluetoothGatt = device.connectGatt(context, false, gattCallback)
    }

    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            onConnectionStateChange?.invoke(newState)
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                gatt.discoverServices()
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            // Prêt à écrire
        }

        @Deprecated("Deprecated in Java")
        override fun onCharacteristicWrite(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) onWriteSuccess?.invoke()
        }
    }

    fun sendRoute(routeIds: List<Int>) {
        val service = bluetoothGatt?.getService(SERVICE_UUID)
        val characteristic = service?.getCharacteristic(CHAR_UUID)
        characteristic?.let {
            it.value = routeIds.map { id -> id.toByte() }.toByteArray()
            it.writeType = BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE
            bluetoothGatt?.writeCharacteristic(it)
        }
    }
}
