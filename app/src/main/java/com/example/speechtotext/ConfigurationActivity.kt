package com.example.speechtotext

import android.app.Activity
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.ContactsContract
import android.text.InputType
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.example.speechtotext.manager.FeaturesManager
import kotlinx.android.synthetic.main.activity_configuration.*

/*
* NOTE:
* - Improve view to allow only 1 camera and 1 gimbal selection from a dropdown list
* - Improve view to have a column for gimbal controls and camera controls that dynamically render upon change of selection of camera and gimbal from it's intitial selection
* - Improve controls such that gimbal controls and camera controls are stored separately    
* */

/*
* TODO:
*  - Once device from dropdown is chosen, we show the chosen device's controls
* -
* */
class ConfigurationActivity : AppCompatActivity() {

    var hasAdded = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_configuration)

        val featuresManager = intent.getSerializableExtra("EXTRA_FEATURES_MANAGER") as FeaturesManager

        val cameraUiController = UiController(
            DeviceType.CAMERA,
            spCameras,
            etCameraIp,
            llCameraControls,
            featuresManager.availableCameraFeatures,
            featuresManager.isHttpDevice,
            applicationContext
        )

        cameraUiController.init()

        val gimbalUiController = UiController(
            DeviceType.GIMBAL,
            spGimbals,
            etGimbalIp,
            llGimbalControls,
            featuresManager.availableGimbalFeatures,
            featuresManager.isHttpDevice,
            applicationContext
        )

        gimbalUiController.init()

        btnLoad.setOnClickListener {

            Log.d("CANCEL", "Camera chosen: ${Database(this).getCamera()}; Gimbal chosen: ${Database(this).getGimbal()}")
            Log.d("CANCEL", "Controls chosen: ${Database(this).getControls()}")

//                cancel()
        }

        CommitView(
            btnSave,
            btnCancel,
            this,
            cameraUiController,
            gimbalUiController
            ).init()

//        val uiControllerCamera = UiController(
//            spCameras,
//            etCameraIp,
//            llCameraControls,
//            mutableMapOf<String, ArrayList<Feature>>(
//                "Canon Camera" to arrayListOf(
//                    Feature.APERTURE,
//                    Feature.FOCUS,
//                    Feature.SHOOT,
//                    Feature.ZOOM,
//                    Feature.MODE
//                ),
//                "Native Camera" to arrayListOf(
//                    Feature.APERTURE,
//                    Feature.FOCUS,
//
//                ),
//            ),
//            mutableMapOf<String, Boolean>(
//                "Canon Camera" to true,
//                "Native Camera" to false
//            ),
//           applicationContext
//        )
//
//        uiControllerCamera.init()

//        val uiControllerGimbal = UiController(
//            spGimbals,
//            etGimbalIp,
//            llGimbalControls,
//            mutableMapOf<String, ArrayList<Feature>>(
//                "DJI Ronin" to arrayListOf(
//                    Feature.MOVE
//                ),
//                "Pilotfly" to arrayListOf(
//                    Feature.MOVE
//                    ),
//            ),
//            mutableMapOf<String, Boolean>(
//                "DJI Ronin" to true,
//                "Pilotfly" to false
//            ),
//            applicationContext
//        )
//
//        uiControllerGimbal.init()
//
//        var cameraSwitches = mutableMapOf<Feature, Switch>()
//        var gimbalSwitches = mutableMapOf<Feature, Switch>()
//
//
//        canonSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
//
//            Log.d("CANON_SWITCH", "CAnon button clicked ")
//
//            if (isChecked) {
//                llControls.removeAllViews()
//
//                featuresManager.availableCameraFeatures.get(DeviceName.CANON)?.forEach {
//
//                    // Remove current controls
//
//                    val switch = Switch(this)
//
//                    switch.layoutParams = LinearLayout.LayoutParams(
//                        LinearLayout.LayoutParams.WRAP_CONTENT,
//                        ViewGroup.LayoutParams.WRAP_CONTENT)
//
//                    switch.text = it.toString()
//
//                    cameraSwitches.put(it, switch)
//
//                    llControls.addView(switch)
//                }
//            }
//
//        }
//
//        roninGimbalSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
//
//            if (isChecked && !hasAdded) {
//
//                featuresManager.availableGimbalFeatures.get(DeviceName.DJI_RS_2)?.forEach {
//
//                    // Remove current controls
//
//                    val switch = Switch(this)
//
//                    switch.layoutParams = LinearLayout.LayoutParams(
//                        LinearLayout.LayoutParams.WRAP_CONTENT,
//                        ViewGroup.LayoutParams.WRAP_CONTENT)
//
//                    switch.text = it.toString()
//
//                    gimbalSwitches.put(it, switch)
//
//                    llControls.addView(switch)
//
//                }
//
//                hasAdded = true
//            }
//
//        }
//
//        saveBtn.setOnClickListener {
//
//
//        }

//        spCameras.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
//
//            override fun onItemSelected(
//                parent: AdapterView<*>?,
//                view: View?,
//                position: Int,
//                id: Long
//            ) {
//
//                Toast.makeText(applicationContext, "Selected item: ${parent?.getItemAtPosition(position).toString()}", Toast.LENGTH_SHORT).show()
//            }
//
//            override fun onNothingSelected(parent: AdapterView<*>?) {
//
//            }
//        }

    }

    class CommitView(
        val btnSave: Button,
        val btnCancel: Button,
        val activity: Activity,
        val cameraUiController: UiController,
        val gimbalUiController: UiController
    ){

        val database = Database(activity)

        /*
        * Add click listeners for both btnSave and btnCancel
        * */
        fun init() {

            btnSave.setOnClickListener {


                // Get chosen controls from the uiController object
                database.saveControls(cameraUiController.getChosenControls() + gimbalUiController.getChosenControls())

                val cameraDetails = cameraUiController.getChosenDevice()
                val gimbalDetails = gimbalUiController.getChosenDevice()
                database.saveDevices(cameraDetails, gimbalDetails)
            }

            btnCancel.setOnClickListener {

                Log.d("CANCEL", "Camera chosen: ${database.getCamera().deviceName.toString()}")
//                cancel()
            }

        }

        /*
        * Don't save the settings and finish() the current activity
        * */
        fun cancel() {

            this.activity.finish()
        }
    }

    class UiController(
        val deviceType: DeviceType,
        val spDevice: Spinner,
        val etIp: EditText,
        val llControls: LinearLayout,
        val deviceControls: Map<DeviceName, ArrayList<Feature>>,
        val deviceHasIp: Map<DeviceName , Boolean>,
        val context: Context
    ){

        var chosenDevice = ""
        var chosenControls = mutableMapOf<Feature, CheckBox>()
        var stringToEnum = mutableMapOf<String, DeviceName>()

        val NUMBER_COLUMNS = 3
        fun init() {

            this.spDevice.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {

                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    chosenDevice = parent?.getItemAtPosition(position).toString()

                    setIpView()
                    displayControls()
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {

                }
            }

            deviceControls.forEach {

                stringToEnum.put(it.key.toString(), it.key)
            }

        }

        private fun setIpView() {

            val hasIp = deviceHasIp.get(stringToEnum.get(chosenDevice)) ?: false

            etIp.apply {
                isFocusable = hasIp
                isFocusableInTouchMode = hasIp
                inputType = mapOf<Boolean, Int>(
                    false to InputType.TYPE_NULL,
                    true to InputType.TYPE_CLASS_TEXT
                ).get(hasIp)!!
            }
        }

        private fun displayControls() {

            this.llControls.removeAllViews()
            chosenControls.clear()

            val features = deviceControls.get(stringToEnum.get(chosenDevice))

            var verticalLinearLayout:LinearLayout? = null
            var i = 0

            features?.forEach {

                if (i%NUMBER_COLUMNS == 0) {

                    verticalLinearLayout = LinearLayout(context)
                    verticalLinearLayout!!.layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT)
                    verticalLinearLayout!!.orientation = LinearLayout.VERTICAL

                    this.llControls.addView(verticalLinearLayout)
                }

                val newFeature = CheckBox(context)

                newFeature.layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT)

                newFeature.text = it.toString()

                chosenControls.put(it, newFeature)

                verticalLinearLayout!!.addView(newFeature)
                i += 1
            }
        }

        fun getChosenControls(): ArrayList<Feature> {

            val chosenControlsList = arrayListOf<Feature>()

            chosenControls.forEach {

                if (it.value.isChecked) {
                    chosenControlsList.add(it.key)
                }
            }

            return chosenControlsList
        }

        fun getChosenDevice(): DeviceDetails {

            return DeviceDetails(DeviceName.valueOf(chosenDevice), etIp.text.toString())
        }
    }

}