package com.example.quizloop.feature.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.quizloop.core.model.UserProfile

val predefinedColors = listOf(
    Color(0xFFF44336), Color(0xFFE91E63), Color(0xFF9C27B0), Color(0xFF673AB7),
    Color(0xFF3F51B5), Color(0xFF2196F3), Color(0xFF03A9F4), Color(0xFF00BCD4),
    Color(0xFF009688), Color(0xFF4CAF50)
)
fun Color.toHexString(): String {
    return String.format("#%06X", 0xFFFFFF and this.value.toLong())
}
fun parseColor(hex: String): Color {
    return try {
        Color(android.graphics.Color.parseColor(hex))
    } catch (e: Exception) {
        predefinedColors.first()
    }
}

@Composable
fun ProfileScreen(viewModel: ProfileViewModel) {
    val userProfile by viewModel.userProfile.collectAsState()
    var isEditMode by remember { mutableStateOf(false) }

    var editedName by remember { mutableStateOf(userProfile.name) }
    var editedColor by remember { mutableStateOf(parseColor(userProfile.backgroundColorHex)) }

    LaunchedEffect(userProfile) {
        if (!isEditMode) {
            editedName = userProfile.name
            editedColor = parseColor(userProfile.backgroundColorHex)
        }
    }

    val displayColor = if(isEditMode) editedColor else parseColor(userProfile.backgroundColorHex)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(displayColor.copy(alpha = 0.2f))
    ) {
        if (isEditMode) {
            EditProfileView(
                editedName = editedName,
                editedColor = editedColor,
                onNameChange = { editedName = it },
                onColorChange = { editedColor = it },
                onSave = {
                    viewModel.updateUserProfile(editedName, editedColor.toHexString())
                    isEditMode = false
                },
                onCancel = { isEditMode = false }
            )
        } else {
            DisplayProfileView(userProfile, displayColor)
        }

        if (!isEditMode) {
            FloatingActionButton(
                onClick = { isEditMode = true },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp),
                shape = CircleShape
            ) {
                Icon(Icons.Filled.Edit, contentDescription = "Edytuj profil")
            }
        }
    }
}

@Composable
fun DisplayProfileView(userProfile: UserProfile, backgroundColor: Color) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(backgroundColor),
            contentAlignment = Alignment.Center
        ) {
            if (userProfile.name.isNotBlank()) {
                Text(
                    text = userProfile.name.first().toString(),
                    style = MaterialTheme.typography.headlineLarge,
                    color = Color.White
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = userProfile.name, style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(32.dp))
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Statystyki", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Punkty: ${userProfile.score}", style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Poziom: ${userProfile.level}", style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}

@Composable
fun EditProfileView(
    editedName: String,
    editedColor: Color,
    onNameChange: (String) -> Unit,
    onColorChange: (Color) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Edytuj profil", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(24.dp))
        OutlinedTextField(
            value = editedName,
            onValueChange = onNameChange,
            label = { Text("Nazwa użytkownika") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text("Wybierz kolor tła:", style = MaterialTheme.typography.bodyLarge)
        Spacer(modifier = Modifier.height(8.dp))
        ColorPicker(predefinedColors, editedColor, onColorChange)
        Spacer(modifier = Modifier.height(32.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            Button(onClick = onCancel) {
                Text("Anuluj")
            }
            Spacer(modifier = Modifier.padding(start = 8.dp))
            Button(onClick = onSave, enabled = editedName.isNotBlank()) {
                Text("Zapisz")
            }
        }
    }
}

@Composable
fun ColorPicker(colors: List<Color>, selectedColor: Color, onColorSelected: (Color) -> Unit) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        items(colors) { color ->
            val modifier = if (color == selectedColor) {
                Modifier
                    .padding(4.dp)
                    .size(40.dp)
                    .background(color, CircleShape)
                    .border(2.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
            } else {
                Modifier
                    .padding(4.dp)
                    .size(40.dp)
                    .background(color, CircleShape)
            }
            Box(
                modifier = modifier.clickable { onColorSelected(color) }
            )
        }
    }
}
