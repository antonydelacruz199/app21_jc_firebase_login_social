package com.example.app21_jc_firebase_login_social.model

// Datos del usuario autenticado que la app necesita mostrar
data class Usuario(
    val uid: String,
    val nombre: String,
    val correo: String,
    val fotoUrl: String?,
    val proveedor: String,          // "Correo", "Google", "Facebook" o "GitHub"
    val correoVerificado: Boolean
)