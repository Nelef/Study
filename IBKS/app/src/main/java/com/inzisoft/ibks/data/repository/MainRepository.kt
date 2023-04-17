package com.inzisoft.ibks.data.repository

import com.inzisoft.ibks.data.remote.model.ApiResult
import com.inzisoft.ibks.data.remote.model.DocsResponse
import com.inzisoft.ibks.data.remote.model.ResponseEntryIdData
import com.inzisoft.ibks.data.web.DocsProductCode
import com.inzisoft.ibks.data.web.EntryInfo
import kotlinx.coroutines.flow.Flow

interface MainRepository {

    suspend fun generateEntryId(entryInfo: EntryInfo): Flow<ApiResult<ResponseEntryIdData>>

    suspend fun getDocsList(prdCd: DocsProductCode): Flow<ApiResult<List<DocsResponse>>>

    suspend fun downloadEform(
        provId: String,
        folderName: String,
        fileName: String
    ): Flow<ApiResult<ByteArray>>

    suspend fun downloadForm(
        provId: String = "IS",
        path: String
    ): Flow<ApiResult<ByteArray>>

}