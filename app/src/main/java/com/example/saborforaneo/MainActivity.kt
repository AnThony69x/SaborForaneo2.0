package com.example.saborforaneo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.example.saborforaneo.permissions.rememberLocationPermissionState
import com.example.saborforaneo.permissions.rememberNotificationPermissionState
import com.example.saborforaneo.ui.navigation.GrafoNavegacion
import com.example.saborforaneo.ui.screens.profile.PerfilViewModel
import com.example.saborforaneo.ui.theme.SaborForaneoTheme
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Leer el Intent para ver si viene de una notificación
        val navigateTo = intent.getStringExtra("NAVIGATE_TO")

        setContent {
            val perfilViewModel: PerfilViewModel = viewModel()
            val estadoPerfil by perfilViewModel.estado.collectAsState()

            // Estado para controlar cuándo pedir cada permiso
            var pedirNotificaciones by remember { mutableStateOf(false) }
            var pedirUbicacion by remember { mutableStateOf(false) }

            // Permiso de Notificaciones
            val notificationPermission = rememberNotificationPermissionState(
                onPermissionGranted = {
                    perfilViewModel.cambiarNotificacionesActivas(true)
                    pedirUbicacion = true
                },
                onPermissionDenied = {
                    perfilViewModel.cambiarNotificacionesActivas(false)
                    pedirUbicacion = true
                }
            )

            // Permiso de Ubicación
            val locationPermission = rememberLocationPermissionState(
                onPermissionGranted = {
                    perfilViewModel.cambiarUbicacionActiva(true)
                },
                onPermissionDenied = {
                    perfilViewModel.cambiarUbicacionActiva(false)
                }
            )

            // Pedir permisos en secuencia
            LaunchedEffect(Unit) {
                delay(1000)
                pedirNotificaciones = true
            }

            LaunchedEffect(pedirNotificaciones) {
                if (pedirNotificaciones) {
                    notificationPermission.requestPermission()
                    pedirNotificaciones = false
                }
            }

            LaunchedEffect(pedirUbicacion) {
                if (pedirUbicacion) {
                    delay(800)
                    locationPermission.requestPermission()
                    pedirUbicacion = false
                }
            }

            SaborForaneoTheme(
                temaOscuro = estadoPerfil.temaOscuro,
                colorPrimario = estadoPerfil.temaColorSeleccionado.colorPrimario
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val controladorNav = rememberNavController()

                    // Navegar automáticamente al Home si viene de notificación
                    LaunchedEffect(navigateTo) {
                        if (navigateTo == "home") {
                            delay(500) // Pequeño delay para que se inicialice la navegación
                            controladorNav.navigate("home") {
                                popUpTo("login") { inclusive = false }
                            }
                        }
                    }

                    GrafoNavegacion(
                        controladorNav = controladorNav,
                        perfilViewModel = perfilViewModel
                    )
                }
            }
        }
    }
}