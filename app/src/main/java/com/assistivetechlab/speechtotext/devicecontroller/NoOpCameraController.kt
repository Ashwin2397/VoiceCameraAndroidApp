package com.assistivetechlab.speechtotext.devicecontroller

import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.widget.TextView
import androidx.annotation.RequiresApi
import com.assistivetechlab.speechtotext.*
import kotlinx.android.synthetic.main.activity_main.*
import java.lang.ref.WeakReference

object NoOpCameraController: Device, Camera {

    val featuresAvailable = arrayListOf<Feature>(Feature.SHOOT, Feature.MODE, Feature.ZOOM, Feature.FOCUS, Feature.APERTURE)
    private val deviceName = DeviceName.NO_OP_CAMERA
    private val connectionType = ConnectionType.NATIVE

    lateinit var textView: WeakReference<TextView>
    lateinit var context: WeakReference<Context>

    override fun setApplicationContext(context: Context) {
        this.context = WeakReference(context)
    }

    override fun setIp(ip: String) {
    }

    override fun getDeviceName(): DeviceName {
        return this.deviceName
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun connectDevice(mainActivity: MainActivity) {

        textView = WeakReference(mainActivity.transcription)
        mainActivity.transcription.setZ(2.toFloat()) // I didnt need to explicit state that this runs on the UI thread for some reason ...
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

        return this.featuresAvailable
    }

    /*
    * Configures liveview so that camera can connect to imageView.
    *
    * */
    override fun configureLiveview(callback: (camera: Camera) -> Unit) {

    }

    /*
    * Gets the static live view image as a bitmap
    * Uses okHTTP client.
    * @param {} Callback function to run once image has been received from camera.
    * */
    override fun getLiveviewFlip(callback: (bitmap: Bitmap?) -> Unit) {


    }

    /*
    * Sends HTTP shoot request to camera to grab a picture.
    * Uses retrofit.
    * */
    override fun shoot(): Unit {

        this.textView.get()?.text = "SHOOT"

    }

    /*
    * Zooms in as per the zoom factor provided
    * @param {String} zoomFactor The integer amount to zoom in by.
    * */
    override fun setZoom(zoomFactor: String): Unit {

       this.textView.get()?.text = "ZOOM: ${zoomFactor}"
    }

    /*
    * Changes the aperture of the camera
    * @param {String} apertureFactor The new aperture to update to.
    * */
    override fun setAperture(apertureFactor: String): Unit {

        this.textView.get()?.text = "APERTURE: ${apertureFactor}"
    }

    /*
    * Changes the focus type of the camera
    * @param {String} focusType The new focus type to update to
    * */
    override fun setFocusType(focusType: String): Unit {

        this.textView.get()?.text = "FOCUS_TYPE: ${focusType}"
    }

}
