package com.inzisoft.ibks.data.remote

import com.inzisoft.paperless.update.data.UpdateData
import kotlinx.coroutines.flow.Flow

interface UpdateFormDataSource {

    suspend fun checkVersion(): Flow<UpdateFormState>

    fun update(): Flow<UpdateFormState>

    suspend fun updateProductCode(productCode: String): Flow<UpdateFormState>

    fun getLocalFormDataList(): List<UpdateData>

    fun getFormCode(productCode: String): List<String>

    sealed class UpdateFormState {
        object Loading : UpdateFormState()
        data class OnError(val message: String) : UpdateFormState()
        data class Instruction(val list: List<String>) : UpdateFormState()
        object OnReadyUpdate : UpdateFormState()
        data class OnStart(val currentCount: Int, val totalCount: Int) : UpdateFormState()
        data class OnProgress(val progress: Float) : UpdateFormState()
        data class OnComplete(val currentCount: Int, val totalCount: Int) : UpdateFormState()
        object OnUpdateComplete : UpdateFormState()
    }

}