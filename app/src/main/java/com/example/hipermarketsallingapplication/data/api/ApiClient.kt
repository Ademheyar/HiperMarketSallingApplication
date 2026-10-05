package com.example.hipermarketsallingapplication.data.api

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

class ApiClient(private val context: Context) {

    suspend fun checkUrlConnection(urlStr: String): Boolean = withContext(Dispatchers.IO) {
        if (!isNetworkAvailable() || urlStr.isBlank()) return@withContext false
        try {
            val url = URL(urlStr)
            val connection = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = 3000
                readTimeout = 3000
                requestMethod = "GET"
            }
            val responseCode = connection.responseCode
            responseCode in 200..299
        } catch (e: Exception) {
            false
        }
    }

    fun isNetworkAvailable(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}
