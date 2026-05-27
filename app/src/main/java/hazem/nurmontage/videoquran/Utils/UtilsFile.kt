package hazem.nurmontage.videoquran.Utils

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.provider.DocumentsContract
import android.provider.MediaStore

/**
 * URI-to-file path resolution utility.
 */
object UtilsFile {

    fun getPath(context: Context, uri: Uri): String? {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (DocumentsContract.isDocumentUri(context, uri)) {
                return when {
                    isExternalStorageDocument(uri) -> getExternalStorageDocumentPath(uri)
                    isDownloadsDocument(uri) -> getDownloadsDocumentPath(context, uri)
                    isMediaDocument(uri) -> getMediaDocumentPath(context, uri)
                    else -> null
                }
            }
        }
        if ("content".equals(uri.scheme, ignoreCase = true)) {
            return getDataColumn(context, uri, null, null)
        }
        if ("file".equals(uri.scheme, ignoreCase = true)) {
            return uri.path
        }
        return null
    }

    private fun getDataColumn(context: Context, uri: Uri, selection: String?, selectionArgs: Array<String>?): String? {
        var cursor: Cursor? = null
        val column = "_data"
        try {
            cursor = context.contentResolver.query(uri, arrayOf(column), selection, selectionArgs, null)
            if (cursor != null && cursor.moveToFirst()) {
                return cursor.getString(cursor.getColumnIndexOrThrow(column))
            }
        } catch (_: Exception) {
        } finally {
            cursor?.close()
        }
        return null
    }

    private fun isExternalStorageDocument(uri: Uri): Boolean =
        "com.android.externalstorage.documents" == uri.authority

    private fun isDownloadsDocument(uri: Uri): Boolean =
        "com.android.providers.downloads.documents" == uri.authority

    private fun isMediaDocument(uri: Uri): Boolean =
        "com.android.providers.media.documents" == uri.authority

    private fun getExternalStorageDocumentPath(uri: Uri): String? {
        val docId = DocumentsContract.getDocumentId(uri)
        val split = docId.split(":").toTypedArray()
        val type = split[0]
        if ("primary".equals(type, ignoreCase = true)) {
            return "${android.os.Environment.getExternalStorageDirectory()}/${split[1]}"
        }
        return "/storage/$type/${split[1]}"
    }

    private fun getDownloadsDocumentPath(context: Context, uri: Uri): String? {
        val id = DocumentsContract.getDocumentId(uri)
        val contentUri = ContentUris.withAppendedId(
            Uri.parse("content://downloads/public_downloads"), id.toLongOrNull() ?: return null
        )
        return getDataColumn(context, contentUri, null, null)
    }

    private fun getMediaDocumentPath(context: Context, uri: Uri): String? {
        val docId = DocumentsContract.getDocumentId(uri)
        val split = docId.split(":").toTypedArray()
        val type = split[0]
        val contentUri = when (type) {
            "image" -> MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            "video" -> MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            "audio" -> MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
            else -> return null
        }
        return getDataColumn(context, contentUri, "_id=?", arrayOf(split[1]))
    }
}
