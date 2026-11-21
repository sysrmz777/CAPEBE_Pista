package com.example.appvuelos_capebe.model

data class RegisterModel(
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val pin: String = "",
    val errorMessage: String? = null
)
