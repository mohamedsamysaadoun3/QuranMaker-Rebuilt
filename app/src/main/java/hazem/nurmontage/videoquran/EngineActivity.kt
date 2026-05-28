package hazem.nurmontage.videoquran

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.arthenica.ffmpegkit.FFmpegKit
import com.arthenica.ffmpegkit.FFmpegSessionCompleteCallback
import com.arthenica.ffmpegkit.ReturnCode
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import hazem.nurmontage.videoquran.Utils.*
import hazem.nurmontage.videoquran.adabter.DimensionAdabters
import hazem.nurmontage.videoquran.common.Common
import hazem.nurmontage.videoquran.constant.AyaTextPreset
import hazem.nurmontage.videoquran.constant.EffectAudioType
import hazem.nurmontage.videoquran.constant.EntityAction
import hazem.nurmontage.videoquran.constant.IpadType
import hazem.nurmontage.videoquran.constant.ResizeType
import hazem.nurmontage.videoquran.constant.SurahNameStyle
import hazem.nurmontage.videoquran.entity_timeline.Entity
import hazem.nurmontage.videoquran.entity_timeline.EntityAudio
import hazem.nurmontage.videoquran.entity_timeline.EntityBismilahTimeline
import hazem.nurmontage.videoquran.entity_timeline.EntityQuranTimeline
import hazem.nurmontage.videoquran.entity_timeline.EntityTrslTimeline
import hazem.nurmontage.videoquran.fragment.*
import hazem.nurmontage.videoquran.model.*
import hazem.nurmontage.videoquran.views.BlurredImageView
import hazem.nurmontage.videoquran.views.ButtonCustumFont
import hazem.nurmontage.videoquran.views.CustomDiscreteSeekBar
import hazem.nurmontage.videoquran.views.TextCustumFont
import hazem.nurmontage.videoquran.views.TextCustumFontBold
import hazem.nurmontage.videoquran.views.TrackEntityView
import java.io.File
import java.util.concurrent.Executor
import java.util.concurrent.Executors

/**
 * Main video editor activity — the core of the NurMontage app.
 * Manages timeline, entities, audio, video, and export.
 */
class EngineActivity : BaseActivity() {

    companion object {
        private const val FPS = 25
        private const val REQUEST_WRITE_EXTERNAL_STORAGE = 1
        private const val REQUEST_CODE_AUDIO = 2
        private const val IMAGE_PERMISSION_REQUEST_CODE = 10
        private const val VIDEO_PERMISSION_REQUEST_CODE = 11
        private const val EXTRACT_AUDIO_VIDEO_PERMISSION_REQUEST_CODE = 12
    }

    // ── UI Elements ────────────────────────────────────────────────────
    lateinit var blurredImageView: BlurredImageView
    lateinit var trackViewEntity: TrackEntityView
    lateinit var btnPlayPause: ImageButton
    lateinit var btnToEnd: ImageButton
    lateinit var btnToStart: ImageButton
    lateinit var btnRedo: ImageButton
    lateinit var btnUndo: ImageButton
    lateinit var btn_cancel: ImageButton
    lateinit var btn_export: ButtonCustumFont
    lateinit var btn_setup_fps: LinearLayout
    lateinit var btnIpod: LinearLayout
    lateinit var btnChangeResize: LinearLayout
    lateinit var ivIpod: ImageView
    lateinit var ivResize: ImageView
    lateinit var tv_currentTime: TextView
    lateinit var tv_endTime: TextView
    lateinit var tv_resolution: TextCustumFont
    lateinit var tv_tittle_fragment: TextCustumFont
    lateinit var textChangeResize: TextCustumFont
    lateinit var seekBar_fps: CustomDiscreteSeekBar
    lateinit var seekBar_res: CustomDiscreteSeekBar
    lateinit var layout_resolution: LinearLayout

    // ── State ──────────────────────────────────────────────────────────
    var mIsPlaying = false
    var isOnScroll = false
    var isToCrop = false
    var isSaveTmpTemplate = true
    var oneExport = false
    var isProcessingFrame = false
    var mCurrentFragment: Fragment? = null
    var mPlayer: MediaPlayer? = null
    var mResources: android.content.res.Resources? = null
    var mTemplate: Template? = null
    var uri_bg: String? = null
    var current_position_time = 0
    var startCursur = 0
    var endFrame = 0
    var endTimeAudioVisible = 0
    var lastIndexVisible = 0
    var entityAudio_player: EntityAudio? = null
    var entityAudio_visible: EntityAudio? = null
    var dialog: Dialog? = null
    var dialogInternet: Dialog? = null

    private val executor: Executor = Executors.newSingleThreadExecutor()
    private val id_ffmpeg = mutableListOf<Long>()
    private val mainHandler = Handler(Looper.getMainLooper())
    private var pendingFramePath: String? = null
    private val frameLock = Any()
    private var start_extenstion = 0
    private val extentions = arrayOf(".mp3", ".ogg", ".acc", ".m4a", ".wav", ".mpeg")

    // ── Animator state ─────────────────────────────────────────────────
    private var timelineAnimatorListener: SmoothTimelineAnimator.AnimatorListener? = null
    private var smoothTimelineAnimator: SmoothTimelineAnimator? = null
    private var smoothVideoAnimator: SmoothVideoAnimator? = null

    // ── Activity Result Launchers ──────────────────────────────────────
    private lateinit var activityLauncher: ActivityResultLauncher<Intent>
    private lateinit var searchAyaResult: ActivityResultLauncher<Intent>
    private lateinit var nameReaderResult: ActivityResultLauncher<Intent>
    private lateinit var editSurahNameResult: ActivityResultLauncher<Intent>
    private lateinit var editTrslResult: ActivityResultLauncher<Intent>
    private lateinit var launchChoiceBgActivity: ActivityResultLauncher<Intent>
    private lateinit var launchCropActivity: ActivityResultLauncher<Intent>
    private lateinit var launchImg: ActivityResultLauncher<Intent>
    private lateinit var launchVideo: ActivityResultLauncher<Intent>
    private lateinit var launchVideoExtract: ActivityResultLauncher<Intent>

    // ── Back Press ─────────────────────────────────────────────────────
    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            if (mCurrentFragment != null) {
                hideFragment()
            } else {
                dialog()
            }
        }
    }

    // ── Lifecycle ──────────────────────────────────────────────────────

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_time_line)

        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
        WindowCompat.setDecorFitsSystemWindows(window, true)

        setStatusBarColor(ViewCompat.MEASURED_STATE_MASK)
        setNavigationBarColor(ViewCompat.MEASURED_STATE_MASK)

        val insetsController = WindowCompat.getInsetsController(window, window.decorView)
        insetsController.isAppearanceLightStatusBars = false
        insetsController.isAppearanceLightNavigationBars = false

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { view, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(insets.left, insets.top, insets.right, insets.bottom)
            windowInsets
        }

        mResources = resources
        if (mResources == null) { finish(); return }

        wakeLockAquire()
        showProgress()
        loadTemplate()
        initLaunchers()
        initTimeLineView()
        initViews()
        checkUriShared()
    }

    override fun onPause() {
        super.onPause()
        if (isSaveTmpTemplate) {
            saveTemplateTmp()
        }
        iTrimLineCallback.onEmptySelect()
        cancelDialog()
    }

    override fun onResume() {
        super.onResume()
        isToCrop = false
        isSaveTmpTemplate = true
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            Glide.get(this).clearMemory()
        } catch (_: Exception) {}
        clearFFmpeg()
        clearCallback()
        pausePlayer()
        mPlayer?.release()
        mPlayer = null
    }

    // ── Template Management ────────────────────────────────────────────

    private fun loadTemplate() {
        try {
            val templateId = intent.getStringExtra(Common.TEMPLATE)
            if (templateId != null) {
                mTemplate = LocalPersistence.readObjectFromFile(this, templateId)
            }
            if (mTemplate == null) {
                mTemplate = LocalPersistence.readObjectFromFile(this, Common.TEMPLATE_TMP)
            }
            if (mTemplate == null) {
                mTemplate = Template().apply {
                    width = 720
                    height = 1280
                    fps = 25
                    uri_bg = "default"
                    ipad_type = IpadType.IPAD.ordinal
                    resizeType = ResizeType.SOCIAL_STORY.ordinal
                    color_ipad = -1
                }
            }
            uri_bg = mTemplate!!.uri_bg
        } catch (e: Exception) {
            e.printStackTrace()
            mTemplate = Template().apply {
                width = 720
                height = 1280
                fps = 25
                uri_bg = "default"
                ipad_type = IpadType.IPAD.ordinal
                resizeType = ResizeType.SOCIAL_STORY.ordinal
            }
        }
    }

    fun saveTemplateTmp() {
        try {
            val template = mTemplate ?: return
            // Save entity states from views
            template.quranEntityList.clear()
            template.translationTemplateList.clear()
            template.entityMediaList.clear()

            for (entity in trackViewEntity.getEntityListQuran()) {
                entity.quranEntity?.let { qe ->
                    val tmpl = EntityQuranTemplate(
                        transition = qe.entityTransition,
                        aya = qe.aya,
                        complete_aya = qe.completeAya,
                        name_font = qe.fontName,
                        color = qe.textColor,
                        number = qe.entityIndex
                    )
                    tmpl.icon = qe.icon
                    tmpl.x = qe.x
                    tmpl.y = qe.y
                    tmpl.scale = qe.scale
                    tmpl.factor_size = qe.factorSize
                    tmpl.start = entity.startMs.toFloat()
                    tmpl.end = entity.endMs.toFloat()
                    template.addQuranEntityList(tmpl)
                }
            }
            for (entity in trackViewEntity.getEntityListTrslQuran()) {
                // TranslationTrslTimeline has template field but we need to create EntityTranslationTemplate
                val tmpl = EntityTranslationTemplate(
                    start = entity.startMs.toFloat(),
                    end = entity.endMs.toFloat()
                )
                template.addTrslEntityList(tmpl)
            }
            for (audio in trackViewEntity.getEntityListAudio()) {
                val media = EntityMedia(
                    uri = audio.filePath,
                    start = audio.startMs.toFloat(),
                    end = audio.endMs.toFloat(),
                    duration_fade_in = audio.fadeInDurationMs.toFloat(),
                    duration_fade_out = audio.fadeOutDurationMs.toFloat()
                )
                template.addMedia(media)
            }

            // Save bismilah/isti3ada
            trackViewEntity.getBismilahTimeline()?.let { btl ->
                template.entityBismilahTemplate = EntityBismilahTemplate(
                    start = btl.startMs.toFloat(),
                    end = btl.endMs.toFloat(),
                    aya = btl.bismilahText
                )
            }
            trackViewEntity.getmIsi3adaTimeline()?.let { itl ->
                template.entityIsti3adaTemplate = EntityBismilahTemplate(
                    start = itl.startMs.toFloat(),
                    end = itl.endMs.toFloat(),
                    aya = itl.bismilahText
                )
            }

            LocalPersistence.duplicateTemplate(this, template, Common.TEMPLATE_TMP)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun saveTemplate() {
        try {
            val template = mTemplate ?: return
            saveTemplateTmp()
            val id = template.idTemplate ?: System.currentTimeMillis().toString()
            template.idTemplate = id
            LocalPersistence.writeTemplate(this, template, Common.TEMPLATE_TMP, id)
            LocalPersistence.deleteTemplate(this, Common.TEMPLATE_TMP)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun addEntityFromTemplate() {
        val template = mTemplate ?: return
        try {
            // Restore Quran entities
            for (tmpl in template.quranEntityList) {
                addEntityFromTemplateQuran(tmpl)
            }
            // Restore translation entities
            for (tmpl in template.translationTemplateList) {
                addEntityTrslFromTemplate(tmpl)
            }
            // Restore bismilah entity
            template.entityBismilahTemplate?.let { addEntityBismilahFromTemplate(it) }
            // Restore isti3ada entity
            template.entityIsti3adaTemplate?.let { addEntityIsti3adaFromTemplate(it) }
            // Restore audio entities
            for (media in template.entityMediaList) {
                addAudioFromTemplate(media)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        updateTime()
    }

    // ── UI Initialization ──────────────────────────────────────────────

    private fun initViews() {
        blurredImageView = findViewById(R.id.view)
        trackViewEntity = findViewById(R.id.time_line_view)
        btnPlayPause = findViewById(R.id.btn_play_pause)
        btnToEnd = findViewById(R.id.btn_to_end)
        btnToStart = findViewById(R.id.btn_to_start)
        btnRedo = findViewById(R.id.btn_redo)
        btnUndo = findViewById(R.id.btn_undo)
        btn_cancel = findViewById(R.id.btn_cancel)
        btn_export = findViewById(R.id.btn_export)
        btn_setup_fps = findViewById(R.id.btn_setup_fps)
        btnIpod = findViewById(R.id.btn_ipad)
        btnChangeResize = findViewById(R.id.btn_change_aspect)
        ivIpod = findViewById(R.id.iv_ipod)
        ivResize = findViewById(R.id.iv_ratio)
        tv_currentTime = findViewById(R.id.tv_current_time)
        tv_endTime = findViewById(R.id.tv_end_time)
        tv_resolution = findViewById(R.id.tv_resolution)
        tv_tittle_fragment = findViewById(R.id.tv_tittle_fragment)
        textChangeResize = findViewById(R.id.tv_ratio)
        // seekBar_fps and seekBar_res are not in the layout - use layout_resolution directly
        // seekBar_fps = findViewById(R.id.seek_bar_fps)
        // seekBar_res = findViewById(R.id.seek_bar_res)
        layout_resolution = findViewById(R.id.layout_resolution)

        // Play/Pause
        btnPlayPause.setOnClickListener {
            if (mIsPlaying) {
                pausePlayer()
            } else {
                startTimelineAnimation()
            }
        }

        // To End
        btnToEnd.setOnClickListener {
            val maxTime = trackViewEntity.getMaxTime()
            if (maxTime > 0) {
                startCursur = maxTime
                trackViewEntity.updateCurrentCursurPosition(maxTime)
                updateTime(maxTime)
                updateBtnToStart()
                updateBtnToEnd()
            }
        }

        // To Start
        btnToStart.setOnClickListener {
            startCursur = 0
            trackViewEntity.updateCurrentCursurPosition(0)
            updateTime(0)
            updateBtnToStart()
            updateBtnToEnd()
        }

        // Undo
        btnUndo.setOnClickListener {
            executor.execute {
                trackViewEntity.undo()
                mainHandler.post {
                    updateTime()
                    blurredImageView.postInvalidate()
                    disableUndoBtn()
                }
            }
        }

        // Redo
        btnRedo.setOnClickListener {
            executor.execute {
                trackViewEntity.redo()
                mainHandler.post {
                    updateTime()
                    blurredImageView.postInvalidate()
                    disableRedoBtn()
                }
            }
        }

        // Cancel
        btn_cancel.setOnClickListener { dialog() }

        // Export
        btn_export.setOnClickListener {
            if (!oneExport) {
                save()
            }
        }

        // FPS/Resolution setup
        btn_setup_fps.setOnClickListener {
            layout_resolution.visibility = if (layout_resolution.visibility == View.VISIBLE) View.GONE else View.VISIBLE
        }

        // Add Quran
        findViewById<View>(R.id.btn_add_quran).setOnClickListener {
            val ft = supportFragmentManager.beginTransaction()
            mCurrentFragment = AddQuranFragment.getInstance(iAddQuran, mResources!!)
            ft.replace(R.id.m_container, mCurrentFragment!!)
            ft.commit()
            setupShowFragment(mResources!!.getString(R.string.quran))
        }

        // Background
        findViewById<View>(R.id.btn_bg).setOnClickListener {
            val ft = supportFragmentManager.beginTransaction()
            mCurrentFragment = ChangeBgFragment.getInstance(iChangeBgCallback, mResources!!, uri_bg)
            ft.replace(R.id.m_container, mCurrentFragment!!)
            ft.commit()
            setupShowFragment(mResources!!.getString(R.string.bg))
        }

        // iPad style
        btnIpod.setOnClickListener {
            val ft = supportFragmentManager.beginTransaction()
            mCurrentFragment = EditIpadFragment.getInstance(
                mResources,
                mTemplate?.ipad_type ?: IpadType.IPAD.ordinal,
                iIpadEditCallback,
                mTemplate?.index_color ?: -1,
                mTemplate?.gradient != null,
                mTemplate?.isGlass ?: false
            )
            ft.replace(R.id.m_container, mCurrentFragment!!)
            ft.commit()
            setupShowFragment(mResources!!.getString(R.string.ipad))
        }

        // Resize / Aspect ratio
        btnChangeResize.setOnClickListener {
            if (!BillingPreferences.isSubscribed(this)) {
                dialogPremium(0)
                return@setOnClickListener
            }
            val ft = supportFragmentManager.beginTransaction()
            val resizeTypeStr = when (mTemplate?.resizeType ?: ResizeType.SOCIAL_STORY.ordinal) {
                ResizeType.YOUTUBE_16_9.ordinal -> "16:9"
                ResizeType.SQUARE.ordinal -> "1:1"
                else -> "9:16"
            }
            mCurrentFragment = ResizeFragment(iDimensionCallback, mResources!!, resizeTypeStr)
            ft.replace(R.id.m_container, mCurrentFragment!!)
            ft.commit()
            setupShowFragment("Resize")
        }

        // Pro button
        findViewById<View>(R.id.to_pro).setOnClickListener {
            if (!BillingPreferences.isSubscribed(this)) {
                toProVersion()
            }
        }

        // Resolution / FPS seekbars
        initResolution()

        // BlurredImageView callback
        blurredImageView.iViewCallback = object : BlurredImageView.IViewCallback {
            override fun onDrawFinish() {}
            override fun onEmtyClick() {
                iTrimLineCallback.onEmptySelect()
            }
            override fun onEndMove() {
                trackViewEntity.invalidate()
            }
            override fun onEndScale() {
                trackViewEntity.invalidate()
            }
            override fun onSelect(entityView: hazem.nurmontage.videoquran.model.EntityView) {
                // Handled via track selection
            }
            override fun onSquare() {}
            override fun onWattermark() {}
        }

        // Initialize BlurredImageView with template
        blurredImageView.post {
            val template = mTemplate ?: return@post
            blurredImageView.isPlaying = false

            if (template.isVideoSquare) {
                initTypeVideo()
            } else {
                iniTypeImg()
            }
        }
    }

    private fun initTimeLineView() {
        val screenWidth = ScreenUtils.getScreenWidth(this)
        trackViewEntity.init(screenWidth, (screenWidth * 0.12f).toInt())
        trackViewEntity.setiTrimLineCallback(iTrimLineCallback)
    }

    private fun initResolution() {
        seekBar_fps.mListener = object : CustomDiscreteSeekBar.OnProgressChangeListener {
            override fun onProgressChanged(seekBar: CustomDiscreteSeekBar, index: Int, label: String, fromUser: Boolean) {
                mTemplate?.fps = index
            }
            override fun onStartTrackingTouch(seekBar: CustomDiscreteSeekBar) {}
            override fun onStopTrackingTouch(seekBar: CustomDiscreteSeekBar) {}
        }
        seekBar_res.mListener = object : CustomDiscreteSeekBar.OnProgressChangeListener {
            override fun onProgressChanged(seekBar: CustomDiscreteSeekBar, index: Int, label: String, fromUser: Boolean) {
                val dims = when (index) {
                    0 -> Pair(480, 854)
                    1 -> Pair(720, 1280)
                    2 -> Pair(1080, 1920)
                    3 -> Pair(2160, 3840)
                    else -> Pair(720, 1280)
                }
                mTemplate?.width = dims.first
                mTemplate?.height = dims.second
                tv_resolution.text = when (index) {
                    0 -> "480p"
                    1 -> "720p"
                    2 -> "1080p"
                    3 -> "4K"
                    else -> "720p"
                }
            }
            override fun onStartTrackingTouch(seekBar: CustomDiscreteSeekBar) {}
            override fun onStopTrackingTouch(seekBar: CustomDiscreteSeekBar) {}
        }
    }

    private fun initLaunchers() {
        // Search Aya result
        searchAyaResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            isToCrop = false
            try {
                if (AddQuranFragment.instance != null) {
                    AddQuranFragment.instance!!.addAyaIndex()
                } else {
                    val ft = supportFragmentManager.beginTransaction()
                    mCurrentFragment = AddQuranFragment.getInstance(iAddQuran, mResources!!)
                    ft.replace(R.id.m_container, mCurrentFragment!!)
                    ft.commit()
                    setupShowFragment(mResources!!.getString(R.string.quran))
                }
            } catch (_: Exception) {}
        }

        // Name reader result
        nameReaderResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            isToCrop = false
            val data = result.data ?: return@registerForActivityResult
            try {
                if (AddQuranFragment.instance != null) {
                    val audioUri = Uri.parse(data.getStringExtra("audio"))
                    val videoPath = data.getStringExtra("path_video_copy")
                    // AddQuranFragment.instance doesn't have onAddReaderName in new API - re-launch fragment
                    val ft2 = supportFragmentManager.beginTransaction()
                    mCurrentFragment = AddQuranFragment.getInstance(iAddQuran, mResources!!, audioUri, videoPath, data.getStringExtra("name") ?: "")
                    ft2.replace(R.id.m_container, mCurrentFragment!!)
                    ft2.commit()
                } else {
                    val audioUri = Uri.parse(data.getStringExtra("audio"))
                    val videoPath = data.getStringExtra("path_video_copy")
                    val name = data.getStringExtra("name") ?: ""
                    val ft = supportFragmentManager.beginTransaction()
                    mCurrentFragment = AddQuranFragment.getInstance(iAddQuran, mResources!!, audioUri, videoPath, name)
                    ft.replace(R.id.m_container, mCurrentFragment!!)
                    ft.commit()
                    setupShowFragment(mResources!!.getString(R.string.quran))
                }
            } catch (_: Exception) {}
        }

        // Edit Surah Name result
        editSurahNameResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            isToCrop = false
            val data = result.data ?: return@registerForActivityResult
            if (result.resultCode != RESULT_OK) return@registerForActivityResult
            try {
                val text = data.getStringExtra(Common.READER) ?: return@registerForActivityResult
                val isBg = data.getBooleanExtra("isBg", false)
                val style = data.getIntExtra("style", 0)
                // SurahNameEntity is private in BlurredImageView, can't access directly
                // This would need to be handled through the fragment callback
                blurredImageView.invalidate()
            } catch (_: Exception) {}
        }

        // Edit Translation result
        editTrslResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            isToCrop = false
            val data = result.data ?: return@registerForActivityResult
            if (result.resultCode != RESULT_OK) return@registerForActivityResult
            try {
                // entity_select is private, handled through fragment
                blurredImageView.invalidate()
            } catch (_: Exception) {}
        }

        // Choice BG from video
        launchChoiceBgActivity = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            isToCrop = false
            val data = result.data ?: return@registerForActivityResult
            try {
                val uriString = data.getStringExtra("uri_video")
                if (uriString != null) {
                    handleVideo(Uri.parse(uriString))
                }
            } catch (_: Exception) {}
        }

        // Crop result
        launchCropActivity = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            isToCrop = false
            val data = result.data ?: return@registerForActivityResult
            if (result.resultCode != RESULT_OK) return@registerForActivityResult
            try {
                val uriString = data.getStringExtra("uri_crop")
                if (uriString != null) {
                    handleImg(Uri.parse(uriString))
                }
            } catch (_: Exception) {}
        }

        // Image picker
        launchImg = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            isToCrop = false
            val data = result.data ?: return@registerForActivityResult
            try {
                data.data?.let { handleImg(it) }
            } catch (_: Exception) {}
        }

        // Video picker
        launchVideo = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            isToCrop = false
            val data = result.data ?: return@registerForActivityResult
            try {
                data.data?.let { handleVideo(it) }
            } catch (_: Exception) {}
        }

        // Video extract for audio
        launchVideoExtract = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            isToCrop = false
            val data = result.data ?: return@registerForActivityResult
            try {
                data.data?.let { addAudioFromVideo(it, null) }
            } catch (_: Exception) {}
        }

        // General activity launcher (audio pick)
        activityLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val data = result.data ?: return@registerForActivityResult
            try {
                data.data?.let { addAudio(it) }
            } catch (_: Exception) {}
        }
    }

    // ── Fragment Management ────────────────────────────────────────────

    fun setupShowFragment(title: String) {
        findViewById<View>(R.id.layout_menu).visibility = View.GONE
        findViewById<View>(R.id.layout_time).visibility = View.GONE
        trackViewEntity.visibility = View.GONE
        tv_tittle_fragment.text = title
        tv_tittle_fragment.visibility = View.VISIBLE
    }

    fun setupHideFragment() {
        findViewById<View>(R.id.layout_menu).visibility = View.VISIBLE
        findViewById<View>(R.id.layout_time).visibility = View.VISIBLE
        trackViewEntity.visibility = View.VISIBLE
        tv_tittle_fragment.visibility = View.GONE
    }

    fun hideFragment() {
        try {
            if (mCurrentFragment != null && !supportFragmentManager.isDestroyed) {
                val ft = supportFragmentManager.beginTransaction()
                ft.remove(mCurrentFragment!!)
                ft.commit()
                mCurrentFragment = null
            }
            setupHideFragment()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun showEditEntity(entity: Entity) {
        val ft = supportFragmentManager.beginTransaction()
        mCurrentFragment = EditEntityFragment.getInstance(
            iEditEntityCallback, mResources!!, entity, -trackViewEntity.getCurrentPosition()
        )
        ft.replace(R.id.m_container, mCurrentFragment!!)
        ft.commit()
        setupShowFragment(mResources!!.getString(R.string.edit))
    }

    fun showEditTrslEntity(entity: Entity) {
        val ft = supportFragmentManager.beginTransaction()
        mCurrentFragment = EditTrslEntityFragment.getInstance(
            iEditTrstEntityCallback, mResources!!, entity, -trackViewEntity.getCurrentPosition()
        )
        ft.replace(R.id.m_container, mCurrentFragment!!)
        ft.commit()
        setupShowFragment(mResources!!.getString(R.string.edit))
    }

    fun showEditBismilahEntity(entity: Entity) {
        val ft = supportFragmentManager.beginTransaction()
        mCurrentFragment = EditBismilahEntityFragment.getInstance(
            iBismilahEntityCallback, mResources, entity, -trackViewEntity.getCurrentPosition()
        )
        ft.replace(R.id.m_container, mCurrentFragment!!)
        ft.commit()
        setupShowFragment(mResources!!.getString(R.string.edit))
    }

    fun showEditAudioEntity(entityAudio: EntityAudio) {
        val ft = supportFragmentManager.beginTransaction()
        mCurrentFragment = EditMediaFragment.getInstance(
            iEditMediaCallback, mResources!!, entityAudio, -trackViewEntity.getCurrentPosition()
        )
        ft.replace(R.id.m_container, mCurrentFragment!!)
        ft.commit()
        setupShowFragment(mResources!!.getString(R.string.audio))
    }

    fun showEditMultipleEntity(count: Int) {
        val ft = supportFragmentManager.beginTransaction()
        mCurrentFragment = EditMultipleEntityFragment(iEditMultipleCallback, mResources!!, count)
        ft.replace(R.id.m_container, mCurrentFragment!!)
        ft.commit()
        setupShowFragment(mResources!!.getString(R.string.edit))
    }

    fun selectSurahName() {
        // EditS_NameFragment requires SurahNameEntity which is private in BlurredImageView
        // This needs a different approach - for now skip
        try {
            hideFragment()
        } catch (e: Exception) { e.printStackTrace() }
    }

    fun showProgress() {
        try {
            findViewById<View>(R.id.container_progress).visibility = View.VISIBLE
            if (isFinishing || supportFragmentManager.isDestroyed) return
            val ft = supportFragmentManager.beginTransaction()
            ft.replace(R.id.container_progress, ProgressViewFragment.getInstance())
            ft.commit()
        } catch (_: Exception) {}
    }

    fun showProgressSimple() {
        try {
            findViewById<View>(R.id.container_progress).visibility = View.VISIBLE
            if (isFinishing || supportFragmentManager.isDestroyed) return
            val ft = supportFragmentManager.beginTransaction()
            ft.replace(R.id.container_progress, SimpleProgressViewFragment.getInstance())
            ft.commit()
        } catch (_: Exception) {}
    }

    fun hideProgressFragment() {
        try {
            if (!isFinishing && !supportFragmentManager.isDestroyed) {
                val fm = supportFragmentManager
                val ft = fm.beginTransaction()
                val fragment = fm.findFragmentById(R.id.container_progress)
                if (fragment != null) {
                    ft.remove(fragment)
                }
                ft.commit()
            }
        } catch (_: Exception) {}
        findViewById<View>(R.id.container_progress).visibility = View.GONE
    }

    // ── Timeline / Playback ────────────────────────────────────────────

    fun startTimelineAnimation() {
        try {
            val maxTime = trackViewEntity.getMaxTime()
            if (maxTime <= 0) return
            mIsPlaying = true
            trackViewEntity.setPlaying(true)
            blurredImageView.isPlaying = true
            btnPlayPause.setImageResource(R.drawable.pause_24px)

            timelineAnimatorListener = object : SmoothTimelineAnimator.AnimatorListener {
                override fun onUpdate(timeMs: Int) {
                    try {
                        startCursur = timeMs
                        trackViewEntity.updateCurrentCursurPosition(timeMs)

                        // Sync audio
                        for (audio in trackViewEntity.getEntityListAudio()) {
                            try {
                                val mp = audio.mediaPlayer ?: continue
                                val audioStart = audio.startMs
                                val audioEnd = audio.endMs
                                if (timeMs.toLong() in audioStart..audioEnd) {
                                    if (!mp.isPlaying) {
                                        val seekTo = timeMs.toLong() - audioStart
                                        if (seekTo >= 0 && seekTo < mp.duration.toLong()) {
                                            mp.seekTo(seekTo.toInt())
                                            mp.start()
                                        }
                                    }
                                } else {
                                    if (mp.isPlaying) mp.pause()
                                }
                            } catch (e: Exception) { e.printStackTrace() }
                        }

                        updateViewTime(maxTime, timeMs)
                        updateFrame()
                    } catch (e: Exception) { e.printStackTrace() }
                }

                override fun onEnd() {
                    pausePlayer()
                }
            }

            smoothTimelineAnimator = SmoothTimelineAnimator(startCursur, maxTime, timelineAnimatorListener!!)
            smoothTimelineAnimator?.start()

            // Start video frame animation if video bg
            if (mTemplate?.isVideoSquare == true) {
                startVideoAnimation()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun pauseTimelineAnimation() {
        try {
            smoothTimelineAnimator?.stop()
            smoothTimelineAnimator = null
        } catch (e: Exception) { e.printStackTrace() }
    }

    fun pausePlayer() {
        try {
            mIsPlaying = false
            trackViewEntity.setPlaying(false)
            blurredImageView.isPlaying = false
            btnPlayPause.setImageResource(R.drawable.play_arrow_24px)
            pauseTimelineAnimation()
            stopVideoAnimation()

            // Pause all audio
            for (audio in trackViewEntity.getEntityListAudio()) {
                try {
                    audio.mediaPlayer?.let { if (it.isPlaying) it.pause() }
                } catch (e: Exception) { e.printStackTrace() }
            }
        } catch (e: Exception) { e.printStackTrace() }
    }

    fun updateTime() {
        try {
            val maxTime = trackViewEntity.getMaxTime()
            val current = trackViewEntity.getCurrentCursurPosition()
            startCursur = current
            updateViewTime(maxTime, current)
            updateBtnToStart()
            updateBtnToEnd()
            updateBtnCutState()
        } catch (e: Exception) { e.printStackTrace() }
    }

    fun updateTime(timeMs: Int) {
        try {
            val maxTime = trackViewEntity.getMaxTime()
            updateViewTime(maxTime, timeMs)
            blurredImageView.postInvalidate()
        } catch (e: Exception) { e.printStackTrace() }
    }

    fun updateTimeToEndAya() {
        updateTime()
    }

    fun updateViewTime(maxTime: Int, currentTime: Int) {
        tv_currentTime.text = TimeFormatter.formatMs(currentTime.toLong())
        tv_endTime.text = ":${TimeFormatter.formatMs(maxTime.toLong())}"
    }

    fun updateStartViewTime(currentTime: Int) {
        tv_currentTime.text = TimeFormatter.formatMs(currentTime.toLong())
    }

    fun updateEndViewTime(maxTime: Int) {
        tv_endTime.text = ":${TimeFormatter.formatMs(maxTime.toLong())}"
    }

    fun updateBtnToStart() {
        btnToStart.isEnabled = startCursur > 0
        btnToStart.imageAlpha = if (startCursur > 0) 255 else 100
    }

    fun updateBtnToEnd() {
        val maxTime = trackViewEntity.getMaxTime()
        btnToEnd.isEnabled = startCursur < maxTime
        btnToEnd.imageAlpha = if (startCursur < maxTime) 255 else 100
    }

    fun updateBtnCutState() {
        // Check if split is possible at current cursor position
    }

    fun enableUndoBtn() {
        btnUndo.imageAlpha = 255
        btnUndo.isEnabled = true
    }

    fun disableUndoBtn() {
        btnUndo.imageAlpha = 100
        btnUndo.isEnabled = false
    }

    fun enableRedoBtn() {
        btnRedo.imageAlpha = 255
        btnRedo.isEnabled = true
    }

    fun disableRedoBtn() {
        btnRedo.imageAlpha = 100
        btnRedo.isEnabled = false
    }

    // ── Video Frame Animation ──────────────────────────────────────────

    fun startVideoAnimation() {
        try {
            val template = mTemplate ?: return
            smoothVideoAnimator = SmoothVideoAnimator(
                trackViewEntity, template, FPS,
                object : SmoothVideoAnimator.FrameUpdateListener {
                    override fun onFrameUpdate(framePath: String) {
                        synchronized(frameLock) {
                            pendingFramePath = framePath
                        }
                        mainHandler.post(frameProcessorRunnable)
                    }
                    override fun onAnimationEnd() {}
                }
            )
            smoothVideoAnimator?.start()
        } catch (e: Exception) { e.printStackTrace() }
    }

    fun stopVideoAnimation() {
        try {
            smoothVideoAnimator?.stop()
            smoothVideoAnimator = null
        } catch (e: Exception) { e.printStackTrace() }
    }

    fun updateFrame() {
        if (mTemplate?.isVideoSquare != true) return
        try {
            val frameIndex = (startCursur / 1000f * FPS).toInt()
            val frameFile = File(cacheDir, "frames/frame_${String.format("%04d", frameIndex)}.jpg")
            if (frameFile.exists()) {
                processFrame(frameFile.absolutePath)
            }
        } catch (e: Exception) { e.printStackTrace() }
    }

    private fun processFrame(framePath: String) {
        try {
            val bmp = Glide.with(this)
                .asBitmap()
                .load(File(framePath))
                .override(blurredImageView.width, blurredImageView.height)
                .submit()
                .get()
            blurredImageView.setBitmapSquare(bmp)
        } catch (e: Exception) { e.printStackTrace() }
    }

    private val frameProcessorRunnable = Runnable {
        try {
            var path: String? = null
            synchronized(frameLock) {
                path = pendingFramePath
                pendingFramePath = null
            }
            if (path != null) {
                processFrame(path!!)
            }
        } catch (e: Exception) { e.printStackTrace() }
    }

    // ── Entity Management ──────────────────────────────────────────────

    fun addEntity(
        ayaText: String, surahInfo: String, completeAya: String,
        translation: String, surahNum: Int, ayaNum: Int,
        fontName: String, iconIndex: Int, colorIndex: Int
    ) {
        try {
            val quranEntity = QuranEntity()
            quranEntity.aya = ayaText
            quranEntity.completeAya = completeAya
            quranEntity.translation = translation
            quranEntity.fontName = fontName.ifBlank { "خط فارس الكوفي.otf" }
            quranEntity.icon = iconIndex.toString()
            quranEntity.textColor = colorIndex
            quranEntity.entityTransition = Transition()

            val entityTimeline = EntityQuranTimeline().apply {
                ayaNumber = ayaNum
                surahNumber = surahNum
                surahName = surahInfo
                this.ayaText = ayaText
                completeAyaText = completeAya
                val widthMs = (ayaText.length * 150L).coerceIn(3000L, 30000L)
                startMs = startCursur.toLong()
                endMs = startCursur.toLong() + widthMs
            }
            entityTimeline.quranEntity = quranEntity

            trackViewEntity.addQuran(entityTimeline)
            blurredImageView.addEntity(quranEntity)
            enableUndoBtn()
            updateTime()
            trackViewEntity.translateToRight()
        } catch (e: Exception) { e.printStackTrace() }
    }

    fun addEntityFromTemplateQuran(tmpl: EntityQuranTemplate) {
        try {
            val quranEntity = QuranEntity().apply {
                aya = tmpl.aya ?: ""
                completeAya = tmpl.complete_aya ?: ""
                fontName = tmpl.name_font ?: "خط فارس الكوفي.otf"
                textColor = tmpl.color
                entityIndex = tmpl.number
                icon = tmpl.icon
                x = tmpl.x
                y = tmpl.y
                scale = tmpl.scale
                factorSize = tmpl.factor_size
                entityTransition = tmpl.transition ?: Transition()
            }

            val entityTimeline = EntityQuranTimeline().apply {
                this.quranEntity = quranEntity
                startMs = tmpl.start.toLong()
                endMs = tmpl.end.toLong()
            }

            trackViewEntity.addQuran(entityTimeline)
            blurredImageView.addEntity(quranEntity)
        } catch (e: Exception) { e.printStackTrace() }
    }

    fun addTranslationEntity(text: String, index: Int, isAuto: Boolean) {
        try {
            val trslEntity = TranslationQuranEntity().apply {
                this.text = text
                fontName = "ReadexPro_Regular.ttf"
                entityTransition = Transition()
                entityIndex = index
            }

            val entityTimeline = EntityTrslTimeline().apply {
                translationText = text
                val widthMs = (text.length * 80L).coerceIn(2000L, 20000L)
                startMs = startCursur.toLong()
                endMs = startCursur.toLong() + widthMs
            }

            trackViewEntity.addTrslQuran(entityTimeline)
            blurredImageView.addEntity(trslEntity)
            enableUndoBtn()
            updateTime()
        } catch (e: Exception) { e.printStackTrace() }
    }

    fun addEntityTrslFromTemplate(tmpl: EntityTranslationTemplate) {
        try {
            val trslEntity = TranslationQuranEntity().apply {
                text = tmpl.aya ?: ""
                fontName = tmpl.name_font ?: "ReadexPro_Regular.ttf"
                textColor = tmpl.color
                entityTransition = tmpl.transition ?: Transition()
            }

            val entityTimeline = EntityTrslTimeline().apply {
                translationText = tmpl.aya ?: ""
                startMs = tmpl.start.toLong()
                endMs = tmpl.end.toLong()
            }

            trackViewEntity.addTrslQuran(entityTimeline)
            blurredImageView.addEntity(trslEntity)
        } catch (e: Exception) { e.printStackTrace() }
    }

    fun addEntityBissmilah(): Boolean {
        try {
            val bismilahEntity = BismilahEntity().apply {
                text = "بسم الله الرحمن الرحيم"
                fontName = "خط الإبل.otf"
                entityTransition = Transition()
            }

            val entityTimeline = EntityBismilahTimeline().apply {
                bismilahText = "بسم الله الرحمن الرحيم"
                startMs = startCursur.toLong()
                endMs = startCursur.toLong() + 3000
            }

            trackViewEntity.setBismilahTimeline(entityTimeline)
            blurredImageView.setBismilahEntity(bismilahEntity)
            enableUndoBtn()
            return true
        } catch (e: Exception) { e.printStackTrace(); return false }
    }

    fun addEntityIste3adha(): Boolean {
        try {
            val isti3adaEntity = BismilahEntity().apply {
                text = "أعوذ بالله من الشيطان الرجيم"
                fontName = "خط الإبل.otf"
                entityTransition = Transition()
            }

            val entityTimeline = EntityBismilahTimeline().apply {
                bismilahText = "أعوذ بالله من الشيطان الرجيم"
                startMs = startCursur.toLong()
                endMs = startCursur.toLong() + 3000
            }

            trackViewEntity.setmIsi3adaTimeline(entityTimeline)
            blurredImageView.setIsti3adhaEntity(isti3adaEntity)
            enableUndoBtn()
            return true
        } catch (e: Exception) { e.printStackTrace(); return false }
    }

    fun addEntityBismilahFromTemplate(tmpl: EntityBismilahTemplate) {
        try {
            val bismilahEntity = BismilahEntity().apply {
                text = tmpl.aya ?: "بسم الله الرحمن الرحيم"
                fontName = "خط الإبل.otf"
                textColor = tmpl.color
                entityTransition = tmpl.transition ?: Transition()
            }

            val entityTimeline = EntityBismilahTimeline().apply {
                bismilahText = tmpl.aya ?: "بسم الله الرحمن الرحيم"
                startMs = tmpl.start.toLong()
                endMs = tmpl.end.toLong()
            }

            trackViewEntity.setBismilahTimeline(entityTimeline)
            blurredImageView.setBismilahEntity(bismilahEntity)
        } catch (e: Exception) { e.printStackTrace() }
    }

    fun addEntityIsti3adaFromTemplate(tmpl: EntityBismilahTemplate) {
        try {
            val isti3adaEntity = BismilahEntity().apply {
                text = tmpl.aya ?: "أعوذ بالله من الشيطان الرجيم"
                fontName = "خط الإبل.otf"
                textColor = tmpl.color
                entityTransition = tmpl.transition ?: Transition()
            }

            val entityTimeline = EntityBismilahTimeline().apply {
                bismilahText = tmpl.aya ?: "أعوذ بالله من الشيطان الرجيم"
                startMs = tmpl.start.toLong()
                endMs = tmpl.end.toLong()
            }

            trackViewEntity.setmIsi3adaTimeline(entityTimeline)
            blurredImageView.setIsti3adhaEntity(isti3adaEntity)
        } catch (e: Exception) { e.printStackTrace() }
    }

    // ── Audio Management ───────────────────────────────────────────────

    fun addAudio(uri: Uri) {
        try {
            showProgress()
            val mp = MediaPlayer.create(this, uri) ?: run { hideProgressFragment(); return }
            val entityAudio = EntityAudio().apply {
                filePath = uri.toString()
                mediaPlayer = mp
                startMs = startCursur.toLong()
                endMs = startCursur.toLong() + mp.duration.toLong()
            }
            trackViewEntity.addAudio(entityAudio)
            enableUndoBtn()
            updateTime()
            hideProgressFragment()
        } catch (e: Exception) {
            e.printStackTrace()
            hideProgressFragment()
        }
    }

    fun addAudioFromVideo(uri: Uri, videoPath: String?) {
        try {
            showProgress()
            // Extract audio from video using FFmpeg
            val outputFile = File(cacheDir, "audio_${System.currentTimeMillis()}.mp3")
            val cmd = "-y -i \"$uri\" -vn -acodec libmp3lame -q:a 2 \"${outputFile.absolutePath}\""

            FFmpegKit.executeAsync(cmd, { session ->
                if (ReturnCode.isSuccess(session.returnCode)) {
                    mainHandler.post {
                        addAudio(Uri.fromFile(outputFile))
                    }
                } else {
                    mainHandler.post {
                        hideProgressFragment()
                        Toast.makeText(this, "Failed to extract audio", Toast.LENGTH_SHORT).show()
                    }
                }
            })
        } catch (e: Exception) {
            e.printStackTrace()
            hideProgressFragment()
        }
    }

    fun addAudioFromTemplate(entityMedia: EntityMedia) {
        try {
            val uri = entityMedia.uri ?: return
            val mp = MediaPlayer.create(this, Uri.parse(uri)) ?: return
            val entityAudio = EntityAudio().apply {
                filePath = uri
                mediaPlayer = mp
                startMs = entityMedia.start.toLong()
                endMs = entityMedia.end.toLong()
            }
            trackViewEntity.addAudio(entityAudio)
        } catch (e: Exception) { e.printStackTrace() }
    }

    fun addAudioReciters(reciters: List<RecitersModel>) {
        try {
            showProgress()
            // AudioUtils doesn't have copyRecitersAudio - simplified version
            hideProgressFragment()
            Toast.makeText(this, "Audio download not yet implemented", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            hideProgressFragment()
        }
    }

    // ── Background Management ──────────────────────────────────────────

    fun iniTypeImg() {
        try {
            val template = mTemplate ?: return
            val bgUri = uri_bg

            if (bgUri == null || bgUri == "default" || bgUri.isEmpty()) {
                // No background - use default color
                addEntityFromTemplate()
                hideProgressFragment()
                return
            }

            executor.execute {
                try {
                    val bitmap = Glide.with(this@EngineActivity)
                        .asBitmap()
                        .load(Uri.parse(bgUri))
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .submit(template.width, template.height)
                        .get()

                    mainHandler.post {
                        try {
                            blurredImageView.setBitmap(bitmap, null, template.color_ipad, template.ipad_type, template.resizeType, null)
                            addEntityFromTemplate()
                            hideProgressFragment()
                        } catch (e: Exception) { e.printStackTrace() }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    mainHandler.post {
                        addEntityFromTemplate()
                        hideProgressFragment()
                    }
                }
            }
        } catch (e: Exception) { e.printStackTrace() }
    }

    fun initTypeVideo() {
        try {
            val template = mTemplate ?: return
            val bgUri = uri_bg ?: return

            executor.execute {
                try {
                    // Extract frames from video
                    val framesDir = File(cacheDir, "frames")
                    framesDir.mkdirs()
                    framesDir.listFiles()?.forEach { it.delete() }

                    val cmd = "-y -i \"$bgUri\" -vf fps=$FPS -q:v 2 \"${framesDir.absolutePath}/frame_%04d.jpg\""
                    FFmpegKit.executeAsync(cmd, { session ->
                        if (ReturnCode.isSuccess(session.returnCode)) {
                            mainHandler.post {
                                try {
                                    val firstFrame = File(framesDir, "frame_0001.jpg")
                                    if (firstFrame.exists()) {
                                        val bitmap = Glide.with(this@EngineActivity)
                                            .asBitmap()
                                            .load(firstFrame)
                                            .submit(template.width, template.height)
                                            .get()
                                        blurredImageView.setBitmap(bitmap, null, template.color_ipad, template.ipad_type, template.resizeType, null)
                                    }
                                    addEntityFromTemplate()
                                    hideProgressFragment()
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    hideProgressFragment()
                                }
                            }
                        } else {
                            mainHandler.post {
                                iniTypeImg() // Fallback to image
                            }
                        }
                    })
                } catch (e: Exception) {
                    e.printStackTrace()
                    mainHandler.post {
                        iniTypeImg()
                    }
                }
            }
        } catch (e: Exception) { e.printStackTrace() }
    }

    fun handleImg(uri: Uri) {
        try {
            showProgress()
            mTemplate?.uri_bg = uri.toString()
            uri_bg = uri.toString()
            mTemplate?.isVideoSquare = false

            executor.execute {
                try {
                    val bitmap = Glide.with(this@EngineActivity)
                        .asBitmap()
                        .load(uri)
                        .submit(mTemplate?.width ?: 720, mTemplate?.height ?: 1280)
                        .get()

                    mainHandler.post {
                        blurredImageView.setBitmap(bitmap, null, mTemplate?.color_ipad ?: -1, mTemplate?.ipad_type ?: IpadType.IPAD.ordinal, mTemplate?.resizeType ?: ResizeType.SOCIAL_STORY.ordinal, null)
                        hideProgressFragment()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    mainHandler.post { hideProgressFragment() }
                }
            }
        } catch (e: Exception) { e.printStackTrace() }
    }

    fun handleVideo(uri: Uri) {
        try {
            showProgress()
            mTemplate?.uri_bg = uri.toString()
            uri_bg = uri.toString()
            mTemplate?.isVideoSquare = true
            initTypeVideo()
        } catch (e: Exception) { e.printStackTrace() }
    }

    // ── Export ─────────────────────────────────────────────────────────

    fun save() {
        try {
            oneExport = true
            trackViewEntity.finishScroll()

            blurredImageView.isRemoveWattermark = BillingPreferences.isSubscribed(this)
            stopVideoAnimation()

            executor.execute {
                try {
                    val template = mTemplate ?: return@execute
                    val maxTime = trackViewEntity.getMaxTime()

                    // Save template for rendering
                    saveTemplate()

                    mainHandler.post {
                        try {
                            // Launch progress activity for FFmpeg rendering
                            val intent = Intent(this@EngineActivity, ProgressViewActivity::class.java)
                            intent.putExtra(Common.TEMPLATE, template.idTemplate)
                            intent.putExtra("maxTime", maxTime)
                            startActivity(intent)
                            overridePendingTransition(0, 0)
                            finish()
                        } catch (e: Exception) { e.printStackTrace() }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    mainHandler.post { oneExport = false }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            oneExport = false
        }
    }

    // ── Dialogs ────────────────────────────────────────────────────────

    fun dialog() {
        try {
            val dlg = Dialog(this)
            dialog = dlg
            dlg.setCancelable(true)
            dlg.requestWindowFeature(1)
            dlg.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            dlg.window?.setBackgroundDrawable(ColorDrawable(0))

            val inflate = LayoutInflater.from(this).inflate(R.layout.layout_dialog, null)
            dlg.setContentView(inflate)

            val titleView = inflate.findViewById<TextCustumFontBold>(R.id.dialog_title)
            if (LocaleHelper.getLanguage(this) == "ar") {
                titleView.text = "هل تريد المغادرة؟"
            } else {
                titleView.text = "Do you want to leave?"
            }

            inflate.findViewById<TextCustumFont>(R.id.dialog_message).text = ""

            val btnLeave = inflate.findViewById<ButtonCustumFont>(R.id.dialog_no)
            btnLeave.text = mResources?.getString(R.string.leave) ?: "Leave"
            btnLeave.setOnClickListener {
                isSaveTmpTemplate = false
                finish()
            }

            val btnContinue = inflate.findViewById<ButtonCustumFont>(R.id.dialog_yes)
            btnContinue.text = "Continue"
            btnContinue.setOnClickListener { cancelDialog() }

            dlg.show()
        } catch (e: Exception) { e.printStackTrace() }
    }

    fun dialogPremium(type: Int) {
        try {
            val dlg = Dialog(this)
            dialog = dlg
            dlg.setCancelable(true)
            dlg.requestWindowFeature(1)
            dlg.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            dlg.window?.setBackgroundDrawable(ColorDrawable(0))

            val inflate = LayoutInflater.from(this).inflate(R.layout.layout_dialog_premuim, null)
            dlg.setContentView(inflate)
            inflate.findViewById<View>(R.id.dialog_title).visibility = View.GONE
            inflate.findViewById<View>(R.id.dialog_no).setOnClickListener { cancelDialog() }
            dlg.show()
        } catch (e: Exception) { e.printStackTrace() }
    }

    fun dialogCopyRight() {
        try {
            val dlg = Dialog(this)
            dialog = dlg
            dlg.setCancelable(true)
            dlg.requestWindowFeature(1)
            dlg.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            dlg.window?.setBackgroundDrawable(ColorDrawable(0))

            val inflate = LayoutInflater.from(this).inflate(R.layout.layout_dialog_copyright, null)
            dlg.setContentView(inflate)
            inflate.findViewById<View>(R.id.dialog_no).setOnClickListener { cancelDialog() }

            val dialogTitle = inflate.findViewById<TextCustumFontBold>(R.id.dialog_title)
            val tvMsj = inflate.findViewById<TextCustumFont>(R.id.tv_msj)
            if (LocaleHelper.getLanguage(this) == "ar") {
                dialogTitle.text = "تنبيه حقوق الاستخدام ⚠️"
                tvMsj.text = "بعض تسجيلات تلاوات القرّاء محمية بحقوق النشر، وهي مخصّصة للاستخدام الشخصي فقط."
            } else {
                dialogTitle.text = "⚠️ Copyright Notice"
                tvMsj.text = "Some reciters' audio recordings are protected by copyright and are intended for personal use only."
            }
            dlg.show()
        } catch (e: Exception) { e.printStackTrace() }
    }

    private fun cancelDialog() {
        dialog?.dismiss()
        dialog = null
    }

    // ── Permissions & Pickers ──────────────────────────────────────────

    fun pickVideoForAudio() {
        try {
            val intent = Intent(this, GalleryPickerVideo::class.java)
            launchVideoExtract.launch(intent)
        } catch (e: Exception) { e.printStackTrace() }
    }

    fun pickVideoFromGallery() {
        try {
            val intent = Intent(this, GalleryPickerVideo::class.java)
            launchVideo.launch(intent)
        } catch (e: Exception) { e.printStackTrace() }
    }

    fun pickImageFromGallery() {
        try {
            val intent = Intent(this, GalleryPickerOneImage::class.java)
            launchImg.launch(intent)
        } catch (e: Exception) { e.printStackTrace() }
    }

    fun checkPermissionAudio(): Boolean {
        return true // Permissions handled at install time on modern Android
    }

    fun pickAudio() {
        try {
            val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                type = "audio/*"
                addCategory(Intent.CATEGORY_OPENABLE)
            }
            activityLauncher.launch(intent)
        } catch (e: Exception) { e.printStackTrace() }
    }

    // ── Utility ────────────────────────────────────────────────────────

    fun updateHitRatio(resizeType: Int, text: String) {
        textChangeResize.text = text
        ivResize.setImageResource(when (resizeType) {
            ResizeType.YOUTUBE_16_9.ordinal -> R.drawable.ic_youtube
            ResizeType.SOCIAL_STORY.ordinal -> R.drawable.ic_youtube
            ResizeType.SQUARE.ordinal -> R.drawable.ic_youtube
            else -> R.drawable.ic_youtube
        })
    }

    fun toProVersion() {
        startActivity(Intent(this, ProVersionActivity::class.java))
        overridePendingTransition(0, 0)
    }

    fun toChoiceBgFromVideo(uri: Uri) {
        val intent = Intent(this, ChoiceBgFromVideoActivity::class.java)
        intent.putExtra("uri_video", uri.toString())
        launchChoiceBgActivity.launch(intent)
    }

    private fun checkUriShared() {
        try {
            val typeShare = intent.getStringExtra("type_share")
            val pathShare = intent.getStringExtra("path_share")
            if (typeShare != null && pathShare != null) {
                when (typeShare) {
                    "audio" -> addAudio(Uri.parse(pathShare))
                    "image" -> handleImg(Uri.parse(pathShare))
                    "video" -> handleVideo(Uri.parse(pathShare))
                }
            }
        } catch (e: Exception) { e.printStackTrace() }
    }

    private fun clearFFmpeg() {
        try {
            for (id in id_ffmpeg) {
                FFmpegKit.cancel(id)
            }
            id_ffmpeg.clear()
        } catch (e: Exception) { e.printStackTrace() }
    }

    private fun clearCallback() {
        // Clear callback references to prevent memory leaks
    }

    // ── Callback: ITrimLineCallback ────────────────────────────────────

    private val iTrimLineCallback = object : TrackEntityView.ITrimLineCallback {
        override fun fadeInAudio(v: Float) {}
        override fun fadeOutAudio(v: Float) {}
        override fun onMove() {}
        override fun onUpdatePlayerAudio(audio: EntityAudio) {}
        override fun onSelectMultiple(count: Int) { showEditMultipleEntity(count) }

        override fun onDelete(entityView: hazem.nurmontage.videoquran.model.EntityView) {
            try {
                blurredImageView.setEntity_select(null)
                blurredImageView.postInvalidate()
                hideFragment()
            } catch (e: Exception) { e.printStackTrace() }
        }

        override fun onEmptySelect() {
            blurredImageView.setEntity_select(null)
            blurredImageView.postInvalidate()
            pausePlayer()
            hideFragment()
        }

        override fun onUpdate() {
            blurredImageView.postInvalidate()
        }

        override fun onUp() {
            isOnScroll = false
            updateBtnCutState()
        }

        override fun onAddStack(entityAction: EntityAction) {
            enableUndoBtn()
        }

        override fun onSeekPlayer(f: Float) {
            try {
                isOnScroll = true
                for (audio in trackViewEntity.getEntityListAudio()) {
                    try {
                        audio.mediaPlayer?.let { if (it.isPlaying) it.pause() }
                    } catch (e: Exception) { e.printStackTrace() }
                }
                if (mIsPlaying) {
                    btnPlayPause.setImageResource(R.drawable.play_arrow_24px)
                    mIsPlaying = false
                    trackViewEntity.setPlaying(false)
                    blurredImageView.isPlaying = false
                }
                pauseTimelineAnimation()
                stopVideoAnimation()

                val secInScreen = trackViewEntity.getSecondInScreen()
                val maxTime = trackViewEntity.getMaxTime()
                if (secInScreen != 0f) {
                    val timeMs = Math.round(Math.abs(f / secInScreen * -1000f))
                    if (timeMs <= maxTime) {
                        updateTime(timeMs)
                    }
                }
                trackViewEntity.updateCurrentCursurPosition(trackViewEntity.getCurrentCursurPosition())
                current_position_time = System.currentTimeMillis().toInt()
                startCursur = trackViewEntity.getCurrentCursurPosition()
                updateViewTime(trackViewEntity.getMaxTime(), trackViewEntity.getCurrentCursurPosition())
                updateBtnCutState()
                updateBtnToStart()
                updateBtnToEnd()
                updateFrame()
            } catch (_: Exception) {}
        }

        override fun pause() { pausePlayer() }

        override fun onPlayVibration() {
            pausePlayer()
            runOnUiThread { MyVibrationHelper.vibrate(this@EngineActivity, 50) }
        }

        override fun onSelectEntity(entity: Entity, f: Float) {
            pausePlayer()
            when (entity) {
                is EntityQuranTimeline -> {
                    blurredImageView.setEntity_select(entity.quranEntity)
                    blurredImageView.invalidate()
                    if (EditEntityFragment.instance != null) {
                        // EditEntityFragment.instance methods - just show edit
                        showEditEntity(entity)
                    } else {
                        showEditEntity(entity)
                    }
                }
                is EntityTrslTimeline -> {
                    // Can't set entity_select since TrslTimeline doesn't expose TranslationQuranEntity
                    blurredImageView.invalidate()
                    showEditTrslEntity(entity)
                }
                is EntityBismilahTimeline -> {
                    blurredImageView.setEntity_select(entity.quranEntity)
                    blurredImageView.invalidate()
                    showEditBismilahEntity(entity)
                }
                is EntityAudio -> {
                    if (EditMediaFragment.instance != null) {
                        // EditMediaFragment.instance methods - just show edit
                        showEditAudioEntity(entity)
                    } else {
                        showEditAudioEntity(entity)
                    }
                }
            }
        }

        override fun enableRedo(enable: Boolean) { if (enable) enableRedoBtn() else disableRedoBtn() }
        override fun enableUndo(enable: Boolean) { if (enable) enableUndoBtn() else disableUndoBtn() }

        override fun progress(isProgress: Boolean) {
            runOnUiThread { if (isProgress) showProgress() else hideProgressFragment() }
        }

        override fun onUpdateTime() {
            startCursur = trackViewEntity.getCurrentCursurPosition()
            updateTime()
        }
    }

    // ── Callback: IAddQuran ────────────────────────────────────────────

    private val iAddQuran = object : AddQuranFragment.IAddQuran {
        override fun onVuCopyRight() { dialogCopyRight() }
        override fun progress() { runOnUiThread { showProgress() } }

        override fun onSearch() {
            isToCrop = true
            searchAyaResult.launch(Intent(this@EngineActivity, QuranSearchActivity::class.java))
        }

        override fun onAddReaderName(readerName: String, pathVideoCopy: String, uri: Uri) {
            isToCrop = true
            val intent = Intent(this@EngineActivity, AddReaderNameActivity::class.java)
            intent.putExtra("name", readerName)
            intent.putExtra("audio", uri.toString())
            intent.putExtra("path_video_copy", pathVideoCopy)
            nameReaderResult.launch(intent)
        }

        override fun onAddTranslation(translation: String, ayaNumber: Int, isEnglish: Boolean) {
            addTranslationEntity(translation, ayaNumber, isEnglish)
        }

        override fun onAdd(
            text: String, completeAya: String, translationWords: String?,
            translationComplete: String?, textLength: Int, ayaNumber: Int,
            icon: String, startWordIndex: Int, endWordIndex: Int
        ) {
            addEntity(text, "", completeAya, translationWords ?: "", 0, ayaNumber, "", icon.toIntOrNull() ?: 0, 0)
        }

        override fun onDone(
            surahHint: String, surahPosition: Int, readerName: String,
            uri: Uri, pathVideoCopy: String
        ) {
            runOnUiThread { hideFragment() }
            blurredImageView.updateSizeAya()
            blurredImageView.updateSizeTrslAya()
            if (pathVideoCopy.isEmpty()) {
                addAudio(uri)
            } else {
                addAudioFromVideo(uri, pathVideoCopy)
            }
        }

        override fun onDone(
            surahHint: String, surahPosition: Int, readerName: String,
            recitersModels: List<RecitersModel>
        ) {
            runOnUiThread { hideFragment() }
            blurredImageView.updateSizeAya()
            blurredImageView.updateSizeTrslAya()
            if (NetworkUtils.isNetworkAvailable(this@EngineActivity) && recitersModels.isNotEmpty()) {
                addAudioReciters(recitersModels)
            } else {
                runOnUiThread {
                    updateTimeToEndAya()
                    updateBtnToEnd()
                    updateBtnToStart()
                    hideProgressFragment()
                }
            }
        }

        override fun onBismilah() {
            val isti3ada = addEntityIste3adha()
            val bismilah = addEntityBissmilah()
            trackViewEntity.translateToRight()
        }

        override fun onCancel() { hideFragment() }

        override fun onErrorLimitation() {
            runOnUiThread {
                Toast.makeText(this@EngineActivity, mResources?.getString(R.string.error_limit) ?: "Limit reached", Toast.LENGTH_SHORT).show()
            }
        }

        override fun uploadRecitation() {
            try {
                val ft = supportFragmentManager.beginTransaction()
                mCurrentFragment = AddAudioFragment.getInstance(iAudioCallback, mResources!!)
                ft.replace(R.id.m_container, mCurrentFragment!!)
                ft.commit()
                setupShowFragment(mResources!!.getString(R.string.audio))
            } catch (_: Exception) {}
        }
    }

    // ── Callback: IAudioCallback ───────────────────────────────────────

    private val iAudioCallback = object : AddAudioFragment.IAudioCallback {
        override fun upload() {
            if (checkPermissionAudio()) pickAudio()
        }
        override fun extract() { pickVideoForAudio() }
        override fun cancel() {
            hideFragment()
            try {
                setupShowFragment(mResources!!.getString(R.string.quran))
                val ft = supportFragmentManager.beginTransaction()
                val frag = AddQuranFragment()
                mCurrentFragment = frag
                ft.replace(R.id.m_container, frag)
                ft.commit()
            } catch (_: Exception) {}
        }
    }

    // ── Callback: IIpadEditCallback ────────────────────────────────────

    private val iIpadEditCallback = object : EditIpadFragment.IIpadEditCallback {
        override fun onClick(index: Int, color: Int) {
            mTemplate?.color_ipad = color
            mTemplate?.index_color = index
            mTemplate?.gradient = null
            blurredImageView.setColorIpad(color)
            blurredImageView.invalidate()
        }

        override fun onClick(gradient: Gradient, index: Int) {
            mTemplate?.gradient = gradient
            mTemplate?.index_color = index
            blurredImageView.setColorIpad(gradient)
            blurredImageView.invalidate()
        }

        override fun onDialogPremium() { dialogPremium(0) }

        override fun onGlassType(isGlass: Boolean) {
            mTemplate?.isGlass = isGlass
            blurredImageView.invalidate()
        }

        override fun onChangeType(type: Int) {
            mTemplate?.ipad_type = type
            blurredImageView.invalidate()
        }

        override fun onCancel() { hideFragment() }
        override fun onDone() { hideFragment() }
    }

    // ── Callback: IChangeBgCallback ────────────────────────────────────

    private val iChangeBgCallback = object : ChangeBgFragment.IChangeBgCallback {
        override fun onAdd(bgItem: BgItem) {
            try {
                mTemplate?.uri_bg = bgItem.image
                uri_bg = bgItem.image
                mTemplate?.isVideoSquare = false
                iniTypeImg()
            } catch (e: Exception) { e.printStackTrace() }
        }

        override fun onUploadImg() { pickImageFromGallery() }
        override fun onUploadVideo() { pickVideoFromGallery() }
        override fun onCrop() {
            isToCrop = true
            toCrop()
        }
        override fun onSubscribe() { dialogPremium(0) }
        override fun onCancel() { hideFragment() }
        override fun onDone() { hideFragment() }
    }

    // ── Callback: IDimensionCallback ───────────────────────────────────

    private val iDimensionCallback = object : DimensionAdabters.IDimensionCallback {
        override fun done() {
            hideFragment()
        }

        override fun isCustomSize(isCustom: Boolean, resizeType: ResizeType) {}

        override fun onCustumSize(w: Int, h: Int, resizeType: Int, id: String, image: Int) {
            try {
                mTemplate?.resizeType = resizeType
                mTemplate?.width = w
                mTemplate?.height = h

                val text = when (resizeType) {
                    ResizeType.YOUTUBE_16_9.ordinal -> "16:9"
                    ResizeType.SOCIAL_STORY.ordinal -> "9:16"
                    ResizeType.SQUARE.ordinal -> "1:1"
                    else -> "9:16"
                }
                updateHitRatio(resizeType, text)

                // Re-setup background at new dimensions
                if (mTemplate?.isVideoSquare == true) {
                    initTypeVideo()
                } else {
                    iniTypeImg()
                }
                hideFragment()
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    // ── Callback: IEditMediaCallback ───────────────────────────────────

    private val iEditMediaCallback = object : EditMediaFragment.IEditMediaCallback {
        override fun echoEffect() {}
        override fun enhanceVoice() {}
        override fun fadeffect() {}
        override fun noice() {}
        override fun onCmd(cmd: String) {}
        override fun onCmdAll(effectAudio: EffectAudio) {}
        override fun onCmdPlay(cmd: String) {}
        override fun onCut() { updateTime() }
        override fun onDelete() { hideFragment(); updateTime() }
        override fun onDone() { hideFragment() }
        override fun onDuplicate() { updateTime() }
        override fun onReplace() {
            if (checkPermissionAudio()) pickAudio()
        }
        override fun pausePreview() {}
        override fun pitchffect() {}
        override fun reverbEffect() {}
        override fun speedffect() {}
        override fun startPreview() {}
        override fun updateEntity(effectAudioType: EffectAudioType, entityAudio: EntityAudio) {}
        override fun volumeEffect() {}
    }

    // ── Callback: IEditEntityCallback ──────────────────────────────────

    private val iEditEntityCallback = object : EditEntityFragment.IEditEntityCallback {
        override fun onFont() {
            val ft = supportFragmentManager.beginTransaction()
            mCurrentFragment = FontFragment.getInstance(iFontCallback, "", Typeface.DEFAULT)
            ft.replace(R.id.m_container, mCurrentFragment!!)
            ft.commit()
        }
        override fun onIcon() {
            val ft = supportFragmentManager.beginTransaction()
            mCurrentFragment = EditIconQuranFragment.getInstance(iQuranIconCallback, "hafes")
            ft.commit()
        }
        override fun onColor() {
            val ft = supportFragmentManager.beginTransaction()
            // ColorAyaFragment requires entity - use default
            mCurrentFragment = ColorAyaFragment()
            ft.replace(R.id.m_container, mCurrentFragment!!)
            ft.commit()
        }
        override fun onAnim() {}
        override fun onEdit() {}
        override fun onCut() { updateTime() }
        override fun onDuplicate() { updateTime() }
        override fun onDelete() {
            blurredImageView.setEntity_select(null)
            blurredImageView.invalidate()
            hideFragment()
            updateTime()
        }
        override fun onDone() { hideFragment() }
        override fun fromNow() { trackViewEntity.translateFromNow() }
        override fun fromTheStart() { trackViewEntity.translateFromStart() }
        override fun untilNow() { trackViewEntity.translateUntilNow() }
        override fun untilTheEnd() { trackViewEntity.translateToRight() }
        override fun updateAya(i: Int) {}
        override fun updatePreset(ayaTextPreset: AyaTextPreset) {}
        override fun updateTrsl(i: Int) {}
    }

    // ── Callback: IEditTrslEntityCallback ───────────────────────────────

    private val iEditTrstEntityCallback = object : EditTrslEntityFragment.IEditEntityCallback {
        override fun onFont() {
            val ft = supportFragmentManager.beginTransaction()
            mCurrentFragment = FontFragment.getInstance(iFontCallback, "", Typeface.DEFAULT)
            ft.replace(R.id.m_container, mCurrentFragment!!)
            ft.commit()
        }
        override fun onIcon() {}
        override fun onColor() {
            val ft = supportFragmentManager.beginTransaction()
            mCurrentFragment = ColorTrslAyaFragment()
            ft.replace(R.id.m_container, mCurrentFragment!!)
            ft.commit()
        }
        override fun onAnim() {}
        override fun onEdit() {}
        override fun onCut() { updateTime() }
        override fun onDuplicate() { updateTime() }
        override fun onDelete() {
            blurredImageView.setEntity_select(null)
            blurredImageView.invalidate()
            hideFragment()
            updateTime()
        }
        override fun onDone() { hideFragment() }
        override fun fromNow() { trackViewEntity.translateFromNow() }
        override fun fromTheStart() { trackViewEntity.translateFromStart() }
        override fun untilNow() { trackViewEntity.translateUntilNow() }
        override fun untilTheEnd() { trackViewEntity.translateToRight() }
        override fun updateAya(i: Int) {}
        override fun updatePreset(ayaTextPreset: AyaTextPreset) {}
        override fun updateTrsl(i: Int) {}
    }

    // ── Callback: IBismilahEntityCallback ───────────────────────────────

    private val iBismilahEntityCallback = object : EditBismilahEntityFragment.IBismilahEntityCallback {
        override fun onColor() {
            val ft = supportFragmentManager.beginTransaction()
            mCurrentFragment = ColorBismilahFragment()
            ft.replace(R.id.m_container, mCurrentFragment!!)
            ft.commit()
        }
        override fun onAnim() {}
        override fun onDelete() {
            blurredImageView.setEntity_select(null)
            blurredImageView.invalidate()
            hideFragment()
            updateTime()
        }
        override fun onDone() { hideFragment() }
        override fun fromNow() { trackViewEntity.translateFromNow() }
        override fun fromTheStart() { trackViewEntity.translateFromStart() }
        override fun untilNow() { trackViewEntity.translateUntilNow() }
        override fun untilTheEnd() { trackViewEntity.translateToRight() }
        override fun update() {}
        override fun updateAya(color: Int) {}
        override fun updatePreset(preset: AyaTextPreset) {}
    }

    // ── Callback: IEditMultipleCallback ────────────────────────────────

    private val iEditMultipleCallback = object : EditMultipleEntityFragment.IEditMultipleCallback {
        override fun onDelete() {
            blurredImageView.setEntity_select(null)
            blurredImageView.invalidate()
            hideFragment()
            updateTime()
        }
    }

    // ── Callback: IEditSName ───────────────────────────────────────────

    private val iEditSName = object : EditS_NameFragment.IEditS_Name {
        override fun onFont(entity: SurahNameEntity) {
            val ft = supportFragmentManager.beginTransaction()
            mCurrentFragment = FontFragment.getInstance(iFontCallback, entity.fontName, Typeface.DEFAULT)
            ft.replace(R.id.m_container, mCurrentFragment!!)
            ft.commit()
        }
        override fun onEdit(entity: SurahNameEntity) {
            isToCrop = true
            val intent = Intent(this@EngineActivity, EditS_NameActivity::class.java)
            intent.putExtra("name", entity.surahName)
            editSurahNameResult.launch(intent)
        }
        override fun onColor(entity: SurahNameEntity) {
            val ft = supportFragmentManager.beginTransaction()
            mCurrentFragment = ColorS_NameFragment()
            ft.replace(R.id.m_container, mCurrentFragment!!)
            ft.commit()
        }
        override fun onDone() { hideFragment() }
        override fun update() {}
    }

    // ── Callback: IFontCallback ────────────────────────────────────────

    private val iFontCallback = object : FontFragment.IFontCallback {
        override fun onAdd(fontName: String, typeface: Typeface) {
            blurredImageView.setTypeface(typeface, fontName)
        }
        override fun onCancel(lastFont: String, lastTypeface: Typeface) { hideFragment() }
        override fun onDone(fontName: String, typeface: Typeface) { hideFragment() }
    }

    // ── Callback: IQuranIconCallback ───────────────────────────────────

    private val iQuranIconCallback = object : EditIconQuranFragment.IQuranIconCallback {
        override fun add(icon: String) {}
        override fun onCancel(lastIcon: String) { hideFragment() }
        override fun onDone(icon: String) { hideFragment() }
    }

    // ── Callback: ITransition (EffectAyaFragment) ──────────────────────

    private val iTransitionCallback = object : EffectAyaFragment.ITransition {
        override fun `in`(type: String, entityQuranTimeline: EntityQuranTimeline) {
            entityQuranTimeline.quranEntity.entityTransition.type_in = type
            entityQuranTimeline.quranEntity.entityTransition.isIn = true
            blurredImageView.invalidate()
        }
        override fun out(type: String, entityQuranTimeline: EntityQuranTimeline) {
            entityQuranTimeline.quranEntity.entityTransition.type_out = type
            entityQuranTimeline.quranEntity.entityTransition.isOut = true
            blurredImageView.invalidate()
        }
        override fun applyAll(tabIndex: Int, entityQuranTimeline: EntityQuranTimeline) {}
        override fun destroy(entityQuranTimeline: EntityQuranTimeline) { hideFragment() }
        override fun onHideFragment(entityQuranTimeline: EntityQuranTimeline) { hideFragment() }
        override fun playing(entityQuranTimeline: EntityQuranTimeline) {}
        override fun remove(tabIndex: Int, entityQuranTimeline: EntityQuranTimeline) {}
        override fun toSubscribe() { dialogPremium(0) }
        override fun updateDurationIn(duration: Float, entityQuranTimeline: EntityQuranTimeline) {
            entityQuranTimeline.quranEntity.entityTransition.duration_in = duration
        }
        override fun updateDurationOut(duration: Float, entityQuranTimeline: EntityQuranTimeline) {
            entityQuranTimeline.quranEntity.entityTransition.duration_out = duration
        }
    }

    // ── Callback: ITransition (EffectBismilahFragment) ─────────────────

    private val iTransitionBismilahCallback = object : EffectBismilahFragment.ITransition {
        override fun `in`(type: String, entityBismilahTimeline: EntityBismilahTimeline) {
            entityBismilahTimeline.quranEntity.entityTransition.type_in = type
            entityBismilahTimeline.quranEntity.entityTransition.isIn = true
            blurredImageView.invalidate()
        }
        override fun out(type: String, entityBismilahTimeline: EntityBismilahTimeline) {
            entityBismilahTimeline.quranEntity.entityTransition.type_out = type
            entityBismilahTimeline.quranEntity.entityTransition.isOut = true
            blurredImageView.invalidate()
        }
        override fun applyAll(entityBismilahTimeline: EntityBismilahTimeline) {}
        override fun destroy(entityBismilahTimeline: EntityBismilahTimeline) { hideFragment() }
        override fun onHideFragment(entityBismilahTimeline: EntityBismilahTimeline) { hideFragment() }
        override fun playing(entityBismilahTimeline: EntityBismilahTimeline) {}
        override fun remove(tabIndex: Int, entityBismilahTimeline: EntityBismilahTimeline) {}
        override fun updateDurationIn(duration: Float, entityBismilahTimeline: EntityBismilahTimeline) {
            entityBismilahTimeline.quranEntity.entityTransition.duration_in = duration
        }
        override fun updateDurationOut(duration: Float, entityBismilahTimeline: EntityBismilahTimeline) {
            entityBismilahTimeline.quranEntity.entityTransition.duration_out = duration
        }
    }

    // ── Callback: IEdiTextCallback ─────────────────────────────────────

    private val iEdiTextCallback = object : EditTextFragment.IEdiTextCallback {
        override fun onDone(entityQuranTimeline: EntityQuranTimeline) {
            blurredImageView.invalidate()
            hideFragment()
        }
        override fun onUpdate(quranEntity: QuranEntity) {
            blurredImageView.invalidate()
        }
    }

    // ── Audio Effects ──────────────────────────────────────────────────

    private fun applyEffect(effect: EffectAudio, entityAudio: EntityAudio) {
        try {
            val uri = entityAudio.filePath
            val outputFile = File(cacheDir, "effect_${System.currentTimeMillis()}.mp3")
            val cmd = "-y -i \"$uri\" -af \"volume=${effect.volume}\" \"${outputFile.absolutePath}\""

            FFmpegKit.executeAsync(cmd, { session ->
                if (ReturnCode.isSuccess(session.returnCode)) {
                    mainHandler.post {
                        try {
                            entityAudio.mediaPlayer?.release()
                            entityAudio.mediaPlayer = MediaPlayer.create(this@EngineActivity, Uri.fromFile(outputFile))
                            entityAudio.filePath = Uri.fromFile(outputFile).toString()
                        } catch (e: Exception) { e.printStackTrace() }
                    }
                }
            })
        } catch (e: Exception) { e.printStackTrace() }
    }

    private fun applyEffectAll(effect: EffectAudio, index: Int) {
        val audioList = trackViewEntity.getEntityListAudio()
        if (audioList.isNotEmpty()) {
            applyEffect(effect, audioList[index.coerceIn(0, audioList.size - 1)])
        }
    }

    // ── Crop ───────────────────────────────────────────────────────────

    private fun toCrop() {
        try {
            val intent = Intent(this, CropBitmapActivity::class.java)
            intent.putExtra("uri", uri_bg)
            launchCropActivity.launch(intent)
        } catch (e: Exception) { e.printStackTrace() }
    }

    // ── Update Progress ────────────────────────────────────────────────

    fun updateProgress(progress: Int, max: Int) {
        try {
            val fragment = supportFragmentManager.findFragmentById(R.id.container_progress)
            // ProgressViewFragment doesn't have updateProgress method yet
        } catch (_: Exception) {}
    }
}
