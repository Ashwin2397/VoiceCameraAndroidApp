package com.example.speechtotext

import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.TextView
import com.example.speechtotext.devicecontroller.*
import java.util.Timer
import kotlin.concurrent.schedule

/*
* NOTE:
* - The textview is for debugging purposes only
* - REFACTOR_MULTI_CMDS
*   -
* */
//
//class ShootObserver: Observer {
//
//    private val TAG = "SHOOT_OBSERVER"
//
//    private var cameraController: Camera? = null
//    private var gimbalController: Gimbal? = null
//    var uiController:UIController? = null
//
//
//    val parameters = mutableListOf<String>()
//
//    val command = Word("shoot", Feature.SHOOT, InputType.COMMAND_1, DeviceType.CAMERA)
//    // Change to list
//
//    var fsm = FSM(
//        consecutiveWords = mapOf<String, Word>(),
//        command = this.command
//    )
//    var currentState = 0
//    var currentWord = Word("", Feature.UNDEFINED, InputType.UNDEFINED, DeviceType.UNDEFINED)
//
//    /*
//    * Run only after setting uiController and cameraController/gimbalController.
//    * Run everytime after any controllers have been changed.
//    * */
//    fun setFsmStates() {
//
//        this.fsm.states = mapOf(
//            0 to State(0, mapOf(InputType.COMMAND_1 to 1), arrayOf(this::reset)),
//            1 to State(1, mapOf(), arrayOf(uiController!!::selectCommand,
//                cameraController!!::shoot, this::reset)),
//        )
//
//    }
//
//    override fun setCameraController(cameraController: Camera) {
//        this.cameraController = cameraController
//    }
//
//    fun getCameraController(): Camera? {
//        return this.cameraController
//    }
//
//    override fun setGimbalController(gimbalController: Gimbal) {
//        this.gimbalController = gimbalController
//    }
//
//    fun getGimbalController(): Gimbal? {
//        return this.gimbalController
//    }
//
//    override fun setUIController(uiController: UIController) {
//
//        this.uiController = uiController
//    }
//
//    override fun getUIController(): UIController? {
//        return this.uiController
//    }
//
//
//    override fun getControlParameters(): MutableList<String> {
//
//        return this.parameters
//    }
//
//    override fun newWord(word: String) {
//
//       fsm.newWord(word)
//
//    }
//
//    fun reset(optionalWord: Word?) {
//
////        timer.schedule(TimerTask {}, 100)
//        fsm.currentState = 0
//
//        // Does'nt work unless you run on ui thread, which I have not figured out yet
//        val mainLooper = Looper.getMainLooper()
//
//        if (fsm.currentWord.inputType == InputType.OTHER_COMMAND) {
//            uiController!!.reset()
//        }else {
//
//            Thread(Runnable {
//
//                Timer("Reset UI", false).schedule(1000) {
//
//                    Handler(mainLooper).post {
//
//                        uiController!!.reset()
//                    }
//                }
//            }).start()
//        }
//    }
//
//    fun shoot() {
//        Log.d("SHOOT_OBSERVER", "SHOOT!!!!")
//    }
//
//    override fun onCommandClick() {
//
//        newWord(this.command.value)
//    }
//
//    override fun onParameterClick(parameter: String) {
//
//        newWord(parameter)
//
//    }
//
//}
//
//class ZoomObserver: Observer {
//    private val TAG = "ZOOM_OBSERVER"
//
//    private var cameraController: Camera? = null
//    private var gimbalController: Gimbal? = null
//    var uiController:UIController? = null
//
//    val states by lazy {
//        mapOf<Int, State>(
//            0 to State(0, mapOf(InputType.COMMAND_1 to 1), arrayOf(this::reset), this as Observer),
//            1 to State(1, mapOf(InputType.NUMERICAL_PARAMETER to 2, InputType.OTHER_COMMAND to 0), arrayOf(uiController!!::selectCommand, uiController!!::showParameterButtonBar), this as Observer),
//            2 to State(2, mapOf(), arrayOf(this::selectParameter, this::zoom, this::reset), this as Observer),
//        )
//    }
//
//
//    val parameters = mutableListOf<String>("0", "25", "50", "75", "100")
//
//    val command = Word("zoom", Feature.ZOOM, InputType.COMMAND_1, DeviceType.CAMERA)
//
//    var fsm = FSM(
//        consecutiveWords = mapOf<String, Word>(),
//        command = this.command
//    )
//
//    var consecutiveWords = mapOf<String, Word>()
//
//    var currentState = 0
//    var currentWord = Word("", Feature.UNDEFINED, InputType.UNDEFINED, DeviceType.UNDEFINED)
//
//    override fun setCameraController(cameraController: Camera) {
//        this.cameraController = cameraController
//    }
//
//    override fun setGimbalController(gimbalController: Gimbal) {
//        this.gimbalController = gimbalController
//    }
//
//    override fun setUIController(uiController: UIController) {
//
//        this.uiController = uiController
//    }
//
//    override fun getUIController(): UIController? {
//        return this.uiController
//    }
//    override fun getControlParameters(): MutableList<String> {
//
//        return this.parameters
//    }
//
//    override fun newWord(word: String) {
//
//        // Log.d(this.TAG, word)
//        val parsedWord = this.parseWord(word)
//        this.currentWord = parsedWord // Save word to be used by cb
//
//        val currentStateObject = this.states[this.currentState]
//
//        val newState = currentStateObject!!.newWord(parsedWord.inputType)
//
//        changeState(newState)
//
//    }
//
//    private fun selectParameter(optionalWord: Word) {
//        this.uiController!!.selectParameter(optionalWord)
//    }
//
//    fun changeState(newState: Int) {
//
//        val currentStateObject = this.states[this.currentState]
//
//        val newStateObject = this.states[newState]
//
//        val hasStateChanged = newState != this.currentState
//        if (hasStateChanged) {
//
//            this.currentState = newState;
//
//            newStateObject!!.runCallbacks();
//
//        }
//
//
//    }
//    fun parseWord(word: String): Word {
//
//        var parsedWord = Word("", Feature.UNDEFINED, InputType.UNDEFINED, DeviceType.UNDEFINED);
//
//        val isDefinedCommand = firstCommands[word] != null
//        val isOurCommand = isDefinedCommand && firstCommands[word]?.value == this.command.value
//        val isConsecutiveWord = this.consecutiveWords[word] != null
//        val isOtherCommand = isDefinedCommand && firstCommands[word]?.value != this.command.value;
////        val isNumericalParameter = numericalParameters[word] != null
//        var isNumericalParameter = false
//
//        try {
//            word.toFloat()
//            isNumericalParameter = true
//        }catch (e: NumberFormatException) {
//            isNumericalParameter = false
//        }
//
//        if (isOurCommand) {
//            parsedWord = firstCommands[word]!!
//        }else if (isConsecutiveWord) {
//            parsedWord = this.consecutiveWords[word]!!
//        }else if (isOtherCommand) {
//            parsedWord = Word("", Feature.ANY, InputType.OTHER_COMMAND, DeviceType.ANY)
//        }else if (isNumericalParameter) {
////            parsedWord = numericalParameters[word]!!
//            parsedWord = Word(word, Feature.MODE, InputType.NUMERICAL_PARAMETER, DeviceType.CAMERA)
//        }
//
//        return parsedWord
//    }
//
//    fun reset(optionalWord: Word) {
//
////        timer.schedule(TimerTask {}, 100)
//        this.currentState = 0
//
//        // Does'nt work unless you run on ui thread, which I have not figured out yet
//        val mainLooper = Looper.getMainLooper()
//
//        if (this.currentWord.inputType == InputType.OTHER_COMMAND) {
//            uiController!!.reset()
//        }else {
//
//            Thread(Runnable {
//
//                Timer("Reset UI", false).schedule(1000) {
//
//                    Handler(mainLooper).post {
//
//                        uiController!!.reset()
//                    }
//                }
//            }).start()
//        }
//    }
//
//    fun zoom(optionalWord: Word) {
//        Log.d(TAG, "ZOOM: " + this.currentWord.value)
//
//        this.cameraController?.setZoom(this.currentWord.value)
//    }
//
//    override fun onCommandClick() {
//
//        newWord(this.command.value)
//    }
//
//    override fun onParameterClick(parameter: String) {
//
//        newWord(parameter)
//
//    }
//
//
//}
//
//
//class ApertureObserver: Observer {
//    private val TAG = "APERTURE_OBSERVER"
//
//    private var cameraController: Camera? = null
//    private var gimbalController: Gimbal? = null
//
//    var uiController:UIController? = null
//
//    val states by lazy {
//        mapOf<Int, State>(
//            0 to State(0, mapOf(InputType.COMMAND_1 to 1), arrayOf(this::reset)),
//            1 to State(
//                1,
//                mapOf(InputType.NUMERICAL_PARAMETER to 2, InputType.OTHER_COMMAND to 0),
//                arrayOf(uiController!!::selectCommand, uiController!!::showParameterButtonBar)
//            ),
//            2 to State(2, mapOf(), arrayOf(this::selectParameter, this::aperture, this::reset)),
//        )
//    }
//
//    val parameters = mutableListOf<String>("3.4", "4.0", "4.5", "5.0", "5.6", "6.3", "7.1", "8.0")
//    val consecutiveWords = mapOf<String, Word>()
//
//    val command = Word("aperture", Feature.APERTURE, InputType.COMMAND_1, DeviceType.CAMERA)
//    var currentState = 0
//    var currentWord = Word("", Feature.UNDEFINED, InputType.UNDEFINED, DeviceType.UNDEFINED)
//
//    override fun setCameraController(cameraController: Camera) {
//        this.cameraController = cameraController
//    }
//
//    override fun setGimbalController(gimbalController: Gimbal) {
//        this.gimbalController = gimbalController
//    }
//
//    override fun setUIController(uiController: UIController) {
//
//        this.uiController = uiController
//    }
//    override fun getUIController(): UIController? {
//        return this.uiController
//    }
//    override fun getControlParameters(): MutableList<String> {
//
//        return this.parameters
//    }
//
//    private fun selectParameter() {
//        this.uiController!!.selectParameter(false, this.currentWord.value)
//    }
//
//    override fun newWord(word: String) {
//
//        // Log.d(this.TAG, word)
//        val parsedWord = this.parseWord(word)
//        this.currentWord = parsedWord // Save word to be used by cb
//
//        val currentStateObject = this.states[this.currentState]
//
//        val newState = currentStateObject!!.newWord(parsedWord.inputType)
//
//        changeState(newState)
//
//    }
//
//    fun changeState(newState: Int) {
//
//        val currentStateObject = this.states[this.currentState]
//
//        val newStateObject = this.states[newState]
//
//        val hasStateChanged = newState != this.currentState
//        if (hasStateChanged) {
//
//            this.currentState = newState;
//
//            newStateObject!!.runCallbacks();
//
//        }
//
//
//    }
//    fun parseWord(word: String): Word {
//
//        var parsedWord = Word("", Feature.UNDEFINED, InputType.UNDEFINED, DeviceType.UNDEFINED);
//
//        val isDefinedCommand = firstCommands[word] != null
//        val isOurCommand = isDefinedCommand && firstCommands[word]?.value == this.command.value
//        val isConsecutiveWord = this.consecutiveWords[word] != null
//        val isOtherCommand = isDefinedCommand && firstCommands[word]?.value != this.command.value;
////        val isNumericalParameter = numericalParameters[word] != null
//        var isNumericalParameter = false
//
//        try {
//            word.toFloat()
//            isNumericalParameter = true
//        }catch (e: NumberFormatException) {
//            isNumericalParameter = false
//        }
//
//        if (isOurCommand) {
//            parsedWord = firstCommands[word]!!
//        }else if (isConsecutiveWord) {
//            parsedWord = this.consecutiveWords[word]!!
//        }else if (isOtherCommand) {
//            parsedWord = Word("", Feature.ANY, InputType.OTHER_COMMAND, DeviceType.ANY)
//        }else if (isNumericalParameter) {
////            parsedWord = numericalParameters[word]!!
//            parsedWord = Word(word, Feature.MODE, InputType.NUMERICAL_PARAMETER, DeviceType.CAMERA)
//        }
//
//        return parsedWord
//    }
//
//    fun reset() {
//
////        timer.schedule(TimerTask {}, 100)
//        this.currentState = 0
//
//        // Does'nt work unless you run on ui thread, which I have not figured out yet
//        val mainLooper = Looper.getMainLooper()
//
//        if (this.currentWord.inputType == InputType.OTHER_COMMAND) {
//            uiController!!.reset()
//        }else {
//
//            Thread(Runnable {
//
//                Timer("Reset UI", false).schedule(1000) {
//
//                    Handler(mainLooper).post {
//
//                        uiController!!.reset()
//                    }
//                }
//            }).start()
//        }
//    }
//
//    fun aperture() {
//        Log.d(TAG, "APERTURE: " + this.currentWord.value)
//
//        this.cameraController?.setAperture(this.currentWord.value)
//    }
//
//    override fun onCommandClick() {
//
//        newWord(this.command.value)
//    }
//
//    override fun onParameterClick(parameter: String) {
//
//        newWord(parameter)
//
//    }
//
//
//}
//
//
//class FocusObserver: Observer {
//    private val TAG = "FOCUS_OBSERVER"
//
//    private var cameraController: Camera? = null
//    private var gimbalController: Gimbal? = null
//    var uiController:UIController? = null
//
//    val states by lazy {
//        mapOf<Int, State>(
//            0 to State(0, mapOf(InputType.COMMAND_1 to 1), arrayOf(this::reset)),
//            1 to State(
//                1,
//                mapOf(InputType.PARAMETER to 2, InputType.OTHER_COMMAND to 0),
//                arrayOf(uiController!!::selectCommand, uiController!!::showParameterButtonBar)
//            ),
//            2 to State(2, mapOf(), arrayOf(this::selectParameter, this::focus, this::reset)),
//        )
//    }
//
//    val parameters = mutableListOf<String>("point", "face", "spot")
//    val consecutiveWords = mapOf<String, Word>(
//        "point" to Word("point", Feature.FOCUS, InputType.PARAMETER, DeviceType.CAMERA),
//        "face" to Word("face", Feature.FOCUS, InputType.PARAMETER, DeviceType.CAMERA),
//        "spot" to Word("spot", Feature.FOCUS, InputType.PARAMETER, DeviceType.CAMERA),
//        )
//
//    val command = Word("focus", Feature.FOCUS, InputType.COMMAND_1, DeviceType.CAMERA)
//    var currentState = 0
//    var currentWord = Word("", Feature.UNDEFINED, InputType.UNDEFINED, DeviceType.UNDEFINED)
//
//    override fun setCameraController(cameraController: Camera) {
//        this.cameraController = cameraController
//    }
//
//    override fun setGimbalController(gimbalController: Gimbal) {
//        this.gimbalController = gimbalController
//    }
//
//    override fun setUIController(uiController: UIController) {
//
//        this.uiController = uiController
//    }
//    override fun getUIController(): UIController? {
//        return this.uiController
//    }
//    override fun getControlParameters(): MutableList<String> {
//
//        return this.parameters
//    }
//
//    private fun selectParameter() {
//        this.uiController!!.selectParameter(false, this.currentWord.value)
//    }
//
//    override fun newWord(word: String) {
//
//        // Log.d(this.TAG, word)
//        val parsedWord = this.parseWord(word)
//        this.currentWord = parsedWord // Save word to be used by cb
//
//        val currentStateObject = this.states[this.currentState]
//
//        val newState = currentStateObject!!.newWord(parsedWord.inputType)
//
//        changeState(newState)
//
//    }
//
//    fun changeState(newState: Int) {
//
//        val currentStateObject = this.states[this.currentState]
//
//        val newStateObject = this.states[newState]
//
//        val hasStateChanged = newState != this.currentState
//        if (hasStateChanged) {
//
//            this.currentState = newState;
//
//            newStateObject!!.runCallbacks();
//
//        }
//
//
//    }
//    fun parseWord(word: String): Word {
//
//        var parsedWord = Word("", Feature.UNDEFINED, InputType.UNDEFINED, DeviceType.UNDEFINED);
//
//        val isDefinedCommand = firstCommands[word] != null
//        val isOurCommand = isDefinedCommand && firstCommands[word]?.value == this.command.value
//        val isConsecutiveWord = this.consecutiveWords[word] != null
//        val isOtherCommand = isDefinedCommand && firstCommands[word]?.value != this.command.value;
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
//    fun reset() {
//
////        timer.schedule(TimerTask {}, 100)
//        this.currentState = 0
//
//        // Does'nt work unless you run on ui thread, which I have not figured out yet
//        val mainLooper = Looper.getMainLooper()
//
//        if (this.currentWord.inputType == InputType.OTHER_COMMAND) {
//            uiController!!.reset()
//        }else {
//
//            Thread(Runnable {
//
//                Timer("Reset UI", false).schedule(1000) {
//
//                    Handler(mainLooper).post {
//
//                        uiController!!.reset()
//                    }
//                }
//            }).start()
//        }
//    }
//
//    fun focus() {
//        Log.d(TAG, "FOCUS: " + this.currentWord.value)
//
//        this.cameraController?.setFocusType(this.currentWord.value)
//    }
//
//    override fun onCommandClick() {
//
//        newWord(this.command.value)
//    }
//
//    override fun onParameterClick(parameter: String) {
//
//        newWord(parameter)
//
//    }
//
//
//}
//
//
//class ModeObserver: Observer {
//    private val TAG = "MODE_OBSERVER"
//
//    private var cameraController: Camera? = null
//    private var gimbalController: Gimbal? = null
//    var uiController:UIController? = null
//
//    val states by lazy {
//        mapOf<Int, State>(
//            0 to State(0, mapOf(InputType.COMMAND_1 to 1), arrayOf(this::reset)),
//            1 to State(
//                1,
//                mapOf(InputType.PARAMETER to 2, InputType.OTHER_COMMAND to 0),
//                arrayOf(uiController!!::selectCommand, uiController!!::showParameterButtonBar)
//            ),
//            2 to State(2, mapOf(), arrayOf(this::selectParameter, this::mode, this::reset)),
//        )
//    }
//
//    val parameters = mutableListOf<String>("photo", "movie")
//    val consecutiveWords = mapOf<String, Word>(
//        "photo" to Word("photo", Feature.MODE, InputType.PARAMETER, DeviceType.CAMERA),
//        "movie" to Word("movie", Feature.MODE, InputType.PARAMETER, DeviceType.CAMERA),
//    )
//
//    val command = Word("mode", Feature.MODE, InputType.COMMAND_1, DeviceType.CAMERA)
//    var currentState = 0
//    var currentWord = Word("", Feature.UNDEFINED, InputType.UNDEFINED, DeviceType.UNDEFINED)
//
//    override fun setCameraController(cameraController: Camera) {
//        this.cameraController = cameraController
//    }
//
//    override fun setGimbalController(gimbalController: Gimbal) {
//        this.gimbalController = gimbalController
//    }
//
//    override fun setUIController(uiController: UIController) {
//
//        this.uiController = uiController
//    }
//    override fun getUIController(): UIController? {
//        return this.uiController
//    }
//    override fun getControlParameters(): MutableList<String> {
//
//        return this.parameters
//    }
//
//    private fun selectParameter() {
//        this.uiController!!.selectParameter(false, this.currentWord.value)
//    }
//
//    override fun newWord(word: String) {
//
//        // Log.d(this.TAG, word)
//        val parsedWord = this.parseWord(word)
//        this.currentWord = parsedWord // Save word to be used by cb
//
//        val currentStateObject = this.states[this.currentState]
//
//        val newState = currentStateObject!!.newWord(parsedWord.inputType)
//
//        changeState(newState)
//
//    }
//
//    fun changeState(newState: Int) {
//
//        val currentStateObject = this.states[this.currentState]
//
//        val newStateObject = this.states[newState]
//
//        val hasStateChanged = newState != this.currentState
//        if (hasStateChanged) {
//
//            this.currentState = newState;
//
//            newStateObject!!.runCallbacks();
//
//        }
//
//
//    }
//    fun parseWord(word: String): Word {
//
//        var parsedWord = Word("", Feature.UNDEFINED, InputType.UNDEFINED, DeviceType.UNDEFINED);
//
//        val isDefinedCommand = firstCommands[word] != null
//        val isOurCommand = isDefinedCommand && firstCommands[word]?.value == this.command.value
//        val isConsecutiveWord = this.consecutiveWords[word] != null
//        val isOtherCommand = isDefinedCommand && firstCommands[word]?.value != this.command.value;
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
//    fun reset() {
//
////        timer.schedule(TimerTask {}, 100)
//        this.currentState = 0
//
//        // Does'nt work unless you run on ui thread, which I have not figured out yet
//        val mainLooper = Looper.getMainLooper()
//
//        if (this.currentWord.inputType == InputType.OTHER_COMMAND) {
//            uiController!!.reset()
//        }else {
//
//            Thread(Runnable {
//
//                Timer("Reset UI", false).schedule(1000) {
//
//                    Handler(mainLooper).post {
//
//                        uiController!!.reset()
//                    }
//                }
//            }).start()
//        }
//    }
//
//    fun mode() {
//        Log.d(TAG, "MODE: " + this.currentWord.value)
//    }
//
//    override fun onCommandClick() {
//
//        newWord(this.command.value)
//    }
//
//    override fun onParameterClick(parameter: String) {
//
//        newWord(parameter)
//
//    }
//
//
//}
//
//
//class LeftObserver: Observer {
//    private val TAG = "LEFT_OBSERVER"
//
//    private var cameraController: Camera? = null
//    private var gimbalController: Gimbal? = null
//
//    var uiController:UIController? = null
//
//    val states by lazy {
//        mapOf<Int, State>(
//            0 to State(0, mapOf(InputType.COMMAND_1 to 1), arrayOf(this::reset)),
//            1 to State(1, mapOf(InputType.NUMERICAL_PARAMETER to 2, InputType.OTHER_COMMAND to 0), arrayOf(uiController!!::selectCommand, uiController!!::showParameterButtonBar)),
//            2 to State(2, mapOf(), arrayOf(this::selectParameter, this::left, this::reset)),
//        )
//    }
//
//    val parameters = mutableListOf<String>("1", "2", "3", "4", "5", "6", "7", "8", "9", "10")
//    val consecutiveWords = mapOf<String, Word>()
//
//    val command = Word("left", Feature.LEFT, InputType.COMMAND_1, DeviceType.CAMERA)
//    var currentState = 0
//    var currentWord = Word("", Feature.UNDEFINED, InputType.UNDEFINED, DeviceType.UNDEFINED)
//
//    override fun setCameraController(cameraController: Camera) {
//        this.cameraController = cameraController
//    }
//
//    override fun setGimbalController(gimbalController: Gimbal) {
//        this.gimbalController = gimbalController
//    }
//
//    override fun setUIController(uiController: UIController) {
//
//        this.uiController = uiController
//    }
//    override fun getUIController(): UIController? {
//        return this.uiController
//    }
//    override fun getControlParameters(): MutableList<String> {
//
//        return this.parameters
//    }
//
//    private fun selectParameter() {
//        this.uiController!!.selectParameter(false, this.currentWord.value)
//    }
//
//    override fun newWord(word: String) {
//
//        // Log.d(this.TAG, word)
//        val parsedWord = this.parseWord(word)
//        this.currentWord = parsedWord // Save word to be used by cb
//
//        val currentStateObject = this.states[this.currentState]
//
//        val newState = currentStateObject!!.newWord(parsedWord.inputType)
//
//        changeState(newState)
//
//    }
//
//    fun changeState(newState: Int) {
//
//        val currentStateObject = this.states[this.currentState]
//
//        val newStateObject = this.states[newState]
//
//        val hasStateChanged = newState != this.currentState
//        if (hasStateChanged) {
//
//            this.currentState = newState;
//
//            newStateObject!!.runCallbacks();
//
//        }
//
//
//    }
//    fun parseWord(word: String): Word {
//
//        var parsedWord = Word("", Feature.UNDEFINED, InputType.UNDEFINED, DeviceType.UNDEFINED);
//
//        val isDefinedCommand = firstCommands[word] != null
//        val isOurCommand = isDefinedCommand && firstCommands[word]?.value == this.command.value
//        val isConsecutiveWord = this.consecutiveWords[word] != null
//        val isOtherCommand = isDefinedCommand && firstCommands[word]?.value != this.command.value;
////        val isNumericalParameter = numericalParameters[word] != null
//        var isNumericalParameter = false
//
//        try {
//            word.toFloat()
//            isNumericalParameter = true
//        }catch (e: NumberFormatException) {
//            isNumericalParameter = false
//        }
//
//        if (isOurCommand) {
//            parsedWord = firstCommands[word]!!
//        }else if (isConsecutiveWord) {
//            parsedWord = this.consecutiveWords[word]!!
//        }else if (isOtherCommand) {
//            parsedWord = Word("", Feature.ANY, InputType.OTHER_COMMAND, DeviceType.ANY)
//        }else if (isNumericalParameter) {
////            parsedWord = numericalParameters[word]!!
//            parsedWord = Word(word, Feature.LEFT, InputType.NUMERICAL_PARAMETER, DeviceType.CAMERA)
//        }
//
//        return parsedWord
//    }
//
//    fun reset() {
//
////        timer.schedule(TimerTask {}, 100)
//        this.currentState = 0
//
//        // Does'nt work unless you run on ui thread, which I have not figured out yet
//        val mainLooper = Looper.getMainLooper()
//
//        if (this.currentWord.inputType == InputType.OTHER_COMMAND) {
//            uiController!!.reset()
//        }else {
//
//            Thread(Runnable {
//
//                Timer("Reset UI", false).schedule(1000) {
//
//                    Handler(mainLooper).post {
//
//                        uiController!!.reset()
//                    }
//                }
//            }).start()
//        }
//    }
//
//    fun left() {
//        Log.d(TAG, "LEFT: " + this.currentWord.value)
//
//        this.gimbalController!!.move("-${this.currentWord.value}", "0", "0", 0)
////        this.cameraController?.setZoom(this.currentWord.value)
//    }
//
//    override fun onCommandClick() {
//
//        newWord(this.command.value)
//    }
//
//    override fun onParameterClick(parameter: String) {
//
//        newWord(parameter)
//
//    }
//
//
//}
//
//
//class RightObserver: Observer {
//    private val TAG = "RIGHT_OBSERVER"
//
//    private var cameraController: Camera? = null
//    private var gimbalController: Gimbal? = null
//
//    var uiController:UIController? = null
//
//    val states by lazy {
//        mapOf<Int, State>(
//            0 to State(0, mapOf(InputType.COMMAND_1 to 1), arrayOf(this::reset)),
//            1 to State(1, mapOf(InputType.NUMERICAL_PARAMETER to 2, InputType.OTHER_COMMAND to 0), arrayOf(uiController!!::selectCommand, uiController!!::showParameterButtonBar)),
//            2 to State(2, mapOf(), arrayOf(this::selectParameter, this::right, this::reset)),
//        )
//    }
//
//    val parameters = mutableListOf<String>("1", "2", "3", "4", "5", "6", "7", "8", "9", "10")
//    val consecutiveWords = mapOf<String, Word>()
//
//    val command = Word("right", Feature.RIGHT, InputType.COMMAND_1, DeviceType.CAMERA)
//    var currentState = 0
//    var currentWord = Word("", Feature.UNDEFINED, InputType.UNDEFINED, DeviceType.UNDEFINED)
//
//    override fun setCameraController(cameraController: Camera) {
//        this.cameraController = cameraController
//    }
//
//    override fun setGimbalController(gimbalController: Gimbal) {
//        this.gimbalController = gimbalController
//    }
//
//    override fun setUIController(uiController: UIController) {
//
//        this.uiController = uiController
//    }
//    override fun getUIController(): UIController? {
//        return this.uiController
//    }
//    override fun getControlParameters(): MutableList<String> {
//
//        return this.parameters
//    }
//
//    private fun selectParameter() {
//        this.uiController!!.selectParameter(false, this.currentWord.value)
//    }
//
//    override fun newWord(word: String) {
//
//        // Log.d(this.TAG, word)
//        val parsedWord = this.parseWord(word)
//        this.currentWord = parsedWord // Save word to be used by cb
//
//        val currentStateObject = this.states[this.currentState]
//
//        val newState = currentStateObject!!.newWord(parsedWord.inputType)
//
//        changeState(newState)
//
//    }
//
//    fun changeState(newState: Int) {
//
//        val currentStateObject = this.states[this.currentState]
//
//        val newStateObject = this.states[newState]
//
//        val hasStateChanged = newState != this.currentState
//        if (hasStateChanged) {
//
//            this.currentState = newState;
//
//            newStateObject!!.runCallbacks();
//
//        }
//
//
//    }
//    fun parseWord(word: String): Word {
//
//        var parsedWord = Word("", Feature.UNDEFINED, InputType.UNDEFINED, DeviceType.UNDEFINED);
//
//        val isDefinedCommand = firstCommands[word] != null
//        val isOurCommand = isDefinedCommand && firstCommands[word]?.value == this.command.value
//        val isConsecutiveWord = this.consecutiveWords[word] != null
//        val isOtherCommand = isDefinedCommand && firstCommands[word]?.value != this.command.value;
////        val isNumericalParameter = numericalParameters[word] != null
//        var isNumericalParameter = false
//
//        try {
//            word.toFloat()
//            isNumericalParameter = true
//        }catch (e: NumberFormatException) {
//            isNumericalParameter = false
//        }
//
//        if (isOurCommand) {
//            parsedWord = firstCommands[word]!!
//        }else if (isConsecutiveWord) {
//            parsedWord = this.consecutiveWords[word]!!
//        }else if (isOtherCommand) {
//            parsedWord = Word("", Feature.ANY, InputType.OTHER_COMMAND, DeviceType.ANY)
//        }else if (isNumericalParameter) {
////            parsedWord = numericalParameters[word]!!
//            parsedWord = Word(word, Feature.RIGHT, InputType.NUMERICAL_PARAMETER, DeviceType.CAMERA)
//        }
//
//        return parsedWord
//    }
//
//    fun reset() {
//
////        timer.schedule(TimerTask {}, 100)
//        this.currentState = 0
//
//        // Does'nt work unless you run on ui thread, which I have not figured out yet
//        val mainLooper = Looper.getMainLooper()
//
//        if (this.currentWord.inputType == InputType.OTHER_COMMAND) {
//            uiController!!.reset()
//        }else {
//
//            Thread(Runnable {
//
//                Timer("Reset UI", false).schedule(1000) {
//
//                    Handler(mainLooper).post {
//
//                        uiController!!.reset()
//                    }
//                }
//            }).start()
//        }
//    }
//
//    fun right() {
//        Log.d(TAG, "RIGHT: " + this.currentWord.value)
//
//        this.gimbalController!!.move("${this.currentWord.value}", "0", "0", 0)
//
////        this.cameraController?.setZoom(this.currentWord.value)
//    }
//
//    override fun onCommandClick() {
//
//        newWord(this.command.value)
//    }
//
//    override fun onParameterClick(parameter: String) {
//
//        newWord(parameter)
//
//    }
//
//
//}
//
//
//class UpObserver: Observer {
//    private val TAG = "UP_OBSERVER"
//
//    private var cameraController: Camera? = null
//    private var gimbalController: Gimbal? = null
//
//    var uiController:UIController? = null
//
//    val states by lazy {
//        mapOf<Int, State>(
//            0 to State(0, mapOf(InputType.COMMAND_1 to 1), arrayOf(this::reset)),
//            1 to State(1, mapOf(InputType.NUMERICAL_PARAMETER to 2, InputType.OTHER_COMMAND to 0), arrayOf(uiController!!::selectCommand, uiController!!::showParameterButtonBar)),
//            2 to State(2, mapOf(), arrayOf(this::selectParameter, this::up, this::reset)),
//        )
//    }
//
//    val parameters = mutableListOf<String>("1", "2", "3", "4", "5", "6", "7", "8", "9", "10")
//    val consecutiveWords = mapOf<String, Word>()
//
//    val command = Word("up", Feature.UP, InputType.COMMAND_1, DeviceType.CAMERA)
//    var currentState = 0
//    var currentWord = Word("", Feature.UNDEFINED, InputType.UNDEFINED, DeviceType.UNDEFINED)
//
//    override fun setCameraController(cameraController: Camera) {
//        this.cameraController = cameraController
//    }
//
//    override fun setGimbalController(gimbalController: Gimbal) {
//        this.gimbalController = gimbalController
//    }
//
//    override fun setUIController(uiController: UIController) {
//
//        this.uiController = uiController
//    }
//    override fun getUIController(): UIController? {
//        return this.uiController
//    }
//    override fun getControlParameters(): MutableList<String> {
//
//        return this.parameters
//    }
//
//    private fun selectParameter() {
//        this.uiController!!.selectParameter(false, this.currentWord.value)
//    }
//
//    override fun newWord(word: String) {
//
//        val parsedWord = this.parseWord(word)
//        this.currentWord = parsedWord // Save word to be used by cb
//
//        val currentStateObject = this.states[this.currentState]
//
//        val newState = currentStateObject!!.newWord(parsedWord.inputType)
//
//        changeState(newState)
//
//    }
//
//    fun changeState(newState: Int) {
//
//        val currentStateObject = this.states[this.currentState]
//
//        val newStateObject = this.states[newState]
//
//        val hasStateChanged = newState != this.currentState
//        if (hasStateChanged) {
//
//            this.currentState = newState;
//
//            newStateObject!!.runCallbacks();
//
//        }
//
//
//    }
//    fun parseWord(word: String): Word {
//
//        var parsedWord = Word("", Feature.UNDEFINED, InputType.UNDEFINED, DeviceType.UNDEFINED);
//
//        val isDefinedCommand = firstCommands[word] != null
//        val isOurCommand = isDefinedCommand && firstCommands[word]?.value == this.command.value
//        val isConsecutiveWord = this.consecutiveWords[word] != null
//        val isOtherCommand = isDefinedCommand && firstCommands[word]?.value != this.command.value;
////        val isNumericalParameter = numericalParameters[word] != null
//        var isNumericalParameter = false
//
//        try {
//            word.toFloat()
//            isNumericalParameter = true
//        }catch (e: NumberFormatException) {
//            isNumericalParameter = false
//        }
//
//        if (isOurCommand) {
//            parsedWord = firstCommands[word]!!
//        }else if (isConsecutiveWord) {
//            parsedWord = this.consecutiveWords[word]!!
//        }else if (isOtherCommand) {
//            parsedWord = Word("", Feature.ANY, InputType.OTHER_COMMAND, DeviceType.ANY)
//        }else if (isNumericalParameter) {
////            parsedWord = numericalParameters[word]!!
//            parsedWord = Word(word, Feature.UP, InputType.NUMERICAL_PARAMETER, DeviceType.CAMERA)
//        }
//
//        return parsedWord
//    }
//
//    fun reset() {
//
////        timer.schedule(TimerTask {}, 100)
//        this.currentState = 0
//
//        // Does'nt work unless you run on ui thread, which I have not figured out yet
//        val mainLooper = Looper.getMainLooper()
//
//        if (this.currentWord.inputType == InputType.OTHER_COMMAND) {
//            uiController!!.reset()
//        }else {
//
//            Thread(Runnable {
//
//                Timer("Reset UI", false).schedule(1000) {
//
//                    Handler(mainLooper).post {
//
//                        uiController!!.reset()
//                    }
//                }
//            }).start()
//        }
//    }
//
//    fun up() {
//        Log.d(TAG, "UP: " + this.currentWord.value)
//
//        this.gimbalController!!.move("0", "${this.currentWord.value}", "0", 0)
//
////        this.cameraController?.setZoom(this.currentWord.value)
//    }
//
//    override fun onCommandClick() {
//
//        newWord(this.command.value)
//    }
//
//    override fun onParameterClick(parameter: String) {
//
//        newWord(parameter)
//
//    }
//
//
//}
//
//
//class DownObserver: Observer {
//    private val TAG = "DOWN_OBSERVER"
//
//    private var cameraController: Camera? = null
//    private var gimbalController: Gimbal? = null
//
//    var uiController:UIController? = null
//
//    val states by lazy {
//        mapOf<Int, State>(
//            0 to State(0, mapOf(InputType.COMMAND_1 to 1), arrayOf(this::reset)),
//            1 to State(1, mapOf(InputType.NUMERICAL_PARAMETER to 2, InputType.OTHER_COMMAND to 0), arrayOf(uiController!!::selectCommand, uiController!!::showParameterButtonBar)),
//            2 to State(2, mapOf(), arrayOf(this::selectParameter, this::down, this::reset)),
//        )
//    }
//
//    val parameters = mutableListOf<String>("1", "2", "3", "4", "5", "6", "7", "8", "9", "10")
//    val consecutiveWords = mapOf<String, Word>()
//
//    val command = Word("down", Feature.DOWN, InputType.COMMAND_1, DeviceType.CAMERA)
//    var currentState = 0
//    var currentWord = Word("", Feature.UNDEFINED, InputType.UNDEFINED, DeviceType.UNDEFINED)
//
//    override fun setCameraController(cameraController: Camera) {
//        this.cameraController = cameraController
//    }
//
//    override fun setGimbalController(gimbalController: Gimbal) {
//        this.gimbalController = gimbalController
//    }
//
//    override fun setUIController(uiController: UIController) {
//
//        this.uiController = uiController
//    }
//    override fun getUIController(): UIController? {
//        return this.uiController
//    }
//    override fun getControlParameters(): MutableList<String> {
//
//        return this.parameters
//    }
//
//    private fun selectParameter() {
//        this.uiController!!.selectParameter(false, this.currentWord.value)
//    }
//
//    override fun newWord(word: String) {
//
//        // Log.d(this.TAG, word)
//        val parsedWord = this.parseWord(word)
//        this.currentWord = parsedWord // Save word to be used by cb
//
//        val currentStateObject = this.states[this.currentState]
//
//        val newState = currentStateObject!!.newWord(parsedWord.inputType)
//
//        changeState(newState)
//
//    }
//
//    fun changeState(newState: Int) {
//
//        val currentStateObject = this.states[this.currentState]
//
//        val newStateObject = this.states[newState]
//
//        val hasStateChanged = newState != this.currentState
//        if (hasStateChanged) {
//
//            this.currentState = newState;
//
//            newStateObject!!.runCallbacks();
//
//        }
//
//
//    }
//    fun parseWord(word: String): Word {
//
//        var parsedWord = Word("", Feature.UNDEFINED, InputType.UNDEFINED, DeviceType.UNDEFINED);
//
//        val isDefinedCommand = firstCommands[word] != null
//        val isOurCommand = isDefinedCommand && firstCommands[word]?.value == this.command.value
//        val isConsecutiveWord = this.consecutiveWords[word] != null
//        val isOtherCommand = isDefinedCommand && firstCommands[word]?.value != this.command.value;
////        val isNumericalParameter = numericalParameters[word] != null
//        var isNumericalParameter = false
//
//        try {
//            word.toFloat()
//            isNumericalParameter = true
//        }catch (e: NumberFormatException) {
//            isNumericalParameter = false
//        }
//
//        if (isOurCommand) {
//            parsedWord = firstCommands[word]!!
//        }else if (isConsecutiveWord) {
//            parsedWord = this.consecutiveWords[word]!!
//        }else if (isOtherCommand) {
//            parsedWord = Word("", Feature.ANY, InputType.OTHER_COMMAND, DeviceType.ANY)
//        }else if (isNumericalParameter) {
////            parsedWord = numericalParameters[word]!!
//            parsedWord = Word(word, Feature.DOWN, InputType.NUMERICAL_PARAMETER, DeviceType.CAMERA)
//        }
//
//        return parsedWord
//    }
//
//    fun reset() {
//
////        timer.schedule(TimerTask {}, 100)
//        this.currentState = 0
//
//        // Does'nt work unless you run on ui thread, which I have not figured out yet
//        val mainLooper = Looper.getMainLooper()
//
//        if (this.currentWord.inputType == InputType.OTHER_COMMAND) {
//            uiController!!.reset()
//        }else {
//
//            Thread(Runnable {
//
//                Timer("Reset UI", false).schedule(1000) {
//
//                    Handler(mainLooper).post {
//
//                        uiController!!.reset()
//                    }
//                }
//            }).start()
//        }
//    }
//
//    fun down() {
//        Log.d(TAG, "DOWN: " + this.currentWord.value)
//
//        this.gimbalController!!.move("0", "-${this.currentWord.value}", "0", 0)
//
////        this.cameraController?.setZoom(this.currentWord.value)
//    }
//
//    override fun onCommandClick() {
//
//        newWord(this.command.value)
//    }
//
//    override fun onParameterClick(parameter: String) {
//
//        newWord(parameter)
//
//    }
//
//
//}
//
//
//class RollObserver: Observer {
//    private val TAG = "ROLL_OBSERVER"
//
//    private var cameraController: Camera? = null
//    private var gimbalController: Gimbal? = null
//
//    var uiController:UIController? = null
//
//    val states by lazy {
//        mapOf<Int, State>(
//            0 to State(0, mapOf(InputType.COMMAND_1 to 1), arrayOf(this::reset)),
//            1 to State(1, mapOf(InputType.NUMERICAL_PARAMETER to 2, InputType.OTHER_COMMAND to 0), arrayOf(uiController!!::selectCommand, uiController!!::showParameterButtonBar)),
//            2 to State(2, mapOf(), arrayOf(this::selectParameter, this::roll, this::reset)),
//        )
//    }
//
//    val parameters = mutableListOf<String>("1", "2", "3", "4", "5", "6", "7", "8", "9", "10")
//    val consecutiveWords = mapOf<String, Word>()
//
//    val command = Word("roll", Feature.ROLL, InputType.COMMAND_1, DeviceType.CAMERA)
//    var currentState = 0
//    var currentWord = Word("", Feature.UNDEFINED, InputType.UNDEFINED, DeviceType.UNDEFINED)
//
//    override fun setCameraController(cameraController: Camera) {
//        this.cameraController = cameraController
//    }
//
//    override fun setGimbalController(gimbalController: Gimbal) {
//        this.gimbalController = gimbalController
//    }
//
//    override fun setUIController(uiController: UIController) {
//
//        this.uiController = uiController
//    }
//    override fun getUIController(): UIController? {
//        return this.uiController
//    }
//    override fun getControlParameters(): MutableList<String> {
//
//        return this.parameters
//    }
//
//    private fun selectParameter() {
//        this.uiController!!.selectParameter(false, this.currentWord.value)
//    }
//
//    override fun newWord(word: String) {
//
//        // Log.d(this.TAG, word)
//        val parsedWord = this.parseWord(word)
//        this.currentWord = parsedWord // Save word to be used by cb
//
//        val currentStateObject = this.states[this.currentState]
//
//        val newState = currentStateObject!!.newWord(parsedWord.inputType)
//
//        changeState(newState)
//
//    }
//
//    fun changeState(newState: Int) {
//
//        val currentStateObject = this.states[this.currentState]
//
//        val newStateObject = this.states[newState]
//
//        val hasStateChanged = newState != this.currentState
//        if (hasStateChanged) {
//
//            this.currentState = newState;
//
//            newStateObject!!.runCallbacks();
//
//        }
//
//
//    }
//    fun parseWord(word: String): Word {
//
//        var parsedWord = Word("", Feature.UNDEFINED, InputType.UNDEFINED, DeviceType.UNDEFINED);
//
//        val isDefinedCommand = firstCommands[word] != null
//        val isOurCommand = isDefinedCommand && firstCommands[word]?.value == this.command.value
//        val isConsecutiveWord = this.consecutiveWords[word] != null
//        val isOtherCommand = isDefinedCommand && firstCommands[word]?.value != this.command.value;
////        val isNumericalParameter = numericalParameters[word] != null
//        var isNumericalParameter = false
//
//        try {
//            word.toFloat()
//            isNumericalParameter = true
//        }catch (e: NumberFormatException) {
//            isNumericalParameter = false
//        }
//
//        if (isOurCommand) {
//            parsedWord = firstCommands[word]!!
//        }else if (isConsecutiveWord) {
//            parsedWord = this.consecutiveWords[word]!!
//        }else if (isOtherCommand) {
//            parsedWord = Word("", Feature.ANY, InputType.OTHER_COMMAND, DeviceType.ANY)
//        }else if (isNumericalParameter) {
////            parsedWord = numericalParameters[word]!!
//            parsedWord = Word(word, Feature.ROLL, InputType.NUMERICAL_PARAMETER, DeviceType.CAMERA)
//        }
//
//        return parsedWord
//    }
//
//    fun reset() {
//
////        timer.schedule(TimerTask {}, 100)
//        this.currentState = 0
//
//        // Does'nt work unless you run on ui thread, which I have not figured out yet
//        val mainLooper = Looper.getMainLooper()
//
//        if (this.currentWord.inputType == InputType.OTHER_COMMAND) {
//            uiController!!.reset()
//        }else {
//
//            Thread(Runnable {
//
//                Timer("Reset UI", false).schedule(1000) {
//
//                    Handler(mainLooper).post {
//
//                        uiController!!.reset()
//                    }
//                }
//            }).start()
//        }
//    }
//
//    fun roll() {
//        Log.d(TAG, "ROLL: " + this.currentWord.value)
//        this.gimbalController!!.move("0", "0", "${this.currentWord.value}", 0)
//
////        this.cameraController?.setZoom(this.currentWord.value)
//    }
//
//    override fun onCommandClick() {
//
//        newWord(this.command.value)
//    }
//
//    override fun onParameterClick(parameter: String) {
//
//        newWord(parameter)
//
//    }
//
//
//}
//
//
//class FSM(
//    val consecutiveWords: Map<String, Word>,
//    val command: Word,
//){
//
//    var states = mapOf<Int, State>()
//
//    var currentState = 0
//    var currentWord = Word("", Feature.UNDEFINED, InputType.UNDEFINED, DeviceType.UNDEFINED)
//
//    fun newWord(word: String) {
//
//        val parsedWord = this.parseWord(word)
//        this.currentWord = parsedWord // Save word to be used by cb
//
//        val currentStateObject = this.states[this.currentState]
//
//        val newState = currentStateObject!!.newWord(parsedWord.inputType)
//
//        changeState(newState)
//
//    }
//
//    fun changeState(newState: Int) {
//
//        val newStateObject = this.states[newState]
//
//        val hasStateChanged = newState != this.currentState
//        if (hasStateChanged) {
//
//            this.currentState = newState;
//
//            newStateObject!!.runCallbacks();
//
//        }
//
//
//    }
//    fun parseWord(word: String): Word {
//
//        var parsedWord = Word("", Feature.UNDEFINED, InputType.UNDEFINED, DeviceType.UNDEFINED);
//
//        val isDefinedCommand = firstCommands[word] != null
//        val isOurCommand = isDefinedCommand && firstCommands[word]?.value == this.command.value
//        val isConsecutiveWord = this.consecutiveWords[word] != null
//        val isOtherCommand = isDefinedCommand && firstCommands[word]?.value != this.command.value;
////        val isNumericalParameter = numericalParameters[word] != null
//        var isNumericalParameter = false
//
//        try {
//            word.toFloat()
//            isNumericalParameter = true
//        }catch (e: NumberFormatException) {
//            isNumericalParameter = false
//        }
//
//        if (isOurCommand) {
//            parsedWord = firstCommands[word]!!
//        }else if (isConsecutiveWord) {
//            parsedWord = this.consecutiveWords[word]!!
//        }else if (isOtherCommand) {
//            parsedWord = Word("", Feature.ANY, InputType.OTHER_COMMAND, DeviceType.ANY)
//        }else if (isNumericalParameter) {
////            parsedWord = numericalParameters[word]!!
//            parsedWord = Word(word, command.feature, InputType.NUMERICAL_PARAMETER, DeviceType.CAMERA)
//        }
//
//        return parsedWord
//    }
//
//
//}

class FSMObserver(
    val uiController: UIController,
    val command: Word,
    val consecutiveWords: Map<String, Word>,
    ){

    lateinit var states: Map<Int, State> // Init later

    var currentState = 0
    var currentWord = Word("", Feature.UNDEFINED, InputType.UNDEFINED, DeviceType.UNDEFINED)

    fun newWord(word: String) {


        val parsedWord = this.parseWord(word)
        this.currentWord = parsedWord // Save word to be used by cb

        val currentStateObject = this.states[this.currentState]

        val newState = currentStateObject!!.newWord(parsedWord.inputType)

        changeState(newState)

    }

    fun parseWord(word: String): Word {

        var parsedWord = Word("", Feature.UNDEFINED, InputType.UNDEFINED, DeviceType.UNDEFINED);

        val isDefinedCommand = firstCommands[word] != null
        val isOurCommand = isDefinedCommand && firstCommands[word]?.value == this.command.value
        val isConsecutiveWord = this.consecutiveWords[word] != null
        val isOtherCommand = isDefinedCommand && firstCommands[word]?.value != this.command.value;
//        val isNumericalParameter = numericalParameters[word] != null
        var isNumericalParameter = false

        try {
            word.toFloat()
            isNumericalParameter = true
        }catch (e: NumberFormatException) {
            isNumericalParameter = false
        }

        if (isOurCommand) {
            parsedWord = firstCommands[word]!!
        }else if (isConsecutiveWord) {
            parsedWord = this.consecutiveWords[word]!!
        }else if (isOtherCommand) {
            parsedWord = Word("", Feature.ANY, InputType.OTHER_COMMAND, DeviceType.ANY)
        }else if (isNumericalParameter) {
//            parsedWord = numericalParameters[word]!!
            parsedWord = Word(word, Feature.MODE, InputType.NUMERICAL_PARAMETER, DeviceType.CAMERA)
        }

        return parsedWord
    }

    fun changeState(newState: Int) {

        val newStateObject = this.states[newState]

        val hasStateChanged = newState != this.currentState
        if (hasStateChanged) {

            this.currentState = newState;
            newStateObject!!.runCallbacks(this.currentWord)
        }
    }

    fun reset(optionalWord: Word) {

//        timer.schedule(TimerTask {}, 100)
        this.currentState = 0

        // Does'nt work unless you run on ui thread, which I have not figured out yet
        val mainLooper = Looper.getMainLooper()

        if (this.currentWord.inputType == InputType.OTHER_COMMAND) {
            uiController!!.reset()
        }else {

            Thread(Runnable {

                Timer("Reset UI", false).schedule(1000) {

                    Handler(mainLooper).post {

                        uiController!!.reset()
                    }
                }
            }).start()
        }
    }

    fun onCommandClick() {

        newWord(this.command.value)
    }

    fun onParameterClick(parameter: String) {

        newWord(parameter)

    }
}