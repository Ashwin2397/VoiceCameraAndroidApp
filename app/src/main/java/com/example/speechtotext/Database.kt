package com.example.speechtotext

import android.app.Activity
import android.content.Context

/*
* NOTE:
* - This is persistent but it uses the native shared preferences to achieve this
* - It might not be necessary to create an SQLite DB, unless there are pictures that we might want to handle?
* */
class Database(
    val activity: Activity
){

    val sharedPreferences by lazy {
        activity
            .getSharedPreferences("controls", Context.MODE_PRIVATE)
    }

    val editor by lazy {
       sharedPreferences
           .edit()
    }

    fun saveControls(chosenControls: List<Feature>) {

        val chosenControlsSet = mutableSetOf<String>()

        chosenControls.forEach {
            chosenControlsSet.add(it.toString())
        }

        editor.apply {

            putStringSet("chosenControls", chosenControlsSet)
            apply()
        }
    }

    fun saveDevices(chosenCamera: DeviceDetails, chosenGimbal: DeviceDetails) {

        editor.apply {

            putString("chosenCamera", chosenCamera.deviceName.toString())
            putString("chosenCameraIp", chosenCamera.ipAddress)

            putString("chosenGimbal", chosenGimbal.deviceName.toString())
            putString("chosenGimbalIp", chosenGimbal.ipAddress)

            apply()
        }
    }

    fun getControls(): MutableList<Feature> {

        val features = mutableListOf<Feature>()
        sharedPreferences.getStringSet("chosenControls", mutableSetOf<String>())!!.forEach {

            features.add(Feature.valueOf(it))
        }

        return features
    }

    fun getCamera(): DeviceDetails {

        return DeviceDetails(
            DeviceName.valueOf(
            sharedPreferences.getString("chosenCamera", DeviceName.CANON.toString())
                ?: DeviceName.CANON.toString()
            ),
            sharedPreferences.getString("chosenCameraIp", "")
                ?: ""
        )
    }

    fun getGimbal(): DeviceDetails {

        return DeviceDetails(
            DeviceName.valueOf(
                sharedPreferences.getString("chosenGimbal", DeviceName.DJI_RS_2.toString())
                    ?: DeviceName.CANON.toString()
            ),
            sharedPreferences.getString("chosenGimbalIp", "")
                ?: ""
        )
    }

}

data class DeviceDetails(
    val deviceName: DeviceName,
    val ipAddress: String
)