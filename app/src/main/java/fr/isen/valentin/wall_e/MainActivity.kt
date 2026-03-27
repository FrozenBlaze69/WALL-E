package fr.isen.valentin.wall_e

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import fr.isen.valentin.wall_e.ui.theme.WALLETheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WALLETheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ClimbingAppNavigation()
                }
            }
        }
    }
}

@Composable
fun ClimbingAppNavigation() {
    val navController = rememberNavController()

    // Le NavHost contient toutes nos pages comme un livre
    NavHost(navController = navController, startDestination = "login") {

        // 1. PAGE DE CONNEXION
        composable("login") {
            LoginScreen(
                onNavigateToHome = {
                    // Quand on se connecte, on va au scanner et on empêche le retour en arrière
                    navController.navigate("scanner") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }

        // 2. PAGE DU SCANNER
        composable("scanner") {
            QrScannerScreen(
                onQrScanned = { scannedData ->
                    // Dès que la caméra lit un QR Code, on va sur la page des parcours !
                    // (Plus tard, on pourra utiliser "scannedData" pour filtrer la bonne salle)
                    navController.navigate("routes")
                }
            )
        }

        // 3. PAGE DES VOIES (Routes)
        composable("routes") {
            RoutesScreen(
                onBackClick = {
                    // Si on clique sur la flèche retour en haut à gauche, on retourne au scanner
                    navController.popBackStack()
                }
            )
        }
    }
}