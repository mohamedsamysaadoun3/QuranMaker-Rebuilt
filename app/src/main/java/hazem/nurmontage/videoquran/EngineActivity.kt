package hazem.nurmontage.videoquran

import android.content.Intent
import android.os.Bundle
import hazem.nurmontage.videoquran.common.Common

/**
 * STUB: EngineActivity - Main video editor
 * This is a placeholder for Phase 7. The original EngineActivity is ~8000 lines.
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
        // TODO: Phase 7 - Full EngineActivity implementation (~8000 lines)
        // For now, just finish and go back
        finish()
    }
}
