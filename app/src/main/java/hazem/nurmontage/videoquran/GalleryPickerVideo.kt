package hazem.nurmontage.videoquran

import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.core.content.ContextCompat
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import hazem.nurmontage.videoquran.Utils.AppSettingsHelper
import hazem.nurmontage.videoquran.Utils.AppUtils
import hazem.nurmontage.videoquran.Utils.LocaleHelper
import hazem.nurmontage.videoquran.Utils.ScreenUtils
import hazem.nurmontage.videoquran.adabter.ExploreAdabters
import hazem.nurmontage.videoquran.adabter.GalleryVideoAdabters
import hazem.nurmontage.videoquran.common.Common
import hazem.nurmontage.videoquran.model.ExploreItem
import hazem.nurmontage.videoquran.model.GallerySelected
import hazem.nurmontage.videoquran.model.PhotoItem
import hazem.nurmontage.videoquran.model.VideoItem
import hazem.nurmontage.videoquran.views.TextCustumFont
import java.io.File
import java.util.Locale

/**
 * Activity to pick a single video from the device gallery.
 * Displays videos in a grid with folder navigation (explore) support.
 */
class GalleryPickerVideo : BaseActivity() {

    private lateinit var btnDone: ImageButton
    private lateinit var btnExplore: TextCustumFont
    private var galleryPickerAdabters: GalleryVideoAdabters? = null
    private var isUpdate = false
    private var layoutSetting: LinearLayout? = null
    private lateinit var mResources: android.content.res.Resources
    private lateinit var rvExplore: RecyclerView
    private var videoItem: VideoItem? = null

    /**
     * Callback interface for gallery picker events.
     * Implemented by GalleryPickerOneImage and other consumers.
     */
    interface IPicker {
        fun onAdd(photoItem: PhotoItem, position: Int)
        fun onAdd(videoItem: VideoItem, position: Int)
        fun onDelete(gallerySelected: GallerySelected)
        fun onEmptyList()
    }

    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            if (::rvExplore.isInitialized && rvExplore.visibility == View.VISIBLE) {
                btnExplore.performClick()
                return
            }
            Common.LIST_SELECT = null
            Common.INDEX_LIST_SELECT = 1
            finish()
        }
    }

    private val iPicker = object : IPicker {
        override fun onAdd(photoItem: PhotoItem, position: Int) {}
        override fun onDelete(gallerySelected: GallerySelected) {}
        override fun onEmptyList() {}

        override fun onAdd(videoItem: VideoItem, position: Int) {
            this@GalleryPickerVideo.videoItem = videoItem
        }
    }

    private val iExplore = object : ExploreAdabters.IExplore {
        override fun folder(file: File?, name: String, path: String) {
            if (rvExplore.visibility != View.INVISIBLE) {
                rvExplore.visibility = View.INVISIBLE
            }
            changeFolder(path)
            btnExplore.text = name
        }

        override fun done() {
            if (rvExplore.visibility != View.INVISIBLE) {
                rvExplore.visibility = View.INVISIBLE
            }
        }
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase))
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_gallery_picker_video)

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

        Common.LIST_SELECT = null
        Common.INDEX_LIST_SELECT = 1
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)

        mResources = resources
        btnDone = findViewById(R.id.tv_done)
        rvExplore = findViewById(R.id.rv_explore)

        btnDone.setOnClickListener {
            if (videoItem != null) {
                val intent = Intent().apply {
                    data = Uri.parse(videoItem!!.path)
                }
                setResult(RESULT_OK, intent)
            }
            finish()
        }

        initViews()
        initFolder()

        // Permission check
        if (Build.VERSION.SDK_INT >= 33 &&
            (ContextCompat.checkSelfPermission(this, "android.permission.READ_MEDIA_IMAGES") ==
                android.content.pm.PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, "android.permission.READ_MEDIA_VIDEO") ==
                    android.content.pm.PackageManager.PERMISSION_GRANTED)
        ) {
            setSetting(true)
        } else if (Build.VERSION.SDK_INT >= 34 &&
            ContextCompat.checkSelfPermission(this, "android.permission.READ_MEDIA_VISUAL_USER_SELECTED") ==
                android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            setSetting(false)
        } else if (ContextCompat.checkSelfPermission(this, "android.permission.READ_EXTERNAL_STORAGE") ==
            android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            setSetting(true)
        } else {
            setSetting(false)
        }
    }

    private fun setSetting(hasPermission: Boolean) {
        if (hasPermission) return

        val toSetting = findViewById<LinearLayout>(R.id.to_setting)
        layoutSetting = toSetting
        toSetting.visibility = View.VISIBLE
        layoutSetting?.setOnClickListener {
            isUpdate = true
            AppSettingsHelper.openAppSettings(this@GalleryPickerVideo)
        }
    }

    private fun updateSetting() {
        if (Build.VERSION.SDK_INT >= 33 &&
            (ContextCompat.checkSelfPermission(this, "android.permission.READ_MEDIA_IMAGES") ==
                android.content.pm.PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, "android.permission.READ_MEDIA_VIDEO") ==
                    android.content.pm.PackageManager.PERMISSION_GRANTED)
        ) {
            recreate()
        } else if ((Build.VERSION.SDK_INT < 34 ||
            ContextCompat.checkSelfPermission(this, "android.permission.READ_MEDIA_VISUAL_USER_SELECTED") !=
                android.content.pm.PackageManager.PERMISSION_GRANTED) &&
            ContextCompat.checkSelfPermission(this, "android.permission.READ_EXTERNAL_STORAGE") ==
                android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            recreate()
        }
        isUpdate = false
    }

    override fun onResume() {
        super.onResume()
        if (isUpdate) {
            updateSetting()
        }
    }

    fun formatDuration(durationMs: Int): String {
        val seconds = durationMs / 1000
        return String.format(Locale.ENGLISH, "%02d:%02d", (seconds / 60) % 60, seconds % 60)
    }

    private fun initFolder() {
        val tvFolders = findViewById<TextCustumFont>(R.id.tv_folders)
        btnExplore = tvFolders
        btnExplore.text = mResources.getString(R.string.all)

        btnExplore.setOnClickListener {
            if (!::rvExplore.isInitialized) return@setOnClickListener
            if (rvExplore.visibility != View.VISIBLE) {
                rvExplore.visibility = View.VISIBLE
                btnExplore.setCompoundDrawablesWithIntrinsicBounds(0, 0, android.R.drawable.arrow_up_float, 0)
            } else {
                rvExplore.visibility = View.INVISIBLE
                btnExplore.setCompoundDrawablesWithIntrinsicBounds(0, 0, android.R.drawable.arrow_down_float, 0)
            }
        }

        Thread {
            val exploreItems = ArrayList<ExploreItem>()
            val videoItems = ArrayList<VideoItem>()
            val folderSet = HashSet<String>()

            var firstVideoPath: String? = null
            var totalVideoCount = 0
            var initialCount = 0

            val cursor: Cursor? = contentResolver.query(
                MediaStore.Files.getContentUri("external"),
                arrayOf("_id", "duration", "_data", "parent"),
                "media_type=3",
                null,
                "date_added DESC"
            )

            cursor?.use {
                while (it.moveToNext()) {
                    val duration = it.getInt(it.getColumnIndexOrThrow("duration"))
                    if (duration != 0) {
                        val dataPath = it.getString(it.getColumnIndexOrThrow("_data"))
                        val uri = ContentUris.withAppendedId(
                            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                            it.getLong(it.getColumnIndexOrThrow("_id"))
                        ).toString()

                        val parent = File(uri).parent
                        if (!folderSet.contains(parent)) {
                            folderSet.add(parent)
                            val folder = File(parent ?: continue)
                            val listFiles = folder.listFiles()
                            var videoCount = 0
                            if (listFiles != null) {
                                for (file in listFiles) {
                                    if (isVideoFile(file)) {
                                        videoCount++
                                        if (firstVideoPath == null) {
                                            firstVideoPath = uri
                                        }
                                    }
                                }
                            }
                            if (videoCount > 0) {
                                totalVideoCount += videoCount
                                exploreItems.add(ExploreItem(folder, parent, "$videoCount", folder.name, uri))
                            }
                        }

                        videoItems.add(VideoItem(parent, uri, formatDuration(duration), false))
                        initialCount++
                        if (initialCount > 50) {
                            // We have initial batch, break for UI update
                            break
                        }
                    }
                }
            }

            // Set up RecyclerView with initial items on UI thread
            runOnUiThread {
                val recyclerView = findViewById<RecyclerView>(R.id.rv)
                recyclerView.setHasFixedSize(true)
                recyclerView.layoutManager = GridLayoutManager(this@GalleryPickerVideo, 3)
                recyclerView.setItemViewCacheSize(20)
                @Suppress("DEPRECATION")
                recyclerView.isDrawingCacheEnabled = true
                recyclerView.itemAnimator = null

                val screenWidth = (ScreenUtils.getScreenWidth(this@GalleryPickerVideo) * 0.3f).toInt()
                galleryPickerAdabters = GalleryVideoAdabters(
                    AppUtils.getAppVersionName(this@GalleryPickerVideo),
                    mResources,
                    null,
                    screenWidth,
                    iPicker
                )
                galleryPickerAdabters?.addItems(videoItems)
                recyclerView.adapter = galleryPickerAdabters
            }

            // Continue reading remaining items from cursor
            cursor?.use {
                while (it.moveToNext()) {
                    val duration = it.getInt(it.getColumnIndexOrThrow("duration"))
                    if (duration != 0) {
                        val dataPath = it.getString(it.getColumnIndexOrThrow("_data"))
                        val parent = File(dataPath).parent
                        val uri = ContentUris.withAppendedId(
                            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                            it.getLong(it.getColumnIndexOrThrow("_id"))
                        ).toString()

                        if (!folderSet.contains(parent)) {
                            folderSet.add(parent)
                            val folder = File(parent ?: continue)
                            val listFiles = folder.listFiles()
                            var videoCount = 0
                            if (listFiles != null) {
                                for (file in listFiles) {
                                    if (isVideoFile(file)) {
                                        videoCount++
                                        if (firstVideoPath == null) {
                                            firstVideoPath = uri
                                        }
                                    }
                                }
                            }
                            if (videoCount > 0) {
                                totalVideoCount += videoCount
                                exploreItems.add(ExploreItem(folder, parent, "$videoCount", folder.name, uri))
                            }
                        }

                        videoItems.add(VideoItem(parent, uri, formatDuration(duration), false))
                    }
                }
            }

            // Add "All" item at the beginning
            exploreItems.add(
                0,
                ExploreItem(
                    null,
                    mResources.getString(R.string.all),
                    "$totalVideoCount",
                    mResources.getString(R.string.all),
                    firstVideoPath
                )
            )

            // Update UI with full data
            runOnUiThread {
                galleryPickerAdabters?.doneItems(videoItems)
                galleryPickerAdabters?.notifyDataSetChanged()

                rvExplore.setHasFixedSize(true)
                rvExplore.layoutManager = LinearLayoutManager(this@GalleryPickerVideo)
                rvExplore.setItemViewCacheSize(20)
                @Suppress("DEPRECATION")
                rvExplore.isDrawingCacheEnabled = true
                rvExplore.itemAnimator = null
                rvExplore.adapter = ExploreAdabters(
                    exploreItems,
                    (ScreenUtils.getScreenWidth(this@GalleryPickerVideo) * 0.2f).toInt(),
                    iExplore,
                    btnExplore.text.toString()
                )

                findViewById<View>(R.id.view_progress).visibility = View.GONE
                btnExplore.visibility = View.VISIBLE
            }
        }.start()
    }

    fun isVideoFile(file: File): Boolean {
        val lowerName = file.name.lowercase()
        return lowerName.endsWith(".mp4") || lowerName.endsWith(".avi") ||
                lowerName.endsWith(".mov") || lowerName.endsWith(".mkv") ||
                lowerName.endsWith(".wmv") || lowerName.endsWith(".flv") ||
                lowerName.endsWith(".webm") || lowerName.endsWith(".3gp") ||
                lowerName.endsWith(".m4v") || lowerName.endsWith(".mpg") ||
                lowerName.endsWith(".mpeg")
    }

    private fun isImageFile(file: File): Boolean {
        val lowerName = file.name.lowercase()
        return lowerName.endsWith(".jpg") || lowerName.endsWith(".jpeg") || lowerName.endsWith(".png")
    }

    private fun initViews() {
        findViewById<View>(R.id.btn_onBack).setOnClickListener {
            finish()
        }
    }

    fun changeFolder(path: String) {
        if (path == mResources.getString(R.string.all)) {
            galleryPickerAdabters?.updateAll()
        } else {
            galleryPickerAdabters?.update(path)
        }
    }
}
