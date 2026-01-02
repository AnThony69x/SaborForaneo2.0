package com.example.saborforaneo.ui.screens.profile.dialogos

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun DialogoTerminos(
    alCerrar: () -> Unit
) {
    AlertDialog(
        onDismissRequest = alCerrar,
        icon = {
            Icon(
                imageVector = Icons.Default.Description,
                contentDescription = null
            )
        },
        title = { Text("Términos y Condiciones") },
        text = {
            LazyColumn(
                modifier = Modifier.height(300.dp)
            ) {
                item {
                    Text(
                        text = "1. Aceptación de Términos",
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    Text(
                        text = "Al usar SaborForáneo, aceptas estos términos y condiciones.",
                        fontSize = 14.sp,
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "2. Uso de la Aplicación",
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    Text(
                        text = "• Puedes usar la app de forma gratuita\n• No debes compartir contenido inapropiado\n• Respeta los derechos de autor de las recetas",
                        fontSize = 14.sp,
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "3. Contenido del Usuario",
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    Text(
                        text = "Al publicar recetas, garantizas que tienes los derechos sobre el contenido compartido.",
                        fontSize = 14.sp,
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "4. Limitación de Responsabilidad",
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    Text(
                        text = "SaborForáneo no se responsabiliza por alergias alimentarias o problemas de salud derivados del uso de las recetas.",
                        fontSize = 14.sp,
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "5. Modificaciones",
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    Text(
                        text = "Nos reservamos el derecho de modificar estos términos en cualquier momento.",
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