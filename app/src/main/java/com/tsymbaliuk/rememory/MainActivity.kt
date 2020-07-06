package com.tsymbaliuk.rememory

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.asLiveData
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.onNavDestinationSelected
import androidx.navigation.ui.setupWithNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.gms.ads.*
import com.google.android.gms.common.images.ImageManager
import com.tsymbaliuk.domain.level.usecase.LevelUseCase
import com.tsymbaliuk.rememory.user.PlayGamesServicesManager
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.drawer_header.view.*
import org.koin.android.ext.android.inject

class MainActivity : AppCompatActivity() {

    private val playGamesServicesManager: PlayGamesServicesManager by inject()
    private val levelUseCase: LevelUseCase by inject()
    /*private val billingViewModel: BillingViewModel by inject()*/
    /*private lateinit var billingManager: BillingManager*/

    private lateinit var navController: NavController

    private lateinit var mAdView: AdView

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setUpSignIn()
        /* setUpBilling()*/
        setUpAdMob()
        setUpDrawerNavigation()
        setUpSplash()

    }

    private fun setUpSignIn() {
        playGamesServicesManager.signInPlayGamesWithFirebase(this)

        playGamesServicesManager.userDisplayedName.observe(this, Observer { userDisplayedName ->
            nav_view.getHeaderView(0).header_profile_name.text = userDisplayedName
        })

        playGamesServicesManager.userImageUri.observe(this, Observer { uri ->
            ImageManager.create(this).loadImage({ u, d, r ->
                Glide.with(this)
                    .load(d)
                    .apply(RequestOptions().circleCrop())
                    .into(nav_view.getHeaderView(0).header_profile_image)
            }, uri)
        })
    }

    private fun setUpSplash() {
        levelUseCase.getAll().asLiveData().observe(this, Observer {
            splash.visibility = View.GONE
        })
    }

    private fun setUpAdMob() {
        MobileAds.initialize(this) { }

        mAdView = ad_view
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)

        mAdView.adListener = object : AdListener() {
            override fun onAdLoaded() {
                Log.e("AdMob", "onAdLoaded")
            }

            override fun onAdFailedToLoad(errorCode: Int) {
                Log.e("AdMob", "onAdFailedToLoad $errorCode")
            }

            override fun onAdOpened() {
                Log.e("AdMob", "onAdOpened")
            }

            override fun onAdClicked() {
                Log.e("AdMob", "onAdClicked")
            }

            override fun onAdLeftApplication() {
                Log.e("AdMob", "onAdLeftApplication")
            }

            override fun onAdClosed() {
                Log.e("AdMob", "onAdClosed")
            }
        }
    }

    /*private fun setUpBilling() {
        billingManager = (application as App).billingClientLifecycle
        lifecycle.addObserver(billingManager)

        billingManager.purchaseUpdateEvent.observe(this, Observer {
            it?.let {
                //registerPurchases(it)
            }
        })

        billingViewModel.buyEvent.observe(this, Observer {
            it?.let {
                billingManager.launchBillingFlow(this, it)
            }
        })
    }*/

    private fun setUpDrawerNavigation() {
        navController = findNavController(R.id.nav_host_fragment)

        /* val appBarConfiguration = AppBarConfiguration(
             setOf(
                 R.id.setting_fragment,
                 R.id.inapp_purchases_fragment,
                 R.id.achieves_fragment,
                 R.id.leaderbord_fragment,
                 R.id.main_fragment,
                 R.id.privacy_fragment
             ), drawer_layout
         )*/

        setSupportActionBar(toolbar)
        // setupActionBarWithNavController(navController, appBarConfiguration)
        nav_view.setupWithNavController(navController)

        nav_view.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.buy_fragment -> {
                    navController.navigate(R.id.buy_fragment)
                    drawer_layout.closeDrawer(Gravity.LEFT)
                    true
                }
                R.id.leaderbord_fragment -> {
                    playGamesServicesManager.showLeaderboard(this)
                    drawer_layout.closeDrawer(Gravity.LEFT)
                    true
                }
                R.id.achieves_fragment -> {
                    playGamesServicesManager.showAchievements(this)
                    drawer_layout.closeDrawer(Gravity.LEFT)
                    true
                }
                R.id.share_app -> {
                    shareApp()
                    drawer_layout.closeDrawer(Gravity.LEFT)
                    true
                }
                R.id.rate_app -> {
                    rateAppIntent()
                    drawer_layout.closeDrawer(Gravity.LEFT)
                    true
                }
                R.id.send_feedback -> {
                    sendFeedback()
                    drawer_layout.closeDrawer(Gravity.LEFT)
                    true
                }
                R.id.privacy_fragment -> {
                    navController.navigate(R.id.privacy_fragment)
                    drawer_layout.closeDrawer(Gravity.LEFT)
                    true
                }
                else -> false
            }
        }

        navController.addOnDestinationChangedListener { _, destination, _ ->
            if (destination.id != R.id.main_fragment) {
                toolbar.visibility = View.GONE
            } else toolbar.visibility = View.VISIBLE
        }
    }

    private fun rateAppIntent() {
        val uri: Uri = Uri.parse("market://details?id=$packageName")
        val goToMarket = Intent(Intent.ACTION_VIEW, uri)
        goToMarket.addFlags(
            Intent.FLAG_ACTIVITY_NO_HISTORY or
                    Intent.FLAG_ACTIVITY_NEW_DOCUMENT or
                    Intent.FLAG_ACTIVITY_MULTIPLE_TASK
        )
        try {
            startActivity(goToMarket)
        } catch (e: ActivityNotFoundException) {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("http://play.google.com/store/apps/details?id=$packageName")
                )
            )
        }
    }

    private fun sendFeedback() {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:")
            putExtra(Intent.EXTRA_EMAIL, arrayOf("Iryna.Tsymbaliuk.dev@gmail.com"))
            putExtra(Intent.EXTRA_SUBJECT, "Rememory: feedback")
        }
        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        }
    }

    private fun shareApp() {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(
                Intent.EXTRA_TEXT,
                "http://play.google.com/store/apps/details?id=$packageName"
            )
        }
        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val retValue = super.onCreateOptionsMenu(menu)
        if (nav_view == null) {
            menuInflater.inflate(R.menu.drawer_navigation, menu)
            return true
        }
        return retValue
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return item.onNavDestinationSelected(navController)
                || super.onOptionsItemSelected(item)
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp()
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)
        playGamesServicesManager.onActivityResult(this, requestCode, resultCode, data)
    }

    override fun onResume() {
        super.onResume()
        playGamesServicesManager.signInPlayGamesWithFirebase(this)
    }

}
