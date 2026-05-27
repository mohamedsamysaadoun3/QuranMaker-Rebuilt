package hazem.nurmontage.videoquran.model

import java.io.File
import java.io.Serializable

/**
 * Represents a folder/item in the gallery explorer.
 * Maps to a folder on the device's storage with image/video files.
 */
class ExploreItem(
    val folder: File?,
    val path: String,
    val size: String,
    val name: String,
    val firstFilePath: String?
) : Serializable
