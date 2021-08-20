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
                InputType.DEVICE to 1,
                InputType.HEADER to 1,
                InputType.COMMAND to 2,
                InputType.PARAMETER to -1 // NO_OP
            ),
            callbacks = listOf<(optionalWord: Word) -> Unit>(uiController::reset)
        ),
        1 to State(
            state = 1,
            input = mapOf<InputType, Int>(
                InputType.DEVICE to 1,
                InputType.HEADER to 1,
                InputType.COMMAND to 2,
                InputType.PARAMETER to 1
            ),
            callbacks = listOf<(optionalWord: Word) -> Unit>(uiController::reset, uiController::selectDevice, uiController::showParentHeaders, uiController::showChildHeaders)
        ),
        2 to State(
            state = 2,
            input = mapOf<InputType, Int>(
                InputType.DEVICE to 1,
                InputType.HEADER to 1,
                InputType.COMMAND to 2,
                InputType.PARAMETER_TRUE to 3,
                InputType.PARAMETER_FALSE to -1
            ),
            callbacks = listOf<(optionalWord: Word) -> Unit>(uiController::reset, uiController::selectDevice, uiController::selectCommand, uiController::showParentHeaders, this::showParameters )
        ),
        3 to State(
            state = 3,
            input = mapOf<InputType, Int>(
                InputType.DEVICE to 3,
                InputType.HEADER to 3,
                InputType.COMMAND to 3,
                InputType.PARAMETER to 3
            ),
            callbacks = listOf<(optionalWord: Word) -> Unit>(uiController::selectParameter, this::scheduleReset, this::sendCommand)
        ),
    )

    var currentState = 0
    var currentWord = Word("", Feature.UNDEFINED, Header.UNDEFINED, InputType.UNDEFINED, DeviceType.ANY, listOf())

    fun Word(word: String) {

        var parsedWord = parseWord(word)
        this.currentWord = parsedWord // Save word to be used by cb

        val currentStateObject = this.states[this.currentState]

        var inputType = parsedWord.inputType

        if (currentState == 2 && inputType == InputType.PARAMETER) {
            inputType = when(uiController.isParameterInBounds(parsedWord)) {
                true -> InputType.PARAMETER_TRUE
                false -> InputType.PARAMETER_FALSE
            }
        }
        val newState = currentStateObject!!.Word(inputType)

        changeState(newState)

/*
        // Reset UI:
        uiController.reset()
        uiController.selectDevice(Word)
        uiController.showChildHeaders(Word)


        when(Word.inputType) {

            InputType.DEVICE -> {

                // Select device button
                // Show all sub headers from header at top
                // Add all parents of header including itself to the header text view

                uiController.selectDevice(Word)
                uiController.showSubHeaders(headersToCommands.get(Word.header))
                headerTextView.addParents(Word)
            }
            InputType.HEADER -> {

                // Select device button
                // Show all sub headers from header at top
                // Add all parents of header including itself to the header text view

                uiController.selectHeader(Word)

//                val commands = headersToCommands.get(Word.header)
//                displayCommands(commands!!)


            }
            InputType.COMMAND -> {

                // Select device button
                // Show all sub headers in parent header
                // Add all parents of header including to the header text view

                // Select command
                // Get parameter bar to display parameters
                //

                uiController.selectCommand(Word)
                uiController.showParameters(Word)


                // if this command has a parameter, set selectedCommand to be this word object
                selectedCommand = Word
            }
            InputType.PARAMETER -> {

                //
                val isCommandSelected = selectedCommand != null
                if (isCommandSelected) {

                    Log.d(TAG, "Command: ${selectedCommand!!.value} Parameter: ${Word.value}")

                    // Check if given parameter from bar is within the "bounds" of the chosen command
                    // Select parameter from bar if parameter given is within the bounds
                    uiController.selectParameter(Word)

                    // Send command and parameter to a master controller, along with the device type
                    // Master controller would then fetch the controller to be used and give the information to achieve this
                    sendCommand(Word)
                }

            }
            else -> {
                // Ignore
            }
        }*/

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
        val mostLikelyWord = similarWords[word] // Pass to words map, have yet to add these words

        val isDefinedWord = words[word] != null
        var isNumericalParameter = false
        try {
            word.toFloat()
            isNumericalParameter = true
        }catch (e: NumberFormatException) {
            isNumericalParameter = false
        }

        if (isDefinedWord) {
            parsedWord = words[word]!!
        }else if (isNumericalParameter) {
            parsedWord = Word(word, currentWord.feature, Header.UNDEFINED, InputType.PARAMETER, currentWord.deviceType, listOf())
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
            false -> { changeState(3) }
        }
    }
}
