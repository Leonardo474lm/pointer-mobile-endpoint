package com.example.scan.view.access

import android.Manifest
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.NavController
import com.example.scan.R
import com.example.scan.model.retrofit.sendImageToApi
import com.google.common.util.concurrent.ListenableFuture
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import kotlinx.coroutines.delay
import java.io.ByteArrayOutputStream
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

private var isProcessing = false

@Composable
fun FaceScanner(navController: NavController) {
    val context = LocalContext.current
    val cameraPermission = Manifest.permission.CAMERA
    var hasPermission by remember { mutableStateOf(false) }
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            hasPermission = isGranted
            if (!isGranted) {
                navController.navigate("Home")
                Toast.makeText(context, "Permiso de cámara denegado", Toast.LENGTH_SHORT).show()
            }
        }
    )
    LaunchedEffect(Unit) {
        permissionLauncher.launch(cameraPermission)
    }
    if (hasPermission) {
        FaceScan(navController)
    }
}

@Composable
fun FaceScan(navController: NavController) {
    val context = LocalContext.current
    val lifecycleOwner = context as LifecycleOwner
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    var isProcessing by remember { mutableStateOf(false) }
    var showMessage by remember { mutableStateOf(false) }
    Box(modifier = Modifier.fillMaxSize()) {
        CameraPreview(
            lifecycleOwner = lifecycleOwner,
            cameraExecutor = cameraExecutor,
            navController,
            setProcessing = {isProcessing=it}
        )

    }
    LaunchedEffect(isProcessing) {
        if (isProcessing) {
            delay(2000)
            showMessage = true

        }
    }
    if (isProcessing &&!showMessage) {
        ScanningAnimation(
            Modifier
                .fillMaxSize()
                .zIndex(1f) // Asegura que la animación está sobre la cámara
        )


    }
    if (showMessage) {
        AnalyzingDialog()
    }
}
@Composable
fun CameraPreview(
    lifecycleOwner: LifecycleOwner,
    cameraExecutor: ExecutorService,
    navController: NavController,
    setProcessing: (Boolean) -> Unit
) {
    val context = LocalContext.current
    val cameraProviderFuture: ListenableFuture<ProcessCameraProvider> =
        remember { ProcessCameraProvider.getInstance(context) }
    AndroidView(
        factory = { ctx ->
            val previewView = androidx.camera.view.PreviewView(ctx).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            }
            val executor = ContextCompat.getMainExecutor(ctx)
            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()
                val preview = Preview.Builder().build().also {
                    it.surfaceProvider = previewView.surfaceProvider
                }
                val imageAnalyzer = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                    .also {
                        it.setAnalyzer(cameraExecutor) { image ->
                            processImage(image, context, navController,setProcessing)
                        }
                    }
                val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA
                try {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner, cameraSelector, preview, imageAnalyzer
                    )
                } catch (e: Exception) {
                    Log.e("FaceScanner", "Error al iniciar la cámara", e)
                }
            }, executor)
            previewView
        },
        modifier = Modifier.fillMaxSize()
    )
}


//TODO: PROCESO DE LA IMAGEN CAPTURADA
@OptIn(ExperimentalGetImage::class)
private fun processImage(
    imageProxy: ImageProxy,
    context: Context,
    navController: NavController,
    setProcessing: (Boolean) -> Unit
) {
    if (isProcessing) {
        imageProxy.close()
        return
    }
    val mediaImage = imageProxy.image ?: return
    val rotationDegrees = imageProxy.imageInfo.rotationDegrees
    val image = InputImage.fromMediaImage(mediaImage, rotationDegrees)
    val options = FaceDetectorOptions.Builder()
        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
        .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
        .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
        .build()
    val detector = FaceDetection.getClient(options)
    isProcessing = true
    setProcessing(true)
    detector.process(image)
        .addOnSuccessListener { faces ->
            if (faces.isNotEmpty()) {
                val capturedBitmap =imageProxy.toBitmap()
                if (capturedBitmap == null || capturedBitmap.width == 0 || capturedBitmap.height == 0) {
                    Log.e("FaceScanner", "La imagen capturada es nula o inválida")
                    isProcessing = false
                    setProcessing(false)

                    return@addOnSuccessListener
                }
                val rotatedBitmap = ensureCorrectOrientation(capturedBitmap, rotationDegrees)
                val resizedBitmap = resizeBitmapPreservingAspectRatio(rotatedBitmap, 640)
                val compressedBitmap = compressBitmap(resizedBitmap, quality = 95)
                val rostropreuba=BitmapFactory.decodeResource(context.resources, R.drawable.rostro1)
                Log.d("FaceScanner", ". . . ESCANEANDO . . .")

                Handler(Looper.getMainLooper()).post {
                    sendImageToApi(rostropreuba, compressedBitmap, context) { success ->
                        if (success) {
                            Toast.makeText(
                                context,
                                "Rostro Verficado Correctamente",
                                Toast.LENGTH_SHORT
                            )
                                .show()
                            navController.navigate("Home")
                            Log.d("FaceScanner", "✅ Rostro verificado")

                            isProcessing = false
                            setProcessing(false)
                        }
                        else{
                            Toast.makeText(context, "Rostro no encontrado", Toast.LENGTH_SHORT)
                                .show()
                            isProcessing = false
                            setProcessing(false)
                        }
                    }
                }
            }else{
                isProcessing = false
                setProcessing(false)

            }
        }
        .addOnFailureListener { e ->
            isProcessing = false
            setProcessing(false)

            Log.e("FaceScanner", "Error en detección de rostros", e)
        }
        .addOnCompleteListener {
            imageProxy.close()
           // setProcessing(false)
        }
}
// Método para asegurar la orientación correcta
fun ensureCorrectOrientation(bitmap: Bitmap, rotationDegrees: Int): Bitmap {
    val correctRotation = when (rotationDegrees) {
        90 -> 270  // Ajuste manual si la imagen sale girada
        270 -> 90
        else -> rotationDegrees
    }
    if (correctRotation == 0) return bitmap
    val matrix = Matrix().apply {
        postRotate(rotationDegrees.toFloat())
    }
    return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
}

// Método para redimensionar la imagen manteniendo la relación de aspecto
fun resizeBitmapPreservingAspectRatio(bitmap: Bitmap, maxWidth: Int): Bitmap {
    val originalWidth = bitmap.width
    val originalHeight = bitmap.height
    val aspectRatio = originalHeight / originalWidth.toFloat()

    val newWidth = maxWidth
    val newHeight = (newWidth * aspectRatio).toInt()

    return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
}

// Método para comprimir la imagen con una calidad específica
fun compressBitmap(bitmap: Bitmap, quality: Int): Bitmap {
    val outputStream = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream) // Calidad ajustable
    val compressedBytes = outputStream.toByteArray()
    return BitmapFactory.decodeByteArray(compressedBytes, 0, compressedBytes.size)
}


@Composable
fun ScanningAnimation(modifier: Modifier = Modifier) {
    var position by remember { mutableStateOf(0f) }
    val infiniteTransition = rememberInfiniteTransition()

    val animatedPosition by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    position = animatedPosition

    Canvas(modifier = modifier
        .fillMaxSize()
        .zIndex(1f)) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        val lineY = canvasHeight * animatedPosition
        drawRect(
            color = Color(0x55000000), // Fondo semitransparente para verificar
            size = size
        )

        drawLine(
            color = Color.Red,
            start = Offset(0f, lineY),
            end = Offset(canvasWidth, lineY),
            strokeWidth = 5f
        )
    }
}
@Composable
fun AnalyzingDialog() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.7f)), // Fondo oscuro semitransparente
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .padding(20.dp)
                .background(Color.White, shape = RoundedCornerShape(16.dp))
                .padding(20.dp)
        ) {
            CircularProgressIndicator(color = Color.Blue, strokeWidth = 4.dp)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Analizando rostro...",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
        }
    }
}
