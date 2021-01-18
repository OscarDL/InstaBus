package com.franscar.instabus.data.images

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.franscar.instabus.data.images.UserImage

@Dao
interface UserImageDao {
    @Query("SELECT * FROM userImages")
    fun getAll(): List<UserImage>

    @Insert
    suspend fun insertImage(userImage: UserImage)
}