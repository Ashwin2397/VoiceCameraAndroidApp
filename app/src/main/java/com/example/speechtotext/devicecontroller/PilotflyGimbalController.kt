package com.example.speechtotext.devicecontroller

import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.example.speechtotext.*
import java.lang.ref.WeakReference

object PilotflyGimbalController: Device, Gimbal {

//    val featuresAvailable = arrayListOf<Feature>(Feature.ABSOLUTE_MOVEMENT, Feature.INCREMENTAL_MOVEMENT)
    private val featuresAvailable = arrayListOf<Feature>(Feature.LEFT, Feature.RIGHT, Feature.UP, Feature.DOWN, Feature.ROLL)


    private val connectionType = ConnectionType.BLUETOOTH
    private val DEVICE_NAME = DeviceName.PILOTFLY
    lateinit var context: WeakReference<Context>

    // BT variables
    lateinit var bluetoothAdapter: BluetoothAdapter
    val REQUEST_ENABLE_BT = 0
    private val SCAN_PERIOD: Long = 10000 // Stops scanning after 10 seconds.
    val bleService = BluetoothLeService()


    override fun setApplicationContext(context: Context) {
        this.context = WeakReference(context)
    }

    override fun getDeviceName(): DeviceName {
        return this.DEVICE_NAME
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun connectDevice(mainActivity: MainActivity) {

        val permissions: Array<String> = arrayOf(android.Manifest.permission.BLUETOOTH, android.Manifest.permission.ACCESS_FINE_LOCATION)
        mainActivity.requestPermissions( permissions, 101)

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

        if (bluetoothAdapter == null) {
            // Device doesn't support Bluetooth
        }

        if (bluetoothAdapter?.isEnabled == false) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            mainActivity.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
        }else {

            scanLeDevice()
        }
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun scanLeDevice() {

        val bluetoothLeScanner = bluetoothAdapter.bluetoothLeScanner
        var scanning = false
        val handler = Handler()

        if (!scanning) { // Stops scanning after a pre-defined scan period.
            handler.postDelayed({
                scanning = false
                bluetoothLeScanner.stopScan(leScanCallback)
            }, SCAN_PERIOD)
            scanning = true
            bluetoothLeScanner.startScan(
                listOf<ScanFilter>(ScanFilter.Builder().setDeviceName("Pilotfly").build()),
                ScanSettings.Builder().build(),
                leScanCallback,
            )
        } else {
            scanning = false
            bluetoothLeScanner.stopScan(leScanCallback)
        }
    }

    private val leScanCallback: ScanCallback = @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    object : ScanCallback() {
        @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)

            Log.d("BLE_SCANNER", "Device: ${result.device.name}")
//            device = result.device
//
            // Connect to device
            bleService.initialize()
            bleService.connect(result.device.address)

            // Pass service to Pilotfly controller to use

        }
    }

    override fun getConnectionType(): ConnectionType {
        return this.connectionType
    }

    override fun isFeatureAvailable(feature: Feature): Boolean {
        if ( this.featuresAvailable.contains(feature) ) {
            return true;
        }

        return false;
    }

    override fun getAvailableFeatures(): ArrayList<Feature> {
        return featuresAvailable
    }

    override fun setIp(ip: String) {
    }

    override fun move(yaw: String, pitch: String, roll: String, isAbsolute: Int) {

        bleService.sendCommand()
    }
}