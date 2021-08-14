package com.example.speechtotext


import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
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
        Feature.ROLL_NEGATIVE to Word("roll", Feature.ROLL_NEGATIVE, InputType.COMMAND_1, DeviceType.CAMERA),
        Feature.ROLL_POSITIVE to Word("roll", Feature.ROLL_POSITIVE, InputType.COMMAND_1, DeviceType.CAMERA),
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
        Feature.ROLL_NEGATIVE to mapOf<String, Word>(
            "negative" to Word("negative", Feature.ROLL_NEGATIVE, InputType.PARAMETER, DeviceType.CAMERA),
        ),
        Feature.ROLL_POSITIVE to mapOf<String, Word>(
            "positive" to Word("positive", Feature.ROLL_POSITIVE, InputType.PARAMETER, DeviceType.CAMERA),
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
            Feature.ROLL_NEGATIVE to
                    mapOf<Int, State>(
                        0 to State(0,
                            mapOf(InputType.COMMAND_1 to 1),
                            arrayOf(observers.get(Feature.ROLL_NEGATIVE)!!::reset)), // If it is list, then reset all the buttons
                        1 to State(1,
                            mapOf(InputType.PARAMETER to 2),
                            arrayOf()),
                        2 to State(2,
                            mapOf(InputType.NUMERICAL_PARAMETER to 3, InputType.OTHER_COMMAND to 0),
                            arrayOf(observers.get(Feature.ROLL_NEGATIVE)!!.uiController!!::selectCommand,
                                observers.get(Feature.ROLL_NEGATIVE)!!.uiController!!::showParameterButtonBar)),
                        3 to State(3,
                            mapOf(),
                            arrayOf(observers.get(Feature.ROLL_NEGATIVE)!!.uiController!!::selectParameter,
                                MasterGimbal::move,
                                observers.get(Feature.ROLL_NEGATIVE)!!::reset)),
                    ),
            Feature.ROLL_POSITIVE to
                    mapOf<Int, State>(
                        0 to State(0,
                            mapOf(InputType.COMMAND_1 to 1),
                            arrayOf(observers.get(Feature.ROLL_POSITIVE)!!::reset)), // If it is list, then reset all the buttons
                        1 to State(1,
                            mapOf(InputType.PARAMETER to 2),
                            arrayOf()),
                        2 to State(2,
                            mapOf(InputType.NUMERICAL_PARAMETER to 3, InputType.OTHER_COMMAND to 0),
                            arrayOf(observers.get(Feature.ROLL_POSITIVE)!!.uiController!!::selectCommand,
                                observers.get(Feature.ROLL_POSITIVE)!!.uiController!!::showParameterButtonBar)),
                        3 to State(3,
                            mapOf(),
                            arrayOf(observers.get(Feature.ROLL_POSITIVE)!!.uiController!!::selectParameter,
                                MasterGimbal::move,
                                observers.get(Feature.ROLL_POSITIVE)!!::reset)),
                    )

        )
    }

    val parameters = mutableMapOf<Feature, MutableList<String>>(
        Feature.ZOOM to mutableListOf<String>("0", "25", "50", "75", "100"),
        Feature.FOCUS to mutableListOf<String>("point", "face", "spot"),
        Feature.MODE to mutableListOf<String>("photo", "movie"),
        Feature.APERTURE to mutableListOf<String>("3.4", "4.0", "4.5", "5.0", "5.6", "6.3", "7.1", "8.0"),
        Feature.LEFT to mutableListOf<String>("0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10"),
        Feature.RIGHT to mutableListOf<String>("0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10"),
        Feature.UP to mutableListOf<String>("0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10"),
        Feature.DOWN to mutableListOf<String>("0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10"),
        Feature.ROLL_NEGATIVE to mutableListOf<String>("0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10"),
        Feature.ROLL_POSITIVE to mutableListOf<String>("0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10")
    )

    /*REFACTOR END*/

    var staticFeatures = mutableListOf(Feature.SHOOT, Feature.RIGHT, Feature.UP, Feature.DOWN, Feature.LEFT, Feature.ROLL_NEGATIVE, Feature.ROLL_POSITIVE, Feature.INCREMENTAL_MOVEMENT, Feature.ABSOLUTE_MOVEMENT)
    var chosenControls = mutableListOf<Feature>()

    var buttons = mutableMapOf<Feature, View>()

    // REFACTOR: Use factory design pattern
    val factory = MasterControllerFactory()

    var screenChanged = false


    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Toast.makeText(this, "MAIN", Toast.LENGTH_SHORT).show()

//        imageView.setOnClickListener {
//
//        }

//        btnUp.setOnClickListener {
//            Toast.makeText(this, "Hi there pundeh", Toast.LENGTH_SHORT).show()
//        }

        val systemManager = SystemManager()
        val features = systemManager.getFeatures()


        // Initialize all controllers for the first time
        factory.controllers.forEach {

            it.value.forEach {
                (it.value as Device).setApplicationContext(applicationContext)
            }
        }

        // Initialize all required instances
        initObservers()
        initButtons()
        initControllers()

        // Definitely alot faster on partial results
        // With isOnUnstable definitely produces unstable results
            // Example: "25" => "2", "20", "25" ...
        SpeechToTextEngine.applicationContext = WeakReference(applicationContext)
        SpeechToTextEngine.initialize(false, false, Model)

        // Must start STT via user interaction
        startTranscription.setOnClickListener {

            startTranscription.text = "VOICE CONTROL: ${mapOf(true to "ON", false to "OFF").get(!SpeechToTextEngine.isActive)}"

            try {
                val permissions: Array<String> = arrayOf(android.Manifest.permission.RECORD_AUDIO, android.Manifest.permission.INTERNET)
                requestPermissions( permissions, 101)

                SpeechToTextEngine.toggleStream()

            }catch (e: ActivityNotFoundException){
                Toast.makeText(this, "Your device does not support STT.", Toast.LENGTH_LONG).show()
                e.printStackTrace()
            }

        }

        btnConnectCamera.setOnClickListener {

            val camera = factory.getCameraInstance(MasterCamera.chosenCamera)
            camera?.configureLiveview(this@MainActivity::setImageView)

        }

        // To render the configuration view
        configurationButton?.setOnClickListener{

            screenChanged = true
            Intent(this, ConfigurationActivity::class.java).also {
                it.putExtra("EXTRA_FEATURES", features)
                startActivity(it)
            }

        }
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onResume() {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        super.onResume()

        if (SpeechToTextEngine.isActive) {

            SpeechToTextEngine.openStream()
        }

        // Refactor screenChanged boolean, might not need it
        if (screenChanged) {

            Toast.makeText(this, "RESUME", Toast.LENGTH_SHORT).show()

            // Check db for controls
            Log.d(TAG, "You are back to main activity")
            Log.d(TAG, "Camera chosen: ${db.getCamera()}; Gimbal chosen: ${db.getGimbal()}")
            Log.d(TAG, "Controls chosen: ${db.getControls()}")

            initControllers()
            initButtons()

            screenChanged = false
        }

    }

    override fun onPause() {
        super.onPause()

        // To ensure that the stt does not remain on when a user navigates to another activity
        SpeechToTextEngine.closeStream()
    }

    /*
    * Handles ALL activity results that get sent to this activity.
    * */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PilotflyGimbalController.REQUEST_ENABLE_BT && resultCode == AppCompatActivity.RESULT_OK) {
            PilotflyGimbalController.scanLeDevice()
        }
    }

    /*
    * Sets the chosenCamera and MasterGimbal.chosenGimbal by retrieving from the database
    * */
    fun initControllers() {

        MasterGimbal.chosenGimbal = db.getGimbal().deviceName
        MasterCamera.chosenCamera = db.getCamera().deviceName

        val cameraController = factory.getDeviceInstance(MasterCamera.chosenCamera)
        cameraController.setIp(db.getCamera().ipAddress)

        val gimbalController = factory.getDeviceInstance(MasterGimbal.chosenGimbal)
        gimbalController.setIp(db.getGimbal().ipAddress)

        connectDevices()
    }

    fun connectDevices() {

        MasterCamera.connectDevice(this@MainActivity)
        MasterGimbal.connectDevice(this@MainActivity)
    }

    fun initButtons() {

        // Create buttons
        createButtons()
        setButtons()
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

        val NUMBER_ROWS = 4
        var verticalLinearLayout:LinearLayout? = null
        var i = 0

        chosenControls = db.getControls()
        chosenControls.forEach {


            if (it !in staticFeatures) {

                if (i%NUMBER_ROWS == 0) {

                    verticalLinearLayout = LinearLayout(applicationContext).apply {

                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT)

                        orientation = LinearLayout.VERTICAL

                    }

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

                buttons.put(it, newButton as View)
                i+= 1
            }
        }
    }

    fun removeButtons() {

        commandButtonCreatorLayout.removeAllViews()
        buttons.clear()
        buttons.put(Feature.SHOOT, shootImage as View)
        buttons.put(Feature.UP, btnUp as View)
        buttons.put(Feature.RIGHT, btnRight as View)
        buttons.put(Feature.DOWN, btnDown as View)
        buttons.put(Feature.LEFT, btnLeft as View)
        buttons.put(Feature.ROLL_NEGATIVE, btnRollLeft as View)
        buttons.put(Feature.ROLL_POSITIVE, btnRollRight as View)

//        buttons.put(Feature.RIGHT, btnRight as View)
//        buttons.put(Feature.RIGHT, btnRight as View)

//        if (Feature.UP in chosenControls) {
//            buttons.put(Feature.UP, btnUp as View)
//        }

    }

    // Run only once when app starts
    fun initObservers() {

        features.forEach {

            val uiController = when(it.key) {

                Feature.ZOOM -> {
                    UIController(AdaptiveParameterGaugeBar(parameterButtonCreatorLayout, applicationContext))
                }
                else -> {
                    UIController(AdaptiveParameterButtonBar(parameterButtonCreatorLayout, applicationContext))
                }
            }

            uiController.setFeature(it.key, parameters.get(it.key) ?: mutableListOf<String>(), Model::newWord, Model::newWord)

            val obs = FSMObserver(uiController, it.value, consecutiveWords.get(it.key) ?: mapOf<String, Word>())

            observers.put(it.key, obs)
        }

        // Set states for each observer
        observers.forEach{

            it.value.states = observerStates.get(it.key)!!
        }

    }

    // Run everytime the controls change
    fun setButtons() {

        Model.removeAllObservers()
        buttons.forEach {

            observers.get(it.key)!!.uiController.setCommandButton(it.value as Button)
            Model.addObserver(observers.get(it.key)!!)
        }
    }
}