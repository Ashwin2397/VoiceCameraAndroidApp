package com.example.speechtotext.manager

import com.example.speechtotext.ConnectionType
import com.example.speechtotext.DeviceName
import com.example.speechtotext.Feature
import com.example.speechtotext.ZoomObserver
import com.example.speechtotext.devicecontroller.*
import java.io.Serializable
import java.util.*
import kotlin.collections.ArrayList


/*
* NOTE: If we used a frontend framework, we would be able to dynamically render all the
* cameras and gimbals that were available. Maybe when porting it over, we can do that.
*/

class SystemManager: Serializable {

    val deviceManager = DeviceManager()

    fun getFeaturesManager(): FeaturesManager {

        var availableCameraFeatures = mutableMapOf<DeviceName, ArrayList<Feature>>()
        var availableGimbalFeatures = mutableMapOf<DeviceName, ArrayList<Feature>>()
        var isHttpDevice = mutableMapOf<DeviceName, Boolean>()

        deviceManager.availableCameras().forEach {

            availableCameraFeatures.put(it.key, it.value.getAvailableFeatures())

            isHttpDevice.put(it.key,
                mapOf<ConnectionType, Boolean>(
                    ConnectionType.BLUETOOTH to false,
                    ConnectionType.HTTP to true,
                    ConnectionType.NATIVE to false
                ).get(it.value.getConnectionType())!!
            )
        }

        deviceManager.availableGimbals().forEach {

            availableGimbalFeatures.put(it.key, it.value.getAvailableFeatures())

            isHttpDevice.put(it.key,
                mapOf<ConnectionType, Boolean>(
                    ConnectionType.BLUETOOTH to false,
                    ConnectionType.HTTP to true
                ).get(it.value.getConnectionType())!!
            )
        }


        var featuresManager = FeaturesManager(availableCameraFeatures, availableGimbalFeatures, isHttpDevice)

        return featuresManager
    }


}

/*
* Manages all the UI components that are selected and to be rendered
*/
class FeaturesManager(
    val availableCameraFeatures: Map<DeviceName, ArrayList<Feature>>,
    val availableGimbalFeatures: Map<DeviceName, ArrayList<Feature>>,
    val isHttpDevice: Map<DeviceName, Boolean>
): Serializable {


    // All the camera features UI components that have been implemented
    /*val cameraFeatures = mutableMapOf<String, Observer>(

        "zoom" to ZoomObserver()
    )
    */

    var chosenCamera: DeviceName = DeviceName.CANON     // REFACTOR: System manager will provide this after fetching data from DB
    var cameraIp = ""

    var chosenGimbal: DeviceName = DeviceName.DJI_RS_2    // REFACTOR: System manager will provide this after fetching data from DB
    var gimbalIp = ""

    // REFACTOR: System manager will provide this after fetching data from DB
    var chosenCameraFeatures = arrayListOf<Feature>(
        Feature.SHOOT
    )

    // All the gimbal features UI components that have been implemented
    /*val gimbalFeatures = {

        "left": new LeftObserver()

    };*/

    // REFACTOR: System manager will provide this after fetching data from DB
    var  chosenGimbalFeatures = arrayListOf<Feature>(
        Feature.MOVE
    )

    /*
    * Activates the chosen feature.
    * @param {String} cameraFeature The chosen camera feature to be activated by the user.
    */
    fun addCameraFeature(cameraFeature: Feature) {

        this.chosenCameraFeatures.add(cameraFeature)
    }

    /*
    * Activates the chosen feature.
    * @param {String} gimbalFeature The chosen gimbal feature to be activated by the user.
    */
    fun addGimbalFeature(gimbalFeature: Feature) {

        this.chosenGimbalFeatures.add(gimbalFeature)
    }


}


class DeviceManager{

    var gimbal:Device? = null
    var camera:Device? = null

    val gimbals = mutableMapOf<DeviceName, Device>(

        DeviceName.DJI_RS_2 to DJIGimbalController(),
        DeviceName.PILOTFLY to PilotflyGimbalController(),

    )

    val cameras = mutableMapOf<DeviceName, Device>(

        DeviceName.CANON to CanonCameraController(),
        DeviceName.NATIVE to NativeCameraController()
    )


    /*
    * Connects the chosen gimbal and specifies it as the current gimbal of choice.
    * @param {String} gimbal The chosen gimbal.
    */
    fun connectGimbal(gimbal: DeviceName) {

        this.gimbal = this.gimbals[gimbal]

    }

    /*
    * Connects the chosen camera and specifies it as the current camera of choice.
    * @param {String} camera The chosen camera.
    */
    fun connectCamera(camera: DeviceName) {

        this.camera = this.cameras[camera]

    }

    /*
    * Returns all the gimbals that is available for use by user.
    * @return list of all gimbals
    * NOTE: Needs refactoring to only return enums rather than the controllers too.
    */
    fun availableGimbals(): Map<DeviceName, Device>{

        return this.gimbals
    }

    /*
    * Returns all the cameras that is available for use by user.
    * @return list of all cameras.
    * NOTE: Needs refactoring to only return enums rather than the controllers too.
    */
    fun availableCameras(): Map<DeviceName, Device>{

        return this.cameras

    }

}
