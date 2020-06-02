package com.tsymbaliuk.rememory

import com.tsymbaliuk.rememory.di.managers
import com.tsymbaliuk.rememory.di.viewModels
import com.tsymbaliuk.rememory.model.BillingManager
import com.tsymbaliuk.rememory.model.database.AppDatabase
import kotlinx.coroutines.MainScope
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidFileProperties
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class App : android.app.Application() {

    val billingClientLifecycle: BillingManager
        get() = BillingManager.getInstance(this)

    companion object {

        private var self: App? = null

        fun getInstance(): App {
            return self!!
        }
    }

    override fun onCreate() {
        super.onCreate()

        self = this

        startKoin {
            androidLogger()
            androidContext(this@App)
            androidFileProperties()
            modules(viewModels, managers)
        }

    }
}
