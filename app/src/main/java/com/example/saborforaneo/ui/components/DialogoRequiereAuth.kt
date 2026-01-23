package com.example.saborforaneo.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

@Composable
fun DialogoRequiereAuth(
    titulo: String = "Función exclusiva",
    mensaje: String = "Para acceder a esta función necesitas tener una cuenta.",
    emoji: String = "🔐",
    onDismiss: () -> Unit,
    onIniciarSesion: () -> Unit,
    onRegistrarse: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Emoji decorativo
                Text(
                    text = emoji,
                    fontSize = 56.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Título
                Text(
                    text = titulo,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Mensaje
                Text(
                    text = mensaje,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Botón principal - Registrarse
                Button(
                    onClick = onRegistrarse,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Crear cuenta gratis",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Botón secundario - Iniciar sesión
                OutlinedButton(
                    onClick = onIniciarSesion,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Ya tengo cuenta",
                        fontWeight = FontWeight.Medium,
                        fontSize = 16.sp
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Botón de cancelar
                TextButton(onClick = onDismiss) {
                    Text(
                        text = "Quizás más tarde",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}

/**
 * Mensajes predefinidos para diferentes funciones
 */
object MensajesAuth {
    val FAVORITOS = Triple(
        "Guarda tus favoritos",
        "Crea una cuenta para guardar tus recetas favoritas y acceder a ellas desde cualquier dispositivo.",
        "❤️"
    )

    val COMUNIDAD = Triple(
        "Únete a la comunidad",
        "Regístrate para compartir tus propias recetas, comentar y conectar con otros amantes de la cocina.",
        "👨‍🍳"
    )

    val ASISTENTE = Triple(
        "Asistente virtual",
        "Crea una cuenta para usar nuestro asistente de cocina con IA que te ayudará con tus recetas.",
        "🤖"
    )

    val PERFIL = Triple(
        "Tu perfil personal",
        "Regístrate para personalizar tu experiencia, guardar preferencias y mucho más.",
        "👤"
    )
}
