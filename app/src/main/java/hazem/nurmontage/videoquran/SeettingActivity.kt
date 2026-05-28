package hazem.nurmontage.videoquran

import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesResponseListener
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryPurchasesParams
import hazem.nurmontage.videoquran.Utils.BillingPreferences
import hazem.nurmontage.videoquran.Utils.LocaleHelper
import hazem.nurmontage.videoquran.Utils.MyPrefereces
import hazem.nurmontage.videoquran.views.TextCustumFont
import hazem.nurmontage.videoquran.views.TextCustumFontBold

/**
 * Settings activity for the app.
 * Provides access to: pro subscription, language change, rating, sharing,
 * about, copyright notice, social media links, and contact support.
 * Implements PurchasesUpdatedListener for Google Play Billing restore functionality.
 */
class SeettingActivity : BaseActivity(), PurchasesUpdatedListener {

    private var billingClient: BillingClient? = null
    private var dialog: Dialog? = null
    private lateinit var mResources: android.content.res.Resources
    private var hasPurchasedForever = false

    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            // just finish() - WorkUserActivity is already in the back stack
            // no need for overridePendingTransition when finishing
            finish()
        }
    }

    // ── PurchasesUpdatedListener ────────────────────────────────────────

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: List<Purchase>?) {
        // No-op: handled via restore flow
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_seetting)

        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
        WindowCompat.setDecorFitsSystemWindows(window, true)

        setStatusBarColor(ViewCompat.MEASURED_STATE_MASK)
        setNavigationBarColor(ViewCompat.MEASURED_STATE_MASK)

        val insetsController = WindowCompat.getInsetsController(window, window.decorView)
        insetsController.isAppearanceLightStatusBars = false
        insetsController.isAppearanceLightNavigationBars = false

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { view, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(insets.left, insets.top, insets.right, insets.bottom)
            windowInsets
        }

        mResources = resources
        if (mResources == null) {
            finish()
        }
        init()
    }

    private fun setPro() {
        (findViewById<TextCustumFontBold>(R.id.tv_your_pro)).text = mResources.getString(R.string.you_are_premium)
        (findViewById<LinearLayout>(R.id.btn_to_pro)).setBackgroundResource(R.drawable.bg_your_pro)
        findViewById<View>(R.id.btn_restore).visibility = View.GONE
    }

    override fun onResume() {
        super.onResume()
        if (BillingPreferences.isSubscribed(this)) {
            setPro()
        } else {
            findViewById<View>(R.id.btn_restore).setOnClickListener {
                findViewById<View>(R.id.progress).visibility = View.VISIBLE
                restoreSubscribe()
            }
        }
    }

    private fun init() {
        // Back button
        findViewById<View>(R.id.btn_on_back).setOnClickListener {
            onBackPressedCallback.handleOnBackPressed()
        }

        // Version display
        val tvVersion = findViewById<TextCustumFont>(R.id.tv_version)
        try {
            var versionName = packageManager.getPackageInfo(packageName, 0).versionName
            if (versionName != null) {
                versionName = versionName.replace("-nurmontage4kb", "").replace("-nurmontage16kb", "")
            }
            if (LocaleHelper.getLanguage(this) == "ar") {
                tvVersion.text = "إصدار : $versionName"
            } else {
                tvVersion.text = "Version : $versionName"
            }
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }

        // Rate app
        findViewById<View>(R.id.btn_rate_app).setOnClickListener {
            openPlayStoreForRating()
        }

        // More apps
        findViewById<View>(R.id.btn_more_app).setOnClickListener {
            openMoreApps()
        }

        // Share
        findViewById<View>(R.id.btn_share).setOnClickListener {
            shareApp()
        }

        // Language
        findViewById<View>(R.id.btn_lang).setOnClickListener {
            changeLang()
        }

        // Copyright
        findViewById<View>(R.id.btn_copyRight).setOnClickListener {
            dialogCopyRight()
        }

        // Pro
        findViewById<View>(R.id.btn_to_pro).setOnClickListener {
            toPro()
        }

        // About
        findViewById<View>(R.id.btn_about).setOnClickListener {
            toAbout()
        }

        // Youtuber / Blogger
        findViewById<View>(R.id.btn_im_bloger).setOnClickListener {
            toYoutuber()
        }

        // Instagram
        findViewById<View>(R.id.btn_instagram).setOnClickListener {
            openInstagramPage()
        }

        // YouTube
        findViewById<View>(R.id.btn_youtbe).setOnClickListener {
            openYouTubePage()
        }

        // TikTok
        findViewById<View>(R.id.btn_ticktock).setOnClickListener {
            openTikTokPage()
        }

        // WhatsApp help
        findViewById<View>(R.id.btn_whatsap).setOnClickListener {
            help()
        }
    }

    // ── Navigation helpers ──────────────────────────────────────────────

    private fun help() {
        try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("https://chat.whatsapp.com/F0kqOjZS1VuBAvoiOG4XEZ")
                setPackage("com.whatsapp")
            }
            startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun toYoutuber() {
        startActivity(Intent(this, YoutuberActivity::class.java))
        overridePendingTransition(0, 0)
    }

    override fun onPause() {
        cancelDialog()
        super.onPause()
    }

    private fun changeLang() {
        val intent = Intent(this, ChoiceLangActivity::class.java)
        intent.putExtra("from_setting", true)
        startActivity(intent)
        overridePendingTransition(0, 0)
        // removed finish() - preserve back stack for language change
    }

    private fun openMoreApps() {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("market://dev?id=8943620497392395895")).apply {
            setPackage("com.android.vending")
        }
        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/dev?id=8943620497392395895")
                )
            )
        }
    }

    private fun openPlayStoreForRating() {
        val packageName = packageName
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName")).apply {
            setPackage("com.android.vending")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
        }
        try {
            try {
                startActivity(intent)
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(this, "Unable to open app store or browser.", Toast.LENGTH_SHORT).show()
            }
        } catch (e: ActivityNotFoundException) {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("http://play.google.com/store/apps/details?id=$packageName")
                )
            )
        }
    }

    private fun toPro() {
        val intent = if (BillingPreferences.isSubscribed(this)) {
            Intent(this, ProVersionActivityDone::class.java)
        } else {
            Intent(this, ProVersionActivity::class.java)
        }
        startActivity(intent)
        overridePendingTransition(0, 0)
    }

    private fun toAbout() {
        MyPrefereces.putVueAbout(this)
        startActivity(Intent(this, AboutActivity::class.java))
        overridePendingTransition(0, 0)
    }

    private fun shareApp() {
        val shareText = if (LocaleHelper.getLanguage(this) == "ar") {
            "أنشئ ريلز قرآنية جميلة بسهولة 🎧✨\nجرّب NurMontage:\nhttps://play.google.com/store/apps/details?id=hazem.nurmontage.videoquran"
        } else {
            "Create beautiful Quran Reels easily 🎧✨\nTry NurMontage:\nhttps://play.google.com/store/apps/details?id=hazem.nurmontage.videoquran"
        }
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "Check out this app!")
            putExtra(Intent.EXTRA_TEXT, shareText)
        }
        startActivity(Intent.createChooser(intent, "Share via"))
    }

    private fun openInstagramPage() {
        val uri = Uri.parse("https://www.instagram.com/nurmontage.app/")
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            setPackage("com.instagram.android")
        }
        try {
            try {
                startActivity(intent)
            } catch (e: ActivityNotFoundException) {
                startActivity(Intent(Intent.ACTION_VIEW, uri))
            }
        } catch (_: ActivityNotFoundException) {
        }
    }

    private fun openYouTubePage() {
        val intent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("https://www.youtube.com/@NurMontageApp/")
        ).apply {
            setPackage("com.google.android.youtube")
        }
        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://www.youtube.com/@NurMontageApp/")
                )
            )
        }
    }

    private fun openTikTokPage() {
        val intent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("https://www.tiktok.com/@nurmontagesupport")
        ).apply {
            setPackage("com.zhiliaoapp.musically")
        }
        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://www.tiktok.com/@nurmontagesupport")
                )
            )
        }
    }

    // ── Dialog helpers ──────────────────────────────────────────────────

    private fun cancelDialog() {
        dialog?.let {
            if (it.isShowing) it.dismiss()
        }
        dialog = null
    }

    private fun dialogCopyRight() {
        try {
            val dlg = Dialog(this)
            dialog = dlg
            dlg.setCancelable(true)
            dlg.requestWindowFeature(1) // FEATURE_NO_TITLE
            dlg.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            dlg.window?.setBackgroundDrawable(ColorDrawable(0))

            val inflate = LayoutInflater.from(this).inflate(R.layout.layout_dialog_copyright, null)
            dlg.setContentView(inflate)

            val dialogTitle = inflate.findViewById<TextCustumFontBold>(R.id.dialog_title)
            val tvMsj = inflate.findViewById<TextCustumFont>(R.id.tv_msj)

            inflate.findViewById<View>(R.id.dialog_no).setOnClickListener {
                cancelDialog()
            }

            if (LocaleHelper.getLanguage(this) == "ar") {
                dialogTitle.text = "تنبيه حقوق الاستخدام ⚠️"
                tvMsj.text = "بعض تسجيلات تلاوات القرّاء محمية بحقوق النشر، وهي مخصّصة للاستخدام الشخصي فقط.\n\nقد تسمح بعض المنصات باستخدام هذه الأصوات دون مشاكل، لكن ذلك لا يُعدّ تصريحًا بالنشر أو الاستخدام التجاري.\n\nللنشر الآمن، يُرجى اختيار قارئ مذكور على أنه مسموح بالنشر أو استخدام صوتك الخاص.\n\nالمستخدم مسؤول بالكامل عن الالتزام بسياسات حقوق النشر الخاصة بكل منصة."
            } else {
                dialogTitle.text = "⚠️ Copyright Notice"
                tvMsj.text = "Some reciters' audio recordings are protected by copyright and are intended for personal use only.\n\nCertain platforms may allow these sounds without issues, but this does not constitute permission to publish or use them commercially.\n\nFor safe publishing, please select a reciter marked as allowed for publishing or use your own audio.\n\nThe user is solely responsible for complying with the copyright policies of each platform."
            }

            dlg.show()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // ── Billing / restore ───────────────────────────────────────────────

    private fun startBillingConnection() {
        val client = BillingClient.newBuilder(this)
            .setListener(this)
            .enablePendingPurchases()
            .build()
        billingClient = client

        client.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    checkUserSubscriptionStatus()
                }
            }

            override fun onBillingServiceDisconnected() {
                startBillingConnection()
            }
        })
    }

    private fun restoreSubscribe() {
        startBillingConnection()
    }

    private fun checkUserSubscriptionStatus() {
        billingClient?.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.SUBS).build(),
            object : PurchasesResponseListener {
                override fun onQueryPurchasesResponse(billingResult: BillingResult, purchases: List<Purchase>) {
                    if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                        if (purchases.isNotEmpty()) {
                            handleSubscriptionPurchases(purchases)
                        } else {
                            checkInAppPurchases()
                        }
                    } else {
                        checkInAppPurchases()
                    }
                }
            }
        )
    }

    private fun handleSubscriptionPurchases(purchases: List<Purchase>) {
        for (purchase in purchases) {
            if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                BillingPreferences.saveSubscriptionStatus(applicationContext, true)
                dialogStateSubscribe(true)
                return
            }
        }
        BillingPreferences.saveSubscriptionStatus(applicationContext, false)
        checkInAppPurchases()
    }

    private fun checkInAppPurchases() {
        billingClient?.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.INAPP).build(),
            object : PurchasesResponseListener {
                override fun onQueryPurchasesResponse(billingResult: BillingResult, purchases: List<Purchase>) {
                    if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                        hasPurchasedForever = false
                        for (purchase in purchases) {
                            if (purchase.products.contains(PRODUCT_ID_FOREVER) &&
                                purchase.purchaseState == Purchase.PurchaseState.PURCHASED
                            ) {
                                hasPurchasedForever = true
                                break
                            }
                        }
                    }
                    if (!hasPurchasedForever) {
                        dialogStateSubscribe(false)
                    } else {
                        BillingPreferences.saveSubscriptionStatus(applicationContext, true)
                        dialogStateSubscribe(true)
                    }
                }
            }
        )
    }

    private fun dialogStateSubscribe(isSubscribed: Boolean) {
        runOnUiThread {
            try {
                val dlg = Dialog(this@SeettingActivity)
                dialog = dlg
                dlg.setCancelable(false)
                dlg.requestWindowFeature(1) // FEATURE_NO_TITLE
                dlg.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                dlg.window?.setBackgroundDrawable(ColorDrawable(0))

                if (isSubscribed) {
                    val inflate = LayoutInflater.from(this@SeettingActivity)
                        .inflate(R.layout.layout_pro_done, null)
                    dlg.setContentView(inflate)

                    inflate.findViewById<TextCustumFontBold>(R.id.dialog_title).text =
                        mResources.getString(R.string.premium_activated)
                    inflate.findViewById<TextCustumFont>(R.id.tv_msj).text =
                        mResources.getString(R.string.subscription_restored)

                    inflate.findViewById<View>(R.id.dialog_no).setOnClickListener {
                        setPro()
                        cancelDialog()
                    }
                } else {
                    val inflate = LayoutInflater.from(this@SeettingActivity)
                        .inflate(R.layout.layout_pro_not_found, null)
                    dlg.setContentView(inflate)

                    inflate.findViewById<TextCustumFontBold>(R.id.dialog_title).text =
                        mResources.getString(R.string.nothing_to_restore)
                    inflate.findViewById<TextCustumFont>(R.id.tv_msj).text =
                        mResources.getString(R.string.msj_no_found_subscribe)

                    val typeface = Typeface.createFromAsset(
                        this@SeettingActivity.resources.assets,
                        "fonts/ReadexPro_Medium.ttf"
                    )
                    val contactBtn = inflate.findViewById<Button>(R.id.contact_us)
                    contactBtn.typeface = typeface
                    contactBtn.setOnClickListener {
                        contact()
                        cancelDialog()
                    }

                    inflate.findViewById<View>(R.id.dialog_no).setOnClickListener {
                        cancelDialog()
                    }
                }

                dlg.show()
                findViewById<View>(R.id.progress).visibility = View.GONE
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun contact() {
        var subject = mResources.getString(R.string.support_team)
        if (BillingPreferences.isSubscribed(this)) {
            subject = "$subject : "
        }
        val emailArr = arrayOf("nurmontage.contact@gmail.com")

        if (isGmailAvailable(this)) {
            val intent = Intent(Intent.ACTION_SEND).apply {
                putExtra(Intent.EXTRA_EMAIL, emailArr)
                putExtra(Intent.EXTRA_BCC, emailArr)
                putExtra(Intent.EXTRA_SUBJECT, subject)
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
                putExtra(Intent.EXTRA_EMAIL, emailArr)
                putExtra(Intent.EXTRA_BCC, emailArr)
                putExtra(Intent.EXTRA_SUBJECT, subject)
                type = "message/rfc822"
            }
            startActivity(Intent.createChooser(intent, "Send email using"))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun isGmailAvailable(context: Context): Boolean {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "message/rfc822"
            setPackage("com.google.android.gm")
        }
        return !context.packageManager.queryIntentActivities(intent, 0).isEmpty()
    }

    companion object {
        private const val PRODUCT_ID_FOREVER = "sku.nurmontage.foreiver"
    }
}
