package com.example.speechtotext.devicecontroller

import android.content.Context
import android.graphics.Color
import android.os.Build
import android.widget.TextView
import androidx.annotation.RequiresApi
import com.example.speechtotext.ConnectionType
import com.example.speechtotext.DeviceName
import com.example.speechtotext.Feature
import com.example.speechtotext.MainActivity
import kotlinx.android.synthetic.main.activity_main.*
import java.lang.ref.WeakReference

object NoOpGimbalController: Device, Gimbal {

    private val featuresAvailable = arrayListOf<Feature>(Feature.LEFT, Feature.RIGHT, Feature.UP, Feature.DOWN, Feature.ROLL)
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

    override fun move(yaw: String, pitch: String, roll: String, isAbsolute: Int) {
        this.textView.get()?.text = "YAW: ${yaw}\nPITCH: ${pitch}\nROLL: ${roll}\n"

    }
}