package com.example.mobiilr

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

// Võrguleht: GET kasutajad reqres.in serverist (tekst + pildid) ja POST sisselogimine.
@Composable
fun NetworkScreen(onBack: () -> Unit, viewModel: NetworkViewModel = viewModel()) {
    val context = LocalContext.current
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var email by remember { mutableStateOf("eve.holt@reqres.in") } // reqres.in testkasutaja
    var password by remember { mutableStateOf("cityslicka") }

    // Lehe avamisel too kasutajad (ainult esimesel korral)
    LaunchedEffect(Unit) {
        if (state.users.isEmpty()) viewModel.loadUsers(isOnline(context))
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Button(onClick = onBack) { Text("Back") }
            Spacer(Modifier.size(8.dp))
            Button(onClick = { viewModel.loadUsers(isOnline(context)) }) { Text("Reload") }
        }

        // ---- POST: sisselogimine ----
        Text("POST $BASE_URL/login", style = MaterialTheme.typography.labelMedium)
        OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(
            value = password, onValueChange = { password = it }, label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth()
        )
        Button(onClick = { viewModel.login(email, password, isOnline(context)) }) { Text("Login") }
        state.token?.let { Text("Logged in, token: $it") }
        state.loginError?.let { Text(it, color = MaterialTheme.colorScheme.error) }

        // ---- GET: kasutajad ----
        Text("GET $BASE_URL/users?page=2", style = MaterialTheme.typography.labelMedium)
        when {
            state.loading -> CircularProgressIndicator()
            state.error != null -> Text(state.error!!, color = MaterialTheme.colorScheme.error)
        }
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(state.users, key = { it.id }) { user -> UserRow(user) }
        }
    }
}

@Composable
private fun UserRow(user: User) {
    // Profiilipilt laaditakse eraldi GET päringuga
    val avatar by produceState<Bitmap?>(null, user.avatarUrl) { value = downloadImage(user.avatarUrl) }
    Row(verticalAlignment = Alignment.CenterVertically) {
        avatar?.let {
            Image(it.asImageBitmap(), contentDescription = user.name, modifier = Modifier.size(48.dp).clip(CircleShape))
        } ?: Spacer(Modifier.size(48.dp))
        Column(modifier = Modifier.padding(start = 12.dp)) {
            Text(user.name)
            Text(user.email, style = MaterialTheme.typography.bodySmall)
        }
    }
}
