package com.franscar.instabus.data.images

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [UserImage::class], version = 1, exportSchema = false)
abstract class UserImageDatabase: RoomDatabase() {
    abstract fun userImageDao(): UserImageDao

    companion object {
        @Volatile
        private var INSTANCE: UserImageDatabase? = null

        fun getDatabase(context: Context): UserImageDatabase {
            if (INSTANCE == null) {
                synchronized(this) {
                    INSTANCE = Room.databaseBuilder(
                        context.applicationContext,
                        UserImageDatabase::class.java,
                        "userImages.db"
                    ).build()
                }
            }
            return INSTANCE!!
        }
    }
}