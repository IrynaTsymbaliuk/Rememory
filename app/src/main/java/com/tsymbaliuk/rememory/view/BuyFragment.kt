package com.tsymbaliuk.rememory.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.tsymbaliuk.rememory.R
import com.tsymbaliuk.rememory.viewmodel.BillingViewModel
import kotlinx.android.synthetic.main.inapp_purchases_fragment.*
import org.koin.android.ext.android.inject

class BuyFragment: Fragment() {

    private val billingViewModel: BillingViewModel by inject()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.inapp_purchases_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        buy_add_free.setOnClickListener {
           billingViewModel.buyAdFreeSubscription()
        }

    }

}