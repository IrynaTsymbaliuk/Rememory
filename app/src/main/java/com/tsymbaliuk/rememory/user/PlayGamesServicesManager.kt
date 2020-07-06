package com.tsymbaliuk.rememory.user

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.games.Games
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PlayGamesAuthProvider
import com.tsymbaliuk.rememory.R

class PlayGamesServicesManager(val context: Context) {

    private var signedInAccount: GoogleSignInAccount? = null

    private var _userDisplayedName = MutableLiveData<String?>()
    val userDisplayedName: LiveData<String?>
        get() = _userDisplayedName

    private var _userImageUri = MutableLiveData<Uri?>()
    val userImageUri: LiveData<Uri?>
        get() = _userImageUri

    private val RC_SIGN_IN = 9001
    private val RC_ACHIEVEMENT_UI = 9003
    private val RC_LEADERBOARD_UI = 9004
    private val TAG = "PlayGameServiceManager"

    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN)
        .requestServerAuthCode(context.getString(R.string.web_client_id))
        .build()

    fun signInPlayGamesWithFirebase(activity: Activity) {
        signedInAccount = GoogleSignIn.getLastSignedInAccount(context)
        if (signedInAccount != null) signInFirebase(activity, signedInAccount!!)
        updateUI(activity)
        if (!GoogleSignIn.hasPermissions(signedInAccount, *gso.scopeArray)) {
            val signInClient = GoogleSignIn.getClient(context, gso)
           signInClient.silentSignIn()
                .addOnCompleteListener(activity) { task ->
                    if (task.isSuccessful) {
                        signedInAccount = task.result
                        if (signedInAccount != null) signInFirebase(activity, signedInAccount!!)
                        updateUI(activity)
                    } else {
                        val intent = signInClient.signInIntent
                        activity.startActivityForResult(intent, RC_SIGN_IN)
                    }
                }
        }
    }

    fun onActivityResult(
        activity: Activity,
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        if (requestCode == RC_SIGN_IN) {
            val result = Auth.GoogleSignInApi.getSignInResultFromIntent(data)
            if (result != null && result.isSuccess) {
                signedInAccount = result.signInAccount
                if (signedInAccount != null) signInFirebase(activity, signedInAccount!!)
                updateUI(activity)
            } else {
                Toast.makeText(activity, "Sign in error", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun signInFirebase(activity: Activity, acct: GoogleSignInAccount) {
        val auth = FirebaseAuth.getInstance()
        if (acct.serverAuthCode != null) {
            val credential = PlayGamesAuthProvider.getCredential(acct.serverAuthCode!!)
            auth.signInWithCredential(credential)
                .addOnCompleteListener(activity) { task ->
                    if (task.isSuccessful) {
                        Log.e(TAG, "signInWithCredential:success")
                        val user = auth.currentUser
                    } else {
                        Log.e(TAG, "signInWithCredential:failure", task.exception)
                        Toast.makeText(activity, "Authentication failed.", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }


    private fun getPlayerName(activity: Activity) {
        if (signedInAccount != null) {
            val playersClient = Games.getPlayersClient(activity, signedInAccount!!)
            playersClient.currentPlayer.addOnSuccessListener { player ->
                _userDisplayedName.value = player.displayName
            }
        }
    }

    private fun getPlayerImage(activity: Activity) {
        if (signedInAccount != null) {
            val playersClient = Games.getPlayersClient(activity, signedInAccount!!)
            playersClient.currentPlayer.addOnSuccessListener { player ->
                _userImageUri.value = player.iconImageUri
            }
        }
    }

    fun showLeaderboard(activity: Activity) {
        if (signedInAccount != null) {
            Games.getLeaderboardsClient(context, signedInAccount!!)
                .getLeaderboardIntent(context.getString(R.string.leaderboard_id))
                .addOnSuccessListener { intent ->
                    activity.startActivityForResult(
                        intent,
                        RC_LEADERBOARD_UI
                    )
                }
        } else signInPlayGamesWithFirebase(activity)
    }

    fun submitScore(score: Long) {
        if (signedInAccount != null) {
            Games.getLeaderboardsClient(context, signedInAccount!!)
                .submitScore(context.getString(R.string.leaderboard_id), score)
        }
    }

    fun showAchievements(activity: Activity) {
        if (signedInAccount != null) {
            Games.getAchievementsClient(context, signedInAccount!!)
                .achievementsIntent
                .addOnSuccessListener { intent ->
                    activity.startActivityForResult(intent, RC_ACHIEVEMENT_UI)
                }
        }
    }

    fun incrementAchieve(achieveId: Int, score: Int) {
        if (signedInAccount != null) {
            Games.getAchievementsClient(context, signedInAccount!!)
                .increment(context.getString(achieveId), score)
        }
    }

    fun unlockAchieve(achieveId: Int) {
        if (signedInAccount != null) {
            Games.getAchievementsClient(context, signedInAccount!!)
                .unlock(context.getString(achieveId))
        }
    }

    fun updateUI(activity: Activity) {
        getPlayerImage(activity)
        getPlayerImage(activity)
    }

}