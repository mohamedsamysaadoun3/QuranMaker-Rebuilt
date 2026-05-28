package hazem.nurmontage.videoquran

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import hazem.nurmontage.videoquran.Utils.AppUtils
import hazem.nurmontage.videoquran.Utils.ScreenUtils
import hazem.nurmontage.videoquran.adabter.YoutuberAdabter
import hazem.nurmontage.videoquran.model.YoutuberModel
import hazem.nurmontage.videoquran.views.TextCustumFont

class YoutuberActivity : BaseActivity() {

    private var mResources: Resources? = null

    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            finish()
        }
    }

    // YoutuberAdabter is available in adabter package
    private var iYoutuber: YoutuberAdabter.IYoutuber? = object : YoutuberAdabter.IYoutuber {
        override fun onClick(videoId: String) {
            val youtubeAppIntent = Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:$videoId"))
            val youtubeWebIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://youtu.be/$videoId"))
            try {
                try {
                    startActivity(youtubeAppIntent)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } catch (e: ActivityNotFoundException) {
                startActivity(youtubeWebIntent)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_youtuber)
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)

        setStatusBarColor(ViewCompat.MEASURED_STATE_MASK)
        setNavigationBarColor(ViewCompat.MEASURED_STATE_MASK)

        val insetsController = WindowCompat.getInsetsController(window, window.decorView)
        insetsController.isAppearanceLightStatusBars = false
        insetsController.isAppearanceLightNavigationBars = false

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { view, windowInsetsCompat ->
            val insets: Insets = windowInsetsCompat.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(insets.left, insets.top, insets.right, insets.bottom)
            windowInsetsCompat
        }

        mResources = resources
        if (mResources == null) {
            finish()
        }

        findViewById<View>(R.id.btn_on_back).setOnClickListener {
            onBackPressedCallback.handleOnBackPressed()
        }

        init()

        findViewById<View>(R.id.btn_send_lnk).setOnClickListener {
            youtuberLnk(this)
        }

        (findViewById<TextCustumFont>(R.id.tv_tutorial)).text = mResources?.getString(R.string.my_tutorial)
    }

    override fun onDestroy() {
        iYoutuber = null
        super.onDestroy()
    }

    private fun init() {
        val youtuberList = ArrayList<YoutuberModel>()
        youtuberList.add(YoutuberModel("AjFCfILaEI8", R.drawable.hilal_ytb))
        youtuberList.add(YoutuberModel("vMgFSEE2hmg", R.drawable.gasadi_ytb))
        youtuberList.add(YoutuberModel("dr1LTEvCEHk", R.drawable.hicham_ytb))
        youtuberList.add(YoutuberModel("cRNG62W8ZLk", R.drawable.pakestain))
        youtuberList.add(YoutuberModel("tkPEq4qz2OQ", R.drawable.sajad_ytb))
        youtuberList.add(YoutuberModel("5IQzSF0wqJE", R.drawable.noor_ytb))
        youtuberList.add(YoutuberModel("E9cVRHeDzeU", R.drawable.ytb_yesser))

        val recyclerView = findViewById<RecyclerView>(R.id.rv)

        // YoutuberAdabter is available in adabter package
        val screenWidth = ScreenUtils.getScreenWidth(this)
        val itemHeight = (ScreenUtils.getScreenHeight(this) * 0.35f).toInt()
        val adapter = YoutuberAdabter(
            iYoutuber,
            youtuberList,
            AppUtils.getVersionName(this),
            screenWidth,
            itemHeight
        )

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.itemAnimator = null
        recyclerView.setHasFixedSize(true)
        recyclerView.adapter = adapter
    }

    private fun isGmailAvailable(context: Context): Boolean {
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "message/rfc822"
        intent.setPackage("com.google.android.gm")
        return !context.packageManager.queryIntentActivities(intent, 0).isEmpty()
    }

    fun youtuberLnk(context: Context) {
        val subject = mResources?.getString(R.string.i_m_youtuber) ?: return
        val emailAddresses = arrayOf("hazemourari08@gmail.com")

        if (isGmailAvailable(context)) {
            val intent = Intent(Intent.ACTION_SEND).apply {
                putExtra(Intent.EXTRA_EMAIL, emailAddresses)
                putExtra(Intent.EXTRA_BCC, emailAddresses)
                putExtra(Intent.EXTRA_SUBJECT, subject)
                putExtra(Intent.EXTRA_TEXT, mResources?.getString(R.string.link))
                type = "message/rfc822"
                setPackage("com.google.android.gm")
            }
            try {
                startActivity(intent)
                return
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        try {
            val intent = Intent(Intent.ACTION_SEND).apply {
                putExtra(Intent.EXTRA_EMAIL, emailAddresses)
                putExtra(Intent.EXTRA_BCC, emailAddresses)
                putExtra(Intent.EXTRA_SUBJECT, subject)
                putExtra(Intent.EXTRA_TEXT, mResources?.getString(R.string.link))
                type = "message/rfc822"
            }
            startActivity(Intent.createChooser(intent, "Send email using"))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
