package com.example.beamsyncmobile.data.history

enum class TransferDirection { SEND, RECEIVE }
enum class TransferStatus { SUCCESS, FAILED }

data class TransferRecord(
    val id: Long,
    val fileName: String,
    val fileSize: Long,
    val direction: TransferDirection,
    val status: TransferStatus,
    val timestamp: Long,
)
