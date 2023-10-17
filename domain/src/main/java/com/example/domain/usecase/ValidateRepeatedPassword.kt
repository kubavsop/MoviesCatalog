package com.example.domain.usecase

class ValidateRepeatedPassword {
    operator fun invoke(password: String, repeatedPassword: String): Boolean {
        return password == repeatedPassword
    }
}