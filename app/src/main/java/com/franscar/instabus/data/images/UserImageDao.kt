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

    @Query("SELECT * FROM userImages WHERE station = :station ORDER BY " +
            "CASE WHEN :sorting = 0 THEN imageId END ASC, " +
            "CASE WHEN :sorting = 1 THEN imageId END DESC, " +
            "CASE WHEN :sorting = 2 THEN title END ASC, " +
            "CASE WHEN :sorting = 3 THEN title END DESC")
    fun getImages(station: String, sorting: Int): List<UserImage>

    @Insert
    suspend fun insertImage(userImage: UserImage)

    @Query("DELETE FROM userImages WHERE date = :date")
    suspend fun deleteImage(date: String)
}