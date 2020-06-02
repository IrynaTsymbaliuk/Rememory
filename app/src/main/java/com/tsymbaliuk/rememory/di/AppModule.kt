package com.tsymbaliuk.rememory.di

import com.tsymbaliuk.rememory.model.FirebaseManager
import com.tsymbaliuk.rememory.model.PlayGamesServicesManager
import com.tsymbaliuk.rememory.model.database.AppDao
import com.tsymbaliuk.rememory.model.database.AppDatabase
import com.tsymbaliuk.rememory.model.database.AppRepository
import com.tsymbaliuk.rememory.viewmodel.BillingViewModel
import com.tsymbaliuk.rememory.viewmodel.GameViewModel
import kotlinx.coroutines.MainScope
import org.koin.android.ext.koin.androidApplication
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModels = module {
    viewModel {
        GameViewModel(get(), get())
    }
    viewModel {
        BillingViewModel(get())
    }
}

val managers = module {
    single { PlayGamesServicesManager(get()) }
    single { FirebaseManager(get()) }
    single { AppRepository( AppDatabase.getDatabase(androidApplication(), MainScope()).appDao(), get()) }
}