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
    var observers = mutableMapOf<Feature, Any>(
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

        val systemManager = SystemManager(applicationContext)
        val featuresManager = systemManager.getFeaturesManager()

        // To render the configuration view
        configurationButton?.setOnClickListener{

            screenChanged = true
            Intent(this, ConfigurationActivity::class.java).also {
               it.putExtra("EXTRA_FEATURES_MANAGER", featuresManager)
               startActivity(it)
           }

        }

        factory.controllers.forEach {
            (it.value as Device).setApplicationContext(applicationContext)
        }


        // Create buttons
        createButtons()
        initializeObservers()

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

            // Update gimbal controller
            chosenGimbal = db.getGimbal().deviceName
            val gimbalController = factory.getDeviceInstance(chosenGimbal)
            gimbalController.setIp(db.getGimbal().ipAddress)

            if (chosenCamera == DeviceName.NO_OP_CAMERA) {
                (factory.controllers.get(chosenCamera) as NoOpCameraController).textView = WeakReference(transcription)

                transcription.setZ(2.toFloat())

            }

            if (chosenGimbal == DeviceName.NO_OP_GIMBAL) {
                (factory.controllers.get(chosenGimbal) as NoOpGimbalController).textView = WeakReference(transcription)
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

    fun initializeObservers() {

        chosenCamera = db.getCamera().deviceName
        chosenGimbal = db.getGimbal().deviceName

        Model.removeAllObservers()
        observers.forEach {

            val observer = it.value as Observer

            var isChosenControl = buttons.containsKey(it.key)
            if (isChosenControl) {


                if (observer.getUIController() == null) {

                    val uiController = UIController(buttons[it.key]!!, AdaptiveParameterButtonBar(parameterButtonCreatorLayout, applicationContext))

                    observer.setUIController(uiController)

                }else{

                    observer.getUIController()!!.button = buttons[it.key]!!
                }
                val uiController = observer.getUIController()

                uiController!!.setFeature(it.key, observer.getControlParameters(), Model::newWord, observer::onParameterClick)
                observer.setCameraController(factory.getCameraInstance(chosenCamera))
                observer.setGimbalController(factory.getGimbalInstance(chosenGimbal))

                if (it.value is ShootObserver) {

                    val shootObserver  = it.value as ShootObserver

                    shootObserver.setFsmStates()
                }
                Model.addObserver(observer)
            }
        }
    }
}