package com.example.speechtotext

import android.content.Context
import android.graphics.Color
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import androidx.core.view.setPadding
import java.util.ArrayList
import kotlin.reflect.KFunction1


class UIController(
    val adaptiveParameterButtonBar: AdaptiveParameterButtonBar
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


    /*
    * Renders "selected" button view for command.
    * Called by on click handler and by the STT Observer.
    * @param {Boolean} isUITouch Boolean indicating if the user input is touch
    * */
    // REFACTOR: Temporarily removed parameter
    fun selectCommand(optionalWord: Word) {

        this.selected = true

        // Render button selected by changing its color
        button.setBackgroundColor(Color.parseColor(this.selectedColor))


    }

    /*
    * Renders "selected" button view for command.
    * Called by on click handler and by the STT Observer.
    * @param {Boolean} isUITouch Boolean indicating if the user input is touch
    * */
    // REFACTOR: Temporarily removed parameter
    fun deSelectCommand() {

        this.selected = false

        // Render button selected by changing its color
        button.setBackgroundColor(Color.parseColor(this.deSelectedColor))

    }

    /*
   * Renders "selected" button view for adaptive parameter button bar.
   * Called by the STT Observer.
   * Note: Each button in bar already has on click listener.
   * */
    fun selectParameter(parameter: Word) {

        this.adaptiveParameterButtonBar.select(parameter.value)

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

        this.feature = feature

        this.onCommandClick = onCommandClick
        this.onParameterClick = onParameterClick

    }

    fun setCommandButton(button: Button) {

        this.button = button

        this.button.apply {
            setText(feature.toString())
            setOnClickListener {

                Model.newWord(feature.toString().lowercase())
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
        deSelectCommand()

    }
}


class DynamicButton(
    val applicationContext: Context
){
    val button = Button(applicationContext)

    fun setText(text: String) {

        button.setText(text)
    }

    fun select() {}

    fun addOnClickListener(callback: () -> Unit) {
//        callback(button.text)
    }

}

class AdaptiveParameterButtonBar(
    val layout: LinearLayout,
    val context: Context
){

    var buttons = mutableMapOf<String, Button>()
    val selectedColor = "#25c433"
    var parameterSelected = false

    /*
    * Renders the selected view for the specified button.
    * Execute when a parameter has been selected.
    * Selection can be done via STT Observer or UI click.
    * @param {String} parameter The parameter that has been selected
    * */
    fun select(parameter: String) {

        // REFACTOR:
        // - I do this because the parameter is an integer but the value of the displayed button is a float
        var params = parameter
        try {
            params = parameter.toFloat().toString()
        }catch (e: NumberFormatException) {

        }

        // Retrieve button from map using the name of the parameter
        var selectedButton = buttons.get(params)

        if (selectedButton == null) {
            selectedButton = buttons.get(parameter)
        }

        // Initiate selection of that button
        selectedButton?.setBackgroundColor(Color.parseColor(this.selectedColor))

    }

    /*
    * Use the supplied list of parameters and display it.
    * @param {ArrayList<String>} parameters A list of parameters that represents each parameter.
    * @param {(() -> Unit)?} onParameterClick Execute this callback upon selection of parameter via UI touch.
    * */
    fun show(parameters: MutableList<String>?, onParameterClick: ((parameter: String) -> Unit)?) {


        // Create buttons with text acquired from parameters list
        parameters!!.forEach {

            // REFACTOR
            // Create button
            var newButton = Button(this.context).apply {

                setText(it)

                // Set constraints
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT)
                
                setOnClickListener {

                    // REFACTOR: Change to Model.newWord(feature.toString().lowercase())
                    val btn = it as Button
                    val parameter = btn.text.toString()

                    Model.newWord(parameter)
//                    if (onParameterClick != null) {
//                        onParameterClick(parameter) // REFACTOR: Pass in parameter as an argument
//                    }
                }
            }

            this.layout.addView(newButton) // Add to view
            this.buttons.put(it, newButton) // Add to map

        }

    }

    /*
    * "Hides" the currently showing parameter bar by destroying it.
    * */
    fun hide() {

        // Rm all buttons from view
        this.buttons.forEach {
            this.layout.removeView(it.value)
        }

        // rm all buttons from map
        this.buttons = mutableMapOf<String, Button>()
    }

}