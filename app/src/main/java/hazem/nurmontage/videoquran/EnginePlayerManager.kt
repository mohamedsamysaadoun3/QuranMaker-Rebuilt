package hazem.nurmontage.videoquran

import android.media.MediaPlayer
import android.net.Uri
import hazem.nurmontage.videoquran.entity_timeline.EntityAudio

/**
 * Manages audio/video playback, MediaPlayer instances, 
 * video frame animation, and frame processing for EngineActivity.
 */
class EnginePlayerManager(
    private val activity: EngineActivity
) {
    var mPlayer: MediaPlayer? = null

    fun prepareAndStart(uri: Uri, onPrepared: (MediaPlayer) -> Unit) {
        try {
            val player = MediaPlayer()
            mPlayer = player
            player.setAudioStreamType(android.media.AudioManager.STREAM_MUSIC)
            if (uri.scheme?.startsWith("http") == true) {
                player.setDataSource(uri.toString())
            } else {
                player.setDataSource(activity, uri)
            }
            player.prepareAsync()
            player.setOnPreparedListener { mp ->
                if (mp != null) {
                    onPrepared(mp)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            activity.hideProgressFragment()
            activity.hideFragment()
        }
    }

    fun pauseAllAudio() {
        try {
            activity.trackViewEntity.getEntityListAudio().forEach { ea ->
                try {
                    if (ea.mediaPlayer?.isPlaying == true) {
                        ea.mediaPlayer?.pause()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun releasePlayer() {
        try {
            mPlayer?.release()
        } catch (_: Exception) {}
        mPlayer = null
    }

    fun isPlaying(): Boolean {
        return try {
            mPlayer?.isPlaying == true
        } catch (_: Exception) {
            false
        }
    }

    fun setMediaPlayerForEntity(entityAudio: EntityAudio, player: MediaPlayer) {
        entityAudio.mediaPlayer = player
    }

    fun updateFrame() {
        // Update video frame if video background is playing
        if (activity.mTemplate?.isVideoSquare != true) return
        // Frame update handled by video animator
    }

    fun clearCallbacks() {
        releasePlayer()
    }
}
