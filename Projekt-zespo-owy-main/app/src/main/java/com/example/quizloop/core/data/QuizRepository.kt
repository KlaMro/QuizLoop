package com.example.quizloop.core.data

import com.example.quizloop.core.model.LiveQuizSession
import com.example.quizloop.core.model.QuizSummary
import kotlinx.coroutines.flow.StateFlow

interface QuizRepository {
    val quizzes: StateFlow<List<QuizSummary>>
    val liveSession: StateFlow<LiveQuizSession?>

    suspend fun createQuiz(title: String, subject: String, questionCount: Int): QuizSummary
    suspend fun saveQuiz(title: String, subject: String, questions: List<com.example.quizloop.core.model.QuizQuestion>): QuizSummary
    suspend fun joinRoom(roomCode: String): LiveQuizSession
    suspend fun startSinglePlayer(quizId: String): LiveQuizSession?
    fun submitAnswer(optionIndex: Int)
    fun advanceQuestion()
    fun endSession()
}
