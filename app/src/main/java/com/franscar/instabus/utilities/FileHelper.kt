package com.franscar.instabus.utilities

import android.app.Application
import android.content.Context
import java.io.File

class FileHelper {
    companion object {
        /*fun getTextFromResources(context: Context, resourceId: Int): String {
            return context.resources.openRawResource(resourceId).use { it ->
                it.bufferedReader().use {
                    it.readText()
                }
            }
        }*/

        fun saveToTextFile(app: Application, json: String?) {
            var file = File(app.cacheDir, "cache.json")
            file.writeText(json ?: "", Charsets.UTF_8)
        }

        fun readTextFile(app: Application): String? {
            var file = File(app.cacheDir, "cache.json")
            return if (file.exists()) {
                file.readText()
            } else null
        }
    }
}