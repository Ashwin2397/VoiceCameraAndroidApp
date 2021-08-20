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

    var selectedCommand:Word? = null

    val selectedColor = "#25c433"
    val unselectedColor = "#FF6200EE"

    var dynamicButtons = mutableMapOf<String, ToggleButton>(

    )

    // Move this to the master controller
    var featuresToParameters = mapOf<Feature, ParameterDetails>(
        Feature.ZOOM to ParameterDetails(AdaptiveParameterBarType.GAUGE, numericalParameters = IntRange(0, 10), currentNumericalSelection = 0),
        Feature.MODE to ParameterDetails(AdaptiveParameterBarType.BUTTON, stringParameters = listOf("movie", "photo"), currentStringSelection =  "photo"),
        Feature.FOCUS to ParameterDetails(AdaptiveParameterBarType.BUTTON, stringParameters = listOf("point", "face", "spot"), currentStringSelection = "point"),
        Feature.LEFT to ParameterDetails(AdaptiveParameterBarType.GAUGE, numericalParameters = IntRange(0, 10), currentNumericalSelection = 0),
        Feature.RIGHT to ParameterDetails(AdaptiveParameterBarType.GAUGE, numericalParameters = IntRange(0, 10), currentNumericalSelection = 0),
        Feature.UP to ParameterDetails(AdaptiveParameterBarType.GAUGE, numericalParameters = IntRange(0, 10), currentNumericalSelection = 0),
        Feature.DOWN to ParameterDetails(AdaptiveParameterBarType.GAUGE, numericalParameters = IntRange(0, 10), currentNumericalSelection = 0),
        Feature.MOVE to ParameterDetails(AdaptiveParameterBarType.GAUGE, numericalParameters = IntRange(0, 10), currentNumericalSelection = 0),
    )

    val adaptiveParameterBars = mapOf<AdaptiveParameterBarType, AdaptiveParameterBar>(
        AdaptiveParameterBarType.BUTTON to AdaptiveParameterButtonBar(parameterCreaterLayout, applicationContext),
        AdaptiveParameterBarType.GAUGE to AdaptiveParameterGaugeBar(parameterCreaterLayout, applicationContext),
    )

    init {

        // Add on click listeners for device buttons
        deviceButtons.forEach {
            val deviceType = it.key.toString()

            it.value.setOnClickListener {
                SpeechToTextEngine.notifyModel(deviceType)
            }
        }

        // Add on click listeners for static buttons
        staticButtons.forEach {

            val feature = it.key.toString()

            it.value.setOnClickListener {
                SpeechToTextEngine.notifyModel(feature)
            }
        }
    }
    /*
    * Shows parameters based on the value of the Word object.
    * If command has parameters => Show parameters
    * Else => Schedule reset 
    * @param {Word} word Contains the key to retrieve it's parameter details.
    * */
    fun showParameters(selectedCommand: Word) {

        val parameterDetails = featuresToParameters.get(selectedCommand.feature)

        val adaptiveParameterBar = adaptiveParameterBars.get(parameterDetails!!.adaptiveParameterBarType)
        adaptiveParameterBar?.show(parameterDetails, SpeechToTextEngine::notifyModel)
    }

    /*
    * Checks if the given command has parameters or not.
    * @param {Word} selectedCommand The command to check.
    * */
    fun hasParameters(selectedCommand: Word): Boolean {

        val parameterDetails = featuresToParameters.get(selectedCommand.feature)
        return parameterDetails != null
    }

    /*
    * If command is static => select command from static map
    * else => select command from list of displayed buttons
    * @param {Word} Word Contains the key to retrieve the button.
    * */
    fun selectCommand(newCommand: Word) {

        val button = (staticButtons[newCommand.feature] ?: getDynamicButton(newCommand.value)) as ToggleButton
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
    * Fetches dynamic button from it's map.
    * If button exits in map => returns that button
    * Else => Creates button, puts it in the map and returns it
    * */
    fun getDynamicButton(buttonText: String): ToggleButton {

        val newButton = dynamicButtons.get(buttonText) ?: ToggleButton(applicationContext).apply {

            setTextOff(buttonText)
            setTextOn(buttonText)

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

    fun showChildHeaders(Word: Word) {

        val childHeaders = headersToCommands.get(Word.header)

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

    fun showParentHeaders(Word: Word) {
        // Show parents headers in HeaderTextView too
        headerTextView.showParentHeaders(Word)
    }

    /*
    * Reset UI:
    * 1. Un-select both devices
    * 2. Remove all buttons from top
    * 3. Remove all buttons from parameter bar
    * 4. Remove all buttons from HeaderTextView
    * */
    fun reset(Word: Word) {

        // Reset device buttons
        deviceButtons.forEach {
            unSelectDevice(it.key)
        }

        // Remove child headers
        headerCreaterLayout.removeAllViews()

        // Remove parent headers
        headerTextView.clear()

        // Remove parameter bars
        adaptiveParameterBars.forEach {
            it.value.hide()
        }

        // Reset static buttons
        staticButtons.forEach {
            (it.value as ToggleButton).isChecked = false
        }

        // Reset dynamic buttons
        dynamicButtons.forEach {
            it.value.isChecked = false
        }
    }

    /*
    * Checks if the given parameter is in bounds.
    * @param selectedCommand {Word} The command that the user has previously selected.
    * @param selectedParameter {Word} The parameter that the user has just selected.
    * @return {Boolean} Value of in bounds check.
    * */
    fun isParameterInBounds(selectedParameter: Word): Boolean {

        val parameterDetails = featuresToParameters[selectedCommand?.feature]

        return parameterDetails!!.isInBounds(selectedParameter.value)
    }

    /*
    * Selects the given parameter from it's adaptive bar for the given command.
    * @param selectedCommand {Word} The command that the user has previously selected.
    * @param selectedParameter {Word} The parameter that the user has just selected.
    * */
    fun selectParameter(selectedParameter: Word) {

        val parameterDetails = featuresToParameters[selectedCommand?.feature]

        parameterDetails?.setCurrentSelection(selectedParameter)

        val adaptiveParameterBar = adaptiveParameterBars[parameterDetails?.adaptiveParameterBarType]
        adaptiveParameterBar?.select(selectedParameter.value)
    }


}
