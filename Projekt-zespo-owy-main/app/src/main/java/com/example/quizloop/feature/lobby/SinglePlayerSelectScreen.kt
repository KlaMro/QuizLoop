package com.example.quizloop.feature.lobby

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.quizloop.core.model.QuizSummary

@Composable
fun SinglePlayerSelectScreen(
    quizzes: List<QuizSummary>,
    onStart: (String) -> Unit,
    onBack: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Select a quiz to play")
        quizzes.forEach { quiz ->
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors()) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(quiz.title)
                    Text("${quiz.subject} · ${quiz.questionCount} questions")
                    Button(onClick = { onStart(quiz.id) }, modifier = Modifier.fillMaxWidth()) {
                        Text("Start")
                    }
                }
            }
        }
        Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("Back") }
    }
}
