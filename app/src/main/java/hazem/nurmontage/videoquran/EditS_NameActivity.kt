package hazem.nurmontage.videoquran

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.CompoundButton
import androidx.activity.OnBackPressedCallback
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import hazem.nurmontage.videoquran.Utils.LocaleHelper
import hazem.nurmontage.videoquran.Utils.Utils
import hazem.nurmontage.videoquran.adabter.ColorBgAdabter
import hazem.nurmontage.videoquran.common.Common
import hazem.nurmontage.videoquran.constant.SurahNameStyle
import hazem.nurmontage.videoquran.views.CheckboxCustumFont
import hazem.nurmontage.videoquran.views.EditTextCustumFont
import hazem.nurmontage.videoquran.views.TextCustumFont
import hazem.nurmontage.videoquran.views.TextCustumFontBold

/**
 * Activity for editing the surah name style and reader name.
 * Allows choosing between plain text and decorative (Zaghrafat) surah name styles,
 * editing the reader name text, and selecting a background color.
 */
class EditS_NameActivity : BaseActivity() {

    // TODO: ColorBgAdabter needs to be converted to Kotlin
    private var adapter: ColorBgAdabter? = null
    private lateinit var checkBg: CheckboxCustumFont
    private var clrBg: Int = ViewCompat.MEASURED_STATE_MASK
    private lateinit var editText: EditTextCustumFont
    private var indexSurah: Int = 0
    private lateinit var recyclerView: RecyclerView
    private var style: Int = 0
    private lateinit var tvOption1: TextCustumFont
    private lateinit var tvOption2: TextCustumFont

    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            closeKeyboard()
            setResult(RESULT_CANCELED, null)
            @Suppress("DEPRECATION")
            overridePendingTransition(0, 0)
            finish()
        }
    }

    private val bgColorArray = intArrayOf(
        -8388608, -1, ViewCompat.MEASURED_STATE_MASK,
        -2838729, -16777088, -16694239, -13220529, -9404272
    )

    private val iColor = ColorBgAdabter.IColor { color, _ ->
        clrBg = color
        scrollToSelectedPosition()
    }

    // ── Lifecycle ──────────────────────────────────────────────────────

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_sname)

        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)

        val insetsController = WindowCompat.getInsetsController(window, window.decorView)
        insetsController.isAppearanceLightStatusBars = false
        insetsController.isAppearanceLightNavigationBars = false

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { view, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(insets.left, insets.top, insets.right, insets.bottom)
            windowInsets
        }

        setStatusBarColor(-15658735)
        setNavigationBarColor(-14935010)

        val resources = resources ?: run { finish(); return }

        val tvTitle = findViewById<TextCustumFontBold>(R.id.tv_tittle)
        val tvReaderName = findViewById<TextCustumFontBold>(R.id.tv_reader_name)
        val tvAddBg = findViewById<TextCustumFontBold>(R.id.tv_add_bg)

        tvAddBg.setText(R.string.add_bg)
        tvTitle.setText(R.string.edit_and_style)
        tvReaderName.setText(R.string.reader_name)

        checkBg = findViewById(R.id.checkbox_bg)
        editText = findViewById<EditTextCustumFont>(R.id.edt_reader).also { it.requestFocus() }

        // Read intent extras
        val readerName = intent.getStringExtra("reader_name")
        val surahName = intent.getStringExtra("surah_name")
        style = intent.getIntExtra("style", 0)
        clrBg = intent.getIntExtra("clrBg", ViewCompat.MEASURED_STATE_MASK)
        checkBg.isChecked = intent.getBooleanExtra("isBg", false)

        var idx = intent.getIntExtra("index", 0)
        indexSurah = idx
        if (idx == 0) {
            indexSurah = findWordIndexLoop(surahName)
        }

        if (!surahName.isNullOrBlank() && surahName.length > 3) {
            editText.setText(readerName)
        }

        showKeyboard()

        // Surah name style options
        tvOption1 = findViewById(R.id.tv_option_1)
        tvOption2 = findViewById(R.id.tv_option_2)
        tvOption1.text = surahName
        tvOption2.typeface = Typeface.createFromAsset(assets, "fonts/surah_name.otf")

        val formattedSurah = when {
            indexSurah < 10  -> "00${indexSurah}sura"
            indexSurah < 100 -> "0${indexSurah}sura"
            else             -> "${indexSurah}sura"
        }
        tvOption2.text = formattedSurah

        if (style == 1) {
            selectOption(tvOption2, tvOption1)
        }

        tvOption1.setOnClickListener {
            style = SurahNameStyle.NONE.ordinal
            selectOption(tvOption1, tvOption2)
        }

        tvOption2.setOnClickListener {
            style = SurahNameStyle.ZAGHRAFAT.ordinal
            selectOption(tvOption2, tvOption1)
        }

        // Back button
        findViewById<View>(R.id.btn_on_back).setOnClickListener {
            onBackPressedCallback.handleOnBackPressed()
        }

        // Done button
        findViewById<View>(R.id.btn_done).setOnClickListener {
            Intent().apply {
                putExtra(Common.READER, editText.text?.toString() ?: "")
                putExtra("style", style)
                putExtra("index", indexSurah)
                putExtra("isBg", checkBg.isChecked)
                putExtra("clrBg", clrBg)
                setResult(RESULT_OK, this)
            }
            @Suppress("DEPRECATION")
            overridePendingTransition(0, 0)
            finish()
        }

        // Toggle background checkbox when label clicked
        tvAddBg.setOnClickListener {
            checkBg.isChecked = !checkBg.isChecked
        }

        initRv()

        checkBg.setOnCheckedChangeListener { _: CompoundButton, isChecked: Boolean ->
            updateColorUI(isChecked)
        }

        updateColorUI(checkBg.isChecked)
    }

    override fun onPause() {
        closeKeyboard()
        super.onPause()
    }

    // ── RecyclerView ───────────────────────────────────────────────────

    private fun initRv() {
        recyclerView = findViewById(R.id.rv_color)
        adapter = ColorBgAdabter(
            iColor,
            bgColorArray,
            Utils.indexOf(bgColorArray, clrBg)
        )
        recyclerView.layoutManager = LinearLayoutManager(
            this,
            RecyclerView.HORIZONTAL,
            LocaleHelper.getLanguage(this) == "ar"
        )
        recyclerView.itemAnimator = null
        recyclerView.setHasFixedSize(true)
        recyclerView.adapter = adapter
        scrollToSelectedPosition()
    }

    private fun updateColorUI(enabled: Boolean) {
        recyclerView.isEnabled = enabled
        recyclerView.animate()
            .alpha(if (enabled) 1.0f else 0.4f)
            .setDuration(180L)
            .start()
        adapter?.setEnabled(enabled)
    }

    fun scrollToSelectedPosition() {
        val layoutManager = recyclerView.layoutManager as? LinearLayoutManager ?: return
        layoutManager.scrollToPositionWithOffset(
            adapter?.posSelect ?: 0,
            (recyclerView.width / 2) - 50
        )
    }

    // ── Style option selection ─────────────────────────────────────────

    private fun selectOption(selected: TextCustumFont, unselected: TextCustumFont) {
        selected.setBackgroundResource(R.drawable.bg_option_surah_selected)
        selected.setTextColor(Color.WHITE)
        unselected.setBackgroundResource(R.drawable.bg_option_surah_unselected)
        unselected.setTextColor(Color.parseColor("#888888"))
    }

    // ── Surah index lookup ─────────────────────────────────────────────

    fun findWordIndexLoop(str: String?): Int {
        val stringArray = resources.getStringArray(R.array.surah_names_merged)
        if (str == null) return -1
        for (i in stringArray.indices) {
            if (str.contains(stringArray[i])) {
                return i + 1
            }
        }
        return -1
    }

    // ── Keyboard helpers ───────────────────────────────────────────────

    fun showKeyboard() {
        try {
            (getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager)
                .showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)
        } catch (_: Exception) {
        }
    }

    fun closeKeyboard() {
        try {
            (getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager)
                .hideSoftInputFromWindow(editText.windowToken, 0)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
