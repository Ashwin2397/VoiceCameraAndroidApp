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
import com.example.speechtotext.devicecontroller.*
import com.example.speechtotext.manager.SystemManager
import kotlinx.android.synthetic.main.activity_configuration.*
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*


/*
* TODO:
* - Refactor start transcription button to be in the system configuration or a button by itself?
* - Refactor such that the button SHOOT can never be disabled
* - Add camera connection button
* - Create a button toggle class
* - Create toggle function for STT
* - When camera gets disconnected, must report to user and stop polling camera for the view
* - Refactor controllers map and abstract it to another class that uses a factory design pattern
* - Refactor all controllers to adopt the singleton design pattern
* - Reset all views before starting a new activity
*   - Experienced a bug that basically impaired all command buttons when I clicked on a command button, navigated to config view and navigated back.
* */


class MainActivity : AppCompatActivity(){

    val TAG = "MAIN_ACTIVITY"
    val db by lazy {
        Database(this)
    }
    val model = Model()

    val stt by lazy {
        SpeechToTextEngine(applicationContext)
    }
    var observers = mutableMapOf<Feature, Observer>(
        Feature.SHOOT to ShootObserver(),
        Feature.ZOOM to ZoomObserver(),
        Feature.APERTURE to ApertureObserver(),
        Feature.FOCUS to FocusObserver(),
        Feature.MODE to ModeObserver(),
        Feature.LEFT to LeftObserver(),
        Feature.RIGHT to RightObserver(),
        Feature.UP to UpObserver(),
        Feature.DOWN to DownObserver(),
        Feature.ROLL to RollObserver(),
    )

    var chosenControls = mutableListOf<Feature>(
        Feature.ZOOM, Feature.APERTURE, Feature.FOCUS, Feature.MODE
    )
    var chosenCamera = DeviceName.CANON
    var chosenGimbal = DeviceName.PILOTFLY

    var buttons = mutableMapOf<Feature, Button>()

    // REFACTOR: Use factory design pattern
    val controllers = mutableMapOf<DeviceName, Any>(
        DeviceName.CANON to CanonCameraController(),
        DeviceName.NATIVE to NativeCameraController(),
        DeviceName.DJI_RS_2 to DJIGimbalController(),
        DeviceName.PILOTFLY to PilotflyGimbalController(),
        DeviceName.NO_OP_CAMERA to NoOpCameraController(),
        DeviceName.NO_OP_GIMBAL to NoOpGimbalController()
    )

    var screenChanged = false

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onResume() {
        super.onResume()

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
            val cameraController = controllers.get(chosenCamera) as Device
            cameraController.setIp(db.getCamera().ipAddress)

            // Update gimbal controller
            chosenGimbal = db.getGimbal().deviceName
            val gimbalController = controllers.get(chosenGimbal) as Device
            gimbalController.setIp(db.getGimbal().ipAddress)

            if (chosenCamera == DeviceName.NO_OP_CAMERA) {
                (controllers.get(chosenCamera) as NoOpCameraController).textView = transcription

                transcription.setZ(2.toFloat())

            }

            if (chosenGimbal == DeviceName.NO_OP_GIMBAL) {
                (controllers.get(chosenGimbal) as NoOpGimbalController).textView = transcription
                transcription.apply {
                    setZ(2.toFloat())
                    textSize = 20F
                    setTextColor(Color.parseColor("#ffffff"))
                    setBackgroundColor(Color.parseColor("#0f0f0f"))
                }

            }
            createButtons()
            initializeObservers()

            screenChanged = false
        }

    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Toast.makeText(this, "MAIN", Toast.LENGTH_SHORT).show()

        val systemManager = SystemManager()
        val featuresManager = systemManager.getFeaturesManager()

        // To render the configuration view
        configurationButton?.setOnClickListener{

            screenChanged = true
            Intent(this, ConfigurationActivity::class.java).also {
               it.putExtra("EXTRA_FEATURES_MANAGER", featuresManager)
               startActivity(it)
           }

        }

        // Create buttons
        createButtons()
        initializeObservers()

        stt.initialize(true, model)

        // Must start STT upon user interaction
        //
        startTranscription.setOnClickListener {


            startTranscription.text = "VOICE CONTROL: ON"

            try {
                val permissions: Array<String> = arrayOf(android.Manifest.permission.RECORD_AUDIO, android.Manifest.permission.INTERNET)
                requestPermissions( permissions, 101)

                stt.openStream()

            }catch (e: ActivityNotFoundException){
                Toast.makeText(this, "Your device does not support STT.", Toast.LENGTH_LONG).show()
                e.printStackTrace()
            }
            
//            // Refactor, create toggle feature
            if (!stt.isActive) {



                stt.isActive = true
            }else {
                startTranscription.text = "VOICE CONTROL: OFF"

                try {
                    stt.closeStream()

                }catch (e: ActivityNotFoundException){
                    Toast.makeText(this, "Your device does not support STT.", Toast.LENGTH_LONG).show()
                    e.printStackTrace()
                }

                stt.isActive = false

            }

//            val camera = CanonCameraController()
//
//            Timer().scheduleAtFixedRate(object : TimerTask() {
//                override fun run() {
////                    getImage()
//                    camera.getLiveviewFlip(this@MainActivity::setBm)
//
//
//                }
//            },0 ,100)

        }

        btnConnectCamera.setOnClickListener {

            val camera = controllers.get(chosenCamera) as com.example.speechtotext.devicecontroller.Camera

            camera?.configureLiveview(this@MainActivity::setImageView)

        }


    }

    fun setImageView(camera: com.example.speechtotext.devicecontroller.Camera) {

        Timer().scheduleAtFixedRate(object : TimerTask() {
            override fun run() {

                try{

                    camera.getLiveviewFlip(this@MainActivity::setBm)
                }catch (e: Exception) {
                    Toast.makeText(applicationContext, "Camera IP is incorrect", Toast.LENGTH_LONG)

                    // Stop timer
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

    fun initializeObservers() {

        model.removeAllObservers()
        observers!!.forEach {

            val observer = it.value

            var isChosenControl = buttons.containsKey(it.key)
            if (isChosenControl) {


                if (observer.getUIController() == null) {

                    val uiController = UIController(buttons[it.key]!!, AdaptiveParameterButtonBar(parameterButtonCreatorLayout, applicationContext))

                    observer.setUIController(uiController)

                }else{

                    observer.getUIController()!!.button = buttons[it.key]!!
                }
                val uiController = observer.getUIController()

                uiController!!.setFeature(it.key, observer.getControlParameters(), model::newWord, observer::onParameterClick)
                observer.setCameraController(controllers.get(chosenCamera)!! as com.example.speechtotext.devicecontroller.Camera)
                observer.setGimbalController(controllers.get(chosenGimbal)!! as Gimbal)
                model.addObserver(observer)
            }
        }
    }
}