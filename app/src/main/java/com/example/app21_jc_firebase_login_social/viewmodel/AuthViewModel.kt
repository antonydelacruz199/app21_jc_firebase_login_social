package com.example.app21_jc_firebase_login_social.viewmodel

import android.app.Activity
import android.app.Application
import android.util.Patterns
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.NoCredentialException
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.app21_jc_firebase_login_social.model.AuthRepository
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// AndroidViewModel porque el Repository necesita el Context de la aplicación
class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AuthRepository(application)

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        // Si el usuario ya había iniciado sesión, Firebase lo recuerda y entra directo al perfil
        viewModelScope.launch {
            repository.usuarioActual.collect { usuario ->
                _uiState.update { it.copy(usuario = usuario, verificandoSesion = false) }
            }
        }
    }

    // ---------- Formulario de correo ----------

    fun onNombreChange(valor: String) = _uiState.update { it.copy(nombre = valor) }

    fun onCorreoChange(valor: String) = _uiState.update { it.copy(correo = valor) }

    fun onClaveChange(valor: String) = _uiState.update { it.copy(clave = valor) }

    fun cambiarModo() = _uiState.update {
        it.copy(modoRegistro = !it.modoRegistro, error = null, info = null)
    }

    fun enviarFormularioCorreo() {
        val estado = _uiState.value
        val error = validarFormulario(estado)
        if (error != null) {
            mostrarError(error)
            return
        }
        val correo = estado.correo.trim()

        if (estado.modoRegistro) {
            ejecutar {
                val usuario = repository.registrarConCorreo(estado.nombre.trim(), correo, estado.clave)
                // Se actualiza a mano porque el nombre se guardó después de crear la cuenta
                _uiState.update {
                    it.copy(
                        usuario = usuario ?: it.usuario,
                        clave = "",
                        info = "Te enviamos un enlace de verificación a $correo"
                    )
                }
            }
        } else {
            ejecutar {
                repository.loginConCorreo(correo, estado.clave)
                _uiState.update { it.copy(clave = "") }
            }
        }
    }

    fun recuperarClave() {
        val correo = _uiState.value.correo.trim()
        if (!Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
            mostrarError("Escribe tu correo arriba para enviarte el enlace")
            return
        }
        ejecutar {
            repository.enviarCorreoRecuperacion(correo)
            _uiState.update {
                it.copy(info = "Si $correo está registrado, recibirás un enlace para cambiar tu contraseña")
            }
        }
    }

    fun reenviarVerificacion() = ejecutar {
        repository.reenviarVerificacion()
        _uiState.update { it.copy(info = "Enlace de verificación reenviado. Revisa también la carpeta de spam.") }
    }

    fun comprobarVerificacion() = ejecutar {
        val usuario = repository.recargarUsuario()
        _uiState.update {
            it.copy(
                usuario = usuario,
                info = if (usuario?.correoVerificado == true) "Correo verificado"
                else "Todavía no confirmas el correo. Abre el enlace que te enviamos."
            )
        }
    }

    // ---------- Proveedores externos ----------

    // La Activity solo se usa durante la llamada; el ViewModel no la guarda
    fun loginConGoogle(activity: Activity) = ejecutar { repository.loginConGoogle(activity) }

    fun loginConFacebook(accessToken: String) = ejecutar { repository.loginConFacebook(accessToken) }

    fun loginConGitHub(activity: Activity) =
        ejecutar { repository.loginConProveedorOAuth(activity, "github.com") }

    // ---------- Sesión y mensajes ----------

    fun cerrarSesion() {
        viewModelScope.launch {
            repository.cerrarSesion()
            _uiState.update { it.copy(clave = "", info = null, error = null) }
        }
    }

    fun mostrarError(mensaje: String) = _uiState.update { it.copy(error = mensaje, info = null) }

    private fun ejecutar(accion: suspend () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(cargando = true, error = null, info = null) }
            try {
                accion()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update { it.copy(error = traducirError(e)) }
            } finally {
                _uiState.update { it.copy(cargando = false) }
            }
        }
    }

    // Validación local antes de llamar a Firebase
    private fun validarFormulario(estado: AuthUiState): String? = when {
        estado.modoRegistro && estado.nombre.isBlank() -> "Ingresa tu nombre"
        !Patterns.EMAIL_ADDRESS.matcher(estado.correo.trim()).matches() -> "Ingresa un correo válido"
        estado.clave.length < 6 -> "La contraseña debe tener al menos 6 caracteres"
        else -> null
    }

    // null = no se muestra nada (el usuario canceló). El orden importa: primero las subclases.
    private fun traducirError(e: Exception): String? = when {
        e is GetCredentialCancellationException -> null
        e is NoCredentialException ->
            "No hay cuentas de Google en el dispositivo. Agrega una en Ajustes."
        e is FirebaseAuthWeakPasswordException ->
            "La contraseña es muy débil. Usa al menos 6 caracteres."
        e is FirebaseAuthUserCollisionException ->
            "Ese correo ya tiene una cuenta. Ingresa con el método que usaste al registrarte."
        e is FirebaseAuthInvalidCredentialsException -> "Correo o contraseña incorrectos"
        e is FirebaseAuthInvalidUserException -> "La cuenta no existe o fue deshabilitada"
        e is FirebaseTooManyRequestsException -> "Demasiados intentos. Espera unos minutos."
        e is FirebaseNetworkException -> "Sin conexión a Internet"
        e is FirebaseAuthException && e.errorCode == "ERROR_WEB_CONTEXT_CANCELED" -> null
        e is FirebaseAuthException && e.errorCode == "ERROR_OPERATION_NOT_ALLOWED" ->
            "Este método de acceso no está activado en Firebase"
        else -> e.message ?: "No se pudo completar la operación"
    }
}