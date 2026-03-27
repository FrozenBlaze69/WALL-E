package fr.isen.valentin.wall_e

import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

// 1. Notre modèle de données simple (La carte d'identité d'une voie)
data class ClimbingRoute(
    val id: String = "",
    val nom: String = "",
    val difficulte: String = ""
)

class RoutesViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()

    // L'état de notre liste de voies
    private val _routes = MutableStateFlow<List<ClimbingRoute>>(emptyList())
    val routes: StateFlow<List<ClimbingRoute>> = _routes.asStateFlow()

    // L'état de chargement (le petit rond qui tourne)
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // L'état des erreurs
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // Le bloc init s'exécute automatiquement quand on arrive sur l'écran
    init {
        fetchRoutes()
    }

    private fun fetchRoutes() {
        _isLoading.value = true
        _errorMessage.value = null

        // On va chercher dans ta collection "parcours"
        db.collection("parcours")
            .get()
            .addOnSuccessListener { result ->
                // On transforme les documents Firebase en notre objet ClimbingRoute
                val fetchedRoutes = result.documents.mapNotNull { doc ->
                    val nom = doc.getString("nom") ?: "Sans nom"
                    val difficulte = doc.getString("difficulté") ?: "Inconnue" // Le fameux accent !

                    ClimbingRoute(id = doc.id, nom = nom, difficulte = difficulte)
                }

                _routes.value = fetchedRoutes
                _isLoading.value = false
            }
            .addOnFailureListener { exception ->
                _errorMessage.value = "Erreur de connexion à la base : ${exception.message}"
                _isLoading.value = false
            }
    }
}