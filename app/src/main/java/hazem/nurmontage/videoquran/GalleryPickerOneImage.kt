package hazem.nurmontage.videoquran

import android.content.ContentUris
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
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
import hazem.nurmontage.videoquran.Utils.ScreenUtils
import hazem.nurmontage.videoquran.adabter.ExploreAdabters
import hazem.nurmontage.videoquran.adabter.GalleryPickerAdabters
import hazem.nurmontage.videoquran.common.Common
import hazem.nurmontage.videoquran.model.ExploreItem
import hazem.nurmontage.videoquran.model.GallerySelected
import hazem.nurmontage.videoquran.model.PhotoItem
import hazem.nurmontage.videoquran.model.VideoItem
import hazem.nurmontage.videoquran.views.TextCustumFont
import java.io.File

/**
 * Activity to pick a single image from the device gallery.
 * Displays images in a grid with folder navigation (explore) support.
 */
class GalleryPickerOneImage : BaseActivity() {

    private lateinit var btnDone: ImageButton
    private lateinit var btnExplore: TextCustumFont
    private var galleryPickerAdabters: GalleryPickerAdabters? = null
    private var isUpdate = false
    private var layoutSetting: LinearLayout? = null
    private var mPhotoItem: PhotoItem? = null
    private lateinit var mResources: android.content.res.Resources
    private lateinit var rvExplore: RecyclerView

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

    private val iPicker = object : GalleryPickerVideo.IPicker {
        override fun onAdd(videoItem: VideoItem, position: Int) {}
        override fun onDelete(gallerySelected: GallerySelected) {}
        override fun onEmptyList() {
            setSetting(false)
        }

        override fun onAdd(photoItem: PhotoItem, position: Int) {
            mPhotoItem = photoItem
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
            if (mPhotoItem != null) {
                val intent = Intent().apply {
                    data = Uri.parse(mPhotoItem!!.path)
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
            AppSettingsHelper.openAppSettings(this@GalleryPickerOneImage)
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
            val photoItems = ArrayList<PhotoItem>()
            val folderSet = HashSet<String>()

            var firstImagePath: String? = null
            var totalImageCount = 0
            var initialCount = 0

            val cursor: Cursor? = contentResolver.query(
                MediaStore.Files.getContentUri("external"),
                arrayOf("_id", "_data", "parent"),
                "media_type=1",
                null,
                "date_added DESC"
            )

            Log.e("query start", "$cursor")

            cursor?.use {
                while (it.moveToNext()) {
                    val parent = File(it.getString(it.getColumnIndexOrThrow("_data"))).parent
                    if (!folderSet.contains(parent)) {
                        folderSet.add(parent)
                        val folder = File(parent)
                        val listFiles = folder.listFiles()
                        var imageCount = 0
                        var folderFirstImage: String? = null
                        if (listFiles != null) {
                            for (file in listFiles) {
                                if (isImageFile(file)) {
                                    imageCount++
                                    if (folderFirstImage == null) {
                                        folderFirstImage = file.absolutePath
                                        if (firstImagePath == null) {
                                            firstImagePath = folderFirstImage
                                        }
                                    }
                                }
                            }
                        }
                        if (imageCount > 0) {
                            totalImageCount += imageCount
                            exploreItems.add(ExploreItem(folder, parent, "$imageCount", folder.name, folderFirstImage))
                        }
                    }

                    photoItems.add(
                        PhotoItem(
                            parent,
                            ContentUris.withAppendedId(
                                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                                it.getLong(it.getColumnIndexOrThrow("_id"))
                            ).toString(),
                            false
                        )
                    )
                    initialCount++
                    if (initialCount > 50) break
                }
            }

            // Set up RecyclerView with initial items on UI thread
            runOnUiThread {
                val recyclerView = findViewById<RecyclerView>(R.id.rv)
                recyclerView.setHasFixedSize(true)
                recyclerView.layoutManager = GridLayoutManager(this@GalleryPickerOneImage, 3)
                recyclerView.setItemViewCacheSize(20)
                @Suppress("DEPRECATION")
                recyclerView.isDrawingCacheEnabled = true
                recyclerView.itemAnimator = null

                val screenWidth = (ScreenUtils.getScreenWidth(this@GalleryPickerOneImage) * 0.3f).toInt()
                galleryPickerAdabters = GalleryPickerAdabters(
                    AppUtils.getAppVersionName(this@GalleryPickerOneImage),
                    mResources,
                    null,
                    screenWidth,
                    iPicker
                )
                galleryPickerAdabters?.addItems(photoItems)
                recyclerView.adapter = galleryPickerAdabters
            }

            // Continue reading remaining items from cursor
            cursor?.use {
                while (it.moveToNext()) {
                    val parent = File(it.getString(it.getColumnIndexOrThrow("_data"))).parent
                    if (!folderSet.contains(parent)) {
                        folderSet.add(parent)
                        val folder = File(parent)
                        val listFiles = folder.listFiles()
                        var imageCount = 0
                        var folderFirstImage: String? = null
                        if (listFiles != null) {
                            for (file in listFiles) {
                                if (isImageFile(file)) {
                                    imageCount++
                                    if (folderFirstImage == null) {
                                        folderFirstImage = file.absolutePath
                                        if (firstImagePath == null) {
                                            firstImagePath = folderFirstImage
                                        }
                                    }
                                }
                            }
                        }
                        if (imageCount > 0) {
                            totalImageCount += imageCount
                            exploreItems.add(ExploreItem(folder, parent, "$imageCount", folder.name, folderFirstImage))
                        }
                    }
                    photoItems.add(
                        PhotoItem(
                            parent,
                            ContentUris.withAppendedId(
                                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                                it.getLong(it.getColumnIndexOrThrow("_id"))
                            ).toString(),
                            false
                        )
                    )
                }
            }

            // Add "All" item at the beginning
            exploreItems.add(
                0,
                ExploreItem(
                    null,
                    mResources.getString(R.string.all),
                    "$totalImageCount",
                    mResources.getString(R.string.all),
                    firstImagePath
                )
            )

            // Update UI with full data
            runOnUiThread {
                galleryPickerAdabters?.doneItems(photoItems)
                galleryPickerAdabters?.notifyDataSetChanged()

                rvExplore.setHasFixedSize(true)
                rvExplore.layoutManager = LinearLayoutManager(this@GalleryPickerOneImage)
                rvExplore.setItemViewCacheSize(20)
                @Suppress("DEPRECATION")
                rvExplore.isDrawingCacheEnabled = true
                rvExplore.itemAnimator = null
                rvExplore.adapter = ExploreAdabters(
                    exploreItems,
                    (ScreenUtils.getScreenWidth(this@GalleryPickerOneImage) * 0.2f).toInt(),
                    iExplore,
                    btnExplore.text.toString()
                )

                findViewById<View>(R.id.view_progress).visibility = View.GONE
                btnExplore.visibility = View.VISIBLE
            }
        }.start()
    }

    fun isImageFile(file: File): Boolean {
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
