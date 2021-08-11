package com.example.speechtotext.devicecontroller

import android.content.Context
import android.graphics.Bitmap
import com.example.speechtotext.*

/*
* TODO:
*  - Should add adapters for both gimbal and camera that contains Toasts for functions that
*   are not implemented
* -
*
* */

interface Device {
    fun getDeviceName(): DeviceName
    fun connectDevice(mainActivity: MainActivity): Unit
    fun getConnectionType(): ConnectionType

    /*
    * Check if given feature is available.
    * @param {feature.<SOME FEATURE>} Feature -.
    * @return {Boolean} Indicating if feature is available.
    */
    fun isFeatureAvailable(feature: Feature): Boolean

    /*
   * Get list of features available by camera
   * @return {Array} An array of feature.camera enums.
   */
    fun getAvailableFeatures(): ArrayList<Feature>

    fun setIp(ip: String)

    fun setApplicationContext(context: Context)


}


interface Gimbal {

    /*
    * Moves gimbal incrementally with the provided angles in it's specified ranges.
    * Yaw: Horizontal rotation, [-180, 180]
    * Pitch: Vertical rotation, [-90, 90]
    * Roll: Rotate about central point, [-90, 90]
    * @param {Map<MasterGimbal.Axis, Int>} coordinates The coordinates that the gimbal has to move to.
    * @param {Boolean} isAbsolute We wil always send true and handle the logic for both cases in MasterGimbal.
    * */
    fun move(coordinates: Map<MasterGimbal.Axis, Int>, isAbsolute: Boolean): Unit

}

interface Camera {

    fun configureLiveview(callback: (camera: Camera) -> Unit): Unit
    fun getLiveviewFlip(callback: (bitmap: Bitmap?) -> Unit)
    fun shoot(): Unit
    fun setZoom(zoomFactor: String): Unit
    fun setAperture(apertureFactor: String): Unit
    fun setFocusType(focusType: String): Unit

}



