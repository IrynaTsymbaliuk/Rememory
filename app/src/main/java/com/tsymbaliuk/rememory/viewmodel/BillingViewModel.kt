package com.tsymbaliuk.rememory.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.Purchase
import com.tsymbaliuk.rememory.App
import com.tsymbaliuk.rememory.R
import com.tsymbaliuk.rememory.view.utils.SingleLiveEvent

class BillingViewModel(val app: Application) : ViewModel() {

    private val purchases = (app as App).billingClientLifecycle.purchases
    private val skusWithSkuDetails =
        (app as App).billingClientLifecycle.skusWithSkuDetails
    val buyEvent = SingleLiveEvent<BillingFlowParams>()

    fun buyAdFreeSubscription() {
        val hasPremium = deviceHasGooglePlaySubscription(
            purchases.value,
            app.getString(R.string.subscribe_id)
        )
        Log.d("Billing", "hasPremium: $hasPremium")
        if (!hasPremium) {
            buy(sku = app.getString(R.string.subscribe_id))
        }
    }

    private fun deviceHasGooglePlaySubscription(purchases: List<Purchase>?, sku: String) =
        purchaseForSku(purchases, sku) != null

    private fun purchaseForSku(purchases: List<Purchase>?, sku: String): Purchase? {
        purchases?.let {
            for (purchase in it) {
                if (purchase.sku == sku) {
                    return purchase
                }
            }
        }
        return null
    }

    private fun buy(sku: String) {
       /* val isSkuOnServer = serverHasSubscription(subscriptions.value, sku)
        val isSkuOnDevice = deviceHasGooglePlaySubscription(purchases.value, sku)
        Log.d("Billing", "$sku - isSkuOnServer: $isSkuOnServer")
        Log.d("Billing", "isSkuOnDevice: $isSkuOnDevice")
       when {
            isSkuOnDevice && isSkuOnServer -> {
                Log.e("Billing", "You cannot buy a SKU that is already owned: $sku. " +
                        "This is an error in the application trying to use Google Play Billing.")
                return
            }
            isSkuOnDevice && !isSkuOnServer -> {
                Log.e("Billing", "The Google Play Billing Library APIs indicate that" +
                        "this SKU is already owned, but the purchase token is not registered " +
                        "with the server. There might be an issue registering the purchase token.")
                return
            }
            !isSkuOnDevice && isSkuOnServer -> {
                Log.w("Billing", "WHOA! The server says that the user already owns " +
                        "this item: $sku. This could be from another Google account. " +
                        "You should warn the user that they are trying to buy something " +
                        "from Google Play that they might already have access to from " +
                        "another purchase, possibly from a different Google account " +
                        "on another device.\n" +
                        "You can choose to block this purchase.\n" +
                        "If you are able to cancel the existing subscription on the server, " +
                        "you should allow the user to subscribe with Google Play, and then " +
                        "cancel the subscription after this new subscription is complete. " +
                        "This will allow the user to seamlessly transition their payment " +
                        "method from an existing payment method to this Google Play account.")
                return
            }
        }*/

        val skuDetails = skusWithSkuDetails.value?.get(sku) ?: run {
            Log.e("Billing", "Could not find SkuDetails to make purchase.")
            return
        }
        val billingBuilder = BillingFlowParams.newBuilder().setSkuDetails(skuDetails)
        val billingParams = billingBuilder.build()
        buyEvent.postValue(billingParams)
        buyEvent.value = buyEvent.value
    }

   /* fun serverHasSubscription(subscriptions: List<SubscriptionStatus>?, sku: String) =
        subscriptionForSku(subscriptions, sku) != null
*/

}