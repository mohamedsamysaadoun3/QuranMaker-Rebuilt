package hazem.nurmontage.videoquran.fragment

import android.content.ContentUris
import android.database.Cursor
import android.media.MediaMetadataRetriever
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import hazem.nurmontage.videoquran.GalleryPickerVideo
import hazem.nurmontage.videoquran.R
import hazem.nurmontage.videoquran.Utils.AppUtils
import hazem.nurmontage.videoquran.Utils.ScreenUtils
import hazem.nurmontage.videoquran.adabter.GalleryVideoAdabters
import hazem.nurmontage.videoquran.databinding.FragmentGalleryVideoBinding
import hazem.nurmontage.videoquran.model.GallerySelected
import hazem.nurmontage.videoquran.model.VideoItem
import java.io.File
import java.io.IOException

/**
 * Fragment for displaying and selecting photos from the device gallery.
 * Despite the name "Photos", the original Java queries MediaStore.Video and uses
 * video-related classes — this appears to be for selecting image thumbnails from video files.
 * Converted from GalleryPhotosFragment.java (181 lines).
 */
class GalleryPhotosFragment() : Fragment() {

    companion object {
        var instance: GalleryPhotosFragment? = null

        @Synchronized
        fun get(
            gallerySelecteds: List<GallerySelected>?,
            folder: File?,
            iPicker: GalleryPickerVideo.IPicker?
        ): GalleryPhotosFragment {
            if (instance == null) {
                instance = GalleryPhotosFragment(gallerySelecteds, folder, iPicker)
            }
            return instance!!
        }
    }

    private var adabters: GalleryVideoAdabters? = null
    private var folder: File? = null
    private var gallerySelecteds: List<GallerySelected>? = null
    private var galleryVideoBinding: FragmentGalleryVideoBinding? = null
    private var iPicker: GalleryPickerVideo.IPicker? = null


    constructor(
        gallerySelecteds: List<GallerySelected>?,
        folder: File?,
        iPicker: GalleryPickerVideo.IPicker?
    ) : this() {
        this.iPicker = iPicker
        this.folder = folder
        this.gallerySelecteds = gallerySelecteds
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val inflate = FragmentGalleryVideoBinding.inflate(inflater, container, false)
        galleryVideoBinding = inflate
        val root = inflate.root as FrameLayout
        loadVideos(root)
        return root
    }

    fun isContains(path: String): VideoItem? {
        val list = gallerySelecteds ?: return null
        for (gallerySelected in list) {
            val videoItem = gallerySelected.videoItem
            if (videoItem != null && videoItem.path == path) {
                return videoItem
            }
        }
        return null
    }

    fun inselect(position: Int) {
        adabters?.inselectItem(position)
    }

    @Suppress("DEPRECATION")
    private fun loadVideos(view: View) {
        val recyclerView = view.findViewById<RecyclerView>(R.id.rv_gallery)
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = GridLayoutManager(context, 3)
        recyclerView.setItemViewCacheSize(20)
        recyclerView.isDrawingCacheEnabled = true
        recyclerView.itemAnimator = null

        // GalleryVideoAdabters is already implemented
        val galleryVideoAdabters = GalleryVideoAdabters(
            AppUtils.getAppVersionName(requireContext()),
            resources,
            gallerySelecteds,
            (ScreenUtils.getScreenWidth(requireActivity()) * 0.24f).toInt(),
            iPicker
        )
        adabters = galleryVideoAdabters
        recyclerView.adapter = galleryVideoAdabters

        val folderFile = folder
        if (folderFile != null) {
            changeFolder(folderFile)
        } else {
            Thread {
                val query: Cursor? = requireActivity().contentResolver.query(
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                    arrayOf("_id", "duration"),
                    null, null, null
                )
                val arrayList = ArrayList<VideoItem>()
                query?.use { cursor ->
                    while (cursor.moveToNext()) {
                        cursor.getInt(cursor.getColumnIndexOrThrow("duration"))
                        val contentUri = ContentUris.withAppendedId(
                            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                            cursor.getLong(cursor.getColumnIndexOrThrow("_id"))
                        ).toString()
                        val isContainsResult = isContains(contentUri)
                        if (isContainsResult != null) {
                            isContainsResult.isSelect = true
                            arrayList.add(isContainsResult)
                        }
                    }
                }
                requireActivity().runOnUiThread {
                    adabters?.addItems(arrayList)
                    adabters?.notifyDataSetChanged()
                }
            }.start()
        }
    }

    private fun isVideoFile(file: File): Boolean {
        val lowerCase = file.name.lowercase()
        return lowerCase.endsWith(".mp4") || lowerCase.endsWith(".avi") ||
                lowerCase.endsWith(".mov") || lowerCase.endsWith(".mkv") ||
                lowerCase.endsWith(".wmv") || lowerCase.endsWith(".flv") ||
                lowerCase.endsWith(".webm") || lowerCase.endsWith(".3gp") ||
                lowerCase.endsWith(".m4v") || lowerCase.endsWith(".mpg") ||
                lowerCase.endsWith(".mpeg")
    }

    fun changeFolder(file: File?) {
        adabters?.clear()
        if (file != null && file.exists() && file.isDirectory) {
            val listFiles = file.listFiles() ?: return
            val arrayList = ArrayList<VideoItem>()
            for (file2 in listFiles) {
                if (file2.isFile && isVideoFile(file2)) {
                    val absolutePath = file2.absolutePath
                    getVideoDuration(absolutePath)
                    val isContainsResult = isContains(absolutePath)
                    if (isContainsResult != null) {
                        isContainsResult.isSelect = true
                        arrayList.add(isContainsResult)
                    }
                }
            }
            if (arrayList.isNotEmpty()) {
                adabters?.addItems(arrayList)
                adabters?.let {
                    it.notifyItemInserted(it.itemCount - 1)
                }
                return
            }
        }
        if (adabters?.itemCount == 0) {
            adabters?.notifyDataSetChanged()
        }
    }

    private fun getVideoDuration(path: String): Int {
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(path)
        val duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toInt() ?: 0
        try {
            retriever.release()
        } catch (_: IOException) {
        }
        return duration
    }

    override fun onDestroyView() {
        iPicker = null
        galleryVideoBinding?.root?.removeAllViews()
        galleryVideoBinding = null
        instance = null
        super.onDestroyView()
    }
}
