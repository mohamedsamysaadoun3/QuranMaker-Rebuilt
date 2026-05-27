package hazem.nurmontage.videoquran.fragment

import android.content.res.Resources
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.tabs.TabLayout
import hazem.nurmontage.videoquran.R
import hazem.nurmontage.videoquran.Utils.BillingPreferences
import hazem.nurmontage.videoquran.adabter.TransitionEntityAdabters
import hazem.nurmontage.videoquran.constant.TransitionType
import hazem.nurmontage.videoquran.databinding.FragmentEffectAyaBinding
import hazem.nurmontage.videoquran.entity_timeline.EntityQuranTimeline
import hazem.nurmontage.videoquran.model.Transition
import hazem.nurmontage.videoquran.views.TextCustumFont
import nl.dionsegijn.konfetti.core.Angle

/**
 * Fragment for applying transition effects to Quran ayah entities on the timeline.
 * Supports in/out transitions (fade, slide-left, slide-right) with duration control.
 * Includes subscription check for premium transitions.
 * Converted from Java decompiled source.
 */
class EffectAyaFragment() : Fragment() {

    companion object {
        var instance: EffectAyaFragment? = null

        @Synchronized
        fun get(
            transition: Transition?,
            resources: Resources?,
            iTransition: ITransition?,
            entityQuranTimeline: EntityQuranTimeline?
        ): EffectAyaFragment {
            if (instance == null) {
                instance = EffectAyaFragment(transition, resources, iTransition, entityQuranTimeline!!)
            }
            return instance!!
        }

        private const val COLOR_DISABLED = -8355712
        private const val COLOR_ENABLED = -1
    }

    interface ITransition {
        fun applyAll(tabIndex: Int, entityQuranTimeline: EntityQuranTimeline)
        fun destroy(entityQuranTimeline: EntityQuranTimeline)
        fun `in`(type: String, entityQuranTimeline: EntityQuranTimeline)
        fun onHideFragment(entityQuranTimeline: EntityQuranTimeline)
        fun out(type: String, entityQuranTimeline: EntityQuranTimeline)
        fun playing(entityQuranTimeline: EntityQuranTimeline)
        fun remove(tabIndex: Int, entityQuranTimeline: EntityQuranTimeline)
        fun toSubscribe()
        fun updateDurationIn(duration: Float, entityQuranTimeline: EntityQuranTimeline)
        fun updateDurationOut(duration: Float, entityQuranTimeline: EntityQuranTimeline)
    }

    private var btnApplyAll: LinearLayout? = null
    private var btnUnEffect: ImageButton? = null
    private var entityQuranTimeline: EntityQuranTimeline? = null
    private var iTransition: ITransition? = null
    private var index: Int = -1
    private var ivApplyAll: ImageView? = null
    private var recyclerView: RecyclerView? = null
    private var resources: Resources? = null
    private var seekBarDuration: SeekBar? = null
    private var tabSelected: Int = 0
    private var time: Float = 0f
    private var transition: Transition? = null
    private var transitionEntityAdabters: TransitionEntityAdabters? = null
    private var transitionEntityBinding: FragmentEffectAyaBinding? = null
    private var tvDuration: TextCustumFont? = null
    private var tvApplyAll: TextCustumFont? = null


    constructor(
        transition: Transition?,
        resources: Resources?,
        iTransition: ITransition?,
        entityQuranTimeline: EntityQuranTimeline
    ) : this() {
        this.resources = resources
        this.iTransition = iTransition
        this.transition = transition
        this.time = (entityQuranTimeline.getRect().width() / entityQuranTimeline.secondInScreen) * 0.5f
        this.entityQuranTimeline = entityQuranTimeline
    }

    fun updateView(duration: Float, transition: Transition) {
        this.transition = transition
        if (seekBarDuration?.visibility != View.VISIBLE) {
            seekBarDuration?.visibility = View.VISIBLE
            tvDuration?.visibility = View.VISIBLE
        }
        btnUnEffect?.setBackgroundResource(R.drawable.circle_effect)
        updateSeek(duration)
        visibleApplyAll()
    }

    private fun addCustomViewToTab(tab: TabLayout.Tab) {
        val inflate = layoutInflater.inflate(R.layout.layout_tablayout, null as ViewGroup?)
        val nameView: TextCustumFont = inflate.findViewById(R.id.name)
        inflate.findViewById<View>(R.id.icon).visibility = View.GONE
        nameView.text = tab.text.toString()
        tab.customView = inflate
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val inflate = FragmentEffectAyaBinding.inflate(inflater, container, false)
        transitionEntityBinding = inflate
        val root: LinearLayout = inflate.root

        if (resources != null && iTransition != null) {
            iTransition!!.playing(entityQuranTimeline!!)

            val tabLayout: TabLayout = root.findViewById(R.id.tab_layout)
            tvDuration = root.findViewById(R.id.status_duration)

            val inTab = tabLayout.newTab()
            inTab.text = resources!!.getString(R.string.in_transition)
            tabLayout.addTab(inTab)
            addCustomViewToTab(inTab)

            val outTab = tabLayout.newTab()
            outTab.text = resources!!.getString(R.string.out_transition)
            tabLayout.addTab(outTab)
            addCustomViewToTab(outTab)

            tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                override fun onTabReselected(tab: TabLayout.Tab) {}
                override fun onTabUnselected(tab: TabLayout.Tab) {}
                override fun onTabSelected(tab: TabLayout.Tab) {
                    tabSelected = tab.position
                    loadTransition(tab.position)
                }
            })

            tabLayout.getTabAt(0)?.select()

            val seekBar: SeekBar = root.findViewById(R.id.seekbar)
            seekBarDuration = seekBar
            seekBar.max = (time * 4.0f).toInt()

            seekBarDuration?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    tvDuration?.text = (progress / 10.0f).toString()
                }
                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                    if (iTransition != null) {
                        if (tabSelected == 0) {
                            iTransition!!.updateDurationIn(
                                seekBar?.progress?.div(4.0f) ?: 0f,
                                entityQuranTimeline!!
                            )
                        } else if (tabSelected == 1) {
                            iTransition!!.updateDurationOut(
                                seekBar?.progress?.div(4.0f) ?: 0f,
                                entityQuranTimeline!!
                            )
                        }
                        visibleApplyAll()
                    }
                }
            })

            btnUnEffect = root.findViewById(R.id.btn_unEffect)

            if (transition != null && transition!!.isIn) {
                val min = minOf(transition!!.duration_in, time)
                seekBarDuration?.progress = (4.0f * min).toInt()
                tvDuration?.text = (seekBarDuration?.progress?.div(10.0f)).toString()
                iTransition!!.updateDurationIn(min, entityQuranTimeline!!)
                btnUnEffect?.setBackgroundResource(R.drawable.circle_effect)
            } else {
                seekBarDuration?.visibility = View.GONE
                tvDuration?.visibility = View.GONE
                btnUnEffect?.setBackgroundResource(R.drawable.circle_item_menu_select)
            }

            val rv: RecyclerView = root.findViewById(R.id.rv)
            recyclerView = rv
            rv.setHasFixedSize(true)
            rv.layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
            rv.setItemViewCacheSize(20)
            rv.isDrawingCacheEnabled = true
            rv.itemAnimator = null
            rv.setDrawingCacheQuality(0x100000)

            btnUnEffect?.setOnClickListener {
                if (transitionEntityAdabters?.isHaveSelect() == true) {
                    iTransition?.remove(tabSelected, entityQuranTimeline!!)
                    transitionEntityAdabters?.unselect()
                    btnUnEffect?.setBackgroundResource(R.drawable.circle_item_menu_select)
                    seekBarDuration?.visibility = View.GONE
                    tvDuration?.visibility = View.GONE
                    visibleApplyAll()
                }
            }

            root.findViewById<View>(R.id.btn_close).setOnClickListener {
                iTransition?.onHideFragment(entityQuranTimeline!!)
            }

            btnApplyAll = root.findViewById(R.id.btn_appl_all)
            tvApplyAll = root.findViewById(R.id.tv_apply_all) as TextCustumFont
            ivApplyAll = root.findViewById(R.id.iv_apply_all)
            tvApplyAll?.text = resources!!.getString(R.string.applyall)
            btnApplyAll?.isEnabled = false
            btnApplyAll?.isClickable = false

            btnApplyAll?.setOnClickListener {
                if (iTransition != null) {
                    iTransition!!.applyAll(tabSelected, entityQuranTimeline!!)
                    invisibleApplyAll()
                }
            }

            root.post {
                val inTransition = getInTransition()
                val idx = if (transition == null || !transition!!.isIn) {
                    -1
                } else {
                    getIndex(inTransition, transition!!.type_in)
                }
                transitionEntityAdabters = TransitionEntityAdabters(
                    BillingPreferences.isSubscribed(requireContext()),
                    iTransition!!,
                    inTransition,
                    idx,
                    entityQuranTimeline!!
                )
                recyclerView?.adapter = transitionEntityAdabters
                scroll(transitionEntityAdabters!!.select)
            }
        }

        return root
    }

    private fun invisibleApplyAll() {
        if (btnApplyAll?.isEnabled == true) {
            btnApplyAll?.isEnabled = false
            btnApplyAll?.isClickable = false
            tvApplyAll?.setTextColor(COLOR_DISABLED)
            ivApplyAll?.setColorFilter(COLOR_DISABLED, PorterDuff.Mode.SRC_IN)
        }
    }

    fun visibleApplyAll() {
        if (btnApplyAll?.isEnabled == true) return
        btnApplyAll?.isEnabled = true
        btnApplyAll?.isClickable = true
        tvApplyAll?.setTextColor(COLOR_ENABLED)
        ivApplyAll?.setColorFilter(COLOR_ENABLED, PorterDuff.Mode.SRC_IN)
    }

    fun scroll(i: Int) {
        val layoutManager = recyclerView?.layoutManager as? LinearLayoutManager ?: return
        val view = layoutManager.findViewByPosition(i)
        layoutManager.scrollToPositionWithOffset(
            i,
            ((recyclerView?.width ?: 0) - (view?.width ?: 0)) / 2
        )
    }

    fun updateButton(transition: Transition) {
        this.transition = transition
        btnUnEffect?.setBackgroundResource(R.drawable.circle_effect)
        visibleSeekbar()
    }

    fun getIndex(list: List<TransitionEntityAdabters.TransitionItem>, type: String): Int {
        for (i in list.indices) {
            if (type == list[i].type) {
                return i
            }
        }
        return -1
    }

    private fun updateSeek(duration: Float) {
        seekBarDuration?.progress = (duration * 4.0f).toInt()
        tvDuration?.text = (seekBarDuration?.progress?.div(10.0f)).toString()
    }

    private fun visibleSeekbar() {
        seekBarDuration?.visibility = View.VISIBLE
        tvDuration?.visibility = View.VISIBLE
    }

    private fun invisibleSeekbar() {
        seekBarDuration?.visibility = View.GONE
        tvDuration?.visibility = View.GONE
    }

    fun loadTransition(tabPosition: Int) {
        index = -1
        if (tabPosition == 0) {
            val inTransition = getInTransition()
            if (transition != null) {
                if (transition!!.isIn) {
                    val idx = getIndex(inTransition, transition!!.type_in)
                    index = idx
                    if (idx != -1) {
                        visibleSeekbar()
                        iTransition?.updateDurationIn(transition!!.duration_in, entityQuranTimeline!!)
                        btnUnEffect?.setBackgroundResource(R.drawable.circle_effect)
                    } else {
                        entityQuranTimeline?.quranEntity?.endAnimator()
                        invisibleSeekbar()
                        btnUnEffect?.setBackgroundResource(R.drawable.circle_item_menu_select)
                    }
                } else {
                    entityQuranTimeline?.quranEntity?.endAnimator()
                    invisibleSeekbar()
                    btnUnEffect?.setBackgroundResource(R.drawable.circle_item_menu_select)
                }
            }
            transitionEntityAdabters?.update(inTransition, "in", index)
            scroll(index)
            if (transition != null) {
                updateSeek(transition!!.duration_in)
            }
        } else if (tabPosition == 1) {
            val outTransition = getOutTransition()
            if (transition != null) {
                if (transition!!.isOut) {
                    val idx = getIndex(outTransition, transition!!.type_out)
                    index = idx
                    if (idx != -1) {
                        visibleSeekbar()
                        iTransition?.updateDurationOut(transition!!.duration_out, entityQuranTimeline!!)
                        btnUnEffect?.setBackgroundResource(R.drawable.circle_effect)
                    } else {
                        entityQuranTimeline?.quranEntity?.endAnimator()
                        invisibleSeekbar()
                        btnUnEffect?.setBackgroundResource(R.drawable.circle_item_menu_select)
                    }
                } else {
                    entityQuranTimeline?.quranEntity?.endAnimator()
                    invisibleSeekbar()
                    btnUnEffect?.setBackgroundResource(R.drawable.circle_item_menu_select)
                }
            }
            transitionEntityAdabters?.update(outTransition, "out", index)
            scroll(index)
            if (transition != null) {
                updateSeek(transition!!.duration_out)
            }
        }
    }

    fun getInTransition(): List<TransitionEntityAdabters.TransitionItem> {
        return listOf(
            TransitionEntityAdabters.TransitionItem(
                TransitionType.FADE_IN.value, R.drawable.ic_linear_gradient, 0
            ),
            TransitionEntityAdabters.TransitionItem(
                TransitionType.SLIDE_TO_RIGHT.value, R.drawable.ic_btn_back, Angle.LEFT
            ),
            TransitionEntityAdabters.TransitionItem(
                TransitionType.SLIDE_TO_LEFT.value, R.drawable.ic_btn_back, 0
            )
        )
    }

    private fun getOutTransition(): List<TransitionEntityAdabters.TransitionItem> {
        return listOf(
            TransitionEntityAdabters.TransitionItem(
                TransitionType.FADE_OUT.value, R.drawable.ic_linear_gradient, 0
            ),
            TransitionEntityAdabters.TransitionItem(
                TransitionType.SLIDE_TO_RIGHT.value, R.drawable.ic_btn_back, Angle.LEFT
            ),
            TransitionEntityAdabters.TransitionItem(
                TransitionType.SLIDE_TO_LEFT.value, R.drawable.ic_btn_back, 0
            )
        )
    }

    override fun onDestroyView() {
        iTransition?.destroy(entityQuranTimeline!!)
        transitionEntityBinding?.let { binding ->
            binding.root.removeAllViews()
        }
        transitionEntityBinding = null
        instance = null
        super.onDestroyView()
    }
}
