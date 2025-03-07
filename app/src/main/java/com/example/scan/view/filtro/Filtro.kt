package com.example.scan.view.filtro
import android.app.DatePickerDialog
import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Filtro(showSheet: Boolean, sheetState: SheetState, onChangesheet: (Boolean) -> Unit = {}) {

    val context = LocalContext.current
    var fechaInicio by remember { mutableStateOf("") }
    var fechaFin by remember { mutableStateOf("") }
    var otroFiltro by remember { mutableStateOf("") }
    if (showSheet) {

        ModalBottomSheet(
            onDismissRequest = { onChangesheet(false) },
            sheetState = sheetState
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                Text("Filtros", style = MaterialTheme.typography.titleMedium)
                Row {
                    OutlinedTextField(
                        value = fechaInicio,
                        onValueChange = {},
                        label = { Text("Fecha de inicio") },
                        readOnly = true,
                        modifier = Modifier.width(width = 150.dp),
                        trailingIcon = { DatePickerIcon(context) { fechaInicio = it } },
                    )
                    OutlinedTextField(
                        value = fechaFin,
                        onValueChange = {},
                        label = { Text("Fecha de fin") },
                        readOnly = true,
                        modifier = Modifier.width(width = 150.dp),
                        trailingIcon = { DatePickerIcon(context) { fechaFin = it } }
                    )
                }


                OutlinedTextField(
                    value = otroFiltro,
                    onValueChange = { otroFiltro = it },
                    label = { Text("Busqueda") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { /* TODO: Aplicar filtro */
                        onChangesheet(false)},
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Aplicar filtro")
                }
            }
        }

    }
}

@Composable
fun DatePickerIcon(context: Context, onDateSelected: (String) -> Unit) {
    IconButton(onClick = { showDatePicker(context, onDateSelected) }) {
        Icon(imageVector = Icons.Default.DateRange, contentDescription = "Seleccionar fecha")
    }
}
fun showDatePicker(context: Context, onDateSelected: (String) -> Unit) {
    val calendar = Calendar.getInstance()
    DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            val formattedDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                .format(Calendar.getInstance().apply {
                    set(year, month, dayOfMonth)
                }.time)
            onDateSelected(formattedDate)
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    ).show()
}