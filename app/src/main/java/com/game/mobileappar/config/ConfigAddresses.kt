package com.game.mobileappar.config

object ConfigAddresses {
    // Адрес центрального сервера
    const val SERVER_CENTRAL_ADDRESS    = "http://45.147.177.83:5000"//"http://10.0.2.2:5000" //"http://45.147.177.9:5000"

    // Адрес сервера обмена сообщениями
    const val SERVER_MESSENGER_ADDRESS  = "http://10.0.2.2:5002"//"http://10.0.2.2:5001" //"http://45.147.177.83:5000"

    // Адрес медиа сервера
    const val SERVER_MEDIA_ADDRESS      = "http://45.147.177.83:5001" //"http://10.0.2.2:6000" //"http://45.147.177.9:6000"

    // Авторизация
    const val AUTH_LOGIN    = "/auth/login"
    const val AUTH_REGISTER = "/auth/register"
    const val AUTH_OAUTH    = "/auth/oauth"

    // Обновление токена доступа
    const val AUTH_REFRESH_TOKEN = "/auth/refresh/token"

    // Проверка доступа
    const val SECURITY_ACCESS = "/sequrity/access"
    const val SECURITY_EXISTS = "/sequrity/exists"
    const val SECURITY_TOKEN  = "/sequrity/token"

    // **********************************************************************
    // Функции игрока
    const val FUNCTION_PLAYER_GAMES         = "/function/player/games"
    const val FUNCTION_PLAYER_INFO          = "/function/player/info"
    const val FUNCTION_PLAYER_INFO_UPDATE   = "/function/player/info/update"
    const val FUNCTION_PLAYER_STATISTICS    = "/function/player/statistics"
    const val FUNCTION_PLAYER_GAME_STATUS   = "/function/player/game/status"
    //------------------------------------------------------------------------
    // Команда:
    const val FUNCTION_PLAYER_COMMAND           = "/function/player/command"
    const val FUNCTION_PLAYER_COMMAND_PLAYERS   = "/function/player/command/players"
    const val FUNCTION_PLAYER_COMMAND_GAMES     = "/function/player/command/games"
    const val FUNCTION_PLAYER_COMMAND_CURRENT_GAME
                                                = "/function/player/command/current/game"
    const val FUNCTION_PLAYER_COMMANDS_LIST     = "/function/player/commands/list"
    const val FUNCTION_PLAYER_COMMAND_JOIN      = "/function/player/command/join"
    const val FUNCTION_PLAYER_COMMAND_DETACH    = "/function/player/command/detach"
    const val FUNCTION_PLAYER_COMMAND_CREATE    = "/function/player/command/create"
    const val FUNCTION_PLAYER_COMMAND_REGISTER_GAME
                                                = "/function/player/command/register/game"
    const val FUNCTION_PLAYER_COMMAND_AVAILABLE_GAMES
                                                = "/function/player/command/available/games"
    const val FUNCTION_PLAYER_COMMAND_FREE_LIST_TAG
                                                = "/function/player/command/free/list/tag"
    const val FUNCTION_PLAYER_COMMAND_JOIN_CERTAIN
                                                = "/function/player/command/join/certain"
    const val FUNCTION_PLAYER_FIND_CERTAIN      = "/function/player/find/certain"
    const val FUNCTION_PLAYER_COMMAND_CURRENT_MEDIA
                                                = "/function/player/command/current/media/instructions"
    const val FUNCTION_PLAYER_COMMAND_ADD_RESULT
                                                = "/function/player/command/add/result"
    const val FUNCTION_PLAYER_JUDGE_GET_INFO    = "/function/player/judge/get/info"
    const val FUNCTION_PLAYER_JUDGE_SET_SCORE   = "/function/player/judge/set/score"

    // Загрузка файлов
    const val MEDIA_UPLOAD                      = "/media/upload"
    const val MEDIA_DOWNLOAD                    = "/media/download"
    const val MEDIA_INSTRUCTIONS_DOWNLOAD       = "/media/instructions/download"

    // Загрузка пользовательких изображений
    const val USER_IMAGE_UPLOAD                 = "/users/icons/upload"
    const val USER_IMAGE_DOWNLOAD               = "/users/icons/download"
    const val TEAM_PLAYERS_IMAGE_DOWNLOAD       = "/users/team/players/icons/download"

    const val MEDIA_DOWNLOAD_USERS_FILE         = "/media/download/users/file"
    const val MEDIA_UPLOAD_INSTRUCTIONS_FILE    = "/media/upload/instructions/file"
    const val MEDIA_DOWNLOAD_INSTRUCTIONS_FILE  = "/media/download/instructions/file"
}