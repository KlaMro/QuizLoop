package com.example.quizloop.feature.play

import android.content.Context
import android.media.MediaPlayer
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.quizloop.R
import com.example.quizloop.core.model.LiveQuizSession
import com.example.quizloop.core.model.QuestionType

@Composable
fun PlayQuizScreen(
    session: LiveQuizSession?,
    onAnswer: (List<Int>) -> Unit,
    onNext: () -> Unit,
    onExit: () -> Unit
) {
    if (session == null) {
        EmptySessionState(onExit = onExit)
        return
    }

    val context = LocalContext.current
    val correctPlayer = remember { MediaPlayer.create(context, R.raw.correct) }
    val wrongPlayer = remember { MediaPlayer.create(context, R.raw.wrong) }

    var selectedAnswers by remember(session.currentQuestionIndex) { mutableStateOf<List<Int>>(emptyList()) }
    var answerConfirmed by remember(session.currentQuestionIndex) { mutableStateOf(false) }

    DisposableEffect(Unit) {
        onDispose {
            correctPlayer.release()
            wrongPlayer.release()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            tonalElevation = 4.dp,
            shadowElevation = 10.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.secondary
                            )
                        )
                    )
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(session.quizTitle, style = MaterialTheme.typography.headlineMedium, color = Color.White)
                Text("Room ${session.roomCode} · ${session.subject}", color = Color.White.copy(alpha = 0.9f))
                Text("Players online: ${session.playersOnline}", color = Color.White.copy(alpha = 0.9f))
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text("Question ${session.progressLabel}", style = MaterialTheme.typography.titleLarge)
                Text(session.currentQuestion?.prompt ?: "No active question")
                
                session.currentQuestion?.options?.forEachIndexed { index, option ->
                    val isSelected = selectedAnswers.contains(index)
                    val isCorrect = session.currentQuestion.correctAnswers.contains(index) || session.currentQuestion.correctIndex == index
                    
                    val buttonColors = when {
                        answerConfirmed && isCorrect -> ButtonDefaults.buttonColors(containerColor = Color.Green.copy(alpha = 0.5f))
                        answerConfirmed && isSelected && !isCorrect -> ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.5f))
                        isSelected -> ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        else -> ButtonDefaults.outlinedButtonColors()
                    }

                    Button(
                        onClick = {
                            if (!answerConfirmed) {
                                if (session.currentQuestion.questionType == QuestionType.SINGLE_CHOICE) {
                                    selectedAnswers = listOf(index)
                                } else {
                                    selectedAnswers = if (isSelected) {
                                        selectedAnswers - index
                                    } else {
                                        selectedAnswers + index
                                    }
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = buttonColors,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(option)
                    }
                }

                if (answerConfirmed) {
                     Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        ElevatedButton(onClick = onNext) {
                            Text(if (session.currentQuestionIndex >= session.questions.lastIndex) "Finish round" else "Next question")
                        }
                        OutlinedButton(onClick = onExit) {
                            Text("Exit room")
                        }
                    }
                } else if (selectedAnswers.isNotEmpty()) {
                    Button(onClick = {
                        answerConfirmed = true
                        onAnswer(selectedAnswers)
                        
                        val isAnswerCorrect = when (session.currentQuestion.questionType) {
                            QuestionType.SINGLE_CHOICE -> selectedAnswers.firstOrNull() == session.currentQuestion.correctIndex
                            QuestionType.MULTIPLE_CHOICE -> selectedAnswers.sorted() == session.currentQuestion.correctAnswers.sorted()
                        }

                        if (isAnswerCorrect) {
                            correctPlayer.start()
                        } else {
                            wrongPlayer.start()
                        }
                    }) {
                        Text("Confirm")
                    }
                } else {
                    OutlinedButton(onClick = onExit) {
                        Text("Leave room")
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
private fun EmptySessionState(onExit: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("No live room is active right now.", style = MaterialTheme.typography.headlineMedium)
        Text("Join a room first to open the playable quiz experience.")
        Button(onClick = onExit) {
            Text("Back to home")
        }
    }
}
