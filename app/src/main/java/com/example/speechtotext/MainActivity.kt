package com.example.speechtotext


import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.hardware.Camera
import android.os.Build
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.SpeechRecognizer
import android.util.Log
import android.view.Display
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.activity_main.*
import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.net.URL
import java.util.*


const val BASE_URL = "http://10.0.0.97:8080"

class MainActivity : AppCompatActivity(), Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

    /*
        // Get the Intent action
        val sttIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)

        sttIntent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        // Language model defines the purpose, there are special models for other use cases, like search.
        sttIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        // Adding an extra language, you can use any language from the Locale class(These are the ones that you have downloaded in your phone).
        sttIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())

        sttIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5);
        // Text that shows up on the Speech input prompt.
//            sttIntent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak now!")

        sttIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,
          applicationContext.getPackageName());

        // Add custom listeners.
        // Add custom listeners.

        val sr = SpeechRecognizer.createSpeechRecognizer(applicationContext)
        val speechCallback = SpeechCallback(sr, sttIntent)

        val listener = CustomRecognitionListener(transcription, sr, sttIntent, speechCallback)
        sr.setRecognitionListener(listener)*/

        /*DO HTTP REQUESTS HERE */


//        print(r.headers)
//        val stt = SpeechToTextEngine(applicationContext)
//        val model = Model(transcription)
//        val adaptiveParameterButtonBar = AdaptiveParameterButtonBar()
//        val uiController = UIController(shootImage, adaptiveParameterButtonBar)
//        val shootObserver = ShootObserver(uiController)
//
//        // Pass random functions for simulation purposes only
//        uiController.setFeature(Feature.SHOOT, shootObserver::reset, shootObserver::reset)
//
//        model.addObserver(shootObserver)
//
//        stt.initialize(true, model)
//
//        startTranscription.setOnClickListener {
//            transcription.text = "BUTTON CLICKED"
//
//
//            try {
//                val permissions: Array<String> = arrayOf(android.Manifest.permission.RECORD_AUDIO, android.Manifest.permission.INTERNET)
//                requestPermissions( permissions, 101)
//
//                stt.openStream()
//
//            }catch (e: ActivityNotFoundException){
//                e.printStackTrace()
//            }

//            try {
//                // Start the intent for a result, and pass in our request code.
//            } catch (e: ActivityNotFoundException) {
//                // Handling error when the service is not available.
//                e.printStackTrace()
//                Toast.makeText(this, "Your device does not support STT.", Toast.LENGTH_LONG).show()
//            }

        }
    }

    private fun getImage() {


        Glide.with(applicationContext)
            .load("http://10.0.0.97:8080/ccapi/ver100/shooting/liveview/flip")
            .into(imageView);
//
////        val retroFitBuilder = Retrofit.Builder()
////            .addConverterFactory(GsonConverterFactory.create(
////                GsonBuilder().setLenient().create()
////            ))
////            .baseUrl(BASE_URL)
////            .build()
////            .create(ApiInterface::class.java)
//        // Converters are for both requests and responses
//        val retroFitBuilder = Retrofit.Builder()
//            .baseUrl(BASE_URL)
//            .addConverterFactory(ScalarsConverterFactory.create())
//            .build()
//            .create(ApiInterface :: class.java)
//
//        val data = retroFitBuilder.getLiveViewFlip()
//
//        data.enqueue(object : Callback<String?> {
//            @RequiresApi(Build.VERSION_CODES.O)
//            override fun onResponse(call: Call<String?>, response: Response<String?>) {
//
//                Log.d("RESPONSE", response.body().toString())
//
//
////                val responseBody = response.body()!!.byteInputStream()
//
////                val imageView = findViewById<ImageView>(R.id.imageView)
////
////                val byteArray = response.body()!!
////                    .chunked(2)
////                    .map { it(2).toByte() }
////                    .toByteArray()
////                val imageByteArray = Base64.getDecoder().decode(response.body()!!.toString())
//                    val inputStream: InputStream = response.body().toString().byteInputStream()
//
//
////
////                val imageBm = BitmapFactory.decodeByteArray(imageByteArray)
//                val imageBm = BitmapFactory.decodeStream(inputStream)
//
//                imageView.setImageBitmap(imageBm)
//                runOnUiThread( Runnable {
//
//
//                    fun run(){
//                        Log.d("IMAGE_SET", "In here")
//                        imageView.setImageBitmap(imageBm)
//                    }
//                })
//            }
//
//            override fun onFailure(call: Call<String?>, t: Throwable) {
//                Log.d("RESPONSE_ERROR", t.message?:"Error: No Error message")
//            }
//        })


    }
//    private fun shoot() {
//
//        val retroFitBuilder = Retrofit.Builder()
//            .baseUrl(BASE_URL)
//            .addConverterFactory(GsonConverterFactory.create())
//            .addConverterFactory(ScalarsConverterFactory.create())
//            .build()
//            .create(CanonCameraApiInterface :: class.java)
//
//        val data = retroFitBuilder.shoot(Focus(af = true))
//
//        data.enqueue(object : Callback<Empty?> {
//            override fun onResponse(call: Call<Empty?>, response: Response<Empty?>) {
//
//                Log.d("RESPONSE", response.body().toString())
//
//            }
//
//            override fun onFailure(call: Call<Empty?>, t: Throwable) {
//                Log.d("RESPONSE_ERROR", t.message?:"Error: No Error message")
//            }
//        })
//
//
//    }
//
//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//        val transcription = findViewById<TextView>(R.id.transcription)
//
//
//        when (requestCode) {
//            // Handle the result for our request code.
//            1 -> {
//                // Safety checks to ensure data is available.
//                if (resultCode == Activity.RESULT_OK && data != null) {
//                    // Retrieve the result array.
//                    val result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
//                    // Ensure result array is not null or empty to avoid errors.
//                    if (!result.isNullOrEmpty()) {
//                        // Recognized text is in the first position.
//                        val recognizedText = result[0]
//                        // Do what you want with the recognized text.
//                        transcription.setText(recognizedText)
//                    }
//                }
//            }
//        }
//    }
//
//    override fun onEndOfSpeech() {
//
//
//    }
//    fun initialize() {
//
//        setRecognitionListener
//        val speechRecognizer = SpeechRecognizer.createSpeechRecognizer(android.content.Context)
//    }
//
//    fun openStream() {}
//
//    fun closeStream() {}
//
//    fun transcribeWord() {}



}

class SpeechCallback(private val speechRecognizer: SpeechRecognizer,private val intent: Intent){

    fun onEndOfSpeech() {
        speechRecognizer.startListening(intent)

    }

}

class CustomRecognitionListener(private val transcription: TextView, private val speechRecognizer: SpeechRecognizer,private val intent: Intent, val cb: SpeechCallback): RecognitionListener {

    val TAG = "SPEECH_LISTENER"
    override fun onReadyForSpeech(params: Bundle?) {
        Log.d(TAG, "onReadyForSpeech")
    }

    override fun onBeginningOfSpeech() {
        Log.d(TAG, "onBeginningOfSpeech")
    }

    override fun onRmsChanged(rmsdB: Float) {

    }

    override fun onBufferReceived(buffer: ByteArray?) {
        Log.d(TAG, "onBufferReceived")
    }

    override fun onEndOfSpeech() {

//        val permissions: Array<String> = arrayOf(android.Manifest.permission.RECORD_AUDIO)
//        requestPermissions(permissions, 101)

//        speechRecognizer.setRecognitionListener(this)
//        cb.onEndOfSpeech()

        Log.d(TAG, "onEndofSpeech")
    }

    override fun onError(error: Int) {
        Log.e(TAG, "error $error")
//        conversionCallaback.onErrorOccured(TranslatorUtil.getErrorText(error))
        speechRecognizer.startListening(intent)

    }

    override fun onResults(results: Bundle) {

        val result = results.getStringArrayList("results_recognition")

        Log.d(TAG, "onResults; Results: " + results.toString() )
        if (!result.isNullOrEmpty()) {
            // Recognized text is in the first position.
            val recognizedText = result[0]
            // Do what you want with the recognized text.
            transcription.setText(recognizedText)

            speechRecognizer.startListening(intent)

        }




    }

    override fun onPartialResults(partialResults: Bundle?) {
        Log.d(TAG, "onPartialResults")

        val result = partialResults?.getStringArrayList("results_recognition")

        Log.d(TAG, "onResults; Results: " + partialResults?.toString() )
        if (!result.isNullOrEmpty()) {
            // Recognized text is in the first position.
            val recognizedText = result[0]

            // Do what you want with the recognized text.
            transcription.setText(recognizedText)

        }
    }

    override fun onEvent(eventType: Int, params: Bundle?) {
        Log.d(TAG, "onEvent $eventType")
    }

}

/** A basic Camera preview class */
class CameraPreview(
    context: Context,
    private val mCamera: Camera
) : SurfaceView(context), SurfaceHolder.Callback {

    val TAG = "CAMERA_PREVIEW"

    private val mHolder: SurfaceHolder = holder.apply {
        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        addCallback(this@CameraPreview)
        // deprecated setting, but required on Android versions prior to 3.0
        setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS)
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        // The Surface has been created, now tell the camera where to draw the preview.
        mCamera.apply {
            try {
                setPreviewDisplay(holder)
                startPreview()
            } catch (e: IOException) {
                Log.d(TAG, "Error setting camera preview: ${e.message}")
            }
        }
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        // empty. Take care of releasing the Camera preview in your activity.
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, w: Int, h: Int) {
        // If your preview can change or rotate, take care of those events here.
        // Make sure to stop the preview before resizing or reformatting it.
        if (mHolder.surface == null) {
            // preview surface does not exist
            return
        }

        // stop preview before making changes
        try {
            mCamera.stopPreview()
        } catch (e: Exception) {
            // ignore: tried to stop a non-existent preview
        }

        // set preview size and make any resize, rotate or
        // reformatting changes here

        // start preview with new settings
        mCamera.apply {
            try {
                setPreviewDisplay(mHolder)
                startPreview()
            } catch (e: Exception) {
                Log.d(TAG, "Error starting camera preview: ${e.message}")
            }
        }
    }
}