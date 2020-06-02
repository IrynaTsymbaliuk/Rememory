package com.tsymbaliuk.rememory.model

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.FirebaseFirestore
import com.tsymbaliuk.rememory.App
import com.tsymbaliuk.rememory.model.database.AppDatabase
import com.tsymbaliuk.rememory.model.database.AppRepository
import kotlinx.coroutines.MainScope

class FirebaseManager(val context: Context) {

    private val TAG = "FirebaseManager"

    fun getAllLevels(): MutableLiveData<ArrayList<Level>> {
        val allLevelsWithRounds =
            MutableLiveData<ArrayList<Level>>().apply { value = arrayListOf() }
        FirebaseFirestore.getInstance().collection("levels")
            .orderBy("indexNumber")
            .get()
            .addOnSuccessListener { levels ->
                for (levelItem in levels) {
                    val level = levelItem.toObject(Level::class.java)

                    FirebaseFirestore.getInstance().collection("levels/${levelItem.id}/rounds")
                        .orderBy("indexNumber")
                        .get()
                        .addOnSuccessListener { rounds ->
                            for (roundItem in rounds) {
                                val round = roundItem.toObject(Round::class.java)
                                if (level.rounds == null) level.rounds = arrayListOf()
                                level.rounds?.add(round)
                            }
                        }

                    allLevelsWithRounds.value?.add(level)
                    allLevelsWithRounds.value = allLevelsWithRounds.value
                }
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents.", exception)
            }
        return allLevelsWithRounds
    }

}