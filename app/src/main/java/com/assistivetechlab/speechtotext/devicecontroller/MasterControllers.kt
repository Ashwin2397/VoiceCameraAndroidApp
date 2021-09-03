package com.assistivetechlab.speechtotext.devicecontroller

import android.os.Build
import androidx.annotation.RequiresApi
import com.assistivetechlab.speechtotext.*
import java.lang.Integer.min
import kotlin.math.absoluteValue

object MasterCamera {


    var featureToFun = mapOf<Feature, (word: Word) -> Unit>(

        Feature.ZOOM to this::setZoom,
        Feature.FOCUS to this::setFocusType,
        Feature.MODE to this::setMode,
        Feature.APERTURE to this::setAperture,
        Feature.SHOOT to this::shoot
    )

    var featuresToParameters = mapOf(
        Feature.ZOOM to ParameterDetails(AdaptiveParameterBarType.BUTTON, stringParameters = listOf("0", "25", "50", "75", "100"), currentStringSelection =  "0"),
        Feature.MODE to ParameterDetails(AdaptiveParameterBarType.BUTTON, stringParameters = listOf("movie", "photo"), currentStringSelection =  "photo"),
        Feature.FOCUS to ParameterDetails(AdaptiveParameterBarType.BUTTON, stringParameters = listOf("point", "face", "spot"), currentStringSelection = "point"),
        Feature.APERTURE to ParameterDetails(AdaptiveParameterBarType.BUTTON, stringParameters = listOf("3.4", "4.0", "4.5", "5.0", "5.6", "6.3", "7.1", "8.0"), currentStringSelection = "3.4"),
    )

    val factory = MasterControllerFactory()
    var chosenCamera = DeviceName.CANON

    fun sendCommand(command: Word, parameter: Word) {

        val cb = featureToFun.get(command.feature)

        if (cb != null) {
            cb(Word(parameter.value, parameter.feature, parameter.header, parameter.inputType, parameter.deviceType, listOf()))
        }
    }

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

    var featureToFun = mapOf<Feature, (word: Word) -> Unit>(

        Feature.MOVE to this::move,
        Feature.PORTRAIT to this::portrait,
        Feature.LANDSCAPE to this::landscape,
        Feature.HOME to this::home,
        Feature.LEFT to this::move,
        Feature.RIGHT to this::move,
        Feature.UP to this::move,
        Feature.DOWN to this::move,
        Feature.ROLL to this::move
    )

    var featuresToParameters = mapOf(
        Feature.LEFT to ParameterDetails(AdaptiveParameterBarType.BUTTON, stringParameters = listOf("0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10"), currentStringSelection = "0"),
        Feature.RIGHT to ParameterDetails(AdaptiveParameterBarType.BUTTON, stringParameters = listOf("0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10"), currentStringSelection = "0"),
        Feature.UP to ParameterDetails(AdaptiveParameterBarType.BUTTON, stringParameters = listOf("0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10"), currentStringSelection = "0"),
        Feature.DOWN to ParameterDetails(AdaptiveParameterBarType.BUTTON, stringParameters = listOf("0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10"), currentStringSelection = "0"),
        Feature.ROLL to ParameterDetails(AdaptiveParameterBarType.BUTTON, stringParameters = listOf("0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10"), currentStringSelection = "0"),

        Feature.MOVE to ParameterDetails(AdaptiveParameterBarType.GAUGE, numericalParameters = IntRange(0, 10), currentNumericalSelection = 0F),
    )

    val factory = MasterControllerFactory()
    var chosenGimbal = DeviceName.DJI_RS_2

    var isAbsolute = false // If incremental is set, then it changes to false

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

    fun sendCommand(command: Word, parameter: Word) {

        val cb = featureToFun.get(command.feature)

        if (cb != null) {
            cb(Word(parameter.value, parameter.feature, parameter.header, parameter.inputType, parameter.deviceType, listOf()))
        }
    }

    fun connectDevice(mainActivity: MainActivity) {

        (MasterCamera.factory.controllers.get(DeviceType.GIMBAL)?.get(chosenGimbal) as Device).connectDevice(mainActivity)
    }

    fun home(word: Word) {

        coordinates[Axis.ROLL] = 0
        coordinates[Axis.PITCH] = 0
        coordinates[Axis.YAW] = 0

        factory.getGimbalInstance(chosenGimbal).move(coordinates, isAbsolute)
    }

    fun portrait(word: Word) {

        coordinates[Axis.ROLL] = 90

        factory.getGimbalInstance(chosenGimbal).move(coordinates, isAbsolute)
    }

    fun landscape(word: Word) {

        coordinates[Axis.ROLL] = 0

        factory.getGimbalInstance(chosenGimbal).move(coordinates, isAbsolute)
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

        factory.getGimbalInstance(chosenGimbal).move(coordinates, isAbsolute)

        updateParameterDetails()
    }

    /*
    * Updates parameter details based on the current mode of the gimbal.
    * If incremental => Everything resets to zero
    * Else if absolute => Everything stays as is
    * NOTE: This should actually only happen upon successfully sending a command.
    * */
    private fun updateParameterDetails() {

        if (!isAbsolute){

            featuresToParameters.forEach {
                it.value.currentNumericalSelection = 0F
            }
        }
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
                    Feature.ROLL -> if (parseDirection(word) == -1) {
                        -90
                    }else {
                        90
                    }
                    Feature.ROLL_NEGATIVE, Feature.DOWN -> -90
                    Feature.ROLL_POSITIVE, Feature.UP -> 90
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
        val magnitude = word.value.toFloat()

        val boundary = parseBoundary(word)

        return min((magnitude.absoluteValue).times(multiplier).toInt(), boundary)*direction
    }

    fun parseBoundary(word: Word): Int {

        return when(word.feature) {

            Feature.UP, Feature.DOWN, Feature.ROLL_POSITIVE, Feature.ROLL_NEGATIVE, Feature.ROLL -> 90
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

            Feature.UP, Feature.RIGHT, Feature.ROLL_POSITIVE -> 1
            Feature.DOWN, Feature.LEFT, Feature.ROLL_NEGATIVE -> -1
            Feature.ROLL -> {
                if (word.value.toFloat() < 0) {
                    -1
                }else {
                    1
                }
            }
            else -> 0
        }
    }

    fun parseAxis(word: Word): Axis {

        return when(word.feature) {

            Feature.RIGHT, Feature.LEFT -> Axis.YAW
            Feature.UP, Feature.DOWN -> Axis.PITCH
            Feature.ROLL, Feature.ROLL_NEGATIVE, Feature.ROLL_POSITIVE -> Axis.ROLL
            else -> Axis.UNDEFINED
        }
    }

}
