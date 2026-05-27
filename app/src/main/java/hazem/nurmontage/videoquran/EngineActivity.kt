package hazem.nurmontage.videoquran

import android.graphics.Color
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.widget.AppCompatTextView
import hazem.nurmontage.videoquran.views.TextCustumFont

/**
 * STUB: EngineActivity - Main video editor
 * This is a placeholder for Phase 7. The original EngineActivity is ~8000 lines.
 * Currently shows a placeholder screen instead of closing the app.
 */
class EngineActivity : BaseActivity() {

    companion object {
        const val EXTRA_TEMPLATE = "template"
        const val EXTRA_FROM_SETTING = "from_setting"
        const val EXTRA_AUDIO_PATH = "audio"
        const val EXTRA_PATH_VIDEO_COPY = "path_video_copy"
        const val EXTRA_READER_NAME = "name"
        const val EXTRA_TYPE_SHARE = "type_share"
        const val EXTRA_PATH_SHARE = "path_share"
        const val EXTRA_SURAH_INDEX = "surah_index"
        const val EXTRA_FROM_LANG = "from_lang"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Placeholder UI - will be replaced in Phase 7
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor("#1A1A2E"))
            setPadding(64, 128, 64, 64)
        }

        val title = TextCustumFont(this).apply {
            text = "NurMontage"
            setTextColor(Color.WHITE)
            textSize = 28f
        }

        val subtitle = TextCustumFont(this).apply {
            text = "محرك الفيديو قيد التطوير"
            setTextColor(Color.parseColor("#B0BEC5"))
            textSize = 16f
            setPadding(0, 32, 0, 0)
        }

        val phase = TextCustumFont(this).apply {
            text = "Phase 7 — قادم قريباً"
            setTextColor(Color.parseColor("#7C4DFF"))
            textSize = 14f
            setPadding(0, 16, 0, 0)
        }

        val progress = ProgressBar(this).apply {
            setPadding(0, 48, 0, 0)
        }

        layout.addView(title)
        layout.addView(subtitle)
        layout.addView(phase)
        layout.addView(progress)
        setContentView(layout)
    }
}
