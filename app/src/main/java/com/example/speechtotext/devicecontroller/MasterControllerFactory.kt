package com.example.speechtotext.devicecontroller

import com.example.speechtotext.DeviceName
import com.example.speechtotext.DeviceType

class MasterControllerFactory {

    val controllers = mapOf<DeviceType, Map<DeviceName, Any>>(

        DeviceType.GIMBAL to mapOf<DeviceName, Any>(

            DeviceName.DJI_RS_2 to DJIGimbalController,
            DeviceName.PILOTFLY to PilotflyGimbalController,
            DeviceName.NO_OP_GIMBAL to NoOpGimbalController

        ),
        DeviceType.CAMERA to mapOf<DeviceName, Any>(

            DeviceName.CANON to CanonCameraController,
            DeviceName.NATIVE to NativeCameraController,
            DeviceName.NO_OP_CAMERA to NoOpCameraController
        )
    )

    fun getCameraInstance(deviceName: DeviceName): Camera {

        return controllers.get(DeviceType.CAMERA)!!.get(deviceName) as Camera
    }

    fun getGimbalInstance(deviceName: DeviceName): Gimbal {

        return controllers.get(DeviceType.GIMBAL)!!.get(deviceName) as Gimbal
    }

    fun getDeviceInstance(deviceName: DeviceName): Device {

        return (controllers.get(DeviceType.CAMERA)!!.get(deviceName) ?: controllers.get(DeviceType.GIMBAL)!!.get(deviceName)) as Device
    }

    /*
    * Returns all the gimbals that is available for use by user.
    * @return list of all gimbals
    * NOTE: Needs refactoring to only return enums rather than the controllers too.
    */
    fun availableGimbals(): Map<DeviceName, Device>{

        return this.controllers.get(DeviceType.GIMBAL) as Map<DeviceName, Device>
    }

    /*
    * Returns all the cameras that is available for use by user.
    * @return list of all cameras.
    * NOTE: Needs refactoring to only return enums rather than the controllers too.
    */
    fun availableCameras(): Map<DeviceName, Device>{

        return this.controllers.get(DeviceType.CAMERA) as Map<DeviceName, Device>

    }
}