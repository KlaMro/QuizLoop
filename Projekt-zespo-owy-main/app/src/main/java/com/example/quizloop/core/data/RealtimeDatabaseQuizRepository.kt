package com.example.quizloop.core.data

import com.example.quizloop.core.model.Quiz
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await

class RealtimeDatabaseQuizRepository : QuizRepository {
    private val db = FirebaseDatabase.getInstance().reference

    override suspend fun getQuizzes(): List<Quiz> {
        val snapshot = db.child("quizzes").get().await()
        return snapshot.children.mapNotNull { it.getValue(Quiz::class.java) }
    }

    override suspend fun getQuiz(id: String): Quiz? {
        val snapshot = db.child("quizzes").child(id).get().await()
        return snapshot.getValue(Quiz::class.java)
    }

    override suspend fun addQuiz(quiz: Quiz) {
        db.child("quizzes").push().setValue(quiz).await()
    }
}
