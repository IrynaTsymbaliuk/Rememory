package com.tsymbaliuk.rememory

import android.content.Context
import com.bumptech.glide.Glide
import com.bumptech.glide.Registry
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.module.AppGlideModule
import com.firebase.ui.storage.images.FirebaseImageLoader
import com.google.firebase.storage.StorageReference
import com.tsymbaliuk.domain.gameResult.usecase.GameResultUseCase
import com.tsymbaliuk.domain.gameResult.usecase.GameResultUseCaseImpl
import com.tsymbaliuk.domain.level.usecase.LevelUseCase
import com.tsymbaliuk.data.gameResult.AppDatabase
import com.tsymbaliuk.data.gameResult.GameResultRepositoryImpl
import com.tsymbaliuk.data.level.LevelRepositoryImpl
import com.tsymbaliuk.domain.gameResult.repository.GameResultRepository
import com.tsymbaliuk.domain.level.repository.LevelRepository
import com.tsymbaliuk.domain.level.usecase.LevelUseCaseImpl
import com.tsymbaliuk.rememory.game.GameViewModel
import com.tsymbaliuk.rememory.game.GameViewModelImpl
import com.tsymbaliuk.rememory.user.PlayGamesServicesManager
import kotlinx.coroutines.MainScope
import org.koin.android.ext.koin.androidApplication
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import java.io.InputStream

val appModule = module {
    viewModel {
        GameViewModelImpl(get(), get(), get())
    }
    /*viewModel {
        BillingViewModel(get())
    }*/
    single { PlayGamesServicesManager(get()) }

}

val coreModule = module {

    factory {
        GameResultUseCaseImpl(
            get()
        ) as GameResultUseCase
    }

    factory {
        LevelUseCaseImpl(
            get()
        ) as LevelUseCase
    }

}

val dataModule = module {

    single {
            GameResultRepositoryImpl(AppDatabase.getDatabase(
                androidApplication(),
                MainScope()
            ).appDao()
        ) as GameResultRepository
    }
    single { LevelRepositoryImpl() as LevelRepository }

}

@GlideModule
class AppGlideModule : AppGlideModule() {
    override fun registerComponents(context: Context, glide: Glide, registry: Registry) {
        registry.append(
            StorageReference::class.java, InputStream::class.java,
            FirebaseImageLoader.Factory()
        )
    }
}
