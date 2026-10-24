package com.example.app21_jc_firebase_login_social.viewmodel

import com.example.app21_jc_firebase_login_social.model.Usuario

data class AuthUiState(
    // Sesión
    val usuario: Usuario? = null,          // null = sin sesión
    val verificandoSesion: Boolean = true, // true mientras Firebase revisa si ya había sesión
    val cargando: Boolean = false,         // true durante una operación

    // Formulario de correo y contraseña
    val modoRegistro: Boolean = false,     // false = Ingresar, true = Crear cuenta
    val nombre: String = "",
    val correo: String = "",
    val clave: String = "",

    // Mensajes
    val error: String? = null,
    val info: String? = null               // avisos de éxito: correo enviado, verificado...
)