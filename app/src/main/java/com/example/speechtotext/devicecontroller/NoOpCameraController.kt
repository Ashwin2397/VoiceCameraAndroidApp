package com.example.speechtotext.devicecontroller

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import android.widget.TextView
import com.example.speechtotext.ConnectionType
import com.example.speechtotext.DeviceName
import com.example.speechtotext.Feature
import com.example.speechtotext.Word
import okhttp3.OkHttpClient
import okhttp3.Request
import org.w3c.dom.Text
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.PUT
import java.io.IOException
import java.io.Serializable
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
