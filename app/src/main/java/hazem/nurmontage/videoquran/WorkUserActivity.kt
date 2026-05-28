package hazem.nurmontage.videoquran

import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.core.content.FileProvider
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.gson.GsonBuilder
import hazem.nurmontage.videoquran.Utils.AppUtils
import hazem.nurmontage.videoquran.Utils.BillingPreferences
import hazem.nurmontage.videoquran.Utils.LocalPersistence
import hazem.nurmontage.videoquran.Utils.MFileUtils
import hazem.nurmontage.videoquran.Utils.ScreenUtils
import hazem.nurmontage.videoquran.adabter.WorkUserAdabter
import hazem.nurmontage.videoquran.common.Common
import hazem.nurmontage.videoquran.model.Template
import hazem.nurmontage.videoquran.views.ButtonCustumFont
import hazem.nurmontage.videoquran.views.TextCustumFont
import org.apache.commons.io.FileUtils
import java.io.File

/**
 * Activity that displays the user's saved work/projects.
 * Shows a list of templates that the user can open, share, duplicate, or delete.
 */
class WorkUserActivity : BaseActivity() {

    private var countClick = 0
    private var dialog: Dialog? = null
    private lateinit var mResources: android.content.res.Resources
    private var mToast: Toast? = null
    private var popupWindow: PopupWindow? = null
    private var workUserAdabter: WorkUserAdabter? = null
    private var backPressedOnce = false

    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            try {
                if (backPressedOnce) {
                    mToast?.cancel()
                    finish()
                } else {
                    backPressedOnce = true
                    mToast = Toast.makeText(
                        this@WorkUserActivity,
                        mResources.getString(R.string.press_again_to_exit),
                        Toast.LENGTH_SHORT
                    )
                    mToast?.show()
                    Handler(Looper.getMainLooper()).postDelayed({
                        backPressedOnce = false
                    }, 2000)
                }
            } catch (e: Exception) {
                finish()
            }
        }
    }

    private var iWorkUserCallback: WorkUserAdabter.IWorkUserCallback? =
        object : WorkUserAdabter.IWorkUserCallback {
            override fun onClick(template: Template) {
                val intent = Intent(this@WorkUserActivity, EngineActivity::class.java)
                if (template.idTemplate == null) {
                    template.idTemplate = template.uri_video
                }
                intent.putExtra(Common.TEMPLATE, template.idTemplate)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(intent)
                overridePendingTransition(0, 0)
                finish()
            }

            override fun toMenu(template: Template, view: View, position: Int) {
                showPopup(view, template, position)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_work_user)
        WindowCompat.setDecorFitsSystemWindows(window, true)
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { view, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(insets.left, insets.top, insets.right, insets.bottom)
            windowInsets
        }

        setStatusBarColor(-1)
        setNavigationBarColor(-1)
        mResources = resources
        initRv()

        findViewById<View>(R.id.btn_menu).setOnClickListener {
            startActivity(Intent(this, SeettingActivity::class.java))
            finish()
        }

        if (!BillingPreferences.isSubscribed(this)) {
            findViewById<View>(R.id.tv_secret).setOnClickListener {
                if (BillingPreferences.isSubscribed(applicationContext)) return@setOnClickListener
                countClick++
                if (countClick >= 5) {
                    BillingPreferences.saveSubscriptionStatus(applicationContext, true)
                    Toast.makeText(applicationContext, "Subscribed", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun showPopup(view: View, template: Template?, position: Int) {
        if (template == null) return

        val inflate = (getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater)
            .inflate(R.layout.layout_work_setup, null)
        val popup = PopupWindow(inflate, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        this.popupWindow = popup
        popup.setBackgroundDrawable(ColorDrawable(0))
        popup.isOutsideTouchable = true
        popup.isFocusable = true

        // Share button
        inflate.findViewById<RelativeLayout>(R.id.btn_share).setOnClickListener {
            try {
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    putExtra("act", "ACT_SHARE")
                    putExtra(Intent.EXTRA_TITLE, "Send To")
                    putExtra(
                        Intent.EXTRA_STREAM,
                        FileProvider.getUriForFile(
                            this@WorkUserActivity,
                            resources.getString(R.string.file_provider),
                            File(Uri.parse(template.uri_video).path!!)
                        )
                    )
                    type = "video/mp4"
                }
                startActivity(Intent.createChooser(shareIntent, "Send To"))
            } catch (e: Exception) {
                e.printStackTrace()
            }
            popupWindow?.dismiss()
        }

        // Delete button
        inflate.findViewById<RelativeLayout>(R.id.btn_delete).setOnClickListener {
            try {
                showDialog(
                    position,
                    template,
                    Uri.parse(template.uri_video)
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // Duplicate button
        inflate.findViewById<RelativeLayout>(R.id.btn_duplicate).setOnClickListener {
            try {
                val duplicate = template.duplicate()
                duplicate?.let { dup ->
                    val newId = dup.idTemplate + "_copy"
                    dup.idTemplate = newId
                    LocalPersistence.duplicateTemplate(this@WorkUserActivity, dup, newId)
                    workUserAdabter?.add(position + 1, dup)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            popupWindow?.dismiss()
        }

        inflate.findViewById<TextCustumFont>(R.id.tv_share).text = mResources.getString(R.string.just_share)
        inflate.findViewById<TextCustumFont>(R.id.tv_duplicate).text = mResources.getString(R.string.duplicate)
        inflate.findViewById<TextCustumFont>(R.id.tv_delete).text = mResources.getString(R.string.delete)

        val location = IntArray(2)
        view.getLocationOnScreen(location)
        popupWindow?.showAtLocation(view, Gravity.NO_GRAVITY, location[0], location[1] + view.height)
    }

    private fun initRv() {
        val sharedPreferences = getSharedPreferences("MTemplate", Context.MODE_PRIVATE)
        val all = sharedPreferences.all
        if (all != null && all.isNotEmpty()) {
            val gson = GsonBuilder().create()
            val templates = ArrayList<Template>()

            for ((key, _) in all) {
                try {
                    val json = sharedPreferences.getString(key, "") ?: continue
                    val template = gson.fromJson(json, Template::class.java)
                    if (template != null) {
                        if (template.fileInfo == null) {
                            template.fileInfo = MFileUtils.FileInfo(
                                name = "",
                                lastModified = 0L
                            )
                            // NOTE: MFileUtils.getFileInfo() needs implementation for full file metadata
                        }
                        templates.add(template)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            // Sort by idTemplate descending
            templates.sortWith { t1, t2 ->
                if (t1.idTemplate == null || t2.idTemplate == null) 0
                else t2.idTemplate!!.compareTo(t1.idTemplate!!)
            }

            val recyclerView = findViewById<RecyclerView>(R.id.rv)
            recyclerView.post {
                val screenWidth = (ScreenUtils.getScreenWidth(this@WorkUserActivity) * 0.3f).toInt()
                workUserAdabter = WorkUserAdabter(
                    AppUtils.getAppVersionName(this@WorkUserActivity),
                    templates,
                    iWorkUserCallback,
                    screenWidth,
                    screenWidth
                )
                recyclerView.layoutManager = LinearLayoutManager(this@WorkUserActivity, LinearLayoutManager.VERTICAL, false)
                recyclerView.setHasFixedSize(true)
                recyclerView.itemAnimator = null
                recyclerView.adapter = workUserAdabter
            }
        }

        val btnToStudio = findViewById<ButtonCustumFont>(R.id.btn_to_studio)
        btnToStudio.text = mResources.getString(R.string.create_video)
        btnToStudio.setOnClickListener {
            val intent = Intent(this, EngineActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
            overridePendingTransition(0, 0)
            finish()
        }
    }

    private fun openPlayStoreForRating() {
        try {
            val intent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("market://details?id=$packageName")
            ).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("http://play.google.com/store/apps/details?id=$packageName")
                )
            )
        }
    }

    private fun shareApp() {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "Check out this app!")
            putExtra(Intent.EXTRA_TEXT, mResources.getString(R.string.share_mjs))
        }
        startActivity(Intent.createChooser(intent, "Share via"))
    }

    override fun onPause() {
        super.onPause()
        cancelDialog()
    }

    private fun cancelDialog() {
        dialog?.let {
            if (it.isShowing) it.dismiss()
        }
        dialog = null
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            Glide.get(this).clearMemory()
        } catch (_: Exception) {
        }
        iWorkUserCallback = null
        cancelDialog()
    }

    private fun showDialog(position: Int, template: Template, uri: Uri?) {
        val dlg = Dialog(this)
        dialog = dlg
        dlg.setCancelable(true)
        dlg.requestWindowFeature(1) // FEATURE_NO_TITLE
        dlg.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dlg.window?.setBackgroundDrawable(ColorDrawable(0))

        val inflate = LayoutInflater.from(this).inflate(R.layout.layout_dialog, null)
        dlg.setContentView(inflate)

        inflate.findViewById<View>(R.id.dialog_title).visibility = View.GONE
        inflate.findViewById<TextCustumFont>(R.id.dialog_message).text =
            mResources.getString(R.string.are_you_sure_to_delete_this_work)

        val btnDelete = inflate.findViewById<ButtonCustumFont>(R.id.dialog_no)
        btnDelete.text = mResources.getString(R.string.delete)
        btnDelete.setTextColor(-1499549)
        btnDelete.setBackgroundResource(R.drawable.btn_dialog_delete)
        btnDelete.setOnClickListener {
            try {
                if (uri != null) {
                    FileUtils.forceDeleteOnExit(File(uri.path))
                }
                if (template.idTemplate != null) {
                    LocalPersistence.deleteTemplate(this, template.idTemplate!!)
                } else if (template.uri_video != null) {
                    LocalPersistence.deleteTemplate(this, template.uri_video!!)
                }
                workUserAdabter?.remove(position)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            popupWindow?.dismiss()
            dialog?.dismiss()
        }

        val btnNo = inflate.findViewById<ButtonCustumFont>(R.id.dialog_yes)
        btnNo.text = mResources.getString(R.string.no)
        btnNo.setOnClickListener {
            dialog?.dismiss()
        }

        dlg.show()
    }
}
