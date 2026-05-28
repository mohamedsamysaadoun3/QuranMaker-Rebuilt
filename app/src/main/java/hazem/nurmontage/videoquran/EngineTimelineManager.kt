package hazem.nurmontage.videoquran

import android.graphics.PorterDuff
import android.os.Handler
import android.os.Looper
import android.widget.ImageButton
import hazem.nurmontage.videoquran.Utils.MyVibrationHelper
import hazem.nurmontage.videoquran.Utils.SmoothTimelineAnimator
import hazem.nurmontage.videoquran.Utils.TimeFormatter
import hazem.nurmontage.videoquran.entity_timeline.EntityAudio
import hazem.nurmontage.videoquran.views.TrackEntityView

/**
 * Manages timeline animation, play/pause, cursor, time updates, 
 * transport buttons, and undo/redo for the EngineActivity.
 */
class EngineTimelineManager(
    private val activity: EngineActivity
) {
    var valueAnimator: SmoothTimelineAnimator? = null
    var startCursur: Int = 0
    var current_position_time: Int = 0
    var isOnScroll: Boolean = false

    // Audio player tracking during playback
    var entityAudio_visible: EntityAudio? = null
    var entityAudio_player: EntityAudio? = null
    var lastIndexVisible: Int = 0
    var endTimeAudioVisible: Int = 0

    // References set from EngineActivity
    var btnPlayPause: ImageButton? = null
    var btnToStart: ImageButton? = null
    var btnToEnd: ImageButton? = null
    var btnUndo: ImageButton? = null
    var btnRedo: ImageButton? = null
    var trackViewEntity: TrackEntityView? = null

    fun pauseTimelineAnimation() {
        stop()
        valueAnimator?.let { anim ->
            if (anim.isRunning()) {
                startCursur = anim.getCurrentTimeMs()
                anim.stop()
            }
        }
        valueAnimator = null
    }

    fun startTimelineAnimation() {
        entityAudio_visible = null
        entityAudio_player = null
        lastIndexVisible = 0
        val maxTime = trackViewEntity?.getMaxTime() ?: return
        val timeLineW = trackViewEntity?.getTimeLineW() ?: return

        val animator = SmoothTimelineAnimator(startCursur, maxTime, object : SmoothTimelineAnimator.AnimatorListener {
            override fun onUpdate(timeMs: Int) {
                if (!activity.mIsPlaying || timeMs == 0) return
                val progress = timeMs.toFloat() / maxTime
                activity.updateTime(timeMs)
                trackViewEntity?.updateCursur(progress * timeLineW)
                trackViewEntity?.updateCurrentCursurPosition(timeMs)

                // Audio player management during playback
                val secInScreen = trackViewEntity?.getSecondInScreen() ?: 1f
                val abs = Math.abs(Math.round((trackViewEntity?.getCurrentPosition()?.div(secInScreen))?.times(1000f) ?: 0f))
                if (abs > endTimeAudioVisible) {
                    entityAudio_visible = null
                }
                if (entityAudio_visible == null) {
                    val entityList = trackViewEntity?.getEntityListAudio() ?: return
                    for (i in lastIndexVisible until entityList.size) {
                        val ea = entityList[i]
                        if (ea.visible() && ea.isVisible) {
                            entityAudio_visible = ea
                            endTimeAudioVisible = Math.round((ea.getRect().right / secInScreen) * 1000f)
                            lastIndexVisible = i
                            break
                        }
                    }
                }

                try {
                    val visible = entityAudio_visible
                    val mPlayer = activity.mPlayer
                    if (visible != null) {
                        if (entityAudio_player != visible && mPlayer != null && mPlayer.isPlaying) {
                            mPlayer.pause()
                        }
                        activity.mPlayer = visible.mediaPlayer
                        if (activity.mPlayer != null && !activity.mPlayer!!.isPlaying) {
                            entityAudio_player = visible
                            val seekPos = (abs - Math.abs(Math.round((visible.getRect().left / secInScreen) * 1000f))).toInt()
                            if (seekPos <= activity.mPlayer!!.duration) {
                                activity.mPlayer!!.seekTo(seekPos)
                            }
                            activity.mPlayer!!.start()
                        }
                    } else if (mPlayer != null && mPlayer.isPlaying) {
                        mPlayer.pause()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                updateStartViewTime(trackViewEntity?.getCurrentCursurPosition() ?: 0)
                activity.updateBtnCutState()
            }

            override fun onEnd() {
                if (activity.mIsPlaying) {
                    activity.mIsPlaying = false
                    trackViewEntity?.setPlaying(false)
                    activity.blurredImageView.isPlaying = false
                    activity.stopVideoAnimation()
                    trackViewEntity?.setCurrentCursurPosition(trackViewEntity?.getMaxTime() ?: 0)
                    trackViewEntity?.updateCursur(trackViewEntity?.getMaxTime() ?: 0)
                    try {
                        if (entityAudio_visible?.mediaPlayer?.isPlaying == true) {
                            entityAudio_visible?.mediaPlayer?.pause()
                        }
                        if (activity.mPlayer?.isPlaying == true) {
                            activity.mPlayer?.pause()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    startCursur = 0
                    current_position_time = 0
                    btnPlayPause?.setImageResource(R.drawable.play_arrow_24px)
                    activity.updateBtnToEnd()
                    activity.updateBtnToStart()
                }
            }
        })
        valueAnimator = animator
        animator.start()
        if (activity.mTemplate?.isVideoSquare == true) {
            activity.startVideoAnimation()
        }
    }

    fun startTimelineAnimationPreview(entityAudio: EntityAudio) {
        val maxTime = trackViewEntity?.getMaxTime() ?: return
        val timeLineW = trackViewEntity?.getTimeLineW() ?: return

        val animator = SmoothTimelineAnimator(startCursur, maxTime, object : SmoothTimelineAnimator.AnimatorListener {
            override fun onUpdate(timeMs: Int) {
                if (!activity.mIsPlaying || timeMs == 0) return
                val progress = timeMs.toFloat() / maxTime
                activity.updateTime(timeMs)
                trackViewEntity?.updateCursur(progress * timeLineW)
                trackViewEntity?.updateCurrentCursurPosition(timeMs)
                try {
                    if (entityAudio.mediaPlayer != null && !entityAudio.mediaPlayer!!.isPlaying) {
                        val secInScreen = trackViewEntity?.getSecondInScreen() ?: 1f
                        val seekPos = (Math.abs(Math.round((trackViewEntity?.getCurrentPosition()?.div(secInScreen))?.times(1000f) ?: 0f)) - Math.abs(Math.round((entityAudio.getRect().left / secInScreen) * 1000f))).toInt()
                        if (seekPos <= entityAudio.mediaPlayer!!.duration) {
                            entityAudio.mediaPlayer!!.seekTo(seekPos)
                        }
                        entityAudio.mediaPlayer!!.start()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                updateStartViewTime(trackViewEntity?.getCurrentCursurPosition() ?: 0)
            }

            override fun onEnd() {
                if (activity.mIsPlaying) {
                    activity.mIsPlaying = false
                    trackViewEntity?.setPlaying(false)
                    activity.blurredImageView.isPlaying = false
                    activity.stopVideoAnimation()
                    try {
                        if (entityAudio.mediaPlayer?.isPlaying == true) {
                            entityAudio.mediaPlayer?.pause()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    startCursur = trackViewEntity?.getCurrentCursurPosition() ?: 0
                }
            }
        })
        valueAnimator = animator
        animator.start()
        if (activity.mTemplate?.isVideoSquare == true) {
            activity.startVideoAnimation()
        }
    }

    fun stop() {
        // Stop video frame animator if any
        // Handled by EngineActivity
    }

    fun updateBtnToStart() {
        val pos = trackViewEntity?.getCurrentCursurPosition() ?: 0
        if (pos == 0) {
            btnToStart?.setColorFilter(-8355712, PorterDuff.Mode.SRC_IN)
            btnToStart?.isClickable = false
        } else {
            btnToStart?.setColorFilter(-1, PorterDuff.Mode.SRC_IN)
            btnToStart?.isClickable = true
        }
    }

    fun updateBtnToEnd() {
        val pos = trackViewEntity?.getCurrentCursurPosition() ?: 0
        val max = trackViewEntity?.getMaxTime() ?: 0
        if (pos == max) {
            btnToEnd?.setColorFilter(-8355712, PorterDuff.Mode.SRC_IN)
            btnToEnd?.isClickable = false
        } else {
            btnToEnd?.setColorFilter(-1, PorterDuff.Mode.SRC_IN)
            btnToEnd?.isClickable = true
        }
    }

    fun updateBtnToEndAndStart() {
        btnToStart?.setColorFilter(-1, PorterDuff.Mode.SRC_IN)
        btnToStart?.isClickable = true
        btnToEnd?.setColorFilter(-1, PorterDuff.Mode.SRC_IN)
        btnToEnd?.isClickable = true
    }

    fun enableUndoBtn() {
        try {
            val btn = btnUndo ?: return
            if (btn.isEnabled) return
            activity.runOnUiThread {
                btnUndo?.setColorFilter(-1, PorterDuff.Mode.SRC_IN)
                btnUndo?.isEnabled = true
                btnUndo?.isClickable = true
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun enableRedoBtn() {
        try {
            val btn = btnRedo ?: return
            if (btn.isEnabled) return
            activity.runOnUiThread {
                btnRedo?.setColorFilter(-1, PorterDuff.Mode.SRC_IN)
                btnRedo?.isEnabled = true
                btnRedo?.isClickable = true
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun disableUndoBtn() {
        try {
            val btn = btnUndo ?: return
            if (!btn.isEnabled) return
            activity.runOnUiThread {
                btnUndo?.setColorFilter(-8355712, PorterDuff.Mode.SRC_IN)
                btnUndo?.isEnabled = false
                btnUndo?.isClickable = false
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun disableRedoBtn() {
        try {
            val btn = btnRedo ?: return
            if (!btn.isEnabled) return
            activity.runOnUiThread {
                btnRedo?.setColorFilter(-8355712, PorterDuff.Mode.SRC_IN)
                btnRedo?.isEnabled = false
                btnRedo?.isClickable = false
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun updateStartViewTime(timeMs: Int) {
        val formatted = TimeFormatter.formatMs(timeMs.toLong())
        activity.tv_currentTime?.text = formatted
    }

    fun updateEndViewTime(timeMs: Int) {
        val formatted = TimeFormatter.formatMs(timeMs.toLong())
        activity.tv_endTime?.text = ":$formatted"
    }

    fun updateViewTime(maxTime: Int, cursorPos: Int) {
        updateStartViewTime(cursorPos)
        updateEndViewTime(maxTime)
    }

    fun updateTime() {
        trackViewEntity?.calculMaxTime()
        updateViewTime(trackViewEntity?.getMaxTime() ?: 0, trackViewEntity?.getCurrentCursurPosition() ?: 0)
        val pos = trackViewEntity?.getCurrentCursurPosition() ?: 0
        val max = trackViewEntity?.getMaxTime() ?: 1
        if (pos <= max) {
            activity.updateTime(pos)
            trackViewEntity?.setCurrentCursurPosition(pos)
        }
    }

    fun updateTimeToEndAya() {
        trackViewEntity?.calculMaxTime()
        updateViewTime(trackViewEntity?.getMaxTime() ?: 0, trackViewEntity?.getCurrentCursurPosition() ?: 0)
        val pos = trackViewEntity?.getCurrentCursurPosition() ?: 0
        val max = trackViewEntity?.getMaxTime() ?: 1
        if (pos <= max) {
            activity.updateTime(pos)
            trackViewEntity?.setCurrentCursurPosition(pos)
        }
    }

    fun onSeekPlayer(position: Float) {
        try {
            isOnScroll = true
            for (ea in trackViewEntity?.getEntityListAudio() ?: emptyList()) {
                try {
                    if (ea.mediaPlayer?.isPlaying == true) {
                        ea.mediaPlayer?.pause()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            if (activity.mIsPlaying) {
                btnPlayPause?.setImageResource(R.drawable.play_arrow_24px)
                activity.mIsPlaying = false
                trackViewEntity?.setPlaying(false)
                activity.blurredImageView.isPlaying = false
            }
            pauseTimelineAnimation()
            activity.stopVideoAnimation()
            val secondInScreen = trackViewEntity?.getSecondInScreen() ?: 1f
            val round = Math.round(Math.abs((position / secondInScreen) * -1000f))
            val maxTime = trackViewEntity?.getMaxTime() ?: 0
            if (round <= maxTime) {
                activity.updateTime(round)
            }
            trackViewEntity?.updateCurrentCursurPosition(round)
            current_position_time = System.currentTimeMillis().toInt()
            startCursur = trackViewEntity?.getCurrentCursurPosition() ?: 0
            updateViewTime(maxTime, trackViewEntity?.getCurrentCursurPosition() ?: 0)
            activity.updateBtnCutState()
            activity.updateBtnToStart()
            activity.updateBtnToEnd()
            activity.updateFrame()
        } catch (_: Exception) {}
    }

    fun clearCallbacks() {
        valueAnimator?.stop()
        valueAnimator = null
        entityAudio_visible = null
        entityAudio_player = null
    }
}
