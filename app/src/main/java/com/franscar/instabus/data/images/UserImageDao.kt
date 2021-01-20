package com.franscar.instabus.data.images

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.franscar.instabus.data.images.UserImage

@Dao
interface UserImageDao {
    @Query("SELECT * FROM userImages")
    fun getAll(): List<UserImage>

    @Query("SELECT * FROM userImages WHERE station = :station ORDER BY imageId DESC")
    fun getImages(station: String): List<UserImage>

    @Insert
    suspend fun insertImage(userImage: UserImage)

    @Query("DELETE FROM userImages WHERE date = :date")
    suspend fun deleteImage(date: String)
}