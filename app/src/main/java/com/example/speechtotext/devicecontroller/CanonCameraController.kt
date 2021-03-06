package com.example.speechtotext.devicecontroller

import com.example.speechtotext.*

import android.graphics.BitmapFactory
import android.util.Log
import kotlinx.android.synthetic.main.activity_main.*
import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.io.IOException
import android.app.*
import android.content.Context
import android.graphics.Bitmap
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.PUT
import java.io.Serializable
import java.lang.ref.WeakReference

/*
* TODO:
* - Refactor data classes to have general data classes for some requests
* - Create function to reduce repetition of code
* - Add to the feautureURL map
* - Update all functions to represent a call to action
*   - EXAMPLE: fun shoot() -> fun shootPhoto() and fun zoom() -> fun updateZoom()
* - Test all implemented controls
* - All responses of each HTTP call must be parsed to render the appropriate UI view and to handle errors efficiently
* - Refactor getURl(), getIP(), setIP() methods to a HTTPDevice object?
* - Create function for HTTP requests to reduce repetition of code
* - Add error handling so as to prevent crash from failing, this includes the uses of Toasts
* - Change all methods to accept strings only, to accomodate the STTE
* - Implement "mode" to toggle between movie and photo
* */


object CanonCameraController: Device, Camera, Serializable {

    val featuresAvailable = arrayListOf<Feature>(Feature.SHOOT, Feature.MODE, Feature.ZOOM, Feature.FOCUS, Feature.APERTURE)
    private val deviceName = DeviceName.CANON
    private val connectionType = ConnectionType.HTTP
    lateinit var context: WeakReference<Context>

    val httpNetworkManager = null // API Interface
    val featureURL = mapOf<Feature, String>(

        Feature.SHOOT to "ccapi/ver100/shooting/control/shutterbutton/",
        Feature.ZOOM to "ccapi/ver100/shooting/control/zoom/",
        Feature.LIVEVIEW_FLIP to "/ccapi/ver100/shooting/liveview/flip/",
        Feature.APERTURE to ""

    )
    private var ip = "192.168.0.107";
    var BASE_URL = "http://" + this.ip + ":8080/"

    val okClient by lazy {
        OkHttpClient()
    }

    val okRequest by lazy {
        Request.Builder()
            .url("http://${ip}:8080/ccapi/ver100/shooting/liveview/flip/")
            .build()
    }


    override fun setApplicationContext(context: Context) {
        this.context = WeakReference(context)
    }

    override fun setIp(ip: String) {
        this.ip = ip
    }

    override fun getDeviceName(): DeviceName {
        return this.deviceName
    }

    override fun connectDevice() {

    }

    override fun getConnectionType(): ConnectionType {
        return this.connectionType
    }


    override fun isFeatureAvailable(cameraFeature: Feature): Boolean {


        if ( this.featuresAvailable.contains(cameraFeature) ) {
            return true;
        }

        return false;
    }

    override fun getAvailableFeatures(): ArrayList<Feature> {

        return this.featuresAvailable
    }

    /*
    * Gets root url of the API service
    */
    fun getBaseURL(): String { return this.BASE_URL }

    /*
    * Gets URL of the specified service."Private" method
    * @param {Feature} The feature who's URL is needed.
    */
    private fun getURL(feature: Feature): String {

        return this.BASE_URL + this.featureURL[feature]
    }

    /*
    * Sets the IP address of the camera
    * @param {String} ipAddress The IP address of the camera
    */
    fun setIP(ipAddress: String) {

        this.ip = ipAddress
    }

    /*
    * Configures liveview so that camera can connect to imageView.
    *
    * */
    override fun configureLiveview(callback: (camera: Camera) -> Unit) {
        val retroFitBuilder = Retrofit.Builder()
            .baseUrl(this.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .addConverterFactory(ScalarsConverterFactory.create())
            .build()
            .create(CanonCameraApiInterface :: class.java)

        val data = retroFitBuilder.configureLiveview(LiveView("medium", "on"))

        data.enqueue(object : Callback<Empty?> {
            override fun onResponse(call: Call<Empty?>, response: Response<Empty?>) {

                Log.d("RESPONSE", response.body().toString())

                callback(this@CanonCameraController)

            }

            override fun onFailure(call: Call<Empty?>, t: Throwable) {
                Log.d("RESPONSE_ERROR", t.message?:"Error: No Error message")
            }
        })
    }

    /*
    * Gets the static live view image as a bitmap
    * Uses okHTTP client.
    * @param {} Callback function to run once image has been received from camera.
    * */
    override fun getLiveviewFlip(callback: (bitmap: Bitmap?) -> Unit) {


        this.okClient.newCall(this.okRequest).enqueue(object : okhttp3.Callback {

            override fun onFailure(call: okhttp3.Call, e: IOException) {
                Log.d("RESPONSE_ERROR", e.message ?: "Error: No Error message")

            }

            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {


                val inputStream = response?.body?.byteStream()

                val bitmap = BitmapFactory.decodeStream(inputStream)

                // Supply bitmap to callback function
                callback(bitmap) // Correct syntax for calling callback that requires parameter

                Log.d("RESPONSE", response.body.toString())

            }
        })
    }

    /*
    * Sends HTTP shoot request to camera to grab a picture.
    * Uses retrofit.
    * */
    override fun shoot(): Unit {

        val retroFitBuilder = Retrofit.Builder()
            .baseUrl(this.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .addConverterFactory(ScalarsConverterFactory.create())
            .build()
            .create(CanonCameraApiInterface :: class.java)

        val data = retroFitBuilder.shoot(Focus(af = true))

        data.enqueue(object : Callback<Empty?> {
            override fun onResponse(call: Call<Empty?>, response: Response<Empty?>) {

                Log.d("RESPONSE", response.body().toString())

            }

            override fun onFailure(call: Call<Empty?>, t: Throwable) {
                Log.d("RESPONSE_ERROR", t.message?:"Error: No Error message")
            }
        })

    }


    /*
    * Zooms in as per the zoom factor provided
    * @param {String} zoomFactor The integer amount to zoom in by.
    * */
    override fun setZoom(zoomFactor: String): Unit {

        val retroFitBuilder = Retrofit.Builder()
            .baseUrl(this.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .addConverterFactory(ScalarsConverterFactory.create())
            .build()
            .create(CanonCameraApiInterface :: class.java)

        val data = retroFitBuilder.zoom(Zoom(value = zoomFactor.toInt()))

        data.enqueue(object : Callback<Zoom?> {
            override fun onResponse(call: Call<Zoom?>, response: Response<Zoom?>) {

                Log.d("RESPONSE", response.body().toString())

            }

            override fun onFailure(call: Call<Zoom?>, t: Throwable) {
                Log.d("RESPONSE_ERROR", t.message?:"Error: No Error message")
            }
        })
    }

    /*
    * Changes the aperture of the camera
    * @param {String} apertureFactor The new aperture to update to.
    * */
    override fun setAperture(apertureFactor: String): Unit {

        val apertureValue = "f" + apertureFactor.toFloat().toString()

        val retroFitBuilder = Retrofit.Builder()
            .baseUrl(this.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .addConverterFactory(ScalarsConverterFactory.create())
            .build()
            .create(CanonCameraApiInterface :: class.java)

        val data = retroFitBuilder.aperture(Aperture(value = apertureValue))

        data.enqueue(object : Callback<Aperture?> {
            override fun onResponse(call: Call<Aperture?>, response: Response<Aperture?>) {

                Log.d("RESPONSE", response.body().toString())

            }

            override fun onFailure(call: Call<Aperture?>, t: Throwable) {
                Log.d("RESPONSE_ERROR", t.message?:"Error: No Error message")
            }
        })

    }

    /*
    * Changes the focus type of the camera
    * @param {String} focusType The new focus type to update to
    * */
    override fun setFocusType(focusType: String): Unit {

        val focusTypes = mapOf<String, String>(
            "face" to "face+tracking",
            "spot" to "spot",
            "point" to "1point"
        )

        val retroFitBuilder = Retrofit.Builder()
            .baseUrl(this.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .addConverterFactory(ScalarsConverterFactory.create())
            .build()
            .create(CanonCameraApiInterface :: class.java)

        val data = retroFitBuilder.focusType(FocusType(value = focusTypes[focusType]!!))

        data.enqueue(object : Callback<FocusType?> {
            override fun onResponse(call: Call<FocusType?>, response: Response<FocusType?>) {

                Log.d("RESPONSE", response.body().toString())

            }

            override fun onFailure(call: Call<FocusType?>, t: Throwable) {
                Log.d("RESPONSE_ERROR", t.message?:"Error: No Error message")
            }
        })

    }



}

interface CanonCameraApiInterface {

    @POST("ccapi/ver100/shooting/control/shutterbutton")
    fun shoot(@Body body: Focus): Call<Empty>

    @POST("ccapi/ver100/shooting/control/zoom")
    fun zoom(@Body body: Zoom): Call<Zoom>

    @POST("ccapi/ver100/shooting/liveview")
    fun configureLiveview(@Body body: LiveView): Call<Empty>

    @PUT("ccapi/ver100/shooting/settings/av")
    fun aperture(@Body body: Aperture): Call<Aperture>

    @PUT("ccapi/ver100/shooting/settings/afmethod")
    fun focusType(@Body body: FocusType): Call<FocusType>
}

// Use @SerializedName("liveviewsize") and camelcase the variable
data class LiveView(
    val liveviewsize: String,
    val cameradisplay: String
)

data class Focus(

    val af: Boolean
)

data class FocusType(

    val value: String
)

data class Zoom(

    val value: Int
)

data class Aperture(

    val value: String
)

data class Empty(
    val body: String
)
