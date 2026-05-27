package hazem.nurmontage.videoquran.views

import android.content.Context
import android.graphics.*
import android.graphics.drawable.VectorDrawable
import android.text.TextPaint
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import androidx.core.view.GestureDetectorCompat
import androidx.core.view.ViewCompat
import hazem.nurmontage.videoquran.Utils.*
import hazem.nurmontage.videoquran.common.Common
import hazem.nurmontage.videoquran.constant.*
import hazem.nurmontage.videoquran.model.*
import hazem.nurmontage.videoquran.multitouch.MoveGestureDetector
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.sqrt

class BlurredImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr), View.OnTouchListener {

    companion object {
        private const val SNAP_FORCE = 0.2f
        private const val SNAP_THRESHOLD = 30f
    }

    interface IViewCallback {
        fun onDrawFinish()
        fun onEmtyClick()
        fun onEndMove()
        fun onEndScale()
        fun onSelect(entityView: EntityView)
        fun onSquare()
        fun onWattermark()
    }

    // === FIELDS ===
    var backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    var darkShadowPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    var lightShadowPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private var bismilahEntity: BismilahEntity? = null
    private var bitmapBlured: Bitmap? = null
    var bitmapNotBlur: Bitmap? = null
    var bitmapOriginal: Bitmap? = null
    private var bitmapSquare: Bitmap? = null
    private var btmX = 0f
    private var btmY = 0f
    private var clrAya = 0
    private var clrTrsl = 0
    private var colorBgTypeClassic = 0
    private var colorGradient: Gradient? = null
    private var colorIpad = -1
    private var colorLineBg = 0
    private var currentTime = "0:00"
    private var remainingTime = "0:15"
    private var entitySelect: EntityView? = null
    private var frameInterval = 0L
    private lateinit var gestureDetector: GestureDetectorCompat
    private var grayscalePaint = Paint()
    var iViewCallback: IViewCallback? = null
    private var ipadRect = RectF()
    private var isAnimWatermk = false
    var isDrawingSquareVideo = false
    private var isGlass = false
    private var isNotDraw = false
    private var isOnScale = false
    var isPlaying = false
    var isPro = false
    var isRemoveWattermark = false
    private var isSquare = false
    var isVideo = false
    private var isWattermark = false
    private var leftSquare = 0f
    private var linePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var linearGradientClassic: LinearGradient? = null
    private var mCanvasHeight = 0
    private var mCanvasWidth = 0
    private var mDrawingTranslationX = 0f
    private var mDrawingTranslationY = 0f
    private var mIpadType = IpadType.IPAD.ordinal
    private var mIsti3adhaEntity: BismilahEntity? = null
    private var mRectWattermark: RectF? = null
    private var mResizetype = 0
    private lateinit var moveGestureDetector: MoveGestureDetector
    private var newLeftTxt = 0f
    private var paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var paintClear = Paint()
    private var paintIpad = Paint(Paint.ANTI_ALIAS_FLAG)
    var paintLecture = Paint(Paint.ANTI_ALIAS_FLAG)
    private var paintText = TextPaint(Paint.ANTI_ALIAS_FLAG)
    private var paintWattermark = Paint(Paint.ANTI_ALIAS_FLAG)
    private var prevDistance = -1f
    var progress = 0f
    private val quranEntities = mutableListOf<QuranEntity>()
    private var radiusCursur = 0f
    private var radiusSquare = 0
    private var rectFAya = RectF()
    private var rectFLecture = RectF()
    private var rectFProgress = RectF()
    private var rectFSurahName = RectF()
    private var rectSquare: Rect? = null
    private lateinit var scaleGestureDetector: ScaleGestureDetector
    private var scheme: ColorSchemeGenerator.Scheme? = null
    private var selectTool: EntitySelectTool? = null
    private var showCenterLineX = false
    private var showCenterLineY = false
    private var startTime = -1L
    private var surahNameEntity: SurahNameEntity? = null
    private var topSquare = 0f
    private val translationEntities = mutableListOf<TranslationQuranEntity>()
    private var txtY = 0f
    private var wmAlpha = 1f
    private var wmScale = 1f
    private var wmTranslateY = 0f

    // === GESTURE LISTENER ===
    private val gestureListener = object : GestureDetector.SimpleOnGestureListener() {
        override fun onDown(e: MotionEvent): Boolean {
            if (!isPro && mRectWattermark?.contains(e.x, e.y) == true) {
                isWattermark = true
            }
            val sel = entitySelect
            if (sel != null && sel.isVisible && !isWattermark) {
                val tool = selectTool ?: return true
                if (tool.isApply(sel, e.x, e.y)) {
                    if (tool.isApply_Move()) iViewCallback?.onEndMove()
                    if (tool.isApply_Scale()) iViewCallback?.onEndScale()
                    tool.setClick_apply(true)
                    tool.reset()
                } else {
                    tool.isScale(sel, e.x, e.y)
                }
                if (tool.isApply_Scale()) {
                    tool.setOnProgress(true)
                    prevDistance = distanceToCenter(e.x, e.y)
                }
            }
            return true
        }

        override fun onSingleTapUp(e: MotionEvent): Boolean {
            val tool = selectTool
            if (entitySelect != null && tool?.isClick_apply() == true) {
                tool.setClick_apply(false)
                invalidate()
                return true
            }
            if (!isWattermark) updateSelectionOnTap(e)
            isOnScale = false
            iViewCallback?.let { cb ->
                if (entitySelect == null) {
                    when {
                        isWattermark -> cb.onWattermark()
                        isSquare -> cb.onSquare()
                        else -> cb.onEmtyClick()
                    }
                } else if (tool != null && tool.isApply_Move() &&
                    (entitySelect is QuranEntity || entitySelect is TranslationQuranEntity) &&
                    !tool.isApply_all()
                ) {
                    tool.setApply_all(true)
                    invalidate()
                }
                isWattermark = false
                isSquare = false
            }
            return super.onSingleTapUp(e)
        }
    }

    // === INIT ===
    init {
        setOnTouchListener(this)
        moveGestureDetector = MoveGestureDetector(context, MoveListener())
        scaleGestureDetector = ScaleGestureDetector(context, ScaleListener())
        gestureDetector = GestureDetectorCompat(context, gestureListener)

        val colorMatrix = ColorMatrix()
        colorMatrix.setSaturation(0f)
        grayscalePaint.colorFilter = ColorMatrixColorFilter(colorMatrix)

        paintWattermark.apply {
            color = ViewCompat.MEASURED_STATE_MASK
            alpha = 25
            typeface = UtilsFileLast.loadFontFromAsset(context, "fonts/ReadexPro_Medium.ttf")
            isFakeBoldText = true
        }
        paintClear.xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
        paintText.typeface = UtilsFileLast.loadFontFromAsset(context, "fonts/arabic/NotoNaskhArabic.ttf")
    }

    // === SETTERS / GETTERS ===
    fun setEntity_select(entity: EntityView?) {
        if (entitySelect != entity) selectTool?.reset()
        entitySelect = entity
    }

















    override fun onSizeChanged(w: Int, h: Int, oldW: Int, oldH: Int) {
        super.onSizeChanged(w, h, oldW, oldH)
        if (selectTool == null) selectTool = EntitySelectTool(w, context)
    }
















    fun initCanvasDimension(w: Int, h: Int, resizeType: Int) {
        when (resizeType) {
            ResizeType.SOCIAL_STORY.ordinal -> {
                mCanvasHeight = h
                mCanvasWidth = AspectRatioCalculator.calculateWidth(h)
            }
            ResizeType.SQUARE.ordinal, ResizeType.SQUARE_1_1.ordinal -> {
                val minVal = minOf(w, h)
                mCanvasWidth = minVal
                mCanvasHeight = minVal
            }
            else -> {
                mCanvasWidth = w
                mCanvasHeight = AspectRatioCalculator.calculateHeight_Youtube(w)
            }
        }
    }

    fun getW(): Int = width - paddingStart - paddingEnd
    fun getH(): Int = height - paddingTop - paddingBottom

    fun updatePosCanvas(bitmap: Bitmap?) {
        bitmap ?: return
        val w = (width - paddingStart - paddingEnd).toFloat()
        val h = (height - paddingTop - paddingBottom).toFloat()
        mDrawingTranslationX = (w - mCanvasWidth) / 2f
        mDrawingTranslationY = (h - mCanvasHeight) / 2f
        btmX = ((w - bitmap.width) / 2f) - mDrawingTranslationX
        btmY = ((h - bitmap.height) / 2f) - mDrawingTranslationY
    }

    fun updatePosCanvas(w: Int, h: Int, bitmap: Bitmap?) {
        bitmap ?: return
        mDrawingTranslationX = (w - mCanvasWidth) / 2f
        mDrawingTranslationY = (h - mCanvasHeight) / 2f
        btmX = ((w - bitmap.width) / 2f) - mDrawingTranslationX
        btmY = ((h - bitmap.height) / 2f) - mDrawingTranslationY
    }

    fun addEntity(entity: QuranEntity) {
        quranEntities.add(entity)
        entity.setIndex(quranEntities.size - 1)
    }

    fun addEntity(entity: TranslationQuranEntity) {
        translationEntities.add(entity)
        entity.setIndex(translationEntities.size - 1)
    }

    fun addEntity(entity: QuranEntity, index: Int) {
        if (index < quranEntities.size) quranEntities.add(index, entity)
        else quranEntities.add(entity)
        entity.setIndex(index)
    }

    fun addEntity(entity: TranslationQuranEntity, index: Int) {
        if (index < translationEntities.size) translationEntities.add(index, entity)
        else translationEntities.add(entity)
        entity.setIndex(index)
    }

    fun getQuranEntities(): List<QuranEntity> = quranEntities

    fun setBitmapSquare(bmp: Bitmap?) {
        if (bmp == null || bmp.isRecycled) return
        bitmapSquare = bmp
    }

    // === SET BITMAP (multiple overloads) ===
    fun setBitmap(blured: Bitmap?, square: Bitmap?, color: Int, ipadType: Int, resizeType: Int, rect: Rect?) {
        bitmapBlured = blured
        if (square != null) bitmapSquare = square
        rectSquare = rect
        mIpadType = ipadType
        if (color != -1) setColorIpad(color)
        else if (square != null) setColorIpad(ColorUtils.getAverageColor(square))
        mResizetype = resizeType
        setupTextSize()
        createRect()
    }

    fun updateBitmap(blured: Bitmap?, square: Bitmap?, color: Int, ipadType: Int, resizeType: Int, rect: Rect?) {
        bitmapBlured = blured
        if (square != null) bitmapSquare = square
        rectSquare = rect
        mIpadType = ipadType
        if (color != -1) setColorIpad(color)
        else if (square != null) setColorIpad(ColorUtils.getAverageColor(square))
        mResizetype = resizeType
        setupTextSize()
    }

    fun setBitmap(blured: Bitmap?, square: Bitmap?, gradient: Gradient, ipadType: Int, resizeType: Int, rect: Rect?) {
        bitmapBlured = blured
        if (square != null) bitmapSquare = square
        rectSquare = rect
        mIpadType = ipadType
        setColorIpad(gradient)
        mResizetype = resizeType
        setupTextSize()
    }

    private fun setupTextSize() {
        if (mIpadType == IpadType.BOTTOM_RECT.ordinal) {
            paintText.textSize = minOf(ipadRect.width(), ipadRect.height()) * 0.07f
        } else if (mIpadType == IpadType.BORDER.ordinal) {
            paintText.textSize = minOf(ipadRect.width(), ipadRect.height()) * 0.027f
        } else {
            paintText.textSize = ipadRect.width() * 0.0388f
        }
    }

    // === COLOR IPAD (int) ===
    fun colorIpad(): Int = colorIpad

    fun changeColorIpad() {
        if (colorGradient != null) setColorIpad(colorGradient!!)
        else setColorIpad(colorIpad())
    }

    fun setColorIpad(color: Int) {
        colorGradient = null
        paintIpad.shader = null
        colorIpad = color
        if (mIpadType == IpadType.IPAD_CLASSIC.ordinal) {
            colorBgTypeClassic = ColorUtils.lighten(color, 0.4f)
            paintIpad.color = ColorUtils.darken(color, 0.2f)
        } else {
            paintIpad.color = color
        }
        when (mIpadType) {
            IpadType.BORDER.ordinal -> {
                colorLineBg = ColorUtils.darken(color, 0.4f)
                paintLecture.color = color
            }
            IpadType.BLUE_TYPE.ordinal -> {
                paintLecture.color = ColorUtils.getEnergyColor(0.7f)
                colorLineBg = ColorUtils.darken(paintLecture.color, 0.7f)
            }
            IpadType.CASSET.ordinal, IpadType.CASSET_IMG.ordinal, IpadType.CASSET_IMG_BLUR.ordinal -> {
                val s = ColorSchemeGenerator.generateScheme(color)
                scheme = s
                paintLecture.color = if (ColorUtils.isColorDark(s.label)) -1 else ViewCompat.MEASURED_STATE_MASK
                colorLineBg = ColorUtils.darken(paintLecture.color, 0.7f)
            }
            else -> {
                colorLineBg = ColorUtils.darken(color, 0.4f)
                paintIpad.alpha = 190
                paintLecture.color = if (ColorUtils.isColorDark(paintIpad.color)) -1 else ViewCompat.MEASURED_STATE_MASK
            }
        }
        paintText.color = paintLecture.color
    }

    fun setColorIpad(gradient: Gradient) {
        colorGradient = gradient
        val color = gradient.startColor
        if (mIpadType == IpadType.IPAD_CLASSIC.ordinal) {
            paintIpad.shader = null
            linearGradientClassic = CreateGradient.createLinearGradientWithAngle(
                ipadRect, gradient.angle,
                intArrayOf(
                    ColorUtils.lighten(gradient.startColor, 0.4f),
                    ColorUtils.lighten(gradient.centerColor, 0.4f),
                    ColorUtils.lighten(gradient.endColor, 0.4f)
                ),
                floatArrayOf(0f, 0.7f, 1f)
            )
            paintIpad.color = ColorUtils.darken(gradient.centerColor, 0.2f)
        } else {
            val lg = CreateGradient.createLinearGradientWithAngle(
                ipadRect, gradient.angle,
                intArrayOf(gradient.startColor, gradient.centerColor, gradient.endColor),
                floatArrayOf(0f, 0.7f, 1f)
            )
            linearGradientClassic = lg
            paintIpad.shader = lg
            paintIpad.color = color
        }
        colorLineBg = ColorUtils.darken(color, 0.4f)
        when (mIpadType) {
            IpadType.BORDER.ordinal -> paintLecture.color = color
            IpadType.BLUE_TYPE.ordinal -> paintLecture.color = ColorUtils.lighten(color, 0.7f)
            IpadType.CASSET.ordinal, IpadType.CASSET_IMG.ordinal, IpadType.CASSET_IMG_BLUR.ordinal -> {
                val s = ColorSchemeGenerator.generateScheme(color, gradient.angle)
                scheme = s
                paintLecture.color = if (ColorUtils.isColorDark(s.label)) -1 else ViewCompat.MEASURED_STATE_MASK
            }
            else -> {
                paintIpad.alpha = 190
                paintLecture.color = if (ColorUtils.isColorDark(paintIpad.color)) -1 else ViewCompat.MEASURED_STATE_MASK
            }
        }
        paintText.color = paintLecture.color
    }

    // === TYPEFACE / PRESET ===
    fun setIcon(name: String, drawable: VectorDrawable) {
        for (entity in quranEntities) {
            if (entity.icon != null && entity.icon != name && entity.number != -1) {
                entity.setVectorDrawable(drawable)
                entity.icon = name
                entity.updateIconDraw()
            }
        }
        updateSizeAya()
        invalidate()
    }

    fun setTypeface(typeface: Typeface, name: String) {
        val sel = entitySelect
        if (sel is QuranEntity) {
            for (entity in quranEntities) {
                if (entity.getNameFont() != null && entity.getNameFont() != name) {
                    entity.setTypeface(typeface, name)
                }
            }
            updateSizeAyaResize()
        } else if (sel is TranslationQuranEntity) {
            for (entity in translationEntities) {
                if (entity.getNameFont() != null && entity.getNameFont() != name) {
                    entity.setTypeface(typeface, name)
                }
            }
            updateSizeTrslAyaResize()
        }
        invalidate()
    }

    fun setPreset(preset: AyaTextPreset) {
        for (entity in quranEntities) entity.preset = preset
        mIsti3adhaEntity?.let { if (it.getBismilahTimeline().visible()) it.preset = preset }
        bismilahEntity?.let { if (it.getBismilahTimeline().visible()) it.preset = preset }
        invalidate()
    }

    fun setTrslPreset(preset: AyaTextPreset) {
        for (entity in translationEntities) entity.preset = preset
        invalidate()
    }

    fun setColorAya(color: Int) {
        clrAya = color
        for (entity in quranEntities) entity.setColor(color)
        mIsti3adhaEntity?.let { if (it.getBismilahTimeline().visible()) it.setColor(color) }
        bismilahEntity?.let { if (it.getBismilahTimeline().visible()) it.setColor(color) }
        invalidate()
    }

    fun setColorTrsl(color: Int) {
        clrTrsl = color
        for (entity in translationEntities) entity.setColor(color)
        invalidate()
    }

    fun applyAll(factorSize: Float, rect: RectF, color: Int, color2: Int) {
        val sel = entitySelect ?: return
        if (sel is QuranEntity) {
            for (entity in quranEntities) {
                if (entity != sel) entity.applyAll(mCanvasWidth, rect, sel.getPaintAya().textSize, sel.factorSize)
            }
            invalidate()
        } else if (sel is TranslationQuranEntity) {
            for (entity in translationEntities) {
                if (entity != sel) entity.applyAll(mCanvasWidth, rect, sel.getPaintAya().textSize, sel.factorSize)
            }
            invalidate()
        }
    }

    fun setCurrentTime(current: String, remaining: String) {
        currentTime = current
        remainingTime = "-$remaining"
    }

    // === CREATE RECT ===
    fun createRect() {
        createRectWithoutSurahName()
        updateSurahNameEntity()
    }

    fun createRectWithoutSurahName() {
        val w = mCanvasWidth.toFloat()
        val h = mCanvasHeight.toFloat()
        val ipadType = mIpadType

        when (ipadType) {
            IpadType.IPAD.ordinal, IpadType.IPAD_CLASSIC.ordinal -> {
                val margin = w * 0.05f
                val top = h * 0.15f
                val bottom = h * 0.85f
                ipadRect.set(margin, top, w - margin, bottom)
            }
            IpadType.BOTTOM_RECT.ordinal -> {
                val margin = w * 0.03f
                val top = h * 0.55f
                ipadRect.set(margin, top, w - margin, h - margin)
            }
            IpadType.BORDER.ordinal -> {
                val margin = w * 0.04f
                val top = h * 0.12f
                val bottom = h * 0.88f
                ipadRect.set(margin, top, w - margin, bottom)
            }
            IpadType.CASSET.ordinal, IpadType.CASSET_IMG.ordinal, IpadType.CASSET_IMG_BLUR.ordinal -> {
                val margin = w * 0.06f
                val top = h * 0.2f
                val bottom = h * 0.8f
                ipadRect.set(margin, top, w - margin, bottom)
            }
            IpadType.BLUE_TYPE.ordinal -> {
                val margin = w * 0.05f
                val top = h * 0.15f
                val bottom = h * 0.85f
                ipadRect.set(margin, top, w - margin, bottom)
            }
            IpadType.IPAD_NEOMORPHIC.ordinal, IpadType.NEUMORPHIC.ordinal -> {
                val margin = w * 0.05f
                val top = h * 0.15f
                val bottom = h * 0.85f
                ipadRect.set(margin, top, w - margin, bottom)
            }
            IpadType.PROGRESS.ordinal -> {
                val margin = w * 0.05f
                val top = h * 0.15f
                val bottom = h * 0.85f
                ipadRect.set(margin, top, w - margin, bottom)
            }
            else -> {
                val margin = w * 0.05f
                val top = h * 0.15f
                val bottom = h * 0.85f
                ipadRect.set(margin, top, w - margin, bottom)
            }
        }
        rectFAya = createAyaRect(ipadRect, ipadType)
    }

    private fun createAyaRect(ipad: RectF, type: Int): RectF {
        return when (type) {
            IpadType.BOTTOM_RECT.ordinal -> {
                val margin = ipad.width() * 0.05f
                RectF(ipad.left + margin, ipad.top + ipad.height() * 0.1f, ipad.right - margin, ipad.bottom - margin)
            }
            IpadType.BORDER.ordinal -> {
                val margin = ipad.width() * 0.06f
                RectF(ipad.left + margin, ipad.top + ipad.height() * 0.15f, ipad.right - margin, ipad.bottom - margin)
            }
            else -> {
                val margin = ipad.width() * 0.06f
                RectF(ipad.left + margin, ipad.top + ipad.height() * 0.12f, ipad.right - margin, ipad.bottom - ipad.height() * 0.12f)
            }
        }
    }

    private fun updateSurahNameEntity() {
        surahNameEntity?.let { sn ->
            sn.setRect(RectF(rectFAya.left, ipadRect.top, rectFAya.right, rectFAya.top))
        }
    }

    // === UPDATE IPAD ===
    fun updateIpad(bitmap: Bitmap?, w: Int, h: Int) {
        bitmap ?: return
        val ratio = bitmap.width.toFloat() / bitmap.height
        val ipW: Float
        val ipH: Float
        if (mIpadType == IpadType.BOTTOM_RECT.ordinal) {
            ipW = w * 0.94f
            ipH = ipW / ratio
        } else {
            ipW = w * 0.9f
            ipH = ipW / ratio
        }
        ipadRect.set((w - ipW) / 2f, (h - ipH) / 2f, (w + ipW) / 2f, (h + ipH) / 2f)
    }

    fun updateIpad() {
        val w = mCanvasWidth.toFloat()
        val h = mCanvasHeight.toFloat()
        createRect()
    }

    // === SIZE UPDATES ===
    fun updateSizeAya() {
        for (entity in quranEntities) {
            entity.updateSize(mCanvasWidth, rectFAya)
        }
        invalidate()
    }

    fun updateSizeTrslAya() {
        for (entity in translationEntities) {
            entity.updateSize(mCanvasWidth, rectFAya)
        }
        invalidate()
    }

    fun updateSizeAyaResize() {
        for (entity in quranEntities) {
            entity.updateSizeResize(mCanvasWidth, rectFAya)
        }
        invalidate()
    }

    fun updateSizeTrslAyaResize() {
        for (entity in translationEntities) {
            entity.updateSizeResize(mCanvasWidth, rectFAya)
        }
        invalidate()
    }

    fun updatePosSurahName() {
        surahNameEntity?.let {
            it.setRect(RectF(rectFAya.left, ipadRect.top, rectFAya.right, rectFAya.top))
        }
    }

    fun updateBismilahEntity(bismilah: BismilahEntity) {
        bismilahEntity = bismilah
    }

    fun setBismilahEntity(bismilah: BismilahEntity?) { bismilahEntity = bismilah }
    fun getBismilahEntity(): BismilahEntity? = bismilahEntity
    fun setIsti3adhaEntity(bismilah: BismilahEntity?) { mIsti3adhaEntity = bismilah }
    fun getIsti3adhaEntity(): BismilahEntity? = mIsti3adhaEntity

    // === SELECTION ===
    private fun updateSelectionOnTap(event: MotionEvent) {
        val x = event.x
        val y = event.y
        val tool = selectTool ?: return

        // Check Quran entities first (reverse order for z-order)
        for (i in quranEntities.indices.reversed()) {
            val entity = quranEntities[i]
            if (entity.isVisible && entity.contains(x, y)) {
                entitySelect = entity
                tool.reset()
                iViewCallback?.onSelect(entity)
                invalidate()
                return
            }
        }
        // Check translation entities
        for (i in translationEntities.indices.reversed()) {
            val entity = translationEntities[i]
            if (entity.isVisible && entity.contains(x, y)) {
                entitySelect = entity
                tool.reset()
                iViewCallback?.onSelect(entity)
                invalidate()
                return
            }
        }
        // Check bismilah
        bismilahEntity?.let {
            if (it.getBismilahTimeline().visible() && it.contains(x, y)) {
                entitySelect = it
                tool.reset()
                iViewCallback?.onSelect(it)
                invalidate()
                return
            }
        }
        mIsti3adhaEntity?.let {
            if (it.getBismilahTimeline().visible() && it.contains(x, y)) {
                entitySelect = it
                tool.reset()
                iViewCallback?.onSelect(it)
                invalidate()
                return
            }
        }
        // Nothing hit
        entitySelect = null
        tool.reset()
        // Check square video area
        if (isDrawingSquareVideo) isSquare = true
        invalidate()
    }

    private fun distanceToCenter(x: Float, y: Float): Float {
        val sel = entitySelect ?: return -1f
        val cx = sel.getRect().centerX()
        val cy = sel.getRect().centerY()
        return sqrt(((x - cx) * (x - cx) + (y - cy) * (y - cy)).toDouble()).toFloat()
    }

    private fun handleTranslate(dx: Float, dy: Float) {
        val sel = entitySelect ?: return
        val rect = sel.getRect()
        var newX = rect.left + dx
        var newY = rect.top + dy

        // Snap to center
        val centerX = mCanvasWidth / 2f
        val centerY = mCanvasHeight / 2f
        if (abs(newX + rect.width() / 2f - centerX) < SNAP_THRESHOLD) {
            newX = centerX - rect.width() / 2f
        }
        if (abs(newY + rect.height() / 2f - centerY) < SNAP_THRESHOLD) {
            newY = centerY - rect.height() / 2f
        }

        sel.setPos(newX, newY)
    }

    // === CENTER LINE HELPERS ===
    fun showCenterLines(showX: Boolean, showY: Boolean) {
        showCenterLineX = showX
        showCenterLineY = showY
        invalidate()
    }

    // === ON TOUCH ===
    override fun onTouch(v: View, event: MotionEvent): Boolean {
        moveGestureDetector.onTouchEvent(event)
        scaleGestureDetector.onTouchEvent(event)
        gestureDetector.onTouchEvent(event)

        when (event.action) {
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                selectTool?.let { tool ->
                    if (tool.isApply_Scale()) {
                        tool.setOnProgress(false)
                    }
                    if (tool.isApply_Move()) {
                        // Commit final position
                    }
                }
                isOnScale = false
                prevDistance = -1f
            }
            MotionEvent.ACTION_MOVE -> {
                selectTool?.let { tool ->
                    if (tool.isApply_Scale() && tool.isOnProgress()) {
                        val newDist = distanceToCenter(event.x, event.y)
                        if (prevDistance > 0f && newDist > 0f) {
                            val scaleFactor = newDist / prevDistance
                            entitySelect?.scale(scaleFactor)
                        }
                        prevDistance = newDist
                        invalidate()
                    }
                }
                moveGestureDetector.onTouchEvent(event)
            }
        }
        return true
    }

    // === INNER CLASSES ===
    private inner class MoveListener : MoveGestureDetector.OnMoveGestureListener {
        override fun onMoveBegin(detector: MoveGestureDetector): Boolean {
            selectTool?.let { tool ->
                if (tool.isApply_Move()) {
                    return true
                }
            }
            return false
        }

        override fun onMoveEnd(detector: MoveGestureDetector) {}

        override fun onMove(detector: MoveGestureDetector): Boolean {
            val dx = detector.getFocusX()
            val dy = detector.getFocusY()
            handleTranslate(dx, dy)
            invalidate()
            return true
        }
    }

    private inner class ScaleListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
            isOnScale = true
            return true
        }

        override fun onScale(detector: ScaleGestureDetector): Boolean {
            val sel = entitySelect ?: return false
            sel.scale(detector.scaleFactor)
            invalidate()
            return true
        }

        override fun onScaleEnd(detector: ScaleGestureDetector) {
            isOnScale = false
        }
    }

    // === DRAWING ===
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (isNotDraw) return

        canvas.save()
        canvas.translate(mDrawingTranslationX, mDrawingTranslationY)

        // Draw background
        bitmapBlured?.let { canvas.drawBitmap(it, 0f, 0f, paint) }

        // Draw glass overlay
        if (isGlass) {
            bitmapNotBlur?.let { canvas.drawBitmap(it, btmX, btmY, grayscalePaint) }
        }

        // Draw iPad frame
        drawIpad(canvas, false)

        // Draw entities
        drawBismilah(canvas)
        drawAya(canvas)
        drawNameSurah(canvas)

        // Draw watermark
        drawWattermark(canvas)

        // Draw selection handles
        entitySelect?.let { sel ->
            if (sel.isVisible) {
                selectTool?.draw(canvas, sel)
            }
        }

        // Draw center lines
        if (showCenterLineX) {
            canvas.drawLine(mCanvasWidth / 2f, 0f, mCanvasWidth / 2f, mCanvasHeight.toFloat(), linePaint)
        }
        if (showCenterLineY) {
            canvas.drawLine(0f, mCanvasHeight / 2f, mCanvasWidth.toFloat(), mCanvasHeight / 2f, linePaint)
        }

        canvas.restore()
        iViewCallback?.onDrawFinish()
    }

    // === DRAW IPAD ===
    fun drawIpad(canvas: Canvas, isExport: Boolean) {
        when (mIpadType) {
            IpadType.IPAD.ordinal -> drawIpadType(canvas, isExport)
            IpadType.IPAD_CLASSIC.ordinal -> drawIpadClassic(canvas, isExport)
            IpadType.BOTTOM_RECT.ordinal -> drawIpadBottomRect(canvas, isExport)
            IpadType.BORDER.ordinal -> drawIpadBorder(canvas, isExport)
            IpadType.CASSET.ordinal, IpadType.CASSET_IMG.ordinal, IpadType.CASSET_IMG_BLUR.ordinal -> drawCassette(canvas, isExport)
            IpadType.BLUE_TYPE.ordinal -> drawBlueType(canvas, isExport)
            IpadType.IPAD_NEOMORPHIC.ordinal, IpadType.NEUMORPHIC.ordinal -> drawNeumorphic(canvas, isExport)
            IpadType.HEART.ordinal -> drawHeartType(canvas, isExport)
            IpadType.BATTERY.ordinal -> drawBatteryType(canvas, isExport)
            IpadType.PROGRESS.ordinal -> drawProgress(canvas, isExport)
            IpadType.BLACK_LAYER.ordinal -> drawBlackLayer(canvas, isExport)
            else -> drawIpadType(canvas, isExport)
        }
    }

    private fun drawIpadType(canvas: Canvas, isExport: Boolean) {
        canvas.drawRoundRect(ipadRect, 20f, 20f, paintIpad)
    }

    private fun drawIpadClassic(canvas: Canvas, isExport: Boolean) {
        canvas.drawRoundRect(ipadRect, 20f, 20f, paintIpad)
        // Draw classic inner border
        val inner = RectF(ipadRect.left + 5, ipadRect.top + 5, ipadRect.right - 5, ipadRect.bottom - 5)
        val classicPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = colorBgTypeClassic }
        canvas.drawRoundRect(inner, 15f, 15f, classicPaint)
    }

    private fun drawIpadBottomRect(canvas: Canvas, isExport: Boolean) {
        canvas.drawRoundRect(ipadRect, 20f, 20f, paintIpad)
    }

    private fun drawIpadBorder(canvas: Canvas, isExport: Boolean) {
        canvas.drawRoundRect(ipadRect, 20f, 20f, paintIpad)
        // Draw border lines
        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = colorLineBg
            style = Paint.Style.STROKE
            strokeWidth = 3f
        }
        canvas.drawRoundRect(ipadRect, 20f, 20f, borderPaint)
    }

    private fun drawCassette(canvas: Canvas, isExport: Boolean) {
        canvas.drawRoundRect(ipadRect, 20f, 20f, paintIpad)
        // Draw cassette holes
        scheme?.let { s ->
            val holePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = s.secondary
                alpha = 80
            }
            val holeRadius = ipadRect.height() * 0.12f
            val cy = ipadRect.centerY()
            // Left hole
            canvas.drawCircle(ipadRect.left + ipadRect.width() * 0.25f, cy, holeRadius, holePaint)
            // Right hole
            canvas.drawCircle(ipadRect.left + ipadRect.width() * 0.75f, cy, holeRadius, holePaint)
        }
    }

    private fun drawBlueType(canvas: Canvas, isExport: Boolean) {
        canvas.drawRoundRect(ipadRect, 20f, 20f, paintIpad)
        // Draw energy line at bottom
        if (colorLineBg != 0) {
            val energyLinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = colorLineBg
                strokeWidth = 3f
                style = Paint.Style.STROKE
            }
            val lineY = ipadRect.bottom - ipadRect.height() * 0.08f
            canvas.drawLine(
                ipadRect.left + ipadRect.width() * 0.1f,
                lineY,
                ipadRect.right - ipadRect.width() * 0.1f,
                lineY,
                energyLinePaint
            )
        }
    }

    private fun drawNeumorphic(canvas: Canvas, isExport: Boolean) {
        val offset = 10f
        // Light shadow
        lightShadowPaint.apply {
            color = paintIpad.color
            style = Paint.Style.FILL
            clearShadowLayer()
            setShadowLayer(offset, -offset, -offset, ColorUtils.lighten(paintIpad.color, 0.15f))
        }
        canvas.drawRoundRect(ipadRect, 20f, 20f, lightShadowPaint)
        // Dark shadow
        darkShadowPaint.apply {
            color = paintIpad.color
            style = Paint.Style.FILL
            clearShadowLayer()
            setShadowLayer(offset, offset, offset, ColorUtils.darken(paintIpad.color, 0.15f))
        }
        canvas.drawRoundRect(ipadRect, 20f, 20f, darkShadowPaint)
        // Base
        canvas.drawRoundRect(ipadRect, 20f, 20f, paintIpad)
    }

    private fun drawHeartType(canvas: Canvas, isExport: Boolean) {
        canvas.drawRoundRect(ipadRect, 20f, 20f, paintIpad)
    }

    private fun drawBatteryType(canvas: Canvas, isExport: Boolean) {
        canvas.drawRoundRect(ipadRect, 20f, 20f, paintIpad)
    }

    private fun drawProgress(canvas: Canvas, isExport: Boolean) {
        canvas.drawRoundRect(ipadRect, 20f, 20f, paintIpad)
        // Draw progress bar at the bottom of the iPad frame
        val progressRect = RectF(
            ipadRect.left,
            ipadRect.bottom - 8f,
            ipadRect.left + ipadRect.width() * progress,
            ipadRect.bottom
        )
        val progressPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                ipadRect.left, 0f, ipadRect.right, 0f,
                intArrayOf(-0xa8ce46, -0xd2de49, -0xf4d853),
                null,
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRoundRect(progressRect, 4f, 4f, progressPaint)
    }

    private fun drawBlackLayer(canvas: Canvas, isExport: Boolean) {
        val blackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = -16777216
            alpha = 128
        }
        canvas.drawRoundRect(ipadRect, 20f, 20f, blackPaint)
    }

    // === DRAW ENTITIES ===
    private fun drawBismilah(canvas: Canvas) {
        bismilahEntity?.let {
            if (it.getBismilahTimeline().visible()) it.draw(canvas)
        }
        mIsti3adhaEntity?.let {
            if (it.getBismilahTimeline().visible()) it.draw(canvas)
        }
    }

    private fun drawAya(canvas: Canvas) {
        for (entity in quranEntities) {
            if (entity.isVisible) entity.draw(canvas)
        }
        for (entity in translationEntities) {
            if (entity.isVisible) entity.draw(canvas)
        }
    }

    private fun drawNameSurah(canvas: Canvas) {
        surahNameEntity?.draw(canvas)
    }

    // === DRAW WATERMARK ===
    private fun drawWattermark(canvas: Canvas) {
        if (!isRemoveWattermark && !isPro) {
            val text = "NurMontage"
            paintWattermark.textSize = mCanvasWidth * 0.03f
            val textWidth = paintWattermark.measureText(text)
            val x = mCanvasWidth - textWidth - 10f
            val y = mCanvasHeight - 10f
            canvas.drawText(text, x, y, paintWattermark)
            mRectWattermark = RectF(
                x - 10f,
                y - paintWattermark.textSize,
                x + textWidth + 10f,
                y + 10f
            )
        } else {
            mRectWattermark = null
        }
    }

    // === SAVE BITMAP ===
    fun saveBitmap(file: File): Boolean {
        val bmp = getBitmapDraw() ?: return false
        return try {
            FileOutputStream(file).use { fos ->
                bmp.compress(Bitmap.CompressFormat.PNG, 100, fos)
            }
            true
        } catch (e: IOException) {
            e.printStackTrace()
            false
        }
    }

    fun getBitmapDraw(): Bitmap? {
        val w = mCanvasWidth
        val h = mCanvasHeight
        if (w <= 0 || h <= 0) return null
        val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        isNotDraw = true

        // Draw background
        bitmapBlured?.let { canvas.drawBitmap(it, 0f, 0f, paint) }
        bitmapNotBlur?.let {
            if (isGlass) canvas.drawBitmap(it, btmX, btmY, grayscalePaint)
        }

        // Draw iPad
        drawIpad(canvas, true)

        // Draw entities
        drawBismilah(canvas)
        drawAya(canvas)
        drawNameSurah(canvas)

        // Draw watermark only if not removed
        if (!isRemoveWattermark && !isPro) drawWattermark(canvas)

        isNotDraw = false
        return bmp
    }

    // === TRANSITIONS ===
    fun slideInToLeft(entity: EntityView) { entity.setTransition(Transition.SLIDE_IN_LEFT) }
    fun slideInToRight(entity: EntityView) { entity.setTransition(Transition.SLIDE_IN_RIGHT) }
    fun slideOutToLeft(entity: EntityView) { entity.setTransition(Transition.SLIDE_OUT_LEFT) }
    fun slideOutToRight(entity: EntityView) { entity.setTransition(Transition.SLIDE_OUT_RIGHT) }
    fun fadeIn(entity: EntityView) { entity.setTransition(Transition.FADE_IN) }
    fun fadeOut(entity: EntityView) { entity.setTransition(Transition.FADE_OUT) }

    fun setNotDraw(v: Boolean) { isNotDraw = v }

    fun getSelectTool(): EntitySelectTool? = selectTool

    fun getTranslationEntities(): List<TranslationQuranEntity> = translationEntities

    // === CALCULATE TEXT SIZE ===
    fun calculateTextSize(text: String, maxWidth: Int, paint: Paint): Float {
        var size = 400f
        paint.textSize = size
        val bounds = Rect()
        while (bounds.width() > maxWidth || bounds.height() > maxWidth) {
            size -= 1f
            paint.textSize = size
            paint.getTextBounds(text, 0, text.length, bounds)
        }
        return size
    }

    // === HELPER: Average color from bitmap (kept for direct usage) ===
    private fun getAverageColor(bitmap: Bitmap): Int {
        return ColorUtils.getAverageColor(bitmap)
    }
}
