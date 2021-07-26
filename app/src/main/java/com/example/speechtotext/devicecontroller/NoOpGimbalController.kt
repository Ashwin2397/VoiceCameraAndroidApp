package com.example.speechtotext.devicecontroller

import android.content.Context
import android.widget.TextView
import com.example.speechtotext.ConnectionType
import com.example.speechtotext.DeviceName
import com.example.speechtotext.Feature

class NoOpGimbalController: Device, Gimbal {

    private val singleton = NoOpGimbalController()

    private val featuresAvailable = arrayListOf<Feature>(Feature.LEFT, Feature.RIGHT, Feature.UP, Feature.DOWN, Feature.ROLL)
    private val deviceName = DeviceName.NO_OP_GIMBAL
    private val connectionType = ConnectionType.NATIVE
    var context: Context? = null


    var textView: TextView? = null

    fun getInstance(): NoOpGimbalController {

        return this.singleton
    }
    override fun setApplicationContext(context: Context) {
        this.context = context
    }

    override fun getDeviceName(): DeviceName {
        return this.deviceName

    }

    override fun connectDevice() {

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
        return this.featuresAvailable

    }

    override fun setIp(ip: String) {

    }

    override fun move(yaw: String, pitch: String, roll: String, isAbsolute: Int) {
        textView?.text = "YAW: ${yaw}\nPITCH: ${pitch}\nROLL: ${roll}\n"

    }
}