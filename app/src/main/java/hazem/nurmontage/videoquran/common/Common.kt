package hazem.nurmontage.videoquran.common

import android.graphics.Bitmap
import android.graphics.Rect
import hazem.nurmontage.videoquran.model.Gradient

object Common {

    //region Layout constants
    const val BLOCK_CORNER: Float = 0.5f
    const val BLOCK_HEIGHT: Float = 0.077f
    const val CIRCLE_SIZE: Float = 4.2f
    const val PADDING_BETWEEN_BLOCK: Float = 0.026f
    const val PADDING_BOTTOM: Float = 0.015f
    const val PADDING_LAYER: Float = 0.015f
    const val SIZE_IF_ONE: Float = 0.95f
    const val SIZE_IF_ONE_8_CHAR: Float = 0.7f
    const val SIZE_TEXT_TIME: Float = 0.0388f
    const val SIZE_TEXT_TIME_BORDER: Float = 0.027f
    const val SIZE_TEXT_TIME_BOTTOM: Float = 0.07f
    const val START_Y_BLOCK: Float = 0.18f
    const val STROKE_BORDER: Float = 0.013f
    const val aya_h: Float = 0.10071f
    const val surah_name_h: Float = 0.075f
    const val surah_name_w: Float = 0.4f
    //endregion

    //region Color constants
    const val COLOR_AYA: Int = -16441312
    const val COLOR_BLOCK_AUDIO: Int = -3042963
    const val COLOR_BLOCK_QURAN: Int = -5253382
    const val COLOR_BLOCK_TRSLATION: Int = -67133
    const val COLOR_LIGHT_TEXT: Int = -1
    const val COLOR_TRANSLATION: Int = -8780025
    const val COLOR_WAVE: String = "#522123"
    const val COLOR_WAVE_INT: Int = -11394781
    //endregion

    //region Font constants
    const val FONT_APP: String = "ReadexPro_Medium.ttf"
    const val FONT_APP_BOLD: String = "ReadexPro_Bold.ttf"
    const val FONT_APP_LIGHT: String = "ReadexPro-Regular.ttf"
    const val FONT_NUMBER: String = "NotoNaskhArabic.ttf"
    const val FONT_QURAN: String = "\u0639\u062B\u0645\u0627\u0646\u064A.otf"
    const val FONT_SURAH_NAME: String = "\u062E\u0637 \u0627\u0644\u0625\u0628\u0644.otf"
    const val FONT_TRANSLATION: String = "ReadexPro_Medium.ttf"
    const val FONT_TRANSLATION_AR: String = "\u062E\u0637 \u0627\u0644\u0625\u0628\u0644.otf"
    //endregion

    //region File / folder names
    const val LINE_BG: String = "line_bg.png"
    const val LINE_BG_TMP: String = "line_bg_tmp.png"
    const val LINE_PROGESS: String = "line_progress.png"
    const val SURAH_NAME: String = "surah_name.png"
    const val TEMPLATE: String = "template"
    const val TEMPLATE_TMP: String = "template_tmp"
    const val VIDEO_FRAME_FOLDER: String = "VideoFrame"
    //endregion

    //region Misc string constants
    const val ENHANCE_CMD: String =
        "equalizer=f=3000:t=h:width=200:g=2,compand=attacks=0.3:decays=0.8:points=-80/-80|-20/-10|0/-3"
    const val FADE_TIME: Float = 0.2f
    const val NUMBER_CHAR: String = "\u0646\u0635"
    const val READER: String = "reader"
    const val SURAH: String = "surah"
    //endregion

    //region iPad / square layout constants
    const val ipad_NEOMORPHIC: Float = 0.32f
    const val ipad_h: Float = 0.7601563f
    const val ipad_h_bottom: Float = 0.2f
    const val ipad_h_bottom_square: Float = 0.25f
    const val ipad_h_landscape: Float = 0.7601563f
    const val ipad_h_square: Float = 0.7601563f
    const val ipad_radius: Float = 0.12f
    const val ipad_w: Float = 0.56f
    const val ipad_w_bottom: Float = 0.75f
    const val ipad_w_bottom_square: Float = 0.7f
    const val ipad_w_landscape: Float = 0.56f
    const val ipad_w_square: Float = 0.56f
    const val lecture_h: Float = 0.1109f
    const val progress_h: Float = 0.064f
    const val square_h: Float = 1.13f
    const val square_h_landscape: Float = 1.13f
    const val square_h_no_radius: Float = 0.5355f
    const val square_h_square: Float = 0.4f
    const val square_padding_y: Float = 0.02f
    const val square_radius: Float = 0.10800001f
    const val square_w: Float = 0.87530595f
    const val square_w_NEOMORPHIC: Float = 0.6f
    const val square_w_landscape: Float = 0.87530595f
    const val square_w_no_radius: Float = 1.0f
    const val square_w_square: Float = 0.5623592f
    //endregion

    //region Color arrays
    // ViewCompat.MEASURED_STATE_MASK = 0xff000000 = -16777216
    // SupportMenu.CATEGORY_MASK = 0xffff0000 = -65536
    val MUSLIM_AYA_COLORS: IntArray = intArrayOf(
        -0x1000000, -1, -1096636, -340971, -15132186, -14498466,
        -5745162, -72990, -1096636, -2349530, COLOR_BLOCK_TRSLATION,
        -340971, -1395960, -2037761, -12877066, -14856488,
        -2294553, -14498466, -15368131, -3343375, -15419226,
        -1185282, -5745162, -8635667
    )

    val MUSLIM_COLORS: IntArray = intArrayOf(
        -1, -0x1000000, -478827, -626048, -4166524, -9675909,
        -13280131, -6702952, -78165, -31620, -1553825, -14010821,
        -5708082, -2298430, -11339, -21850, -29548, -5724249,
        -3386758, -1566883, -12105913, -13224394, -5823890,
        -1302455, -890056, -533681, -13658727, -1968700,
        -1186444, -404445, -224966, -45488, -113819, -221798,
        -406099, -3618647, -8147045, -2129313, -486033, -2117285,
        -11713425, -9410923
    )
    //endregion

    //region Mutable state
    var INDEX_LIST_SELECT: Int = 1
    var LIST_SELECT: List<hazem.nurmontage.videoquran.model.GallerySelected>? = null
    var MIN_SQUARE_H: Int = 0
    var MIN_SQUARE_W: Int = 0
    var bitmap: Bitmap? = null
    var p_h_border: Float = 0.065f
    var p_w_border: Float = 0.1f
    var radius: Int = 0
    var rect: android.graphics.RectF? = null
    var english_app_font: String = "Poppins-Regular.ttf"
    //endregion

    fun getListGradientColor(): List<Gradient> = listOf(
        Gradient(-711565, -6000461, -10897425),
        Gradient(-4919188, -2572422, -356473),
        Gradient(-11748097, -7244289, -2414081),
        Gradient(-4185359, -3829603, -3410353),
        Gradient(-124555, -305327, -486100),
        Gradient(-16422401, -13654017, -10556161),
        Gradient(-40350, -28271, -14907),
        Gradient(-25322, -15052, -2728),
        Gradient(-4659057, -2833534, -817794),
        Gradient(-11456910, -6132874, -808581),
        Gradient(-11756549, -7236869, -1603589),
        Gradient(-1510924, -2760212, -4798496),
        Gradient(-7735856, -5524737, -1596929),
        Gradient(-1590320, -2173472, -2821648),
        Gradient(-14174241, -7829855, -2008728),
        Gradient(-3020841, -737321, -4732954),
        Gradient(-5008712, -3432023, -1657706),
        Gradient(-10687276, -8195195, -5506257),
        Gradient(-1446295, -5516436, -10439568),
        Gradient(-12709248, -14257011, -16191843),
        Gradient(-664919, -812139, -966279),
        Gradient(-7597819, -14343650, -14408642),
        Gradient(-14408642, -7597819, -14343650),
        Gradient(-4144960, -2039584, -1),
        Gradient(-13421773, -11513776, -9934744),
        Gradient(-5197648, -2302756, -657931),
        Gradient(-9408400, -7303024, -4868683),
        Gradient(-986896, -460552, -1),
        Gradient(-16111037, -15051912, -12810056),
        Gradient(-10443815, -8208152, -5711632),
        Gradient(-15901613, -13992072, -11492697),
        Gradient(-16773077, -16768435, -16761736),
        Gradient(-8858141, -6233877, -3608587),
        Gradient(-5731463, -2836059, -660512),
        Gradient(-8889786, -6259616, -3628928),
        Gradient(-5214128, -3108752, -1527664),
        Gradient(-11179217, -9728477, -7357297),
        Gradient(-1654861, -994103, -464416),
        Gradient(-16757440, -16750244, -16746133),
        Gradient(-14791381, -13734593, -11751600),
        Gradient(-7278960, -5248081, -3212592),
        Gradient(-7365251, -5719910, -4140873),
        Gradient(-11927438, -9826899, -7114533),
        Gradient(-1644806, -986881, -460545),
        Gradient(-14417850, -12842644, -10872678),
        Gradient(-13676721, -9404272, -5192482),
        Gradient(-8943463, -6250336, -3421237),
        Gradient(-12156236, -8934691, -5383962),
        Gradient(-5206697, -2838662, -466776),
        Gradient(-537911, -336415, -3851),
        Gradient(-16777168, -16771760, -16766352),
        Gradient(-7667712, -0x10000, -32897),
        Gradient(-3596489, -907757, -26215),
        Gradient(-10092544, -4194304, -38294),
        Gradient(-1897636, -52347, -18223),
        Gradient(-2081743, -41892, -21075),
        Gradient(-2555828, -65434, -32589),
        Gradient(-2532608, -23296, -9543),
        Gradient(-4671488, -1331, -32),
        Gradient(-4872638, -268383, -560),
        Gradient(-29696, -19641, -8014),
        Gradient(-5005056, -989556, -1828),
        Gradient(-2528000, -274767, -5424),
        Gradient(-16744448, -13447886, -7278960),
        Gradient(-12951797, -7357297, -4530771),
        Gradient(-11179217, -5374161, -2031693),
        Gradient(-11840736, -7886485, -3941975),
        Gradient(-4994142, -2495789, -983056),
        Gradient(-16777088, -12156236, -5383962),
        Gradient(-13625036, -10666029, -5006849),
        Gradient(-11927478, -8388480, -2461482),
        Gradient(-15073254, -12844996, -9820034),
        Gradient(-10667462, -5214096, -2051920),
        Gradient(-11916246, -8700160, -3890044),
        Gradient(-10667241, -7644629, -3628944),
        Gradient(-11912399, -8692934, -5732248),
        Gradient(-7733169, -2490246, -36680),
        Gradient(-5023232, -29696, -12173),
        Gradient(-4696225, -423032, -16181),
        Gradient(-2039584, -331546, -1),
        Gradient(-5028051, -19559, -6708),
    )
}
