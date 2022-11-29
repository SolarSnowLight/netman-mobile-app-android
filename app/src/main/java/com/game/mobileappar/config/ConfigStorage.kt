package com.game.mobileappar.config

object ConfigStorage {
    // *******************************************************
    // Локальное хранилище идентификационных данных пользователя
    const val LOCAL_STORAGE                         = "local_storage"
    const val USERS_DATA                            = "users_data"
    // *******************************************************

    // *******************************************************
    // Локальное хранилище идентификационных данных команды пользователя
    const val COMMAND_INFO_STORAGE                  = "command_info_storage"
    const val COMMAND_INFO_DATA                     = "command_info_data"
    // *******************************************************

    // *******************************************************
    // Локальные хранилища для ссылок на изображения:

    // Текущее изображение пользователя
    const val PROFILE_IMAGE                         = "profile_image"
    const val PROFILE_IMAGE_DATA                    = "image_data"

    // Изображение пользователей, которые находятся в команде
    // с данным пользователем
    const val PROFILE_IMAGES_TEAM_PLAYERS           = "profile_images_team_players"
    const val PROFILE_IMAGES_TEAM_PLAYERS_DATA      = "profile_images_team_players_data"

    // Изображения всех пользователей, с которыми данный игрок
    // имеет общие чаты (как приватные, так и групповые)
    const val PROFILE_IMAGES_CHAT                   = "profile_images_chat"
    const val PROFILE_IMAGES_CHAT_DATA              = "profile_images_chat_data"

    // Изображения всех остальных пользователей (данные изображения время от времени
    // удаляются, т.к. обычно они используются в поиске или при просмотре статистики)
    const val PROFILE_IMAGES_CASH                   = "profile_images_cash"
    const val PROFILE_IMAGES_CASH_DATA              = "profile_images_cash_data"
    // *******************************************************
}