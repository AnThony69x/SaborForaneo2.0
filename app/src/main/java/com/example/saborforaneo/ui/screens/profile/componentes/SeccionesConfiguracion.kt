package com.example.saborforaneo.ui.screens.profile.componentes

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.saborforaneo.ui.screens.profile.TemaColor

@Composable
fun SeccionPreferencias(
    temaOscuro: Boolean,
    notificaciones: Boolean,
    temaColor: TemaColor,
    alCambiarTema: (Boolean) -> Unit,
    alCambiarNotificaciones: (Boolean) -> Unit,
    alAbrirSelectorTema: () -> Unit
) {
    Text(
        text = "Preferencias",
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    )

    ItemConfiguracion(
        icono = Icons.Default.Palette,
        titulo = "Tema de Color",
        descripcion = temaColor.nombreMostrar,
        alHacerClic = alAbrirSelectorTema
    )

    ItemConfiguracion(
        icono = Icons.Default.DarkMode,
        titulo = "Tema Oscuro",
        descripcion = if (temaOscuro) "Activado" else "Desactivado",
        contenidoExtra = {
            Switch(
                checked = temaOscuro,
                onCheckedChange = alCambiarTema
            )
        }
    )

    ItemConfiguracion(
        icono = Icons.Default.Notifications,
        titulo = "Notificaciones",
        descripcion = if (notificaciones) "Activadas" else "Desactivadas",
        contenidoExtra = {
            Switch(
                checked = notificaciones,
                onCheckedChange = alCambiarNotificaciones
            )
        }
    )
}

@Composable
fun SeccionCuenta(
    descripcionRestricciones: String,
    alAbrirRestricciones: () -> Unit,
    alCambiarContrasena: () -> Unit
) {
    Text(
        text = "Cuenta",
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    )

    ItemConfiguracion(
        icono = Icons.Default.Restaurant,
        titulo = "Restricciones Alimentarias",
        descripcion = descripcionRestricciones,
        alHacerClic = alAbrirRestricciones
    )

    ItemConfiguracion(
        icono = Icons.Default.Lock,
        titulo = "Cambiar Contraseña",
        descripcion = "Actualiza tu contraseña",
        alHacerClic = alCambiarContrasena
    )
}

@Composable
fun SeccionInformacion(
    alAbrirAcercaDe: () -> Unit,
    alAbrirTerminos: () -> Unit,
    alAbrirPrivacidad: () -> Unit
) {
    Text(
        text = "Información",
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    )

    ItemConfiguracion(
        icono = Icons.Default.Info,
        titulo = "Acerca de SaborForáneo",
        descripcion = "Versión 1.0.0",
        alHacerClic = alAbrirAcercaDe
    )

    ItemConfiguracion(
        icono = Icons.Default.Description,
        titulo = "Términos y Condiciones",
        descripcion = "Lee nuestros términos",
        alHacerClic = alAbrirTerminos
    )

    ItemConfiguracion(
        icono = Icons.Default.PrivacyTip,
        titulo = "Política de Privacidad",
        descripcion = "Cómo manejamos tus datos",
        alHacerClic = alAbrirPrivacidad
    )
}

@Composable
fun DivisorSeccion() {
    Spacer(modifier = Modifier.height(8.dp))
    HorizontalDivider()
    Spacer(modifier = Modifier.height(16.dp))
}