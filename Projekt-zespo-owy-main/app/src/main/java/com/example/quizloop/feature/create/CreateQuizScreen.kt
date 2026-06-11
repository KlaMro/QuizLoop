package com.example.quizloop.feature.create

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
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.quizloop.core.model.QuestionType
import com.example.quizloop.core.model.Quiz
import com.example.quizloop.core.model.QuizQuestion

// Definicja 10 predefiniowanych kolorów
val predefinedColors = listOf(
    Color(0xFFF44336), Color(0xFFE91E63), Color(0xFF9C27B0), Color(0xFF673AB7),
    Color(0xFF3F51B5), Color(0xFF2196F3), Color(0xFF03A9F4), Color(0xFF00BCD4),
    Color(0xFF009688), Color(0xFF4CAF50)
)

fun Color.toHexString(): String {
    return String.format("#%06X", 0xFFFFFF and this.value.toLong())
}

@Composable
fun CreateQuizScreen(onSaveQuiz: (Quiz) -> Unit, onBack: () -> Unit) {
    var title by remember { mutableStateOf("") }
    var subject by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf(predefinedColors.first()) }
    val questions = remember { mutableStateListOf<QuizQuestion>() }

    // TODO: Pobierz prawdziwe dane użytkownika po implementacji autentykacji
    val authorId = "user123"
    val authorName = "TestUser"

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Text("Stwórz nowy quiz", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Tytuł quizu") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = subject,
                onValueChange = { subject = it },
                label = { Text("Temat") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = imageUrl,
                onValueChange = { imageUrl = it },
                label = { Text("URL obrazka (opcjonalnie)") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text("Lub wybierz kolor tła:", style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.height(8.dp))
            ColorPicker(
                colors = predefinedColors,
                selectedColor = selectedColor,
                onColorSelected = { selectedColor = it },
                enabled = imageUrl.isBlank()
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text("Pytania", style = MaterialTheme.typography.titleLarge)
        }

        items(questions) { question ->
            QuestionItem(question = question)
            Spacer(modifier = Modifier.height(8.dp))
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
            QuestionEditor(onQuestionAdd = { questions.add(it) })
            Spacer(modifier = Modifier.height(24.dp))
            Row {
                Button(onClick = {
                    val newQuiz = Quiz(
                        title = title,
                        subject = subject,
                        questions = questions.toList(),
                        authorId = authorId,
                        authorName = authorName,
                        imageUrl = imageUrl,
                        backgroundColorHex = if (imageUrl.isBlank()) selectedColor.toHexString() else ""
                    )
                    onSaveQuiz(newQuiz)
                    onBack()
                }, enabled = title.isNotBlank() && subject.isNotBlank() && questions.isNotEmpty()) {
                    Text("Zapisz quiz")
                }
                Spacer(modifier = Modifier.weight(1f))
                Button(onClick = onBack) {
                    Text("Anuluj")
                }
            }
        }
    }
}

@Composable
fun QuestionEditor(onQuestionAdd: (QuizQuestion) -> Unit) {
    var prompt by remember { mutableStateOf("") }
    var questionType by remember { mutableStateOf(QuestionType.SINGLE_CHOICE) }

    // State for single/multiple choice
    val options = remember { mutableStateListOf("", "") }
    val selectedAnswers = remember { mutableStateListOf<Int>() }

    // State for true/false
    var trueFalseAnswer by remember { mutableStateOf(true) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Nowe pytanie", style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(
                value = prompt,
                onValueChange = { prompt = it },
                label = { Text("Pytanie") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))

            Text("Typ pytania:", style = MaterialTheme.typography.labelLarge)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                QuestionType.values().forEach { type ->
                    OutlinedButton(onClick = { questionType = type }) {
                        Text(type.name.replace('_', ' '))
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            when (questionType) {
                QuestionType.SINGLE_CHOICE, QuestionType.MULTIPLE_CHOICE -> {
                    options.forEachIndexed { index, option ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = selectedAnswers.contains(index), onCheckedChange = {
                                if (questionType == QuestionType.SINGLE_CHOICE) {
                                    selectedAnswers.clear()
                                    selectedAnswers.add(index)
                                } else {
                                    if (selectedAnswers.contains(index)) selectedAnswers.remove(index) else selectedAnswers.add(index)
                                }
                            })
                            OutlinedTextField(value = option, onValueChange = { options[index] = it }, label = { Text("Opcja ${index + 1}") })
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row {
                        Button(onClick = { options.add("") }, enabled = options.size < 6) {
                            Text("Dodaj opcję")
                        }
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
                QuestionType.TRUE_FALSE -> {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = trueFalseAnswer, onClick = { trueFalseAnswer = true })
                        Text("Prawda")
                        Spacer(Modifier.padding(start = 16.dp))
                        RadioButton(selected = !trueFalseAnswer, onClick = { trueFalseAnswer = false })
                        Text("Fałsz")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            val isQuestionValid = prompt.isNotBlank() && when (questionType) {
                QuestionType.SINGLE_CHOICE, QuestionType.MULTIPLE_CHOICE -> options.count { it.isNotBlank() } >= 2 && selectedAnswers.isNotEmpty()
                QuestionType.TRUE_FALSE -> true
            }

            Button(
                onClick = {
                    val question = when (questionType) {
                        QuestionType.SINGLE_CHOICE -> QuizQuestion(
                            prompt = prompt,
                            options = options.filter { it.isNotBlank() },
                            questionType = QuestionType.SINGLE_CHOICE,
                            correctIndex = selectedAnswers.firstOrNull(),
                            correctBoolean = null,
                            correctAnswers = emptyList()
                        )
                        QuestionType.MULTIPLE_CHOICE -> QuizQuestion(
                            prompt = prompt,
                            options = options.filter { it.isNotBlank() },
                            questionType = QuestionType.MULTIPLE_CHOICE,
                            correctIndex = null,
                            correctBoolean = null,
                            correctAnswers = selectedAnswers.toList()
                        )
                        QuestionType.TRUE_FALSE -> QuizQuestion(
                            prompt = prompt,
                            options = emptyList(),
                            questionType = QuestionType.TRUE_FALSE,
                            correctIndex = null,
                            correctBoolean = trueFalseAnswer,
                            correctAnswers = emptyList()
                        )
                    }
                    onQuestionAdd(question)

                    // Reset editor state
                    prompt = ""
                    options.clear(); options.addAll(listOf("", ""))
                    selectedAnswers.clear()
                    trueFalseAnswer = true
                },
                enabled = isQuestionValid,
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Dodaj pytanie")
            }
        }
    }
}

@Composable
fun QuestionItem(question: QuizQuestion) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(question.prompt, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))

            when (question.questionType) {
                QuestionType.SINGLE_CHOICE, QuestionType.MULTIPLE_CHOICE -> {
                    question.options.forEachIndexed { index, option ->
                        val isCorrect = (question.questionType == QuestionType.SINGLE_CHOICE && question.correctIndex == index) ||
                                (question.questionType == QuestionType.MULTIPLE_CHOICE && question.correctAnswers.contains(index))
                        Text(text = option, color = if (isCorrect) Color.Green else Color.Unspecified)
                    }
                }
                QuestionType.TRUE_FALSE -> {
                    Text(text = "Prawda", color = if (question.correctBoolean == true) Color.Green else Color.Unspecified)
                    Text(text = "Fałsz", color = if (question.correctBoolean == false) Color.Green else Color.Unspecified)
                }
            }
        }
    }
}

// TODO: Implement ColorPicker Composable if not already present
@Composable
fun ColorPicker(colors: List<Color>, selectedColor: Color, onColorSelected: (Color) -> Unit, enabled: Boolean) {
    // Placeholder implementation
}
