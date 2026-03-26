package fr.isen.valentin.wall_e.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import fr.isen.valentin.wall_e.ble.BleManager
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class MainViewModel(private val bleManager: BleManager) : ViewModel() {
    private val auth = FirebaseAuth.getInstance()

    private val _uiState = MutableStateFlow<UiState>(UiState.Login)
    val uiState = _uiState.asStateFlow()

    private val _timerText = MutableStateFlow("00:00:00")
    val timerText = _timerText.asStateFlow()

    private val _authError = MutableStateFlow<String?>(null)
    val authError = _authError.asStateFlow()

    private var timerJob: Job? = null
    var scannedRoute: List<Int> = emptyList()

    init {
        bleManager.onConnectionStateChange = { state ->
            if (state == 2) {
                _uiState.value = UiState.RouteReady
            }
        }
        bleManager.onWriteSuccess = { startTimer() }
        
        // Check if user is already logged in
        if (auth.currentUser != null) {
            _uiState.value = UiState.Scanner
        }
    }

    fun signUp(email: String, pass: String) {
        auth.createUserWithEmailAndPassword(email, pass)
            .addOnSuccessListener { _uiState.value = UiState.Scanner }
            .addOnFailureListener { _authError.value = it.localizedMessage }
    }

    fun signIn(email: String, pass: String) {
        auth.signInWithEmailAndPassword(email, pass)
            .addOnSuccessListener { _uiState.value = UiState.Scanner }
            .addOnFailureListener { _authError.value = it.localizedMessage }
    }

    fun onLogin() {
        _uiState.value = UiState.Scanner
    }

    fun onQrScanned(data: String) {
        scannedRoute = data.filter { it.isDigit() || it == ',' }
            .split(",")
            .mapNotNull { it.trim().toIntOrNull() }
        
        _uiState.value = UiState.ConnectingBle
        bleManager.startScanning()
    }

    fun launchRoute() {
        bleManager.sendRoute(scannedRoute)
    }

    private fun startTimer() {
        timerJob?.cancel()
        val startTime = System.currentTimeMillis()
        timerJob = viewModelScope.launch(Dispatchers.Main) {
            while (isActive) {
                val elapsed = System.currentTimeMillis() - startTime
                _timerText.value = formatTime(elapsed)
                delay(100)
            }
        }
    }

    private fun formatTime(ms: Long): String {
        val s = (ms / 1000) % 60
        val m = (ms / (1000 * 60)) % 60
        val h = (ms / (1000 * 60 * 60))
        return String.format("%02d:%02d:%02d", h, m, s)
    }
}

sealed class UiState {
    object Login : UiState()
    object Scanner : UiState()
    object ConnectingBle : UiState()
    object RouteReady : UiState()
}
