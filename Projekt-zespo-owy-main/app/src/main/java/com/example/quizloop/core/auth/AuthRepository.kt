package com.example.quizloop.core.auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.AuthResult
import kotlinx.coroutines.tasks.await

class AuthRepository(private val firebaseAuth: FirebaseAuth) {

    suspend fun registerUser(email: String, pass: String): AuthResult {
        return firebaseAuth.createUserWithEmailAndPassword(email, pass).await()
    }

    suspend fun loginUser(email: String, pass: String): AuthResult {
        return firebaseAuth.signInWithEmailAndPassword(email, pass).await()
    }

    suspend fun sendVerificationEmail() {
        firebaseAuth.currentUser?.sendEmailVerification()?.await()
    }

    suspend fun sendPasswordResetEmail(email: String) {
        firebaseAuth.sendPasswordResetEmail(email).await()
    }

    fun getCurrentUser() = firebaseAuth.currentUser

    fun logout() {
        firebaseAuth.signOut()
    }
}
