package com.game.mobileappar.constants.addresses.auth

object AuthApiConstants {
    // Авторизация
    const val LOGIN            = "/auth/login"
    const val OAUTH            = "/auth/oauth"

    // Регистрация
    const val REGISTER         = "/auth/register"

    // Выход из системы
    const val LOGOUT           = "/auth/logout"

    // Обновление токена доступа
    const val REFRESH_TOKEN    = "/auth/refresh/token"
}