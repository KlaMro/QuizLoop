package com.example.quizloop.feature.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.quizloop.core.model.Quiz

@Composable
fun HomeScreen(
    quizzes: List<Quiz>,
    onNavigateToCreateQuiz: () -> Unit,
    onNavigateToMultiplayer: () -> Unit,
    onNavigateToSinglePlayer: (String) -> Unit,
    onNavigateToProfile: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            IconButton(onClick = onNavigateToProfile) {
                Icon(Icons.Default.AccountCircle, contentDescription = "Profil", modifier = Modifier.fillMaxSize())
            }
            Text("Witaj w QuizLoop!", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = onNavigateToCreateQuiz, modifier = Modifier.fillMaxWidth()) {
                Text("Stwórz nowy quiz")
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = onNavigateToMultiplayer, modifier = Modifier.fillMaxWidth()) {
                Text("Tryb wieloosobowy")
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text("Dostępne quizy:", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (quizzes.isEmpty()) {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Text("Brak dostępnych quizów. Stwórz jeden!", modifier = Modifier.padding(16.dp))
                }
            }
        } else {
            items(quizzes) { quiz ->
                QuizCard(quiz = quiz, onStartSinglePlayer = { onNavigateToSinglePlayer(quiz.id) })
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun QuizCard(quiz: Quiz, onStartSinglePlayer: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onStartSinglePlayer() }
    ) {
        Box(modifier = Modifier.height(150.dp)) {
            if (quiz.imageUrl.isNotBlank()) {
                Image(
                    painter = rememberAsyncImagePainter(quiz.imageUrl),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                val color = try {
                    Color(android.graphics.Color.parseColor(quiz.backgroundColorHex))
                } catch (e: IllegalArgumentException) {
                    MaterialTheme.colorScheme.primary
                }
                Box(modifier = Modifier
                    .fillMaxSize()
                    .background(color))
            }
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = quiz.title,
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = "Autor: ${quiz.authorName}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White,
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}
