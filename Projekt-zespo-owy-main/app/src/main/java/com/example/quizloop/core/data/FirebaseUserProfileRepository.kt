package com.example.quizloop.core.data

import com.example.quizloop.core.model.UserProfile
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

interface UserProfileRepository {
    fun getUserProfile(userId: String): Flow<UserProfile?>
    suspend fun createUserProfile(userProfile: UserProfile)
    suspend fun addScore(userId: String, pointsToAdd: Int)
    suspend fun updateUserProfile(userId: String, newName: String, newColorHex: String)
}

class FirebaseUserProfileRepository : UserProfileRepository {
    private val db = FirebaseDatabase.getInstance().reference.child("userProfiles")

    override fun getUserProfile(userId: String): Flow<UserProfile?> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val profile = snapshot.getValue<UserProfile>()
                trySend(profile)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        db.child(userId).addValueEventListener(listener)
        awaitClose { db.child(userId).removeEventListener(listener) }
    }

    override suspend fun createUserProfile(userProfile: UserProfile) {
        db.child(userProfile.userId).setValue(userProfile)
    }

    override suspend fun addScore(userId: String, pointsToAdd: Int) {
        val profile = getUserProfileSuspend(userId)
        if (profile != null) {
            val newScore = profile.score + pointsToAdd
            val newLevel = calculateLevel(newScore, profile.level)
            db.child(userId).child("score").setValue(newScore)
            db.child(userId).child("level").setValue(newLevel)
        } else {
            val newUser = UserProfile(userId = userId, name = "New User", score = pointsToAdd, level = 1)
            createUserProfile(newUser)
        }
    }

    override suspend fun updateUserProfile(userId: String, newName: String, newColorHex: String) {
        val updates = mapOf(
            "name" to newName,
            "backgroundColorHex" to newColorHex
        )
        db.child(userId).updateChildren(updates)
    }

    private fun calculateLevel(score: Int, currentLevel: Int): Int {
        var level = currentLevel
        var required = requiredPointsForLevel(level)
        while (score >= required) {
            level++
            required = requiredPointsForLevel(level)
        }
        return level
    }

    private fun requiredPointsForLevel(level: Int): Int {
        return 1000 * level * (level + 1) / 2 // Example: 1000, 3000, 6000, 10000, ...
    }

    private suspend fun getUserProfileSuspend(userId: String): UserProfile? = suspendCoroutine { continuation ->
        db.child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                continuation.resume(snapshot.getValue<UserProfile>())
            }

            override fun onCancelled(error: DatabaseError) {
                continuation.resume(null)
            }
        })
    }
}
