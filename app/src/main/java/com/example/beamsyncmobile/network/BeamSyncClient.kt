package com.example.beamsyncmobile.network

import android.content.ContentResolver
import android.net.Uri
import android.provider.OpenableColumns
import java.io.InputStream
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okio.Buffer
import okio.BufferedSink
import okio.source
import org.json.JSONArray
import org.json.JSONObject

data class ServerConnection(
    val scheme: String,
    val host: String,
    val port: Int,
    val token: String,
)

data class UploadFileSpec(
    val name: String,
    val uri: Uri,
    val size: Long,
)

class BeamSyncClient {

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(120, TimeUnit.SECONDS)
        .build()

    suspend fun connect(url: String): Result<ServerConnection> = withContext(Dispatchers.IO) {
        try {
            val cleanUrl = url.trim()
            val isHttps = cleanUrl.startsWith("https://")
            val scheme = if (isHttps) "https" else "http"
            val withoutScheme = cleanUrl.substringAfter("://")
            val hostPort = withoutScheme.substringBefore("/")
            val host = hostPort.substringBefore(":")
            val port = hostPort.substringAfter(":", "80")
                .substringBefore("?")
                .toIntOrNull() ?: 80

            val tokenFromUrl = if ("token=" in cleanUrl) {
                cleanUrl.substringAfter("token=").substringBefore("&")
            } else null

            if (tokenFromUrl != null && tokenFromUrl.isNotBlank()) {
                return@withContext Result.success(
                    ServerConnection(scheme, host, port, tokenFromUrl)
                )
            }

            val baseUrl = "$scheme://$host:$port"
            val token = fetchToken(baseUrl)
            if (token == null) {
                return@withContext Result.failure(
                    Exception("Could not find session token. Check that BeamSync desktop is running.")
                )
            }
            Result.success(ServerConnection(scheme, host, port, token))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun heartbeat(conn: ServerConnection): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("${conn.scheme}://${conn.host}:${conn.port}/heartbeat?token=${conn.token}")
                .post("".toRequestBody(null))
                .build()
            val response = client.newCall(request).execute()
            if (response.isSuccessful) Result.success(Unit)
            else Result.failure(Exception("Heartbeat failed: ${response.code}"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun upload(
        conn: ServerConnection,
        files: List<UploadFileSpec>,
        contentResolver: ContentResolver,
        onFileProgress: (fileName: String, progress: Float) -> Unit,
        onFileComplete: (fileName: String) -> Unit,
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val manifest = JSONArray()
            for (f in files) {
                val entry = JSONObject().apply {
                    put("name", f.name)
                    put("size", f.size)
                }
                manifest.put(entry)
            }

            val bodyBuilder = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("beam_manifest", manifest.toString())

            for (f in files) {
                val stream = contentResolver.openInputStream(f.uri)
                    ?: return@withContext Result.failure(Exception("Cannot open: ${f.name}"))

                val fileBody = progressRequestBody(
                    stream = stream,
                    fileName = f.name,
                    totalBytes = f.size,
                    onProgress = onFileProgress,
                    onComplete = onFileComplete,
                )
                bodyBuilder.addFormDataPart("file", f.name, fileBody)
            }

            val requestBody = bodyBuilder.build()

            val request = Request.Builder()
                .url("${conn.scheme}://${conn.host}:${conn.port}/upload?token=${conn.token}")
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                val errBody = response.body?.string() ?: "HTTP ${response.code}"
                Result.failure(Exception("Upload failed: $errBody"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun progressRequestBody(
        stream: InputStream,
        fileName: String,
        totalBytes: Long,
        onProgress: (String, Float) -> Unit,
        onComplete: (String) -> Unit,
    ): RequestBody {
        return object : RequestBody() {
            override fun contentType() = "application/octet-stream".toMediaType()
            override fun contentLength() = totalBytes

            override fun writeTo(sink: BufferedSink) {
                val source = stream.source()
                val buffer = Buffer()
                var totalRead = 0L
                val chunkSize = 8192L

                while (true) {
                    val bytesRead = source.read(buffer, chunkSize)
                    if (bytesRead == -1L) break
                    sink.write(buffer, bytesRead)
                    totalRead += bytesRead
                    if (totalBytes > 0) {
                        onProgress(fileName, totalRead.toFloat() / totalBytes.toFloat())
                    }
                }

                source.close()
                stream.close()
                onComplete(fileName)
            }
        }
    }

    private fun fetchToken(baseUrl: String): String? {
        val request = Request.Builder()
            .url(baseUrl)
            .get()
            .build()

        val response = client.newCall(request).execute()
        if (!response.isSuccessful) return null

        val body = response.body?.string() ?: return null
        val tokenRegex = Regex("""token["'\s]*[:=]["'\s]*["']?([0-9a-fA-F]{32})["']?""")
        val match = tokenRegex.find(body)
        return match?.groupValues?.get(1)
    }
}
