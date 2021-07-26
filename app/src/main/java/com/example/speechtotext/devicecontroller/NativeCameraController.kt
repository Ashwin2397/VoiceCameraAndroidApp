package com.example.speechtotext.devicecontroller

import android.content.Context
import android.graphics.Bitmap
import com.example.speechtotext.ConnectionType
import com.example.speechtotext.DeviceName
import com.example.speechtotext.Feature

//
//import android.content.Context
//import android.content.pm.PackageManager
//import android.hardware.Camera
//import android.hardware.camera2
//import android.hardware.camera2.CameraManager
//
//    class NativeCameraController(
//        val context: Context
//    ){
//
////        val cameraManager = CameraManager
//
//        fun connect() {
//
//
//        }
//
//        /*
//        * Checks if device has a camera
//        * @return Boolean to indicate existence of camera
//        * */
//        private fun checkCameraHardware(): Boolean {
//
//            var hasCamera = false
//
//            if (this.context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
//
//                // this device has a camera
//                hasCamera = true
//            }
//
//            return hasCamera
//        }
//
//        /** A safe way to get an instance of the Camera object. */
//        private fun getCameraInstance(): Camera? {
//            return try {
//                Camera.open() // attempt to get a Camera instance
//            } catch (e: Exception) {
//                // Camera is not available (in use or does not exist)
//                null // returns null if camera is unavailable
//            }
//
//
//        }
//
//
//    }

class NativeCameraController: Device, Camera {

    private val singleton = NativeCameraController()

    val featuresAvailable = arrayListOf<Feature>(Feature.SHOOT, Feature.ZOOM)
    private val connectionType = ConnectionType.NATIVE
    private val deviceName = DeviceName.NATIVE
    var context: Context? = null

    fun getInstance(): NativeCameraController {

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
        return featuresAvailable
    }

    override fun setIp(ip: String) {
    }

    override fun configureLiveview(callback: (camera: Camera) -> Unit) {
    }

    override fun getLiveviewFlip(callback: (bitmap: Bitmap?) -> Unit) {
    }

    override fun shoot() {
    }

    override fun setZoom(zoomFactor: String) {
    }

    override fun setAperture(apertureFactor: String) {
    }

    override fun setFocusType(focusType: String) {
    }
}