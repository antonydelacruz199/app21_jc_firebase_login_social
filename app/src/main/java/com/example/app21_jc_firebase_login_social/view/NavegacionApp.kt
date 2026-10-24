package com.example.app21_jc_firebase_login_social.view

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.app21_jc_firebase_login_social.viewmodel.AuthViewModel

// Decide qué pantalla mostrar según el estado de la sesión
@Composable
fun NavegacionApp(viewModel: AuthViewModel = viewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val usuario = state.usuario

    when {
        state.verificandoSesion -> PantallaCarga()

        usuario != null -> HomeScreen(
            usuario = usuario,
            cargando = state.cargando,
            info = state.info,
            onReenviarVerificacion = viewModel::reenviarVerificacion,
            onComprobarVerificacion = viewModel::comprobarVerificacion,
            onCerrarSesion = viewModel::cerrarSesion
        )

        else -> LoginScreen(
            state = state,
            onNombreChange = viewModel::onNombreChange,
            onCorreoChange = viewModel::onCorreoChange,
            onClaveChange = viewModel::onClaveChange,
            onEnviarCorreo = viewModel::enviarFormularioCorreo,
            onRecuperarClave = viewModel::recuperarClave,
            onCambiarModo = viewModel::cambiarModo,
            onGoogle = viewModel::loginConGoogle,
            onFacebookToken = viewModel::loginConFacebook,
            onGitHub = viewModel::loginConGitHub,
            onError = viewModel::mostrarError
        )
    }
}

@Composable
private fun PantallaCarga() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}