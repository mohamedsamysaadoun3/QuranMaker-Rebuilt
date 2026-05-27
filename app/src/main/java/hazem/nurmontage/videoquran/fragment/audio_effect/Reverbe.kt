package hazem.nurmontage.videoquran.fragment.audio_effect

/**
 * Represents a reverb preset with a display name and FFmpeg command.
 */
data class Reverbe(
    val name: String,
    val cmdFfmpeg: String?
) {
}
