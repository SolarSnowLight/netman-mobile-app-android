package com.game.mobileappar.config.command

object ConfigStatusCommand {
    const val WITHOUT_TEAM:  Byte = 0;    // Игрок без команды
    const val TEAM_MEMBER:   Byte = 1;    // Игрок находится в определённой команде
    const val TEAM_CREATOR:  Byte = 2;    // Игрок является создателем команды
}