package com.example.speechtotext

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import android.widget.TextView
import java.util.*
import kotlin.coroutines.suspendCoroutine


class SpeechToTextEngine(val applicationContext: Context): RecognitionListener {

    val TAG = "SPEECH_ENGINE"
    private var sttIntent: Intent? = null
    private var speechRecognizer: SpeechRecognizer? = null
    var isOnPartial:Boolean = false
    var model: SpeechToTextEngineObserver? = null

    fun initialize(isOnPartial: Boolean, model: SpeechToTextEngineObserver) {

        this.isOnPartial = isOnPartial
        this.model = model

        sttIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)

        sttIntent!!.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)

        // Language model defines the purpose, there are special models for other use cases, like search.
        sttIntent!!.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)

        // Adding an extra language, you can use any language from the Locale class(These are the ones that you have downloaded in your phone).
        sttIntent!!.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())

        sttIntent!!.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5);

        sttIntent!!.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,
            applicationContext.getPackageName());
    }

    fun openStream() {

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(applicationContext)

        speechRecognizer!!.setRecognitionListener(this)

        speechRecognizer!!.startListening(sttIntent!!)

    }

    fun closeStream() {

        speechRecognizer!!.stopListening()
    }

    /*Speech Listener Methods*/

    override fun onReadyForSpeech(params: Bundle?) {
        Log.d(TAG, "onReadyForSpeech")
    }

    override fun onBeginningOfSpeech() {
        Log.d(TAG, "onBeginningOfSpeech")
    }

    override fun onRmsChanged(rmsdB: Float) {
//        Log.d(TAG, "onRmsChanged")

    }

    override fun onBufferReceived(buffer: ByteArray?) {
        Log.d(TAG, "onBufferReceived")
    }

    override fun onEndOfSpeech() {

        Log.d(TAG, "onEndofSpeech")
    }

    override fun onError(error: Int) {
//        Log.e(TAG, "error $error")

        // Ignore error and restart engine
        speechRecognizer!!.startListening(sttIntent!!)

    }

    override fun onResults(results: Bundle) {

        val result = results.getStringArrayList("results_recognition")

        Log.d(TAG, "onResults; Results: " + results.toString() )
        if (!result.isNullOrEmpty()) {

            // Most probable recognized text is in the first position.
            val recognizedText = result[0]

            // Give each word to callback
            notifyModel(recognizedText)

            // Restart engine
            speechRecognizer!!.startListening(sttIntent!!)

        }
    }

    override fun onPartialResults(partialResults: Bundle?) {

        val result = partialResults?.getStringArrayList("results_recognition")

        Log.d(TAG, "onPartialResults; Results: " + partialResults?.toString() )
        if (!result.isNullOrEmpty()) {

            // Most probable recognized text is in the first position.
            val recognizedText = result[0]

            if (isOnPartial) {

                notifyModel(recognizedText)
            }


        }
    }

    override fun onEvent(eventType: Int, params: Bundle?) {
        Log.d(TAG, "onEvent $eventType")
    }

    fun notifyModel(recognizedText: String) {

        val words = recognizedText.lowercase().trim().split(" ")

        words.forEach {

            // Give each word to callback
            model!!.newWord(it)
        }
    }

}