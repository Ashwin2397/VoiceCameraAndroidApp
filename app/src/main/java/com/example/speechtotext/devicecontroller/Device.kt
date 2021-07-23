package com.example.speechtotext.devicecontroller

import android.content.Context
import android.graphics.Bitmap
import com.example.speechtotext.ConnectionType
import com.example.speechtotext.DeviceName
import com.example.speechtotext.Feature

/*
* TODO:
*  - Should add adapters for both gimbal and camera that contains Toasts for functions that
*   are not implemented
* -
*
* */

interface Device {
    fun getDeviceName(): DeviceName
    fun connectDevice(): Unit
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
    * Moves gimbal incrementally with the provided angles.
    * @param {Float} yaw Horizontal rotation
    * @param {Float} pitch Vertical rotation
    * @param {Float} roll Rotate about central point
    * @param {Integer} isIncremental 1 for incremental, 0 for absolute
    * */
    fun move(yaw: String, pitch: String, roll: String, isAbsolute: Int): Unit

}

interface Camera {

    fun configureLiveview(callback: (camera: Camera) -> Unit): Unit
    fun getLiveviewFlip(callback: (bitmap: Bitmap?) -> Unit)
    fun shoot(): Unit
    fun setZoom(zoomFactor: String): Unit
    fun setAperture(apertureFactor: String): Unit
    fun setFocusType(focusType: String): Unit

}



