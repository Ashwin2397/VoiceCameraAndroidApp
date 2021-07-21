package com.example.speechtotext.devicecontroller

import android.util.Log
import com.example.speechtotext.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.PUT
import java.io.Serializable

class DJIGimbalController: Device, Gimbal, Serializable {

    private val deviceName = DeviceName.DJI_RS_2
    private val connectionType = ConnectionType.HTTP
    private val featuresAvailable = arrayListOf<Feature>(Feature.LEFT, Feature.RIGHT, Feature.UP, Feature.DOWN, Feature.ROLL)

    val featureURL = mapOf<Feature, String>(

        Feature.ABSOLUTE_MOVEMENT to "",
        Feature.INCREMENTAL_MOVEMENT to "",

    )
    private var ip = "";
    var BASE_URL = "http://" + this.ip + ":8080/"

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

    override fun isFeatureAvailable(gimbalFeature: Feature): Boolean {

        if ( this.featuresAvailable.contains(gimbalFeature) ) {
            return true;
        }

        return false;
    }

    override fun getAvailableFeatures(): ArrayList<Feature> {

        return this.featuresAvailable
    }

    override fun move(yaw: Float, pitch: Float, roll: Float, isIncremental: Int) {

        val retroFitBuilder = Retrofit.Builder()
            .baseUrl(this.BASE_URL + this.featureURL[Feature.MOVE])
            .addConverterFactory(GsonConverterFactory.create())
            .addConverterFactory(ScalarsConverterFactory.create())
            .build()
            .create(DJIGimbalAPIInterface :: class.java)

        val data = retroFitBuilder.moveGimbal(MoveGimbal(yaw, pitch, roll, isIncremental))

        data.enqueue(object: Callback<Empty?> {
            override fun onResponse(call: Call<Empty?>, response: Response<Empty?>) {

                Log.d("RESPONSE", response.body().toString())

            }

            override fun onFailure(call: Call<Empty?>, t: Throwable) {
                Log.d("RESPONSE_ERROR", t.message?:"Error: No Error message")
            }
        })

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

    @POST("/move/absolute")
    fun moveGimbal(@Body body: MoveGimbal): Call<Empty>

}

data class MoveGimbal(
    val yaw: Float,
    val pitch: Float,
    val roll: Float,
    val ctrl: Int
)
