package com.tsymbaliuk.data.level

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.tsymbaliuk.domain.level.model.LevelModel
import com.tsymbaliuk.domain.level.repository.LevelRepository
import com.tsymbaliuk.data.LevelContract
import com.tsymbaliuk.data.level.model.Level
import com.tsymbaliuk.data.level.model.Round
import com.tsymbaliuk.data.toLevelModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class LevelRepositoryImpl : LevelRepository {

    val firestore = FirebaseFirestore.getInstance()
    private val TAG = "FirebaseManager"

    override fun getAll(): Flow<ArrayList<LevelModel>> {
        val allLevelsWithRounds = arrayListOf<LevelModel>()
        firestore.collection(LevelContract.levelCollection)
            .orderBy(LevelContract.levelOrderParam, Query.Direction.ASCENDING)
            .get()
            .addOnSuccessListener { levels ->
                for (levelItem in levels) {
                    val level = levelItem.toObject(Level::class.java)
                    Log.e(TAG, "${level.indexNumber}")
                    firestore
                        .collection("levels/${levelItem.id}/rounds")
                        .orderBy(LevelContract.roundOrderParam, Query.Direction.ASCENDING)
                        .get()
                        .addOnSuccessListener { rounds ->
                            for (roundItem in rounds) {
                                val round = roundItem.toObject(Round::class.java)
                                Log.e(TAG, "${round.indexNumber}")
                                if (level.rounds == null) level.rounds = arrayListOf()
                                level.rounds?.add(round)
                            }
                            allLevelsWithRounds.add(level.toLevelModel())
                        }
                    Log.e(TAG, "${allLevelsWithRounds.size}")
                }
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error getting documents.", exception)
            }

        return flowOf(allLevelsWithRounds)

    }

}