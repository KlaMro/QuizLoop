package com.example.quizloop.core.data

import com.example.quizloop.core.auth.AuthRepository
import com.google.firebase.auth.FirebaseAuth

class QuizAppContainer {
    val quizRepository: QuizRepository by lazy {
        RealtimeDatabaseQuizRepository()
    }
    val userProfileRepository: UserProfileRepository by lazy {
        FirebaseUserProfileRepository()
    }
    val authRepository: AuthRepository by lazy {
        AuthRepository(FirebaseAuth.getInstance())
    }
}
