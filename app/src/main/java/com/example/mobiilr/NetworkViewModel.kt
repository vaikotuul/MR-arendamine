package com.example.mobiilr

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class NetworkUiState(
    val loading: Boolean = false,
    val users: List<User> = emptyList(),
    val error: String? = null,
    val token: String? = null,
    val loginError: String? = null
)

// Võrgulehe olek. Päringud käivad viewModelScope'is – ekraani pööramisel need ei katke ega korrata.
class NetworkViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(NetworkUiState())
    val uiState: StateFlow<NetworkUiState> = _uiState.asStateFlow()

    // GET: too kasutajad serverist
    fun loadUsers(online: Boolean) {
        if (!online) {
            _uiState.update { it.copy(error = "No internet connection") }
            return
        }
        _uiState.update { it.copy(loading = true, error = null) }
        viewModelScope.launch {
            try {
                val users = fetchUsers()
                _uiState.update { it.copy(loading = false, users = users) }
            } catch (e: Exception) {
                _uiState.update { it.copy(loading = false, error = errorMessage(e)) }
            }
        }
    }

    // POST: saada sisselogimisandmed serverile
    fun login(email: String, password: String, online: Boolean) {
        if (!online) {
            _uiState.update { it.copy(loginError = "No internet connection") }
            return
        }
        _uiState.update { it.copy(loginError = null) }
        viewModelScope.launch {
            try {
                val token = postLogin(email, password)
                _uiState.update { it.copy(token = token) }
            } catch (e: Exception) {
                _uiState.update { it.copy(token = null, loginError = errorMessage(e)) }
            }
        }
    }
}
