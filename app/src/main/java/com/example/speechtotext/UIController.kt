package com.example.speechtotext

import android.content.Context
import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ToggleButton
import com.google.android.material.button.MaterialButtonToggleGroup
import java.util.*
import kotlin.concurrent.timerTask


class UIController(
    val headerCreaterLayout: LinearLayout,
    val parameterCreaterLayout: LinearLayout,
    val deviceButtons: Map<DeviceType, Button>,
    val applicationContext: Context,
    val headerTextView: HeaderTextView,
    val staticButtons: Map<Feature, Button>
){

    private val TAG = "UICONTROLLER"

    var selectedCommand:NewWord? = null

    val selectedColor = "#25c433"
    val unselectedColor = "#FF6200EE"

    var dynamicButtons = mutableMapOf<String, ToggleButton>(

    )

    // Move this to the master controller
    var featuresToParameters = mapOf<Feature, ParameterDetails>(
        Feature.ZOOM to ParameterDetails(AdaptiveParameterBarType.GAUGE, numericalParameters = IntRange(0, 10), currentNumericalSelection = 0),
        Feature.MODE to ParameterDetails(AdaptiveParameterBarType.BUTTON, stringParameters = listOf("movie", "photo"), currentStringSelection =  "photo"),
        Feature.FOCUS to ParameterDetails(AdaptiveParameterBarType.BUTTON, stringParameters = listOf("point", "face", "spot"), currentStringSelection = "point"),
        Feature.MOVE to ParameterDetails(AdaptiveParameterBarType.GAUGE, numericalParameters = IntRange(0, 10), currentNumericalSelection = 0),
    )

    val adaptiveParameterBars = mapOf<AdaptiveParameterBarType, AdaptiveParameterBar>(
        AdaptiveParameterBarType.BUTTON to AdaptiveParameterButtonBar(parameterCreaterLayout, applicationContext),
        AdaptiveParameterBarType.GAUGE to AdaptiveParameterGaugeBar(parameterCreaterLayout, applicationContext),
    )

    init {

        deviceButtons.forEach {
            val deviceType = it.key.toString()

            it.value.setOnClickListener {
                SpeechToTextEngine.notifyModel(deviceType)
            }
        }
    }
    /*
    * Shows parameters based on the value of the Word object.
    * If command has parameters => Show parameters
    * Else => Schedule reset 
    * @param {NewWord} word Contains the key to retrieve it's parameter details. 
    * */
    fun showParameters(selectedCommand: NewWord) {
        
        val parameterDetails = featuresToParameters.get(selectedCommand.feature)
        val hasParameters = parameterDetails != null

        if (hasParameters) {

            val adaptiveParameterBar = adaptiveParameterBars.get(parameterDetails!!.adaptiveParameterBarType)
            adaptiveParameterBar?.show(parameterDetails, SpeechToTextEngine::notifyModel)
        }else {
            scheduleReset(selectedCommand)
        }

        Log.d(TAG, "Show parameters for: ${selectedCommand.feature}")
    }

    /*
    * Schedules a reset after a fixed time period.
    * @param {NewWord} selectedParameter
    * */
    fun scheduleReset(selectedParameter: NewWord) {

        val mainLooper = Looper.getMainLooper()

        Thread(Runnable {

            Timer("Reset UI", false).schedule(timerTask {
                Handler(mainLooper).post {

                    reset(selectedParameter)
                }
            }, 1000)
        }).start()
    }

    /*
    * If command is static => select command from static map
    * else => select command from list of displayed buttons
    * @param {NewWord} newWord Contains the key to retrieve the button.
    * */
    fun selectCommand(newCommand: NewWord) {

        val button = (staticButtons[newCommand.feature] ?: getDynamicButton(newCommand.value)) as ToggleButton
        button.isChecked = true

        selectedCommand = newCommand
    }

    fun selectDevice(newWord: NewWord) {

        deviceButtons.get(newWord.deviceType)?.setBackgroundColor(Color.parseColor(this.selectedColor))
    }

    fun unSelectDevice(deviceType: DeviceType) {

        deviceButtons.get(deviceType)?.setBackgroundColor(Color.parseColor(this.unselectedColor))
    }

    fun selectHeader(newWord: NewWord) {

    }

    /*
    * Fetches dynamic button from it's map.
    * If button exits in map => returns that button
    * Else => Creates button, puts it in the map and returns it
    * */
    fun getDynamicButton(buttonText: String): ToggleButton {

        val newButton = dynamicButtons.get(buttonText) ?: ToggleButton(applicationContext).apply {

//                setTextOff(buttonText)
//                setTextOn(buttonText)

            setText(buttonText)

            // Set constraints
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT)

            setOnClickListener {
                SpeechToTextEngine.notifyModel(buttonText)
            }
        }

        if (!dynamicButtons.containsKey(buttonText)) {
            dynamicButtons[buttonText] = newButton
        }

        return newButton
    }

    fun showChildHeaders(newWord: NewWord) {

        val childHeaders = headersToCommands.get(newWord.header)

        // Show child headers
        childHeaders?.forEach {

            // Create button with text set to 'it'
            // Add button to linear layout
            // Add click listeners to report to Model it's text content

            val buttonText = it

            val newButton = getDynamicButton(buttonText)

            headerCreaterLayout.addView(newButton)
        }

    }

    fun showParentHeaders(newWord: NewWord) {
        // Show parents headers in HeaderTextView too
        headerTextView.showParentHeaders(newWord)
    }

    /*
    * Reset UI:
    * 1. Un-select both devices
    * 2. Remove all buttons from top
    * 3. Remove all buttons from parameter bar
    * 4. Remove all buttons from HeaderTextView
    * */
    fun reset(newWord: NewWord) {

        // Reset device buttons
        deviceButtons.forEach {
            unSelectDevice(it.key)
        }

        headerCreaterLayout.removeAllViews()

        headerTextView.clear()

        adaptiveParameterBars.forEach {
            it.value.hide()
        }
    }

    /*
    * Checks if the given parameter is in bounds.
    * @param selectedCommand {NewWord} The command that the user has previously selected.
    * @param selectedParameter {NewWord} The parameter that the user has just selected.
    * @return {Boolean} Value of in bounds check.
    * */
    fun isParameterInBounds(selectedParameter: NewWord): Boolean {

        val parameterDetails = featuresToParameters[selectedCommand?.feature]

        return parameterDetails!!.isInBounds(selectedParameter.value)
    }

    /*
    * Selects the given parameter from it's adaptive bar for the given command.
    * @param selectedCommand {NewWord} The command that the user has previously selected.
    * @param selectedParameter {NewWord} The parameter that the user has just selected.
    * */
    fun selectParameter(selectedParameter: NewWord) {

        val parameterDetails = featuresToParameters[selectedCommand?.feature]

        val adaptiveParameterBar = adaptiveParameterBars[parameterDetails?.adaptiveParameterBarType]
        adaptiveParameterBar?.select(selectedParameter.value)
    }


}
//
//class UIController(
//    val adaptiveParameterBar: AdaptiveParameterBar,
//){
//
//    lateinit var button: Button
//    var selected = false
//    var feature = Feature.ANY
//    var parameters: MutableList<String>? = null
//    var onParameterClick: ((parameter: String) -> Unit)? = null // Refactor to have one callback for any type of UI touch
//    var onCommandClick: ((command: String) -> Unit)? = null
//    var selectedParameter = ""
//
//    val selectedColor = "#25c433"
//    val deSelectedColor = "#c4c4c0"
//
//    val gimbalButtons = mutableListOf(Feature.RIGHT, Feature.LEFT, Feature.UP, Feature.DOWN, Feature.ROLL_NEGATIVE, Feature.ROLL_POSITIVE)
//
//    /*
//    * Renders "selected" button view for command.
//    * Called by on click handler and by the STT Observer.
//    * @param {Boolean} isUITouch Boolean indicating if the user input is touch
//    * */
//    // REFACTOR: Temporarily removed parameter
//    fun selectCommand(optionalWord: Word) {
//
//        this.selected = true
//
//        // Render button selected by changing its color
//
//        if (feature in gimbalButtons) {
//
//            (button as ToggleButton).isChecked = true
//        }else{
//
//            button.setBackgroundColor(Color.parseColor(this.selectedColor))
//
//        }
//
//
//    }
//
//    /*
//    * Renders "selected" button view for command.
//    * Called by on click handler and by the STT Observer.
//    * @param {Boolean} isUITouch Boolean indicating if the user input is touch
//    * */
//    // REFACTOR: Temporarily removed parameter
//    fun deSelectCommand() {
//
//        this.selected = false
//
//
//
//        //REFACTOR_CRITICAL: Remove this, it is here for testing
//        if(this.feature in gimbalButtons) {
//            (button as ToggleButton).isChecked = false
//
//        }else{
//
//            // Render button selected by changing its color
//            button.setBackgroundColor(Color.parseColor(this.deSelectedColor))
//        }
//
//    }
//
//    /*
//   * Renders "selected" button view for adaptive parameter button bar.
//   * Called by the STT Observer.
//   * Note: Each button in bar already has on click listener.
//   * */
//    fun selectParameter(parameter: Word) {
//
//        this.selectedParameter = parameter.value
//        if (feature !in gimbalButtons) {
//            this.button.setText("${feature}: ${selectedParameter}") // Do not do this for the gimbal buttons
//
//
//        }
//        this.adaptiveParameterBar.select(selectedParameter)
//    }
//
//    /*
//    * Sets the feature type that this component represents.
//    * Set at runtime.
//    * @param {Feature} feature The feature that this component represents.
//    * @param {() -> Unit)} onCommandClick The callback to be executed once the user has physically clicked on the command button.
//    * @param {() -> Unit)} onParameterClick The callback to be executed once the user has physically clicked on a parameter from the adaptive button bar.
//    * */
//    fun setFeature(feature: Feature, parameters: MutableList<String>, onCommandClick: (command: String) -> Unit, onParameterClick: (parameter: String) -> Unit) {
//
//        this.parameters = parameters
//
//        if (parameters.count() > 0) {
//
//            this.selectedParameter = parameters.get(0) // Set default selection
//        }
//
//        this.feature = feature
//
//        this.onCommandClick = onCommandClick
//        this.onParameterClick = onParameterClick
//
//    }
//
//    fun setCommandButton(button: Button) {
//
//        this.button = button
//
//        this.button.apply {
//
//            //REFACTOR_CRITICAL: Remove this, it is here for testing
//            if(feature !in gimbalButtons){
//                setText("${feature}: ${selectedParameter}")
//
//            }
//
//            setOnClickListener {
//
//                val words = feature.toString().split("_")
//                var wordsString = ""
//
//                words.forEach {
//                    wordsString += it + " "
//                }
//
//                SpeechToTextEngine.notifyModel(wordsString)
////                Model.newWord(feature.toString().lowercase())
//                // REFACTOR: Change to Model.newWord(feature.toString().lowercase())
////                onCommandClick?.let { it1 -> it1(feature.toString().lowercase()) }
//            }
//        }
//    }
//
//    /*
//    * Shows the parameter button bar with the previously supplied list of parameters.
//    * Runs upon selection of button.
//    * Users can select a parameter from bar or speak it.
//    * */
//    fun showParameterButtonBar(optionalWord: Word) {
//
//        this.adaptiveParameterBar.show(this.selectedParameter, this.parameters, this.onParameterClick)
//
//    }
//
//    /*
//    * Hides the parameter button bar.
//    * */
//    fun hideParameterButtonBar() {
//        this.adaptiveParameterBar.hide()
//    }
//
//    /*
//    * Supplies the parameters that the adaptive button bar should display.
//    * @param {ArrayList<String>} parameters A list of parameters that represents each parameter.
//    * */
//    fun setButtonBarParameters(parameters: ArrayList<String>) {
//
//        this.parameters = parameters
//    }
//
//    /*
//    * Resets the view back to its original render.
//    * Unselects the selected button and hides the parameter button bar.
//    * */
//    fun reset() {
//
//        this.selected = false
//
//        hideParameterButtonBar()
//        deSelectCommand()
//
//    }
//}
