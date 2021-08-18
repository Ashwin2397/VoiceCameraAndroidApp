package com.example.speechtotext

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import com.example.speechtotext.devicecontroller.Device
import com.example.speechtotext.devicecontroller.MasterCamera
import com.example.speechtotext.devicecontroller.MasterGimbal
import java.util.Timer
import kotlin.concurrent.schedule

class FSMObserver(
    val uiController: UIController,
    val command: Word,
    val consecutiveWords: Map<String, Word>,
    ){

    lateinit var states: Map<Int, State> // Init later

    var currentState = 0
    var currentWord = Word("", Feature.UNDEFINED, InputType.UNDEFINED, DeviceType.UNDEFINED)

    fun newWord(word: String) {


        val parsedWord = this.parseWord(word)
        this.currentWord = parsedWord // Save word to be used by cb

        val currentStateObject = this.states[this.currentState]

        val newState = currentStateObject!!.newWord(parsedWord.inputType)

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

class DynamicObserver(
    val textView: TextView,
    val headerCreaterLayout: LinearLayout,
    val deviceButtons: Map<DeviceType, Button>,
    val masterCamera: MasterCamera,
    val masterGimbal: MasterGimbal,
    val applicationContext: Context
){
    private val TAG = "DYNAMIC_OBSERVER"

    var selectedCommand: NewWord? = null
    val uiController = NewUIController(headerCreaterLayout, deviceButtons, applicationContext, HeaderTextView(textView))

    fun newWord(word: String) {


        // Reset UI:
        uiController.reset()

        val newWord = parseWord(word)

        uiController.selectDevice(newWord.deviceType)
        uiController.showHeaders(headersToCommands.get(newWord.header), newWord)


        when(newWord.inputType) {

//            InputType.DEVICE -> {
//
//                // Select device button
//                // Show all sub headers from header at top
//                // Add all parents of header including itself to the header text view
//
//                uiController.selectDevice(newWord)
//                uiController.showSubHeaders(headersToCommands.get(newWord.header))
//                headerTextView.addParents(newWord)
//            }
//            InputType.HEADER -> {
//
//                // Select device button
//                // Show all sub headers from header at top
//                // Add all parents of header including itself to the header text view
//
//                uiController.selectHeader(newWord)
//
////                val commands = headersToCommands.get(newWord.header)
////                displayCommands(commands!!)
//
//
//            }
            InputType.COMMAND -> {

                // Select device button
                // Show all sub headers in parent header
                // Add all parents of header including to the header text view

                // Select command
                // Get parameter bar to display parameters
                //

                uiController.selectCommand(newWord)
                uiController.displayParameters(newWord)


                // if this command has a parameter, set selectedCommand to be this word object
                selectedCommand = newWord
            }
            InputType.PARAMETER -> {

                //
                val isCommandSelected = selectedCommand != null
                if (isCommandSelected) {

                    Log.d(TAG, "Command: ${selectedCommand!!.value} Parameter: ${newWord.value}")

                    // Check if given parameter from bar is within the "bounds" of the chosen command
                    // Select parameter from bar if parameter given is within the bounds
                    uiController.selectParameter(selectedCommand!!, newWord)

                    // Send command and parameter to a master controller, along with the device type
                    // Master controller would then fetch the controller to be used and give the information to achieve this
                    when(newWord.deviceType) {
                        DeviceType.GIMBAL -> {
                            masterGimbal.sendCommand(selectedCommand!!, newWord)
                        }
                        DeviceType.CAMERA -> {
                            masterCamera.sendCommand(selectedCommand!!, newWord)
                        }
                    }
                    selectedCommand = null
                }
                // else ignore

            }
            else -> {
                // Ignore
            }
        }

    }



    private fun parseWord(word: String): NewWord {

        var parsedWord = NewWord("", Feature.UNDEFINED, Header.UNDEFINED, InputType.UNDEFINED, DeviceType.ANY, listOf())

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
            parsedWord = NewWord(word, selectedCommand?.feature ?: Feature.UNDEFINED, Header.UNDEFINED, InputType.PARAMETER, selectedCommand?.deviceType ?: DeviceType.UNDEFINED, listOf())
        }
        return parsedWord
    }

    /*
    * Displays commands by adding buttons to linear layout.
    * Adds click listeners to all buttons that will send the value of it's text content to
    * */
    private fun displayCommands(commands: List<String>) {

    }
}
