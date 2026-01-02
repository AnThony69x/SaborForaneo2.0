package com.example.saborforaneo.ui.screens.profile.dialogos

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun DialogoPrivacidad(
    alCerrar: () -> Unit
) {
    AlertDialog(
        onDismissRequest = alCerrar,
        icon = {
            Icon(
                imageVector = Icons.Default.PrivacyTip,
                contentDescription = null
            )
        },
        title = { Text("Política de Privacidad") },
        text = {
            LazyColumn(
                modifier = Modifier.height(300.dp)
            ) {
                item {
                    Text(
                        text = "Información que Recopilamos",
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    Text(
                        text = "• Nombre y correo electrónico\n• Preferencias de recetas\n• Historial de búsquedas\n• Recetas guardadas como favoritas",
                        fontSize = 14.sp,
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Cómo Usamos tu Información",
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    Text(
                        text = "• Personalizar tu experiencia\n• Mejorar nuestros servicios\n• Enviarte notificaciones relevantes\n• Proteger la seguridad de la app",
                        fontSize = 14.sp,
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Tus Derechos",
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    Text(
                        text = "• Acceder a tus datos personales\n• Corregir información incorrecta\n• Eliminar tu cuenta\n• Exportar tus datos",
                        fontSize = 14.sp,
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Seguridad",
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    Text(
                        text = "Protegemos tu información con medidas de seguridad estándar de la industria. Utilizamos encriptación SSL/TLS para todas las comunicaciones.",
                        fontSize = 14.sp,
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Contacto",
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    Text(
                        text = "Para cualquier pregunta sobre privacidad, contáctanos en: soporte@saborforaneo.com",
                        fontSize = 14.sp,
                        lineHeight = 20.sp
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = alCerrar) {
                Text("Cerrar")
            }
        }
    )
}