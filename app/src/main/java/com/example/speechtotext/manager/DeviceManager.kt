package com.example.speechtotext.manager

import android.content.Context
import com.example.speechtotext.ConnectionType
import com.example.speechtotext.DeviceName
import com.example.speechtotext.Feature
import com.example.speechtotext.devicecontroller.*
import java.io.Serializable
import java.util.*
import kotlin.collections.ArrayList


/*
* NOTE: If we used a frontend framework, we would be able to dynamically render all the
* cameras and gimbals that were available. Maybe when porting it over, we can do that.
*/

class SystemManager: Serializable {

    val deviceManager = MasterControllerFactory()

    fun getFeatures(): Features {

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
                    ConnectionType.HTTP to true,
                    ConnectionType.NATIVE to false
                ).get(it.value.getConnectionType())!!
            )
        }


        var features = Features(availableCameraFeatures, availableGimbalFeatures, isHttpDevice)

        return features
    }


}

data class Features(
    val availableCameraFeatures: Map<DeviceName, ArrayList<Feature>>,
    val availableGimbalFeatures: Map<DeviceName, ArrayList<Feature>>,
    val isHttpDevice: Map<DeviceName, Boolean>
): Serializable

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
