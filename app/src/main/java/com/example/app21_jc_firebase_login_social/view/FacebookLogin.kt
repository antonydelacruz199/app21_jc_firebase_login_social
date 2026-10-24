package com.example.app21_jc_firebase_login_social.view

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult

/**
 * Prepara el inicio de sesión con el SDK de Meta y devuelve la función que lo lanza.
 * Al terminar entrega el access token, que luego se envía a Firebase.
 */
@Composable
fun rememberFacebookLogin(
    onToken: (String) -> Unit,
    onError: (String) -> Unit
): () -> Unit {
    val loginManager = remember { LoginManager.getInstance() }
    val callbackManager = remember { CallbackManager.Factory.create() }
    val onTokenActual by rememberUpdatedState(onToken)
    val onErrorActual by rememberUpdatedState(onError)

    DisposableEffect(Unit) {
        loginManager.registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
            override fun onSuccess(result: LoginResult) {
                onTokenActual(result.accessToken.token)
            }

            override fun onCancel() {
                // El usuario cerró la ventana de Facebook: no se muestra error
            }

            override fun onError(error: FacebookException) {
                onErrorActual(error.message ?: "No se pudo iniciar sesión con Facebook")
            }
        })
        onDispose { loginManager.unregisterCallback(callbackManager) }
    }

    val launcher = rememberLauncherForActivityResult(
        contract = loginManager.createLogInActivityResultContract(callbackManager, null)
    ) { }

    return { launcher.launch(listOf("email", "public_profile")) }
}