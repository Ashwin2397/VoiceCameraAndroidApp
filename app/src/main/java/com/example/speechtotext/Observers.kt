package com.example.speechtotext

import android.os.Handler
import android.os.Looper
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
        val isOurCommand = isDefinedCommand && firstCommands[word]?.value == this.command.value
        val isConsecutiveWord = this.consecutiveWords[word] != null
        val isOtherCommand = isDefinedCommand && firstCommands[word]?.value != this.command.value;
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
            parsedWord = Word(word, Feature.MODE, InputType.NUMERICAL_PARAMETER, DeviceType.CAMERA)
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
            uiController!!.reset()
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

    fun onCommandClick() {

        newWord(this.command.value)
    }

    fun onParameterClick(parameter: String) {

        newWord(parameter)

    }
}