    package com.example.speechtotext

import com.example.speechtotext.devicecontroller.Camera
import com.example.speechtotext.devicecontroller.Gimbal
import kotlin.collections.ArrayList

object Model: SpeechToTextEngineObserver{

    private var observers: ArrayList<FSMObserver> = ArrayList()

    fun addObserver(observer: FSMObserver) {

        this.observers.add(observer)
    }

    fun removeObserver(observer: FSMObserver) {

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

        this.notifyObservers(word)
    }


}

interface SpeechToTextEngineObserver {

    fun newWord(word: String)
}

interface Observer {

    fun newWord(word: String)
    fun getUIController(): UIController?
    fun getWord(): Word
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
    val callbacks: Array<(optionalWord: Word) -> Unit>,
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
    fun runCallbacks(word: Word) {

        this.callbacks.forEach {
            it(word)
        }
    }


}
