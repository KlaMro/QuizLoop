package com.example.quizloop.ui.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.quizloop.core.data.QuizRepository
import com.example.quizloop.core.data.UserProfileRepository

class GameViewModelFactory(private val quizRepository: QuizRepository, private val userProfileRepository: UserProfileRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GameViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return GameViewModel(quizRepository, userProfileRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
