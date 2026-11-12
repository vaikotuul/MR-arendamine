package com.example.mobiilr

import android.graphics.Bitmap
import android.util.Size
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// Galerii: näitab kõiki galeriisse salvestatud pilte. Pildid loetakse iga kord failisüsteemist,
// seega on need olemas ka pärast rakenduse taaskäivitust.
@Composable
fun GalleryScreen(onBack: () -> Unit, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    // Väikesed eelvaated (thumbnail) loeme taustalõimes, et ekraan ei jääks seisma
    val thumbnails by produceState<List<Bitmap>?>(null) {
        value = withContext(Dispatchers.IO) {
            loadPhotos(context).map { context.contentResolver.loadThumbnail(it, Size(300, 300), null) }
        }
    }

    Column(modifier = modifier.fillMaxSize().padding(8.dp)) {
        Text(
            when (val list = thumbnails) {
                null -> "Loading..."
                else -> "Photos: ${list.size} (saved in $PHOTO_FOLDER)"
            }
        )
        Button(onClick = onBack) { Text("Back") }
        LazyVerticalGrid(columns = GridCells.Fixed(3)) {
            items(thumbnails.orEmpty()) { bitmap ->
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "Saved photo",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.padding(2.dp).aspectRatio(1f)
                )
            }
        }
    }
}