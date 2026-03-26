package fr.isen.valentin.wall_e

import android.Manifest
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.firebase.firestore.FirebaseFirestore
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors

@Composable
fun QrScannerWrapper() {
    var scannedRoomByQr by remember { mutableStateOf<String?>(null) }

    if (scannedRoomByQr == null) {
        // Écran de Scan
        QrScannerView(onQrScanned = { data ->
            // Si le QR contient "salle numéro 1", on stocke "1"
            if (data.contains("salle numéro 1", ignoreCase = true)) {
                scannedRoomByQr = "1"
            } else {
                scannedRoomByQr = data // Ou autre logique selon tes QR
            }
        })
    } else {
        // Écran qui affiche les pistes de la salle
        RoomTracksScreen(roomNumber = scannedRoomByQr!!, onBack = { scannedRoomByQr = null })
    }
}

@Composable
fun QrScannerView(onQrScanned: (String) -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (!isGranted) Toast.makeText(context, "Permission caméra requise", Toast.LENGTH_SHORT).show()
    }

    LaunchedEffect(Unit) { cameraPermissionLauncher.launch(Manifest.permission.CAMERA) }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { ctx ->
                val previewView = PreviewView(ctx)
                val executor = Executors.newSingleThreadExecutor()
                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()
                    val preview = Preview.Builder().build().also { it.surfaceProvider = previewView.surfaceProvider }
                    val scanner = BarcodeScanning.getClient()
                    val imageAnalysis = ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()

                    imageAnalysis.setAnalyzer(executor) { imageProxy ->
                        val mediaImage = imageProxy.image
                        if (mediaImage != null) {
                            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
                            scanner.process(image).addOnSuccessListener { barcodes ->
                                barcodes.firstOrNull()?.rawValue?.let { onQrScanned(it) }
                            }.addOnCompleteListener { imageProxy.close() }
                        }
                    }
                    cameraProvider.bindToLifecycle(lifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, preview, imageAnalysis)
                }, ContextCompat.getMainExecutor(ctx))
                previewView
            },
            modifier = Modifier.fillMaxSize()
        )
        Text("Visez le QR Code de la salle", modifier = Modifier.align(Alignment.BottomCenter).padding(50.dp), color = Color.White)
    }
}

@Composable
fun RoomTracksScreen(roomNumber: String, onBack: () -> Unit) {
    val db = FirebaseFirestore.getInstance()
    var tracks by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    // On va chercher les pistes dans Firestore qui correspondent à la salle
    LaunchedEffect(roomNumber) {
        db.collection("climbingRoutes")
            .whereEqualTo("salle", roomNumber) // Filtre par numéro de salle
            .get()
            .addOnSuccessListener { result ->
                tracks = result.documents.map { it.data ?: emptyMap() }
                isLoading = false
            }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Button(onClick = onBack) { Text("<") }
            Spacer(modifier = Modifier.width(16.dp))
            Text("Pistes - Salle $roomNumber", style = MaterialTheme.typography.headlineMedium)
        }

        Spacer(modifier = Modifier.height(20.dp))

        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        } else if (tracks.isEmpty()) {
            Text("Aucune piste trouvée pour cette salle.")
        } else {
            LazyColumn {
                items(tracks) { track ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp).clickable {
                            /* Prochaine étape : Connexion BLE au mur */
                        }
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(text = "Nom : ${track["nom"] ?: "Inconnu"}", style = MaterialTheme.typography.titleLarge)
                            Text(text = "Difficulté : ${track["difficulte"] ?: "N/A"}")
                        }
                    }
                }
            }
        }
    }
}