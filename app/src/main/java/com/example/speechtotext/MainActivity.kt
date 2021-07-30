package com.example.speechtotext


import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.example.speechtotext.devicecontroller.*
import com.example.speechtotext.manager.SystemManager
import kotlinx.android.synthetic.main.activity_configuration.*
import kotlinx.android.synthetic.main.activity_main.*
import java.lang.ref.WeakReference
import java.util.*


/*
* TODO:
* - Refactor start transcription button to be in the system configuration or a button by itself?
* - Refactor such that the button SHOOT can never be disabled
* - Add camera connection button
* - Create a button toggle class
* - Create toggle function for STT
* - When camera gets disconnected, must report to user and stop polling camera for the view
* - Refactor factory map and abstract it to another class that uses a factory design pattern
* - Refactor all factory to adopt the singleton design pattern
* - Reset all views before starting a new activity
*   - Experienced a bug that basically impaired all command buttons when I clicked on a command button, navigated to config view and navigated back.
* */


class MainActivity : AppCompatActivity(){

    val TAG = "MAIN_ACTIVITY"
    val db by lazy {
        Database(this)
    }

    val stt by lazy {
        SpeechToTextEngine(applicationContext)
    }

    /*REFACTOR START*/
    val observers = mutableMapOf<Feature, FSMObserver>(
    )

    val features = mapOf(
        Feature.SHOOT to Word("shoot", Feature.SHOOT, InputType.COMMAND_1, DeviceType.CAMERA),
        Feature.ZOOM to Word("zoom", Feature.ZOOM, InputType.COMMAND_1, DeviceType.CAMERA),
        Feature.APERTURE to Word("aperture", Feature.APERTURE, InputType.COMMAND_1, DeviceType.CAMERA),
        Feature.FOCUS to Word("focus", Feature.FOCUS, InputType.COMMAND_1, DeviceType.CAMERA),
        Feature.MODE to Word("mode", Feature.MODE, InputType.COMMAND_1, DeviceType.CAMERA),
        Feature.LEFT to Word("left", Feature.LEFT, InputType.COMMAND_1, DeviceType.CAMERA),
        Feature.RIGHT to Word("right", Feature.RIGHT, InputType.COMMAND_1, DeviceType.CAMERA),
        Feature.UP to Word("up", Feature.UP, InputType.COMMAND_1, DeviceType.CAMERA),
        Feature.DOWN to Word("down", Feature.DOWN, InputType.COMMAND_1, DeviceType.CAMERA),
        Feature.ROLL to Word("roll", Feature.LEFT, InputType.COMMAND_1, DeviceType.CAMERA),
    )

    val consecutiveWords = mapOf<Feature, Map<String, Word>>(
        Feature.FOCUS to mapOf<String, Word>(
            "point" to Word("point", Feature.FOCUS, InputType.PARAMETER, DeviceType.CAMERA),
            "face" to Word("face", Feature.FOCUS, InputType.PARAMETER, DeviceType.CAMERA),
            "spot" to Word("spot", Feature.FOCUS, InputType.PARAMETER, DeviceType.CAMERA),
        ),
        Feature.MODE to mapOf<String, Word>(
            "photo" to Word("photo", Feature.MODE, InputType.PARAMETER, DeviceType.CAMERA),
            "movie" to Word("movie", Feature.MODE, InputType.PARAMETER, DeviceType.CAMERA),
        ),
    )

    val observerStates by lazy {
        mapOf<Feature, Map<Int, State>>(
            Feature.SHOOT to
                    mapOf(
                        0 to State(0,
                            mapOf(InputType.COMMAND_1 to 1),
                            arrayOf(observers.get(Feature.SHOOT)!!::reset)),
                        1 to State(1,
                            mapOf(),
                            arrayOf(observers.get(Feature.SHOOT)!!.uiController!!::selectCommand,
                                MasterCamera::shoot, observers.get(Feature.SHOOT)!!::reset)),
                    ),
            Feature.ZOOM to
                    mapOf(
                        0 to State(0,
                            mapOf(InputType.COMMAND_1 to 1),
                            arrayOf(observers.get(Feature.ZOOM)!!::reset)),
                        1 to State(1,
                            mapOf(InputType.NUMERICAL_PARAMETER to 2, InputType.OTHER_COMMAND to 0),
                            arrayOf(observers.get(Feature.ZOOM)!!.uiController!!::selectCommand,
                                observers.get(Feature.ZOOM)!!.uiController!!::showParameterButtonBar)),
                        2 to State(2,
                            mapOf(),
                            arrayOf(observers.get(Feature.ZOOM)!!.uiController!!::selectParameter,
                                MasterCamera::setZoom,
                                observers.get(Feature.ZOOM)!!::reset))
                    ),
            Feature.APERTURE to
                    mapOf(
                        0 to State(0,
                            mapOf(InputType.COMMAND_1 to 1),
                            arrayOf(observers.get(Feature.APERTURE)!!::reset)),
                        1 to State(1,
                            mapOf(InputType.NUMERICAL_PARAMETER to 2, InputType.OTHER_COMMAND to 0),
                            arrayOf(observers.get(Feature.APERTURE)!!.uiController!!::selectCommand,
                                observers.get(Feature.APERTURE)!!.uiController!!!!::showParameterButtonBar)),
                        2 to State(2,
                            mapOf(),
                            arrayOf(observers.get(Feature.APERTURE)!!.uiController!!::selectParameter,
                                MasterCamera::setAperture,
                                observers.get(Feature.APERTURE)!!::reset)),
                    ),
            Feature.FOCUS to
                    mapOf<Int, State>(
                        0 to State(0,
                            mapOf(InputType.COMMAND_1 to 1),
                            arrayOf(observers.get(Feature.FOCUS)!!::reset)),
                        1 to State(
                            1,
                            mapOf(InputType.PARAMETER to 2, InputType.OTHER_COMMAND to 0),
                            arrayOf(observers.get(Feature.FOCUS)!!.uiController!!::selectCommand,
                                observers.get(Feature.FOCUS)!!.uiController!!::showParameterButtonBar)
                        ),
                        2 to State(2,
                            mapOf(),
                            arrayOf(observers.get(Feature.FOCUS)!!.uiController!!::selectParameter,
                                MasterCamera::setFocusType,
                                observers.get(Feature.FOCUS)!!::reset)),
                    ),
            Feature.MODE to
                    mapOf<Int, State>(
                        0 to State(0,
                            mapOf(InputType.COMMAND_1 to 1),
                            arrayOf(observers.get(Feature.MODE)!!::reset)),
                        1 to State(
                            1,
                            mapOf(InputType.PARAMETER to 2, InputType.OTHER_COMMAND to 0),
                            arrayOf(observers.get(Feature.MODE)!!.uiController!!::selectCommand,
                                observers.get(Feature.MODE)!!.uiController!!::showParameterButtonBar)
                        ),
                        2 to State(2,
                            mapOf(),
                            arrayOf(observers.get(Feature.MODE)!!.uiController!!::selectParameter,
                                MasterCamera::setMode,
                                observers.get(Feature.MODE)!!::reset)),
                    ),
            Feature.LEFT to
                    mapOf<Int, State>(
                        0 to State(0,
                            mapOf(InputType.COMMAND_1 to 1),
                            arrayOf(observers.get(Feature.LEFT)!!::reset)),
                        1 to State(1,
                            mapOf(InputType.NUMERICAL_PARAMETER to 2, InputType.OTHER_COMMAND to 0),
                            arrayOf(observers.get(Feature.LEFT)!!.uiController!!::selectCommand,
                                observers.get(Feature.LEFT)!!.uiController!!::showParameterButtonBar)),
                        2 to State(2,
                            mapOf(),
                            arrayOf(observers.get(Feature.LEFT)!!.uiController!!::selectParameter,
                                MasterGimbal::move,
                                observers.get(Feature.LEFT)!!::reset)),
                    ),
            Feature.RIGHT to
                    mapOf<Int, State>(
                        0 to State(0,
                            mapOf(InputType.COMMAND_1 to 1),
                            arrayOf(observers.get(Feature.RIGHT)!!::reset)),
                        1 to State(1,
                            mapOf(InputType.NUMERICAL_PARAMETER to 2, InputType.OTHER_COMMAND to 0),
                            arrayOf(observers.get(Feature.RIGHT)!!.uiController!!::selectCommand,
                                observers.get(Feature.RIGHT)!!.uiController!!::showParameterButtonBar)),
                        2 to State(2,
                            mapOf(),
                            arrayOf(observers.get(Feature.RIGHT)!!.uiController!!::selectParameter,
                                MasterGimbal::move,
                                observers.get(Feature.RIGHT)!!::reset)),
                    ),
            Feature.UP to
                    mapOf<Int, State>(
                        0 to State(0,
                            mapOf(InputType.COMMAND_1 to 1),
                            arrayOf(observers.get(Feature.UP)!!::reset)),
                        1 to State(1,
                            mapOf(InputType.NUMERICAL_PARAMETER to 2, InputType.OTHER_COMMAND to 0),
                            arrayOf(observers.get(Feature.UP)!!.uiController!!::selectCommand,
                                observers.get(Feature.UP)!!.uiController!!::showParameterButtonBar)),
                        2 to State(2,
                            mapOf(),
                            arrayOf(observers.get(Feature.UP)!!.uiController!!::selectParameter,
                                MasterGimbal::move,
                                observers.get(Feature.UP)!!::reset)),
                    ),
            Feature.DOWN to
                    mapOf<Int, State>(
                        0 to State(0,
                            mapOf(InputType.COMMAND_1 to 1),
                            arrayOf(observers.get(Feature.DOWN)!!::reset)),
                        1 to State(1,
                            mapOf(InputType.NUMERICAL_PARAMETER to 2, InputType.OTHER_COMMAND to 0),
                            arrayOf(observers.get(Feature.DOWN)!!.uiController!!::selectCommand,
                                observers.get(Feature.DOWN)!!.uiController!!::showParameterButtonBar)),
                        2 to State(2,
                            mapOf(),
                            arrayOf(observers.get(Feature.DOWN)!!.uiController!!::selectParameter,
                                MasterGimbal::move,
                                observers.get(Feature.DOWN)!!::reset)),
                    ),
            Feature.ROLL to
                    mapOf<Int, State>(
                        0 to State(0,
                            mapOf(InputType.COMMAND_1 to 1),
                            arrayOf(observers.get(Feature.ROLL)!!::reset)),
                        1 to State(1,
                            mapOf(InputType.NUMERICAL_PARAMETER to 2, InputType.OTHER_COMMAND to 0),
                            arrayOf(observers.get(Feature.ROLL)!!.uiController!!::selectCommand,
                                observers.get(Feature.ROLL)!!.uiController!!::showParameterButtonBar)),
                        2 to State(2,
                            mapOf(),
                            arrayOf(observers.get(Feature.ROLL)!!.uiController!!::selectParameter,
                                MasterGimbal::move,
                                observers.get(Feature.ROLL)!!::reset)),
                    ),

            )
    }

    val parameters = mutableMapOf<Feature, MutableList<String>>(
        Feature.ZOOM to mutableListOf<String>("0", "25", "50", "75", "100"),
        Feature.FOCUS to mutableListOf<String>("point", "face", "spot"),
        Feature.MODE to mutableListOf<String>("photo", "movie"),
        Feature.APERTURE to mutableListOf<String>("3.4", "4.0", "4.5", "5.0", "5.6", "6.3", "7.1", "8.0"),
        Feature.LEFT to mutableListOf<String>("1", "2", "3", "4", "5", "6", "7", "8", "9", "10"),
        Feature.RIGHT to mutableListOf<String>("1", "2", "3", "4", "5", "6", "7", "8", "9", "10"),
        Feature.UP to mutableListOf<String>("1", "2", "3", "4", "5", "6", "7", "8", "9", "10"),
        Feature.DOWN to mutableListOf<String>("1", "2", "3", "4", "5", "6", "7", "8", "9", "10"),
        Feature.ROLL to mutableListOf<String>("1", "2", "3", "4", "5", "6", "7", "8", "9", "10")
    )

    /*REFACTOR END*/

//    var observers = mutableMapOf<Feature, Any>(
//        Feature.SHOOT to ShootObserver(),
//        Feature.ZOOM to ZoomObserver(),
//        Feature.APERTURE to ApertureObserver(),
//        Feature.FOCUS to FocusObserver(),
//        Feature.MODE to ModeObserver(),
//        Feature.LEFT to LeftObserver(),
//        Feature.RIGHT to RightObserver(),
//        Feature.UP to UpObserver(),
//        Feature.DOWN to DownObserver(),
//        Feature.ROLL to RollObserver(),
//    )

    var chosenControls = mutableListOf<Feature>()
    var chosenCamera = DeviceName.CANON
    var chosenGimbal = DeviceName.DJI_RS_2

    var buttons = mutableMapOf<Feature, Button>()

    // REFACTOR: Use factory design pattern
    val factory = MasterControllerFactory()

    var screenChanged = false


    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Toast.makeText(this, "MAIN", Toast.LENGTH_SHORT).show()

        val systemManager = SystemManager()
        val features = systemManager.getFeatures()

        // To render the configuration view
        configurationButton?.setOnClickListener{

            screenChanged = true
            Intent(this, ConfigurationActivity::class.java).also {
               it.putExtra("EXTRA_FEATURES", features)
               startActivity(it)
           }

        }

        factory.controllers.forEach {

            it.value.forEach {
                (it.value as Device).setApplicationContext(applicationContext)
            }
        }

        initObservers()

        // Create buttons
        createButtons()
        setButtons()

//        initializeObservers()

        stt.initialize(false, Model)

        // Must start STT via user interaction
        startTranscription.setOnClickListener {

            startTranscription.text = "VOICE CONTROL: ${mapOf(true to "ON", false to "OFF").get(!stt.isActive)}"

            try {
                val permissions: Array<String> = arrayOf(android.Manifest.permission.RECORD_AUDIO, android.Manifest.permission.INTERNET)
                requestPermissions( permissions, 101)

                stt.toggleStream()

            }catch (e: ActivityNotFoundException){
                Toast.makeText(this, "Your device does not support STT.", Toast.LENGTH_LONG).show()
                e.printStackTrace()
            }

        }

        btnConnectCamera.setOnClickListener {

            val camera = factory.getCameraInstance(chosenCamera)

            camera?.configureLiveview(this@MainActivity::setImageView)

        }


    }

    override fun onPause() {
        super.onPause()

        // To ensure that the stt does not remain on when a user navigates to another activity







        stt.closeStream()
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onResume() {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        super.onResume()

        if (stt.isActive) {

            stt.openStream()
        }

        // Refactor screenChanged boolean, might not need it
        if (screenChanged) {

            Toast.makeText(this, "RESUME", Toast.LENGTH_SHORT).show()

            // Check db for controls
            Log.d(TAG, "You are back to main activity")
            Log.d(TAG, "Camera chosen: ${db.getCamera()}; Gimbal chosen: ${db.getGimbal()}")
            Log.d(TAG, "Controls chosen: ${db.getControls()}")

            // Update controls being rendered
            chosenControls = db.getControls()

            // Update camera controller
            chosenCamera = db.getCamera().deviceName
            val cameraController = factory.getDeviceInstance(chosenCamera)
            cameraController.setIp(db.getCamera().ipAddress)
            MasterCamera.chosenCamera = chosenCamera

            // Update gimbal controller
            chosenGimbal = db.getGimbal().deviceName
            val gimbalController = factory.getDeviceInstance(chosenGimbal)
            gimbalController.setIp(db.getGimbal().ipAddress)
            MasterGimbal.chosenGimbal = chosenGimbal

            if (chosenCamera == DeviceName.NO_OP_CAMERA) {
                (factory.controllers.get(DeviceType.CAMERA)!!.get(chosenCamera) as NoOpCameraController).textView = WeakReference(transcription)

                transcription.setZ(2.toFloat())

            }

            if (chosenGimbal == DeviceName.NO_OP_GIMBAL) {
                (factory.controllers.get(DeviceType.GIMBAL)!!.get(chosenGimbal) as NoOpGimbalController).textView = WeakReference(transcription)
                transcription.apply {
                    setZ(2.toFloat())
                    textSize = 20F
                    setTextColor(Color.parseColor("#ffffff"))
                    setBackgroundColor(Color.parseColor("#0f0f0f"))
                }

            }
            createButtons()
//            initializeObservers()
            setButtons()

            screenChanged = false
        }

    }

    fun setImageView(camera: com.example.speechtotext.devicecontroller.Camera) {

        Timer().scheduleAtFixedRate(object : TimerTask() {
            override fun run() {

                try{

                    camera.getLiveviewFlip(this@MainActivity::setBm)
                }catch (e: Exception) {
                    Toast.makeText(applicationContext, "Camera IP is incorrect", Toast.LENGTH_LONG)

                    // REFACTOR: Stop timer
                }

            }
        },0 ,100)

    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun setBm(bitmap: Bitmap?) {
        runOnUiThread {
            if (bitmap != null) {

                imageView.apply {

                    setZ(-1.toFloat())
                    setImageBitmap(bitmap)
                }

            }

        }
    }

    fun createButtons() {

        removeButtons()
        val NUMBER_ROWS = 5
        var verticalLinearLayout:LinearLayout? = null
        var i = 0

        chosenControls = db.getControls()
        chosenControls.forEach {


            if (it.toString() != "SHOOT") {

                if (i%NUMBER_ROWS == 0) {

                    verticalLinearLayout = LinearLayout(applicationContext)
                    verticalLinearLayout!!.layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT)
                    verticalLinearLayout!!.orientation = LinearLayout.VERTICAL

                    commandButtonCreatorLayout.addView(verticalLinearLayout)
                }


                var newButton = Button(applicationContext).apply {
                    setText(it.toString())

                    // Set constraints
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT)

                }

                verticalLinearLayout!!.addView(newButton)

                buttons.put(it, newButton)
                i+= 1
            }
        }
    }

    fun removeButtons() {

        commandButtonCreatorLayout.removeAllViews()
        buttons.clear()
        buttons.put(Feature.SHOOT, shootImage)
    }

    // Run only once when app starts
    fun initObservers() {

        features.forEach {

            val uiController = UIController(AdaptiveParameterButtonBar(parameterButtonCreatorLayout, applicationContext))
            uiController.setFeature(it.key, parameters.get(it.key) ?: mutableListOf<String>(), Model::newWord, Model::newWord)

            val obs = FSMObserver(uiController, it.value, consecutiveWords.get(it.key) ?: mapOf<String, Word>())

            observers.put(it.key, obs)
        }

        observers.forEach{

            it.value.states = observerStates.get(it.key)!!
        }

    }

    // Run everytime the controls change
    fun setButtons() {

        Model.removeAllObservers()
        buttons.forEach {

            observers.get(it.key)!!.uiController.setCommandButton(it.value)
            Model.addObserver(observers.get(it.key)!!)
        }
    }

//    fun initializeObservers() {
//
//        chosenCamera = db.getCamera().deviceName
//        chosenGimbal = db.getGimbal().deviceName
//
//        Model.removeAllObservers()
//        observers.forEach {
//
//            val observer = it.value as Observer
//
//            var isChosenControl = buttons.containsKey(it.key)
//            if (isChosenControl) {
//
//
//                if (observer.getUIController() == null) {
//
//                    val uiController = UIController(buttons[it.key]!!, AdaptiveParameterButtonBar(parameterButtonCreatorLayout, applicationContext))
//
//                    observer.setUIController(uiController)
//
//                }else{
//
//                    observer.getUIController()!!.button = buttons[it.key]!!
//                }
//                val uiController = observer.getUIController()
//
//                uiController!!.setFeature(it.key, observer.getControlParameters(), Model::newWord, observer::onParameterClick)
//                observer.setCameraController(factory.getCameraInstance(chosenCamera))
//                observer.setGimbalController(factory.getGimbalInstance(chosenGimbal))
//
//                if (it.value is ShootObserver) {
//
//                    val shootObserver  = it.value as ShootObserver
//
//                    shootObserver.setFsmStates()
//                }
//                Model.addObserver(observer)
//            }
//        }
//    }
}