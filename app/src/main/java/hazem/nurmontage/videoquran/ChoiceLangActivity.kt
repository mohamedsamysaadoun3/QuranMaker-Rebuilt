package hazem.nurmontage.videoquran

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import hazem.nurmontage.videoquran.Utils.LocaleHelper

class ChoiceLangActivity : BaseActivity() {

    private var isFromSetting: Boolean = false
    private var lang: String = "en"
    private var isEnglishSelected: Boolean = true

    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            toStarWork()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_choice_lang)
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)

        setStatusBarColor(ViewCompat.MEASURED_STATE_MASK)
        setNavigationBarColor(ViewCompat.MEASURED_STATE_MASK)

        val insetsController = WindowCompat.getInsetsController(window, window.decorView)
        insetsController.isAppearanceLightStatusBars = false
        insetsController.isAppearanceLightNavigationBars = false

        if (intent != null) {
            isFromSetting = intent.getBooleanExtra("from_setting", false)
        }

        initViews()
    }

    private fun initViews() {
        val layoutEnglish = findViewById<RelativeLayout>(R.id.layout_english)
        val layoutArabic = findViewById<RelativeLayout>(R.id.layout_arabic)
        val radioEnglish = findViewById<ImageView>(R.id.radio_english)
        val radioArabic = findViewById<ImageView>(R.id.radio_arabic)
        val btnConfirm = findViewById<Button>(R.id.btn_confirm)
        val tvCancel = findViewById<TextView>(R.id.tv_cancel)

        btnConfirm.text = resources.getString(R.string.confirm)
        tvCancel.text = resources.getString(R.string.cancel)
        (findViewById<TextView>(R.id.tv_tittle)).text = resources.getString(R.string.select_language)
        (findViewById<TextView>(R.id.tv_subTittle)).text = resources.getString(R.string.choose_your_preferred_language)

        if ("ar" == LocaleHelper.getLanguage(this)) {
            layoutEnglish.setBackgroundResource(R.drawable.bg_item_unselected)
            layoutArabic.setBackgroundResource(R.drawable.bg_item_selected)
            radioArabic.setBackgroundResource(R.drawable.radio_selected)
            radioEnglish.setBackgroundResource(R.drawable.radio_unselected)
        }

        layoutEnglish.setOnClickListener {
            isEnglishSelected = true
            radioEnglish.setBackgroundResource(R.drawable.radio_selected)
            radioArabic.setBackgroundResource(R.drawable.radio_unselected)
            layoutEnglish.setBackgroundResource(R.drawable.bg_item_selected)
            layoutArabic.setBackgroundResource(R.drawable.bg_item_unselected)
        }

        layoutArabic.setOnClickListener {
            isEnglishSelected = false
            radioArabic.setBackgroundResource(R.drawable.radio_selected)
            radioEnglish.setBackgroundResource(R.drawable.radio_unselected)
            layoutArabic.setBackgroundResource(R.drawable.bg_item_selected)
            layoutEnglish.setBackgroundResource(R.drawable.bg_item_unselected)
        }

        btnConfirm.setOnClickListener {
            lang = if (isEnglishSelected) "en" else "ar"
            start()
        }

        tvCancel.setOnClickListener {
            toStarWork()
        }
    }

    private fun toStarWork() {
        val intent: Intent
        val sharedPreferences = getSharedPreferences("Template", MODE_PRIVATE)

        if (isFromSetting) {
            intent = Intent(this, SeettingActivity::class.java)
        } else {
            val all = sharedPreferences.all
            if (all != null && all.isNotEmpty()) {
                intent = Intent(this, WorkUserActivity::class.java)
            } else {
                intent = Intent(this, EngineActivity::class.java)
            }
        }

        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                Intent.FLAG_ACTIVITY_CLEAR_TOP or
                Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun start() {
        if (LocaleHelper.getLanguage(this) == lang) {
            startActivity(Intent(this, SeettingActivity::class.java))
            @Suppress("DEPRECATION")
            overridePendingTransition(0, 0)
            finish()
            return
        }

        LocaleHelper.persist(applicationContext, lang)
        LocaleHelper.onAttach(this)
        recreate()

        val intent = Intent(this, FullscreenActivity::class.java)
        intent.putExtra("from_setting", true)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                Intent.FLAG_ACTIVITY_CLEAR_TOP or
                Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
