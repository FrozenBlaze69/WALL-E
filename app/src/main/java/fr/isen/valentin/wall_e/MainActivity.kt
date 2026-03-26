package fr.isen.valentin.wall_e

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import fr.isen.valentin.wall_e.ui.theme.WALLETheme // Vérifie que ton thème s'appelle bien comme ça

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // 1. On crée un état pour savoir quel écran afficher
            // "auth" = écran de connexion, "scanner" = le scanner QR
            var currentScreen by remember { mutableStateOf("auth") }

            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // 2. On choisit quoi afficher selon l'état
                    if (currentScreen == "auth") {
                        // On passe une fonction "onSuccess" à l'écran d'auth
                        SimpleAuthTestScreen(onSuccess = {
                            currentScreen = "scanner"
                        })
                    } else {
                        // 3. On appelle le wrapper du scanner (celui dans ton autre fichier)
                        QrScannerWrapper()
                    }
                }
            }
        }
    }
}

@Composable
fun SimpleAuthTestScreen(onSuccess: () -> Unit) { // Ajout du paramètre onSuccess
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var resultMessage by remember { mutableStateOf("En attente d'une action...") }

    val auth = com.google.firebase.auth.FirebaseAuth.getInstance()
    val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
    ) {
        Text("Wall-E Login", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("Mot de passe") },
            visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth())

        Spacer(modifier = Modifier.height(24.dp))

        // BOUTON INSCRIPTION
        Button(
            onClick = {
                if (email.isNotEmpty() && password.isNotEmpty()) {
                    auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val userId = task.result?.user?.uid
                            val userProfile = hashMapOf("id" to userId, "email" to email, "role" to "grimpeur")
                            db.collection("users").document(userId!!).set(userProfile)
                                .addOnSuccessListener { onSuccess() } // <--- ON PASSE À LA SUITE
                        } else {
                            resultMessage = "❌ Erreur : ${task.exception?.message}"
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) { Text("Créer un compte") }

        Spacer(modifier = Modifier.height(8.dp))

        // BOUTON CONNEXION
        Button(
            onClick = {
                if (email.isNotEmpty() && password.isNotEmpty()) {
                    auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            onSuccess() // <--- ON PASSE À LA SUITE
                        } else {
                            resultMessage = "❌ Erreur : ${task.exception?.message}"
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) { Text("Se connecter") }

        Spacer(modifier = Modifier.height(24.dp))
        Text(text = resultMessage)
    }
}