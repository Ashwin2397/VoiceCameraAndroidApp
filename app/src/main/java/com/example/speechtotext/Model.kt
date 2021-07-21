package com.example.speechtotext

import android.widget.TextView
import com.example.speechtotext.devicecontroller.Camera
import com.example.speechtotext.devicecontroller.Gimbal
import com.example.speechtotext.devicecontroller.PilotflyGimbalController
import kotlin.collections.ArrayList


fun main() {
//    val a = A()
//    val b = B()
//
//    val cbs:Array<() -> Unit> = arrayOf(a::ruinMe, a::runMe)
//
//
//
//    b.ohman(cbs)


//    zoomObserver.states[0]!!.runCallbacks()

    if (firstCommands["zoom"] != null && firstCommands["zoom"]?.value == "too") {

        print("hi")
    }



}


open class A {

    fun runMe(): Unit {
        print("Hi there ")
    }

    fun ruinMe(): Unit {
        print("Hi I am ruined ")
    }
}

class B {

    fun ohman(cbs: Array<() -> Unit>){

        cbs.forEach {

            it()

        }

    }
}
class Model(val textView: TextView): SpeechToTextEngineObserver{

    private var observers: ArrayList<Observer> = ArrayList()

    fun addObserver(observer: Observer) {

        this.observers.add(observer)
    }

    fun removeObserver(observer: Observer) {

        this.observers.remove(observer)
    }

    fun notifyObservers(word: String) {

        this.observers.forEach {
            it.newWord(word)
        }
    }

    fun removeAllObservers() {

        observers.clear()
    }
    override fun newWord(word: String) {


        textView.setText(word)
        this.notifyObservers(word)
    }


}

interface SpeechToTextEngineObserver {

    fun newWord(word: String)
}

interface Observer {

    fun newWord(word: String)
    fun setUIController(uiController: UIController)
    fun getControlParameters(): MutableList<String>
    fun onCommandClick()
    fun onParameterClick(parameter: String)
    fun setGimbalController(gimbalController: Gimbal)
    fun setCameraController(cameraController: Camera)

}

data class Word(
    val value: String,
    val feature: Feature,
    val inputType: InputType,
    val deviceType: DeviceType
)

class State(
    val state: Int,
    val input: Map<InputType, Int>,
    val callbacks: Array<() -> Unit>,

){

    /*
    * Returns the new state that based on the given input type
	* @param {String} inputType The inputType of the new word that is supplied by the observer
	* @return new state
    */
    fun newWord(inputType: InputType): Int{

        return if (this.input[inputType] != null) this.input[inputType]!! else this.state
    }

    /*
	* Runs all callbacks once state has been attained for the first time
    */
    fun runCallbacks() {

        this.callbacks.forEach {
            it()
        }
    }


}
//
//class ZoomObserver(val textView: TextView): Observer{
//
//    val states = mapOf<Int, State>(
//        0 to State(0, mapOf(InputType.COMMAND_1 to 1), arrayOf(this::reset), arrayOf()),
//    )
//
//    val consecutiveWords = mapOf<String, Word>(
//        "point" to Word("point", Feature.ANY, InputType.COMMAND_2, DeviceType.ANY)
//    )
//
//    val command = Feature.ZOOM
//    var currentState = 0
//    public val finalState = 4
//    var currentWord = Word("", Feature.UNDEFINED, InputType.UNDEFINED, DeviceType.UNDEFINED)
//
//    override fun newWord(word: String) {
//
//        val parsedWord = this.parseWord(word)
//
//        this.currentWord = parsedWord // Save word to be used by cb
//
//        val currentStateObject = this.states[this.currentState]
//
//        val newState = currentStateObject!!.newWord(parsedWord.inputType)
//
//        val newStateObject = this.states[newState]
//
//        val hasStateChanged = newState != this.currentState
//        if (hasStateChanged) {
//
//            // Run final cb before state changes
//            currentStateObject.runFinalCallbacks();
//
//            // Run cb in new state
//            newStateObject!!.runCallbacks();
//
//        }
//
//        this.currentState = newState;
//
//    }
//
//    fun parseWord(word: String): Word {
//
//        var parsedWord = Word("", Feature.UNDEFINED, InputType.UNDEFINED, DeviceType.UNDEFINED);
//
//        val isDefinedCommand = firstCommands[word] != null
//        val isOurCommand = isDefinedCommand && firstCommands[word]?.value == this.command
//        val isConsecutiveWord = this.consecutiveWords[word] != null
//        val isOtherCommand = isDefinedCommand && firstCommands[word]?.value != this.command;
//        val isNumericalParameter = numericalParameters[word] != null
//
//        if (isOurCommand) {
//            parsedWord = firstCommands[word]!!
//        }else if (isConsecutiveWord) {
//            parsedWord = this.consecutiveWords[word]!!
//        }else if (isOtherCommand) {
//            parsedWord = Word("", Feature.ANY, InputType.OTHER_COMMAND, DeviceType.ANY)
//        }else if (isNumericalParameter) {
//            parsedWord = numericalParameters[word]!!
//        }
//
//        return parsedWord
//    }
//
//    fun showZoomParameters() {}
//
//    fun reset() {
//
//        this.currentState = 0;
////        document.getElementById("zoom").innerHTML += " CLEAR ";
//    }
//
//    fun zoom() {
//        this.textView.setText((this.textView.text.)
//    }
//
////    fun wait() {
//
////        setTimeout(observer.isReset.bind(observer), 800);
////    }
//
//    fun isReset() {
//
//        // Only reset if we are waiting for "point"
//        if (this.currentState == 2) {
//            this.reset()
//        }
//    }
//
//}
