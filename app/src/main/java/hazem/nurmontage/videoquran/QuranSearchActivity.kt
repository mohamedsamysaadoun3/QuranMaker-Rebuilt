package hazem.nurmontage.videoquran

import android.content.Context
import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import hazem.nurmontage.videoquran.Utils.JavaBM
import hazem.nurmontage.videoquran.Utils.LocaleHelper
import hazem.nurmontage.videoquran.Utils.QuranPreference
import hazem.nurmontage.videoquran.Utils.RemoveTashkeel
import hazem.nurmontage.videoquran.Utils.Utils
import hazem.nurmontage.videoquran.adabter.SearchQuranAdabters
import hazem.nurmontage.videoquran.model.ItemQuranSearch
import hazem.nurmontage.videoquran.views.ButtonCustumFont
import hazem.nurmontage.videoquran.views.TextCustumFont
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader

/**
 * Activity for searching the Quran text.
 * Supports searching by surah name, surah + aya number, or by arbitrary text.
 * Uses Boyer-Moore algorithm for efficient Arabic text search.
 */
class QuranSearchActivity : BaseActivity() {

    private lateinit var btnDone: ButtonCustumFont
    private lateinit var countAya: TextView
    private lateinit var editText: EditText
    private var indexAya: Int = 0
    @Volatile
    private var indexSurah: Int = 0
    private var isFullSurah = false
    @Volatile
    private var isRun = false
    private var javaBM: JavaBM? = null
    private var lastKey: String? = null
    private var lastSearchKey: String? = null
    private var mTo: Int = -1
    private lateinit var recyclerView: RecyclerView
    private var runnableBySurah: Runnable? = null
    private lateinit var searchProgressBar: ProgressBar
    private var searchQuranAdapter: SearchQuranAdabters? = null
    private lateinit var surahNames: Array<String>
    private var thread: Thread? = null
    private var mFrom: Int = -1
    private val handler = Handler(Looper.getMainLooper())
    private var inQuran: InputStream? = null
    private var bufferedReaderQuran: BufferedReader? = null

    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            closeKeyboard()
            setResult(RESULT_CANCELED)
            finish()
        }
    }

    private val iSearchQuranCallback = object : SearchQuranAdabters.ISearchQuranCallback {
        override fun onClick(from: Int, to: Int, item: ItemQuranSearch) {
            indexSurah = item.surahIndex
            if (!isFullSurah) {
                val ayaTo = item.to - 1
                mTo = ayaTo
                mFrom = ayaTo
                btnDone.performClick()
                return
            }
            mFrom = from
            mTo = to
            if (btnDone.visibility != View.VISIBLE) {
                btnDone.visibility = View.VISIBLE
            }
            btnDone.text = resources.getString(R.string.from_to, from + 1, to + 1)
        }
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
        setContentView(R.layout.activity_quran_search)

        setStatusBarColor(-15658732)
        setNavigationBarColor(-15658732)

        val insetsController = WindowCompat.getInsetsController(window, window.decorView)
        insetsController.isAppearanceLightStatusBars = false
        insetsController.isAppearanceLightNavigationBars = false

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { view, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(insets.left, insets.top, insets.right, insets.bottom)
            windowInsets
        }

        surahNames = resources.getStringArray(R.array.sura_names)

        findViewById<View>(R.id.btn_onBack).setOnClickListener {
            closeKeyboard()
            setResult(RESULT_CANCELED)
            finish()
        }

        val btnDoneView = findViewById<ButtonCustumFont>(R.id.btn_done)
        btnDone = btnDoneView
        btnDone.setOnClickListener {
            if (mFrom == -1) return@setOnClickListener
            QuranPreference.savePreferencesSearch(
                this,
                indexSurah,
                mFrom,
                mTo,
                editText.text.toString()
            )
            setResult(RESULT_OK)
            finish()
        }

        searchProgressBar = findViewById(R.id.progress)
        countAya = findViewById(R.id.tv_count_aya)
        (findViewById<TextCustumFont>(R.id.tv_tittle)).text = resources.getString(R.string.search)

        val rvSearch = findViewById<RecyclerView>(R.id.rv_search_quran)
        recyclerView = rvSearch
        rvSearch.setHasFixedSize(true)
        rvSearch.layoutManager = LinearLayoutManager(applicationContext)
        rvSearch.setItemViewCacheSize(20)
        @Suppress("DEPRECATION")
        rvSearch.isDrawingCacheEnabled = true
        rvSearch.itemAnimator = null
        @Suppress("DEPRECATION")
        rvSearch.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH)

        val adapter = SearchQuranAdabters(iSearchQuranCallback)
        searchQuranAdapter = adapter
        rvSearch.adapter = adapter

        val edtSearch = findViewById<EditText>(R.id.edt_search_quran)
        editText = edtSearch
        edtSearch.hint = resources.getString(R.string.hint_search_quran)
        edtSearch.typeface = Typeface.createFromAsset(resources.assets, "fonts/ReadexPro_Medium.ttf")
        edtSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId != KeyEvent.KEYCODE_ENDCALL) return@setOnEditorActionListener false
            closeKeyboard()
            try {
                performSearch()
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
            true
        }

        findViewById<View>(R.id.btn_search).setOnClickListener {
            closeKeyboard()
            try {
                performSearch()
            } catch (e: InterruptedException) {
                Thread.currentThread().interrupt()
                e.printStackTrace()
            }
        }

        lastSearch()
    }

    private fun lastSearch() {
        val lastSearch = QuranPreference.getLastSearch(this)
        if (lastSearch == null || TextUtils.isEmpty(lastSearch)) return
        try {
            editText.setText(lastSearch)
            performSearch()
        } catch (e: InterruptedException) {
            Thread.currentThread().interrupt()
            e.printStackTrace()
        }
    }

    override fun onResume() {
        super.onResume()
        editText.requestFocus()
        showKeyboard()
    }

    private fun updateCount() {
        countAya.text = "الآيـــات : (${searchQuranAdapter?.getSize() ?: 0})"
    }

    override fun onPause() {
        closeKeyboard()
        super.onPause()
    }

    fun showKeyboard() {
        try {
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)
        } catch (_: Exception) {
        }
    }

    fun closeKeyboard() {
        try {
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(editText.windowToken, 0)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun resetFromTo() {
        mFrom = -1
        mTo = -1
        btnDone.visibility = View.GONE
    }

    @Throws(InterruptedException::class)
    private fun performSearch() {
        resetFromTo()
        val trim = editText.text.toString().trim()
        if (trim.isEmpty() || trim == "--" || !Utils.isProbablyArabic(trim)) {
            return
        }
        if (lastSearchKey == null || lastSearchKey != trim) {
            lastSearchKey = trim.replace("\"", "")
            searchQuranAdapter?.clear()

            val split = lastSearchKey!!.split(" ")
            if (split.size == 1) {
                val surahIdx = if (lastSearchKey!!.contains("عمران")) 3 else getIndexSurah(split[0])
                if (surahIdx != -1) {
                    indexSurah = surahIdx
                    indexAya = -1
                    searchBySurah()
                    return
                }
            } else if (split.size == 2) {
                val surahIdx = if (lastSearchKey!!.contains("عمران")) 3 else getIndexSurah(split[0])
                try {
                    val ayaNum = split[1].toInt()
                    if (surahIdx != -1) {
                        indexSurah = surahIdx
                        indexAya = ayaNum
                        searchBySurah()
                        return
                    }
                } catch (_: NumberFormatException) {
                }
            }

            isFullSurah = false
            if (javaBM == null) {
                javaBM = JavaBM()
            }
            javaBM!!.setmPattern(RemoveTashkeel.remove(lastSearchKey!!))
            searchAllQuran()
        }
    }

    private fun normalizeArabic(str: String?): String {
        if (str == null) return ""
        var result = str.trim()
        if (result.startsWith("ال")) {
            result = result.substring(2)
        }
        return result
            .replace("أ", "ا")
            .replace("إ", "ا")
            .replace("آ", "ا")
            .replace("ى", "ي")
            .replace("ة", "ه")
            .replace(Regex("[\\u064B-\\u065F]"), "")
    }

    private fun getIndexSurah(str: String): Int {
        val normalized = normalizeArabic(str)
        for (i in surahNames.indices) {
            if (normalizeArabic(surahNames[i].split("-")[0].trim()).contains(normalized)) {
                return i
            }
        }
        return -1
    }

    @Throws(InterruptedException::class)
    private fun searchAllQuran() {
        stopCurrentSearchThread()
        isRun = true
        val searchThread = Thread { performSearchAllQuran() }
        thread = searchThread
        searchThread.start()
    }

    private fun performSearchAllQuran() {
        handler.post {
            searchProgressBar.visibility = View.VISIBLE
        }
        try {
            try {
                inQuran = assets.open("quran/quran-simple.txt")
                bufferedReaderQuran = BufferedReader(InputStreamReader(inQuran))

                var line: String = ""
                while (isRun && bufferedReaderQuran!!.readLine().also { line = it } != null && !line.isNullOrEmpty()) {
                    val split = line.split("\\|".toRegex())
                    if (split.size >= 3) {
                        val surahIdx = split[0].toInt() - 1
                        val ayaIdx = split[1].toInt() - 1
                        val ayaText = split[2]

                        val textToSearch = RemoveTashkeel.remove(
                            if (surahIdx > 0 && ayaIdx == 0 && ayaText.contains("بِّسْمِ اللَّهِ"))
                                ayaText.substring(40)
                            else
                                ayaText
                        )

                        val matchIndex = javaBM!!.match(textToSearch)
                        if (matchIndex != -1) {
                            val finalSurahIdx = surahIdx
                            val finalAyaIdx = ayaIdx
                            val finalAyaText = ayaText
                            val finalTextToSearch = textToSearch
                            val finalMatchIndex = matchIndex
                            handler.post {
                                if (searchQuranAdapter != null) {
                                    val countIndex = Utils.countIndex(
                                        Utils.countSpace(finalMatchIndex, finalTextToSearch),
                                        finalAyaText
                                    )
                                    searchQuranAdapter!!.add(
                                        ItemQuranSearch(
                                            finalAyaText,
                                            surahNames[finalSurahIdx],
                                            finalAyaIdx + 1,
                                            finalSurahIdx,
                                            countIndex,
                                            Utils.countIndex(
                                                countIndex,
                                                Utils.countSpace(javaBM!!.getmPattern()),
                                                finalAyaText
                                            )
                                        )
                                    )
                                    updateCount()
                                }
                            }
                        }
                    }
                }
                closeQuranStreams()
            } catch (e: IOException) {
                System.err.println("Error reading Quran file: ${e.message}")
                e.printStackTrace()
                closeQuranStreams()
            }
        } catch (th: Throwable) {
            closeQuranStreams()
            handler.post {
                onSearchComplete()
            }
            throw th
        }
        handler.post {
            onSearchComplete()
        }
    }

    private fun onSearchComplete() {
        if (searchQuranAdapter != null && searchQuranAdapter!!.getSize() == 0) {
            updateCount()
        }
        if (::searchProgressBar.isInitialized) {
            searchProgressBar.visibility = View.GONE
        }
    }

    private fun closeQuranStreams() {
        try {
            bufferedReaderQuran?.close()
            inQuran?.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @Throws(InterruptedException::class)
    private fun searchBySurah() {
        isFullSurah = true
        if (runnableBySurah == null) {
            runnableBySurah = Runnable {
                handler.post {
                    if (::searchProgressBar.isInitialized) {
                        searchProgressBar.visibility = View.VISIBLE
                    }
                }
                try {
                    try {
                        inQuran = assets.open("quran/quran-simple.txt")
                        bufferedReaderQuran = BufferedReader(InputStreamReader(inQuran))

                        var line: String = ""
                        while (isRun && bufferedReaderQuran!!.readLine().also { line = it } != null && !line.isNullOrEmpty()) {
                            val split = line.split("\\|".toRegex())
                            if (split.size < 3) break

                            val surahIdx = split[0].toInt() - 1
                            val ayaNum = split[1].toInt()

                            if (surahIdx == indexSurah && (indexAya == -1 || indexAya == ayaNum)) {
                                val finalAyaText = split[2]
                                val finalAyaNum = ayaNum
                                val finalSurahIdx = surahIdx
                                handler.post {
                                    if (searchQuranAdapter != null) {
                                        searchQuranAdapter!!.add(
                                            ItemQuranSearch(
                                                finalAyaText,
                                                surahNames[indexSurah],
                                                finalAyaNum,
                                                finalSurahIdx,
                                                -1,
                                                -1
                                            )
                                        )
                                        updateCount()
                                    }
                                }
                                if (indexAya != -1) break
                            }
                        }
                        bufferedReaderQuran?.close()
                        inQuran?.close()
                    } catch (e: IOException) {
                        e.printStackTrace()
                        try {
                            bufferedReaderQuran?.close()
                            inQuran?.close()
                        } catch (e2: IOException) {
                            e2.printStackTrace()
                        }
                        return@Runnable
                    }
                } catch (e: IOException) {
                    try {
                        System.out.println(e)
                        bufferedReaderQuran?.close()
                        inQuran?.close()
                    } catch (e2: IOException) {
                        e2.printStackTrace()
                    }
                }

                handler.post {
                    if (searchQuranAdapter != null && searchQuranAdapter!!.getSize() == 0) {
                        updateCount()
                    }
                    if (::searchProgressBar.isInitialized) {
                        searchProgressBar.visibility = View.GONE
                    }
                }
            }
        }

        // Stop any running thread first
        thread?.let {
            try {
                isRun = false
                it.join()
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }

        isRun = true
        val searchThread = Thread(runnableBySurah)
        thread = searchThread
        searchThread.start()
    }

    private fun stopCurrentSearchThread() {
        thread?.let {
            try {
                isRun = false
                it.join()
            } catch (e: InterruptedException) {
                Thread.currentThread().interrupt()
                e.printStackTrace()
            }
        }
    }
}
