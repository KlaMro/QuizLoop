package com.example.quizloop.ui.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quizloop.core.data.QuizRepository
import com.example.quizloop.core.data.UserProfileRepository
import com.example.quizloop.core.model.GameState
import com.example.quizloop.core.model.Player
import com.example.quizloop.core.model.QuestionType
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class GameViewModel(private val quizRepository: QuizRepository, private val userProfileRepository: UserProfileRepository) : ViewModel() {

    private val _gameState = MutableStateFlow<GameState?>(null)
    val gameState: StateFlow<GameState?> = _gameState

    private val _roomCode = MutableStateFlow<String?>(null)
    val roomCode: StateFlow<String?> = _roomCode

    private val db = FirebaseDatabase.getInstance().reference.child("rooms")
    private var roomListener: ValueEventListener? = null
    private var timerJob: Job? = null
    private var isRoomInitialized = false

    // This should be set after user logs in
    val currentUserId: String = "user123" // Hardcoded for now
    val currentUserName: String = "TestUser" // Hardcoded for now

    fun initGame(quizId: String?, roomCode: String?) {
        if (isRoomInitialized) return
        isRoomInitialized = true

        if (roomCode != null) {
            joinRoom(roomCode)
        } else if (quizId != null) {
            createRoom(quizId)
        }
    }

    private fun createRoom(quizId: String) {
        viewModelScope.launch {
            val quiz = quizRepository.getQuiz(quizId)
            quiz?.let {
                val newRoomCode = generateRoomCode()
                _roomCode.value = newRoomCode
                val hostPlayer = Player(id = currentUserId, name = currentUserName)
                val initialGameState = GameState(
                    quiz = it,
                    hostId = currentUserId,
                    players = mutableMapOf(currentUserId to hostPlayer),
                    gameState = "WAITING_FOR_PLAYERS"
                )
                db.child(newRoomCode).setValue(initialGameState).addOnSuccessListener {
                    joinRoom(newRoomCode)
                }
            }
        }
    }

    private fun joinRoom(roomCode: String) {
        val roomRef = db.child(roomCode)
        roomRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    _roomCode.value = roomCode
                    val player = Player(id = currentUserId, name = currentUserName)
                    roomRef.child("players").child(currentUserId).setValue(player).addOnSuccessListener {
                        addRoomEventListener(roomCode)
                    }
                } else {
                    // Handle room not found
                }
            }
            override fun onCancelled(error: DatabaseError) { /* Handle error */ }
        })
    }

    fun startGame() {
        _roomCode.value?.let { code ->
            if (_gameState.value?.hostId == currentUserId) {
                db.child(code).child("gameState").setValue("IN_PROGRESS")
                startTimerForCurrentQuestion(code)
            }
        }
    }

    private fun nextQuestion() {
        _roomCode.value?.let { code ->
            if (_gameState.value?.hostId != currentUserId) return

            viewModelScope.launch {
                val currentState = _gameState.value ?: return@launch
                calculateScores(currentState)

                val nextIndex = currentState.currentQuestionIndex + 1
                if (nextIndex < (currentState.quiz?.questions?.size ?: 0)) {
                    val updates = mapOf(
                        "currentQuestionIndex" to nextIndex,
                        "questionStartTime" to System.currentTimeMillis()
                    )
                    db.child(code).updateChildren(updates).addOnSuccessListener {
                        startTimerForCurrentQuestion(code)
                    }
                } else {
                    db.child(code).child("gameState").setValue("FINISHED").addOnSuccessListener {
                        awardPointsToPlayers(currentState)
                    }
                }
            }
        }
    }

    private fun calculateScores(state: GameState) {
        val question = state.quiz?.questions?.getOrNull(state.currentQuestionIndex) ?: return
        state.players.forEach { (playerId, player) ->
            val answers = player.answers[state.currentQuestionIndex] ?: return@forEach
            var scoreForQuestion = 0.0

            when (question.questionType) {
                QuestionType.SINGLE_CHOICE -> {
                    if (answers.firstOrNull() == question.correctIndex) {
                        scoreForQuestion = 1.0
                    }
                }
                QuestionType.MULTIPLE_CHOICE -> {
                    val correctPicks = answers.count { it in question.correctAnswers }
                    val wrongPicks = answers.count { it !in question.correctAnswers }
                    // Avoid division by zero if all options are correct
                    val penaltyDenominator = (question.options.size - question.correctAnswers.size).coerceAtLeast(1)
                    val score = (correctPicks.toDouble() / question.correctAnswers.size) - (wrongPicks.toDouble() / penaltyDenominator)
                    if (score > 0) scoreForQuestion = score
                }
                QuestionType.TRUE_FALSE -> {
                    val playerAnswer = answers.firstOrNull()
                    // 1 for true, 0 for false
                    val answeredBoolean = playerAnswer == 1
                    if (answeredBoolean == question.correctBoolean) {
                        scoreForQuestion = 1.0
                    }
                }
            }
            val newTotalScore = player.score + (scoreForQuestion * 10).toInt()
            db.child(_roomCode.value!!).child("players").child(playerId).child("score").setValue(newTotalScore)
        }
    }

    private fun awardPointsToPlayers(state: GameState) {
        viewModelScope.launch {
            state.players.forEach { (playerId, player) ->
                userProfileRepository.addScore(playerId, player.score)
            }
        }
    }

    fun submitAnswer(answerIndexes: List<Int>) {
        _roomCode.value?.let { code ->
            val questionIndex = _gameState.value?.currentQuestionIndex ?: return
            db.child(code).child("players").child(currentUserId).child("answers").child(questionIndex.toString()).setValue(answerIndexes)
        }
    }

    private fun addRoomEventListener(roomCode: String) {
        if (roomListener != null) return
        roomListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val newState = snapshot.getValue(GameState::class.java)
                _gameState.value = newState
            }
            override fun onCancelled(error: DatabaseError) { /* Handle error */ }
        }
        db.child(roomCode).addValueEventListener(roomListener!!)
    }

    private fun startTimerForCurrentQuestion(roomCode: String, timeSeconds: Int = 15) {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            delay(timeSeconds * 1000L)
            nextQuestion()
        }
    }

    private fun generateRoomCode(): String = (100000..999999).random().toString()

    override fun onCleared() {
        super.onCleared()
        roomListener?.let { listener -> _roomCode.value?.let { code -> db.child(code).removeEventListener(listener) } }
        timerJob?.cancel()
    }
}
