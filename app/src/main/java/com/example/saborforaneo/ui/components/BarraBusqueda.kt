package com.example.saborforaneo.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BarraBusqueda(
    consulta: String,
    alCambiarConsulta: (String) -> Unit,
    placeholder: String = "Buscar recetas...",
    modifier: Modifier = Modifier,
    habilitado: Boolean = true
) {
    OutlinedTextField(
        value = consulta,
        onValueChange = alCambiarConsulta,
        placeholder = { Text(placeholder) },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Buscar"
            )
        },
        trailingIcon = {
            if (consulta.isNotEmpty()) {
                IconButton(onClick = { alCambiarConsulta("") }) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Limpiar"
                    )
                }
            }
        },
        singleLine = true,
        shape = RoundedCornerShape(28.dp),
        modifier = modifier.fillMaxWidth(),
        enabled = habilitado,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
        )
    )
}