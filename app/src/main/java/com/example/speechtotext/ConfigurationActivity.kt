package com.example.speechtotext

import android.app.Activity
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatDelegate
import com.example.speechtotext.manager.Features
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

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_configuration)

        val features = intent.getSerializableExtra("EXTRA_FEATURES") as Features

        val cameraUiController = UiController(
            DeviceType.CAMERA,
            spCameras,
            etCameraIp,
            llCameraControls,
            features.availableCameraFeatures,
            features.isHttpDevice,
            applicationContext
        )

        cameraUiController.init()

        val gimbalUiController = UiController(
            DeviceType.GIMBAL,
            spGimbals,
            etGimbalIp,
            llGimbalControls,
            features.availableGimbalFeatures,
            features.isHttpDevice,
            applicationContext
        )

        gimbalUiController.init()

        CommitView(
            btnSave,
            btnCancel,
            this,
            cameraUiController,
            gimbalUiController
            ).init()


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

                this.activity.finish()
            }

            btnCancel.setOnClickListener {

                Log.d("CANCEL", "Camera chosen: ${database.getCamera().deviceName.toString()}")
                this.activity.finish()
            }

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

        val NUMBER_ROWS = 3
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

                if (i%NUMBER_ROWS == 0) {

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