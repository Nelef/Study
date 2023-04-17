package com.inzisoft.ibks.data.internal

sealed class RecordState {
    object None : RecordState()
    object Recoding : RecordState()
    object Paused : RecordState()
}

data class RecordData(
    val title: String,
    val filePath: String,
    val fileName: String,
    val fileSize: Long,
    val recordLength: Long
)