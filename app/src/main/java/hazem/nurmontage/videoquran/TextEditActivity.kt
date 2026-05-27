package hazem.nurmontage.videoquran

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import hazem.nurmontage.videoquran.Utils.LocaleHelper
import hazem.nurmontage.videoquran.Utils.WordProcessor
import hazem.nurmontage.videoquran.adabter.WordAyaAdabter
import hazem.nurmontage.videoquran.model.WordModel
import hazem.nurmontage.videoquran.views.ButtonCustumFont

class TextEditActivity : BaseActivity() {

    private var endIndex: Int = 0
    private var startIndex: Int = 0
    private var wordAyaAdabter: WordAyaAdabter? = null

    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            setResult(RESULT_OK, null)
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_text_edit)
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)

        // -13421771 = 0xFF333333
        setStatusBarColor(-13421771)
        setNavigationBarColor(-13421771)

        val insetsController = WindowCompat.getInsetsController(window, window.decorView)
        insetsController.isAppearanceLightStatusBars = false
        insetsController.isAppearanceLightNavigationBars = false

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { view, windowInsetsCompat ->
            val insets: Insets = windowInsetsCompat.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(insets.left, insets.top, insets.right, insets.bottom)
            windowInsetsCompat
        }

        findViewById<View>(R.id.btn_cancel).setOnClickListener {
            onBackPressedCallback.handleOnBackPressed()
        }

        val btnDone = findViewById<ButtonCustumFont>(R.id.btn_done)
        btnDone.text = resources.getString(R.string.done)
        btnDone.setOnClickListener {
            val intent = Intent()
            val selectedAya = getSelectedAya()
            val firstDigitIndex = findFirstDigitIndex(selectedAya)

            intent.putExtra("start_index", startIndex)
            intent.putExtra("end_index", endIndex)

            if (firstDigitIndex == -1) {
                intent.putExtra("aya", selectedAya)
            } else {
                val substring = selectedAya.substring(0, firstDigitIndex)
                try {
                    var number = selectedAya.substring(firstDigitIndex).toInt()
                    if (number > 286) {
                        number = 286
                    }
                    intent.putExtra("number", number)
                    intent.putExtra("index", firstDigitIndex)
                    intent.putExtra("aya", substring + " نص")
                } catch (e: Exception) {
                    intent.putExtra("aya", substring)
                }
            }

            setResult(RESULT_OK, intent)
            finish()
        }

        val aya = intent.getStringExtra("aya")
        val completeAya = intent.getStringExtra("complete_aya")
        startIndex = intent.getIntExtra("start_index", -1)
        endIndex = intent.getIntExtra("end_index", -1)

        if (completeAya != null) {
            init(aya, completeAya)
        }
    }

    private fun getSelectedAya(): String {
        val sb = StringBuilder()
        val list = wordAyaAdabter?.list ?: return ""
        startIndex = -1
        var count = 0

        for (i in list.indices) {
            val wordModel = list[i]
            if (wordModel.isSelected) {
                if (startIndex == -1) {
                    startIndex = i
                }
                count++
                sb.append(wordModel.w).append(" ")
            }
        }

        val selectedStartIndex = startIndex
        endIndex = count + selectedStartIndex

        if (selectedStartIndex != -1) {
            startIndex = WordProcessor.mapIndexAfterGroupReverse(selectedStartIndex, 4, list.size)
            endIndex = WordProcessor.mapIndexAfterGroupReverse(endIndex, 4, list.size)
        }

        return sb.toString().trim()
    }

    private fun init(aya: String?, completeAya: String) {
        val wordProcessor = WordProcessor
        val split = completeAya.trim().split("\\s+".toRegex())
        val arrayList = ArrayList<WordModel>()

        if (startIndex == endIndex) {
            val splitAya = aya?.trim()?.split("\\s+".toRegex()) ?: emptyList()
            val indexOf = completeAya.indexOf(aya ?: "")
            var atStart = indexOf == 0
            var charIndex = 0
            var ayaWordIndex = 0

            for (word in split) {
                if (!atStart) {
                    if (charIndex == indexOf) {
                        atStart = true
                    }
                    charIndex += word.length + 1
                }

                if (atStart && ayaWordIndex < splitAya.size) {
                    val isSelected = word == splitAya[ayaWordIndex]
                    arrayList.add(WordModel(word, isSelected))
                    if (isSelected) {
                        ayaWordIndex++
                    }
                } else {
                    arrayList.add(WordModel(word, false))
                }
            }
        } else {
            for (i in split.indices) {
                arrayList.add(WordModel(split[i], i >= startIndex && i < endIndex))
            }
        }

        wordAyaAdabter = WordAyaAdabter(wordProcessor.reverseInGroupsOfFour(arrayList))

        val recyclerView = findViewById<RecyclerView>(R.id.rv)
        recyclerView.layoutManager = GridLayoutManager(this, 4)
        recyclerView.setHasFixedSize(true)
        recyclerView.itemAnimator = null
        recyclerView.adapter = wordAyaAdabter
    }

    companion object {
        fun findFirstDigitIndex(str: String?): Int {
            if (str.isNullOrEmpty()) return -1
            for (i in str.indices) {
                if (Character.isDigit(str[i])) {
                    return i
                }
            }
            return -1
        }
    }
}
