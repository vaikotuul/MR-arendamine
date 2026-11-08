package com.example.mobiilr

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

// Piltide püsiv salvestamine telefoni galeriisse.
// Kaamera kirjutab pildi ajutiselt cache'i (photo.jpg), siit kopeeritakse see kausta Pictures/Mobiilr.

const val GALLERY_FOLDER = "Mobiilr"
const val GALLERY_RELATIVE_PATH = "Pictures/$GALLERY_FOLDER"

fun galleryPath(fileName: String): String = "$GALLERY_RELATIVE_PATH/$fileName"

// Unikaalne nimi pildistamise aja järgi: IMG_20261108_143005.jpg
fun photoFileName(timeMillis: Long, timeZone: TimeZone = TimeZone.getDefault()): String {
    val format = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).apply { this.timeZone = timeZone }
    return "IMG_${format.format(Date(timeMillis))}.jpg"
}

// Android 10+ (API 29) ei vaja luba, Android 7–9 vajab WRITE_EXTERNAL_STORAGE luba
fun needsStoragePermission(sdkInt: Int = Build.VERSION.SDK_INT): Boolean = sdkInt < Build.VERSION_CODES.Q

data class SavedPhoto(val uri: Uri, val path: String)

// Kopeerib pildi muutmata kujul galeriisse. Tagastab null, kui ei õnnestunud. Kutsu Dispatchers.IO peal.
fun savePhotoToGallery(
    context: Context,
    source: File,
    fileName: String = photoFileName(System.currentTimeMillis())
): SavedPhoto? {
    if (!source.exists() || source.length() == 0L) return null
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) saveWithMediaStore(context, source, fileName)
    else saveLegacy(context, source, fileName)
}

// Android 10+: MediaStore. IS_PENDING = 1 peidab pildi galeriist, kuni kopeerimine käib.
private fun saveWithMediaStore(context: Context, source: File, fileName: String): SavedPhoto? {
    val resolver = context.contentResolver
    val values = ContentValues().apply {
        put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
        put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        put(MediaStore.Images.Media.RELATIVE_PATH, GALLERY_RELATIVE_PATH)
        put(MediaStore.Images.Media.IS_PENDING, 1)
    }
    val collection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
    val uri = resolver.insert(collection, values) ?: return null
    return try {
        resolver.openOutputStream(uri)?.use { out -> source.inputStream().use { it.copyTo(out) } }
            ?: error("Ei saanud faili avada")
        resolver.update(uri, ContentValues().apply { put(MediaStore.Images.Media.IS_PENDING, 0) }, null, null)
        SavedPhoto(uri, galleryPath(resolver.displayName(uri) ?: fileName))
    } catch (e: Exception) {
        resolver.delete(uri, null, null) // ei jäta poolikut pilti galeriisse
        null
    }
}

// Android 7–9: kirjutame otse kausta ja meediaskanner lisab pildi galeriisse
@Suppress("DEPRECATION")
private fun saveLegacy(context: Context, source: File, fileName: String): SavedPhoto? = try {
    val dir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), GALLERY_FOLDER)
    dir.mkdirs()
    var target = File(dir, fileName)
    var n = 1
    while (target.exists()) target = File(dir, fileName.removeSuffix(".jpg") + "_${n++}.jpg")
    source.copyTo(target)
    MediaScannerConnection.scanFile(context, arrayOf(target.path), arrayOf("image/jpeg"), null)
    SavedPhoto(Uri.fromFile(target), galleryPath(target.name))
} catch (e: Exception) {
    null
}

private fun ContentResolver.displayName(uri: Uri): String? =
    query(uri, arrayOf(MediaStore.Images.Media.DISPLAY_NAME), null, null, null)?.use { c ->
        if (c.moveToFirst()) c.getString(0) else null
    }