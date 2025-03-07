package com.example.scan.view

import android.R
import android.content.Context
import android.net.Uri
import android.os.Environment
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.scan.view.filtro.CameraPreview
import com.example.scan.view.filtro.Filtro
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState()
    var showSheet by remember { mutableStateOf(false) }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    fun createImageFile(context: Context): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File? = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir)
    }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (!success) {
            imageUri = null
        }
    }
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showSheet = true }) {
                Icon(imageVector = Icons.Default.KeyboardArrowUp, contentDescription = "Abrir menú")
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            Column() {
                Text("Fecha: 25-03-06", style = MaterialTheme.typography.titleMedium)
                Text("Usuario: Pana Sebastian", style = MaterialTheme.typography.titleMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    OutlinedTextField(
                        value = "", // Estado para manejar el valor
                        onValueChange = { /* TODO: Llamar a función para seleccionar OP */ },
                        label = { Text("Seleccion de OP") },
                        modifier = Modifier.weight(1f).clickable{
                            showSheet = true
                        },
                        singleLine = true,
                        readOnly = true,
                    )
                    OutlinedTextField(
                        value = "", // Estado para manejar el valor
                        onValueChange = { /* TODO: Llamar a función para seleccionar Tipo Marca */ },
                        label = { Text("Seleccion Tipo Marca") },
                        modifier = Modifier.weight(1f)
                    )
                }
                // TODO:Adjuntar Evidencia
                Text("Adjuntar Evidencia", style = MaterialTheme.typography.titleMedium)
                Box(
                    modifier = Modifier
                        .height(120.dp).size(100.dp)
                        .border(width = 2.dp, color = Color.Gray)
                        .clickable {
                            navController.navigate("captura")
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (imageUri != null) {
                        Image(
                            painter = rememberAsyncImagePainter(imageUri),
                            contentDescription = "Imagen Capturada",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Capturar Foto",
                            tint = Color.Gray
                        )
                    }
                }
                // TODO: Tabla
                Text("Tabla de datos", style = MaterialTheme.typography.titleMedium)

                Row {
                    Button(onClick = {
                        //TODO:grabar
                    }) {
                        Text("Grabar")
                    }
                    Button(onClick = {//TODO:Consultar
                    }) {
                        Text("Consultar")
                    }
                }
            }
        }
        // Contenido principal de la pantalla
        Filtro(showSheet, sheetState) { change ->
            showSheet = change
        }
    }


}


