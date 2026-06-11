package com.example.quizloop

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.quizloop.core.data.QuizRepository
import com.example.quizloop.ui.theme.QuizLoopTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val repository = (application as QuizLoopApplication).container.quizRepository

        setContent {
            QuizLoopTheme {
                val viewModel: QuizLoopViewModel = viewModel(
                    factory = QuizLoopViewModelFactory(repository)
                )
                QuizLoopApp(viewModel = viewModel, mainActivity = this)
            }
        }
    }
}

private class QuizLoopViewModelFactory(
    private val repository: QuizRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        if (modelClass.isAssignableFrom(QuizLoopViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return QuizLoopViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
