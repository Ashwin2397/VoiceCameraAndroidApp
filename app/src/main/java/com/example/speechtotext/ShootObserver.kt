package com.example.speechtotext

import android.util.Log
import android.widget.Button
import android.widget.TextView
import com.example.speechtotext.devicecontroller.CanonCameraController
import java.util.*
import kotlin.concurrent.timer

class ShootObserver(
    val uiController: UIController
): Observer {
    private val TAG = "SHOOT_OBSERVER"

    val cameraController = CanonCameraController() // REFACTOR: Pass in a reference upon instantiating this object

    val states = mapOf<Int, State>(
        0 to State(0, mapOf(InputType.COMMAND_1 to 1), arrayOf(), arrayOf(uiController::selectCommand, this::shoot, this::reset)),

    )

    val consecutiveWords = mapOf<String, Word>()


    val command = "shoot"
    var currentState = 0
    val finalState = 1
    var currentWord = Word("", Feature.UNDEFINED, InputType.UNDEFINED, DeviceType.UNDEFINED)

    override fun newWord(word: String) {

        Log.d(this.TAG, word)
        val parsedWord = this.parseWord(word)
        this.currentWord = parsedWord // Save word to be used by cb

        val currentStateObject = this.states[this.currentState]

        val newState = currentStateObject!!.newWord(parsedWord.inputType)

        changeState(newState)

    }

    fun changeState(newState: Int) {

        val currentStateObject = this.states[this.currentState]

        val newStateObject = this.states[newState]

        val hasStateChanged = newState != this.currentState
        if (hasStateChanged) {

            this.currentState = newState;

            // Run final cb before state changes
            currentStateObject!!.runFinalCallbacks();

            // Refactor this, look at the runFinalCallbacks() and runCallbacks()
            // implementation again to decide how to refactor
            if (newState != this.finalState) {

                // Run cb in new state
                newStateObject!!.runCallbacks();

            }

        }


    }
    fun parseWord(word: String): Word {

        var parsedWord = Word("", Feature.UNDEFINED, InputType.UNDEFINED, DeviceType.UNDEFINED);

        val isDefinedCommand = firstCommands[word] != null
        val isOurCommand = isDefinedCommand && firstCommands[word]?.value == this.command
        val isConsecutiveWord = this.consecutiveWords[word] != null
        val isOtherCommand = isDefinedCommand && firstCommands[word]?.value != this.command;
        val isNumericalParameter = numericalParameters[word] != null

        if (isOurCommand) {
            parsedWord = firstCommands[word]!!
        }else if (isConsecutiveWord) {
            parsedWord = this.consecutiveWords[word]!!
        }else if (isOtherCommand) {
            parsedWord = Word("", Feature.ANY, InputType.OTHER_COMMAND, DeviceType.ANY)
        }else if (isNumericalParameter) {
            parsedWord = numericalParameters[word]!!
        }

        return parsedWord
    }

    fun reset() {

//        timer.schedule(TimerTask {}, 100)
       this.currentState = 0
    }

    fun shoot() { Log.d("SHOOT_OBSERVER", "SHOOT!!!!")}

}



class UIController(
    val button:Button,
    val adaptiveParameterButtonBar: AdaptiveParameterButtonBar
    ){

    var selected = false
    var feature = Feature.ANY
    var parameters: ArrayList<String>? = null
    var onParameterClick: (() -> Unit)? = null // Refactor to have one callback for any type of UI touch
    var onCommandClick: (() -> Unit)? = null
        var selectedParameter = ""

    /*
    * Renders "selected" button view for command.
    * Called by on click handler and by the STT Observer.
    * @param {Boolean} isUITouch Boolean indicating if the user input is touch
    * */
    // REFACTOR: Temporarily removed parameter
    fun selectCommand() {

        this.selected = true

        // Render button selected by changing its color
        button.setBackgroundColor(1234)

//        if (isUITouch) {
//            onCommandClick?.let { it() }
//        }

    }

    /*
   * Renders "selected" button view for adaptive parameter button bar.
   * Called by on click handler and by the STT Observer.
   * */
    fun selectParameter(isUITouch: Boolean, parameter: String) {

        this.adaptiveParameterButtonBar.select(parameter)

        if (isUITouch) {
            onParameterClick?.let { it() } // Refactor to take args in function
        }

    }

    /*
    * Sets the feature type that this component represents.
    * Set at runtime.
    * @param {Feature} feature The feature that this component represents.
    * @param {() -> Unit)} onCommandClick The callback to be executed once the user has physically clicked on the command button.
    * @param {() -> Unit)} onParameterClick The callback to be executed once the user has physically clicked on a parameter from the adaptive button bar.
    * */
    fun setFeature(feature: Feature, onCommandClick: () -> Unit, onParameterClick: () -> Unit) {

        this.feature = feature
        this.button.setText(this.feature.toString())

        this.onCommandClick = onCommandClick
        this.onParameterClick = onParameterClick

    }

    /*
    * Shows the parameter button bar with the previously supplied list of parameters.
    * Runs upon selection of button.
    * Users can select a parameter from bar or speak it.
    * */
    fun showParameterButtonBar() {

        // REFACTOR
        // We temporarily use a text box to show the parameters

        this.adaptiveParameterButtonBar.show(this.parameters, this.onParameterClick)

    }

    /*
    * Hides the parameter button bar.
    * */
    fun hideParameterButtonBar() {
        this.adaptiveParameterButtonBar.hide()
    }

    /*
    * Supplies the parameters that the adaptive button bar should display.
    * @param {ArrayList<String>} parameters A list of parameters that represents each parameter.
    * */
    fun setButtonBarParameters(parameters: ArrayList<String>) {

        this.parameters = parameters
    }

    /*
    * Resets the view back to its original render.
    * Unselects the selected button and hides the parameter button bar.
    * */
    fun reset() {

        this.selected = false

        hideParameterButtonBar()

    }
}

class AdaptiveParameterButtonBar {

    /*
    * Execute when a parameter has been selected.
    * Selection can be done via STT Observer or UI click.
    * @param {String} parameter The parameter that has been selected
    * */
    fun select(parameter: String) {
        TODO("The parameter button bar implementation is still undecided.")
    }

    /*
    * Use the supplied list of parameters and display it.
    * @param {ArrayList<String>} parameters A list of parameters that represents each parameter.
    * @param {(() -> Unit)?} onParameterClick Execute this callback upon selection of parameter via UI touch.
    * */
    fun show(parameters: ArrayList<String>?, onParameterClick: (() -> Unit)?) {
        TODO("The parameter button bar implementation is still undecided.")
    }

    /*
    * Hide the currently showing parameter bar.
    * */
    fun hide() {
        TODO("The parameter button bar implementation is still undecided.")
    }

}