package hazem.nurmontage.videoquran.Utils

import android.content.ContentUris
import android.content.Context
import android.graphics.Typeface
import android.net.Uri
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.util.Log

object UtilsFileLast {

    private const val TAG = "UtilsFileLast"

    fun loadFontFromAsset(context: Context, fontPath: String): Typeface? {
        return try {
            Typeface.createFromAsset(context.assets, fontPath)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun getPath(context: Context, uri: Uri): String? {
        Log.d(TAG, "getPath called with URI: $uri")
        if (context == null || uri == null) {
            Log.e(TAG, "Context or URI is null")
            return null
        }

        if (DocumentsContract.isDocumentUri(context, uri)) {
            Log.d(TAG, "URI is a document URI")
            when {
                isExternalStorageDocument(uri) -> {
                    Log.d(TAG, "URI is an external storage document")
                    val docId = DocumentsContract.getDocumentId(uri)
                    val split = docId.split(":")
                    if ("primary".equals(split[0], ignoreCase = true)) {
                        val path = Environment.getExternalStorageDirectory().toString() + "/" + split[1]
                        Log.d(TAG, "External storage path (primary): $path")
                        return path
                    }
                    Log.d(TAG, "External storage path (non-primary): $docId")
                    val treeUri = DocumentsContract.buildTreeDocumentUri(
                        "com.android.externalstorage.documents", docId
                    )
                    return getPathFromTreeUri(context, treeUri, split[1])
                }
                isDownloadsDocument(uri) -> {
                    Log.d(TAG, "URI is a downloads document")
                    val docId = DocumentsContract.getDocumentId(uri)
                    val numericId = extractNumericId(docId) ?: run {
                        Log.e(TAG, "Could not extract numeric ID from downloads document ID: $docId")
                        return null
                    }
                    return try {
                        val path = getDataColumn(
                            context,
                            ContentUris.withAppendedId(
                                Uri.parse("content://downloads/public_downloads"),
                                numericId.toLong()
                            ),
                            null, null
                        )
                        Log.d(TAG, "Downloads document path: $path")
                        path
                    } catch (e: NumberFormatException) {
                        Log.e(TAG, "Error parsing numeric ID from downloads document ID: $numericId", e)
                        null
                    }
                }
                isMediaDocument(uri) -> {
                    Log.d(TAG, "URI is a media document")
                    val docId = DocumentsContract.getDocumentId(uri)
                    val split = docId.split(":")
                    val type = split[0]
                    val contentUri: Uri? = when (type) {
                        "image" -> MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                        "video" -> MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                        "audio" -> MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                        else -> {
                            Log.w(TAG, "Unsupported media document type: $type")
                            return null
                        }
                    }
                    val path = getDataColumn(context, contentUri, "_id=?", arrayOf(split[1]))
                    Log.d(TAG, "Media document path: $path")
                    path
                }
                else -> {
                    Log.w(TAG, "Unsupported document URI: $uri")
                    return null
                }
            }
        }

        if ("content".equals(uri.scheme, ignoreCase = true)) {
            Log.d(TAG, "URI is a content URI")
            val path = getDataColumn(context, uri, null, null)
            Log.d(TAG, "Content URI path: $path")
            return path
        }

        if ("file".equals(uri.scheme, ignoreCase = true)) {
            Log.d(TAG, "URI is a file URI")
            val path = uri.path
            Log.d(TAG, "File URI path: $path")
            return path
        }

        Log.w(TAG, "Unsupported URI scheme: ${uri.scheme}")
        return null
    }

    private fun extractNumericId(docId: String): String? {
        return docId
    }

    private fun getDataColumn(
        context: Context, uri: Uri?, selection: String?,
        selectionArgs: Array<String>?
    ): String? {
        if (uri == null) return null
        var cursor: android.database.Cursor? = null
        try {
            cursor = context.contentResolver.query(
                uri, arrayOf("_data"), selection, selectionArgs, null
            )
            if (cursor != null && cursor.moveToFirst()) {
                return cursor.getString(cursor.getColumnIndexOrThrow("_data"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting data column", e)
        } finally {
            cursor?.close()
        }
        return null
    }

    private fun isExternalStorageDocument(uri: Uri): Boolean {
        return "com.android.externalstorage.documents" == uri.authority
    }

    private fun isDownloadsDocument(uri: Uri): Boolean {
        return "com.android.providers.downloads.documents" == uri.authority
    }

    private fun isMediaDocument(uri: Uri): Boolean {
        return "com.android.providers.media.documents" == uri.authority
    }

    private fun getPathFromTreeUri(
        context: Context, treeUri: Uri, displayName: String
    ): String? {
        try {
            val authority = treeUri.toString()
            val treeDocId = DocumentsContract.getTreeDocumentId(treeUri)
            val childrenUri = DocumentsContract.buildChildDocumentsUri(authority, treeDocId)

            val cursor = context.contentResolver.query(
                childrenUri,
                arrayOf("document_id", "_display_name", "mime_type"),
                null, null, null
            ) ?: return null

            cursor.use {
                while (it.moveToNext()) {
                    val docId = it.getString(it.getColumnIndexOrThrow("document_id"))
                    val name = it.getString(it.getColumnIndexOrThrow("_display_name"))
                    val mimeType = it.getString(it.getColumnIndexOrThrow("mime_type"))

                    if (name == displayName) {
                        if (mimeType == "vnd.android.document/directory") {
                            val childTreeUri = DocumentsContract.buildDocumentUriUsingTree(treeUri, docId)
                            return getPathFromTreeUri(context, childTreeUri, displayName)
                        } else {
                            val docUri = DocumentsContract.buildDocumentUriUsingTree(treeUri, docId)
                            return getDataColumn(context, docUri, null, null)
                        }
                    }

                    if (mimeType == "vnd.android.document/directory") {
                        val childTreeUri = DocumentsContract.buildDocumentUriUsingTree(treeUri, docId)
                        val result = getPathFromTreeUri(context, childTreeUri, displayName)
                        if (result != null) return result
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in getPathFromTreeUri", e)
        }
        return null
    }
}
