package com.inzisoft.ibks.data.internal

import android.content.Context
import androidx.annotation.Keep
import androidx.datastore.core.DataStore
import androidx.datastore.core.Serializer
import androidx.datastore.dataStore
import com.google.gson.Gson
import com.inzisoft.ibks.util.log.QLog
import kotlinx.coroutines.flow.Flow
import java.io.InputStream
import java.io.OutputStream
import javax.inject.Inject

class PaperlessSaveInfoDataSource @Inject constructor(private val context: Context) {
    private val Context.paperlessSaveInfo: DataStore<PaperlessSaveInfo> by dataStore(
        fileName = "paperless_save_info.pb",
        serializer = PaperlessSaveInfoSerializer
    )

    fun getPaperlessSaveInfo(): Flow<PaperlessSaveInfo> {
        return context.paperlessSaveInfo.data
    }

    suspend fun addPaperlessSaveInfo(paperlessSaveInfoData: PaperlessSaveInfoData) {
        context.paperlessSaveInfo.updateData { paperlessSaveInfo ->
            val originList = paperlessSaveInfo.paperlessSaveInfoList
            val updateList = mutableListOf<PaperlessSaveInfoData>()
            var hasContain = false
            originList.forEach {
                if (it.businessLogic == paperlessSaveInfoData.businessLogic) {
                    updateList.add(paperlessSaveInfoData)
                    hasContain = true
                } else {
                    updateList.add(it)
                }
            }
            if (!hasContain) {
                updateList.add(paperlessSaveInfoData)
            }

            PaperlessSaveInfo(updateList)
        }
    }

    suspend fun removePaperlessSaveInfo(removeList: List<String>?) {
        context.paperlessSaveInfo.updateData { paperlessSaveInfo ->
            val originList = paperlessSaveInfo.paperlessSaveInfoList

            val updateList = originList.toMutableList().filter { removeList?.contains(it.businessLogic) ?: true }

            PaperlessSaveInfo(updateList)
        }
    }

    suspend fun clearPaperlessSaveInfo() {
        QLog.i("clearPaperlessSaveInfo 호출됨.")
        context.paperlessSaveInfo.updateData {
            PaperlessSaveInfoSerializer.defaultValue
        }
    }

    object PaperlessSaveInfoSerializer : Serializer<PaperlessSaveInfo> {
        override val defaultValue: PaperlessSaveInfo
            get() = PaperlessSaveInfo(mutableListOf())

        override fun readFrom(input: InputStream): PaperlessSaveInfo {
            return Gson().fromJson(
                input.readBytes().decodeToString(),
                PaperlessSaveInfo::class.java
            )
        }

        override fun writeTo(t: PaperlessSaveInfo, output: OutputStream) {
            output.write(Gson().toJson(t).toByteArray())
        }
    }

    @Keep
    data class PaperlessSaveInfo(
        val paperlessSaveInfoList: List<PaperlessSaveInfoData>
    )

    data class PaperlessSaveInfoData(
        val businessLogic: String,
        val title: String,
        val saveFormDataList: MutableList<SaveFormData>
    )

    data class SaveFormData(
        val formId: String,
        val formName: String,
        val formVersion: String,
        val formPageCount: Int,
        val saveFileDirPath: String
    )
}