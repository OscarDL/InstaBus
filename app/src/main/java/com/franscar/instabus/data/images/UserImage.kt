package com.franscar.instabus.data.images

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.File

@Entity(tableName = "userImages")
data class UserImage(
    @PrimaryKey(autoGenerate = true)
    val imageId: Int,
    val image: String,
    val title: String,
    val date: String,
    val station: String
)