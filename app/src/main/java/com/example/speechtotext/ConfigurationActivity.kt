package com.example.speechtotext

import android.content.Context
import android.graphics.drawable.GradientDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.view.ViewGroup
import android.widget.*
import kotlinx.android.synthetic.main.activity_configuration.*

/*
* NOTE:
* - Improve view to allow only 1 camera and 1 gimbal selection from a dropdown list
* - Improve view to have a column for gimbal controls and camera controls that dynamically render upon change of selection of camera and gimbal from it's intitial selection
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
//
//        val featuresManager = intent.getSerializableExtra("EXTRA_FEATURES_MANAGER") as FeaturesManager
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

        val uiControllerCamera = UiController(
            spCameras,
            etCameraIp,
            llCameraControls,
            mutableMapOf<String, ArrayList<Feature>>(
                "Canon Camera" to arrayListOf(
                    Feature.APERTURE,
                    Feature.FOCUS,
                    Feature.SHOOT,
                    Feature.ZOOM,
                    Feature.MODE
                ),
                "Native Camera" to arrayListOf(
                    Feature.APERTURE,
                    Feature.FOCUS,

                ),
            ),
            mutableMapOf<String, Boolean>(
                "Canon Camera" to true,
                "Native Camera" to false
            ),
           applicationContext
        )

        uiControllerCamera.init()

        val uiControllerGimbal = UiController(
            spGimbals,
            etGimbalIp,
            llGimbalControls,
            mutableMapOf<String, ArrayList<Feature>>(
                "DJI Ronin" to arrayListOf(
                    Feature.MOVE
                ),
                "Pilotfly" to arrayListOf(
                    Feature.MOVE
                    ),
            ),
            mutableMapOf<String, Boolean>(
                "DJI Ronin" to true,
                "Pilotfly" to false
            ),
            applicationContext
        )

        uiControllerGimbal.init()
    }

    class ModelUi{

        var chosenDevice = ""
        var chosenDeviceIp = ""
        var observers = arrayListOf<ObserverUI>()

        fun notifyObservers() {

            observers.forEach {
                it.deviceChange(chosenDevice)
            }
        }

        fun setDevice(chosenDevice: String) {

            this.chosenDevice = chosenDevice

            notifyObservers()
        }

        fun getDevice(): String {

            return chosenDevice
        }
    }

    interface ObserverUI {
        fun deviceChange(newDevice: String): Unit
        fun init(): Unit
    }

    class EtIp(
        val etIp: EditText,
        val deviceHasIp: Map<String, Boolean>
    ): ObserverUI {

        override fun deviceChange(newDevice: String) {

            // Check if device needs an ip, if it does not, then disable the textbox
            val hasIp = deviceHasIp.get(newDevice) ?: false

            etIp.apply {
                isFocusable = hasIp
                isFocusableInTouchMode = hasIp
                inputType = mapOf<Boolean, Int>(
                    false to InputType.TYPE_NULL,
                    true to InputType.TYPE_CLASS_TEXT
                ).get(hasIp)!!
            }

        }

        override fun init() {

        }
    }

    class SpDevice(
        val spDevice: Spinner,
        val modelUi: ModelUi
    ): ObserverUI {

        override fun init() {

            // Set on item selected listener for spinner
            this.spDevice.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {

                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    val chosenDevice = parent?.getItemAtPosition(position).toString()
                    modelUi.setDevice(chosenDevice)

//                    Toast.makeText(applicationContext, "Selected item: ${parent?.getItemAtPosition(position).toString()}", Toast.LENGTH_SHORT).show()
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {

                }
            }

        }

        override fun deviceChange(newDevice: String) {
            // Upon device change, we render the device's list of controls

        }
    }

    class VisibleControls(
        llControls: LinearLayout
    ){

        fun addControls(controls: Map<DeviceName, ArrayList<Feature>>) {}
    }

    class CommitView(
        val btnSave: Button,
        val btnCancel: Button
    ){

        /*
        * Add click listeners for both btnSave and btnCancel
        * */
        fun init() {}

        /*
        * Get:
        * - chosen camera and it's ip
        * - chosen gimbal and it's ip
        * - chosen controls from the checkboxes
        * */
        fun save() {}

        /*
        * Don't save the settings and finish() the current activity
        * */
        fun cancel() {}
    }

    class UiController(
        val spDevice: Spinner,
        val etIp: EditText,
        val llControls: LinearLayout,
        val deviceControls: Map<String, ArrayList<Feature>>,
        val deviceHasIp: Map<String, Boolean>,
        val context: Context
    ){

        var chosenDevice = ""
        var chosenControls = mutableMapOf<Feature, CheckBox>()

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


        }

        private fun setIpView() {

            val hasIp = deviceHasIp.get(chosenDevice) ?: false

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

            val features = deviceControls.get(chosenDevice)

            var verticalLinearLayout:LinearLayout? = null
            var i = 0

            features?.forEach {

                if (i%3 == 0) {

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
    }

}