package com.example.quizloop.ui.multiplayer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.quizloop.core.model.Quiz

@Composable
fun MultiplayerLobbyScreen(
    quizzes: List<Quiz>,
    onNavigateToGame: (String?, String?) -> Unit
) {
    var roomCode by remember { mutableStateOf("") }
    var selectedQuizId by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Tryb wieloosobowy", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(24.dp))

        // Join Room Section
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Dołącz do pokoju", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = roomCode,
                    onValueChange = { roomCode = it },
                    label = { Text("Wprowadź kod pokoju") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { onNavigateToGame(null, roomCode) },
                    enabled = roomCode.isNotBlank(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Dołącz")
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Create Room Section
        Text("Lub stwórz nowy pokój", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(modifier = Modifier.fillMaxWidth()) {
            items(quizzes) {
                quiz ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(quiz.title, style = MaterialTheme.typography.titleMedium)
                            Text("${quiz.questions.size} pytań")
                        }
                        Button(onClick = { onNavigateToGame(quiz.id, null) }) {
                            Text("Stwórz pokój")
                        }https://console.cloud.google.com/welcome?authuser=0&hl=en-US&project=quizloop-b83ce
                    }
                }
            }
        }
    }
}
