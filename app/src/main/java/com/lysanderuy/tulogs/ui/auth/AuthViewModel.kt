package com.lysanderuy.tulogs.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.FirebaseUser
import com.lysanderuy.tulogs.data.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class AuthMode { SIGN_IN, SIGN_UP }

data class AuthUiState(
    val mode: AuthMode = AuthMode.SIGN_IN,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

sealed interface AuthSessionState {
    data object Loading : AuthSessionState
    data class SignedIn(val user: FirebaseUser) : AuthSessionState
    data object SignedOut : AuthSessionState
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    val authState: StateFlow<AuthSessionState> = authRepository.authState
        .map { user -> if (user != null) AuthSessionState.SignedIn(user) else AuthSessionState.SignedOut }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AuthSessionState.Loading
        )

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun onModeToggle() {
        _uiState.update {
            it.copy(
                mode = if (it.mode == AuthMode.SIGN_IN) AuthMode.SIGN_UP else AuthMode.SIGN_IN,
                errorMessage = null
            )
        }
    }

    fun signIn(email: String, password: String) {
        val trimmedEmail = email.trim()
        if (trimmedEmail.isEmpty() || password.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "Enter your email and password.") }
            return
        }
        viewModelScope.launch { performAuthAction { authRepository.signIn(trimmedEmail, password) } }
    }

    fun signUp(email: String, password: String) {
        val trimmedEmail = email.trim()
        if (trimmedEmail.isEmpty() || password.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "Enter your email and password.") }
            return
        }
        if (password.length < 8) {
            _uiState.update { it.copy(errorMessage = "Password must be at least 8 characters.") }
            return
        }
        viewModelScope.launch { performAuthAction { authRepository.signUp(trimmedEmail, password) } }
    }

    fun signOut() {
        authRepository.signOut()
    }

    private suspend fun performAuthAction(action: suspend () -> Unit) {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        try {
            action()
            _uiState.update { it.copy(isLoading = false) }
        } catch (e: FirebaseNetworkException) {
            _uiState.update {
                it.copy(isLoading = false, errorMessage = "No internet connection. Check your connection and try again.")
            }
        } catch (e: FirebaseAuthException) {
            _uiState.update { it.copy(isLoading = false, errorMessage = mapAuthError(e)) }
        } catch (e: FirebaseException) {
            _uiState.update { it.copy(isLoading = false, errorMessage = "Something went wrong. Please try again.") }
        }
    }

    private fun mapAuthError(exception: FirebaseAuthException): String {
        return when (exception) {
            is FirebaseAuthWeakPasswordException -> "Password must be at least 8 characters."
            is FirebaseAuthUserCollisionException -> "An account with this email already exists."
            is FirebaseAuthInvalidCredentialsException -> {
                if (exception.errorCode == "ERROR_INVALID_EMAIL") {
                    "Enter a valid email address."
                } else {
                    "Incorrect email or password."
                }
            }
            is FirebaseAuthInvalidUserException -> "Incorrect email or password."
            else -> "Something went wrong. Please try again."
        }
    }
}
