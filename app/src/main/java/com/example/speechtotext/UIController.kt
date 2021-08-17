package com.example.speechtotext

import android.content.Context
import android.graphics.Color
import android.util.DisplayMetrics
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ToggleButton
import com.github.anastr.speedviewlib.ProgressiveGauge
import java.util.*



class NewUIController(
    val headerCreaterLayout: LinearLayout,
    val deviceButtons: Map<DeviceType, Button>,
    val applicationContext: Context,
    val headerTextView: HeaderTextView
){

    val selectedColor = "#25c433"
    val unselectedColor = "#FF6200EE"
    enum class AdaptiveParameterBarType {
        BUTTON,
        GAUGE
    }

    data class ParameterDetails(
        val adaptiveParameterBarType: AdaptiveParameterBarType,
        val parameters: Any // IntRange for Gauge and List<String> for Button
    )

    // Move this to the master controller
    var featuresToParameters = mapOf<Feature, ParameterDetails>(
        Feature.ZOOM to ParameterDetails(AdaptiveParameterBarType.GAUGE, IntRange(0, 10)),
        Feature.MODE to ParameterDetails(AdaptiveParameterBarType.BUTTON, listOf("movie", "photo")),
        Feature.FOCUS to ParameterDetails(AdaptiveParameterBarType.BUTTON, listOf("point", "face", "spot")),
        Feature.MOVE to ParameterDetails(AdaptiveParameterBarType.GAUGE, IntRange(0, 10)),
    )

    val adaptiveParameterBars = mapOf<AdaptiveParameterBarType, AdaptiveParameterBar>(
        AdaptiveParameterBarType.BUTTON to AdaptiveParameterButtonBar(),
        AdaptiveParameterBarType.GAUGE to AdaptiveParameterGaugeBar(),
    )
    /*
    * Displays parameters based on the value of the Word object
    * */
    fun displayParameters(word: NewWord) {

        // If command has parameters
        // Display parameters
        // Else
        // Schedule reset

        val parameterDetails = featuresToParameters.get(word.feature)
        val hasParameters = parameterDetails != null

        if (hasParameters) {

            val adaptiveParameterBar = adaptiveParameterBars.get(parameterDetails.adaptiveParameterBarType)
            adaptiveParameterBar?.show(parameterDetails.parameters)
        }

    }

    fun select(word: NewWord) {}

    fun selectCommand(newWord: NewWord) {

        // If command is static
        // Select command from static map
        // else
        // Select command from list of displayed buttons

        // Display parameters

    }

    fun selectDevice(deviceType: DeviceType) {

        deviceButtons.get(deviceType)?.setBackgroundColor(Color.parseColor(this.selectedColor))
    }

    fun unSelectDevice(deviceType: DeviceType) {

        deviceButtons.get(deviceType)?.setBackgroundColor(Color.parseColor(this.unselectedColor))
    }

    fun selectHeader(newWord: NewWord) {

    }

    fun showHeaders(subHeaders: List<String>?, newWord: NewWord) {

        // Show sub headers
        subHeaders?.forEach {

            // Create button with text set to 'it'
            // Add button to linear layout
            // Add click listeners to report to Model it's text content

            var newButton = Button(applicationContext).apply {
                setText(it.toString())

                // Set constraints
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT)
            }

            headerCreaterLayout.addView(newButton)

        }

        // Show parents headers in HeaderTextView too
        headerTextView.addParents(newWord)
    }

    /*
    * Reset UI:
    * 1. Un-select both devices
    * 2. Remove all buttons from top
    * 3. Remove all buttons from parameter bar
    * 4. Remove all buttons from HeaderTextView
    * */
    fun reset() {

        // Reset device buttons
        deviceButtons.forEach {
            unSelectDevice(it.key)
        }

        headerCreaterLayout.removeAllViews()

        headerTextView.clear()

    }

    fun selectParameter(selectedCommand: NewWord, newWord: NewWord) {

        // Check if given parameter from bar is within the "bounds" of the chosen command
        // Select parameter from bar if parameter given is within the bounds

    }


}

class UIController(
    val adaptiveParameterBar: AdaptiveParameterBar,
){

    lateinit var button: Button
    var selected = false
    var feature = Feature.ANY
    var parameters: MutableList<String>? = null
    var onParameterClick: ((parameter: String) -> Unit)? = null // Refactor to have one callback for any type of UI touch
    var onCommandClick: ((command: String) -> Unit)? = null
    var selectedParameter = ""

    val selectedColor = "#25c433"
    val deSelectedColor = "#c4c4c0"

    val gimbalButtons = mutableListOf(Feature.RIGHT, Feature.LEFT, Feature.UP, Feature.DOWN, Feature.ROLL_NEGATIVE, Feature.ROLL_POSITIVE)

    /*
    * Renders "selected" button view for command.
    * Called by on click handler and by the STT Observer.
    * @param {Boolean} isUITouch Boolean indicating if the user input is touch
    * */
    // REFACTOR: Temporarily removed parameter
    fun selectCommand(optionalWord: Word) {

        this.selected = true

        // Render button selected by changing its color

        if (feature in gimbalButtons) {
            
            (button as ToggleButton).isChecked = true
        }else{

            button.setBackgroundColor(Color.parseColor(this.selectedColor))

        }


    }

    /*
    * Renders "selected" button view for command.
    * Called by on click handler and by the STT Observer.
    * @param {Boolean} isUITouch Boolean indicating if the user input is touch
    * */
    // REFACTOR: Temporarily removed parameter
    fun deSelectCommand() {

        this.selected = false



        //REFACTOR_CRITICAL: Remove this, it is here for testing
        if(this.feature in gimbalButtons) {
            (button as ToggleButton).isChecked = false

        }else{

            // Render button selected by changing its color
            button.setBackgroundColor(Color.parseColor(this.deSelectedColor))
        }

    }

    /*
   * Renders "selected" button view for adaptive parameter button bar.
   * Called by the STT Observer.
   * Note: Each button in bar already has on click listener.
   * */
    fun selectParameter(parameter: Word) {

        this.selectedParameter = parameter.value
        if (feature !in gimbalButtons) {
            this.button.setText("${feature}: ${selectedParameter}") // Do not do this for the gimbal buttons


        }
        this.adaptiveParameterBar.select(selectedParameter)
    }

    /*
    * Sets the feature type that this component represents.
    * Set at runtime.
    * @param {Feature} feature The feature that this component represents.
    * @param {() -> Unit)} onCommandClick The callback to be executed once the user has physically clicked on the command button.
    * @param {() -> Unit)} onParameterClick The callback to be executed once the user has physically clicked on a parameter from the adaptive button bar.
    * */
    fun setFeature(feature: Feature, parameters: MutableList<String>, onCommandClick: (command: String) -> Unit, onParameterClick: (parameter: String) -> Unit) {

        this.parameters = parameters

        if (parameters.count() > 0) {

            this.selectedParameter = parameters.get(0) // Set default selection
        }

        this.feature = feature

        this.onCommandClick = onCommandClick
        this.onParameterClick = onParameterClick

    }

    fun setCommandButton(button: Button) {

        this.button = button

        this.button.apply {

            //REFACTOR_CRITICAL: Remove this, it is here for testing
            if(feature !in gimbalButtons){
                setText("${feature}: ${selectedParameter}")

            }

            setOnClickListener {

                val words = feature.toString().split("_")
                var wordsString = ""

                words.forEach {
                    wordsString += it + " "
                }

                SpeechToTextEngine.notifyModel(wordsString)
//                Model.newWord(feature.toString().lowercase())
                // REFACTOR: Change to Model.newWord(feature.toString().lowercase())
//                onCommandClick?.let { it1 -> it1(feature.toString().lowercase()) }
            }
        }
    }

    /*
    * Shows the parameter button bar with the previously supplied list of parameters.
    * Runs upon selection of button.
    * Users can select a parameter from bar or speak it.
    * */
    fun showParameterButtonBar(optionalWord: Word) {

        this.adaptiveParameterBar.show(this.selectedParameter, this.parameters, this.onParameterClick)

    }

    /*
    * Hides the parameter button bar.
    * */
    fun hideParameterButtonBar() {
        this.adaptiveParameterBar.hide()
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
        deSelectCommand()

    }
}
