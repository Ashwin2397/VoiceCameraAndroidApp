package com.example.speechtotext


import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
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


class MainActivity : Activity(){

    val TAG = "MAIN_ACTIVITY"
    val db by lazy {
        Database(this)
    }

    // REFACTOR: Use factory design pattern
    val factory = MasterControllerFactory()

    var screenChanged = false

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val uiController = UIController(
            commandButtonCreatorLayout,
            parameterButtonCreatorLayout,
            mapOf(
                DeviceType.CAMERA to btnCamera,
                DeviceType.GIMBAL to btnGimbal
            ),
            this,
            HeaderView(llHeaders, this),
            mapOf(
                Feature.SHOOT to shootImage as Button,
                Feature.LEFT to btnLeft as Button,
                Feature.RIGHT to btnRight as Button,
                Feature.UP to btnUp as Button,
                Feature.DOWN to btnDown as Button,
                Feature.ROLL to btnRoll as Button
            )
        )

        val observer = DynamicObserver(
            uiController,
            MasterCamera,
            MasterGimbal,
            this
        )

        val systemManager = SystemManager()
        val features = systemManager.getFeatures()


        // Initialize all controllers for the first time
        factory.controllers.forEach {

            it.value.forEach {
                (it.value as Device).setApplicationContext(applicationContext)
            }
        }

        initControllers()

        // Initialize all required instances

        // Definitely alot faster on partial results
        // With isOnUnstable definitely produces unstable results
            // Example: "25" => "2", "20", "25" ...
        SpeechToTextEngine.applicationContext = WeakReference(applicationContext)
        SpeechToTextEngine.initialize(false, false, observer)

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

        btnConnectGimbal.setOnClickListener {

            val gimbal = factory.getGimbalInstance(MasterGimbal.chosenGimbal) as Device
            gimbal.connectDevice(this@MainActivity)
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

            // Only change controllers
            // Dont bother with the controls
            initControllers()

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

}