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

        val axis = parseAxis(word)
        val multiplier = parseMultiplier(axis, word) // This assumes that all numerical parameters are within the range [1,10]
        val vectorValue = parseVector(word, multiplier)

        coordinates[axis] = parseIsAbsolute(axis, vectorValue, word)

        factory.getGimbalInstance(chosenGimbal).move(coordinates, true)
    }

    fun parseMultiplier(axis: MasterGimbal.Axis, word: Word): Float {

        return when(axis) {

            Axis.PITCH, Axis.ROLL -> (1F.div(10)).times(90)
            Axis.YAW -> (1F.div(10)).times(180)
            else -> 0F
        }
    }

    fun parseIsAbsolute(axis: Axis, vectorValue: Int, word: Word): Int {

        var coordinate = vectorValue

        val isIncremental = !isAbsolute
        if(isIncremental) {

            coordinate = vectorValue + coordinates[axis]!!
            val boundary = parseBoundary(word)

            val hasExceedRange = coordinate.absoluteValue > boundary
            if (hasExceedRange) {

                coordinate = when(word.feature) {

                    Feature.RIGHT -> coordinate - 360
                    Feature.LEFT -> coordinate + 360
                    Feature.ROLL_LEFT, Feature.DOWN -> -90
                    Feature.ROLL_RIGHT, Feature.UP -> 90
                    else -> 0
                }
            }
        }

        return coordinate
    }


    /*
    * 1. Calculates the given vector value
    * 2. Checks if it is within its range of values for the given direction.
    * 3. Sets the new value for the appropriate axis.
    * @param {Word} word The Word object that contains the feature attribute and the value.
    * @return The vector value of the given word.
    * */
    @RequiresApi(Build.VERSION_CODES.N)
    fun parseVector(word: Word, multiplier: Float): Int {

        val direction = parseDirection(word)

        // REFACTOR_CRITICAL: Run time exception for when the word cannot be parsed to an Integer.
        val magnitude = word.value.toInt()

        val boundary = parseBoundary(word)

        return min((magnitude.absoluteValue).times(multiplier).toInt(), boundary)*direction
    }

    fun parseBoundary(word: Word): Int {

        return when(word.feature) {

            Feature.UP, Feature.DOWN, Feature.ROLL_RIGHT, Feature.ROLL_LEFT, Feature.ROLL -> 90
            Feature.LEFT, Feature.RIGHT -> 180
            else -> 0
        }
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

}
