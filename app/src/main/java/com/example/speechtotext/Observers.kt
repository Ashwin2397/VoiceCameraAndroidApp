package com.example.speechtotext

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.renderscript.ScriptGroup
import android.util.Log
import com.example.speechtotext.devicecontroller.MasterCamera
import com.example.speechtotext.devicecontroller.MasterGimbal
import java.util.*
import kotlin.concurrent.timerTask

/*

class FSMObserver(
    val uiController: UIController,
    val command: Word,
    val consecutiveWords: Map<String, Word>,
    ){

    lateinit var states: Map<Int, State> // Init later

    var currentState = 0
    var currentWord = Word("", Feature.UNDEFINED, InputType.UNDEFINED, DeviceType.UNDEFINED)

    fun Word(word: String) {


        val parsedWord = this.parseWord(word)
        this.currentWord = parsedWord // Save word to be used by cb

        val currentStateObject = this.states[this.currentState]

        val newState = currentStateObject!!.Word(parsedWord.inputType)

        changeState(newState)

    }

    fun parseWord(word: String): Word {

        var parsedWord = Word("", Feature.UNDEFINED, InputType.UNDEFINED, DeviceType.UNDEFINED);

        val isDefinedCommand = firstCommands[word] != null
        val isOurCommand = isDefinedCommand && firstCommands[word]?.value == this.command.value // IF list, then check if it is in list
        val isConsecutiveWord = this.consecutiveWords[word] != null
        val isOtherCommand = isDefinedCommand && firstCommands[word]?.value != this.command.value; // If list, then check if not in list
//        val isNumericalParameter = numericalParameters[word] != null
        var isNumericalParameter = false

        try {
            word.toFloat()
            isNumericalParameter = true
        }catch (e: NumberFormatException) {
            isNumericalParameter = false
        }

        if (isOurCommand) {
            parsedWord = firstCommands[word]!!
        }else if (isConsecutiveWord) {
            parsedWord = this.consecutiveWords[word]!!
        }else if (isOtherCommand) {
            parsedWord = Word("", Feature.ANY, InputType.OTHER_COMMAND, DeviceType.ANY)
        }else if (isNumericalParameter) {
//            parsedWord = numericalParameters[word]!!
            parsedWord = Word(word, command.feature, InputType.NUMERICAL_PARAMETER, command.deviceType)
        }

        return parsedWord
    }

    fun changeState(newState: Int) {

        val newStateObject = this.states[newState]

        val hasStateChanged = newState != this.currentState
        if (hasStateChanged) {

            this.currentState = newState;
            newStateObject!!.runCallbacks(this.currentWord)
        }
    }

    fun reset(optionalWord: Word) {

//        timer.schedule(TimerTask {}, 100)
        this.currentState = 0

        // Does'nt work unless you run on ui thread, which I have not figured out yet
        val mainLooper = Looper.getMainLooper()

        if (this.currentWord.inputType == InputType.OTHER_COMMAND) {
            uiController!!.reset() // If it is list, then reset all the buttons in uiController
        }else {

            Thread(Runnable {

                Timer("Reset UI", false).schedule(1000) {

                    Handler(mainLooper).post {

                        uiController!!.reset()
                    }
                }
            }).start()
        }
    }
}
*/

class DynamicObserver(
    val uiController: UIController,
    val masterCamera: MasterCamera,
    val masterGimbal: MasterGimbal,
    val applicationContext: Context
){
    private val TAG = "DYNAMIC_OBSERVER"

    val states = mapOf<Int, State>(
        0 to State(
            state = 0,
            input = mapOf<InputType, Int>(
                InputType.COMMAND to 1,
                InputType.PARAMETER to -1 // NO_OP
            ),
            callbacks = listOf<(optionalWord: Word) -> Unit>(uiController::resetCommand)
        ),
        1 to State(
            state = 1,
            input = mapOf<InputType, Int>(
                InputType.COMMAND to 1,
                InputType.PARAMETER_TRUE to 2,
                InputType.PARAMETER_FALSE to -1
            ),
            callbacks = listOf<(optionalWord: Word) -> Unit>(uiController::resetCommand, uiController::selectCommand, this::showParameters)
        ),
        2 to State(
            state = 2,
            input = mapOf<InputType, Int>(
                InputType.COMMAND to 2,
                InputType.PARAMETER to 2
            ),
            callbacks = listOf<(optionalWord: Word) -> Unit>(uiController::selectParameter, this::scheduleReset, this::sendCommand)
        ),
    )

    var currentState = 0
    var currentWord = Word("", Feature.UNDEFINED, Header.UNDEFINED, InputType.UNDEFINED, DeviceType.ANY, listOf())
    var currentCommand = Word("", Feature.UNDEFINED, Header.UNDEFINED, InputType.UNDEFINED, DeviceType.ANY, listOf())

    fun newWord(word: String) {

        var parsedWord = parseWord(word)
        this.currentWord = parsedWord // Save word to be used by cb
        if (parsedWord.inputType == InputType.COMMAND) {
            currentCommand = parsedWord
        }

        val currentStateObject = this.states[this.currentState]

        var inputType = parsedWord.inputType

        if (currentState == 1 && inputType == InputType.PARAMETER) {
            inputType = when(uiController.isParameterInBounds(parsedWord)) {
                true -> InputType.PARAMETER_TRUE
                false -> InputType.PARAMETER_FALSE
            }
        }
        val newState = currentStateObject!!.Word(inputType)

        changeState(newState)
    }

    fun changeState(newState: Int) {

        val isNoOp = newState == -1
        if (!isNoOp) {

            val newStateObject = this.states[newState]

            this.currentState = newState
            Log.d(TAG, "NEW STATE: ${currentState }")

            newStateObject!!.runCallbacks(this.currentWord)

        }
    }

    private fun parseWord(word: String): Word {

        var parsedWord = Word("", Feature.UNDEFINED, Header.UNDEFINED, InputType.UNDEFINED, DeviceType.ANY, listOf())
        val mostLikelyWord = similarWords[word] ?: word // Pass to words map, have yet to add these words

        val isDefinedWord = words[mostLikelyWord] != null
        var isNumericalParameter = false
        try {
            mostLikelyWord.toFloat()
            isNumericalParameter = true
        }catch (e: NumberFormatException) {
            isNumericalParameter = false
        }

        if (isDefinedWord && !isNumericalParameter) {
            parsedWord = words[mostLikelyWord]!!
        }else if (isNumericalParameter) {
            parsedWord = Word(word, currentCommand.feature, Header.UNDEFINED, InputType.PARAMETER, currentCommand.deviceType, listOf())
        }
        return parsedWord
    }

    fun sendCommand(selectedParameter: Word) {

        when(selectedParameter.deviceType) {
            DeviceType.GIMBAL -> {
                masterGimbal.sendCommand(currentWord, selectedParameter)
            }
            DeviceType.CAMERA -> {
                masterCamera.sendCommand(currentWord, selectedParameter)
            }
        }
    }

    /*
    * Schedules a reset after a fixed time period.
    * @param {Word} selectedParameter
    * */
    fun scheduleReset(Word: Word) {

        val mainLooper = Looper.getMainLooper()

        Thread(Runnable {

            Timer("Reset UI", false).schedule(timerTask {
                Handler(mainLooper).post {

                    changeState(0)
                }
            }, 1000)
        }).start()
    }

    fun showParameters(selectedCommand: Word) {

        when(uiController.hasParameters(selectedCommand)) {
            true -> uiController.showParameters(selectedCommand)
            false -> { changeState(2) }
        }
    }
}
