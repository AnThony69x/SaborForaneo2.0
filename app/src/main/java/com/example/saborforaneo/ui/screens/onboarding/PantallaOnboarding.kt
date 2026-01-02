package com.example.saborforaneo.ui.screens.onboarding

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import kotlinx.coroutines.launch

@OptIn(ExperimentalPagerApi::class)
@Composable
fun PantallaOnboarding(
    alFinalizar: () -> Unit
) {
    val paginas = OnboardingData.paginas
    val pagerState = rememberPagerState()
    val alcance = rememberCoroutineScope()

    val paginaActual = pagerState.currentPage
    val esUltimaPagina = paginaActual == paginas.size - 1

    val colorFondo by animateColorAsState(
        targetValue = Color(paginas[paginaActual].colorFondo),
        animationSpec = tween(600),
        label = "colorFondo"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorFondo)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.TopEnd
            ) {
                if (!esUltimaPagina) {
                    TextButton(
                        onClick = alFinalizar,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = Color.White
                        )
                    ) {
                        Text(
                            text = "Saltar",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            HorizontalPager(
                count = paginas.size,
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) { pagina ->
                OnboardingPage(pagina = paginas[pagina])
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 24.dp)
                ) {
                    repeat(paginas.size) { index ->
                        val ancho by animateDpAsState(
                            targetValue = if (paginaActual == index) 32.dp else 12.dp,
                            animationSpec = tween(300),
                            label = "ancho"
                        )

                        Box(
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .height(12.dp)
                                .width(ancho)
                                .clip(CircleShape)
                                .background(
                                    if (paginaActual == index)
                                        Color.White
                                    else
                                        Color.White.copy(alpha = 0.5f)
                                )
                        )
                    }
                }

                Button(
                    onClick = {
                        if (esUltimaPagina) {
                            alFinalizar()
                        } else {
                            alcance.launch {
                                pagerState.animateScrollToPage(paginaActual + 1)
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color(paginas[paginaActual].colorFondo)
                    ),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 8.dp
                    )
                ) {
                    Text(
                        text = if (esUltimaPagina) "Comenzar 🚀" else "Siguiente",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}