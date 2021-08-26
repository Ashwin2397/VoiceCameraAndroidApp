package com.example.speechtotext

import android.content.Context
import android.graphics.Color
import android.util.DisplayMetrics
import android.util.Log
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import com.github.anastr.speedviewlib.ProgressiveGauge
import com.google.android.material.slider.Slider

class HeaderView(
    val headerView: LinearLayout,
    val context: Context
){

    val selectedColor = "#25c433"

    fun showParentHeaders(word: Word) {

        // NO OP


        /*var parents = mutableListOf<Button>()
        word.parents.forEach {

            var newButton = createButton(it.toString())

            headerView.addView(newButton) // Add to view
        }

        headerView.addView(createButton(word.value).apply {

            setBackgroundColor(Color.parseColor(selectedColor))
        })*/
    }

    fun clear() {

        // NO OP
        // headerView.removeAllViews()
    }

    private fun createButton(buttonText: String): Button {

        var layoutParameters = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT).apply {

                bottomMargin = 0

        }

        return Button(context).apply {

            setText(buttonText)

            textSize = 15F

            // Set constraints
            layoutParams = layoutParameters

            minWidth = 0
            minimumWidth = 0
            minHeight = 0
            minimumHeight = 0

            setPadding(0, 0, 0, 1)

            setOnClickListener {

                val btn = it as Button
                val parameter = btn.text.toString()

                SpeechToTextEngine.notifyModel(parameter)
                btn.setBackgroundColor(Color.parseColor(selectedColor))
            }
        }
    }

}


interface AdaptiveParameterBar {

    fun select(parameter: String)
    fun show(parameterDetails: ParameterDetails, onParameterClick: ((parameter: String) -> Unit)?)
    fun hide()

}
enum class AdaptiveParameterBarType {
    BUTTON,
    GAUGE,
    SLIDER
}

data class ParameterDetails(
    val adaptiveParameterBarType: AdaptiveParameterBarType,
    var numericalParameters: IntRange? = null, // IntRange for Gauge and List<String> for Button
    var stringParameters: List<String>? = null,
    var currentNumericalSelection: Float? = null,
    var currentStringSelection: String? = null
){

    /*
    * Checks to see if the given parameter is within the bounds provided by it's set of parameters.
    * @param selectedParameter {String} It can be a String or a String representation of an Int.
    * @return {Boolean} The boolean value of whether the selectedParameter is in it's bounds.
    * */
    fun isInBounds(selectedParameter: String): Boolean {

        var isInBounds = false
        val isNumericalParameter = numericalParameters != null
        val isNumeric =  try {
                (selectedParameter).toFloat()
                true
            }catch (e: NumberFormatException) { false }


        if (isNumericalParameter) {
            isInBounds = isNumeric && (selectedParameter.toFloat() <= numericalParameters!!.last && selectedParameter.toFloat() >= numericalParameters!!.first)
        }else {
            isInBounds = (stringParameters?.contains((selectedParameter)) ?: false)
        }

        return isInBounds
    }

    /*
    * Sets the current selection based on the value of the Word object.
    * It's bounds has already been checked!
    * */
    fun setCurrentSelection(selectedParameter: Word) {

        val isNumericalParameter = numericalParameters != null
        if (isNumericalParameter) {

            currentNumericalSelection = selectedParameter.value.toFloat() // Already been checked, so not to worry ...
        }else {

            currentStringSelection = selectedParameter.value
        }
    }
}
class AdaptiveParameterSliderBar(
    val layout: LinearLayout,
    val context: Context
): AdaptiveParameterBar {

    val slider = Slider(context).apply {

        layoutParams = LinearLayout.LayoutParams(
            convertDpToPixel(325F),
            ViewGroup.LayoutParams.WRAP_CONTENT)
    }
    val label = TextView(context).apply {
        layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    init {

        slider.addOnChangeListener { slider, value, fromUser ->


            label.setText(value.toString())
        }

        slider.addOnSliderTouchListener(object : Slider.OnSliderTouchListener {
            override fun onStartTrackingTouch(slider: Slider) {
                // Responds to when slider's touch event is being started
                Log.d("onStartTrackingTouch", slider.value.toString())
            }

            override fun onStopTrackingTouch(slider: Slider) {
                // Responds to when slider's touch event is being stopped
                Log.d("onStopTrackingTouch", slider.value.toString())

                val value = slider.value.toString()
                SpeechToTextEngine.notifyModel(value)
                label.setText(value)
            }
        })




    }

    override fun select(parameter: String) {

        slider.value = parameter.toFloat()
    }

    override fun show(
        parameterDetails: ParameterDetails,
        onParameterClick: ((parameter: String) -> Unit)?,
    ) {
        val range = parameterDetails.numericalParameters

        slider.apply {

            stepSize = 0.5F

            valueFrom = range!!.first.toFloat()
            valueTo = range!!.last.toFloat()

            value = parameterDetails.currentNumericalSelection!! // REFACTOR: Edit to get current selection if it is in absolute mode
        }

        label.apply {
            text = parameterDetails.currentNumericalSelection.toString()
            textSize = convertDpToPixel(10F).toFloat()
        }

        layout.addView(slider)
        layout.addView(label)
    }

    override fun hide() {
        layout.removeAllViews()
    }

    private fun convertDpToPixel(dp: Float): Int {
        return (dp * (context.resources.displayMetrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)).toInt()
    }
}

class AdaptiveParameterButtonBar(
    val layout: LinearLayout,
    val context: Context,
):AdaptiveParameterBar {

    var buttons = mutableMapOf<String, Button>()
    val selectedColor = "#25c433"
    val deSelectedColor = "#c4c4c0"

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
        var selectedButton = buttons.get(params)

        if (selectedButton == null) {
            selectedButton = buttons.get(parameter)
        }

        // De select other buttons
        buttons.forEach {
            it.value.setBackgroundColor((Color.parseColor(deSelectedColor)))
        }

        // Initiate selection of that button
        selectedButton?.setBackgroundColor(Color.parseColor(this.selectedColor))

    }

    /*
    * Use the supplied list of parameters and displays it.
    * @param {String} selectedParameter The currently selected parameter to display to user.
    * @param {ArrayList<String>} parameters A list of parameters that represents each parameter.
    * @param {(() -> Unit)?} onParameterClick Execute this callback upon selection of parameter via UI touch.
    * */
    override fun show(parameterDetails: ParameterDetails, onParameterClick: ((parameter: String) -> Unit)?) {

        val parameters = parameterDetails.stringParameters
        val selectedParameter = parameterDetails.currentStringSelection

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



                setOnClickListener {

                    // REFACTOR: Change to Model.Word(feature.toString().lowercase())
                    val btn = it as Button
                    val parameter = btn.text.toString()

                    SpeechToTextEngine.notifyModel(parameter)
                    btn.setBackgroundColor(Color.parseColor(selectedColor))
                }
            }

            this.layout.addView(newButton) // Add to view
            this.buttons.put(it, newButton) // Add to map
        }

        select(selectedParameter!!)
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
        parameterDetails: ParameterDetails,
        onParameterClick: ((parameter: String) -> Unit)?,
    ) {

        val range = parameterDetails.numericalParameters
        val selectedParameter = parameterDetails.currentNumericalSelection

        progressiveGauge.apply {

            minSpeed = range!!.first.toFloat()
            maxSpeed= range.last.toFloat()

            speedTo(selectedParameter!!.toFloat(), 0)
        }

        // Add min
        layout.addView(TextView(context).apply {
            setText(range!!.first.toString())

            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                leftMargin = 100
                bottomMargin = convertDpToPixel(5.8f).toInt()
            }
        })

        // Display current parameter
        layout.addView(progressiveGauge)

        // Add max
        layout.addView(TextView(context).apply {
            setText(range!!.last.toString())

            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                bottomMargin = convertDpToPixel(5.8f).toInt()
            }
        })
    }

    override fun hide() {

        layout.removeAllViews()
    }

    fun convertDpToPixel(dp: Float): Float {
        return dp * (context.resources.displayMetrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
    }
}

