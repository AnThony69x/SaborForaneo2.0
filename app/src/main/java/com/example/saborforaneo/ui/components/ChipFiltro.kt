package com.example.saborforaneo.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChipFiltro(
    texto: String,
    seleccionado: Boolean,
    alSeleccionar: () -> Unit,
    modifier: Modifier = Modifier,
    icono: @Composable (() -> Unit)? = null
) {
    FilterChip(
        selected = seleccionado,
        onClick = alSeleccionar,
        label = { Text(text = texto) },
        leadingIcon = icono,
        modifier = modifier.padding(horizontal = 4.dp)
    )
}