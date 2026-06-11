package com.example.quizloop.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.quizloop.core.data.UserProfileRepository
import com.example.quizloop.core.model.UserProfile
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ProfileViewModel(private val userProfileRepository: UserProfileRepository) : ViewModel() {

    // TODO: Zastąp "user123" prawdziwym ID użytkownika po implementacji autentykacji
    private val userId = "user123"

    val userProfile: StateFlow<UserProfile> = userProfileRepository.getUserProfile(userId)
        .filterNotNull() // Ensure we don't deal with nulls downstream
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserProfile(userId = userId, name = "Użytkownik") // Provide a sensible default
        )

    fun updateUserProfile(newName: String, newColorHex: String) {
        viewModelScope.launch {
            userProfileRepository.updateUserProfile(userId, newName, newColorHex)
        }
    }
}

class ProfileViewModelFactory(private val userProfileRepository: UserProfileRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ProfileViewModel(userProfileRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
