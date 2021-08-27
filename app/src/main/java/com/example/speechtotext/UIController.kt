package com.example.speechtotext

import android.content.Context
import android.graphics.Color
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ToggleButton
import com.example.speechtotext.devicecontroller.MasterCamera
import com.example.speechtotext.devicecontroller.MasterGimbal


class UIController(
    val headerCreaterLayout: LinearLayout,
    val parameterCreaterLayout: LinearLayout,
    val deviceButtons: Map<DeviceType, Button>,
    val applicationContext: Context,
    val headerView: HeaderView,
){

    private var staticButtons = mutableMapOf<Feature, Button>() // I changed this so that I can change set this attribute everytime the chosen controls changes
    private val TAG = "UICONTROLLER"

    var selectedCommand:Word? = null

    val selectedColor = "#25c433"
    val unselectedColor = "#FF6200EE"

    var dynamicButtons = mutableMapOf<String, ToggleButton>(

    )

    val noParameterBinding = listOf(Feature.SHOOT, Feature.UP, Feature.DOWN, Feature.LEFT, Feature.RIGHT, Feature.ROLL, Feature.HOME, Feature.PORTRAIT, Feature.LANDSCAPE)

    val adaptiveParameterBars = mapOf(
        AdaptiveParameterBarType.BUTTON to AdaptiveParameterButtonBar(parameterCreaterLayout, applicationContext),
        AdaptiveParameterBarType.GAUGE to AdaptiveParameterGaugeBar(parameterCreaterLayout, applicationContext),
        AdaptiveParameterBarType.SLIDER to AdaptiveParameterSliderBar(parameterCreaterLayout, applicationContext)
    )

    init {

        // Add on click listeners for device buttons
        deviceButtons.forEach {
            val deviceType = it.key.toString()

            it.value.setOnClickListener {
                SpeechToTextEngine.notifyModel(deviceType)
            }
        }
    }

    /*
    * Use list of chosen controls to do the following:
    * 1. Create buttons
    * 2. Add to command layout
    * */
    fun setStaticButtons(chosenControls: MutableList<Feature>, otherStaticButtons: MutableMap<Feature, ToggleButton>) {

        staticButtons.clear()
        headerCreaterLayout.removeAllViews()

        val NUMBER_ROWS = 5
        var verticalLinearLayout:LinearLayout? = null
        var i = 0

        // These buttons are the chosen controls
        chosenControls.forEach {

            if (it != Feature.SHOOT) {

                if (i%NUMBER_ROWS == 0) {
                    verticalLinearLayout = LinearLayout(applicationContext).apply {

                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT)
                        orientation = LinearLayout.VERTICAL
                    }

                    headerCreaterLayout.addView(verticalLinearLayout)
                }

                val buttonText = it.toString()
                val newButton = createNewButton(buttonText)

                verticalLinearLayout!!.addView(newButton)
                staticButtons.put(it, newButton)

                i += 1
            }
        }

        otherStaticButtons.forEach {
            val feature = it.key
            staticButtons.put(feature, it.value)

            it.value.setOnClickListener {
                SpeechToTextEngine.notifyModel(feature.toString())
            }
        }
    }

    /*
    * If hasParameterBinding => [COMMAND]:[CURRENT PARAMETER SELECTION]
    * Else => [COMMAND]
    *
    * */
    private fun getButtonText(feature: Feature): String {

        val word = words.get(feature.toString().lowercase())
        val parameterDetails = getParameterDetails(word!!)

        var buttonText = word.value

        val hasParameterBinding = !noParameterBinding.contains(feature)
        if (hasParameterBinding) {
            buttonText += ": ${parameterDetails?.currentNumericalSelection?.toString() ?: parameterDetails?.currentStringSelection ?: ""}"
        }

        return buttonText
    }

    /*
    * Shows parameters based on the value of the Word object.
    * If command has parameters => Show parameters
    * Else => Schedule reset 
    * @param {Word} word Contains the key to retrieve it's parameter details.
    * */
    fun showParameters(selectedCommand: Word) {

        val parameterDetails = getParameterDetails(selectedCommand)

        val adaptiveParameterBar = adaptiveParameterBars.get(parameterDetails!!.adaptiveParameterBarType)
        adaptiveParameterBar?.show(parameterDetails, SpeechToTextEngine::notifyModel)
    }

    /*
    * Checks if the given command has parameters or not.
    * @param {Word} selectedCommand The command to check.
    * */
    fun hasParameters(selectedCommand: Word): Boolean {

        val parameterDetails = getParameterDetails(selectedCommand)
        return parameterDetails != null
    }

    /*
    * If command is static => select command from static map
    * else => select command from list of displayed buttons
    * @param {Word} Word Contains the key to retrieve the button.
    * */
    fun selectCommand(newCommand: Word) {

        val button = (staticButtons[newCommand.feature] ?: getButton(newCommand.value, dynamicButtons)) as ToggleButton
        button.isChecked = true

        selectedCommand = newCommand
    }

    fun selectDevice(Word: Word) {

        deviceButtons.get(Word.deviceType)?.setBackgroundColor(Color.parseColor(this.selectedColor))
    }

    fun unSelectDevice(deviceType: DeviceType) {

        deviceButtons.get(deviceType)?.setBackgroundColor(Color.parseColor(this.unselectedColor))
    }

    /*
    * Fetches button from it's specified map.
    * If button exits in map => returns that button
    * Else => Creates button, puts it in the map and returns it
    * */
    fun getButton(buttonText: String, buttonMap: MutableMap<String, ToggleButton>): ToggleButton {

        val newButton = buttonMap.get(buttonText) ?: createNewButton(buttonText)

        if (!buttonMap.containsKey(buttonText)) {
            buttonMap[buttonText] = newButton
        }

        return newButton
    }

    /*
    * Creates new button and adds on click listeners.
    * @param {String} buttonText The buttons text and the word to send to the model.
    * @return {ToggleButton} A button that represents the given text.
    * */
    private fun createNewButton(buttonText: String): ToggleButton {

        return ToggleButton(applicationContext).apply {

            setButtonText(buttonText)

            minWidth = 0
            minimumWidth = 0
            minHeight = 0
            minimumHeight = 0

            setPadding(35, 35, 35, 35)

            // Set constraints
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT).apply {

                    setMargins(1, 0, 0, 0)
                }

            setOnClickListener {
                SpeechToTextEngine.notifyModel(buttonText)
            }
        }
    }

    /*
    * Parses buttonText and sets it's text appropriately.
    * */
    private fun ToggleButton.setButtonText(buttonText: String) {

        val parsedButtonText = getButtonText(Feature.valueOf(buttonText))

        this.setTextOff(parsedButtonText)
        this.setTextOn(parsedButtonText)

        this.setText(parsedButtonText)
    }

    fun showChildHeaders(Word: Word) {

        val childHeaders = headersToCommands.get(Word.header)

        // Show child headers
        childHeaders?.forEach {

            // Create button with text set to 'it'
            // Add button to linear layout
            // Add click listeners to report to Model it's text content

            val buttonText = it

            val newButton = getButton(buttonText, dynamicButtons)

            headerCreaterLayout.addView(newButton)
        }

    }

    fun showParentHeaders(Word: Word) {
        // Show parents headers in HeaderTextView too
        headerView.showParentHeaders(Word)
    }

    /*
    * Reset UI:
    * 1. Un-select both devices
    * 2. Remove all buttons from top
    * 3. Remove all buttons from parameter bar
    * 4. Remove all buttons from HeaderTextView
    * */
    fun reset(word: Word) {

        // Reset device buttons
        deviceButtons.forEach {
            unSelectDevice(it.key)
        }

        // Remove child headers
        headerCreaterLayout.removeAllViews()

        // Remove parent headers
        headerView.clear()


        resetCommand(word)
    }

    /*
    * Resets all buttons.
    * Removes parameter bar
    * */
    fun resetCommand(word: Word) {

        // Reset dynamic buttons
        dynamicButtons.forEach {
            it.value.isChecked = false
        }

        // Reset static buttons
        staticButtons.forEach {
            (it.value as ToggleButton).isChecked = false
        }

        // Remove parameter bars
        adaptiveParameterBars.forEach {
            it.value.hide()
        }
    }

    /*
    * Checks if the given parameter is in bounds.
    * @param selectedCommand {Word} The command that the user has previously selected.
    * @param selectedParameter {Word} The parameter that the user has just selected.
    * @return {Boolean} Value of in bounds check.
    * */
    fun isParameterInBounds(selectedParameter: Word): Boolean {

        val parameterDetails = getParameterDetails(selectedParameter)

        return parameterDetails!!.isInBounds(selectedParameter.value)
    }

    /*
    * Selects the given parameter from it's adaptive bar for the given command.
    * @param selectedCommand {Word} The command that the user has previously selected.
    * @param selectedParameter {Word} The parameter that the user has just selected.
    * */
    fun selectParameter(selectedParameter: Word) {

        val parameterDetails = getParameterDetails(selectedParameter)

        parameterDetails?.setCurrentSelection(selectedParameter)

        val adaptiveParameterBar = adaptiveParameterBars[parameterDetails?.adaptiveParameterBarType]
        adaptiveParameterBar?.select(selectedParameter.value)

        // Change button text
        val buttonText = getButtonText(selectedParameter.feature)
        (staticButtons.get(selectedParameter.feature) as ToggleButton)?.apply {

            setTextOff(buttonText)
            setTextOn(buttonText)

            setText(buttonText)
        }
    }


    private fun getParameterDetails(word: Word): ParameterDetails? {

        return when(word.deviceType) {
            DeviceType.CAMERA -> MasterCamera.featuresToParameters.get(word.feature)
            DeviceType.GIMBAL -> MasterGimbal.featuresToParameters.get(word.feature)
            else -> null
        }
    }
}
