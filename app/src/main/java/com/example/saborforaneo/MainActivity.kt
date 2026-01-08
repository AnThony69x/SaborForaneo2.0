package com.example.saborforaneo

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.example.saborforaneo.notifications.NotificacionesScheduler
import com.example.saborforaneo.notifications.NotificacionesManager
import com.example.saborforaneo.permissions.rememberLocationPermissionState
import com.example.saborforaneo.permissions.rememberNotificationPermissionState
import com.example.saborforaneo.ui.navigation.GrafoNavegacion
import com.example.saborforaneo.ui.screens.profile.PerfilViewModel
import com.example.saborforaneo.ui.theme.SaborForaneoTheme
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Inicializar el sistema de notificaciones
        inicializarNotificaciones()

        // Leer el Intent para ver si viene de una notificación
        val navigateTo = intent.getStringExtra("NAVIGATE_TO")
        val tipoNotificacion = intent.getStringExtra("tipo_notificacion")

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

            // Determinar si usar tema oscuro según el modo seleccionado
            val esTemaOscuro = when (estadoPerfil.modoTema) {
                com.example.saborforaneo.ui.screens.profile.ModoTema.AUTOMATICO -> isSystemInDarkTheme()
                com.example.saborforaneo.ui.screens.profile.ModoTema.OSCURO -> true
                com.example.saborforaneo.ui.screens.profile.ModoTema.CLARO -> false
            }

            SaborForaneoTheme(
                temaOscuro = esTemaOscuro,
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
    
    /**
     * Inicializa el sistema de notificaciones de la app
     */
    private fun inicializarNotificaciones() {
        try {
            // Obtener el token de FCM
            FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val token = task.result
                    Log.d("MainActivity", "Token FCM: $token")
                    
                    // Aquí podrías guardar el token en Firestore
                    // usando NotificacionesManager si el usuario está autenticado
                } else {
                    Log.e("MainActivity", "Error al obtener token FCM", task.exception)
                }
            }
            
            // Programar notificaciones periódicas de recordatorio (cada 24 horas)
            NotificacionesScheduler.programarRecordatorios(this, intervaloHoras = 24)
            
            Log.d("MainActivity", "Sistema de notificaciones inicializado")
        } catch (e: Exception) {
            Log.e("MainActivity", "Error al inicializar notificaciones", e)
        }
    }
}