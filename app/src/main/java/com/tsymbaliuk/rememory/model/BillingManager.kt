package com.tsymbaliuk.rememory.model

import android.app.Activity
import android.app.Application
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.OnLifecycleEvent
import com.android.billingclient.api.*
import com.tsymbaliuk.rememory.R
import com.tsymbaliuk.rememory.view.utils.SingleLiveEvent

class BillingManager private constructor(
    private val app: Application
) : LifecycleObserver, PurchasesUpdatedListener, BillingClientStateListener,
    SkuDetailsResponseListener {

    private lateinit var billingClient: BillingClient
    val skusWithSkuDetails = MutableLiveData<Map<String, SkuDetails>>()
    val purchases = MutableLiveData<List<Purchase>>()
    val purchaseUpdateEvent = SingleLiveEvent<List<Purchase>>()

    companion object {
        private const val TAG = "Billing"

        @Volatile
        private var INSTANCE: BillingManager? = null

        fun getInstance(app: Application): BillingManager =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: BillingManager(app).also { INSTANCE = it }
            }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun create() {
        Log.d(TAG, "ON_CREATE")
        billingClient = BillingClient.newBuilder(app.applicationContext)
            .setListener(this)
            .enablePendingPurchases()
            .build()
        if (!billingClient.isReady) {
            Log.d(TAG, "BillingClient: Start connection...")
            billingClient.startConnection(this)
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun destroy() {
        Log.d(TAG, "ON_DESTROY")
        if (billingClient.isReady) {
            Log.d(TAG, "BillingClient can only be used once -- closing connection")
            billingClient.endConnection()
        }
    }

    override fun onPurchasesUpdated(
        billingResult: BillingResult?,
        purchases: MutableList<Purchase>?
    ) {
        if (billingResult == null) {
            Log.wtf(TAG, "onPurchasesUpdated: null BillingResult")
            return
        }
        val responseCode = billingResult.responseCode
        val debugMessage = billingResult.debugMessage
        Log.d(TAG, "onPurchasesUpdated: $responseCode $debugMessage")
        when (responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                if (purchases == null) {
                    Log.d(TAG, "onPurchasesUpdated: null purchase list")
                    processPurchases(null)
                } else {
                    processPurchases(purchases)
                }
            }
            BillingClient.BillingResponseCode.USER_CANCELED -> {
                Log.i(TAG, "onPurchasesUpdated: User canceled the purchase")
            }
            BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> {
                Log.i(TAG, "onPurchasesUpdated: The user already owns this item")
            }
            BillingClient.BillingResponseCode.DEVELOPER_ERROR -> {
                Log.e(
                    TAG, "onPurchasesUpdated: Developer error means that Google Play " +
                            "does not recognize the configuration. If you are just getting started, " +
                            "make sure you have configured the application correctly in the " +
                            "Google Play Console. The SKU product ID must match and the APK you " +
                            "are using must be signed with release keys."
                )
            }
        }
    }

    override fun onBillingServiceDisconnected() {
        Log.d(TAG, "onBillingServiceDisconnected")
        // Try connecting again with exponential backoff.
    }

    override fun onBillingSetupFinished(billingResult: BillingResult) {
        val responseCode = billingResult.responseCode
        val debugMessage = billingResult.debugMessage
        Log.d(TAG, "onBillingSetupFinished: $responseCode $debugMessage")
        if (responseCode == BillingClient.BillingResponseCode.OK) {
            querySkuDetails()
            queryPurchases()
        }
    }

    fun querySkuDetails() {
        Log.d(TAG, "querySkuDetails")
        val params = SkuDetailsParams.newBuilder()
            .setType(BillingClient.SkuType.SUBS)
            .setSkusList(listOf(app.applicationContext.getString(R.string.subscribe_id)))
            .build()
        params.let { skuDetailsParams ->
            Log.i(TAG, "querySkuDetailsAsync")
            billingClient.querySkuDetailsAsync(skuDetailsParams, this)
        }
    }

    override fun onSkuDetailsResponse(
        billingResult: BillingResult?,
        skuDetailsList: MutableList<SkuDetails>?
    ) {
        if (billingResult == null) {
            Log.wtf(TAG, "onSkuDetailsResponse: null BillingResult")
            return
        }
        val responseCode = billingResult.responseCode
        val debugMessage = billingResult.debugMessage
        when (responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                Log.i(TAG, "onSkuDetailsResponse: $responseCode $debugMessage")
                if (skuDetailsList == null) {
                    Log.w(TAG, "onSkuDetailsResponse: null SkuDetails list")
                    skusWithSkuDetails.postValue(emptyMap())
                } else
                    skusWithSkuDetails.postValue(HashMap<String, SkuDetails>().apply {
                        for (details in skuDetailsList) {
                            put(details.sku, details)
                        }
                    }.also { postedValue ->
                        Log.i(TAG, "onSkuDetailsResponse: count ${postedValue.size}")
                    })
            }
            BillingClient.BillingResponseCode.SERVICE_DISCONNECTED,
            BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE,
            BillingClient.BillingResponseCode.BILLING_UNAVAILABLE,
            BillingClient.BillingResponseCode.ITEM_UNAVAILABLE,
            BillingClient.BillingResponseCode.DEVELOPER_ERROR,
            BillingClient.BillingResponseCode.ERROR -> {
                Log.e(TAG, "onSkuDetailsResponse: $responseCode $debugMessage")
            }
            BillingClient.BillingResponseCode.USER_CANCELED,
            BillingClient.BillingResponseCode.FEATURE_NOT_SUPPORTED,
            BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED,
            BillingClient.BillingResponseCode.ITEM_NOT_OWNED -> {
                Log.wtf(TAG, "onSkuDetailsResponse: $responseCode $debugMessage")
            }
        }
    }

    fun queryPurchases() {
        if (!billingClient.isReady) {
            Log.e(TAG, "queryPurchases: BillingClient is not ready")
        }
        Log.d(TAG, "queryPurchases: SUBS")
        val result = billingClient.queryPurchases(BillingClient.SkuType.SUBS)
        if (result == null) {
            Log.i(TAG, "queryPurchases: null purchase result")
            processPurchases(null)
        } else {
            if (result.purchasesList == null) {
                Log.i(TAG, "queryPurchases: null purchase list")
                processPurchases(null)
            } else {
                processPurchases(result.purchasesList)
            }
        }
    }

    private fun processPurchases(purchasesList: List<Purchase>?) {
        Log.d(TAG, "processPurchases: ${purchasesList?.size} purchase(s)")
        purchases.postValue(purchasesList)
        purchasesList?.let {
            logAcknowledgementStatus(purchasesList)
        }
    }

    private fun logAcknowledgementStatus(purchasesList: List<Purchase>) {
        var ack_yes = 0
        var ack_no = 0
        for (purchase in purchasesList) {
            if (purchase.isAcknowledged) {
                ack_yes++
            } else {
                ack_no++
            }
        }
        Log.d(TAG, "logAcknowledgementStatus: acknowledged=$ack_yes unacknowledged=$ack_no")
    }

    fun launchBillingFlow(activity: Activity, params: BillingFlowParams): Int {
        val sku = params.sku
        val oldSku = params.oldSku
        Log.i(TAG, "launchBillingFlow: sku: $sku, oldSku: $oldSku")
        if (!billingClient.isReady) {
            Log.e(TAG, "launchBillingFlow: BillingClient is not ready")
        }
        val billingResult = billingClient.launchBillingFlow(activity, params)
        val responseCode = billingResult.responseCode
        val debugMessage = billingResult.debugMessage
        Log.d(TAG, "launchBillingFlow: BillingResponse $responseCode $debugMessage")
        return responseCode
    }

}