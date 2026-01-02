package com.example.saborforaneo.ui.screens.profile.dialogos

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun DialogoAcercaDe(
    alCerrar: () -> Unit
) {
    AlertDialog(
        onDismissRequest = alCerrar,
        icon = {
            Text(text = "🍳", fontSize = 48.sp)
        },
        title = {
            Text(
                text = "SaborForáneo",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text(
                    text = "Versión 1.0.0",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "SaborForáneo es tu compañero perfecto para descubrir recetas deliciosas de Ecuador y el mundo.",
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "✨ Cocina fácil, rico y barato",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Desarrollado por: AnThony69x",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "© 2025 SaborForáneo",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
        },
        confirmButton = {
            TextButton(onClick = alCerrar) {
                Text("Entendido")
            }
        }
    )
}