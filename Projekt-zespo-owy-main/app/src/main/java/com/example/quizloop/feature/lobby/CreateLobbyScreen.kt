package com.example.quizloop.feature.lobby

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun CreateLobbyScreen(
    onSinglePlayer: () -> Unit,
    onMultiPlayer: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Create lobby")
        Button(onClick = onSinglePlayer, modifier = Modifier.fillMaxWidth()) {
            Text("Singleplayer")
        }
        Button(onClick = onMultiPlayer, modifier = Modifier.fillMaxWidth(), enabled = false, colors = ButtonDefaults.buttonColors()) {
            Text("Multiplayer (disabled)")
        }
        Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
            Text("Back")
        }
    }
}
