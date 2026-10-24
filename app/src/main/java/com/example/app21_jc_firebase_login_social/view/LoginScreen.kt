package com.example.app21_jc_firebase_login_social.view

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.app21_jc_firebase_login_social.viewmodel.AuthUiState

@Composable
fun LoginScreen(
    state: AuthUiState,
    // Correo y contraseña
    onNombreChange: (String) -> Unit,
    onCorreoChange: (String) -> Unit,
    onClaveChange: (String) -> Unit,
    onEnviarCorreo: () -> Unit,
    onRecuperarClave: () -> Unit,
    onCambiarModo: () -> Unit,
    // Proveedores externos
    onGoogle: (Activity) -> Unit,
    onFacebookToken: (String) -> Unit,
    onGitHub: (Activity) -> Unit,
    onError: (String) -> Unit
) {
    val activity = LocalContext.current.buscarActivity()
    val iniciarFacebook = rememberFacebookLogin(onToken = onFacebookToken, onError = onError)

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (state.modoRegistro) "Crear cuenta" else "Bienvenido",
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Usa tu correo o una de tus cuentas",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(24.dp))

            FormularioCorreo(
                state = state,
                onNombreChange = onNombreChange,
                onCorreoChange = onCorreoChange,
                onClaveChange = onClaveChange,
                onEnviar = onEnviarCorreo,
                onRecuperarClave = onRecuperarClave,
                onCambiarModo = onCambiarModo
            )

            Separador("o continúa con")

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                BotonProveedor("Continuar con Google", habilitado = !state.cargando) {
                    activity?.let(onGoogle)
                }
                BotonProveedor("Continuar con Facebook", habilitado = !state.cargando) {
                    iniciarFacebook()
                }
                BotonProveedor("Continuar con GitHub", habilitado = !state.cargando) {
                    activity?.let(onGitHub)
                }
            }

            Spacer(Modifier.height(24.dp))
            if (state.cargando) CircularProgressIndicator()
            state.error?.let { Mensaje(texto = it, esError = true) }
            state.info?.let { Mensaje(texto = it, esError = false) }
        }
    }
}

@Composable
private fun Separador(texto: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        HorizontalDivider(Modifier.weight(1f))
        Text(
            text = texto,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 12.dp)
        )
        HorizontalDivider(Modifier.weight(1f))
    }
}

// LocalContext puede venir envuelto en un ContextWrapper; se busca la Activity real
private fun Context.buscarActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.buscarActivity()
    else -> null
}