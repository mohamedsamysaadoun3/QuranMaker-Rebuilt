package hazem.nurmontage.videoquran.constant

enum class TransitionType(val value: String) {
    NONE("none"),
    FADE("fade"),
    DISSOLVE("dissolve"),
    WIPE_RIGHT("wipeRight"),
    WIPE_LEFT("wipeLeft"),
    WIPE_UP("wipeUp"),
    WIPE_DOWN("wipeDown"),
    SLIDE_LEFT("slideLeft"),
    SLIDE_RIGHT("slideRight"),
    SLIDE_TOP("slideTop"),
    SLIDE_BOTTOM("slideBottom"),
    ZOOM_IN("zoomIn"),
    ZOOM_OUT("zoomOut"),
    ROTATE("rotate"),
    BLUR("blur"),
    PIXELATE("pixelate"),
    FLASH("flash"),
    INVERT("invert"),
    SEPIA("sepia"),
    BRIGHTNESS("brightness"),
    CIRCLE_CROP("circleCrop"),
    RECT_CROP("rectCrop"),
    SLIDE_FADE("slideFade"),
    ZOOM_FADE("zoomFade"),
    ROTATE_FADE("rotateFade"),
    FLIP_HORIZONTAL("flipH"),
    FLIP_VERTICAL("flipV"),
    SHAKE("shake"),
    GLITCH("glitch"),
    MORPH("morph");

    companion object {
        fun fromValue(value: String): TransitionType =
            entries.find { it.value == value } ?: NONE
    }
}
