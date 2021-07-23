package com.example.speechtotext.devicecontroller

import com.example.speechtotext.ConnectionType
import com.example.speechtotext.DeviceName
import com.example.speechtotext.Feature

class PilotflyGimbalController: Device, Gimbal {

    val featuresAvailable = arrayListOf<Feature>(Feature.ABSOLUTE_MOVEMENT, Feature.INCREMENTAL_MOVEMENT)
    private val connectionType = ConnectionType.BLUETOOTH

    override fun getDeviceName(): DeviceName {
        TODO("Not yet implemented")
    }

    override fun connectDevice() {
        TODO("Not yet implemented")
    }

    override fun getConnectionType(): ConnectionType {
        return this.connectionType
    }

    override fun isFeatureAvailable(feature: Feature): Boolean {
        TODO("Not yet implemented")
    }

    override fun getAvailableFeatures(): ArrayList<Feature> {
        return featuresAvailable
    }

    override fun setIp(ip: String) {
        TODO("Not yet implemented")
    }

    override fun move(yaw: String, pitch: String, roll: String, isAbsolute: Int) {
        TODO("Not yet implemented")
    }
}