package fr.isen.valentin.wall_e

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class LoginViewModel : ViewModel() {

    // --- Services Firebase ---
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    // --- États de l'UI ---
    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email.asStateFlow()

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password.asStateFlow()

    private val _confirmPassword = MutableStateFlow("")
    val confirmPassword: StateFlow<String> = _confirmPassword.asStateFlow()

    // NOUVEAU : Un état pour afficher les erreurs ou les messages de statut
    private val _message = MutableStateFlow("")
    val message: StateFlow<String> = _message.asStateFlow()

    // --- Actions de saisie ---
    fun onEmailChange(newEmail: String) { _email.value = newEmail }
    fun onPasswordChange(newPassword: String) { _password.value = newPassword }
    fun onConfirmPasswordChange(newPassword: String) { _confirmPassword.value = newPassword }

    // --- Logique d'authentification ---

    fun login(onSuccess: () -> Unit) {
        val currentEmail = _email.value
        val currentPassword = _password.value

        if (currentEmail.isBlank() || currentPassword.isBlank()) {
            _message.value = "Veuillez remplir tous les champs."
            return
        }

        _message.value = "Connexion en cours..."

        auth.signInWithEmailAndPassword(currentEmail, currentPassword)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _message.value = "Connexion réussie !"
                    onSuccess()
                } else {
                    _message.value = "❌ Erreur : ${task.exception?.message}"
                }
            }
    }

    fun signUp(onSuccess: () -> Unit) {
        val currentEmail = _email.value
        val currentPassword = _password.value
        val currentConfirm = _confirmPassword.value

        if (currentEmail.isBlank() || currentPassword.isBlank() || currentConfirm.isBlank()) {
            _message.value = "Veuillez remplir tous les champs."
            return
        }

        if (currentPassword != currentConfirm) {
            _message.value = "Les mots de passe ne correspondent pas."
            return
        }

        if (currentPassword.length < 6) {
            _message.value = "Le mot de passe doit faire au moins 6 caractères."
            return
        }

        _message.value = "Création du compte..."

        auth.createUserWithEmailAndPassword(currentEmail, currentPassword)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userId = task.result?.user?.uid
                    if (userId != null) {
                        // Création du profil dans Firestore (comme l'a fait ton collègue)
                        val userProfile = hashMapOf("id" to userId, "email" to currentEmail, "role" to "grimpeur")
                        db.collection("users").document(userId).set(userProfile)
                            .addOnSuccessListener {
                                _message.value = "Compte créé avec succès !"
                                onSuccess()
                            }
                            .addOnFailureListener { e ->
                                _message.value = "Compte créé, mais erreur de profil : ${e.message}"
                            }
                    }
                } else {
                    _message.value = "❌ Erreur : ${task.exception?.message}"
                }
            }
    }
}