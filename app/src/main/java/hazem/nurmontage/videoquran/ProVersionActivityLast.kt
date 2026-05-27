package hazem.nurmontage.videoquran

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.core.graphics.Insets
import androidx.core.view.OnApplyWindowInsetsListener
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.AcknowledgePurchaseResponseListener
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
import hazem.nurmontage.videoquran.Utils.BillingPreferences
import hazem.nurmontage.videoquran.Utils.LocalPersistence
import hazem.nurmontage.videoquran.Utils.LocaleHelper
import hazem.nurmontage.videoquran.Utils.NetworkUtils
import hazem.nurmontage.videoquran.adabter.FeaturesAdabter
import hazem.nurmontage.videoquran.common.Common
import hazem.nurmontage.videoquran.fragment.ProgressViewFragment
import hazem.nurmontage.videoquran.model.ModelFeatures
import hazem.nurmontage.videoquran.model.Template
import hazem.nurmontage.videoquran.views.ButtonCustumFont
import hazem.nurmontage.videoquran.views.TextCustumFont
import nl.dionsegijn.konfetti.core.PartyFactory
import nl.dionsegijn.konfetti.core.Position
import nl.dionsegijn.konfetti.core.Relative
import nl.dionsegijn.konfetti.core.Spread
import nl.dionsegijn.konfetti.core.emitter.Emitter
import nl.dionsegijn.konfetti.core.models.Shape
import nl.dionsegijn.konfetti.xml.KonfettiView
import nl.dionsegijn.konfetti.xml.image.ImageUtil
import java.util.concurrent.TimeUnit

/**
 * Pro version subscription screen — "Last" variant with 3 plan options
 * (Forever / Year / Month) using RelativeLayout-based plan selectors.
 */
class ProVersionActivityLast : BaseActivity(), PurchasesUpdatedListener {

    companion object {
        private const val PRODUCT_ID_FOREIVER = "sku.nurmontage.foreiver"
        private const val PRODUCT_ID_MONTH = "sku.nurmontage.month"
        private const val PRODUCT_ID_YEAR = "sku.nurmontage.year"
    }

    private var billingClient: BillingClient? = null
    private var btnContinue: ButtonCustumFont? = null
    private var btnForeiver: RelativeLayout? = null
    private var btnMonth: RelativeLayout? = null
    private var btnRestore: TextCustumFont? = null
    private var btnYear: RelativeLayout? = null
    private var featuresAdabter: FeaturesAdabter? = null
    private var hasPurchasedForever = false
    private var isClick = false
    private var isRestore = false
    private var ivForeiver: ImageView? = null
    private var ivMonth: ImageView? = null
    private var ivYear: ImageView? = null
    private var mResources: Resources? = null
    private var mTemplate: Template? = null
    private var tvForeiver: TextCustumFont? = null
    private var tvMonth: TextCustumFont? = null
    private var tvPriceForeiver: TextCustumFont? = null
    private var tvPriceMonth: TextCustumFont? = null
    private var tvPriceYear: TextCustumFont? = null
    private var tvYear: TextCustumFont? = null

    private val colorSelect = -206036

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

    // region Lifecycle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        EdgeToEdge.enable(this)
        setContentView(R.layout.activity_pro_version_last)
        setStatusBarColor(-15658732)
        setNavigationBarColor(-15658732)
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

        if (!NetworkUtils.isNetworkAvailable(applicationContext)) {
            Toast.makeText(this, mResources?.getString(R.string.no_internet), Toast.LENGTH_SHORT).show()
        }

        wakeLockAquire()

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
        initImgFeatures()
        initBtnHelp()

        if (isSubscribed) {
            thnks()
            return
        }

        billingClient = BillingClient.newBuilder(this)
            .setListener(this)
            .enablePendingPurchases()
            .build()
        startBillingConnection()

        btnRestore = findViewById(R.id.btn_restore)
        btnRestore?.text = mResources?.getString(R.string.restort_subscribe)
        btnRestore?.setOnClickListener {
            try {
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

        tvForeiver = findViewById(R.id.tv_foreiver)
        tvPriceYear = findViewById(R.id.tv_price_year)
        tvPriceForeiver = findViewById(R.id.tv_price_foreiver)
        btnForeiver = findViewById(R.id.btn_foreiver)
        btnYear = findViewById(R.id.btn_year)
        ivForeiver = findViewById(R.id.btn_radio_foreiver)
        ivYear = findViewById(R.id.btn_radio_year)

        btnForeiver?.setOnClickListener {
            if (productIdCurrent == PRODUCT_ID_FOREIVER) return@setOnClickListener
            btnForeiver?.setBackgroundResource(R.drawable.bg_price_select)
            btnYear?.setBackgroundResource(R.drawable.bg_price)
            btnMonth?.setBackgroundResource(R.drawable.bg_price)
            ivForeiver?.setImageResource(R.drawable.checked)
            tvForeiver?.setTextColor(colorSelect)
            tvPriceForeiver?.setTextColor(colorSelect)
            tvYear?.setTextColor(-1)
            tvPriceYear?.setTextColor(-1)
            tvMonth?.setTextColor(-1)
            tvPriceMonth?.setTextColor(-1)
            ivYear?.setImageResource(R.drawable.unchecked)
            ivMonth?.setImageResource(R.drawable.unchecked)
            productIdCurrent = PRODUCT_ID_FOREIVER
        }

        btnYear?.setOnClickListener {
            if (productIdCurrent == PRODUCT_ID_YEAR) return@setOnClickListener
            btnYear?.setBackgroundResource(R.drawable.bg_price_select)
            btnForeiver?.setBackgroundResource(R.drawable.bg_price)
            btnMonth?.setBackgroundResource(R.drawable.bg_price)
            productIdCurrent = PRODUCT_ID_YEAR
            ivYear?.setImageResource(R.drawable.checked)
            ivForeiver?.setImageResource(R.drawable.unchecked)
            ivMonth?.setImageResource(R.drawable.unchecked)
            tvYear?.setTextColor(colorSelect)
            tvPriceYear?.setTextColor(colorSelect)
            tvForeiver?.setTextColor(-1)
            tvPriceForeiver?.setTextColor(-1)
            tvMonth?.setTextColor(-1)
            tvPriceMonth?.setTextColor(-1)
        }

        btnMonth?.setOnClickListener {
            if (productIdCurrent == PRODUCT_ID_MONTH) return@setOnClickListener
            btnMonth?.setBackgroundResource(R.drawable.bg_price_select)
            btnForeiver?.setBackgroundResource(R.drawable.bg_price)
            btnYear?.setBackgroundResource(R.drawable.bg_price)
            productIdCurrent = PRODUCT_ID_MONTH
            ivMonth?.setImageResource(R.drawable.checked)
            ivYear?.setImageResource(R.drawable.unchecked)
            ivForeiver?.setImageResource(R.drawable.unchecked)
            tvMonth?.setTextColor(colorSelect)
            tvPriceMonth?.setTextColor(colorSelect)
            tvYear?.setTextColor(-1)
            tvPriceYear?.setTextColor(-1)
            tvForeiver?.setTextColor(-1)
            tvPriceForeiver?.setTextColor(-1)
        }

        val btnContinueView: ButtonCustumFont = findViewById(R.id.btn_continue)
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

    // region UI Setup

    private fun initBtnHelp() {
        findViewById<View>(R.id.btn_contact).setOnClickListener { contact() }
    }

    private fun initImgFeatures() {
        findViewById<View>(R.id.ytb_layout).setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:3xtsWfMQ5KM"))
            val intent2 = Intent(Intent.ACTION_VIEW, Uri.parse("https://youtu.be/3xtsWfMQ5KM"))
            try {
                startActivity(intent)
            } catch (_: ActivityNotFoundException) {
                startActivity(intent2)
            }
        }

        if (LocaleHelper.getLanguage(this) == "ar") {
            val tvProAr: TextCustumFont = findViewById(R.id.tv_pro_ar)
            tvProAr.visibility = View.VISIBLE
            tvProAr.text = mResources?.getString(R.string.pro)
            val tvFreeAr: TextCustumFont = findViewById(R.id.tv_free_ar)
            tvFreeAr.visibility = View.VISIBLE
            tvFreeAr.text = mResources?.getString(R.string.free)
            findViewById<View>(R.id.tv_pro).visibility = View.GONE
        } else {
            val tvPro: TextCustumFont = findViewById(R.id.tv_pro)
            tvPro.visibility = View.VISIBLE
            tvPro.text = mResources?.getString(R.string.pro)
            val tvFree: TextCustumFont = findViewById(R.id.tv_free)
            tvFree.visibility = View.VISIBLE
            tvFree.text = mResources?.getString(R.string.free)
        }

        (findViewById<TextCustumFont>(R.id.tv_tittle_billing)).text = mResources?.getString(R.string.unlock_premium)

        val recyclerView: RecyclerView = findViewById(R.id.rv)
        recyclerView.setHasFixedSize(true)
        recyclerView.setItemViewCacheSize(20)
        @Suppress("DEPRECATION")
        recyclerView.isDrawingCacheEnabled = true
        recyclerView.itemAnimator = null
        recyclerView.layoutManager = LinearLayoutManager(this)

        val featureList = ArrayList<ModelFeatures>()
        val stringArray = mResources?.getStringArray(R.array.feature_list) ?: emptyArray()
        for (i in stringArray.indices) {
            if (i == 0 || i == 1) {
                featureList.add(ModelFeatures(stringArray[i], true))
            } else {
                featureList.add(ModelFeatures(stringArray[i]))
            }
        }
        val adapter = FeaturesAdabter(featureList)
        featuresAdabter = adapter
        recyclerView.adapter = adapter
    }

    // endregion

    // region Email/Contact

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
        val emails = arrayOf("hazemourari08@gmail.com")
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

    // region Billing

    private fun startBillingConnection() {
        billingClient?.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    checkUserSubscriptionStatus()
                    queryProducts()
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
            when (productDetails.productId) {
                PRODUCT_ID_YEAR -> tvPriceYear?.text = formattedPrice ?: "N/A"
                PRODUCT_ID_MONTH -> tvPriceMonth?.text = formattedPrice ?: "N/A"
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
        try {
            findViewById<View>(R.id.container_progress).visibility = View.VISIBLE
            if (isFinishing || supportFragmentManager.isDestroyed) return
            val beginTransaction = supportFragmentManager.beginTransaction()
            beginTransaction.replace(R.id.container_progress, ProgressViewFragment.getInstance())
            beginTransaction.commit()
        } catch (_: Exception) {
        }
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
        // No active subscription or one-time purchase found
        hideProgressFragment()
    }

    // endregion

    // region Confetti / Thanks

    fun explode() {
        val loadDrawable = ImageUtil.loadDrawable(
            ContextCompat.getDrawable(applicationContext, R.drawable.favorite_24px), true, true
        )
        val konfettiView: KonfettiView = findViewById(R.id.konfettiView)
        konfettiView.visibility = View.VISIBLE
        konfettiView.start(
            PartyFactory(Emitter(2800L, TimeUnit.MILLISECONDS).max(100))
                .spread(Spread.ROUND)
                .shapes(listOf(Shape.Square, Shape.Circle, loadDrawable))
                .colors(listOf(16572810, 16740973, 16003181, 11832815))
                .setSpeedBetween(0f, 30f)
                .position(Relative(0.5, 0.3))
                .getParty()
        )
    }

    private fun thnks() {
        try {
            runOnUiThread {
                findViewById<View>(R.id.restore).visibility = View.GONE
                findViewById<View>(R.id.layout_price).visibility = View.GONE
                findViewById<View>(R.id.view_success).visibility = View.VISIBLE
                (findViewById<TextCustumFont>(R.id.tv_thanks)).text = mResources?.getString(R.string.thanks_hint)
                findViewById<View>(R.id.tv_tittle_billing).visibility = View.GONE
                btnContinue = findViewById(R.id.btn_done)
                btnContinue?.text = mResources?.getString(R.string.done)
                btnContinue?.setOnClickListener {
                    onBackPressedCallback.handleOnBackPressed()
                }
                featuresAdabter?.setSubscribe(true)
                explode()
            }
        } catch (_: Exception) {
        }
    }

    // endregion
}
