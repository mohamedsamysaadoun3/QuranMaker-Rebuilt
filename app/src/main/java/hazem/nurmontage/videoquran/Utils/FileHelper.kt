package hazem.nurmontage.videoquran.Utils

import android.content.Context
import android.os.Environment
import android.util.Log
import java.io.File

/**
 * Helper class for creating video output directories in both app-private
 * and public external storage locations.
 *
 * @param context Android context used to resolve app-specific external storage paths.
 */
class FileHelper(private val context: Context) {

    companion object {
        private const val TAG = "FileHelper"
    }

    /**
     * Creates (if needed) a subdirectory inside the app's private external Movies folder.
     *
     * The resulting path is: `[App External Files Dir]/Movies/[folderName]`
     *
     * This directory is deleted when the app is uninstalled.
     *
     * @param folderName Name of the subdirectory to create under Movies.
     * @return The created [File] directory, or `null` if the directory could not be created.
     */
    fun createVideoFolder(folderName: String): File? {
        val folder = File(context.getExternalFilesDir(Environment.DIRECTORY_MOVIES), folderName)
        return ensureFolderExists(folder)
    }

    /**
     * Creates (if needed) a subdirectory inside the public Movies folder on external storage.
     *
     * The resulting path is: `[Public Movies Dir]/[folderName]`
     *
     * This directory persists after the app is uninstalled.
     *
     * @param folderName Name of the subdirectory to create under the public Movies directory.
     * @return The created [File] directory, or `null` if the directory could not be created.
     */
    fun createPublicVideoFolder(folderName: String): File? {
        val folder = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES), folderName)
        return ensureFolderExists(folder)
    }

    /**
     * Ensures the given [folder] exists on disk, creating it (and parent dirs) if necessary.
     *
     * @param folder The directory to verify or create.
     * @return The [folder] if it exists or was successfully created, `null` otherwise.
     */
    private fun ensureFolderExists(folder: File): File? {
        if (folder.exists()) return folder
        return if (folder.mkdirs()) {
            Log.d(TAG, "Folder created successfully: ${folder.absolutePath}")
            folder
        } else {
            Log.e(TAG, "Failed to create folder: ${folder.absolutePath}")
            null
        }
    }
}
