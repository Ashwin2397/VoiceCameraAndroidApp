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
import kotlin.math.absoluteValue


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

    val gimbalButtons = mutableListOf(Feature.RIGHT, Feature.LEFT, Feature.UP, Feature.DOWN, Feature.ROLL,)

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

interface AdaptiveParameterBar {

    /*
    * Selects the specified parameter and deselects the previously selected parameter.
    * */
    fun select(parameter: String)

    fun show(selectedParameter: String, parameters: MutableList<String>?, onParameterClick: ((parameter: String) -> Unit)?)
    fun hide()

}

class AdaptiveParameterGaugeBar(
    val layout: LinearLayout,
    val context: Context,
): AdaptiveParameterBar {

    val progressiveGauge = ProgressiveGauge(context, null, 0)
    val TAG = "PROGRESSIVE_GAUGE"
    /*
    * Initialize guage meter upon object instantiation
    * */
    init {

        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT)

        params.apply {
            width = 500
            height = 500
            leftMargin = 100
            bottomMargin = convertDpToPixel(15f).toInt()
        }

        progressiveGauge.apply {

            withTremble = false
            unitUnderSpeedText = false
            unit = ""
            layoutParams = params
        }
    }
    /*
    * Moves the guage meter to the specified amount
    * @param {String} parameter The amount by which to move to
    * */
    override fun select(parameter: String) {

        progressiveGauge.speedTo(parameter.toFloat(), 500)
    }

    /*
    * Use the supplied list of parameters and displays it.
    * @param {String} selectedParameter The currently selected parameter to display to user.
    * @param {ArrayList<String>} parameters A list of parameters that represents each parameter.
    * @param {(() -> Unit)?} onParameterClick Execute this callback upon selection of parameter via UI touch.
    * */
    override fun show(
        selectedParameter: String,
        parameters: MutableList<String>?,
        onParameterClick: ((parameter: String) -> Unit)?,
    ) {

        // Display current parameter
        progressiveGauge.speedTo(selectedParameter.toFloat(), 0)
        layout.addView(progressiveGauge)
    }

    override fun hide() {

        layout.removeView(progressiveGauge)
    }

    fun convertDpToPixel(dp: Float): Float {
        return dp * (context.resources.displayMetrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
    }
}


class AdaptiveParameterButtonBar(
    val layout: LinearLayout,
    val context: Context,
): AdaptiveParameterBar{

    var buttons = mutableMapOf<String, Button>()
    val selectedColor = "#25c433"
    val unSelectedColor = "#c4c4c0"
    var parameterSelected = false

    /*
    * Renders the selected view for the specified button.
    * Execute when a parameter has been selected.
    * Selection can be done via STT Observer or UI click.
    * @param {String} parameter The parameter that has been selected
    * */
    override fun select(parameter: String) {

        // REFACTOR:
        // - I do this because the parameter is an integer but the value of the displayed button is a float
        var params = parameter
        try {
            params = parameter.toFloat().toString()
        }catch (e: NumberFormatException) {

        }

        // Retrieve button from map using the name of the parameter
        var selectedButton = buttons.get(params) ?: buttons.get(parameter) ?: buttons.get(params.toFloat().absoluteValue.toInt().toString())


        // Deselect currently selected button
        buttons.forEach {
            it.value.setBackgroundColor(Color.parseColor(unSelectedColor))
        }

        // Initiate selection of that button
        selectedButton?.setBackgroundColor(Color.parseColor(selectedColor))

    }

    /*
    * Use the supplied list of parameters and displays it.
    * @param {String} selectedParameter The currently selected parameter to display to user.
    * @param {ArrayList<String>} parameters A list of parameters that represents each parameter.
    * @param {(() -> Unit)?} onParameterClick Execute this callback upon selection of parameter via UI touch.
    * */
    override fun show(selectedParameter: String, parameters: MutableList<String>?, onParameterClick: ((parameter: String) -> Unit)?) {


        // Create buttons with text acquired from parameters list
        parameters!!.forEach {

            // REFACTOR
            // Create button
            var layoutParameters = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT)

            layoutParameters.bottomMargin = convertDpToPixel(8f).toInt()

            var newButton = Button(this.context).apply {

                setText(it)

                // Set constraints
                layoutParams = layoutParameters
                minWidth = 0
                minimumWidth = 0
                minHeight = 0
                minimumHeight = 0

                setPadding(35, 10, 35,  10)

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

        select(selectedParameter)
    }

    /*
    * "Hides" the currently showing parameter bar by destroying it.
    * */
    override fun hide() {

        // Rm all buttons from view
        this.buttons.forEach {
            this.layout.removeView(it.value)
        }

        // rm all buttons from map
        this.buttons = mutableMapOf<String, Button>()
    }

    fun convertDpToPixel(dp: Float): Float {
        return dp * (context.resources.displayMetrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
    }

}