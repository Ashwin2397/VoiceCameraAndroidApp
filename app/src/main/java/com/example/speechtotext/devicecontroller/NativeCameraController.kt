package com.example.speechtotext.devicecontroller

import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Camera
import android.hardware.camera2
import android.hardware.camera2.CameraManager

class NativeCameraController(
    val context: Context
){

    val cameraManager = CameraManager

    fun connect() {

        cameraManager.
    }

    /*
    * Checks if device has a camera
    * @return Boolean to indicate existence of camera
    * */
    private fun checkCameraHardware(): Boolean {

        var hasCamera = false

        if (this.context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA)) {

            // this device has a camera
            hasCamera = true
        }

        return hasCamera
    }

    /** A safe way to get an instance of the Camera object. */
    private fun getCameraInstance(): Camera? {
        return try {
            Camera.open() // attempt to get a Camera instance
        } catch (e: Exception) {
            // Camera is not available (in use or does not exist)
            null // returns null if camera is unavailable
        }

        
    }


}