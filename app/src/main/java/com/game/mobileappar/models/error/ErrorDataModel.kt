package com.game.mobileappar.models.error

import com.google.gson.annotations.SerializedName;

data class ErrorDataModel(
    @SerializedName("errors") var errors: List<ErrorElementModel>?,
    @SerializedName("message") var message : String?
)
