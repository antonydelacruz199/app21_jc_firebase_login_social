package com.example.app21_jc_firebase_login_social.model

import android.app.Activity
import android.content.Context
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.ClearCredentialException
import com.example.app21_jc_firebase_login_social.R
import com.facebook.login.LoginManager
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GithubAuthProvider
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.OAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

// Única capa que conoce Firebase Auth y los SDK de cada proveedor
class AuthRepository(context: Context) {

    private val auth = FirebaseAuth.getInstance().apply {
        setLanguageCode("es")   // correos de verificación y recuperación en español
    }
    private val credentialManager = CredentialManager.create(context)

    // Lo genera el plugin google-services a partir de google-services.json
    private val webClientId = context.getString(R.string.default_web_client_id)

    // Emite el usuario cada vez que inicia o cierra sesión (null = sin sesión)
    val usuarioActual: Flow<Usuario?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            trySend(firebaseAuth.currentUser?.aUsuario())
        }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    // ---------- CORREO Y CONTRASEÑA: Firebase guarda la cuenta, sin proveedor externo ----------

    suspend fun registrarConCorreo(nombre: String, correo: String, clave: String): Usuario? {
        val usuario = auth.createUserWithEmailAndPassword(correo, clave).await().user ?: return null

        // El nombre no se pide al crear la cuenta; se guarda en el perfil después
        val perfil = UserProfileChangeRequest.Builder().setDisplayName(nombre).build()
        usuario.updateProfile(perfil).await()
        usuario.sendEmailVerification().await()

        usuario.reload().await()
        return auth.currentUser?.aUsuario()
    }

    suspend fun loginConCorreo(correo: String, clave: String) {
        auth.signInWithEmailAndPassword(correo, clave).await()
    }

    suspend fun enviarCorreoRecuperacion(correo: String) {
        auth.sendPasswordResetEmail(correo).await()
    }

    suspend fun reenviarVerificacion() {
        auth.currentUser?.sendEmailVerification()?.await()
    }

    // Vuelve a leer el usuario desde Firebase, por ejemplo después de abrir el enlace de verificación
    suspend fun recargarUsuario(): Usuario? {
        auth.currentUser?.reload()?.await()
        return auth.currentUser?.aUsuario()
    }

    // ---------- GOOGLE: Credential Manager entrega un ID token y Firebase lo valida ----------

    suspend fun loginConGoogle(activity: Activity) {
        val opcionGoogle = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)   // muestra todas las cuentas del dispositivo
            .setServerClientId(webClientId)
            .build()

        val solicitud = GetCredentialRequest.Builder()
            .addCredentialOption(opcionGoogle)
            .build()

        val credencial = credentialManager.getCredential(activity, solicitud).credential

        if (credencial is CustomCredential &&
            credencial.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        ) {
            val googleId = GoogleIdTokenCredential.createFrom(credencial.data)
            val firebaseCredencial = GoogleAuthProvider.getCredential(googleId.idToken, null)
            auth.signInWithCredential(firebaseCredencial).await()
        } else {
            throw IllegalStateException("La credencial recibida no es de Google")
        }
    }

    // ---------- FACEBOOK: la pantalla obtiene el access token con el SDK de Meta ----------

    suspend fun loginConFacebook(accessToken: String) {
        val firebaseCredencial = FacebookAuthProvider.getCredential(accessToken)
        auth.signInWithCredential(firebaseCredencial).await()
    }

    // ---------- GITHUB (y otros OAuth, por ejemplo "twitter.com"): Firebase abre el navegador ----------

    suspend fun loginConProveedorOAuth(activity: Activity, proveedorId: String) {
        // Si Android cerró la app mientras el navegador estaba abierto, se recupera el resultado
        val pendiente = auth.pendingAuthResult
        if (pendiente != null) {
            pendiente.await()
            return
        }
        val proveedor = OAuthProvider.newBuilder(proveedorId)
            .setScopes(listOf("user:email"))        // permiso para leer el correo en GitHub
            .build()
        auth.startActivityForSignInWithProvider(activity, proveedor).await()
    }

    // ---------- CERRAR SESIÓN en Firebase y en cada proveedor ----------

    suspend fun cerrarSesion() {
        auth.signOut()
        LoginManager.getInstance().logOut()
        try {
            credentialManager.clearCredentialState(ClearCredentialStateRequest())
        } catch (e: ClearCredentialException) {
            // No impide cerrar la sesión de Firebase
        }
    }

    private fun FirebaseUser.aUsuario(): Usuario {
        val proveedorId = providerData.map { it.providerId }.firstOrNull { it != "firebase" }
        return Usuario(
            uid = uid,
            nombre = displayName?.takeIf { it.isNotBlank() } ?: "Sin nombre",
            correo = email ?: "Sin correo",
            fotoUrl = photoUrl?.toString(),
            proveedor = when (proveedorId) {
                EmailAuthProvider.PROVIDER_ID -> "Correo"
                GoogleAuthProvider.PROVIDER_ID -> "Google"
                FacebookAuthProvider.PROVIDER_ID -> "Facebook"
                GithubAuthProvider.PROVIDER_ID -> "GitHub"
                else -> proveedorId ?: "Desconocido"
            },
            correoVerificado = isEmailVerified
        )
    }
}