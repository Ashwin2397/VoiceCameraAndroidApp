package com.example.speechtotext.devicecontroller

import com.example.speechtotext.DeviceName

class MasterControllerFactory {

    val controllers = mapOf<DeviceName, Any>(
        DeviceName.CANON to CanonCameraController,
        DeviceName.NATIVE to NativeCameraController,
        DeviceName.DJI_RS_2 to DJIGimbalController,
        DeviceName.PILOTFLY to PilotflyGimbalController,
        DeviceName.NO_OP_CAMERA to NoOpCameraController,
        DeviceName.NO_OP_GIMBAL to NoOpGimbalController
    )

    fun getCameraInstance(deviceName: DeviceName): Camera {

        return controllers.get(deviceName) as Camera
    }

    fun getGimbalInstance(deviceName: DeviceName): Gimbal {

        return controllers.get(deviceName) as Gimbal
    }

    fun getDeviceInstance(deviceName: DeviceName): Device {

        return controllers.get(deviceName) as Device
    }
}