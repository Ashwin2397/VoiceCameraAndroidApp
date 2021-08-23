package com.example.speechtotext

import java.util.*

enum class DeviceType {
    GIMBAL,
    CAMERA,
    ANY,
    UNDEFINED
}

enum class ConnectionType {
    BLUETOOTH,
    HTTP,
    NATIVE
}

enum class DeviceName {
    DJI_RS_2,
    PILOTFLY,
    CANON,
    NATIVE,
    ANY,
    UNDEFINED,
    NO_OP,
    NO_OP_CAMERA,
    NO_OP_GIMBAL
}

enum class InputType {
    COMMAND,
    COMMAND_1,
    COMMAND_2,
    OTHER_COMMAND,
    PARAMETER,
    NUMERICAL_PARAMETER,
    POINT,
    ANY,
    UNDEFINED
}

enum class Feature {
    ZOOM,
    SHOOT,
    FOCUS,
    APERTURE,
    MODE,
    FOCUS_TYPE,
    LIVEVIEW_FLIP,
    LEFT,
    MOVE,
    ABSOLUTE_MOVEMENT,
    INCREMENTAL_MOVEMENT,
    ANY,
    UNDEFINED,
    ROLL,
    ROLL_POSITIVE,
    DOWN,
    UP,
    RIGHT,
}

enum class ImageType {
    JPEG,
    PNG,
    SVG
}

val firstCommands = mapOf<String, Word>(
    "zoom" to Word("zoom", Feature.ZOOM, InputType.COMMAND_1, DeviceType.CAMERA),
    "boom" to Word("zoom", Feature.ZOOM, InputType.COMMAND_1, DeviceType.CAMERA),
    "doom" to Word("zoom", Feature.ZOOM, InputType.COMMAND_1, DeviceType.CAMERA),
    "resume" to Word("zoom", Feature.ZOOM, InputType.COMMAND_1, DeviceType.CAMERA),
    "shoot" to Word("shoot", Feature.SHOOT, InputType.COMMAND_1, DeviceType.CAMERA),
    "soot" to Word("shoot", Feature.SHOOT, InputType.COMMAND_1, DeviceType.CAMERA),
    "chute" to Word("shoot", Feature.SHOOT, InputType.COMMAND_1, DeviceType.CAMERA),
    "shute" to Word("shoot", Feature.SHOOT, InputType.COMMAND_1, DeviceType.CAMERA),
    "suit" to Word("shoot", Feature.SHOOT, InputType.COMMAND_1, DeviceType.CAMERA),
    "focus" to Word("focus", Feature.FOCUS, InputType.COMMAND_1, DeviceType.CAMERA),
    "left" to Word("left", Feature.LEFT, InputType.COMMAND_1, DeviceType.CAMERA),
    "right" to Word("right", Feature.RIGHT, InputType.COMMAND_1, DeviceType.CAMERA),
    "up" to Word("up", Feature.UP, InputType.COMMAND_1, DeviceType.CAMERA),
    "app" to Word("up", Feature.UP, InputType.COMMAND_1, DeviceType.CAMERA),
    "op" to Word("up", Feature.UP, InputType.COMMAND_1, DeviceType.CAMERA),
    "hop" to Word("up", Feature.UP, InputType.COMMAND_1, DeviceType.CAMERA),
    "down" to Word("down", Feature.DOWN, InputType.COMMAND_1, DeviceType.CAMERA),
    "roll" to Word("roll", Feature.ROLL, InputType.COMMAND_1, DeviceType.CAMERA),
    "rule" to Word("roll", Feature.ROLL, InputType.COMMAND_1, DeviceType.CAMERA),
    "roehl" to Word("roll", Feature.ROLL, InputType.COMMAND_1, DeviceType.CAMERA),
    "aperture" to Word("aperture", Feature.APERTURE, InputType.COMMAND_1, DeviceType.CAMERA),
    "focus" to Word("focus", Feature.FOCUS, InputType.COMMAND_1, DeviceType.CAMERA),
    "mode" to Word("mode", Feature.MODE, InputType.COMMAND_1, DeviceType.CAMERA),

    )

val numericalParameters = mapOf<String, Word>(
    "1" to Word("1", Feature.ANY, InputType.NUMERICAL_PARAMETER, DeviceType.ANY),
    "2" to Word("2", Feature.ANY, InputType.NUMERICAL_PARAMETER, DeviceType.ANY),
    "3" to Word("3", Feature.ANY, InputType.NUMERICAL_PARAMETER, DeviceType.ANY),
    "one" to Word("1", Feature.ANY, InputType.NUMERICAL_PARAMETER, DeviceType.ANY),
    "what" to Word("1", Feature.ANY, InputType.NUMERICAL_PARAMETER, DeviceType.ANY),
    "too" to Word("2", Feature.ANY, InputType.NUMERICAL_PARAMETER, DeviceType.ANY),
    "to" to Word("2", Feature.ANY, InputType.NUMERICAL_PARAMETER, DeviceType.ANY),
    "two" to Word("2", Feature.ANY, InputType.NUMERICAL_PARAMETER, DeviceType.ANY),
    "three" to Word("3", Feature.ANY, InputType.NUMERICAL_PARAMETER, DeviceType.ANY),
    "tree" to Word("3",Feature.ANY, InputType.NUMERICAL_PARAMETER, DeviceType.ANY),
    "tea" to Word("3", Feature.ANY, InputType.NUMERICAL_PARAMETER, DeviceType.ANY),
)

val masterDictionary = mapOf<String, Word>(
    "zoom" to Word("zoom", Feature.ZOOM, InputType.COMMAND, DeviceType.CAMERA),
    "shoot" to Word("shoot", Feature.SHOOT, InputType.COMMAND, DeviceType.CAMERA),
    "1" to Word("1", Feature.ANY, InputType.NUMERICAL_PARAMETER, DeviceType.ANY),
    "2" to Word("2", Feature.ANY, InputType.NUMERICAL_PARAMETER, DeviceType.ANY),
    "too" to Word("2", Feature.ANY, InputType.NUMERICAL_PARAMETER, DeviceType.ANY),
    "two" to Word("2", Feature.ANY, InputType.NUMERICAL_PARAMETER, DeviceType.ANY),
    "one" to Word("1", Feature.ANY, InputType.NUMERICAL_PARAMETER, DeviceType.ANY),
    "aperture" to Word("aperture", Feature.APERTURE, InputType.COMMAND_1, DeviceType.CAMERA),
    "focus" to Word("focus", Feature.FOCUS, InputType.COMMAND_1, DeviceType.CAMERA),
    "mode" to Word("mode", Feature.MODE, InputType.COMMAND_1, DeviceType.CAMERA),
    "photo" to Word("photo", Feature.MODE, InputType.PARAMETER, DeviceType.CAMERA),
    "movie" to Word("movie", Feature.MODE, InputType.PARAMETER, DeviceType.CAMERA),
    "point" to Word("point", Feature.FOCUS, InputType.PARAMETER, DeviceType.CAMERA),
    "face" to Word("face", Feature.FOCUS, InputType.PARAMETER, DeviceType.CAMERA),
    "spot" to Word("spot", Feature.FOCUS, InputType.PARAMETER, DeviceType.CAMERA),
    "left" to Word("left", Feature.LEFT, InputType.COMMAND_1, DeviceType.CAMERA),
    "right" to Word("right", Feature.RIGHT, InputType.COMMAND_1, DeviceType.CAMERA),
    "up" to Word("up", Feature.UP, InputType.COMMAND_1, DeviceType.CAMERA),
    "down" to Word("down", Feature.DOWN, InputType.COMMAND_1, DeviceType.CAMERA),
    "roll" to Word("roll", Feature.ROLL, InputType.COMMAND_1, DeviceType.CAMERA),

    )