package com.example.saborforaneo.ui.screens.profile.componentes

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.saborforaneo.ui.screens.profile.TemaColor
import com.example.saborforaneo.ui.screens.profile.ModoTema

@Composable
fun SeccionPreferencias(
    modoTema: ModoTema,
    temaColor: TemaColor,
    alCambiarModoTema: (ModoTema) -> Unit,
    alAbrirSelectorTema: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = "Preferencias",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            onClick = { /* Abrir diálogo de selección de modo */ }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.DarkMode,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Modo de Tema",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = modoTema.nombreMostrar,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Selector de modo de tema
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    ModoTema.entries.forEach { modo ->
                        FilterChip(
                            selected = modoTema == modo,
                            onClick = { alCambiarModoTema(modo) },
                            label = {
                                Text(
                                    text = when (modo) {
                                        ModoTema.AUTOMATICO -> "🔄"
                                        ModoTema.CLARO -> "☀️"
                                        ModoTema.OSCURO -> "🌙"
                                    },
                                    style = MaterialTheme.typography.bodySmall
                                )
                            },
                            modifier = Modifier.size(48.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            onClick = alAbrirSelectorTema
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Palette,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Tema de Color",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = temaColor.nombreMostrar,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}