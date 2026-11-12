package com.example.mobiilr

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL
import java.net.UnknownHostException

// Suhtlus välisserveriga: REST API https://reqres.in (tasuta testserver).
// Protokoll: HTTPS, andmed JSON-is. Kasutame Androidi sisseehitatud HttpURLConnection'it ja org.json'it –
// lisateeke pole vaja. Kõik päringud käivad taustalõimes (Dispatchers.IO), mitte UI-lõimes.

const val BASE_URL = "https://reqres.in/api"

data class User(val id: Int, val name: String, val email: String, val avatarUrl: String)

/** Serveri vastus, mis polnud 2xx (nt 400 Bad Request). [serverMessage] on serveri "error" väli. */
class HttpException(val code: Int, val serverMessage: String?) : IOException("HTTP $code")

// ---------- Päringud ----------

/** GET /users?page=2 – kasutajate nimekiri. */
suspend fun fetchUsers(page: Int = 2): List<User> = withContext(Dispatchers.IO) {
    parseUsers(request("GET", "$BASE_URL/users?page=$page"))
}

/** POST /login – saadab e-posti ja parooli JSON-ina, tagastab tokeni. */
suspend fun postLogin(email: String, password: String): String = withContext(Dispatchers.IO) {
    parseToken(request("POST", "$BASE_URL/login", body = loginBody(email, password)))
}

/** GET pildi URL – laadib kasutaja profiilipildi. Vea korral null (nimekiri töötab ka ilma pildita). */
suspend fun downloadImage(url: String): Bitmap? = withContext(Dispatchers.IO) {
    try {
        val connection = URL(url).openConnection() as HttpURLConnection
        try {
            connection.inputStream.use { BitmapFactory.decodeStream(it) }
        } finally {
            connection.disconnect()
        }
    } catch (e: IOException) {
        null
    }
}

// Üks HTTP päring. Tagastab vastuse keha tekstina või viskab vea.
private fun request(method: String, url: String, body: String? = null): String {
    val connection = URL(url).openConnection() as HttpURLConnection
    try {
        connection.requestMethod = method
        connection.connectTimeout = 10_000
        connection.readTimeout = 10_000
        connection.setRequestProperty("Accept", "application/json")
        if (body != null) {
            connection.doOutput = true
            connection.setRequestProperty("Content-Type", "application/json")
            connection.outputStream.use { it.write(body.toByteArray()) }
        }

        val code = connection.responseCode
        if (code !in 200..299) {
            // Vigase vastuse keha on errorStream'is, nt {"error":"Missing password"}
            val errorBody = connection.errorStream?.bufferedReader()?.use { it.readText() }
            throw HttpException(code, parseError(errorBody))
        }
        return connection.inputStream.bufferedReader().use { it.readText() }
    } finally {
        connection.disconnect()
    }
}

// ---------- JSON ----------

fun parseUsers(json: String): List<User> {
    val data = JSONObject(json).getJSONArray("data")
    return List(data.length()) { i ->
        val u = data.getJSONObject(i)
        User(
            id = u.getInt("id"),
            name = "${u.optString("first_name")} ${u.optString("last_name")}".trim(),
            email = u.optString("email"),
            avatarUrl = u.optString("avatar")
        )
    }
}

fun parseToken(json: String): String = JSONObject(json).getString("token")

fun parseError(json: String?): String? =
    try { json?.let { JSONObject(it).optString("error").ifEmpty { null } } } catch (e: Exception) { null }

fun loginBody(email: String, password: String): String =
    JSONObject().put("email", email).put("password", password).toString()

// ---------- Vead ja võrguolek ----------

/** Kasutajale arusaadav veateade. */
fun errorMessage(e: Exception): String = when (e) {
    is HttpException -> "Server error ${e.code}" + (e.serverMessage?.let { ": $it" } ?: "")
    is UnknownHostException -> "Server not found – check internet connection"
    is SocketTimeoutException -> "Server did not respond in time"
    is IOException -> "Network error: ${e.message}"
    else -> "Unexpected error: ${e.message}"
}

/** Kas seadmel on internetiühendus (Wi-Fi või mobiilne andmeside). */
fun isOnline(context: Context): Boolean {
    val cm = context.getSystemService(ConnectivityManager::class.java)
    val capabilities = cm.getNetworkCapabilities(cm.activeNetwork) ?: return false
    return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
}
