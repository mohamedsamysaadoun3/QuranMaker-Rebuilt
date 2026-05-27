package hazem.nurmontage.videoquran.views

import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Handler
import android.util.AttributeSet
import android.util.Log
import android.util.Pair
import android.view.*
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.Scroller
import androidx.core.content.ContextCompat
import androidx.core.view.GestureDetectorCompat
import hazem.nurmontage.videoquran.R
import hazem.nurmontage.videoquran.constant.EntityAction
import hazem.nurmontage.videoquran.entity_timeline.*
import hazem.nurmontage.videoquran.model.*
import java.util.*
import kotlin.math.abs
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

class TrackEntityView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr), View.OnTouchListener {

    interface ITrimLineCallback {
        fun enableRedo(enabled: Boolean)
        fun enableUndo(enabled: Boolean)
        fun fadeInAudio(position: Float)
        fun fadeOutAudio(position: Float)
        fun onAddStack(action: EntityAction)
        fun onDelete(entityView: EntityView)
        fun onEmptySelect()
        fun onMove()
        fun onPlayVibration()
        fun onSeekPlayer(position: Float)
        fun onSelectEntity(entity: Entity, position: Float)
        fun onSelectMultiple(count: Int)
        fun onUp()
        fun onUpdate()
        fun onUpdatePlayerAudio(audio: EntityAudio)
        fun onUpdateTime()
        fun pause()
        fun progress(isProgress: Boolean)
    }

    // === CONSTANTS ===
    private val MAX_SCALE = 8f
    private val MIN_SCALE = 0.09f
    private val FACTOR_VITESSE = 180f
    private val CLR_DEFAULT_BG = -13421771
    private val CLR_SELECT = -794718

    // === FIELDS ===
    private var entityListAudio = mutableListOf<EntityAudio>()
    private val entityListQuran = mutableListOf<EntityQuranTimeline>()
    private val entityListTrslQuran = mutableListOf<EntityTrslTimeline>()
    private var entityList = Stack<Pair<Entity, EntityAction>>()
    private var undoEntityList = Stack<Pair<Entity, EntityAction>>()

    private var autoScrollHandler = Handler()
    private lateinit var autoScrollRunnable: Runnable
    private lateinit var autoMoveRunnable: Runnable

    private var isPassScroll = true
    private var onThink = true
    private var scaleFactor = 0.5f
    private val paintItem = Paint(Paint.ANTI_ALIAS_FLAG)

    private var clrBtnAudio = CLR_DEFAULT_BG
    private var clrBtnQuran = CLR_DEFAULT_BG
    private var clrBtnTrsl = CLR_DEFAULT_BG

    private lateinit var gestureDetector: GestureDetectorCompat
    private var iTrimLineCallback: ITrimLineCallback? = null
    private var isArabicLang = false
    private var isAutoMove = false
    private var isAutoScroll = false
    private var isCheckLine = false
    private var isCheckLineCursur = false
    private var isDetectChange = false
    private var isFling = false
    private var isMove = false
    private var isOnUp = false
    private var isPlaying = false
    private var isProgress = false
    private var isScaleListener = false

    private var eventX = 0f
    private var eventY = 0f
    private var lasX = 0f
    private var lastDifference = 0L
    private var lastTime = 0L

    private var bismilahTimeline: EntityBismilahTimeline? = null
    private var mIsi3adaTimeline: EntityBismilahTimeline? = null
    private var mScrollY = 0f
    private var mPosYMarker = 0f
    private var markerHeight = 0f
    private var maxBottom = 0f
    private var maxTime = -1
    private var maxTrim = 0f

    private lateinit var objectAnimator: ObjectAnimator
    private var p = 0f
    private var paddingCursur = 0f
    private lateinit var paintCursur: Paint
    private lateinit var paintLineCheck: Paint
    private lateinit var paintMaker: Paint
    private lateinit var paintTime: Paint

    private var pass = false
    private var pathItemAudio = Path()
    private var pathItemQuran = Path()
    private var pathItemTrslQuran = Path()
    private var posY = 0f
    private var radius = 0f
    private var rectFItemQuran = RectF()
    private var rectFItemTrslQuran = RectF()
    private var rectItemAudio = RectF()
    private var rectSquareAudio = RectF()
    private var rectSquareQuran = RectF()
    private var rectSquareTrslQuran = RectF()

    private lateinit var scaleGestureDetector: ScaleGestureDetector
    private var scrolledWithZoom = 0f
    private lateinit var scroller: Scroller
    private var secondInScreen = 0f
    private var selectedEntity: Entity? = null
    private var signeX = 0f
    private var signeY = 0f
    private var startXLine = 0f
    private var startYDraw = 0f
    private var target = 0f
    private var timeLineW = 0f
    private var timeStart = 0L
    private var wTimeItem = 0f
    private var widthScreen = 0
    private var canvasTopY = 0f
    private var centerX = 0f
    private var detectLeftMove = 0f
    private var detectRightMove = 0f
    private var dx = 0f
    private var SPEED = 0f
    private var TOLERANCE_X = 0.95f
    private var duration = 0
    private var currentPosition = 0f
    private var currentCursurPosition = 0
    private var countMove = 0
    private var currentEventX = 0f

    private var btnRedo: ImageButton? = null
    private var btnUndo: ImageButton? = null

    private val exclusionRects = mutableListOf<Rect>()

    // === GESTURE LISTENER ===
    private val gestureListener = object : GestureDetector.SimpleOnGestureListener() {
        override fun onDown(e: MotionEvent): Boolean {
            pauseScroll()
            val point = PointF(e.x, e.y)
            isPassScroll = true
            selectedEntity?.let { sel ->
                val contains = sel.contains(point)
                isPassScroll = !contains && sel.getTrimType() == -1
                sel.setSelect(true)
                if (!isPassScroll && iTrimLineCallback != null) {
                    when (sel.getTrimType()) {
                        0 -> {
                            sel.setCurrentRect()
                            sel.setOnTapTime(
                                Math.round(sel.getRect().left / getSecondInScreen()).toLong() * 1000L,
                                sel.getRect().left
                            )
                            iTrimLineCallback?.onPlayVibration()
                        }
                        1 -> {
                            sel.setCurrentRect()
                            sel.setOnTapTime(
                                Math.round(sel.getRect().right / getSecondInScreen()).toLong() * 1000L,
                                sel.getRect().right
                            )
                            iTrimLineCallback?.onPlayVibration()
                        }
                        else -> if (contains) {
                            sel.setCurrentRect()
                            iTrimLineCallback?.onSelectEntity(sel, 0f)
                        }
                    }
                }
            }
            return true
        }

        override fun onSingleTapUp(e: MotionEvent): Boolean {
            if (!isPlaying()) {
                if (handleItemInteraction(
                        e.x + paddingLeft + (centerX - radius * 0.5f) + scrolledWithZoom,
                        e.y
                    )
                ) return true
            } else if (clrBtnQuran != CLR_DEFAULT_BG || clrBtnAudio != CLR_DEFAULT_BG || clrBtnTrsl != CLR_DEFAULT_BG) {
                clrBtnTrsl = CLR_DEFAULT_BG
                clrBtnQuran = CLR_DEFAULT_BG
                clrBtnAudio = CLR_DEFAULT_BG
            }
            if (isPassScroll) updateSelectionOnTap(e)
            return true
        }

        override fun onScroll(
            e1: MotionEvent?,
            e2: MotionEvent,
            dx: Float,
            dy: Float
        ): Boolean {
            if (isProgress || !isPassScroll || (selectedEntity != null && selectedEntity!!.getTrimType() != -1)) {
                return super.onScroll(e1, e2, dx, dy)
            }
            if (!isScaleListener && (e1?.eventTime ?: e2.eventTime).let { e2.eventTime - it } >= 107 && isPass(e2)) {
                if (isPlaying()) setPlaying(false)
                if (eventX == 0f) {
                    eventX = e2.rawX
                    eventY = e2.rawY
                    return true
                }
                val rawDx = e2.rawX - eventX
                currentPosition += rawDx / scaleFactor
                if (currentPosition > 0f) currentPosition = 0f
                scrolledWithZoom = currentPosition * scaleFactor
                iTrimLineCallback?.onSeekPlayer(scrolledWithZoom)
                eventX = e2.rawX
                eventY = e2.rawY
                invalidate()
            }
            return true
        }

        override fun onFling(
            e1: MotionEvent?,
            e2: MotionEvent,
            velocityX: Float,
            velocityY: Float
        ): Boolean {
            if (isProgress) return true
            if (isPlaying()) setPlaying(false)
            if ((e1?.eventTime ?: e2.eventTime).let { e2.eventTime - it } > 107) return true
            if (eventX == 0f) {
                eventX = e1?.rawX ?: 0f
                eventY = e1?.rawY ?: 0f
            }
            val absX = abs(e2.rawX - eventX)
            val absY = abs(e2.rawY - eventY)
            eventX = e2.rawX
            eventY = e2.rawY
            val correctedVx = if ((e2.rawX > e1?.rawX ?: 0f && velocityX < 0) || (e2.rawX < e1?.rawX ?: 0f && velocityX > 0)) -velocityX else velocityX
            if (absY > absX * 1.2f) {
                target = velocityY
                flingY()
            } else {
                scroller.fling(
                    currentPosition.toInt(), 0,
                    correctedVx.toInt(), 0,
                    (-timeLineW).toInt(), 0,
                    0, 0
                )
                invalidate()
            }
            return true
        }
    }

    // === INIT ===
    init {
        maxTime = -1
        TOLERANCE_X = 0.95f
        entityListAudio = mutableListOf()
        lastTime = 0L
        lastDifference = 0L
        setWillNotDraw(false)
        initAutoScroll()
        setOnTouchListener(this)
        scaleGestureDetector = ScaleGestureDetector(context, ScaleListener())
        gestureDetector = GestureDetectorCompat(context, gestureListener)
        scroller = Scroller(context)
    }

    private fun initAutoScroll() {
        autoScrollRunnable = Runnable {
            if (isAutoScroll) {
                val timeDiff = (System.currentTimeMillis() - timeStart) / FACTOR_VITESSE
                val speed = if (SPEED < 0f) -timeDiff else timeDiff + SPEED
                val sel = selectedEntity ?: return@Runnable
                if (sel.getTrimType() == 1) {
                    // Right trim
                    val newRight = sel.getRect().right + speed
                    if (newRight < maxTrim + sel.getRect().left) {
                        sel.getRect().right = newRight
                        iTrimLineCallback?.onUpdate()
                        invalidate()
                    }
                } else if (sel.getTrimType() == 0) {
                    // Left trim
                    val newLeft = sel.getRect().left + speed
                    if (newLeft > sel.getRect().right - maxTrim) {
                        sel.getRect().left = newLeft
                        iTrimLineCallback?.onUpdate()
                        invalidate()
                    }
                }
                autoScrollHandler.postDelayed(autoScrollRunnable, 100L)
            }
        }
        autoMoveRunnable = Runnable {
            if (isAutoMove) {
                val sel = selectedEntity
                if (sel != null) {
                    val dx = currentEventX - (centerX + scrolledWithZoom)
                    sel.getRect().offset(dx, 0f)
                    iTrimLineCallback?.onUpdate()
                    invalidate()
                }
                autoScrollHandler.postDelayed(autoMoveRunnable, 100L)
            }
        }
    }

    // === SCALE LISTENER ===
    private inner class ScaleListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
            isScaleListener = true
            iTrimLineCallback?.pause()
            return super.onScaleBegin(detector)
        }

        override fun onScale(detector: ScaleGestureDetector): Boolean {
            scaleFactor = maxOf(MIN_SCALE, minOf(scaleFactor * detector.scaleFactor, MAX_SCALE))
            scrolledWithZoom = scaleFactor * currentPosition
            invalidate()
            return true
        }

        override fun onScaleEnd(detector: ScaleGestureDetector) {
            super.onScaleEnd(detector)
        }
    }

    // === PUBLIC API ===
    fun getDefaultScale(): Float = 0.5f

    fun setBismilahTimeline(tl: EntityBismilahTimeline?) {
        bismilahTimeline = tl
    }

    fun getBismilahTimeline(): EntityBismilahTimeline? = bismilahTimeline

    fun setmIsi3adaTimeline(tl: EntityBismilahTimeline?) {
        mIsi3adaTimeline = tl
    }

    fun getmIsi3adaTimeline(): EntityBismilahTimeline? = mIsi3adaTimeline

    fun getEntityListTrslQuran(): List<EntityTrslTimeline> = entityListTrslQuran

    fun getEntityListQuran(): List<EntityQuranTimeline> = entityListQuran

    fun getEntityListAudio(): List<EntityAudio> = entityListAudio

    fun getEntityAudioNotDeleted(startIndex: Int): Pair<Int, EntityAudio>? {
        for (i in startIndex until entityListAudio.size) {
            if (entityListAudio[i].visible()) return Pair<Int, EntityAudio>(i, entityListAudio[i])
        }
        return null
    }

    fun clearAudio() {
        if (entityListAudio.isEmpty()) return
        entityListAudio.clear()
        val newStack = Stack<Pair<Entity, EntityAction>>()
        for (item in entityList) {
            if (item.first !is EntityAudio) newStack.push(item)
        }
        entityList.clear()
        entityList = newStack
    }

    fun getScaleFactor(): Float = scaleFactor

    fun setScaleFactor(f: Float) {
        scaleFactor = f
        scrolledWithZoom = f * currentPosition
    }

    fun init(width: Int, height: Int) {
        if (width <= 0 || height <= 0) return
        val wf = width.toFloat()
        SPEED = 0.04f * wf
        paintTime = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = -8355712
            typeface = Typeface.createFromAsset(resources.assets, "fonts/ReadexPro_Medium.ttf")
        }
        radius = 0.006f * wf
        paintTime.textSize = wf * 0.023f
        paintMaker = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = -1
            strokeWidth = radius * 0.5f
        }
        markerHeight = radius * 3f
        mPosYMarker = paintMaker.strokeWidth * 4f
        paddingCursur = 4f * radius
        centerX = widthScreen * 0.5f - radius * 0.5f
        detectRightMove = 0.4f * centerX
        detectLeftMove = centerX * 0.45f
        paintCursur = Paint(Paint.ANTI_ALIAS_FLAG).apply { strokeWidth = radius }
        val sw = paintCursur.strokeWidth * 2.8f
        paintLineCheck = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = -16121
            strokeWidth = paintCursur.strokeWidth
            pathEffect = DashPathEffect(floatArrayOf(sw, sw), 0f)
        }
        wTimeItem = paintTime.measureText("999") * 0.5f
    }

    fun setMaxTime(max: Int) {
        maxTime = max
        timeLineW = (max * getSecondInScreen()) / 1000f
    }

    fun setSecondInScreen(sec: Float, dur: Int, w: Int) {
        secondInScreen = sec
        duration = dur
        widthScreen = w
        dx = 0.03f * sec
        TOLERANCE_X = dx
        maxTrim = sec * 0.2f
    }

    fun setSecondInScreen(sec: Float) {
        secondInScreen = sec
        dx = 0.03f * sec
        maxTrim = sec * 0.2f
    }

    fun getSecondInScreenNoScale(): Float = secondInScreen

    fun getSecondInScreen(): Float = secondInScreen * scaleFactor

    fun getSelectedEntity(): Entity? = selectedEntity

    fun getTextSize(): Float = paintTime?.textSize?.times(1.42f) ?: 1f

    fun getCurrentPosition(): Float = scrolledWithZoom

    fun isPlaying(): Boolean = isPlaying

    fun setPlaying(v: Boolean) {
        isPlaying = v
    }

    fun getDuration(): Int = duration

    fun setDuration(d: Int) {
        duration = d
    }

    fun getXCursur(): Float = -currentPosition * scaleFactor

    fun getCurrentCursurPosition(): Int = currentCursurPosition

    fun setOnProgress(v: Boolean) {
        isProgress = v
    }

    fun getTimeLineW(): Float = timeLineW

    fun getMaxTime(): Int = maxTime

    fun isExist(tl: EntityBismilahTimeline?): Boolean = tl != null && tl.visible()

    fun setiTrimLineCallback(cb: ITrimLineCallback?) {
        iTrimLineCallback = cb
    }

    fun updateCurrentCursurPosition(pos: Int) {
        currentCursurPosition = pos
    }

    fun setCurrentCursurPosition(pos: Int) {
        currentCursurPosition = pos
    }

    fun updateCursur(timeMs: Int) {
        val pos = (timeMs * getSecondInScreen()) / 1000f
        currentPosition = -pos / scaleFactor
        scrolledWithZoom = scaleFactor * currentPosition
        invalidate()
    }

    fun setPosCursur(timeMs: Int) {
        val pos = (timeMs * secondInScreen) / 1000f
        currentPosition = -pos / scaleFactor
        scrolledWithZoom = scaleFactor * currentPosition
        invalidate()
    }

    fun updateCursur(position: Float) {
        currentPosition = -position / scaleFactor
        scrolledWithZoom = scaleFactor * currentPosition
        invalidate()
    }

    fun setRedoUndo(redo: ImageButton, undo: ImageButton) {
        btnRedo = redo
        btnUndo = undo
    }

    fun unselectEntity() {
        selectedEntity?.let {
            it.setSelect(false)
            selectedEntity = null
        }
    }

    // === ADD ENTITIES ===
    fun addAudio(audio: EntityAudio) {
        entityListAudio.add(audio)
    }

    fun addQuran(quran: EntityQuranTimeline) {
        entityListQuran.add(quran)
    }

    fun addTrslQuran(trsl: EntityTrslTimeline) {
        entityListTrslQuran.add(trsl)
    }

    fun addTrslQuran(trsl: EntityTrslTimeline, index: Int) {
        if (index < entityListTrslQuran.size) entityListTrslQuran.add(index, trsl)
        else entityListTrslQuran.add(trsl)
    }

    fun addQuran(quran: EntityQuranTimeline, index: Int) {
        if (index < entityListQuran.size) entityListQuran.add(index, quran)
        else entityListQuran.add(quran)
    }

    fun addQuranSplit(quran: EntityQuranTimeline, index: Int) {
        addQuran(quran, index)
    }

    fun addQuranSplit(trsl: EntityTrslTimeline, index: Int) {
        addTrslQuran(trsl, index)
    }

    // === CALCULATIONS ===
    fun calculMaxTime() {
        var maxEnd = 0f
        for (audio in entityListAudio) {
            if (audio.visible()) maxEnd = maxOf(maxEnd, audio.getRect().right)
        }
        for (quran in entityListQuran) {
            if (quran.visible()) maxEnd = maxOf(maxEnd, quran.getRect().right)
        }
        for (trsl in entityListTrslQuran) {
            if (trsl.visible()) maxEnd = maxOf(maxEnd, trsl.getRect().right)
        }
        bismilahTimeline?.let {
            if (it.visible()) maxEnd = maxOf(maxEnd, it.getRect().right)
        }
        mIsi3adaTimeline?.let {
            if (it.visible()) maxEnd = maxOf(maxEnd, it.getRect().right)
        }
        maxTime = (maxEnd / getSecondInScreen() * 1000).toInt().coerceAtLeast(maxTime)
        timeLineW = maxEnd
    }

    // === UNDO / REDO ===
    fun undo() {
        if (entityList.isEmpty()) return
        val pair = entityList.pop()
        undoEntityList.push(pair)
        val entity = pair.first
        val action = pair.second
        when (action) {
            EntityAction.ADD -> removeEntity(entity)
            EntityAction.DELETE -> restoreEntity(entity)
            EntityAction.MOVE -> entity.undoMove()
            EntityAction.TRIM -> entity.undoTrim()
            else -> {}
        }
        iTrimLineCallback?.enableUndo(entityList.isNotEmpty())
        iTrimLineCallback?.enableRedo(undoEntityList.isNotEmpty())
        invalidate()
    }

    fun redo() {
        if (undoEntityList.isEmpty()) return
        val pair = undoEntityList.pop()
        entityList.push(pair)
        val entity = pair.first
        val action = pair.second
        when (action) {
            EntityAction.ADD -> restoreEntity(entity)
            EntityAction.DELETE -> removeEntity(entity)
            EntityAction.MOVE -> entity.redoMove()
            EntityAction.TRIM -> entity.redoTrim()
            else -> {}
        }
        iTrimLineCallback?.enableUndo(entityList.isNotEmpty())
        iTrimLineCallback?.enableRedo(undoEntityList.isNotEmpty())
        invalidate()
    }

    private fun removeEntity(entity: Entity) {
        when (entity) {
            is EntityAudio -> entity.isVisible = false
            is EntityQuranTimeline -> entity.isVisible = false
            is EntityTrslTimeline -> entity.isVisible = false
            is EntityBismilahTimeline -> entity.isVisible = false
        }
    }

    private fun restoreEntity(entity: Entity) {
        when (entity) {
            is EntityAudio -> entity.isVisible = true
            is EntityQuranTimeline -> entity.isVisible = true
            is EntityTrslTimeline -> entity.isVisible = true
            is EntityBismilahTimeline -> entity.isVisible = true
        }
    }

    // === HIT TESTING ===
    private fun handleItemInteraction(x: Float, y: Float): Boolean {
        val quranCount = processQuranItemsSelection()
        val trslCount = processTrslQuranItemsSelection()
        val audioCount = processAudioItemsSelection()
        val totalSelected = quranCount + trslCount + audioCount
        if (totalSelected > 1) {
            iTrimLineCallback?.onSelectMultiple(totalSelected)
        }
        return totalSelected > 0
    }

    private fun processQuranItemsSelection(): Int {
        var count = 0
        for (entity in entityListQuran) {
            if (entity.visible() && entity.contains(PointF(0f, 0f))) count++
        }
        return count
    }

    private fun processTrslQuranItemsSelection(): Int {
        var count = 0
        for (entity in entityListTrslQuran) {
            if (entity.visible() && entity.contains(PointF(0f, 0f))) count++
        }
        return count
    }

    private fun processAudioItemsSelection(): Int {
        var count = 0
        for (entity in entityListAudio) {
            if (entity.visible() && entity.contains(PointF(0f, 0f))) count++
        }
        return count
    }

    fun isPass(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y
        return y > startYDraw - 10f && y < maxBottom + 10f &&
                x > 0f && x < width.toFloat()
    }

    // === LAYOUT ===
    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        if (changed) updateGestureExclusion()
    }

    private fun updateGestureExclusion() {
        try {
            if (Build.VERSION.SDK_INT >= 29) {
                exclusionRects.clear()
                val insets = rootWindowInsets?.systemGestureInsets ?: return
                exclusionRects.add(Rect(0, 0, insets.left, height))
                exclusionRects.add(Rect(right - insets.right, 0, right, height))
                setSystemGestureExclusionRects(exclusionRects)
            }
        } catch (_: Exception) {
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldW: Int, oldH: Int) {
        super.onSizeChanged(w, h, oldW, oldH)
        if (h < 1 || w < 1) return
        val hf = h.toFloat()
        maxBottom = 0.78f * hf
        startYDraw = 0.18f * hf
        canvasTopY = 0.1f * hf
        posY = 0.05f * hf
        p = hf * 0.026f
    }

    // === DRAWING ===
    override fun onDraw(canvas: Canvas) {
        if (!::paintTime.isInitialized || isProgress) return
        try {
            mDraw(canvas)
            if (!isPlaying()) drawItemBtn(canvas)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        super.onDraw(canvas)
    }

    private fun mDraw(canvas: Canvas) {
        canvas.drawColor(-15658735)
        canvas.save()
        val secInScreen = getSecondInScreen()
        canvas.translate(centerX + scrolledWithZoom, paddingTop.toFloat())
        var startSec = ((abs(scrolledWithZoom) - centerX) / secInScreen).toInt()
        val endSec = ((abs(scrolledWithZoom) + centerX) / secInScreen).toInt() + 1
        if (startSec < 0) startSec = 0
        drawTimeBar(canvas, startSec, endSec, secInScreen)
        canvas.clipRect(-secondInScreen, canvasTopY, width - scrolledWithZoom, height - mScrollY)
        canvas.translate(0f, mScrollY)
        drawAllEntities(canvas, startSec, endSec)
        if (isCheckLine) {
            canvas.drawLine(startXLine, 0f, startXLine, height - mScrollY, paintLineCheck)
        }
        canvas.restore()
        if (isCheckLineCursur) {
            paintCursur.color = paintLineCheck.color
        } else {
            paintCursur.color = -1
        }
        canvas.drawLine(
            centerX + paintMaker.strokeWidth,
            posY + mPosYMarker + paintMaker.strokeWidth,
            centerX,
            height.toFloat(),
            paintCursur
        )
    }

    private fun drawTimeBar(canvas: Canvas, startSec: Int, endSec: Int, secInScreen: Float) {
        var step = 4f
        when {
            scaleFactor >= 4f -> step = 0.25f
            scaleFactor >= 2f -> step = 0.5f
            scaleFactor >= 0.8f -> step = 2f
            scaleFactor < 0.4f -> step = if (scaleFactor > 0.25f) 6f else 8f
        }
        val startF = startSec.toFloat()
        val subStep = secInScreen * step * 0.2f
        var t = startF - (startF % step)
        while (t <= endSec) {
            val x = t * secInScreen
            val sec = x / secInScreen
            drawMarker(canvas, x, markerHeight)
            val label = if (isArabicLang) formatTimeLabelArabic(sec) else formatTimeLabel(sec)
            canvas.drawText(label, x - wTimeItem, posY, paintTime)
            for (i in 1..4) {
                drawMarker(canvas, i * subStep + x, markerHeight / 2f)
            }
            t += step
        }
    }

    private fun drawMarker(canvas: Canvas, x: Float, h: Float) {
        val strokeWidth = x + paintMaker.strokeWidth
        val top = posY + mPosYMarker
        canvas.drawLine(strokeWidth, top, strokeWidth, top + h, paintMaker)
    }

    private fun formatTimeLabel(sec: Float): String {
        return if (sec < 60f) {
            if (abs(sec - Math.round(sec)) < 0.01) "${sec.toInt()}s"
            else String.format(java.util.Locale.ENGLISH, "%.2fs", sec)
        } else {
            val min = (sec / 60f).toInt()
            val rem = Math.round(sec % 60f)
            if (rem == 0) "${min}m" else "${min}m ${rem}s"
        }
    }

    private fun formatTimeLabelArabic(sec: Float): String {
        return if (sec < 60f) {
            if (abs(sec - Math.round(sec)) < 0.01) "${sec.toInt()}\u062B"
            else String.format(java.util.Locale.ENGLISH, "%.2f\u062B", sec)
        } else {
            val min = (sec / 60f).toInt()
            val rem = Math.round(sec % 60f)
            if (rem == 0) "${min}\u062F" else "${min}\u062F ${rem}\u062B"
        }
    }

    private fun drawAllEntities(canvas: Canvas, startSec: Int, endSec: Int) {
        // Draw audio entities
        for (audio in entityListAudio) {
            if (audio.visible()) {
                val rect = audio.getRect()
                paintItem.color = audio.getColor()
                canvas.drawRoundRect(rect, 5f, 5f, paintItem)
            }
        }
        // Draw bismilah
        bismilahTimeline?.let {
            if (it.visible()) {
                val rect = it.getRect()
                paintItem.color = it.getColor()
                canvas.drawRoundRect(rect, 5f, 5f, paintItem)
            }
        }
        mIsi3adaTimeline?.let {
            if (it.visible()) {
                val rect = it.getRect()
                paintItem.color = it.getColor()
                canvas.drawRoundRect(rect, 5f, 5f, paintItem)
            }
        }
        // Draw quran entities
        for (quran in entityListQuran) {
            if (quran.visible()) {
                val rect = quran.getRect()
                paintItem.color = quran.getColor()
                canvas.drawRoundRect(rect, 5f, 5f, paintItem)
                // Draw selection highlight
                if (quran.isSelect()) {
                    paintItem.color = CLR_SELECT
                    paintItem.alpha = 80
                    canvas.drawRoundRect(rect, 5f, 5f, paintItem)
                    paintItem.alpha = 255
                }
            }
        }
        // Draw translation entities
        for (trsl in entityListTrslQuran) {
            if (trsl.visible()) {
                val rect = trsl.getRect()
                paintItem.color = trsl.getColor()
                canvas.drawRoundRect(rect, 5f, 5f, paintItem)
                if (trsl.isSelect()) {
                    paintItem.color = CLR_SELECT
                    paintItem.alpha = 80
                    canvas.drawRoundRect(rect, 5f, 5f, paintItem)
                    paintItem.alpha = 255
                }
            }
        }
        // Draw trim handles for selected entity
        selectedEntity?.let { sel ->
            if (sel.isSelect()) {
                drawTrimHandles(canvas, sel)
            }
        }
    }

    private fun drawTrimHandles(canvas: Canvas, entity: Entity) {
        val rect = entity.getRect()
        val handlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = -1
            style = Paint.Style.FILL
        }
        // Left trim handle
        canvas.drawCircle(rect.left, rect.centerY(), radius * 3f, handlePaint)
        // Right trim handle
        canvas.drawCircle(rect.right, rect.centerY(), radius * 3f, handlePaint)
    }

    private fun drawItemBtn(canvas: Canvas) {
        val w = width
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = -14540254 }
        val iconWidth = (w * 0.104f).toInt()
        val margin = (w * 0.03f).toInt()
        val cornerRadius = (w * 0.015f).toInt()

        // Audio button
        val rect = RectF(
            margin.toFloat(),
            startYDraw,
            margin + iconWidth.toFloat(),
            startYDraw + iconWidth.toFloat()
        )
        canvas.drawRoundRect(rect, cornerRadius.toFloat(), cornerRadius.toFloat(), paint)
        val addAudio = ContextCompat.getDrawable(context, R.drawable.add_audio)
        addAudio?.setTint(-1052689)
        addAudio?.setBounds(
            rect.left.toInt(),
            rect.top.toInt(),
            rect.right.toInt(),
            rect.bottom.toInt()
        )
        addAudio?.draw(canvas)

        // Quran button
        val rect2 = RectF(
            margin.toFloat(),
            rect.bottom + margin,
            margin + iconWidth.toFloat(),
            rect.bottom + margin + iconWidth
        )
        canvas.drawRoundRect(rect2, cornerRadius.toFloat(), cornerRadius.toFloat(), paint)
        val addQuran = ContextCompat.getDrawable(context, R.drawable.add_quran)
        addQuran?.setTint(-1052689)
        addQuran?.setBounds(
            rect2.left.toInt(),
            rect2.top.toInt(),
            rect2.right.toInt(),
            rect2.bottom.toInt()
        )
        addQuran?.draw(canvas)
    }

    // === SCROLL ===
    fun finishScroll() {
        scroller.forceFinished(true)
        isFling = false
    }

    override fun computeScroll() {
        if (scroller.computeScrollOffset()) {
            currentPosition = scroller.currX.toFloat()
            scrolledWithZoom = scaleFactor * currentPosition
            iTrimLineCallback?.onSeekPlayer(scrolledWithZoom)
            invalidate()
        }
        super.computeScroll()
    }

    fun pauseScroll() {
        scroller.forceFinished(true)
    }

    fun flingY() {
        // Vertical fling - scroll Y
        mScrollY = target.coerceIn(0f, 0f)
    }

    fun setFlingY(v: Float) {
        mScrollY = v.coerceIn(0f, 0f)
    }

    // === TOUCH ===
    override fun onTouch(v: View, event: MotionEvent): Boolean {
        scaleGestureDetector.onTouchEvent(event)
        gestureDetector.onTouchEvent(event)

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                currentEventX = event.x
            }
            MotionEvent.ACTION_MOVE -> {
                currentEventX = event.x
                selectedEntity?.let { sel ->
                    if (sel.getTrimType() == 0 || sel.getTrimType() == 1) {
                        // Trim mode - handled by auto-scroll
                    } else if (isMove) {
                        val dx = event.x - eventX
                        val dy = event.y - eventY
                        sel.getRect().offset(dx / scaleFactor, 0f)
                        iTrimLineCallback?.onUpdate()
                        invalidate()
                    }
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                selectedEntity?.let { sel ->
                    if (sel.getTrimType() != -1) {
                        sel.setTrimType(-1)
                        isAutoScroll = false
                        isAutoMove = false
                        entityList.push(Pair(sel, EntityAction.TRIM))
                        iTrimLineCallback?.onAddStack(EntityAction.TRIM)
                        iTrimLineCallback?.onUpdate()
                    } else if (isMove) {
                        isMove = false
                        isAutoMove = false
                        entityList.push(Pair(sel, EntityAction.MOVE))
                        iTrimLineCallback?.onAddStack(EntityAction.MOVE)
                    }
                    sel.setCurrentRect()
                }
                isOnUp = true
                iTrimLineCallback?.onUp()
                eventX = 0f
                eventY = 0f
                isScaleListener = false
            }
        }
        return true
    }

    // === UPDATE SELECTION ON TAP ===
    fun updateSelectionOnTap(event: MotionEvent) {
        val x = event.x + paddingLeft + (centerX - radius * 0.5f) + scrolledWithZoom
        val y = event.y

        // Check entities in reverse z-order
        for (i in entityListQuran.indices.reversed()) {
            val entity = entityListQuran[i]
            if (entity.visible() && entity.getRect().contains(x, y)) {
                selectEntity(entity)
                return
            }
        }
        for (i in entityListTrslQuran.indices.reversed()) {
            val entity = entityListTrslQuran[i]
            if (entity.visible() && entity.getRect().contains(x, y)) {
                selectEntity(entity)
                return
            }
        }
        for (i in entityListAudio.indices.reversed()) {
            val entity = entityListAudio[i]
            if (entity.visible() && entity.getRect().contains(x, y)) {
                selectEntity(entity)
                return
            }
        }
        // Nothing selected
        unselectEntity()
        iTrimLineCallback?.onEmptySelect()
    }

    private fun selectEntity(entity: Entity) {
        unselectEntity()
        selectedEntity = entity
        entity.setSelect(true)
        iTrimLineCallback?.onSelectEntity(entity, 0f)
    }

    // === GET AUDIO / QURAN / TRSL ===
    fun getAudio(): EntityAudio? = entityListAudio.lastOrNull { it.visible() }

    fun getLastAyaQuran(): EntityQuranTimeline? = entityListQuran.lastOrNull { it.visible() }

    fun getQuran(): EntityQuranTimeline? = entityListQuran.firstOrNull { it.visible() }

    fun getTrslQuran(): EntityTrslTimeline? = entityListTrslQuran.firstOrNull { it.visible() }

    fun getPreviewOrNextEntityAudio(
        list: List<EntityAudio>,
        index: Int,
        isNext: Boolean
    ): EntityAudio? {
        if (index < 0 || index >= list.size) return null
        return if (isNext) list.getOrNull(index + 1) else list.getOrNull(index - 1)
    }

    fun getPreviewOrNextEntityQuran(
        list: List<EntityQuranTimeline>,
        index: Int,
        isNext: Boolean
    ): EntityQuranTimeline? {
        if (index < 0 || index >= list.size) return null
        return if (isNext) list.getOrNull(index + 1) else list.getOrNull(index - 1)
    }

    fun getPreviewOrNextEntityTrslQuran(
        list: List<EntityTrslTimeline>,
        index: Int,
        isNext: Boolean
    ): EntityTrslTimeline? {
        if (index < 0 || index >= list.size) return null
        return if (isNext) list.getOrNull(index + 1) else list.getOrNull(index - 1)
    }

    fun updateWhenEffect(audio: EntityAudio) {
        // Update audio when effect changes
        iTrimLineCallback?.onUpdatePlayerAudio(audio)
    }

    // === TRANSITIONS ===
    fun translateFromNow() {
        // Move selected entity from current cursor position
        selectedEntity?.let { sel ->
            val cursorPos = -currentPosition * scaleFactor
            val rect = sel.getRect()
            rect.offsetTo(cursorPos, rect.top)
            invalidate()
        }
    }

    fun translateToRight(animate: Boolean) {
        selectedEntity?.let { sel ->
            val lastEntity = entityListQuran.lastOrNull { it.visible() }
            val targetX = lastEntity?.getRect()?.right ?: 0f
            sel.getRect().offsetTo(targetX, sel.getRect().top)
            invalidate()
        }
    }

    fun translateToRight() {
        translateToRight(false)
    }

    fun translateFromStart() {
        selectedEntity?.let { sel ->
            sel.getRect().offsetTo(0f, sel.getRect().top)
            invalidate()
        }
    }

    fun translateUntilNow() {
        selectedEntity?.let { sel ->
            val cursorPos = -currentPosition * scaleFactor
            sel.getRect().offsetTo(cursorPos - sel.getRect().width(), sel.getRect().top)
            invalidate()
        }
    }

    fun translateToRightBismilah(tl: EntityBismilahTimeline) {
        tl.getRect().offsetTo(0f, tl.getRect().top)
        invalidate()
    }

    fun translateEndNow() {
        selectedEntity?.let { sel ->
            val cursorPos = -currentPosition * scaleFactor
            sel.getRect().offsetTo(cursorPos - sel.getRect().width(), sel.getRect().top)
            invalidate()
        }
    }
}
