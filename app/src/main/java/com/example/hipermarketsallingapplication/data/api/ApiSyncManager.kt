package com.example.hipermarketsallingapplication.data.api

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

class ApiSyncManager(private val context: Context) {
    private val apiClient = ApiClient(context)

    suspend fun postData(baseUrl: String, endpoint: String, jsonPayload: String): Boolean = withContext(Dispatchers.IO) {
        val fullUrl = if (baseUrl.endsWith("/")) "$baseUrl$endpoint" else "$baseUrl/$endpoint"
        if (!apiClient.checkUrlConnection(fullUrl)) {
            saveOfflineQueue(endpoint, jsonPayload)
            return@withContext false
        }
        try {
            val url = URL(fullUrl)
            val connection = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = 5000
                readTimeout = 5000
                requestMethod = "POST"
                doOutput = true
                setRequestProperty("Content-Type", "application/json; utf-8")
                setRequestProperty("Accept", "application/json")
            }
            OutputStreamWriter(connection.outputStream).use { writer ->
                writer.write(jsonPayload)
                writer.flush()
            }
            val responseCode = connection.responseCode
            responseCode in 200..299
        } catch (e: Exception) {
            saveOfflineQueue(endpoint, jsonPayload)
            false
        }
    }

    private fun saveOfflineQueue(endpoint: String, payload: String) {
        try {
            val file = File(context.filesDir, "offline_queue.text")
            file.appendText("$endpoint|$payload\n")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun syncOfflineQueue(baseUrl: String) = withContext(Dispatchers.IO) {
        try {
            val file = File(context.filesDir, "offline_queue.text")
            if (!file.exists()) return@withContext
            val lines = file.readLines()
            val remainingLines = mutableListOf<String>()

            for (line in lines) {
                val parts = line.split("|", limit = 2)
                if (parts.size == 2) {
                    val endpoint = parts[0]
                    val payload = parts[1]
                    val success = postData(baseUrl, endpoint, payload)
                    if (!success) {
                        remainingLines.add(line)
                    }
                }
            }
            file.writeText(remainingLines.joinToString("\n") + if (remainingLines.isNotEmpty()) "\n" else "")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
