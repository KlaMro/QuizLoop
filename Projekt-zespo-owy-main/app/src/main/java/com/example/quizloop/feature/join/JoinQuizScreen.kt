package com.example.quizloop.feature.join

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.example.quizloop.core.model.LiveQuizSession
import kotlinx.coroutines.launch

@Composable
fun JoinQuizScreen(
    onJoinRoom: suspend (String) -> LiveQuizSession,
    onJoined: () -> Unit,
    onBack: () -> Unit
) {
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()
    var roomCode by remember { mutableStateOf("") }
    var joinError by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Join a live room", style = androidx.compose.material3.MaterialTheme.typography.headlineMedium)
        Text(
            text = "Enter a room code and jump into the sample live session. The same flow can later call Firebase listeners instead of the in-memory repository.",
            style = androidx.compose.material3.MaterialTheme.typography.bodyMedium
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors()
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                OutlinedTextField(
                    value = roomCode,
                    onValueChange = { roomCode = it.uppercase() },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Room code") },
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
                    singleLine = true
                )
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(
                        onClick = {
                            scope.launch {
                                joinError = null
                                runCatching { onJoinRoom(roomCode) }
                                    .onSuccess { onJoined() }
                                    .onFailure { joinError = it.message ?: "Unable to join room." }
                            }
                        }
                    ) {
                        Text("Join room")
                    }
                    Button(onClick = onBack) {
                        Text("Back")
                    }
                }
                if (joinError != null) {
                    Text(text = joinError!!)
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("What happens next?", style = androidx.compose.material3.MaterialTheme.typography.titleLarge)
                Text("1. The sample room loads a live quiz session.")
                Text("2. Players answer questions together.")
                Text("3. The repository can later be swapped for Firestore without changing this screen.")
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}
