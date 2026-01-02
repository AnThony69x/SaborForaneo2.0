package com.example.saborforaneo.ui.screens.splash

import android.content.Context
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.saborforaneo.util.Constantes
import com.example.saborforaneo.viewmodel.AuthViewModel
import kotlinx.coroutines.delay

@Composable
fun PantallaSplash(
    navegarAOnboarding: () -> Unit,
    navegarALogin: () -> Unit,
    navegarAInicio: () -> Unit,
    navegarAAdmin: () -> Unit,
    authViewModel: AuthViewModel = viewModel()
) {
    val contexto = LocalContext.current
    val esPrimeraVez = remember { esPrimeraVezApp(contexto) }
    val currentUser by authViewModel.currentUser.collectAsState()
    val esAdmin by authViewModel.esAdmin.collectAsState()
    val haySesionActiva = currentUser != null

    val escalaLogo = remember { Animatable(0f) }
    val alphaLogo = remember { Animatable(0f) }
    val escalaTexto = remember { Animatable(0f) }
    val alphaTexto = remember { Animatable(0f) }

    val colorFondo1 = MaterialTheme.colorScheme.primary
    val colorFondo2 = MaterialTheme.colorScheme.secondary

    val transicionColor = rememberInfiniteTransition(label = "color")
    val offsetColor by transicionColor.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "offset"
    )

    LaunchedEffect(key1 = true) {
        escalaLogo.animateTo(
            targetValue = 1.2f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )

        alphaLogo.animateTo(
            targetValue = 1f,
            animationSpec = tween(800)
        )

        delay(300)

        escalaLogo.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy
            )
        )

        escalaTexto.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )

        alphaTexto.animateTo(
            targetValue = 1f,
            animationSpec = tween(600)
        )

        delay(Constantes.DURACION_SPLASH - 1100)

        alphaLogo.animateTo(
            targetValue = 0f,
            animationSpec = tween(400)
        )
        alphaTexto.animateTo(
            targetValue = 0f,
            animationSpec = tween(400)
        )

        when {
            esPrimeraVez -> navegarAOnboarding()
            haySesionActiva -> {
                // Verificar si es admin o usuario normal
                if (esAdmin) {
                    navegarAAdmin()
                } else {
                    navegarAInicio()
                }
            }
            else -> navegarALogin()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    colors = listOf(colorFondo1, colorFondo2),
                    start = androidx.compose.ui.geometry.Offset(offsetColor, offsetColor),
                    end = androidx.compose.ui.geometry.Offset(offsetColor + 1000f, offsetColor + 1000f)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .alpha(alphaLogo.value)
                .scale(escalaLogo.value)
        ) {
            Text(
                text = "🍳",
                fontSize = 120.sp,
                modifier = Modifier.padding(bottom = 24.dp)
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .alpha(alphaTexto.value)
                .scale(escalaTexto.value)
                .offset(y = 150.dp)
        ) {
            Text(
                text = "SaborForáneo",
                fontSize = 40.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Cocina fácil, rico y barato",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f)
            )
        }
    }
}

private fun esPrimeraVezApp(context: Context): Boolean {
    val sharedPreferences = context.getSharedPreferences("SaborForaneo", Context.MODE_PRIVATE)
    val esPrimeraVez = sharedPreferences.getBoolean("es_primera_vez", true)

    if (esPrimeraVez) {
        sharedPreferences.edit().putBoolean("es_primera_vez", false).apply()
    }

    return esPrimeraVez
}