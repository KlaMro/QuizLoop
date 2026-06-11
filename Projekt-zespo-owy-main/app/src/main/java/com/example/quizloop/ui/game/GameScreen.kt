package com.example.quizloop.ui.game

import android.media.MediaPlayer
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.quizloop.R
import com.example.quizloop.core.model.GameState
import com.example.quizloop.core.model.QuestionType
import com.example.quizloop.core.model.QuizQuestion

@Composable
fun GameScreen(viewModel: GameViewModel) {
    val gameState by viewModel.gameState.collectAsState()
    val roomCode by viewModel.roomCode.collectAsState()
    val localPlayerId = viewModel.currentUserId

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        when (gameState?.gameState) {
            "WAITING_FOR_PLAYERS" -> WaitingRoom(roomCode, gameState, viewModel)
            "IN_PROGRESS" -> Gameplay(viewModel, gameState!!, localPlayerId)
            "FINISHED" -> FinalScoreboard(gameState!!, localPlayerId)
            else -> CircularProgressIndicator()
        }
    }
}

@Composable
fun WaitingRoom(roomCode: String?, gameState: GameState?, viewModel: GameViewModel) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (roomCode != null) {
            Text("Dołącz do pokoju z kodem:", style = MaterialTheme.typography.headlineMedium)
            Text(roomCode, style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(24.dp))
        }

        Text("Gracze w lobby:", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(8.dp))

        if (gameState != null) {
            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                items(gameState.players.values.toList()) { player ->
                    Text(player.name, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(4.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        if (viewModel.currentUserId == gameState?.hostId) {
            Button(onClick = { viewModel.startGame() }) {
                Text("Start")
            }
        } else {
            Text("Oczekiwanie na rozpoczęcie gry przez hosta...")
        }
    }
}

@Composable
fun Gameplay(viewModel: GameViewModel, state: GameState, localPlayerId: String) {
    val currentQuestion = state.quiz?.questions?.getOrNull(state.currentQuestionIndex)
    if (currentQuestion != null) {
        when (currentQuestion.questionType) {
            QuestionType.SINGLE_CHOICE, QuestionType.MULTIPLE_CHOICE -> {
                MultipleChoiceUI(viewModel, state, localPlayerId, currentQuestion)
            }
            QuestionType.TRUE_FALSE -> {
                TrueFalseUI(viewModel, state, localPlayerId, currentQuestion)
            }
        }
    }
}

@Composable
fun TrueFalseUI(
    viewModel: GameViewModel,
    state: GameState,
    localPlayerId: String,
    question: QuizQuestion
) {
    var selectedAnswer by remember(state.currentQuestionIndex) { mutableStateOf<Boolean?>(null) }
    val player = state.players[localPlayerId]
    val answerSubmitted = player?.answers?.containsKey(state.currentQuestionIndex) == true

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        QuestionHeader(state)
        Text(question.prompt, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(24.dp))

        listOf(true, false).forEach { value ->
            val isSelected = selectedAnswer == value
            val backgroundColor = if (answerSubmitted) {
                when {
                    question.correctBoolean == value -> Color.Green.copy(alpha = 0.6f)
                    isSelected -> Color.Red.copy(alpha = 0.6f)
                    else -> MaterialTheme.colorScheme.surface
                }
            } else {
                if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                else MaterialTheme.colorScheme.surface
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .clickable(enabled = !answerSubmitted) {
                        selectedAnswer = value
                    },
                colors = CardDefaults.cardColors(containerColor = backgroundColor)
            ) {
                Text(if (value) "Prawda" else "Fałsz", modifier = Modifier.padding(16.dp), fontSize = 18.sp)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = { if (selectedAnswer != null) viewModel.submitAnswer(listOf(if (selectedAnswer!!) 1 else 0)) }, // Map boolean to index
            enabled = selectedAnswer != null && !answerSubmitted,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Zatwierdź")
        }
    }
    PlaySound(answerSubmitted, state, localPlayerId, question)
}

@Composable
fun MultipleChoiceUI(
    viewModel: GameViewModel,
    state: GameState,
    localPlayerId: String,
    question: QuizQuestion
) {
    var selectedAnswers by remember(state.currentQuestionIndex) { mutableStateOf<List<Int>>(emptyList()) }
    val player = state.players[localPlayerId]
    val answerSubmitted = player?.answers?.containsKey(state.currentQuestionIndex) == true

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        QuestionHeader(state)
        Text(question.prompt, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(24.dp))

        LazyColumn(modifier = Modifier.fillMaxWidth()) {
            itemsIndexed(question.options) { index, option ->
                val isSelected = selectedAnswers.contains(index)
                val backgroundColor = if (answerSubmitted) {
                    val isCorrectAnswer = when (question.questionType) {
                        QuestionType.SINGLE_CHOICE -> question.correctIndex == index
                        QuestionType.MULTIPLE_CHOICE -> question.correctAnswers.contains(index)
                        else -> false
                    }
                    when {
                        isCorrectAnswer -> Color.Green.copy(alpha = 0.6f)
                        isSelected -> Color.Red.copy(alpha = 0.6f)
                        else -> MaterialTheme.colorScheme.surface
                    }
                } else {
                    if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                    else MaterialTheme.colorScheme.surface
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable(enabled = !answerSubmitted) {
                            selectedAnswers = if (question.questionType == QuestionType.SINGLE_CHOICE) {
                                listOf(index)
                            } else {
                                if (isSelected) selectedAnswers - index else selectedAnswers + index
                            }
                        },
                    colors = CardDefaults.cardColors(containerColor = backgroundColor)
                ) {
                    Text(option, modifier = Modifier.padding(16.dp), fontSize = 18.sp)
                }
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = { viewModel.submitAnswer(selectedAnswers) },
            enabled = selectedAnswers.isNotEmpty() && !answerSubmitted,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Zatwierdź")
        }
    }
    PlaySound(answerSubmitted, state, localPlayerId, question)
}

@Composable
private fun QuestionHeader(state: GameState) {
    Text("Pytanie ${state.currentQuestionIndex + 1}/${state.quiz?.questions?.size ?: 0}", style = MaterialTheme.typography.headlineSmall)
    Spacer(modifier = Modifier.height(16.dp))
}

@Composable
fun PlaySound(answerSubmitted: Boolean, state: GameState, localPlayerId: String, question: QuizQuestion) {
    var soundPlayed by remember(state.currentQuestionIndex) { mutableStateOf(false) }
    val context = LocalContext.current
    val correctPlayer = remember { MediaPlayer.create(context, R.raw.correct) }
    val wrongPlayer = remember { MediaPlayer.create(context, R.raw.wrong) }

    DisposableEffect(Unit) {
        onDispose {
            correctPlayer.release()
            wrongPlayer.release()
        }
    }

    if (answerSubmitted && !soundPlayed) {
        val playerAnswer = state.players[localPlayerId]?.answers?.get(state.currentQuestionIndex)
        val isCorrect = when (question.questionType) {
            QuestionType.TRUE_FALSE -> question.correctBoolean == (playerAnswer?.firstOrNull() == 1)
            QuestionType.SINGLE_CHOICE -> playerAnswer?.firstOrNull() == question.correctIndex
            QuestionType.MULTIPLE_CHOICE -> playerAnswer?.let { it.toSet() == question.correctAnswers.toSet() } ?: false
        }

        if (isCorrect) {
            correctPlayer.start()
        } else {
            wrongPlayer.start()
        }
        soundPlayed = true
    }
}

@Composable
fun FinalScoreboard(state: GameState, localPlayerId: String) {
    val sortedPlayers = state.players.values.sortedByDescending { it.score }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Koniec Gry!", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(24.dp))
        Text("Wyniki:", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(modifier = Modifier.fillMaxWidth()) {
            itemsIndexed(sortedPlayers) { index, player ->
                val isLocalPlayer = player.id == localPlayerId
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isLocalPlayer) MaterialTheme.colorScheme.primary.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surface
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${index + 1}. ${player.name}",
                            fontWeight = if (isLocalPlayer) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 18.sp
                        )
                        Text(
                            text = "${player.score} pkt",
                            fontWeight = if (isLocalPlayer) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 18.sp
                        )
                    }
                }
            }
        }
    }
}