package com.example.app21_jc_firebase_login_social.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.app21_jc_firebase_login_social.model.Usuario

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    usuario: Usuario,
    cargando: Boolean,
    info: String?,
    onReenviarVerificacion: () -> Unit,
    onComprobarVerificacion: () -> Unit,
    onCerrarSesion: () -> Unit
) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("Mi perfil") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            FotoPerfil(usuario)
            Text(
                text = usuario.nombre,
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center
            )
            Text(
                text = usuario.correo,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            AssistChip(onClick = {}, label = { Text("Ingresó con ${usuario.proveedor}") })

            // Solo las cuentas de correo necesitan confirmar la dirección
            if (usuario.proveedor == "Correo" && !usuario.correoVerificado) {
                AvisoVerificacion(
                    correo = usuario.correo,
                    cargando = cargando,
                    onReenviar = onReenviarVerificacion,
                    onComprobar = onComprobarVerificacion
                )
            }
            info?.let { Mensaje(texto = it, esError = false) }

            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        text = "UID en Firebase",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(usuario.uid, style = MaterialTheme.typography.bodyMedium)
                }
            }

            Spacer(Modifier.height(24.dp))
            Button(onClick = onCerrarSesion, modifier = Modifier.fillMaxWidth()) {
                Text("Cerrar sesión")
            }
        }
    }
}

@Composable
private fun AvisoVerificacion(
    correo: String,
    cargando: Boolean,
    onReenviar: () -> Unit,
    onComprobar: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                text = "Confirma tu correo",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onTertiaryContainer
            )
            Text(
                text = "Abre el enlace que enviamos a $correo y luego toca \"Ya lo confirmé\".",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onTertiaryContainer
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = onComprobar, enabled = !cargando) { Text("Ya lo confirmé") }
                TextButton(onClick = onReenviar, enabled = !cargando) { Text("Reenviar enlace") }
            }
        }
    }
}

@Composable
private fun FotoPerfil(usuario: Usuario) {
    val forma = Modifier
        .size(96.dp)
        .clip(CircleShape)

    if (usuario.fotoUrl != null) {
        AsyncImage(
            model = usuario.fotoUrl,
            contentDescription = "Foto de ${usuario.nombre}",
            contentScale = ContentScale.Crop,
            modifier = forma
        )
    } else {
        // Sin foto (por ejemplo, cuentas de correo): círculo con la inicial del nombre
        Box(
            modifier = forma.background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = usuario.nombre.take(1).uppercase(),
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}