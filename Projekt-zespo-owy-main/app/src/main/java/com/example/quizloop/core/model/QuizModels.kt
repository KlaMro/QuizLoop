package com.example.quizloop.core.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Quiz(
    @SerialName("id") val id: String = "",
    @SerialName("title") val title: String = "",
    @SerialName("subject") val subject: String = "",
    @SerialName("roomCode") val roomCode: String = "",
    @SerialName("questions") val questions: List<QuizQuestion> = emptyList(),
    @SerialName("authorId") val authorId: String = "",
    @SerialName("authorName") val authorName: String = "",
    @SerialName("imageUrl") val imageUrl: String = "",
    @SerialName("backgroundColorHex") val backgroundColorHex: String = ""
)

@Serializable
data class QuizQuestion(
    @SerialName("prompt") val prompt: String = "",
    @SerialName("options") val options: List<String> = emptyList(),
    @SerialName("correctIndex") val correctIndex: Int? = null, // Can be null for TRUE_FALSE
    @SerialName("correctBoolean") val correctBoolean: Boolean? = null, // For TRUE_FALSE questions
    @SerialName("correctAnswers") val correctAnswers: List<Int> = emptyList(), // dla pytań wielokrotnego wyboru
    @SerialName("questionType") val questionType: QuestionType = QuestionType.SINGLE_CHOICE
)

enum class QuestionType {
    SINGLE_CHOICE,
    MULTIPLE_CHOICE,
    TRUE_FALSE
}

@Serializable
data class Player(
    @SerialName("id") val id: String = "",
    @SerialName("name") val name: String = "",
    @SerialName("score") val score: Int = 0,
    @SerialName("answers") val answers: MutableMap<Int, List<Int>> = mutableMapOf() // Map<questionIndex, List<answerIndex>>
)

@Serializable
data class GameState(
    @SerialName("quiz") val quiz: Quiz? = null,
    @SerialName("hostId") val hostId: String = "",
    @SerialName("players") val players: MutableMap<String, Player> = mutableMapOf(),
    @SerialName("currentQuestionIndex") val currentQuestionIndex: Int = 0,
    @SerialName("questionStartTime") val questionStartTime: Long = 0,
    @SerialName("gameState") val gameState: String = "WAITING_FOR_PLAYERS"
)

@Serializable
data class UserProfile(
    @SerialName("userId") val userId: String = "",
    @SerialName("name") val name: String = "",
    @SerialName("score") val score: Int = 0,
    @SerialName("level") val level: Int = 1,
    @SerialName("backgroundColorHex") val backgroundColorHex: String = ""
)
