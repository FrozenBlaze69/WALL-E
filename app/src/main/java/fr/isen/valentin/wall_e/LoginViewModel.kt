package fr.isen.valentin.wall_e

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class LoginViewModel : ViewModel() {

    // États pour les champs de texte
    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email.asStateFlow()

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password.asStateFlow()

    private val _confirmPassword = MutableStateFlow("")
    val confirmPassword: StateFlow<String> = _confirmPassword.asStateFlow()

    // Méthodes appelées par l'UI lorsque l'utilisateur tape sur son clavier
    fun onEmailChange(newEmail: String) {
        _email.value = newEmail
    }

    fun onPasswordChange(newPassword: String) {
        _password.value = newPassword
    }

    fun onConfirmPasswordChange(newPassword: String) {
        _confirmPassword.value = newPassword
    }

    // Méthode appelée lors du clic sur le bouton de connexion
    fun login(onSuccess: () -> Unit) {
        // Ici, vous ferez plus tard l'appel à votre API ou Firebase
        val currentEmail = _email.value
        val currentPassword = _password.value

        if (currentEmail.isNotBlank() && currentPassword.isNotBlank()) {
            // Simulation d'une connexion réussie
            onSuccess()
        }
    }
}