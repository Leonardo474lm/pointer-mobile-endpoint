package com.example.scan.view

import android.util.Log
import android.widget.Toast
import androidx.collection.mutableObjectListOf
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ExposedDropdownMenuBox
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.scan.R
import com.example.scan.model.data.entiti.Empresa
import com.example.scan.model.retrofit.fetchListEmpresa
import com.example.scan.model.retrofit.fetchListSucursal
import com.example.scan.view.access.FingertipsDeteccion


@Composable
fun LoginScreen(navController: NavController) {
    val context = LocalContext.current;
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val empresa by remember { mutableStateOf("") }
    var sucursal by remember { mutableStateOf("") }
    val activity = context as FragmentActivity
    val executor = ContextCompat.getMainExecutor(activity)
    var opciones by remember { mutableStateOf((emptyList<String> ())) }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {


            Image(
                painter = painterResource(R.drawable.delcrosalogo),
                contentDescription = ""
            )

            Spacer(modifier = Modifier.height(16.dp))
            //todo:registro de empresas
            Button(onClick = {
                fetchListEmpresa() {empresas->
                    Log.d("Retrofit", "${empresas}")
                }
            }) {

                Text("pointer de prueba")
            }
            Button(onClick = {
                fetchListSucursal("01") { sucursales ->
                    Log.d("Retrofit", "${sucursales}")

                }
            }) { Text("pointer") }

            DropdownButton("Empresa",opciones);
            Spacer(modifier = Modifier.height(16.dp))

            DropdownButton("Sucursal",opciones);
            Spacer(modifier = Modifier.height(16.dp))

            //todo:registro
            Column(
                modifier = Modifier
                    .width(IntrinsicSize.Max),
                verticalArrangement = Arrangement.Bottom
            ) {
                Text(text = ". . . . . . . .Personal: . . . . . . . .")
            }
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Usuario") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Contraseña") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (username.isEmpty() || password.isEmpty()) {
                    } else {
                        Toast.makeText(context, "Inicio de sesión exitoso", Toast.LENGTH_SHORT)
                            .show()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Ingresar")
            }

            //TODO:Registro con escaneo
            Spacer(modifier = Modifier.height(32.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = { /* TODO: Implementar login con cámara */
                        navController.navigate("Camera")
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(55.dp),
                    shape = MaterialTheme.shapes.medium,
                    colors = ButtonDefaults.buttonColors(
                        contentColor = Color.Black
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Face,
                        contentDescription = "Cámara",
                        modifier = Modifier.size(40.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Cámara")
                }

                Spacer(modifier = Modifier.width(16.dp))

                Button(
                    onClick = { /* TODO: Implementar login con huella */
                        FingertipsDeteccion(context, activity, executor) {
                            navController.navigate("Home")
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(55.dp),
                    shape = MaterialTheme.shapes.medium,
                    colors = ButtonDefaults.buttonColors(
                        contentColor = Color.Black
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Huella",
                        modifier = Modifier.size(40.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Huella")
                }
            }

        }
    }
}


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun DropdownButton(name: String, opciones: List<String>) {
    var expanded by remember { mutableStateOf(false) }
    var seleccion by remember { mutableStateOf("Seleccionar opción") }
    var empresas by remember { mutableStateOf(emptyList<String>()) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = {
            fetchListEmpresa(){empresa->empresas=empresa?.map{it.glosa_empresa}?: emptyList()}
            expanded = !expanded }
    ) {
        OutlinedTextField(
            value = seleccion,
            onValueChange = { },
            label = { Text(name) },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .clickable {   }, // 🔹 Hace que el campo sea interactivo
            readOnly = true
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            empresas.forEach { opcion ->
                DropdownMenuItem(
                    onClick = {
                        seleccion = opcion
                        expanded = false
                    },
                    content = { Text(opcion) }
                )
            }
        }

    }

}
