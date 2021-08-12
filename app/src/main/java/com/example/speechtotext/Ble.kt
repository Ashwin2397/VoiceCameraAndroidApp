package com.example.speechtotext

import android.app.Service
import android.bluetooth.*
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.speechtotext.devicecontroller.MasterGimbal
import java.util.*
import kotlin.math.absoluteValue
import kotlin.math.roundToInt
import java.lang.Long.parseLong

class BluetoothLeService : Service() {

    private val TAG = "BLE_SERVICE"

    private val binder = LocalBinder()
    lateinit var bluetoothGatt: BluetoothGatt
    lateinit var characteristic: BluetoothGattCharacteristic
    private var bluetoothAdapter: BluetoothAdapter? = null

    val SERVICE_UUID = "0000ffe0-0000-1000-8000-00805f9b34fb"
    val CHARACTERISTIC_UUID = "0000ffe1-0000-1000-8000-00805f9b34fb"

    var i = 0
    var testCommands = arrayListOf(72, 34, 56, 23, 12, 90, 3, 11, 87, 25, 62)

    fun initialize(): Boolean {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (bluetoothAdapter == null) {
            Log.e(TAG, "Unable to obtain a BluetoothAdapter.")
            return false
        }
        return true
    }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    fun connect(address: String) {
        bluetoothAdapter?.let { adapter ->
            try {
                val device = adapter.getRemoteDevice(address)

                bluetoothGatt = device.connectGatt(this, false, bluetoothGattCallback)

            } catch (exception: IllegalArgumentException) {
                Log.w(TAG, "Device not found with provided address.")
            }
            // connect to the GATT server on the device

        } ?: run {
            Log.w(TAG, "BluetoothAdapter not initialized")
        }
    }

    private val bluetoothGattCallback = @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    object : BluetoothGattCallback() {
        private val TAG = "BT_CALLBACK"
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                // successfully connected to the GATT Server
                gatt?.discoverServices()

                Log.d(TAG, "Connected!!")
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                // disconnected from the GATT Server
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            super.onServicesDiscovered(gatt, status)

            characteristic = bluetoothGatt
                .getService(UUID.fromString(SERVICE_UUID))
                .getCharacteristic(UUID.fromString(CHARACTERISTIC_UUID))

            bluetoothGatt = gatt ?: bluetoothGatt

            Log.d(TAG, "Services discovered!!")
        }

    }

    // REFACTOR_CRITICAL: Use coordinates map to send the appropriate coordinates to the Pilotfly gimbal.
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    fun sendCommand(coordinates: Map<MasterGimbal.Axis, Int>) {

        val commandBuilder = CommandBuilder() //REFACTOR_CRITICAL: Command Builder is not able to handle zero!!!


        commandBuilder.setRoll(coordinates[MasterGimbal.Axis.ROLL]!!) // TEST, 'i' variable just simulates change in command
        commandBuilder.setPitch(coordinates[MasterGimbal.Axis.PITCH]!!)
        commandBuilder.setYaw(coordinates[MasterGimbal.Axis.YAW]!!)

//        if (i >= testCommands.size) {
//            i = 0
//        }
//        commandBuilder.setRoll(0) // TEST, 'i' variable just simulates change in command
//        commandBuilder.setPitch(testCommands[i])
//        commandBuilder.setYaw(0)
//        i += 1


        characteristic.setValue(commandBuilder.build())
        characteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT)

        bluetoothGatt.writeCharacteristic(characteristic)

    }
    override fun onBind(intent: Intent): IBinder? {
        return binder
    }

    inner class LocalBinder : Binder() {
        fun getService() : BluetoothLeService {
            return this@BluetoothLeService
        }
    }
}
//
//fun main() {
//
//
////        print(((0x02+0xF0+0x03)%256).hexString())
//    val cmd = CommandBuilder()
//    cmd.angles[2] = -180
//    cmd.angles[0] = 45
//
//    println(cmd.build())
//    //        println(cmd.convertAngle(45))
////        println(cmd.convertAngle(90))
////        println(cmd.convertAngle(180))
////        println(cmd.convertAngle(-45))
////        println(cmd.convertAngle(-90))
////        println(cmd.convertAngle(-180))
//
//
////        print((-2048).absoluteValue)
//}

fun Int.hexString(): String {
    return toString(16)
}

fun String.decodeHex(): ByteArray {
    check(length % 2 == 0) { "Must have an even length" }

    return chunked(2)
        .map { it.toInt(16).toByte() }
        .toByteArray()
}
class CommandBuilder {

    private val TAG = "COMMAND_BUILDER"
    val START_CHARACTER = "3E"
    val MOVE_CMD_ID = "43"
    val PAYLOAD_SIZE = "0D"
    val HEADER_CHECKSUM = "50"

    val CONTROL_MODE_NO_CONTROL = "00"
    val CONTROL_MODE_ANGLE = "02" // Use this as default

    val SPEED = "0002" // Increase for higher speed of movement

    val MAX_ANGLE = 180 // Therefore only ever 1 byte
    val MIN_ANGLE = -180

    val ANGLE_CONVERSION_FACTOR = 0.02197265625
    var angles = mutableListOf(
        0, // Roll
        0, // Pitch
        0, // Yaw
    )

    var payloadSize = ""

    fun setRoll(rollAngle: Int) {
        angles[0] = rollAngle
    }

    fun setPitch(pitchAngle: Int) {
        angles[1] = pitchAngle
    }

    fun setYaw(yawAngle: Int) {
        angles[2] = yawAngle
    }

    fun build(): ByteArray {

        var command = "${START_CHARACTER}${MOVE_CMD_ID}${PAYLOAD_SIZE}${HEADER_CHECKSUM}${CONTROL_MODE_ANGLE}"

        var payloadChecksum = 0x02
        angles.forEach {
            var convertedAngle = convertAngle(it) // String


            // For each byte in converted angle
//            convertedAngle.decodeHex().forEach {
//                payloadChecksum += it.toInt()
//            }
            // Assumes that the first byte is 0x00, which is not exactly good
            payloadChecksum += parseLong(convertedAngle.slice(IntRange(2,3)), 16).toInt()
            payloadChecksum += parseLong(convertedAngle.slice(IntRange(0,1)), 16).toInt()

            // For each byte in speed
            SPEED.decodeHex().forEach {
                payloadChecksum += it.toInt()
            }

            command += SPEED + convertedAngle
        }

        command += parsePayloadChecksum(payloadChecksum)

        Log.d(TAG, command)

        return command.decodeHex()

    }

    fun parsePayloadChecksum(payloadChecksum: Int): String {

        var payloadChecksumHexString = (payloadChecksum%256).hexString()

        val needsPadding = payloadChecksumHexString.length < 2
        if (needsPadding) {

            payloadChecksumHexString = "0" + payloadChecksumHexString
        }
        return payloadChecksumHexString
    }

    /*
    * Converts the given decimal integer angle into the hex representation to be used in command.
    * @param {Int} The decimal integer angle to convert
    * @return {String} The hex representation to be included in the command.
    * */
    fun convertAngle(angle: Int): String {

        val converted = (angle/ANGLE_CONVERSION_FACTOR).roundToInt()
        var hexRepresentation = hexRepresentation(converted)

        return littleEndian(hexRepresentation)
    }

    fun littleEndian(hexRepresentation: String): String{
//        var hexWithPad = (if (hexRepresentation.length == 3) "0${hexRepresentation}" else hexRepresentation)
        var hexWithPad = hexRepresentation
        for(i in 0 until 4 - hexRepresentation.length){
            hexWithPad = "0" + hexWithPad
        }

        return hexWithPad.slice(IntRange(2,3)) + hexWithPad.slice(IntRange(0,1))
    }
    /*
    * Gets the hex representation of a given decimal integer.
    * @param {Int} A signed integer value
    * @return {String} Hex representation of the given integer value, 2's complement if negative.
    * */
    fun hexRepresentation(decimal: Int): String {

        var hexRepresentation = ""
        if (decimal >= 0) {
            hexRepresentation = decimal.hexString()
        }else {
            hexRepresentation = twosComplement(decimal)
        }

        return hexRepresentation
    }
    /*
    * Takes negative decimal number and converts it to it's 2's complement equivalent
    * @param {Int} The negative decimal number
    * @param {String} 2's complement equivalent in hex representation
    * */
    fun twosComplement(negativeInt: Int): String {

        // Take absolute (ie. Make positive)
        // Invert each bit of number
        // Then add 1 to the LSB
        return ((negativeInt.absoluteValue.inv() + 65536) + 1).toString(16)
    }

    fun Int.hexString(): String {
        return toString(16)
    }

    fun String.decodeHex(): ByteArray {
        check(length % 2 == 0) {

            "${this} Must have an even length"
        }

        return chunked(2)
            .map { it.toInt(16).toByte() }
            .toByteArray()
    }

}