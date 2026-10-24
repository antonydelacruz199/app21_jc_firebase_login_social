package com.example.app21_jc_firebase_login_social.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.app21_jc_firebase_login_social.viewmodel.AuthUiState

// Ingresar o crear cuenta con correo y contraseña
@Composable
fun FormularioCorreo(
    state: AuthUiState,
    onNombreChange: (String) -> Unit,
    onCorreoChange: (String) -> Unit,
    onClaveChange: (String) -> Unit,
    onEnviar: () -> Unit,
    onRecuperarClave: () -> Unit,
    onCambiarModo: () -> Unit
) {
    // Mostrar u ocultar la contraseña es un detalle visual: vive en la vista, no en el ViewModel
    var claveVisible by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (state.modoRegistro) {
            OutlinedTextField(
                value = state.nombre,
                onValueChange = onNombreChange,
                label = { Text("Nombre completo") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Next
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }

        OutlinedTextField(
            value = state.correo,
            onValueChange = onCorreoChange,
            label = { Text("Correo electrónico") },
            placeholder = { Text("nombre@gmail.com") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            ),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = state.clave,
            onValueChange = onClaveChange,
            label = { Text("Contraseña") },
            singleLine = true,
            visualTransformation =
                if (claveVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(onDone = { onEnviar() }),
            trailingIcon = {
                IconButton(onClick = { claveVisible = !claveVisible }) {
                    Icon(
                        imageVector = if (claveVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = if (claveVisible) "Ocultar contraseña" else "Mostrar contraseña"
                    )
                }
            },
            supportingText = if (state.modoRegistro) {
                { Text("Mínimo 6 caracteres") }
            } else null,
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = onEnviar,
            enabled = !state.cargando,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Text(if (state.modoRegistro) "Crear cuenta" else "Ingresar")
        }

        if (!state.modoRegistro) {
            TextButton(
                onClick = onRecuperarClave,
                enabled = !state.cargando,
                modifier = Modifier.align(Alignment.End)
            ) { Text("¿Olvidaste tu contraseña?") }
        }

        TextButton(
            onClick = onCambiarModo,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text(
                if (state.modoRegistro) "¿Ya tienes cuenta? Inicia sesión"
                else "¿No tienes cuenta? Regístrate"
            )
        }
    }
}