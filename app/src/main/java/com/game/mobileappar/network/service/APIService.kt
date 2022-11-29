package com.game.mobileappar.network.service

import com.game.mobileappar.config.ConfigAddresses
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

interface APIService {
    //*********************************************************
    // Безопасность:

    @POST(ConfigAddresses.SECURITY_EXISTS)
    suspend fun userExists(@Body requestBody: RequestBody): Response<ResponseBody>

    @POST(ConfigAddresses.SECURITY_TOKEN)
    suspend fun tokenValid(@Body requestBody: RequestBody): Response<ResponseBody>

    @POST(ConfigAddresses.SECURITY_ACCESS)
    suspend fun checkAccess(@Body requestBody: RequestBody): Response<ResponseBody>

    //*********************************************************
    // Авторизация:

    // Логирование пользователя (обычное)
    @POST(ConfigAddresses.AUTH_LOGIN)
    suspend fun authLogin(@Body requestBody: RequestBody): Response<ResponseBody>

    // Логирование пользователя (через Google OAUTH2)
    @POST(ConfigAddresses.AUTH_OAUTH)
    suspend fun authOauth(@Body requestBody: RequestBody): Response<ResponseBody>

    // Регистрация пользователя в системе
    @POST(ConfigAddresses.AUTH_REGISTER)
    suspend fun authRegister(@Body requestBody: RequestBody): Response<ResponseBody>
    //*********************************************************
    // Функциональный модуль "Игрок":

    // Получение персональных данных о пользователе
    @POST(ConfigAddresses.FUNCTION_PLAYER_INFO)
    suspend fun funPlayerInfo(@Body requestBody: RequestBody): Response<ResponseBody>

    // Обновление пользовательских данных
    @POST(ConfigAddresses.FUNCTION_PLAYER_INFO_UPDATE)
    suspend fun funPlayerInfoUpdate(@Body requestBody: RequestBody): Response<ResponseBody>

    // Получение списка доступных игр для регистрации
    @POST(ConfigAddresses.FUNCTION_PLAYER_GAMES)
    suspend fun funPlayerGames(@Body requestBody: RequestBody): Response<ResponseBody>

    // Получение статистики игрока
    @POST(ConfigAddresses.FUNCTION_PLAYER_STATISTICS)
    suspend fun funPlayerStatistics(@Body requestBody: RequestBody): Response<ResponseBody>

    // Получение информации о команде
    @POST(ConfigAddresses.FUNCTION_PLAYER_COMMAND)
    suspend fun funCommand(@Body requestBody: RequestBody): Response<ResponseBody>

    // Получение информации об игроках, которые есть в команде
    @POST(ConfigAddresses.FUNCTION_PLAYER_COMMAND_PLAYERS)
    suspend fun funCommandPlayers(@Body requestBody: RequestBody): Response<ResponseBody>

    // Присоединение игрока к определённой команде
    @POST(ConfigAddresses.FUNCTION_PLAYER_COMMAND_JOIN)
    suspend fun funPlayerCommandJoin(@Body requestBody: RequestBody): Response<ResponseBody>

    @POST(ConfigAddresses.FUNCTION_PLAYER_COMMAND_DETACH)
    suspend fun funPlayerCommandDetach(@Body requestBody: RequestBody): Response<ResponseBody>

    // Получение информации обо всех пройденных играх данной командой
    @POST(ConfigAddresses.FUNCTION_PLAYER_COMMAND_GAMES)
    suspend fun funCommandGames(@Body requestBody: RequestBody): Response<ResponseBody>

    // Получение информации о текущей игре
    @POST(ConfigAddresses.FUNCTION_PLAYER_COMMAND_CURRENT_GAME)
    suspend fun funCommandCurrentGame(@Body requestBody: RequestBody): Response<ResponseBody>

    // Список всех существующих команд
    @POST(ConfigAddresses.FUNCTION_PLAYER_COMMANDS_LIST)
    suspend fun funCommandList(@Body requestBody: RequestBody): Response<ResponseBody>

    // Создание новой команды
    @POST(ConfigAddresses.FUNCTION_PLAYER_COMMAND_CREATE)
    suspend fun funCommandCreate(@Body requestBody: RequestBody): Response<ResponseBody>

    // Регистрация команды на игру
    @POST(ConfigAddresses.FUNCTION_PLAYER_COMMAND_REGISTER_GAME)
    suspend fun funCommandRegisterGame(@Body requestBody: RequestBody): Response<ResponseBody>

    // Получение списка игр, на которые можно зарегистрировать команду
    @POST(ConfigAddresses.FUNCTION_PLAYER_COMMAND_AVAILABLE_GAMES)
    suspend fun funCommandAvailableGames(@Body requestBody: RequestBody): Response<ResponseBody>

    // Получение списка пользователей, у которых нет команды
    @POST(ConfigAddresses.FUNCTION_PLAYER_COMMAND_FREE_LIST_TAG)
    suspend fun funSearchFreePlayerTag(@Body requestBody: RequestBody): Response<ResponseBody>

    // Добавление игрока в команду
    @POST(ConfigAddresses.FUNCTION_PLAYER_COMMAND_JOIN_CERTAIN)
    suspend fun funPlayerCommandJoinCertain(@Body requestBody: RequestBody): Response<ResponseBody>

    // Получение информации о текущем медиафайле
    @POST(ConfigAddresses.FUNCTION_PLAYER_COMMAND_CURRENT_MEDIA)
    suspend fun funPlayerCommandCurrentMedia(@Body requestBody: RequestBody): Response<ResponseBody>

    @POST(ConfigAddresses.FUNCTION_PLAYER_COMMAND_ADD_RESULT)
    suspend fun funCommandAddResultMedia(@Body requestBody: RequestBody): Response<ResponseBody>

    @POST(ConfigAddresses.FUNCTION_PLAYER_JUDGE_GET_INFO)
    suspend fun funPlayerJudgeGetInfo(@Body requestBody: RequestBody): Response<ResponseBody>

    @POST(ConfigAddresses.FUNCTION_PLAYER_JUDGE_SET_SCORE)
    suspend fun funPlayerJudgeSetScore(@Body requestBody: RequestBody): Response<ResponseBody>

    //*********************************************************
    // Адреса с которыми могут взаимодействовать службы для
    // уведомления игрока о начале игрового процесса

    // Проверка: имеется ли на данный момент какая-нибудь игра,
    // в которой участвует команда игрока и которая начата
    @POST(ConfigAddresses.FUNCTION_PLAYER_GAME_STATUS)
    suspend fun isGameActiveExists(@Body requestBody: RequestBody): Response<ResponseBody>

    //*********************************************************
    // Мессенджер

    // Поиск игроков
    @POST(ConfigAddresses.FUNCTION_PLAYER_FIND_CERTAIN)
    suspend fun funPlayerFindCertain(@Body requestBody: RequestBody): Response<ResponseBody>

    //*********************************************************
    // Загрузка медиа

    @Multipart
    @POST(ConfigAddresses.MEDIA_UPLOAD)
    suspend fun mediaUpload(@PartMap map: HashMap<String?, RequestBody?>): Response<ResponseBody>

    @Streaming
    @POST(ConfigAddresses.MEDIA_DOWNLOAD)
    suspend fun mediaDownload(@Body requestBody: RequestBody): Response<ResponseBody>

    @Streaming
    @POST(ConfigAddresses.MEDIA_INSTRUCTIONS_DOWNLOAD)
    suspend fun mediaInstructionsDownload(@Body requestBody: RequestBody): Response<ResponseBody>

    //*********************************************************
    //*********************************************************
    // Загрузка пользовательских изображений

    @Multipart
    @POST(ConfigAddresses.USER_IMAGE_UPLOAD)
    suspend fun userImageUpload(@PartMap map: HashMap<String?, RequestBody?>): Response<ResponseBody>

    @Streaming
    @POST(ConfigAddresses.USER_IMAGE_DOWNLOAD)
    suspend fun userImageDownload(@Body requestBody: RequestBody): Response<ResponseBody>

    @Streaming
    @POST(ConfigAddresses.TEAM_PLAYERS_IMAGE_DOWNLOAD)
    suspend fun teamPlayersImageDownload(@Body requestBody: RequestBody): Response<ResponseBody>

    //*********************************************************
}