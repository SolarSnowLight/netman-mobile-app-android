package com.game.mobileappar.config.game

object ConfigStatusPlayer {
    const val PLAYER_DEFAULT:        Byte = 0       // Обычный пользователь (без текущей игры)
    const val PLAYER_ACTIVE:         Byte = 1       // Игрок, чья команда участвует в игре
    const val PLAYER_ACTIVE_VIDEO:   Byte = 2       // Игрок, который будет записывать видео
    const val JUDGE:                 Byte = 3       // Судъя
}