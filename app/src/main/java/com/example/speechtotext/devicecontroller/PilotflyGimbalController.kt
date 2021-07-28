package com.example.speechtotext.devicecontroller

import android.content.Context
import com.example.speechtotext.ConnectionType
import com.example.speechtotext.DeviceName
import com.example.speechtotext.Feature
import java.lang.ref.WeakReference

object PilotflyGimbalController: Device, Gimbal {

    val featuresAvailable = arrayListOf<Feature>(Feature.ABSOLUTE_MOVEMENT, Feature.INCREMENTAL_MOVEMENT)
    private val connectionType = ConnectionType.BLUETOOTH
    private val deviceName = DeviceName.PILOTFLY
    lateinit var context: WeakReference<Context>

    override fun setApplicationContext(context: Context) {
        this.context = WeakReference(context)
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
        return featuresAvailable
    }

    override fun setIp(ip: String) {
    }

    override fun move(yaw: String, pitch: String, roll: String, isAbsolute: Int) {
    }
}