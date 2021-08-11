package com.example.speechtotext.devicecontroller

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.speechtotext.*
import java.lang.Integer.min
import kotlin.math.absoluteValue

object MasterCamera {

    val factory = MasterControllerFactory()
    var chosenCamera = DeviceName.CANON

    fun connectDevice(mainActivity: MainActivity) {

        (factory.controllers.get(DeviceType.CAMERA)?.get(chosenCamera) as Device).connectDevice(mainActivity)
    }

    fun shoot(word: Word) {

        factory.getCameraInstance(chosenCamera).shoot()
    }

    fun setZoom(word: Word) {

        // Change parameter for setZoom to a string
        factory.getCameraInstance(chosenCamera).setZoom(word.value)
    }

    fun setAperture(word: Word) {

        // Change parameter for setAperture to a float
        factory.getCameraInstance(chosenCamera).setAperture(word.value)
    }

    fun setFocusType(word: Word) {

        factory.getCameraInstance(chosenCamera).setFocusType(word.value)
    }

    fun setMode(word: Word) {}
}

object MasterGimbal {

    val factory = MasterControllerFactory()
    var chosenGimbal = DeviceName.DJI_RS_2

    var isAbsolute = true // If incremental is set, then it changes to false

    val RANGE = IntRange(0, 10)

    enum class Axis {
        YAW,
        PITCH,
        ROLL,
        UNDEFINED
    }

    val coordinates = mutableMapOf(
        Axis.ROLL to 0,
        Axis.PITCH to 0,
        Axis.YAW to 0
    )

    fun connectDevice(mainActivity: MainActivity) {

        (MasterCamera.factory.controllers.get(DeviceType.GIMBAL)?.get(chosenGimbal) as Device).connectDevice(mainActivity)
    }

    /*
    * Processes the given word and maps it to the appropriate axis.
    * @param {Word} word A word object that contains all the attributes we are interested in.
    * */
    fun move(word: Word) {

        val vectorValue = parseVector(word)
        val axis = parseAxis(word)

        coordinates[axis] = vectorValue

        // REFACTOR_CRITICAL: For now, we always assume it's ABSOLUTE, until I figure it out
//        if(isAbsolute) {
//
//        }else{
//
//        }

        factory.getGimbalInstance(chosenGimbal).move(coordinates, isAbsolute)
    }

    /*
    * Parses the word object and returns the directional value for the given feature.
    * @param word A word object containing the feature attribute that helps in parsing its value.
    * @return The directional value for the given word.
    * */
    fun parseDirection(word: Word): Int {

        return when(word.feature) {

            Feature.UP, Feature.RIGHT, Feature.ROLL, Feature.ROLL_RIGHT -> 1
            Feature.DOWN, Feature.LEFT, Feature.ROLL_LEFT -> -1
            else -> 0
        }
    }

    fun parseAxis(word: Word): Axis {

        return when(word.feature) {

            Feature.RIGHT, Feature.LEFT -> Axis.YAW
            Feature.UP, Feature.DOWN -> Axis.PITCH
            Feature.ROLL, Feature.ROLL_LEFT, Feature.ROLL_RIGHT -> Axis.ROLL
            else -> Axis.UNDEFINED
        }
    }

    /*
    * 1. Calculates the given vector value
    * 2. Checks if it is within its range of values for the given direction.
    * 3. Sets the new value for the appropriate axis.
    * @param {Word} word The Word object that contains the feature attribute and the value.
    * @return The vector value of the given word.
    * */
    @RequiresApi(Build.VERSION_CODES.N)
    fun parseVector(word: Word): Int {

        val direction = parseDirection(word)

        // REFACTOR_CRITICAL: Run time exception for when the word cannot be parsed to an Integer.
        val magnitude = word.value.toInt()

        val range = when(word.feature) {

            Feature.UP, Feature.DOWN, Feature.ROLL_RIGHT, Feature.ROLL_LEFT, Feature.ROLL -> 90
            Feature.LEFT, Feature.RIGHT -> 180
            else -> 0
        }

        return min(magnitude.absoluteValue, range)*direction
    }


}