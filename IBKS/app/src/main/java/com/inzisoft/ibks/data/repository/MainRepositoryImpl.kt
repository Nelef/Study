package com.inzisoft.ibks.data.repository

import com.inzisoft.ibks.data.remote.BaseRemoteDataSource
import com.inzisoft.ibks.data.remote.api.EdsApiService
import com.inzisoft.ibks.data.remote.api.ProductApiService
import com.inzisoft.ibks.data.remote.model.*
import com.inzisoft.ibks.data.web.DocsProductCode
import com.inzisoft.ibks.data.web.EntryInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import okhttp3.ResponseBody
import javax.inject.Inject

class MainRepositoryImpl @Inject constructor(
    private val localRepository: LocalRepository,
    private val edsApiService: EdsApiService,
    private val productApiService: ProductApiService,
    private val remoteDataSource: BaseRemoteDataSource
) : MainRepository {


    override suspend fun generateEntryId(entryInfo: EntryInfo): Flow<ApiResult<ResponseEntryIdData>> {

        val userInfo = localRepository.getUserInfo().first()

        val requestEntryIdData = RequestEntryIdData(
            code = entryInfo.code,
            subCode = if (entryInfo.subCode.isNullOrEmpty()) "00000" else entryInfo.subCode,
            officeCd = userInfo.brnNo,
            customerName = entryInfo.custName ?: "",
            userId = userInfo.sabun
        )

        return remoteDataSource.apiCall {
            edsApiService.generateEntryId(requestEntryIdData)
        }
    }

    override suspend fun getDocsList(prdCd: DocsProductCode): Flow<ApiResult<List<DocsResponse>>> {
        return remoteDataSource.apiCall {
            val productCode = prdCd.productCode
            productApiService.docs(productCode)
        }
    }

    override suspend fun downloadEform(
        provId: String,
        folderName: String,
        fileName: String
    ): Flow<ApiResult<ByteArray>> {
        return remoteDataSource.download {
            edsApiService.downloadEform(provId, folderName, fileName)
        }
    }

    override suspend fun downloadForm(provId: String, path: String): Flow<ApiResult<ByteArray>> {
        return remoteDataSource.download {
            edsApiService.downloadForm("$provId/$path")
        }
    }
}