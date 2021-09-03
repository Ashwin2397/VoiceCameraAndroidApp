    package com.assistivetechlab.speechtotext

import com.assistivetechlab.speechtotext.devicecontroller.Camera
import com.assistivetechlab.speechtotext.devicecontroller.Gimbal

    //
//object Model: SpeechToTextEngineObserver{
//
//    private var observers: ArrayList<FSMObserver> = ArrayList()
//
//    fun addObserver(observer: FSMObserver) {
//
//        this.observers.add(observer)
//    }
//
//    fun removeObserver(observer: FSMObserver) {
//
//        this.observers.remove(observer)
//    }
//
//    fun notifyObservers(word: String) {
//
//        this.observers.forEach {
//            it.Word(word)
//        }
//    }
//
//    fun removeAllObservers() {
//
//        observers.clear()
//    }
//
//    override fun Word(word: String) {
//
//        this.notifyObservers(word)
//    }
//
//
//}

data class Word(
    val value: String,
    var feature: Feature,
    val header: Header,
    val inputType: InputType,
    val deviceType: DeviceType,
    val parents: List<Header>
)

interface SpeechToTextEngineObserver {

    fun Word(word: String)
}

interface Observer {

    fun Word(word: String)
    fun getUIController(): UIController?
    fun getWord(): Word
    fun setUIController(uiController: UIController)
    fun getControlParameters(): MutableList<String>
    fun onCommandClick()
    fun onParameterClick(parameter: String)
    fun setGimbalController(gimbalController: Gimbal)
    fun setCameraController(cameraController: Camera)

}

class State(
    val state: Int,
    val input: Map<InputType, Int>,
    val callbacks: List<(optionalWord: Word) -> Unit>,
    ){

    /*
    * Returns the new state that based on the given input type
	* @param {String} inputType The inputType of the new word that is supplied by the observer
	* @return new state
    */
    fun Word(inputType: InputType): Int{

        return this.input[inputType] ?: -1
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
