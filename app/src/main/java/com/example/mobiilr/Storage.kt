package com.example.mobiilr

import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import java.io.File

// Kõik andmete salvestamine ühes kohas.
// 1) seaded -> SharedPreferences (võti-väärtus)
// 2) tekstifail -> rakenduse privaatne kaust (filesDir/note.txt)
// 3) pildid -> telefoni galerii (MediaStore, kaust Pictures/Mobiilr)

//1. Seaded (SharedPreferences)

private fun prefs(context: Context) = context.getSharedPreferences("settings", Context.MODE_PRIVATE)

fun loadDarkTheme(context: Context): Boolean = prefs(context).getBoolean("dark_theme", false)

fun saveDarkTheme(context: Context, dark: Boolean) {
    prefs(context).edit().putBoolean("dark_theme", dark).apply()
}

//2. Tekstifail (filesDir)

fun noteFile(context: Context) = File(context.filesDir, "note.txt")

fun loadNote(context: Context): String = noteFile(context).let { if (it.exists()) it.readText() else "" }

fun saveNote(context: Context, text: String) {
    noteFile(context).writeText(text)
}

//3. Pildid (MediaStore -> galerii)

const val PHOTO_FOLDER = "Pictures/Mobiilr"

// Kopeerib kaamera tehtud faili galeriisse. Iga pilt saab uue nime, nii et midagi üle ei kirjutata.
fun savePhoto(context: Context, photo: File): Uri? {
    val values = ContentValues().apply {
        put(MediaStore.Images.Media.DISPLAY_NAME, "IMG_${System.currentTimeMillis()}.jpg")
        put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        put(MediaStore.Images.Media.RELATIVE_PATH, PHOTO_FOLDER)
    }
    val uri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values) ?: return null
    context.contentResolver.openOutputStream(uri)?.use { out -> photo.inputStream().use { it.copyTo(out) } }
    return uri
}

// Loeb galeriist kõik meie rakenduse pildid, uusimad eespool.
fun loadPhotos(context: Context): List<Uri> {
    val photos = mutableListOf<Uri>()
    context.contentResolver.query(
        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
        arrayOf(MediaStore.Images.Media._ID),
        "${MediaStore.Images.Media.RELATIVE_PATH} = ?",
        arrayOf("$PHOTO_FOLDER/"),
        "${MediaStore.Images.Media.DATE_ADDED} DESC"
    )?.use { cursor ->
        while (cursor.moveToNext()) {
            photos += ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, cursor.getLong(0))
        }
    }
    return photos
}