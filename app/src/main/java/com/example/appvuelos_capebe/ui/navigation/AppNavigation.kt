package com.example.appvuelos_capebe.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.appvuelos_capebe.data.database.AppDatabase
import com.example.appvuelos_capebe.ui.screen.CalendarioVuelos
import com.example.appvuelos_capebe.ui.screen.ConfirmacionVuelo
import com.example.appvuelos_capebe.ui.screen.Login
import com.example.appvuelos_capebe.ui.screen.MisVuelosScreen
import com.example.appvuelos_capebe.ui.screen.PantallaLogin
import com.example.appvuelos_capebe.ui.screen.PinScreen
import com.example.appvuelos_capebe.ui.screen.Principal
import com.example.appvuelos_capebe.ui.screen.RecursosNativos
import com.example.appvuelos_capebe.ui.screen.Register
import com.example.appvuelos_capebe.ui.screen.ReporteDiarioScreen
import com.example.appvuelos_capebe.ui.screen.TerminosCondiciones
import com.example.appvuelos_capebe.ui.viewModel.CalendarioViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object Routes {
    const val INICIO = "inicio"
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val PIN = "pin"
    const val PRINCIPAL = "principal"
    const val CALENDARIO = "calendario"
    const val TERMINOS = "terminos"
    const val CONFIRMACION = "confirmacion"
    const val RECURSOS = "recursos"
    const val MISVUELOS = "misvuelos"
    const val REPORTE = "reporte"
}

@Composable
fun AppNavigation() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val navController = rememberNavController()
    var usuarioActual by remember { mutableStateOf("Encargado de Pista CAPEBE") }
    val calendarioViewModel: CalendarioViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = Routes.INICIO
    ) {
        composable(Routes.INICIO) {
            PantallaLogin(
                onNavigateToLogin = {
                    scope.launch {
                        val ultimoUsuario = withContext(Dispatchers.IO) {
                            AppDatabase.getDatabase(context).usuarioDao().getUltimoUsuario()
                        }
                        if (ultimoUsuario != null) {
                            usuarioActual = ultimoUsuario.name
                            navController.navigate(Routes.PIN)
                        } else {
                            navController.navigate(Routes.LOGIN)
                        }
                    }
                }
            )
        }

        composable(Routes.PIN) {
            PinScreen(
                onPinSuccess = { nombreGuardado ->
                    usuarioActual = nombreGuardado
                    navController.navigate(Routes.PRINCIPAL) {
                        popUpTo(Routes.PIN) { inclusive = true }
                    }
                },
                onCambiarUsuario = {
                    usuarioActual = "Encargado de Pista CAPEBE"
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.PIN) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.LOGIN) {
            Login(
                onNavigateToRegister = {
                    navController.navigate(Routes.REGISTER)
                },
                onNavigateBackToHome = {
                    navController.navigate(Routes.INICIO) {
                        popUpTo(Routes.INICIO) { inclusive = true }
                    }
                },
                onLoginSuccess = { nombreGuardado ->
                    usuarioActual = nombreGuardado
                    navController.navigate(Routes.PRINCIPAL) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.REGISTER) {
            Register(
                onNavigateToLogin = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onRegisterSuccessWithPin = { nombreRegistrado ->
                    usuarioActual = nombreRegistrado
                    navController.navigate(Routes.PIN) {
                        popUpTo(Routes.INICIO) { inclusive = false }
                    }
                }
            )
        }

        composable(Routes.PRINCIPAL) {
            Principal(
                nombreSocio = usuarioActual,
                onNavigateToPlanesVuelo = {
                    navController.navigate(Routes.CALENDARIO)
                },
                onNavigateToMisVuelos = {
                    navController.navigate(Routes.MISVUELOS)
                },
                onNavigateToReporte = {
                    navController.navigate(Routes.REPORTE)
                },
                onNavigateToRecursos = {
                    navController.navigate(Routes.RECURSOS)
                },
                onLogout = {
                    navController.navigate(Routes.PIN) {
                        popUpTo(Routes.PRINCIPAL) { inclusive = true }
                    }
                },
                onCerrarSesionCompleta = {
                    usuarioActual = "Encargado de Pista CAPEBE"
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.PRINCIPAL) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.CALENDARIO) {
            CalendarioVuelos(
                viewModel = calendarioViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToConfirmacion = {
                    navController.navigate(Routes.TERMINOS)
                }
            )
        }

        composable(Routes.TERMINOS) {
            TerminosCondiciones(
                viewModel = calendarioViewModel,
                onAccept = {
                    navController.navigate(Routes.CONFIRMACION)
                },
                onDecline = {
                    navController.popBackStack()
                }
            )
        }

        composable(Routes.MISVUELOS) {
            MisVuelosScreen(
                viewModel = calendarioViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Routes.CONFIRMACION) {
            ConfirmacionVuelo(
                nombreSocio = usuarioActual,
                viewModel = calendarioViewModel,
                onNavigateHome = {
                    navController.navigate(Routes.PRINCIPAL) {
                        popUpTo(Routes.PRINCIPAL) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.RECURSOS) {
            RecursosNativos(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Routes.REPORTE) {
            ReporteDiarioScreen(
                viewModel = calendarioViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
