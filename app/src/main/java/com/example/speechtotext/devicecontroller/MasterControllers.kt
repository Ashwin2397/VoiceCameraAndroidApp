package com.example.speechtotext.devicecontroller

import android.graphics.Bitmap
import com.example.speechtotext.DeviceName
import com.example.speechtotext.Word

object MasterCamera {

    val factory = MasterControllerFactory()
    var chosenCamera = DeviceName.CANON



    fun shoot(word: Word) {

        factory.getCameraInstance(chosenCamera).shoot()
    }

    fun setZoom(word: Word) {

        // Change parameter for setZoom to a string
        factory.getCameraInstance(chosenCamera).setZoom(word.value)
    }

    fun setAperture(word: Word) {

        // Change parameter for setAperture to a float
        factory.getCameraInstance(chosenCamera).setAperture(word.value)
    }

    fun setFocusType(word: Word) {

        factory.getCameraInstance(chosenCamera).setFocusType(word.value)
    }

    fun setMode(word: Word) {}
}

object MasterGimbal {

    val factory = MasterControllerFactory()
    var chosenGimbal = DeviceName.DJI_RS_2

    fun move(word: Word) {

        // Implement logic later on ...
        factory.getGimbalInstance(chosenGimbal).move(word.value,"0", "0", 1)
    }
}