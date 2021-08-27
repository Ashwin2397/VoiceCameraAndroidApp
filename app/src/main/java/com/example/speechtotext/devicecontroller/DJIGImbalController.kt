package com.example.speechtotext.devicecontroller

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.example.speechtotext.*
import com.google.gson.annotations.SerializedName
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import java.io.Serializable
import java.lang.Exception
import java.lang.ref.WeakReference
import kotlin.math.roundToInt

object DJIGimbalController: Device, Gimbal, Serializable {

    private val deviceName = DeviceName.DJI_RS_2
    private val connectionType = ConnectionType.HTTP
    private val featuresAvailable = arrayListOf<Feature>(Feature.INCREMENTAL_MOVEMENT, Feature.HOME, Feature.PORTRAIT, Feature.LANDSCAPE)
    lateinit var context: WeakReference<Context>

    val featureURL = mapOf<Feature, String>(

        Feature.ABSOLUTE_MOVEMENT to "",
        Feature.INCREMENTAL_MOVEMENT to "",

    )
    private var ip = "192.168.0.106";
    var BASE_URL = "http://${ip}:8080/"
    val RANGE:Float = 10F

    override fun setApplicationContext(context: Context) {
        this.context = WeakReference(context)
    }

    override fun setIp(ip: String) {
        this.ip = ip
        this.BASE_URL = "http://${ip}:8080/"
    }
    override fun getDeviceName(): DeviceName {
        return this.deviceName
    }

    override fun connectDevice(mainActivity: MainActivity) {
    }

    override fun getConnectionType(): ConnectionType {
        return this.connectionType
    }

    override fun move(coordinates: Map<MasterGimbal.Axis, Int>, isAbsolute: Boolean) {

        try {
            val url = this.BASE_URL
            val retroFitBuilder = Retrofit.Builder()
                .baseUrl(this.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .addConverterFactory(ScalarsConverterFactory.create())
                .build()
                .create(DJIGimbalAPIInterface :: class.java)

            val data = retroFitBuilder.moveGimbal(MoveGimbal(
                yaw = convert(coordinates[MasterGimbal.Axis.YAW]!!),
                pitch = convert(coordinates[MasterGimbal.Axis.PITCH]!!),
                roll = convert(coordinates[MasterGimbal.Axis.ROLL]!!),
                isAbsolute = when (isAbsolute) {
                    true -> "0"
                    else -> "1"
                },
                timeForAction = "10"
            ))

            data.enqueue(object: Callback<Empty?> {
                override fun onResponse(call: Call<Empty?>, response: Response<Empty?>) {

                    Log.d("RESPONSE", response.body().toString())

                }

                override fun onFailure(call: Call<Empty?>, t: Throwable) {
                    Log.d("RESPONSE_ERROR", t.message?:"Error: No Error message")
                }
            })

        }catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this.context.get(), "DJI Gimbal not connected", Toast.LENGTH_SHORT).show()
        }

    }

    //REFACTOR_CRITICAL: I don't know if this works with the new range that I defined...
    private fun convert(angle: Int): String {

        return ((angle.toFloat())*10).roundToInt().toString()
    }

    override fun isFeatureAvailable(gimbalFeature: Feature): Boolean {

        if ( this.featuresAvailable.contains(gimbalFeature) ) {
            return true;
        }

        return false;
    }

    override fun getAvailableFeatures(): ArrayList<Feature> {

        return this.featuresAvailable
    }

    /*
    * Gets URL of the specified service."Private" method
    * @param {Feature} The feature who's URL is needed.
    */
    private fun getURL(feature: Feature): String {

        return this.BASE_URL + this.featureURL[feature]
    }

}

interface DJIGimbalAPIInterface {

    @POST("/")
    fun moveGimbal(@Body body: MoveGimbal): Call<Empty>

}

data class MoveGimbal(
    @SerializedName("yaw_angle")
    val yaw: String,
    @SerializedName("pitch_angle")
    val pitch: String,
    @SerializedName("roll_angle")
    val roll: String,
    @SerializedName("time_for_action")
    var timeForAction: String,
    @SerializedName("ctrl")
    val isAbsolute: String
)
