package com.assistivetechlab.speechtotext



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
    UNDEFINED,
    HEADER,
    DEVICE,
    PARAMETER_TRUE,
    PARAMETER_FALSE
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
    ROLL_NEGATIVE,
    ROLL_POSITIVE,
    DOWN,
    UP,
    RIGHT,
    PORTRAIT,
    HOME,
    LANDSCAPE,
}

enum class ImageType {
    JPEG,
    PNG,
    SVG
}

enum class Header {
    CAMERA,
    CONTROL,
    SETTINGS,
    UNDEFINED,
    GIMBAL,

}
/*

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
*/

var similarWords = mapOf<String, String>(

    "shoot" to "shoot",
    "chute" to "shoot",
    "suit" to "shoot",
    "shute" to "shoot",

    "zoom" to "zoom",
    "boom" to "zoom",
    "doom" to "zoom",
    "resume" to "zoom",

    "camera" to "camera",
    "tamara" to "camera",
    "kamera" to "camera",
    "camra" to "camera",

    "gimbal" to "gimbal",
    "jimbo" to "gimbal",
    "kimbo" to "gimbal",
    "gimbel" to "gimbal",
    "gimble" to "gimbal",

    "portrait" to "portrait",

    "landscape" to "landscape",

    "home" to "home",
    "hom" to "home",
    "holme" to "home",

    "control" to "control",

    "settings" to "settings",

    "focus" to "focus",
    "focussed" to "focus",
    "forecast" to "focus",
    "pocus" to "focus",

    "mode" to "mode",
    "mowed" to "mode",
    "mood" to "mode",
    "moad" to "mode",

    "movie" to "movie",

    "photo" to "photo",
    "foto" to "photo",
    "toto" to "photo",

    "point" to "point",
    "pointe" to "point",
    "poynt" to "point",

    "face" to "face",
    "bass" to "face",
    "base" to "face",

    "spot" to "spot",
    "stop" to "spot",
    "thought" to "spot",

    "left" to "left",
    "lift" to "left",

    "right" to "right",
    "write" to "right",
    "wright" to "right",
    "rite" to "right",

    "up" to "up",
    "app" to "up",
    "op" to "up",
    "hop" to "up",

    "down" to "down",
    "bound" to "down",
    "downed" to "down",

    "roll" to "roll",
    "role" to "roll",
    "rol" to "roll",
    "roller" to "roll",
    "ruler" to "roll",

    "aperture" to "aperture",

    "one" to "1",
    "two" to "2",
    "three" to "3",
    "four" to "4",
    "five" to "5",
    "six" to "6",
    "seven" to "7",
    "eight" to "8",
    "nine" to "9",
    "ten" to "10",

)


var headersToCommands = mapOf<Header, List<String>>(
    Header.GIMBAL to listOf("portrait", "landscape", "home"),
    Header.CAMERA to listOf("control", "settings"),
    Header.CONTROL to listOf("zoom", "mode"),
    Header.SETTINGS to listOf("focus", "aperture"),
)

val words = mapOf<String, Word>(

    "camera" to Word("camera", Feature.UNDEFINED, Header.CAMERA, InputType.DEVICE, DeviceType.CAMERA, listOf()),
    "gimbal" to Word("gimbal", Feature.UNDEFINED, Header.GIMBAL, InputType.DEVICE, DeviceType.GIMBAL, listOf()),

//    "control" to Word("control", Feature.UNDEFINED, Header.CONTROL, InputType.HEADER, DeviceType.CAMERA, listOf(Header.CAMERA)),
//    "settings" to Word("settings", Feature.UNDEFINED, Header.SETTINGS, InputType.HEADER, DeviceType.CAMERA, listOf(Header.CAMERA)),

    /*GIBMAL COMMANDS*/
    "portrait" to Word("portrait", Feature.PORTRAIT, Header.UNDEFINED, InputType.COMMAND, DeviceType.GIMBAL, listOf(Header.GIMBAL)),
    "home" to Word("home", Feature.HOME, Header.UNDEFINED, InputType.COMMAND, DeviceType.GIMBAL, listOf(Header.GIMBAL)),
    "landscape" to Word("landscape", Feature.LANDSCAPE, Header.UNDEFINED, InputType.COMMAND, DeviceType.GIMBAL, listOf(Header.GIMBAL)),
    "left" to Word("left", Feature.LEFT, Header.UNDEFINED, InputType.COMMAND, DeviceType.GIMBAL, listOf(Header.GIMBAL)),
    "right" to Word("right", Feature.RIGHT, Header.UNDEFINED, InputType.COMMAND, DeviceType.GIMBAL, listOf(Header.GIMBAL)),
    "up" to Word("up", Feature.UP, Header.UNDEFINED, InputType.COMMAND, DeviceType.GIMBAL, listOf(Header.GIMBAL)),
    "down" to Word("down", Feature.DOWN, Header.UNDEFINED, InputType.COMMAND, DeviceType.GIMBAL, listOf(Header.GIMBAL)),
    "roll" to Word("roll", Feature.ROLL, Header.UNDEFINED, InputType.COMMAND, DeviceType.GIMBAL, listOf(Header.GIMBAL)),
    /*---*/

    /*CAMERA COMMANDS*/
    "zoom" to Word("zoom", Feature.ZOOM, Header.UNDEFINED, InputType.COMMAND, DeviceType.CAMERA, listOf(Header.CAMERA, Header.CONTROL)), // shooting/control/zoom
    "shoot" to Word("shoot", Feature.SHOOT, Header.UNDEFINED, InputType.COMMAND, DeviceType.CAMERA, listOf(Header.CAMERA)), // shooting/control/zoom
    "mode" to Word("mode", Feature.MODE, Header.UNDEFINED, InputType.COMMAND, DeviceType.CAMERA, listOf(Header.CAMERA, Header.CONTROL)),  // shooting/control/mode
    "focus" to Word("focus", Feature.FOCUS, Header.UNDEFINED, InputType.COMMAND, DeviceType.CAMERA, listOf(Header.CAMERA, Header.SETTINGS)), // shooting/settings/afmethod
    "aperture" to Word("aperture", Feature.APERTURE, Header.UNDEFINED, InputType.COMMAND, DeviceType.CAMERA, listOf(Header.CAMERA, Header.SETTINGS)), // shooting/settings/av
    /*---*/

    /*CAMERA PARAMETERS*/
    "movie" to Word("movie", Feature.MODE, Header.UNDEFINED, InputType.PARAMETER, DeviceType.CAMERA, listOf()),
    "photo" to Word("photo", Feature.MODE, Header.UNDEFINED, InputType.PARAMETER, DeviceType.CAMERA, listOf()),

    "point" to Word("point", Feature.FOCUS, Header.UNDEFINED, InputType.PARAMETER, DeviceType.CAMERA, listOf()),
    "face" to Word("face", Feature.FOCUS, Header.UNDEFINED, InputType.PARAMETER, DeviceType.CAMERA, listOf()),
    "spot" to Word("spot", Feature.FOCUS, Header.UNDEFINED, InputType.PARAMETER, DeviceType.CAMERA, listOf()),
    /*---*/
    )







