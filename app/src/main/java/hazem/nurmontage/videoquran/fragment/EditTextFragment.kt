package hazem.nurmontage.videoquran.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import hazem.nurmontage.videoquran.R
import hazem.nurmontage.videoquran.adabter.WordAyaAdabter
import hazem.nurmontage.videoquran.databinding.FragmentEditTextBinding
import hazem.nurmontage.videoquran.entity_timeline.EntityQuranTimeline
import hazem.nurmontage.videoquran.model.QuranEntity
import hazem.nurmontage.videoquran.model.WordModel
import hazem.nurmontage.videoquran.views.ArrowOverlayDecoration

/**
 * Fragment for editing/selecting Quran text words.
 * Displays words from an ayah and allows the user to select which words to include.
 * Converted from Java decompiled source.
 */
class EditTextFragment() : Fragment() {

    companion object {
        var instance: EditTextFragment? = null

        fun getInstance(
            iEdiTextCallback: IEdiTextCallback,
            quranEntity: QuranEntity
        ): EditTextFragment {
            if (instance == null) {
                instance = EditTextFragment(iEdiTextCallback, quranEntity)
            }
            return instance!!
        }

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

    interface IEdiTextCallback {
        fun onDone(entityQuranTimeline: EntityQuranTimeline)
        fun onUpdate(quranEntity: QuranEntity)
    }

    private var fragmentBinding: FragmentEditTextBinding? = null
    private var iEditEntityCallback: IEdiTextCallback? = null
    private var iWordAya: WordAyaAdabter.IWordAya? = null
    private var quranEntity: QuranEntity? = null
    private var recyclerView: RecyclerView? = null
    private var wordAyaAdabter: WordAyaAdabter? = null


    constructor(iEdiTextCallback: IEdiTextCallback, quranEntity: QuranEntity) : this() {
        this.iEditEntityCallback = iEdiTextCallback
        this.quranEntity = quranEntity
        this.iWordAya = object : WordAyaAdabter.IWordAya {
            override fun onClick() {
                if (iEditEntityCallback != null) {
                    var selectedAya = getSelectedAya()
                    val findFirstDigitIndex = if (quranEntity.number != -1)
                        findFirstDigitIndex(selectedAya) else -1
                    if (findFirstDigitIndex != -1) {
                        val substring = selectedAya.substring(0, findFirstDigitIndex)
                        try {
                            var num = selectedAya.substring(findFirstDigitIndex).toInt()
                            if (num > 286) {
                                num = 286
                            }
                            quranEntity.number = num
                            quranEntity.indexNumber = findFirstDigitIndex
                            selectedAya = "$substring نص"
                        } catch (e: Exception) {
                            selectedAya = substring
                        }
                    }
                    quranEntity.txt = selectedAya
                    quranEntity.initPreset(quranEntity.getmPreset())
                    iEditEntityCallback!!.onUpdate(quranEntity)
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val inflate = FragmentEditTextBinding.inflate(inflater, container, false)
        fragmentBinding = inflate
        val root: LinearLayout = inflate.root

        if (quranEntity != null && iEditEntityCallback != null) {
            init(root)
            root.findViewById<View>(R.id.btn_close).setOnClickListener {
                iEditEntityCallback?.onDone(quranEntity!!.entityQuran!!)
            }
        }

        return root
    }

    private fun init(view: View) {
        val entity = quranEntity ?: return

        val txt: String = if (entity.indexNumber >= 0) {
            entity.txt.substring(0, minOf(entity.indexNumber, entity.txt.length)) + " " + entity.number
        } else {
            entity.txt
        }

        val completeAya = entity.complete_aya
        val startWordIndex = entity.startWord_index
        val endWordIndex = entity.endWord_index

        val split = completeAya.trim()
            .replace("\\s*([\\u06D6-\\u06ED])".toRegex(), "$1")
            .trim()
            .split("\\s+".toRegex())

        val wordList = ArrayList<WordModel>()

        if (startWordIndex == endWordIndex) {
            val split2 = txt.trim()
                .replace("\\s*([\\u06D6-\\u06ED])".toRegex(), "$1")
                .split("\\s+".toRegex())
            val indexOf = completeAya.indexOf(txt)
            var startFound = if (indexOf == 0) 1 else 0
            var charIndex = 0
            var matchIndex = 0

            for (word in split) {
                if (word != "-1") {
                    if (startFound == 0) {
                        if (charIndex == indexOf) {
                            startFound = 1
                        }
                        charIndex += word.length + 1
                    }
                    if (startFound != 0 && matchIndex < split2.size) {
                        val isEqual = word == split2[matchIndex]
                        wordList.add(WordModel(word, isEqual))
                        if (isEqual) {
                            matchIndex++
                        }
                    } else {
                        wordList.add(WordModel(word, false))
                    }
                }
            }
        } else {
            for (i in split.indices) {
                if (split[i] != "-1") {
                    wordList.add(WordModel(split[i], i >= startWordIndex && i < endWordIndex))
                }
            }
        }

        wordAyaAdabter = WordAyaAdabter(wordList, iWordAya!!)

        val rv: RecyclerView = view.findViewById(R.id.rv)
        recyclerView = rv
        rv.layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, true)
        rv.setHasFixedSize(true)
        rv.itemAnimator = null
        rv.adapter = wordAyaAdabter

        try {
            rv.scrollToPosition(startWordIndex)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        rv.addItemDecoration(ArrowOverlayDecoration(requireContext(), R.drawable.btn_on_back, 18))
    }

    fun update(quranEntity: QuranEntity?) {
        if (quranEntity == null) return
        this.quranEntity = quranEntity

        val txt: String = if (quranEntity.indexNumber >= 0) {
            quranEntity.txt.substring(0, minOf(quranEntity.indexNumber, quranEntity.txt.length)) + " " + quranEntity.number
        } else {
            quranEntity.txt
        }

        val completeAya = quranEntity.complete_aya
        val startWordIndex = quranEntity.startWord_index
        val endWordIndex = quranEntity.endWord_index

        val split = completeAya.trim()
            .replace("\\s*([\\u06D6-\\u06ED])".toRegex(), "$1")
            .trim()
            .split("\\s+".toRegex())

        val wordList = ArrayList<WordModel>()

        if (startWordIndex == endWordIndex) {
            val split2 = txt.trim()
                .replace("\\s*([\\u06D6-\\u06ED])".toRegex(), "$1")
                .split("\\s+".toRegex())
            val indexOf = completeAya.indexOf(txt)
            var startFound = if (indexOf == 0) 1 else 0
            var charIndex = 0
            var matchIndex = 0

            for (word in split) {
                if (word != "-1") {
                    if (startFound == 0) {
                        if (charIndex == indexOf) {
                            startFound = 1
                        }
                        charIndex += word.length + 1
                    }
                    if (startFound != 0 && matchIndex < split2.size) {
                        val isEqual = word == split2[matchIndex]
                        wordList.add(WordModel(word, isEqual))
                        if (isEqual) {
                            matchIndex++
                        }
                    } else {
                        wordList.add(WordModel(word, false))
                    }
                }
            }
        } else {
            for (i in split.indices) {
                if (split[i] != "-1") {
                    wordList.add(WordModel(split[i], i >= startWordIndex && i < endWordIndex))
                }
            }
        }

        wordAyaAdabter?.list = wordList

        try {
            recyclerView?.scrollToPosition(startWordIndex)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun getSelectedAya(): String {
        val sb = StringBuilder()
        val sbTranslation = StringBuilder()
        val list = wordAyaAdabter!!.list
        val split = quranEntity?.translation_complete?.split(",")?.toTypedArray()
        var firstIndex = -1
        var count = 0

        for (i in list!!.indices) {
            val wordModel = list!![i]
            if (wordModel.isSelected) {
                if (firstIndex == -1) {
                    firstIndex = i
                }
                count++
                sb.append(wordModel.w).append(" ")
                if (split != null && i < split.size) {
                    sbTranslation.append(split[i]).append(" ")
                }
            }
        }

        if (sbTranslation.isNotEmpty()) {
            quranEntity?.translation = sbTranslation.toString()
        } else {
            quranEntity?.translation = null
        }

        var endWordIndex = count + firstIndex
        if (quranEntity?.number != -1) {
            endWordIndex++
        }
        quranEntity?.endWord_index = endWordIndex
        quranEntity?.startWord_index = firstIndex

        return sb.toString().trim()
    }

    override fun onDestroyView() {
        fragmentBinding?.let { binding ->
            binding.root.removeAllViews()
        }
        fragmentBinding = null
        iWordAya = null
        instance = null
        super.onDestroyView()
    }
}
