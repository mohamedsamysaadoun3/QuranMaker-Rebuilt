package hazem.nurmontage.videoquran

import android.content.Intent
import android.os.Bundle
import android.util.Pair
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.recyclerview.widget.RecyclerView
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ConsumeParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.ProductDetailsResponseListener
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesResponseListener
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import hazem.nurmontage.videoquran.Utils.AppUtils
import hazem.nurmontage.videoquran.Utils.LocaleHelper
import hazem.nurmontage.videoquran.Utils.NonScrollableLinearLayoutManager
import hazem.nurmontage.videoquran.Utils.PriceFormatter
import hazem.nurmontage.videoquran.Utils.ScreenUtils
import hazem.nurmontage.videoquran.adabter.AboutAdabters
import hazem.nurmontage.videoquran.views.ButtonCustumFont
import hazem.nurmontage.videoquran.views.TextCustumFont

/**
 * Activity for supporting the developer through in-app purchases (donations).
 * Displays several donation tiers ($10, $50, $100, $1000) fetched from
 * Google Play Billing, and lets the user complete a one-time purchase.
 */
class SupportBillingActivity : BaseActivity(), PurchasesUpdatedListener {

    // ── Product IDs ────────────────────────────────────────────────────

    companion object {
        private const val PRODUCT_ID_10 = "sku.nurmontage.min"
        private const val PRODUCT_ID_50 = "sku.nurmontage.medium"
        private const val PRODUCT_ID_100 = "sku.nurmontage.mmedium"
        private const val PRODUCT_ID_1000 = "sku.nurmontage.max"

        private var productIdCurrent = PRODUCT_ID_50
    }

    // ── Fields ─────────────────────────────────────────────────────────

    private var billingClient: BillingClient? = null
    private lateinit var btnLaunch: ButtonCustumFont
    private lateinit var viewPrice10: ButtonCustumFont
    private lateinit var viewPrice50: ButtonCustumFont
    private lateinit var viewPrice100: ButtonCustumFont
    private lateinit var viewPrice1000: ButtonCustumFont
    private var priceSelect: Int = R.id.view_50

    private val productDetailsMap = mutableMapOf<String, ProductDetails>()

    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            finish()
        }
    }

    // ── Lifecycle ──────────────────────────────────────────────────────

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_support_billing)

        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { view, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(insets.left, insets.top, insets.right, insets.bottom)
            windowInsets
        }

        setStatusBarColor(-1) // white
        setNavigationBarColor(-1) // white
        val insetsController = WindowCompat.getInsetsController(window, window.decorView)
        insetsController.isAppearanceLightStatusBars = true
        insetsController.isAppearanceLightNavigationBars = true

        wakeLockAcquire()

        val resources = resources ?: run { finish(); return }

        init()
        initImgBilling()

        // Back button
        findViewById<View>(R.id.btn_on_back).setOnClickListener {
            onBackPressedCallback.handleOnBackPressed()
        }

        // Set up BillingClient
        billingClient = BillingClient.newBuilder(this)
            .setListener(this)
            .enablePendingPurchases()
            .build()
        startBillingConnection()
    }

    // ── Wake lock (kept from original Base class) ──────────────────────

    override fun wakeLockAcquire() {
        try {
            window.addFlags(0x00000080) // FLAG_KEEP_SCREEN_ON
        } catch (_: Exception) {
        }
    }

    // ── UI initialization ──────────────────────────────────────────────

    private fun initImgBilling() {
        ScreenUtils.getScreenWidth(this)
    }

    private fun init() {
        val language = LocaleHelper.getLanguage(applicationContext)

        // Title
        val tvAya = findViewById<TextCustumFont>(R.id.tv_aya)
        tvAya.setText(R.string.tittle_billing)
        if (language == "ar") {
            tvAya.textSize = 16f
        }

        // About items
        // NOTE: AboutAdabters is functional in Java; could be converted to Kotlin later
        val aboutItems = ArrayList<AboutAdabters.ModelAbout>()
        val gravity = if (language == "ar") 5 else GravityCompat.START

        aboutItems.add(AboutAdabters.ModelAbout(14, Pair("<font color='#000000'>${getString(R.string.about_question_1)}</font>", gravity)))
        aboutItems.add(AboutAdabters.ModelAbout(14, Pair("\n", gravity)))
        aboutItems.add(AboutAdabters.ModelAbout(14, Pair("<font color='#000000'>${getString(R.string.about_question_2)}</font>", gravity)))
        aboutItems.add(AboutAdabters.ModelAbout(14, Pair("\n", gravity)))
        aboutItems.add(AboutAdabters.ModelAbout(14, Pair("<font color='#000000'>${getString(R.string.about_question_3)}</font>", gravity)))
        aboutItems.add(AboutAdabters.ModelAbout(14, Pair("\n\n", gravity)))
        aboutItems.add(AboutAdabters.ModelAbout(14, Pair("<font color='#000000'>${getString(R.string.about_no_ads)}</font>", gravity)))
        aboutItems.add(AboutAdabters.ModelAbout(14, Pair("\n", gravity)))
        aboutItems.add(AboutAdabters.ModelAbout(14, Pair("<font color='#000000'>${getString(R.string.about_cost_explanation)}</font>", gravity)))

        val screenWidth = ScreenUtils.getScreenWidth(this)
        val recyclerView = findViewById<RecyclerView>(R.id.rv)
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = NonScrollableLinearLayoutManager(this)
        recyclerView.adapter = AboutAdabters(
            this,
            AppUtils.getVersionName(this),
            aboutItems,
            screenWidth,
            (screenWidth * 0.4f).toInt()
        )

        // Price buttons
        viewPrice10 = findViewById(R.id.view_10)
        viewPrice50 = findViewById(R.id.view_50)
        viewPrice100 = findViewById(R.id.view_100)
        viewPrice1000 = findViewById(R.id.view_1000)
        btnLaunch = findViewById(R.id.btn_launch)

        viewPrice10.setOnClickListener {
            productIdCurrent = PRODUCT_ID_10
            updatePrice(viewPrice10.text.toString(), R.id.view_10, priceSelect)
        }

        viewPrice50.setOnClickListener {
            productIdCurrent = PRODUCT_ID_50
            updatePrice(viewPrice50.text.toString(), R.id.view_50, priceSelect)
        }

        viewPrice100.setOnClickListener {
            productIdCurrent = PRODUCT_ID_100
            updatePrice(viewPrice100.text.toString(), R.id.view_100, priceSelect)
        }

        viewPrice1000.setOnClickListener {
            productIdCurrent = PRODUCT_ID_1000
            updatePrice(viewPrice1000.text.toString(), R.id.view_1000, priceSelect)
        }

        btnLaunch.setOnClickListener {
            launchPurchaseFlow(productIdCurrent)
        }
    }

    // ── Price selection UI ─────────────────────────────────────────────

    private fun updatePrice(priceText: String, newId: Int, oldId: Int) {
        if (newId == oldId) return
        btnLaunch.text = String.format(getString(R.string.btn_launch_billing), priceText)
        findViewById<View>(newId).setBackgroundResource(R.drawable.item_billing_select)
        findViewById<View>(oldId).setBackgroundResource(R.drawable.item_billing)
        priceSelect = newId
    }

    // ── Billing connection ─────────────────────────────────────────────

    private fun startBillingConnection() {
        billingClient?.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    queryProducts()
                    queryPurchases()
                }
            }

            override fun onBillingServiceDisconnected() {
                startBillingConnection()
            }
        })
    }

    private fun queryProducts() {
        val productList = listOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(PRODUCT_ID_10)
                .setProductType(BillingClient.ProductType.INAPP)
                .build(),
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(PRODUCT_ID_50)
                .setProductType(BillingClient.ProductType.INAPP)
                .build(),
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(PRODUCT_ID_100)
                .setProductType(BillingClient.ProductType.INAPP)
                .build(),
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(PRODUCT_ID_1000)
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        )

        billingClient?.queryProductDetailsAsync(
            QueryProductDetailsParams.newBuilder().setProductList(productList).build(),
            object : ProductDetailsResponseListener {
                override fun onProductDetailsResponse(
                    billingResult: BillingResult,
                    productDetailsList: MutableList<ProductDetails>
                ) {
                    if (billingResult.responseCode != BillingClient.BillingResponseCode.OK) return
                    if (productDetailsList == null) return

                    for (productDetails in productDetailsList) {
                        productDetailsMap[productDetails.productId] = productDetails
                        runOnUiThread { updateUI(productDetails) }
                    }
                }
            }
        )
    }

    private fun queryPurchases() {
        billingClient?.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.INAPP)
                .build(),
            object : PurchasesResponseListener {
                override fun onQueryPurchasesResponse(
                    billingResult: BillingResult,
                    purchases: MutableList<Purchase>
                ) {
                    if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                        for (purchase in purchases) {
                            handlePurchase(purchase)
                        }
                    }
                }
            }
        )
    }

    // ── Billing UI updates ─────────────────────────────────────────────

    private fun updateUI(productDetails: ProductDetails) {
        val productId = productDetails.productId
        val formatPrice = PriceFormatter.formatPrice(
            productDetails.oneTimePurchaseOfferDetails?.formattedPrice ?: ""
        )

        when (productId) {
            PRODUCT_ID_10 -> viewPrice10.text = formatPrice
            PRODUCT_ID_50 -> {
                viewPrice50.text = formatPrice
                btnLaunch.text = String.format(
                    getString(R.string.btn_launch_billing),
                    viewPrice50.text.toString()
                )
            }
            PRODUCT_ID_100 -> viewPrice100.text = formatPrice
            PRODUCT_ID_1000 -> viewPrice1000.text = formatPrice
        }
    }

    // ── Purchase flow ──────────────────────────────────────────────────

    private fun launchPurchaseFlow(productId: String) {
        val productDetails = productDetailsMap[productId] ?: return
        val productDetailsParamsList = listOf(
            BillingFlowParams.ProductDetailsParams.newBuilder()
                .setProductDetails(productDetails)
                .build()
        )
        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(productDetailsParamsList)
            .build()
        billingClient?.launchBillingFlow(this, billingFlowParams)
    }

    // ── PurchasesUpdatedListener ───────────────────────────────────────

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: MutableList<Purchase>?) {
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            for (purchase in purchases) {
                handlePurchase(purchase)
            }
        }
        // Other response codes are silently consumed (matching original behavior)
    }

    private fun handlePurchase(purchase: Purchase) {
        when (purchase.purchaseState) {
            Purchase.PurchaseState.PURCHASED -> {
                thnks()
                if (!purchase.isAcknowledged) {
                    billingClient?.consumeAsync(
                        ConsumeParams.newBuilder()
                            .setPurchaseToken(purchase.purchaseToken)
                            .build()
                    ) { billingResult, _ ->
                        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                            Toast.makeText(this, "Purchase consumed successfully", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this, "Error consuming purchase", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
            Purchase.PurchaseState.PENDING -> {
                Toast.makeText(this, "Purchase is pending", Toast.LENGTH_SHORT).show()
            }
            else -> {
                Toast.makeText(this, "Purchase is in unknown state", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // ── Post-purchase ──────────────────────────────────────────────────

    private fun thnks() {
        Intent(this, ThanksYouActivity::class.java).apply {
            val priceText = findViewById<ButtonCustumFont>(priceSelect).text.toString()
            putExtra("price", priceText)
            startActivity(this)
        }
    }
}
