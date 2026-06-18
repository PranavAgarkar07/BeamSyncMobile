package com.example.beamsyncmobile.data.history

import androidx.compose.runtime.Immutable

enum class TransferDirection { SEND, RECEIVE }
enum class TransferStatus { SUCCESS, FAILED }

@Immutable
data class TransferRecord(
    val id: Long,
    val fileName: String,
    val fileSize: Long,
    val direction: TransferDirection,
    val status: TransferStatus,
    val timestamp: Long,
)
