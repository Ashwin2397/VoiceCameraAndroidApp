package com.assistivetechlab.speechtotext.devicecontroller

import android.content.Context
import android.graphics.Color
import android.os.Build
import android.widget.TextView
import androidx.annotation.RequiresApi
import com.assistivetechlab.speechtotext.ConnectionType
import com.assistivetechlab.speechtotext.DeviceName
import com.assistivetechlab.speechtotext.Feature
import com.assistivetechlab.speechtotext.MainActivity
import kotlinx.android.synthetic.main.activity_main.*
import java.lang.ref.WeakReference

object NoOpGimbalController: Device, Gimbal {

    private val featuresAvailable = arrayListOf<Feature>(Feature.INCREMENTAL_MOVEMENT, Feature.HOME, Feature.PORTRAIT, Feature.LANDSCAPE)
    private val deviceName = DeviceName.NO_OP_GIMBAL
    private val connectionType = ConnectionType.NATIVE

    lateinit var context: WeakReference<Context>
    lateinit var textView: WeakReference<TextView>

    override fun setApplicationContext(context: Context) {
        this.context = WeakReference(context)
    }

    override fun getDeviceName(): DeviceName {
        return this.deviceName

    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun connectDevice(mainActivity: MainActivity) {

        textView = WeakReference(mainActivity.transcription)
        mainActivity.transcription.apply {
            setZ(2.toFloat())
            textSize = 20F
            setTextColor(Color.parseColor("#ffffff"))
            setBackgroundColor(Color.parseColor("#0f0f0f"))
        }
    }

    override fun getConnectionType(): ConnectionType {
        return this.connectionType

    }

    override fun isFeatureAvailable(feature: Feature): Boolean {
        if ( this.featuresAvailable.contains(feature) ) {
            return true;
        }

        return false;
    }

    override fun getAvailableFeatures(): ArrayList<Feature> {
        return this.featuresAvailable

    }

    override fun setIp(ip: String) {

    }

    override fun move(coordinates: Map<MasterGimbal.Axis, Int>, isAbsolute: Boolean) {
        this.textView.get()?.text = "YAW: ${coordinates[MasterGimbal.Axis.YAW]}\nPITCH: ${coordinates[MasterGimbal.Axis.PITCH]}\nROLL: ${coordinates[MasterGimbal.Axis.ROLL]}\n"

    }
}