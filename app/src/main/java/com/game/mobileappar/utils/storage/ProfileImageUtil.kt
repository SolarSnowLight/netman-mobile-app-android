package com.game.mobileappar.utils.storage

import com.game.mobileappar.models.storage.StorageProfileImageModel

object ProfileImageUtil {
    // Поиск элемента в списке изображений членов команды по email-адресу
    fun indexOfProfileImagesByUsersId(data: StorageProfileImageModel?, usersId: Int): Int{
        if((data == null) || (data.listProfileImage.size <= 0)){
            return (-1)
        }

        for(i in 0 until data.listProfileImage.size){
            if(data.listProfileImage[i].usersId == usersId){
                return i
            }
        }

        return (-1)
    }
}