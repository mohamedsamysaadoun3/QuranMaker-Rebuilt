package hazem.nurmontage.videoquran.fragment

import android.content.res.Resources
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.Spinner
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import hazem.nurmontage.videoquran.R
import hazem.nurmontage.videoquran.Utils.LocaleHelper
import hazem.nurmontage.videoquran.Utils.MyPrefereces
import hazem.nurmontage.videoquran.Utils.NetworkUtils
import hazem.nurmontage.videoquran.Utils.QuranPreference
import hazem.nurmontage.videoquran.Utils.QuranReader
import hazem.nurmontage.videoquran.adabter.IconQuranAdabters
import hazem.nurmontage.videoquran.databinding.FragmentAddQuranBinding
import hazem.nurmontage.videoquran.model.RecitersModel
import hazem.nurmontage.videoquran.views.CheckboxCustumFont
import hazem.nurmontage.videoquran.views.TextCustumFont

/**
 * Fragment for adding Quran ayahs to a project.
 * Features surah/ayah selection spinners, reciters, translations,
 * icon selection, and Bismilah inclusion toggle.
 * Converted from Java decompiled source.
 */
class AddQuranFragment() : Fragment() {

    companion object {
        var instance: AddQuranFragment? = null

        fun getInstance(
            iAddQuran: IAddQuran,
            resources: Resources,
            uri: Uri?,
            pathVideoCopy: String?,
            readerName: String?
        ): AddQuranFragment {
            if (instance == null) {
                instance = AddQuranFragment(iAddQuran, resources, uri, pathVideoCopy, readerName)
            }
            return instance!!
        }

        fun getInstance(
            iAddQuran: IAddQuran,
            resources: Resources
        ): AddQuranFragment {
            if (instance == null) {
                instance = AddQuranFragment(iAddQuran, resources)
            }
            return instance!!
        }
    }

    interface IAddQuran {
        fun onAdd(
            text: String, completeAya: String, translationWords: String?,
            translationComplete: String?, textLength: Int, ayaNumber: Int,
            icon: String, startWordIndex: Int, endWordIndex: Int
        )
        fun onAddReaderName(readerName: String, pathVideoCopy: String, uri: Uri)
        fun onAddTranslation(translation: String, ayaNumber: Int, isEnglish: Boolean)
        fun onBismilah()
        fun onCancel()
        fun onDone(
            surahHint: String, surahPosition: Int, readerName: String,
            uri: Uri, pathVideoCopy: String
        )
        fun onDone(
            surahHint: String, surahPosition: Int, readerName: String,
            recitersModels: List<RecitersModel>
        )
        fun onErrorLimitation()
        fun onSearch()
        fun onVuCopyRight()
        fun progress()
        fun uploadRecitation()
    }

    private var adapterFromAyah: ArrayAdapter<String>? = null
    private var adapterToAyah: ArrayAdapter<String>? = null
    private var arrayCount: IntArray = intArrayOf()
    private var arrayIdentifier: Array<String> = arrayOf()
    private var arrayReciters: Array<String> = arrayOf()
    private var arraySurah: Array<String> = arrayOf()
    private var arrayTranslation: Array<String> = arrayOf()
    private var fragmentBinding: FragmentAddQuranBinding? = null
    private var iAddQuran: IAddQuran? = null
    private var iconQuranAdabters: IconQuranAdabters? = null
    private var includeBismilah: CheckboxCustumFont? = null
    private var isFromSearch: Boolean = false
    private var isFromSelectReciters: Boolean = true
    private var ivDoneUpload: ImageView? = null
    private var layoutConnection: LinearLayout? = null
    private var pathVideoCopy: String? = null
    private var readerName: String? = null
    private var res: Resources? = null
    private var spinnerFrom: Spinner? = null
    private var spinnerReciters: Spinner? = null
    private var spinnerSurah: Spinner? = null
    private var spinnerTo: Spinner? = null
    private var spinnerTranslation: Spinner? = null
    private var surahHint: String? = null
    private var tvReaderName: TextCustumFont? = null
    private var uriRecitation: Uri? = null

    private var icon: String = "hafes"
    private var recitersModels: MutableList<RecitersModel> = mutableListOf()
    private var currentPos: Int = -1
    private val translationName = arrayOf(
        "en.hilali.txt", "fr.hamidullah.txt", "ur.maududi.txt", "tr.ozturk.txt",
        "de.bubenheim.txt", "id.indonesian.txt", "fa.fooladvand.txt", "bn.bengali.txt"
    )
    private var isInit: Boolean = true
    private var isFromSelect: Boolean = true

    private var iconQuranCallback: IconQuranAdabters.IIconQuranCallback? = null
    private var onFromAyaSelectedListener: AdapterView.OnItemSelectedListener? = null
    private var onSurahSelectedListener: AdapterView.OnItemSelectedListener? = null


    constructor(iAddQuran: IAddQuran, resources: Resources) : this() {
        this.iAddQuran = iAddQuran
        this.res = resources
    }

    constructor(
        iAddQuran: IAddQuran,
        resources: Resources,
        uri: Uri?,
        pathVideoCopy: String?,
        readerName: String?
    ) : this() {
        this.iAddQuran = iAddQuran
        this.res = resources
        this.uriRecitation = uri
        this.pathVideoCopy = pathVideoCopy
        this.readerName = readerName
    }

    private fun setSystemBarsColorBlack() {
        // No-op placeholder
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onStart() {
        super.onStart()
        setSystemBarsColorBlack()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val inflate = FragmentAddQuranBinding.inflate(inflater, container, false)
        fragmentBinding = inflate
        val root: RelativeLayout = inflate.root

        if (res != null && iAddQuran != null) {
            surahHint = if (LocaleHelper.getLanguage(requireContext()) == "ar") "سورة " else "Surah "

            val ivDone: ImageView = root.findViewById(R.id.iv_done)
            ivDoneUpload = ivDone
            if (uriRecitation != null) {
                ivDone.visibility = View.VISIBLE
            }

            (root.findViewById<View>(R.id.tv_surah) as TextCustumFont).text = res!!.getString(R.string.tv_surah)
            (root.findViewById<View>(R.id.tv_icon) as TextCustumFont).text = res!!.getString(R.string.quran_icon)
            (root.findViewById<View>(R.id.tv_add_bismilah) as TextCustumFont).text = res!!.getString(R.string.add_bismilah)
            (root.findViewById<View>(R.id.tv_end_ayah) as TextCustumFont).text = res!!.getString(R.string.tv_end_ayah)
            (root.findViewById<View>(R.id.tv_hint_reader) as TextCustumFont).text = res!!.getString(R.string.tv_hint_reader)
            (root.findViewById<View>(R.id.tv_translation) as TextCustumFont).text = res!!.getString(R.string.translation)

            arraySurah = resources!!.getStringArray(R.array.surah_names_merged)
            arrayCount = resources!!.getIntArray(R.array.surah_count)
            arrayIdentifier = resources!!.getStringArray(R.array.identifier)
            arrayReciters = res!!.getStringArray(R.array.reciters)
            arrayTranslation = res!!.getStringArray(R.array.translation_name)

            val checkbox: CheckboxCustumFont = root.findViewById(R.id.checkbox)
            includeBismilah = checkbox
            checkbox.isChecked = MyPrefereces.isIncludeBismilah(requireContext())

            root.findViewById<View>(R.id.add_bismilah).setOnClickListener {
                includeBismilah?.isChecked = !(includeBismilah?.isChecked ?: false)
            }

            // Setup surah spinner
            spinnerSurah = root.findViewById(R.id.sura_name)
            val surahAdapter = ArrayAdapter(requireContext(), R.layout.row_spinner_aya, arraySurah)
            surahAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

            onSurahSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {}
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    var ayahCount: Int
                    if (position == currentPos) return

                    ayahCount = if (isInit) {
                        arrayCount[QuranPreference.getSurah(requireContext())]
                    } else {
                        arrayCount[position]
                    }

                    val ayahList = ArrayList<String>()
                    for (i in 1..ayahCount) {
                        ayahList.add(i.toString())
                    }

                    adapterFromAyah?.clear()
                    adapterFromAyah?.addAll(ayahList)
                    adapterToAyah?.clear()
                    adapterToAyah?.addAll(ayahList)

                    if (isInit) {
                        try {
                            spinnerSurah?.setSelection(QuranPreference.getSurah(requireContext()), true)
                            spinnerFrom?.setSelection(QuranPreference.getFrom(requireContext()), false)
                            spinnerTo?.setSelection(QuranPreference.getTo(requireContext()), false)
                            spinnerReciters?.setSelection(QuranPreference.getNameReader(requireContext()), false)
                            spinnerTranslation?.setSelection(QuranPreference.getTranslation(requireContext()), false)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                        isInit = false
                    } else {
                        spinnerTo?.setSelection(0, false)
                        spinnerFrom?.setSelection(0, false)
                    }

                    currentPos = spinnerSurah?.selectedItemPosition ?: -1
                }
            }

            spinnerSurah?.onItemSelectedListener = onSurahSelectedListener
            spinnerSurah?.adapter = surahAdapter
            val surahSpinner = spinnerSurah
            surahSpinner?.dropDownVerticalOffset = (surahSpinner?.height ?: 0) * -10

            // Setup from ayah spinner
            spinnerFrom = root.findViewById(R.id.aya_from)
            val fromAdapter = ArrayAdapter<String>(requireContext(), R.layout.row_spinner_aya)
            adapterFromAyah = fromAdapter
            fromAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

            onFromAyaSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {}
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    if (isFromSearch) {
                        spinnerTo?.setSelection(QuranPreference.getTo(requireContext()))
                        isFromSearch = false
                    } else {
                        if (!isFromSelect) {
                            if (spinnerTo?.selectedItemPosition != position) {
                                spinnerTo?.setSelection(position)
                            }
                        } else {
                            isFromSelect = false
                        }
                    }
                }
            }

            spinnerFrom?.onItemSelectedListener = onFromAyaSelectedListener
            spinnerFrom?.adapter = adapterFromAyah

            // Setup to ayah spinner
            spinnerTo = root.findViewById(R.id.aya_to)
            val toAdapter = ArrayAdapter<String>(requireContext(), R.layout.row_spinner_aya)
            adapterToAyah = toAdapter
            toAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerTo?.adapter = adapterToAyah

            // Setup reciters spinner
            spinnerReciters = root.findViewById(R.id.spinner_reciters)
            val recitersAdapter = ArrayAdapter(requireContext(), R.layout.row_spinner_aya, arrayReciters)
            recitersAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

            spinnerReciters?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {}
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    if (isFromSelectReciters) {
                        goneReaderNameUpload()
                    }
                    isFromSelectReciters = true
                }
            }

            spinnerReciters?.adapter = recitersAdapter

            // Setup translation spinner
            spinnerTranslation = root.findViewById(R.id.spinner_translation)
            val translationAdapter = ArrayAdapter(requireContext(), R.layout.row_spinner_aya, arrayTranslation)
            translationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerTranslation?.adapter = translationAdapter

            layoutConnection = root.findViewById(R.id.hint_no_internet)

            // Done button
            root.findViewById<View>(R.id.btn_done).setOnClickListener {
                if (iAddQuran != null) {
                    val fromPosition = spinnerFrom?.selectedItemPosition ?: 0
                    val toPosition = spinnerTo?.selectedItemPosition ?: 0
                    val surahPosition = spinnerSurah?.selectedItemPosition ?: 0

                    val fromAya = fromPosition + 1
                    val toAya = toPosition + 1
                    val surah = surahPosition + 1

                    Thread {
                        iAddQuran!!.progress()
                        if (includeBismilah != null && includeBismilah!!.isChecked) {
                            iAddQuran!!.onBismilah()
                        }
                        addAyaEntityRecursive(fromAya, toAya, surah)
                    }.start()
                }
            }

            // Cancel button
            root.findViewById<View>(R.id.btn_cancel).setOnClickListener {
                iAddQuran?.onCancel()
            }

            // Search button
            root.findViewById<View>(R.id.btn_search).setOnClickListener {
                savePreference()
                iAddQuran?.onSearch()
            }

            // Upload button
            root.findViewById<View>(R.id.btn_upload).setOnClickListener {
                iAddQuran?.uploadRecitation()
                iAddQuran = null
            }

            // Reader name
            val tvReader: TextCustumFont = root.findViewById(R.id.tv_reader)
            tvReaderName = tvReader
            tvReader.setOnClickListener {
                if (iAddQuran == null || uriRecitation == null) return@setOnClickListener
                iAddQuran!!.onAddReaderName(readerName ?: "-", pathVideoCopy ?: "", uriRecitation!!)
            }

            if (readerName.isNullOrEmpty()) {
                this.readerName = "-"
                tvReaderName?.setTextColor(-1)
            } else {
                tvReaderName?.paint?.isUnderlineText = true
                tvReaderName?.text = this.readerName
            }

            initIconRv(root)
        }

        return root
    }

    private fun initIconRv(view: View) {
        val recyclerView: RecyclerView = view.findViewById(R.id.rv)
        recyclerView?.layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
        recyclerView?.itemAnimator = null
        recyclerView?.setHasFixedSize(true)

        val icons = ArrayList<String>()
        icons.add("hafes")
        icons.add("shamerli")
        icons.add("nour_hode")
        icons.add("amiri")

        iconQuranCallback = object : IconQuranAdabters.IIconQuranCallback {
            override fun onIcon(iconName: String) {
                icon = iconName
            }
        }

        val adabters = IconQuranAdabters(iconQuranCallback!!, icons, MyPrefereces.getLastIconIndex(requireContext()))
        iconQuranAdabters = adabters
        icon = icons[iconQuranAdabters?.select ?: 0]
        recyclerView?.adapter = iconQuranAdabters
    }

    private fun goneReaderNameUpload() {
        uriRecitation = null
        ivDoneUpload?.visibility = View.GONE
        tvReaderName?.text = "-"
        tvReaderName?.paint?.isUnderlineText = false
        tvReaderName?.setOnClickListener(null)
    }

    override fun onResume() {
        super.onResume()
        try {
            if (NetworkUtils.isNetworkAvailable(requireContext())) {
                spinnerReciters?.visibility = View.VISIBLE
                spinnerReciters?.isEnabled = true
                layoutConnection?.visibility = View.GONE
            } else {
                spinnerReciters?.isEnabled = false
                spinnerReciters?.visibility = View.INVISIBLE
                layoutConnection?.visibility = View.VISIBLE
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun addAyaIndex() {
        try {
            isFromSearch = true
            val surah = QuranPreference.getSurah(requireContext())
            currentPos = surah
            spinnerSurah?.setSelection(surah, false)

            val ayahCount = arrayCount[QuranPreference.getSurah(requireContext())]
            val ayahList = ArrayList<String>()
            for (i in 1..ayahCount) {
                ayahList.add(i.toString())
            }

            adapterFromAyah?.clear()
            adapterFromAyah?.addAll(ayahList)
            adapterToAyah?.clear()
            adapterToAyah?.addAll(ayahList)
            spinnerFrom?.setSelection(QuranPreference.getFrom(requireContext()), false)
            spinnerReciters?.setSelection(QuranPreference.getNameReader(requireContext()), false)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setNameReader(name: String?, uri: Uri?, pathVideoCopy: String?) {
        uriRecitation = uri
        this.pathVideoCopy = pathVideoCopy
        if (uri != null) {
            ivDoneUpload?.visibility = View.VISIBLE
        }
        var displayName = name
        if (displayName.isNullOrEmpty()) {
            tvReaderName?.paint?.isUnderlineText = false
            displayName = "-"
        } else {
            tvReaderName?.paint?.isUnderlineText = true
        }
        readerName = displayName
        tvReaderName?.text = displayName
    }

    fun splitAya(str: String, translation: String?, ayaNumber: Int) {
        val trimmed = str.trim()
        val split = trimmed.replace("\\s*([\\u06D6-\\u06ED])".toRegex(), "$1").trim().split("\\s+".toRegex())
        val splitTranslation = translation?.split(",")
        val numberMarker = " نص"

        if (split.size <= 4) {
            iAddQuran?.onAdd(
                "$trimmed$numberMarker", trimmed,
                translation?.replace(",", " "),
                translation, trimmed.length, ayaNumber, icon, 0, split.size
            )
            return
        }

        val sb = StringBuilder()
        var longWordCount = 0
        var totalWordCount = 0
        var segmentStart = 0
        var segmentStart2 = 0

        var i = 0
        while (i < split.size) {
            val word = split[i]
            sb.append(word).append(" ")

            if (word.length > 1) {
                longWordCount++
            }

            totalWordCount++

            if (longWordCount == 5) {
                val segmentEnd = segmentStart2 + longWordCount - (totalWordCount - longWordCount)

                if (i == split.size - 1) {
                    // Last word in ayah
                    val segmentText = sb.toString().trim()
                    iAddQuran?.onAdd(
                        "$segmentText$numberMarker", trimmed,
                        splitTranslation?.let { getWords(it.toTypedArray(), segmentStart, segmentEnd) },
                        translation, segmentText.length, ayaNumber, icon, segmentStart, segmentEnd
                    )
                    segmentStart2 = segmentStart + totalWordCount
                } else {
                    // Middle segment
                    val segmentText = sb.toString().trim()
                    val translationWords = splitTranslation?.let {
                        getWords(it.toTypedArray(), segmentStart2, segmentEnd)
                    }
                    iAddQuran?.onAdd(
                        segmentText, trimmed,
                        translationWords, translation, -1, -1, icon,
                        segmentStart2, segmentStart2 + totalWordCount
                    )
                    segmentStart2 += totalWordCount
                }

                sb.setLength(0)
                longWordCount = 0
                totalWordCount = 0
            }

            i++
        }

        // Remaining words
        if (sb.isNotEmpty()) {
            val remainingText = sb.toString().trim()
            iAddQuran?.onAdd(
                "$remainingText$numberMarker", trimmed,
                splitTranslation?.let {
                    getWords(it.toTypedArray(), it.size - longWordCount - (totalWordCount - longWordCount), it.size)
                },
                translation, remainingText.length, ayaNumber, icon,
                segmentStart2, segmentStart2 + totalWordCount
            )
        }
    }

    fun getWords(arr: Array<String>?, from: Int, to: Int): String {
        if (arr == null || arr.isEmpty()) return ""
        var start = from
        var end = to
        if (start < 0) start = 0
        if (end > arr.size) end = arr.size
        if (start >= end) return ""
        return arr.copyOfRange(start, end).joinToString(" ")
    }

    fun addAyaEntityRecursive(from: Int, to: Int, surahNumber: Int) {
        try {
            val ayahText = QuranReader.getAyahText(requireContext(), surahNumber, from)
            val translationText = if ((spinnerTranslation?.selectedItemPosition ?: 0) > 0) {
                QuranReader.getTranslationAyahText(
                    requireContext(),
                    translationName[spinnerTranslation!!.selectedItemPosition - 1],
                    surahNumber,
                    from
                )
            } else null

            splitAya(ayahText ?: "", null, from)

            if (translationText != null) {
                iAddQuran?.onAddTranslation(
                    translationText, from,
                    spinnerTranslation?.selectedItemPosition == 1
                )
            }

            if (iAddQuran != null) {
                if (spinnerReciters?.isEnabled == true) {
                    recitersModels.add(
                        RecitersModel(
                            arrayIdentifier[spinnerReciters!!.selectedItemPosition],
                            surahNumber,
                            from
                        )
                    )
                }

                if (from >= to) {
                    if (uriRecitation != null) {
                        iAddQuran!!.onDone(
                            surahHint + arraySurah[spinnerSurah!!.selectedItemPosition],
                            spinnerSurah!!.selectedItemPosition + 1,
                            readerName ?: "-",
                            uriRecitation!!,
                            pathVideoCopy ?: ""
                        )
                    } else {
                        iAddQuran!!.onDone(
                            surahHint + arraySurah[spinnerSurah!!.selectedItemPosition],
                            spinnerSurah!!.selectedItemPosition + 1,
                            arrayReciters[spinnerReciters!!.selectedItemPosition],
                            recitersModels
                        )
                    }
                    return
                }
            }

            addAyaEntityRecursive(from + 1, to, surahNumber)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun savePreference() {
        QuranPreference.savePreferences(requireContext(),
            spinnerSurah?.selectedItemPosition ?: 0,
            spinnerFrom?.selectedItemPosition ?: 0,
            spinnerTo?.selectedItemPosition ?: 0,
            spinnerReciters?.selectedItemPosition ?: 0,
            spinnerTranslation?.selectedItemPosition ?: 0
        )
        try {
            MyPrefereces.putIndexLastIcon(requireContext(), iconQuranAdabters?.select ?: 0)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        try {
            MyPrefereces.putIncludeBismilah(requireContext(), includeBismilah?.isChecked ?: false)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        savePreference()
        QuranPreference.saveLastSearch(requireContext(), "")
        iAddQuran?.onCancel()
        onFromAyaSelectedListener = null
        onSurahSelectedListener = null
        fragmentBinding = null
        instance = null
        iconQuranCallback = null
    }
}
