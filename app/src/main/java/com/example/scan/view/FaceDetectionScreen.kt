package com.example.scan.view

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.ViewGroup
import androidx.annotation.OptIn
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.example.scan.R
import com.example.scan.model.retrofit.sendImageToApi
import com.google.common.util.concurrent.ListenableFuture
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import java.io.ByteArrayOutputStream
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
private var isProcessing = false

@Composable
fun FaceScannerScreen() {
    val context = LocalContext.current
    val lifecycleOwner = context as LifecycleOwner
    //TODO----
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    val detectedFaces = remember { mutableStateOf<List<Face>>(emptyList()) }
    val imageWidth = remember { mutableStateOf(1) }
    val imageHeight = remember { mutableStateOf(1) }
    Box(modifier = Modifier.fillMaxSize()) {
        CameraPreview(
            lifecycleOwner = lifecycleOwner,
            cameraExecutor = cameraExecutor,
            onFacesDetected = { faces, width, height ->
                detectedFaces.value = faces
                imageWidth.value = width
                imageHeight.value = height
            }

        )

        BasicText("Rostros detectados: ${detectedFaces.value.size}")
    }
}
@Composable
fun CameraPreview(
    lifecycleOwner: LifecycleOwner,
    cameraExecutor: ExecutorService,
    onFacesDetected: (List<Face>, Int, Int) -> Unit,
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
                            processImage(image, onFacesDetected, context)
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
@OptIn(ExperimentalGetImage::class)
private fun processImage(
    imageProxy: ImageProxy,
    onFacesDetected: (List<Face>, Int, Int) -> Unit,
    context: Context
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
    isProcessing=true
    detector.process(image)
        .addOnSuccessListener { faces ->
            if (faces.isNotEmpty()) {

                val capturedBitmap =imageProxy.toBitmap()
                if (capturedBitmap == null || capturedBitmap.width == 0 || capturedBitmap.height == 0) {
                    Log.e("FaceScanner", "La imagen capturada es nula o inválida")
                    isProcessing = false
                    return@addOnSuccessListener
                }
                Log.d("FaceScanner", "Imagen capturada: ${capturedBitmap.width}x${capturedBitmap.height}")

                val rotatedBitmap = ensureCorrectOrientation(capturedBitmap, rotationDegrees)
                Log.d("FaceScanner", "Rotación aplicada: ${imageProxy.imageInfo.rotationDegrees}°")

                val resizedBitmap = resizeBitmapPreservingAspectRatio(rotatedBitmap, 640)
                val compressedBitmap = compressBitmap(resizedBitmap, quality = 95)
                val rostropreuba=BitmapFactory.decodeResource(context.resources, R.drawable.rostro1)
                Log.d("FaceScanner", "ESCANEANDO .....")
                Handler(Looper.getMainLooper()).post {
                    sendImageToApi(rostropreuba, compressedBitmap, context) { success ->
                        // Solo reanuda el escaneo si la API responde
                        if (success) {
                            Log.d("FaceScanner", "✅ Rostro verificado")
                            isProcessing = false

                        }
                        else{
                            isProcessing = false
                        }
                    }
                }

            }else{
                isProcessing = false
            }
        }
        .addOnFailureListener { e ->
            isProcessing = false
            Log.e("FaceScanner", "Error en detección de rostros", e)
        }
        .addOnCompleteListener {
            imageProxy.close()
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