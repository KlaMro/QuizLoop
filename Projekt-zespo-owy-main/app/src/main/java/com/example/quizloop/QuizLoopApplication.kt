package com.example.quizloop

import android.app.Application
import com.example.quizloop.core.data.QuizAppContainer

class QuizLoopApplication : Application() {
    val container: QuizAppContainer by lazy {
        QuizAppContainer()
    }
}
