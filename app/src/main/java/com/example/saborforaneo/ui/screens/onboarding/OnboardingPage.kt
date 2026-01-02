package com.example.saborforaneo.ui.screens.onboarding

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun OnboardingPage(
    pagina: PaginaOnboarding,
    modifier: Modifier = Modifier
) {
    val escalaEmoji = remember { Animatable(0f) }
    val escalaTexto = remember { Animatable(0f) }

    LaunchedEffect(key1 = pagina) {
        escalaEmoji.snapTo(0f)
        escalaTexto.snapTo(0f)

        escalaEmoji.animateTo(
            targetValue = 1.2f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )

        escalaEmoji.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy
            )
        )

        escalaTexto.animateTo(
            targetValue = 1f,
            animationSpec = tween(400)
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = pagina.emoji,
            fontSize = 120.sp,
            modifier = Modifier
                .padding(bottom = 32.dp)
                .scale(escalaEmoji.value)
        )

        Column(
            modifier = Modifier.scale(escalaTexto.value),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = pagina.titulo,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center,
                lineHeight = 36.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = pagina.descripcion,
                fontSize = 16.sp,
                color = Color.White.copy(alpha = 0.9f),
                textAlign = TextAlign.Center,
                lineHeight = 24.sp
            )
        }
    }
}