package com.example.beamsyncmobile.network

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.webkit.MimeTypeMap
import com.google.gson.Gson
import java.io.File

sealed class SaveTarget {
    data class FileTarget(val file: File) : SaveTarget()
    data class MediaStoreTarget(val contentValues: ContentValues) : SaveTarget()
    data class DocumentTreeTarget(val treeUri: Uri, val fileName: String, val mimeType: String) : SaveTarget()
}

object SavePathManager {
    private const val PREFS_NAME = "beamsync_prefs"
    private const val KEY_SAVE_PATH = "save_path"
    private const val KEY_CUSTOM_PATH_URI = "custom_path_uri"
    private const val BEAMSYNC_DIR = "Download/BeamSync"
    private const val KEY_RECENT_CUSTOM_PATHS = "recent_custom_paths"
    private const val MAX_RECENT_PATHS = 5

    const val PATH_APP_DOWNLOADS = "app_downloads"
    const val PATH_PUBLIC_DOWNLOADS = "public_downloads"
    const val PATH_CUSTOM = "custom_path"

    fun getSaveTarget(context: Context, fileName: String): SaveTarget {
        val sanitized = sanitizeFileName(fileName)
        return when (getMode(context)) {
            PATH_PUBLIC_DOWNLOADS -> {
                when {
                    Build.VERSION.SDK_INT >= 30 -> {
                        val mimeType = guessMimeType(fileName)
                        val values = ContentValues().apply {
                            put(MediaStore.Downloads.DISPLAY_NAME, sanitized)
                            put(MediaStore.Downloads.MIME_TYPE, mimeType)
                            put(MediaStore.Downloads.IS_PENDING, 1)
                            put(MediaStore.Downloads.RELATIVE_PATH, BEAMSYNC_DIR)
                        }
                        SaveTarget.MediaStoreTarget(values)
                    }
                    else -> {
                        val dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                        val beamsyncDir = File(dir, "BeamSync")
                        beamsyncDir.mkdirs()
                        SaveTarget.FileTarget(File(beamsyncDir, sanitized))
                    }
                }
            }
            PATH_CUSTOM -> {
                val uriString = getCustomPathUri(context)
                if (uriString != null) {
                    val treeUri = Uri.parse(uriString)
                    SaveTarget.DocumentTreeTarget(treeUri, sanitized, guessMimeType(fileName))
                } else {
                    fallbackTarget(context, sanitized)
                }
            }
            else -> {
                fallbackTarget(context, sanitized)
            }
        }
    }

    private fun fallbackTarget(context: Context, sanitized: String): SaveTarget {
        val dir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
            ?: File(context.filesDir, "downloads")
        dir.mkdirs()
        return SaveTarget.FileTarget(File(dir, sanitized))
    }

    fun getMode(context: Context): String {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_SAVE_PATH, PATH_APP_DOWNLOADS) ?: PATH_APP_DOWNLOADS
    }

    fun setMode(context: Context, mode: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_SAVE_PATH, mode)
            .apply()
    }

    fun getCustomPathUri(context: Context): String? {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_CUSTOM_PATH_URI, null)
    }

    fun setCustomPathUri(context: Context, uri: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_CUSTOM_PATH_URI, uri)
            .apply()
        addRecentCustomPath(context, uri)
    }

    fun getRecentCustomPaths(context: Context): List<String> {
        val json = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_RECENT_CUSTOM_PATHS, null) ?: return emptyList()
        return try {
            Gson().fromJson(json, Array<String>::class.java).toList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun addRecentCustomPath(context: Context, uri: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val existing = getRecentCustomPaths(context).toMutableList()
        existing.remove(uri)
        existing.add(0, uri)
        val trimmed = existing.take(MAX_RECENT_PATHS)
        prefs.edit().putString(KEY_RECENT_CUSTOM_PATHS, Gson().toJson(trimmed)).apply()
    }

    fun getCustomPathLabel(uri: String): String = decodeLastPathSegment(uri)

    fun getModeLabel(context: Context, mode: String): String {
        return when (mode) {
            PATH_PUBLIC_DOWNLOADS -> if (Build.VERSION.SDK_INT >= 30) "Downloads/BeamSync" else "/Downloads/BeamSync"
            PATH_CUSTOM -> {
                val uri = getCustomPathUri(context)
                if (uri != null) decodeLastPathSegment(uri) else "Custom folder"
            }
            else -> "App private storage"
        }
    }

    private fun decodeLastPathSegment(uri: String): String {
        val segments = Uri.parse(uri).pathSegments
        return segments.lastOrNull()?.replace("%20", " ") ?: "Custom folder"
    }

    val modes: List<String> get() = listOf(PATH_APP_DOWNLOADS, PATH_PUBLIC_DOWNLOADS, PATH_CUSTOM)

    private fun sanitizeFileName(name: String): String {
        return name.replace(File.separatorChar, '_').replace("..", "").take(200)
    }

    private fun guessMimeType(fileName: String): String {
        val ext = fileName.substringAfterLast('.', "").lowercase()
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext) ?: "application/octet-stream"
    }
}
