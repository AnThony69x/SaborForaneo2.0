package com.example.saborforaneo.ui.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.saborforaneo.ui.screens.splash.PantallaSplash
import com.example.saborforaneo.ui.screens.onboarding.PantallaOnboarding
import com.example.saborforaneo.ui.screens.auth.PantallaLogin
import com.example.saborforaneo.ui.screens.auth.PantallaRegistro
import com.example.saborforaneo.ui.screens.auth.PantallaRecuperarContrasena
import com.example.saborforaneo.ui.screens.home.PantallaInicio
import com.example.saborforaneo.ui.screens.search.PantallaBusqueda
import com.example.saborforaneo.ui.screens.detail.PantallaDetalleReceta
import com.example.saborforaneo.ui.screens.favorites.PantallaFavoritos
import com.example.saborforaneo.ui.screens.profile.PantallaPerfil
import com.example.saborforaneo.ui.screens.profile.PerfilViewModel
import com.example.saborforaneo.ui.screens.auth.PantallaTerminosCondiciones
import com.example.saborforaneo.ui.screens.admin.PantallaAdmin
import com.example.saborforaneo.ui.screens.admin.PantallaGestionRecetas
import com.example.saborforaneo.viewmodel.AuthViewModel
import com.example.saborforaneo.viewmodel.RecetaAdminViewModel
import com.example.saborforaneo.viewmodel.HomeViewModel

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun GrafoNavegacion(
    controladorNav: NavHostController,
    pantallaInicio: String = Rutas.Splash.ruta,
    perfilViewModel: PerfilViewModel
) {
    val context = LocalContext.current
    
    // Crear instancias compartidas de ViewModels para todas las pantallas
    val authViewModel: AuthViewModel = viewModel()
    val homeViewModel = remember { HomeViewModel(context) }
    
    NavHost(
        navController = controladorNav,
        startDestination = pantallaInicio,
        enterTransition = {
            fadeIn(animationSpec = tween(300)) +
                    slideInHorizontally(
                        initialOffsetX = { it },
                        animationSpec = tween(300)
                    )
        },
        exitTransition = {
            fadeOut(animationSpec = tween(300)) +
                    slideOutHorizontally(
                        targetOffsetX = { -it / 3 },
                        animationSpec = tween(300)
                    )
        },
        popEnterTransition = {
            fadeIn(animationSpec = tween(300)) +
                    slideInHorizontally(
                        initialOffsetX = { -it / 3 },
                        animationSpec = tween(300)
                    )
        },
        popExitTransition = {
            fadeOut(animationSpec = tween(300)) +
                    slideOutHorizontally(
                        targetOffsetX = { it },
                        animationSpec = tween(300)
                    )
        }
    ) {

        composable(
            route = Rutas.Splash.ruta,
            enterTransition = { fadeIn(animationSpec = tween(300)) },
            exitTransition = { fadeOut(animationSpec = tween(300)) }
        ) {
            PantallaSplash(
                navegarAOnboarding = {
                    controladorNav.navigate(Rutas.Onboarding.ruta) {
                        popUpTo(Rutas.Splash.ruta) { inclusive = true }
                    }
                },
                navegarALogin = {
                    controladorNav.navigate(Rutas.Login.ruta) {
                        popUpTo(Rutas.Splash.ruta) { inclusive = true }
                    }
                },
                navegarAInicio = {
                    controladorNav.navigate(Rutas.Inicio.ruta) {
                        popUpTo(Rutas.Splash.ruta) { inclusive = true }
                    }
                },
                navegarAAdmin = {
                    controladorNav.navigate(Rutas.InicioAdmin.ruta) {
                        popUpTo(Rutas.Splash.ruta) { inclusive = true }
                    }
                },
                authViewModel = authViewModel
            )
        }

        composable(
            route = Rutas.Onboarding.ruta,
            enterTransition = { fadeIn(animationSpec = tween(300)) },
            exitTransition = { fadeOut(animationSpec = tween(300)) }
        ) {
            PantallaOnboarding(
                alFinalizar = {
                    controladorNav.navigate(Rutas.Login.ruta) {
                        popUpTo(Rutas.Onboarding.ruta) { inclusive = true }
                    }
                }
            )
        }

        composable(route = Rutas.Login.ruta) {
            PantallaLogin(
                navegarARegistro = {
                    controladorNav.navigate(Rutas.Registro.ruta)
                },
                navegarAInicio = {
                    controladorNav.navigate(Rutas.Inicio.ruta) {
                        popUpTo(Rutas.Login.ruta) { inclusive = true }
                    }
                },
                navegarAAdmin = {
                    controladorNav.navigate(Rutas.Admin.ruta) {
                        popUpTo(Rutas.Login.ruta) { inclusive = true }
                    }
                },
                navegarARecuperarContrasena = {
                    controladorNav.navigate(Rutas.RecuperarContrasena.ruta)
                },
                authViewModel = authViewModel
            )
        }

        composable(route = Rutas.Registro.ruta) {
            PantallaRegistro(
                navegarAtras = {
                    controladorNav.popBackStack()
                },
                navegarAInicio = {
                    controladorNav.navigate(Rutas.Inicio.ruta) {
                        popUpTo(Rutas.Registro.ruta) { inclusive = true }
                    }
                },
                navegarAAdmin = {
                    controladorNav.navigate(Rutas.Admin.ruta) {
                        popUpTo(Rutas.Registro.ruta) { inclusive = true }
                    }
                },
                navegarATerminos = {
                    controladorNav.navigate(Rutas.TerminosCondiciones.ruta)
                },
                authViewModel = authViewModel
            )
        }

        composable(route = Rutas.RecuperarContrasena.ruta) {
            PantallaRecuperarContrasena(
                navegarAtras = {
                    controladorNav.popBackStack()
                },
                authViewModel = authViewModel
            )
        }

        composable(route = Rutas.TerminosCondiciones.ruta) {
            PantallaTerminosCondiciones(
                navegarAtras = {
                    controladorNav.popBackStack()
                }
            )
        }

        // ========== PANTALLAS ADMIN CON BARRA INFERIOR ==========
        
        composable(route = Rutas.InicioAdmin.ruta) {
            com.example.saborforaneo.ui.screens.admin.PantallaInicioAdmin(
                controladorNav = controladorNav,
                authViewModel = authViewModel
            )
        }

        composable(route = Rutas.EstadisticasAdmin.ruta) {
            com.example.saborforaneo.ui.screens.admin.PantallaEstadisticasAdmin(
                controladorNav = controladorNav
            )
        }

        composable(route = Rutas.PerfilAdmin.ruta) {
            com.example.saborforaneo.ui.screens.admin.PantallaPerfilAdmin(
                navegarALogin = {
                    controladorNav.navigate(Rutas.Login.ruta) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                controladorNav = controladorNav,
                authViewModel = authViewModel,
                perfilViewModel = perfilViewModel
            )
        }

        // ========== PANTALLAS ADMIN SIN BARRA (GESTIÓN) ==========

        // Ruta Admin antigua - redirige a InicioAdmin
        composable(route = Rutas.Admin.ruta) {
            PantallaAdmin(
                navegarALogin = {
                    controladorNav.navigate(Rutas.Login.ruta) {
                        popUpTo(Rutas.Admin.ruta) { inclusive = true }
                    }
                },
                navegarAGestionRecetas = {
                    controladorNav.navigate(Rutas.GestionRecetas.ruta)
                },
                navegarADashboard = {
                    controladorNav.navigate(Rutas.Dashboard.ruta)
                },
                navegarAGestionUsuarios = {
                    controladorNav.navigate(Rutas.GestionUsuarios.ruta)
                },
                authViewModel = authViewModel,
                perfilViewModel = perfilViewModel
            )
        }

        composable(route = Rutas.Dashboard.ruta) {
            val context = LocalContext.current
            val adminViewModel = remember {
                com.example.saborforaneo.viewmodel.AdminViewModel(context)
            }
            com.example.saborforaneo.ui.screens.admin.PantallaDashboard(
                viewModel = adminViewModel,
                onNavigateBack = {
                    controladorNav.popBackStack()
                }
            )
        }

        composable(route = Rutas.GestionUsuarios.ruta) {
            com.example.saborforaneo.ui.screens.admin.PantallaGestionUsuarios(
                controladorNav = controladorNav
            )
        }

        composable(route = Rutas.GestionRecetas.ruta) {
            val context = LocalContext.current
            val recetaAdminViewModel = remember {
                RecetaAdminViewModel(context)
            }
            PantallaGestionRecetas(
                viewModel = recetaAdminViewModel,
                userId = authViewModel.currentUser.value?.uid ?: "",
                controladorNav = controladorNav
            )
        }

        composable(route = Rutas.Inicio.ruta) {
            PantallaInicio(
                navegarADetalle = { recetaId ->
                    controladorNav.navigate(Rutas.DetalleReceta.crearRuta(recetaId))
                },
                navegarABusqueda = {
                    controladorNav.navigate(Rutas.Busqueda.ruta)
                },
                controladorNav = controladorNav,
                homeViewModel = homeViewModel
            )
        }

        composable(route = Rutas.Busqueda.ruta) {
            PantallaBusqueda(
                navegarADetalle = { recetaId ->
                    controladorNav.navigate(Rutas.DetalleReceta.crearRuta(recetaId))
                },
                navegarAtras = {
                    controladorNav.popBackStack()
                },
                controladorNav = controladorNav,
                homeViewModel = homeViewModel
            )
        }

        composable(
            route = Rutas.DetalleReceta.ruta,
            arguments = listOf(
                navArgument("recetaId") {
                    type = NavType.StringType
                }
            ),
            enterTransition = {
                fadeIn(animationSpec = tween(300)) +
                        slideInVertically(
                            initialOffsetY = { it },
                            animationSpec = tween(400)
                        )
            },
            exitTransition = {
                fadeOut(animationSpec = tween(300)) +
                        slideOutVertically(
                            targetOffsetY = { it },
                            animationSpec = tween(400)
                        )
            }
        ) { backStackEntry ->
            val recetaId = backStackEntry.arguments?.getString("recetaId") ?: ""
            PantallaDetalleReceta(
                recetaId = recetaId,
                navegarAtras = {
                    controladorNav.popBackStack()
                }
            )
        }

        composable(route = Rutas.Favoritos.ruta) {
            PantallaFavoritos(
                navegarADetalle = { recetaId ->
                    controladorNav.navigate(Rutas.DetalleReceta.crearRuta(recetaId))
                },
                controladorNav = controladorNav
            )
        }

        composable(route = Rutas.Perfil.ruta) {
            PantallaPerfil(
                navegarALogin = {
                    controladorNav.navigate(Rutas.Login.ruta) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                controladorNav = controladorNav,
                modeloVista = perfilViewModel,
                authViewModel = authViewModel
            )
        }
    }
}