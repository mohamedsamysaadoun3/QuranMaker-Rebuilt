package hazem.nurmontage.videoquran.Utils

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.util.Log
import hazem.nurmontage.videoquran.common.Common
import java.io.File

object FileUtils {

    fun checkFileExists(path: String): Boolean {
        return File(path).exists()
    }

    fun getFile(context: Context): File? {
        val externalDir = context.getExternalFilesDir(null) ?: return null
        if (!externalDir.exists() && !externalDir.mkdirs()) {
            Log.e("TAG getFileVideo", "! mkdirs.")
            return null
        }
        val workDir = File(externalDir, "Work_${System.currentTimeMillis()}")
        if (workDir.exists() || workDir.mkdirs()) {
            return workDir
        }
        Log.e("TAG getFileVideo", "! mkdirs.")
        return null
    }

    fun getFileVideo(path: String): File? {
        val dir = File(path)
        if (!dir.exists() && !dir.mkdirs()) {
            Log.e("TAG getFileVideo", "! mkdirs.")
            return null
        }
        val frameDir = File(dir, Common.VIDEO_FRAME_FOLDER)
        if (frameDir.exists() || frameDir.mkdirs()) {
            return frameDir
        }
        Log.e("TAG getFileVideo", "! mkdirs.")
        return null
    }

    @Throws(Exception::class)
    fun getFileFromUri(context: Context, uri: Uri): File {
        val path = getPathFromUri(context, uri)
        return File(path ?: throw Exception("Cannot get file path from URI"))
    }

    fun getPathFromUri(context: Context, uri: Uri): String? {
        if (DocumentsContract.isDocumentUri(context, uri)) {
            when {
                isExternalStorageDocument(uri) -> {
                    val docId = DocumentsContract.getDocumentId(uri)
                    val split = docId.split(":")
                    if ("primary".equals(split[0], ignoreCase = true)) {
                        return Environment.getExternalStorageDirectory().toString() + "/" + split[1]
                    }
                }
                isDownloadsDocument(uri) -> {
                    val docId = DocumentsContract.getDocumentId(uri)
                    return getDataColumn(
                        context,
                        ContentUris.withAppendedId(
                            Uri.parse("content://downloads/public_downloads"),
                            docId.toLong()
                        ),
                        null, null
                    )
                }
                isMediaDocument(uri) -> {
                    val docId = DocumentsContract.getDocumentId(uri)
                    val split = docId.split(":")
                    val type = split[0]
                    val contentUri = when (type) {
                        "image" -> MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                        "video" -> MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                        "audio" -> MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                        else -> null
                    }
                    return getDataColumn(context, contentUri, "_id=?", arrayOf(split[1]))
                }
            }
        } else if ("content".equals(uri.scheme, ignoreCase = true)) {
            return getDataColumn(context, uri, null, null)
        } else if ("file".equals(uri.scheme, ignoreCase = true)) {
            return uri.path
        }
        return null
    }

    fun getDataColumn(
        context: Context, uri: Uri?, selection: String?,
        selectionArgs: Array<String>?
    ): String? {
        var cursor: Cursor? = null
        try {
            cursor = context.contentResolver.query(
                uri!!, arrayOf("_data"), selection, selectionArgs, null
            )
            if (cursor != null && cursor.moveToFirst()) {
                return cursor.getString(cursor.getColumnIndexOrThrow("_data"))
            }
        } catch (e: Exception) {
            Log.e("FileUtils", "Error getting data column", e)
        } finally {
            cursor?.close()
        }
        return null
    }

    fun isExternalStorageDocument(uri: Uri): Boolean {
        return "com.android.externalstorage.documents" == uri.authority
    }

    fun isDownloadsDocument(uri: Uri): Boolean {
        return "com.android.providers.downloads.documents" == uri.authority
    }

    fun isMediaDocument(uri: Uri): Boolean {
        return "com.android.providers.media.documents" == uri.authority
    }
}
