package com.example.quizloop.core.data

import com.example.quizloop.core.model.LiveQuizSession
import com.example.quizloop.core.model.QuizQuestion
import com.example.quizloop.core.model.QuizSummary
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.UUID
import kotlin.math.max

class InMemoryQuizRepository : QuizRepository {
    private val _quizzes = MutableStateFlow(
        listOf(
            QuizSummary(
                id = "sample-science",
                title = "Planet Sprint",
                subject = "Science",
                roomCode = "SPACE7",
                questionCount = 5
            ),
            QuizSummary(
                id = "sample-history",
                title = "History Dash",
                subject = "History",
                roomCode = "TIMELINE",
                questionCount = 4
            )
        )
    )

    private val _liveSession = MutableStateFlow<LiveQuizSession?>(null)
    // store saved quiz questions keyed by room code so joinRoom can load them
    private val quizQuestionBank: MutableMap<String, List<QuizQuestion>> = mutableMapOf()

    override val quizzes: StateFlow<List<QuizSummary>> = _quizzes
    override val liveSession: StateFlow<LiveQuizSession?> = _liveSession

    override suspend fun createQuiz(title: String, subject: String, questionCount: Int): QuizSummary {
        val trimmedTitle = title.trim().ifBlank { "Untitled Quiz" }
        val trimmedSubject = subject.trim().ifBlank { "General" }
        val safeQuestionCount = max(1, questionCount)
        val roomCode = buildRoomCode(trimmedTitle)
        val newQuiz = QuizSummary(
            id = UUID.randomUUID().toString(),
            title = trimmedTitle,
            subject = trimmedSubject,
            roomCode = roomCode,
            questionCount = safeQuestionCount
        )

        _quizzes.value = listOf(newQuiz) + _quizzes.value
        return newQuiz
    }

    override suspend fun saveQuiz(title: String, subject: String, questions: List<com.example.quizloop.core.model.QuizQuestion>): QuizSummary {
        val roomCode = buildRoomCode(title)
        val newQuiz = QuizSummary(
            id = UUID.randomUUID().toString(),
            title = title.trim().ifBlank { "Untitled Quiz" },
            subject = subject.trim().ifBlank { "General" },
            roomCode = roomCode,
            questionCount = questions.size
        )
        // Prepend to list
        _quizzes.value = listOf(newQuiz) + _quizzes.value
        // Also set a live session for demo convenience
        // persist questions for future joins
        quizQuestionBank[roomCode] = questions
        // when creating a live session, shuffle questions and options for variety
        _liveSession.value = LiveQuizSession(
            roomCode = roomCode,
            quizTitle = newQuiz.title,
            subject = newQuiz.subject,
            playersOnline = 1,
            questions = shuffleQuestions(questions)
        )
        return newQuiz
    }

    override suspend fun joinRoom(roomCode: String): LiveQuizSession {
        val requestedCode = roomCode.trim().uppercase()
        val matchedQuiz = _quizzes.value.firstOrNull { it.roomCode.uppercase() == requestedCode }

        if (matchedQuiz != null) {
            // load saved questions if available, otherwise fall back to demo questions
                val savedQuestions = quizQuestionBank[matchedQuiz.roomCode] ?: buildDemoSession(
                    roomCode = matchedQuiz.roomCode,
                    quizTitle = matchedQuiz.title,
                    subject = matchedQuiz.subject,
                    playersOnline = 12
                ).questions

                // randomize order per-join
                val sessionQuestions = shuffleQuestions(savedQuestions)

            val session = LiveQuizSession(
                roomCode = matchedQuiz.roomCode,
                quizTitle = matchedQuiz.title,
                subject = matchedQuiz.subject,
                playersOnline = 12,
                    questions = sessionQuestions
            )
            _liveSession.value = session
            return session
        }

        val session = buildDemoSession(
            roomCode = if (requestedCode.isBlank()) "DEMO42" else requestedCode,
            quizTitle = "Live Quiz Room",
            subject = "General Knowledge",
            playersOnline = 8
        )

        _liveSession.value = session
        return session
    }

    override suspend fun startSinglePlayer(quizId: String): LiveQuizSession? {
        val quiz = _quizzes.value.firstOrNull { it.id == quizId }
        if (quiz == null) return null

        val savedQuestions = quizQuestionBank[quiz.roomCode] ?: buildDemoSession(
            roomCode = quiz.roomCode,
            quizTitle = quiz.title,
            subject = quiz.subject,
            playersOnline = 1
        ).questions

        val sessionQuestions = shuffleQuestions(savedQuestions)

        val session = LiveQuizSession(
            roomCode = quiz.roomCode,
            quizTitle = quiz.title,
            subject = quiz.subject,
            playersOnline = 1,
            questions = sessionQuestions
        )

        _liveSession.value = session
        return session
    }

    override fun submitAnswer(optionIndex: Int) {
        _liveSession.value = _liveSession.value?.let { session ->
            val question = session.currentQuestion ?: return@let session
            if (session.selectedAnswerIndex != null) {
                return@let session
            }

            val isCorrect = optionIndex == question.correctIndex
            session.copy(
                selectedAnswerIndex = optionIndex,
                score = session.score + if (isCorrect) 100 else 0,
                feedbackMessage = if (isCorrect) {
                    "Correct. Nice read of the room."
                } else {
                    "Not this time. The right answer was ${question.options[question.correctIndex]}."
                }
            )
        }
    }

    override fun advanceQuestion() {
        _liveSession.value = _liveSession.value?.let { session ->
            val nextIndex = session.currentQuestionIndex + 1
            if (nextIndex > session.questions.lastIndex) {
                session.copy(
                    feedbackMessage = "Round complete. Final score: ${session.score}/${session.questions.size * 100}"
                )
            } else {
                session.copy(
                    currentQuestionIndex = nextIndex,
                    selectedAnswerIndex = null,
                    feedbackMessage = "Next question is live."
                )
            }
        }
    }

    override fun endSession() {
        _liveSession.value = null
    }

    private fun buildRoomCode(title: String): String {
        val prefix = title
            .filter { it.isLetterOrDigit() }
            .take(4)
            .uppercase()
            .padEnd(4, 'X')
        val suffix = (100..999).random()
        return "$prefix$suffix"
    }

    private fun buildDemoSession(
        roomCode: String,
        quizTitle: String,
        subject: String,
        playersOnline: Int
    ): LiveQuizSession {
        return LiveQuizSession(
            roomCode = roomCode,
            quizTitle = quizTitle,
            subject = subject,
            playersOnline = playersOnline,
            questions = listOf(
                QuizQuestion(
                    prompt = "Which planet is known as the Red Planet?",
                    options = listOf("Earth", "Mars", "Venus", "Jupiter"),
                    correctIndex = 1
                ),
                QuizQuestion(
                    prompt = "What language is used to build this Android app?",
                    options = listOf("Swift", "Kotlin", "TypeScript", "Ruby"),
                    correctIndex = 1
                ),
                QuizQuestion(
                    prompt = "Which pattern is best for swapping a local demo backend with Firebase later?",
                    options = listOf("Repository", "Global state", "Hard-coded screen logic", "No abstraction"),
                    correctIndex = 0
                )
            )
        )
    }

    // Shuffle helpers: randomize options within each question and the question order
    private fun shuffleQuestionOptions(q: QuizQuestion): QuizQuestion {
        if (q.options.size <= 1) return q
        val indices = q.options.indices.toMutableList()
        indices.shuffle()
        val newOptions = indices.map { q.options[it] }
        val newCorrectIndex = indices.indexOf(q.correctIndex).coerceAtLeast(0)
        return q.copy(options = newOptions, correctIndex = newCorrectIndex)
    }

    private fun shuffleQuestions(questions: List<QuizQuestion>): List<QuizQuestion> {
        return questions.map { shuffleQuestionOptions(it) }.shuffled()
    }
}
