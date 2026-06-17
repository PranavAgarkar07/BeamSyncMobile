package com.example.beamsyncmobile.data.history

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File

object HistoryRepository {
    private const val FILE_NAME = "transfer_history.json"
    private var cache: MutableList<TransferRecord>? = null
    private var nextId: Long = 1L

    fun getHistory(context: Context): List<TransferRecord> {
        if (cache == null) load(context)
        return cache?.toList() ?: emptyList()
    }

    fun getSendHistory(context: Context): List<TransferRecord> {
        return getHistory(context).filter { it.direction == TransferDirection.SEND }
    }

    fun getReceiveHistory(context: Context): List<TransferRecord> {
        return getHistory(context).filter { it.direction == TransferDirection.RECEIVE }
    }

    fun addRecord(context: Context, record: TransferRecord) {
        if (cache == null) load(context)
        cache?.add(record)
        if (record.id >= nextId) nextId = record.id + 1
        save(context)
    }

    fun addRecord(
        context: Context,
        fileName: String,
        fileSize: Long,
        direction: TransferDirection,
        status: TransferStatus,
    ) {
        if (cache == null) load(context)
        val record = TransferRecord(
            id = nextId++,
            fileName = fileName,
            fileSize = fileSize,
            direction = direction,
            status = status,
            timestamp = System.currentTimeMillis(),
        )
        cache?.add(record)
        save(context)
    }

    fun deleteRecord(context: Context, id: Long) {
        cache?.removeAll { it.id == id }
        save(context)
    }

    fun clearHistory(context: Context) {
        cache = mutableListOf()
        nextId = 1L
        save(context)
    }

    private fun load(context: Context) {
        val file = File(context.filesDir, FILE_NAME)
        if (file.exists()) {
            val json = file.readText()
            val type = object : TypeToken<List<TransferRecord>>() {}.type
            val items: List<TransferRecord> = Gson().fromJson(json, type) ?: emptyList()
            cache = items.toMutableList()
            nextId = (items.maxOfOrNull { it.id } ?: 0) + 1
        } else {
            cache = mutableListOf()
            nextId = 1L
        }
    }

    private fun save(context: Context) {
        val file = File(context.filesDir, FILE_NAME)
        val json = Gson().toJson(cache ?: emptyList<TransferRecord>())
        file.writeText(json)
    }
}
