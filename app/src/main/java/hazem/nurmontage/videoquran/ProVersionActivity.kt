package hazem.nurmontage.videoquran

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.ProductDetailsResponseListener
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesResponseListener
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.google.android.material.card.MaterialCardView
import hazem.nurmontage.videoquran.Utils.AppUtils
import hazem.nurmontage.videoquran.Utils.BillingPreferences
import hazem.nurmontage.videoquran.Utils.LocalPersistence
import hazem.nurmontage.videoquran.Utils.LocaleHelper
import hazem.nurmontage.videoquran.Utils.NetworkUtils
import hazem.nurmontage.videoquran.Utils.ScreenUtils
import hazem.nurmontage.videoquran.adabter.FeaturesAdabter
import hazem.nurmontage.videoquran.adabter.ImgAdapter
import hazem.nurmontage.videoquran.common.Common
import hazem.nurmontage.videoquran.fragment.ProgressViewFragment
import hazem.nurmontage.videoquran.model.ModelFeatures
import hazem.nurmontage.videoquran.model.Template
import hazem.nurmontage.videoquran.views.ButtonCustumFontBilling
import hazem.nurmontage.videoquran.views.TextCustumFont
import hazem.nurmontage.videoquran.views.TextCustumFontBold
import nl.dionsegijn.konfetti.core.PartyFactory
import nl.dionsegijn.konfetti.core.Position
import nl.dionsegijn.konfetti.core.Relative
import nl.dionsegijn.konfetti.core.Spread
import nl.dionsegijn.konfetti.core.emitter.Emitter
import nl.dionsegijn.konfetti.core.models.Shape
import nl.dionsegijn.konfetti.xml.KonfettiView
import nl.dionsegijn.konfetti.xml.image.ImageUtil
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale
import java.util.concurrent.TimeUnit

/**
 * Pro version subscription screen — main variant with 2 plan options
 * (Month / Year) using MaterialCardView-based plan selectors.
 * Includes auto-scrolling image showcase, confetti, restore purchases.
 */
class ProVersionActivity : BaseActivity(), PurchasesUpdatedListener {

    companion object {
        private const val PRODUCT_ID_FOREIVER = "sku.nurmontage.foreiver"
        private const val PRODUCT_ID_MONTH = "sku.nurmontage.month"
        private const val PRODUCT_ID_YEAR = "sku.nurmontage.year"
        private const val VIDEO_ID = "DY76bAh7i3M"
    }

    private var billingClient: BillingClient? = null
    private var btnContinue: ButtonCustumFontBilling? = null
    private var btnForeiver: MaterialCardView? = null
    private var btnRestore: Button? = null
    private var btnYear: MaterialCardView? = null
    private var dialog: Dialog? = null
    private var featuresAdabter: FeaturesAdabter? = null
    private var hasPurchasedForever = false
    private var isBtnRestore = false
    private var isClick = false
    private var ivForeiver: ImageView? = null
    private var ivYear: ImageView? = null
    private var mResources: Resources? = null
    private var mTemplate: Template? = null
    private var recyclerView: RecyclerView? = null
    private var tvByMonth: TextCustumFont? = null
    private var tvNoCommitmentAr: TextCustumFont? = null
    private var tvNoCommitmentEn: TextCustumFont? = null
    private var tvBest: TextView? = null
    private var tvPriceForeiver: TextCustumFontBold? = null
    private var tvPriceYear: TextCustumFontBold? = null

    // TODO: YouTube player integration
    // private var youTubePlayer: YouTubePlayer? = null
    // private var youTubePlayerView: YouTubePlayerView? = null

    private val colorSelect = -1

    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            if (mTemplate != null) {
                toTrackAct()
            }
            finish()
        }
    }

    private val productDetailsMap = HashMap<String, ProductDetails>()
    private var productIdCurrent = PRODUCT_ID_YEAR

    private val autoScrollHandler = Handler(Looper.getMainLooper())
    private var isUserScrolling = false

    private val autoScrollRunnable = object : Runnable {
        override fun run() {
            try {
                val rv = recyclerView ?: return
                if (isUserScrolling) return
                val layoutManager = rv.layoutManager as? LinearLayoutManager ?: return
                val firstVisible = layoutManager.findFirstVisibleItemPosition()
                if (firstVisible == -1) {
                    autoScrollHandler.postDelayed(this, 16)
                    return
                }
                val itemCount = layoutManager.itemCount / 3
                if (firstVisible >= itemCount * 2) {
                    rv.scrollToPosition(firstVisible - itemCount)
                } else if (firstVisible < itemCount) {
                    rv.scrollToPosition(firstVisible + itemCount)
                } else {
                    rv.scrollBy(2, 0)
                }
                autoScrollHandler.postDelayed(this, 16)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // region Lifecycle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        EdgeToEdge.enable(this)
        setContentView(R.layout.activity_pro_version)
        setStatusBarColor(ViewCompat.MEASURED_STATE_MASK)
        setNavigationBarColor(ViewCompat.MEASURED_STATE_MASK)
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)

        val insetsController = WindowCompat.getInsetsController(window, window.decorView)
        insetsController.isAppearanceLightStatusBars = false
        insetsController.isAppearanceLightNavigationBars = false

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { view, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(insets.left, insets.top, insets.right, insets.bottom)
            windowInsets
        }

        val resources = getResources()
        mResources = resources
        if (resources == null) {
            finish()
        }

        wakeLockAquire()

        (findViewById<TextView>(R.id.mtittle)).text = mResources?.getString(R.string.enjoy_all_premium_features)
        (findViewById<TextView>(R.id.hint_review)).text = mResources?.getString(R.string._4_8_434_reviews_28k_users)

        if (intent != null) {
            val stringExtra = intent.getStringExtra(Common.TEMPLATE)
            if (stringExtra != null) {
                mTemplate = LocalPersistence.readObjectFromFile(this, stringExtra) as? Template
            }
        }

        findViewById<View>(R.id.btn_on_back).setOnClickListener {
            onBackPressedCallback.handleOnBackPressed()
        }

        val isSubscribed = BillingPreferences.isSubscribed(applicationContext)
        setupImg()

        if (isSubscribed) {
            thnks()
            return
        }

        if (!NetworkUtils.isNetworkAvailable(applicationContext)) {
            Toast.makeText(this, mResources?.getString(R.string.no_internet), Toast.LENGTH_SHORT).show()
        }

        billingClient = BillingClient.newBuilder(this)
            .setListener(this)
            .enablePendingPurchases()
            .build()
        startBillingConnection()

        val btnRestoreView: Button = findViewById(R.id.restore)
        btnRestore = btnRestoreView
        btnRestoreView.text = mResources?.getString(R.string.restort_subscribe)
        btnRestore?.setOnClickListener {
            try {
                isBtnRestore = true
                if (billingClient != null && billingClient!!.isReady) {
                    showProgress()
                    checkUserSubscriptionStatus()
                } else {
                    startBillingConnection()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        val tvBestValue: TextView = findViewById(R.id.tv_best_value)
        tvBest = tvBestValue
        tvBestValue.text = mResources?.getString(R.string.best_value)

        tvPriceYear = findViewById(R.id.tv_price_year)
        tvPriceForeiver = findViewById(R.id.tv_price_month)
        tvByMonth = findViewById(R.id.tv_year_bymonth)
        btnForeiver = findViewById(R.id.btn_month)
        btnYear = findViewById(R.id.btn_year)
        ivForeiver = findViewById(R.id.btn_radio_month)
        ivYear = findViewById(R.id.btn_radio_year)

        btnForeiver?.setOnClickListener {
            if (productIdCurrent == PRODUCT_ID_MONTH) return@setOnClickListener
            btnForeiver?.setStrokeColor(-932849)
            btnYear?.setStrokeColor(-13617603)
            ivForeiver?.setImageResource(R.drawable.checked)
            tvPriceForeiver?.setTextColor(-1)
            tvPriceYear?.setTextColor(-1)
            ivYear?.setImageResource(R.drawable.unchecked)
            productIdCurrent = PRODUCT_ID_MONTH
            tvBest?.setBackgroundResource(R.drawable.bg_badge_inactive)
        }

        btnYear?.setOnClickListener {
            if (productIdCurrent == PRODUCT_ID_YEAR) return@setOnClickListener
            btnYear?.setStrokeColor(-932849)
            btnForeiver?.setStrokeColor(-13617603)
            productIdCurrent = PRODUCT_ID_YEAR
            ivYear?.setImageResource(R.drawable.checked)
            ivForeiver?.setImageResource(R.drawable.unchecked)
            tvPriceYear?.setTextColor(-1)
            tvPriceForeiver?.setTextColor(-1)
            tvBest?.setBackgroundResource(R.drawable.bg_best_value_badge)
        }

        val btnContinueView: ButtonCustumFontBilling = findViewById(R.id.btn_continue)
        btnContinue = btnContinueView
        btnContinueView.text = mResources?.getString(R.string.subscribe_now)
        btnContinue?.setOnClickListener {
            try {
                if (isClick) return@setOnClickListener
                isClick = true
                showProgress()
                val productDetails = productDetailsMap[productIdCurrent]
                if (productDetails == null) return@setOnClickListener
                if (productIdCurrent == PRODUCT_ID_FOREIVER) {
                    launchPurchaseFlowINAPP(productDetails)
                } else {
                    launchPurchaseFlowSUB(productDetails)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        tvNoCommitmentAr = findViewById(R.id.tv_hint_ar)
        tvNoCommitmentEn = findViewById(R.id.tv_hint_en)
        if (LocaleHelper.getLanguage(this) == "ar") {
            tvNoCommitmentAr?.visibility = View.VISIBLE
            tvNoCommitmentAr?.text = mResources?.getString(R.string.no_commitment)
        } else {
            tvNoCommitmentEn?.visibility = View.VISIBLE
            tvNoCommitmentEn?.text = mResources?.getString(R.string.no_commitment)
        }
    }

    override fun onPause() {
        super.onPause()
        cancelDialog()
    }

    // endregion

    // region Navigation

    private fun toTrackAct() {
        val intent = Intent(this, EngineActivity::class.java)
        mTemplate?.let { intent.putExtra(Common.TEMPLATE, it.idTemplate) }
        intent.addFlags(65536)
        startActivity(intent)
        overridePendingTransition(0, 0)
    }

    // endregion

    // region Dialog

    private fun showFeatures() {
        try {
            val d = Dialog(this)
            dialog = d
            d.setCancelable(true)
            d.requestWindowFeature(1)
            d.window?.setLayout(-1, -2)
            d.window?.setBackgroundDrawable(ColorDrawable(0))
            val view = LayoutInflater.from(this).inflate(R.layout.layout_dialog_premuim, null as ViewGroup?)
            d.setContentView(view)
            view.findViewById<View>(R.id.dialog_title).visibility = View.GONE
            view.findViewById<View>(R.id.dialog_no).setOnClickListener { cancelDialog() }
            d.show()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun cancelDialog() {
        dialog?.dismiss()
        dialog = null
    }

    // endregion

    // region Help/Contact

    private fun help() {
        try {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse("https://chat.whatsapp.com/F0kqOjZS1VuBAvoiOG4XEZ")
            intent.setPackage("com.whatsapp")
            startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun initBtnHelp(isSubscribed: Boolean) {
        findViewById<View>(R.id.layout_help).visibility = View.VISIBLE
        val typeface = Typeface.createFromAsset(assets, "fonts/ReadexPro_Medium.ttf")
        val button: Button = findViewById(R.id.btn_help)
        button.typeface = typeface
        button.setOnClickListener { contact() }
    }

    private fun isGmailAvailable(context: Context): Boolean {
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "message/rfc822"
        intent.setPackage("com.google.android.gm")
        return !context.packageManager.queryIntentActivities(intent, 0).isEmpty()
    }

    fun contact() {
        var subject = mResources?.getString(R.string.support_team) ?: ""
        if (BillingPreferences.isSubscribed(this)) {
            subject = "$subject : "
        }
        val emails = arrayOf("nurmontage.contact@gmail.com")
        if (isGmailAvailable(this)) {
            val intent = Intent(Intent.ACTION_SEND)
            intent.putExtra(Intent.EXTRA_EMAIL, emails)
            intent.putExtra(Intent.EXTRA_BCC, emails)
            intent.putExtra(Intent.EXTRA_SUBJECT, subject)
            intent.type = "message/rfc822"
            intent.setPackage("com.google.android.gm")
            try {
                startActivity(intent)
                return
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        try {
            val intent2 = Intent(Intent.ACTION_SEND)
            intent2.putExtra(Intent.EXTRA_EMAIL, emails)
            intent2.putExtra(Intent.EXTRA_BCC, emails)
            intent2.putExtra(Intent.EXTRA_SUBJECT, subject)
            intent2.type = "message/rfc822"
            startActivity(Intent.createChooser(intent2, "Send email using"))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // endregion

    // region Features

    private fun initImgFeatures() {
        val rv: RecyclerView = findViewById(R.id.rv)
        rv.layoutManager = LinearLayoutManager(this)
        rv.isNestedScrollingEnabled = false
        rv.setHasFixedSize(false)
        rv.itemAnimator = null
        val featureList = ArrayList<ModelFeatures>()
        for (str in mResources?.getStringArray(R.array.feature_list) ?: emptyArray()) {
            featureList.add(ModelFeatures(str))
        }
        val adapter = FeaturesAdabter(featureList)
        featuresAdabter = adapter
        rv.adapter = adapter
    }

    // endregion

    // region Image Showcase

    private fun setupImg() {
        val rv: RecyclerView = findViewById(R.id.rv_img)
        recyclerView = rv
        rv.post {
            var height = (rv.height * 0.95f).toInt()
            if (height == 0) {
                height = (ScreenUtils.getScreenHeight(this@ProVersionActivity) * 0.4f).toInt()
            }
            val baseList = arrayListOf(
                R.drawable.nur_2,
                R.drawable.nur_3,
                R.drawable.nur_4,
                R.drawable.nur_1
            )
            val tripled = ArrayList<Int>().apply {
                addAll(baseList)
                addAll(baseList)
                addAll(baseList)
            }
            val imgAdapter = ImgAdapter(AppUtils.getAppVersionName(this@ProVersionActivity), tripled, height)
            val layoutManager = LinearLayoutManager(this@ProVersionActivity, RecyclerView.HORIZONTAL, false)
            layoutManager.isItemPrefetchEnabled = true
            layoutManager.initialPrefetchItemCount = 6
            rv.layoutManager = layoutManager
            rv.adapter = imgAdapter
            rv.setHasFixedSize(true)
            rv.setItemViewCacheSize(12)
            rv.itemAnimator = null
            rv.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)
                    if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                        isUserScrolling = true
                    } else if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                        isUserScrolling = false
                        startAutoScroll()
                    }
                }
            })
            rv.post { rv.scrollToPosition(baseList.size) }
            startAutoScroll()
        }
    }

    private fun startAutoScroll() {
        try {
            autoScrollHandler.removeCallbacks(autoScrollRunnable)
            autoScrollHandler.postDelayed(autoScrollRunnable, 250)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun stopAutoScroll() {
        autoScrollHandler.removeCallbacks(autoScrollRunnable)
    }

    // endregion

    // region Billing

    private fun startBillingConnection() {
        billingClient?.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    checkUserSubscriptionStatus()
                    querySubscribe()
                    queryPurchases()
                }
            }

            override fun onBillingServiceDisconnected() {
                startBillingConnection()
            }
        })
    }

    private fun queryProducts() {
        val productList = arrayListOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(PRODUCT_ID_FOREIVER)
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        )
        billingClient?.queryProductDetailsAsync(
            QueryProductDetailsParams.newBuilder().setProductList(productList).build(),
            object : ProductDetailsResponseListener {
                override fun onProductDetailsResponse(
                    billingResult: BillingResult,
                    list: List<ProductDetails>
                ) {
                    if (billingResult.responseCode != BillingClient.BillingResponseCode.OK) return
                    for (productDetails in list) {
                        productDetailsMap[productDetails.productId] = productDetails
                        runOnUiThread { updateUI(productDetails) }
                    }
                }
            }
        )
    }

    private fun queryUserPurchases() {
        billingClient?.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.INAPP).build()
        ) { billingResult, list ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                hasPurchasedForever = false
                if (list != null) {
                    for (purchase in list) {
                        if (purchase.products.contains(PRODUCT_ID_FOREIVER) && purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                            hasPurchasedForever = true
                            break
                        }
                    }
                }
            } else {
                Log.w("Billing", "Error querying INAPP purchases: ${billingResult.debugMessage}")
            }
            Log.e("hasPurchasedForever", "$hasPurchasedForever")
            checkInAppPurchases()
        }
    }

    private fun querySubscribe() {
        val productList = arrayListOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(PRODUCT_ID_YEAR)
                .setProductType(BillingClient.ProductType.SUBS)
                .build(),
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(PRODUCT_ID_MONTH)
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
        )
        billingClient?.queryProductDetailsAsync(
            QueryProductDetailsParams.newBuilder().setProductList(productList).build(),
            object : ProductDetailsResponseListener {
                override fun onProductDetailsResponse(
                    billingResult: BillingResult,
                    list: List<ProductDetails>
                ) {
                    if (billingResult.responseCode != BillingClient.BillingResponseCode.OK) return
                    for (productDetails in list) {
                        productDetailsMap[productDetails.productId] = productDetails
                        runOnUiThread { updateUI(productDetails) }
                    }
                }
            }
        )
    }

    private fun updateUI(productDetails: ProductDetails) {
        if (productDetails.productType == BillingClient.ProductType.INAPP) {
            val oneTime = productDetails.oneTimePurchaseOfferDetails
            if (oneTime != null) {
                tvPriceForeiver?.text = formatPriceWithSymbol(oneTime.priceAmountMicros, oneTime.priceCurrencyCode)
            } else {
                tvPriceForeiver?.text = "N/A"
            }
            return
        }
        if (productDetails.productType == BillingClient.ProductType.SUBS) {
            val pricingPhase = productDetails.subscriptionOfferDetails?.get(0)
                ?.pricingPhases?.pricingPhaseList?.get(0) ?: return
            val priceAmountMicros = pricingPhase.priceAmountMicros
            val priceCurrencyCode = pricingPhase.priceCurrencyCode
            val productId = productDetails.productId
            if (PRODUCT_ID_YEAR == productId) {
                tvPriceYear?.text = formatPriceWithSymbol(priceAmountMicros, priceCurrencyCode)
                val perMonth = formatPriceWithSymbol(Math.round(priceAmountMicros / 12.0), priceCurrencyCode)
                val sb = StringBuilder()
                if (LocaleHelper.getLanguage(applicationContext) == "ar") {
                    sb.append("فقط ").append(perMonth).append(" /شهر")
                } else {
                    sb.append("Only ").append(perMonth).append(" /month")
                }
                tvByMonth?.text = sb
                return
            }
            if (PRODUCT_ID_MONTH == productId) {
                tvPriceForeiver?.text = formatPriceWithSymbol(priceAmountMicros, priceCurrencyCode)
            }
        }
    }

    private fun formatPriceWithSymbol(micros: Long, currencyCode: String): String {
        val formatter = NumberFormat.getCurrencyInstance(Locale.US)
        formatter.currency = Currency.getInstance(currencyCode)
        formatter.maximumFractionDigits = 2
        formatter.minimumFractionDigits = 2
        return formatter.format(micros / 1000000.0)
    }

    private fun updateUILast(productDetails: ProductDetails) {
        var formattedPrice: String? = null
        if (productDetails.productType == BillingClient.ProductType.INAPP) {
            formattedPrice = productDetails.oneTimePurchaseOfferDetails?.formattedPrice
            tvPriceForeiver?.text = formattedPrice ?: "N/A"
            return
        }
        if (productDetails.productType == BillingClient.ProductType.SUBS) {
            val offerDetails = productDetails.subscriptionOfferDetails
            if (offerDetails != null && offerDetails.isNotEmpty()) {
                val subOffer = offerDetails[0]
                val phases = subOffer.pricingPhases
                if (phases != null && phases.pricingPhaseList.isNotEmpty()) {
                    formattedPrice = phases.pricingPhaseList[0].formattedPrice
                }
            }
            if (PRODUCT_ID_YEAR == productDetails.productId) {
                tvPriceYear?.text = formattedPrice ?: "N/A"
            }
        }
    }

    private fun queryPurchases() {
        billingClient?.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.INAPP).build()
        ) { billingResult, list ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                for (purchase in list) {
                    handlePurchase(purchase)
                }
            }
        }
    }

    private fun showProgress() {
        findViewById<View>(R.id.container_progress).visibility = View.VISIBLE
        if (isFinishing || supportFragmentManager.isDestroyed) return
        val beginTransaction = supportFragmentManager.beginTransaction()
        beginTransaction.replace(R.id.container_progress, ProgressViewFragment.getInstance())
        beginTransaction.commit()
    }

    private fun hideProgressFragment() {
        try {
            isClick = false
            if (!isFinishing && !supportFragmentManager.isDestroyed) {
                val fm = supportFragmentManager
                val beginTransaction = fm.beginTransaction()
                val fragment = fm.findFragmentById(R.id.container_progress)
                if (fragment != null) {
                    beginTransaction.remove(fragment)
                }
                beginTransaction.commit()
            }
        } catch (_: Exception) {
        }
        findViewById<View>(R.id.container_progress).visibility = View.GONE
    }

    private fun launchPurchaseFlowINAPP(productDetails: ProductDetails) {
        val paramsList = arrayListOf(
            BillingFlowParams.ProductDetailsParams.newBuilder()
                .setProductDetails(productDetails)
                .build()
        )
        billingClient?.launchBillingFlow(
            this,
            BillingFlowParams.newBuilder().setProductDetailsParamsList(paramsList).build()
        )
    }

    private fun launchPurchaseFlowSUB(productDetails: ProductDetails) {
        val offerToken = findOfferToken(productDetails) ?: return
        val paramsList = arrayListOf(
            BillingFlowParams.ProductDetailsParams.newBuilder()
                .setProductDetails(productDetails)
                .setOfferToken(offerToken)
                .build()
        )
        billingClient?.launchBillingFlow(
            this,
            BillingFlowParams.newBuilder().setProductDetailsParamsList(paramsList).build()
        )
    }

    private fun findOfferToken(productDetails: ProductDetails): String? {
        val offerDetails = productDetails.subscriptionOfferDetails ?: return null
        if (offerDetails.isEmpty()) return null
        return offerDetails[0].offerToken
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, list: List<Purchase>?) {
        hideProgressFragment()
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && list != null) {
            for (purchase in list) {
                handlePurchase(purchase)
            }
        }
    }

    private fun handlePurchase(purchase: Purchase) {
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            if (purchase.isAcknowledged) return
            billingClient?.acknowledgePurchase(
                AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
                    .build()
            ) { billingResult ->
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    Log.e("Billing", "$PRODUCT_ID_FOREIVER acknowledged.")
                } else {
                    Log.e("Billing", "Failed to acknowledge $PRODUCT_ID_FOREIVER: ${billingResult.debugMessage}")
                }
            }
            BillingPreferences.saveSubscriptionStatus(applicationContext, true)
            playVibration()
            thnks()
            return
        }
        if (purchase.purchaseState == Purchase.PurchaseState.PENDING) {
            runOnUiThread {
                Toast.makeText(applicationContext, "Purchase is pending", Toast.LENGTH_SHORT).show()
            }
        } else {
            runOnUiThread {
                Toast.makeText(applicationContext, "Purchase is in unknown state", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun checkUserSubscriptionStatus() {
        billingClient?.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.SUBS).build()
        ) { billingResult, list ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                if (list.isNotEmpty()) {
                    handleSubscriptionPurchases(list)
                } else {
                    checkInAppPurchases()
                }
            } else {
                checkInAppPurchases()
            }
        }
    }

    private fun checkInAppPurchases() {
        billingClient?.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.INAPP).build()
        ) { billingResult, list ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                hasPurchasedForever = false
                for (purchase in list) {
                    if (purchase.products.contains(PRODUCT_ID_FOREIVER) && purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                        hasPurchasedForever = true
                        break
                    }
                }
            }
            if (!hasPurchasedForever) {
                handleNoPurchases()
            } else {
                runOnUiThread { hideProgressFragment() }
                BillingPreferences.saveSubscriptionStatus(applicationContext, true)
                thnks()
            }
        }
    }

    private fun handleSubscriptionPurchases(purchases: List<Purchase>) {
        for (purchase in purchases) {
            if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                if (!purchase.isAcknowledged) {
                    billingClient?.acknowledgePurchase(
                        AcknowledgePurchaseParams.newBuilder()
                            .setPurchaseToken(purchase.purchaseToken)
                            .build()
                    ) { billingResult ->
                        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                            Log.e("Billing", "Subscription acknowledged.")
                        } else {
                            Log.e("Billing", "Failed to acknowledge subscription: ${billingResult.debugMessage}")
                        }
                    }
                }
                BillingPreferences.saveSubscriptionStatus(applicationContext, true)
                playVibration()
                thnks()
                return
            }
        }
        checkInAppPurchases()
    }

    private fun handleNoPurchases() {
        hideProgressFragment()
    }

    // endregion

    // region Confetti / Thanks

    fun explode() {
        val loadDrawable = ImageUtil.loadDrawable(
            ContextCompat.getDrawable(applicationContext, R.drawable.ic_heart), true, true
        )
        val konfettiView: KonfettiView = findViewById(R.id.konfettiView)
        konfettiView.visibility = View.VISIBLE
        konfettiView.start(
            PartyFactory(Emitter(3500L, TimeUnit.MILLISECONDS).max(100))
                .spread(Spread.ROUND)
                .shapes(listOf(loadDrawable))
                .colors(listOf(-1216136524, -1124760279, -2019220, -1124760279))
                .setSpeedBetween(0f, 30f)
                .position(Relative(0.5, 0.3))
                .getParty()
        )
    }

    private fun thnks() {
        try {
            runOnUiThread {
                findViewById<View>(R.id.tv_hint_ar).visibility = View.GONE
                findViewById<View>(R.id.tv_hint_en).visibility = View.GONE
                findViewById<View>(R.id.btn_continue).visibility = View.GONE
                findViewById<View>(R.id.layout_price).visibility = View.GONE
                val tvThanks = findViewById<TextCustumFont>(R.id.tv_thanks)
                tvThanks.text = mResources?.getString(R.string.thanks_hint)
                tvThanks.visibility = View.VISIBLE
                initBtnHelp(true)
                explode()
            }
        } catch (_: Exception) {
        }
    }

    // endregion
}
