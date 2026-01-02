package com.example.saborforaneo.ui.screens.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaTerminosCondiciones(
    navegarAtras: () -> Unit,
    alAceptar: (() -> Unit)? = null
) {
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Términos y Condiciones") },
                navigationIcon = {
                    IconButton(onClick = navegarAtras) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                }
            )
        },
        bottomBar = {
            if (alAceptar != null) {
                Surface(
                    shadowElevation = 8.dp,
                    tonalElevation = 3.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Button(
                            onClick = {
                                alAceptar()
                                navegarAtras()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                        ) {
                            Text(
                                text = "Aceptar Términos",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp)
                .verticalScroll(scrollState)
        ) {
            Text(
                text = "Términos y Condiciones de Uso",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Última actualización: 13 de noviembre de 2025",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )

            Spacer(modifier = Modifier.height(24.dp))

            SeccionTermino(
                titulo = "1. Aceptación de los Términos",
                contenido = """
                    Al acceder y utilizar SaborForáneo ("la Aplicación"), usted acepta estar legalmente vinculado por estos Términos y Condiciones. Si no está de acuerdo con estos términos, por favor no utilice la Aplicación.
                    
                    Estos términos aplican a todos los usuarios de la Aplicación, incluyendo visitantes, usuarios registrados y cualquier otra persona que acceda a nuestros servicios.
                """.trimIndent()
            )

            Spacer(modifier = Modifier.height(16.dp))

            SeccionTermino(
                titulo = "2. Descripción del Servicio",
                contenido = """
                    SaborForáneo es una aplicación móvil que proporciona acceso a recetas de cocina, tutoriales culinarios e información gastronómica. La Aplicación permite a los usuarios:
                    
                    • Explorar recetas de diferentes países y culturas
                    • Guardar recetas favoritas
                    • Buscar recetas por ingredientes, categorías o país
                    • Acceder a instrucciones paso a paso
                    • Compartir recetas con otros usuarios
                    
                    Nos reservamos el derecho de modificar, suspender o descontinuar cualquier aspecto del servicio en cualquier momento.
                """.trimIndent()
            )

            Spacer(modifier = Modifier.height(16.dp))

            SeccionTermino(
                titulo = "3. Registro y Cuenta de Usuario",
                contenido = """
                    Para acceder a ciertas funcionalidades, debe crear una cuenta proporcionando información precisa y completa. Usted es responsable de:
                    
                    • Mantener la confidencialidad de su contraseña
                    • Todas las actividades que ocurran bajo su cuenta
                    • Notificarnos inmediatamente sobre cualquier uso no autorizado
                    
                    Nos reservamos el derecho de suspender o terminar cuentas que violen estos términos.
                """.trimIndent()
            )

            Spacer(modifier = Modifier.height(16.dp))

            SeccionTermino(
                titulo = "4. Uso Aceptable",
                contenido = """
                    Al utilizar SaborForáneo, usted acepta NO:
                    
                    • Violar leyes o regulaciones aplicables
                    • Publicar contenido ofensivo, difamatorio o inapropiado
                    • Intentar acceder de manera no autorizada a sistemas o datos
                    • Interferir con el funcionamiento normal de la Aplicación
                    • Usar la Aplicación para fines comerciales sin autorización
                    • Copiar, reproducir o distribuir contenido sin permiso
                    • Hacerse pasar por otra persona u organización
                """.trimIndent()
            )

            Spacer(modifier = Modifier.height(16.dp))

            SeccionTermino(
                titulo = "5. Propiedad Intelectual",
                contenido = """
                    Todo el contenido de la Aplicación, incluyendo texto, gráficos, logos, iconos, imágenes, clips de audio, descargas digitales y compilaciones de datos, es propiedad de SaborForáneo o de sus proveedores de contenido y está protegido por leyes internacionales de derechos de autor.
                    
                    Las recetas y contenido proporcionado son únicamente para uso personal y no comercial. Cualquier otro uso requiere permiso expreso por escrito.
                """.trimIndent()
            )

            Spacer(modifier = Modifier.height(16.dp))

            SeccionTermino(
                titulo = "6. Contenido Generado por el Usuario",
                contenido = """
                    Al enviar o publicar contenido en la Aplicación, usted otorga a SaborForáneo una licencia mundial, no exclusiva, libre de regalías para usar, reproducir, modificar y distribuir dicho contenido.
                    
                    Usted declara y garantiza que:
                    
                    • Posee los derechos del contenido que publica
                    • El contenido no infringe derechos de terceros
                    • El contenido cumple con estos términos
                    
                    Nos reservamos el derecho de eliminar cualquier contenido que viole estos términos.
                """.trimIndent()
            )

            Spacer(modifier = Modifier.height(16.dp))

            SeccionTermino(
                titulo = "7. Limitación de Responsabilidad",
                contenido = """
                    La Aplicación se proporciona "tal cual" y "según disponibilidad" sin garantías de ningún tipo, expresas o implícitas.
                    
                    SaborForáneo NO será responsable por:
                    
                    • Errores o inexactitudes en las recetas
                    • Alergias alimentarias o reacciones adversas
                    • Daños resultantes del uso de recetas
                    • Interrupciones del servicio
                    • Pérdida de datos
                    • Daños indirectos, incidentales o consecuentes
                    
                    Las recetas son solo orientativas. Siempre verifique los ingredientes si tiene alergias o restricciones alimentarias.
                """.trimIndent()
            )

            Spacer(modifier = Modifier.height(16.dp))

            SeccionTermino(
                titulo = "8. Privacidad y Protección de Datos",
                contenido = """
                    El uso de la Aplicación está sujeto a nuestra Política de Privacidad, que describe cómo recopilamos, usamos y protegemos su información personal.
                    
                    Al usar la Aplicación, usted consiente la recopilación y uso de información de acuerdo con nuestra Política de Privacidad.
                """.trimIndent()
            )

            Spacer(modifier = Modifier.height(16.dp))

            SeccionTermino(
                titulo = "9. Modificaciones de los Términos",
                contenido = """
                    Nos reservamos el derecho de modificar estos Términos y Condiciones en cualquier momento. Los cambios entrarán en vigor inmediatamente después de su publicación en la Aplicación.
                    
                    Es su responsabilidad revisar periódicamente estos términos. El uso continuado de la Aplicación después de los cambios constituye su aceptación de los nuevos términos.
                """.trimIndent()
            )

            Spacer(modifier = Modifier.height(16.dp))

            SeccionTermino(
                titulo = "10. Terminación",
                contenido = """
                    Podemos suspender o terminar su acceso a la Aplicación inmediatamente, sin previo aviso, por cualquier motivo, incluyendo si:
                    
                    • Viola estos Términos y Condiciones
                    • Realizamos cambios en nuestros servicios
                    • Determinamos que su uso constituye un riesgo
                    
                    Usted puede terminar su cuenta en cualquier momento eliminándola desde la configuración de la Aplicación.
                """.trimIndent()
            )

            Spacer(modifier = Modifier.height(16.dp))

            SeccionTermino(
                titulo = "11. Ley Aplicable y Jurisdicción",
                contenido = """
                    Estos Términos se regirán e interpretarán de acuerdo con las leyes de Ecuador, sin consideración a sus disposiciones sobre conflictos de leyes.
                    
                    Cualquier disputa que surja en relación con estos términos estará sujeta a la jurisdicción exclusiva de los tribunales de Ecuador.
                """.trimIndent()
            )

            Spacer(modifier = Modifier.height(16.dp))
            SeccionTermino(
                titulo = "12. Contacto",
                contenido = """
                    Si tiene preguntas sobre estos Términos y Condiciones, puede contactarnos:
                    
                    • Email: soporte@saborforaneo.com
                    • Teléfono: +593 123 456 789
                    • Dirección: Quito, Ecuador
                    
                    Responderemos a su consulta en un plazo de 48 horas hábiles.
                """.trimIndent()
            )

            Spacer(modifier = Modifier.height(24.dp))

            HorizontalDivider()

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Al utilizar SaborForáneo, usted reconoce que ha leído, entendido y acepta estar sujeto a estos Términos y Condiciones.",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun SeccionTermino(
    titulo: String,
    contenido: String
) {
    Column {
        Text(
            text = titulo,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = contenido,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
            lineHeight = 20.sp
        )
    }
}