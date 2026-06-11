package com.example.quizloop

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quizloop.core.data.QuizRepository
import com.example.quizloop.core.model.Quiz
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class QuizLoopViewModel(private val quizRepository: QuizRepository) : ViewModel() {

    val quizzes: StateFlow<List<Quiz>> = quizRepository.getQuizzes()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun addQuiz(quiz: Quiz) {
        viewModelScope.launch {
            quizRepository.addQuiz(quiz)
        }
    }
}
