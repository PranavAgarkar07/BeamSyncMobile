package com.example.beamsyncmobile.network

import android.content.ContentResolver
import android.net.Uri
import android.provider.OpenableColumns
import java.io.InputStream
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.ConnectionPool
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

data class SenderFileInfo(
    val name: String,
    val sizeText: String,
    val downloadUrl: String,
)

object NetworkClient {
    val client: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectionPool(ConnectionPool(5, 30, TimeUnit.SECONDS))
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(120, TimeUnit.SECONDS)
            .build()
    }
}

class BeamSyncClient {

    private val client get() = NetworkClient.client
    @Volatile
    var currentCall: okhttp3.Call? = null

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

            currentCall = client.newCall(request)
            val response = currentCall!!.execute()
            currentCall = null
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
        val tokenRegex = Regex("""token["'\s]*[:=]["'\s]*["']?([0-9a-fA-F]{32})["']?""", RegexOption.IGNORE_CASE)
        val match = tokenRegex.find(body)
        return match?.groupValues?.get(1)
    }

    suspend fun connectToSender(url: String): Result<ServerConnection> = withContext(Dispatchers.IO) {
        try {
            val cleanUrl = url.trim()
            val withoutScheme = cleanUrl.substringAfter("://")
            val hostPort = withoutScheme.substringBefore("/")
            val host = hostPort.substringBefore(":")
            val port = hostPort.substringAfter(":", "80")
                .substringBefore("?")
                .toIntOrNull() ?: 80
            val scheme = if (cleanUrl.startsWith("https://")) "https" else "http"
            val baseUrl = "$scheme://$host:$port"
            val token = fetchToken(baseUrl) ?: ""
            Result.success(ServerConnection(scheme, host, port, token))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun disconnect(conn: ServerConnection) {
        try {
            val request = Request.Builder()
                .url("${conn.scheme}://${conn.host}:${conn.port}/cancel?token=${conn.token}")
                .get()
                .build()
            client.newCall(request).enqueue(object : okhttp3.Callback {
                override fun onFailure(call: okhttp3.Call, e: java.io.IOException) { }
                override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                    response.close()
                }
            })
        } catch (_: Exception) { }
    }

    suspend fun fetchSenderFileList(conn: ServerConnection): Result<List<SenderFileInfo>> = withContext(Dispatchers.IO) {
        try {
            val baseUrl = "${conn.scheme}://${conn.host}:${conn.port}"
            val request = Request.Builder().url(baseUrl).get().build()
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("HTTP ${response.code}"))
            }
            val html = response.body?.string() ?: return@withContext Result.failure(Exception("Empty response"))

            val token = conn.token.ifEmpty {
                val tokenRegex = Regex("""TOKEN\s*=\s*"([^"]+)"""")
                tokenRegex.find(html)?.groupValues?.get(1) ?: ""
            }

            val files = mutableListOf<SenderFileInfo>()
            val cardRegex = Regex(
                """<div class="file-card"[^>]*id="card-([^"]+)"[^>]*>.*?id="name-\1"[^>]*>([^<]+)</div>.*?<div class="file-card__size">([^<]+)</div>""",
                setOf(RegexOption.DOT_MATCHES_ALL)
            )
            for (match in cardRegex.findAll(html)) {
                val cardId = match.groupValues[1]
                val name = match.groupValues[2].trim()
                val sizeText = match.groupValues[3].trim()
                val downloadUrl = if (cardId.startsWith("multi-")) {
                    val idx = cardId.removePrefix("multi-")
                    "$baseUrl/download/$idx?token=$token"
                } else {
                    "$baseUrl/download?token=$token"
                }
                files.add(SenderFileInfo(name, sizeText, downloadUrl))
            }

            Result.success(files)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun downloadFile(
        url: String,
        outputStream: java.io.OutputStream,
        onProgress: (Long, Long) -> Boolean,
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder().url(url).get().build()
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("HTTP ${response.code}"))
            }

            val body = response.body ?: return@withContext Result.failure(Exception("Empty response"))
            val totalBytes = body.contentLength()
            val inputStream = body.byteStream()
            val buffer = ByteArray(8192)
            var bytesRead: Int
            var totalRead = 0L

            outputStream.use { output ->
                while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                    output.write(buffer, 0, bytesRead)
                    totalRead += bytesRead
                    if (totalBytes > 0) {
                        if (!onProgress(totalRead, totalBytes)) {
                            inputStream.close()
                            return@withContext Result.failure(Exception("Cancelled"))
                        }
                    }
                }
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
