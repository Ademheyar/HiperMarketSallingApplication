package com.example.hipermarketsallingapplication.data.api

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class ApiFetcher(private val context: Context) {
    private val apiClient = ApiClient(context)

    suspend fun fetchData(baseUrl: String, endpoint: String): String? = withContext(Dispatchers.IO) {
        val fullUrl = if (baseUrl.endsWith("/")) "$baseUrl$endpoint" else "$baseUrl/$endpoint"
        if (!apiClient.checkUrlConnection(fullUrl)) return@withContext null
        try {
            val url = URL(fullUrl)
            val connection = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = 5000
                readTimeout = 5000
                requestMethod = "GET"
            }
            if (connection.responseCode == 200) {
                BufferedReader(InputStreamReader(connection.inputStream)).use { reader ->
                    val response = StringBuilder()
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        response.append(line)
                    }
                    response.toString()
                }
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
}
