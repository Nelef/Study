package com.inzisoft.ibks.data.repository

import com.google.gson.Gson
import com.inzisoft.ibks.Constants.TEMP_DATA_API_MULTIPART_JSON_NAME
import com.inzisoft.ibks.data.internal.DocImageData
import com.inzisoft.ibks.data.remote.BaseRemoteDataSource
import com.inzisoft.ibks.data.remote.api.EdsApiService
import com.inzisoft.ibks.data.remote.api.IdentityApiService
import com.inzisoft.ibks.data.remote.model.ApiResult
import com.inzisoft.ibks.data.remote.model.AuthRequest
import com.inzisoft.ibks.data.remote.model.SendAttachImageRequest
import kotlinx.coroutines.flow.Flow
import okhttp3.MultipartBody
import java.io.File
import javax.inject.Inject

class CameraRepositoryImpl @Inject constructor(
    private val identityApiService: IdentityApiService,
    private val edsApiService: EdsApiService,
    private val remoteDataSource : BaseRemoteDataSource
) : CameraRepository {

    override suspend fun verifyIdcard(authRequest: AuthRequest): Flow<ApiResult<Unit>> {
        return remoteDataSource.apiCall {
            identityApiService.verifyIdcard(authRequest)
        }
    }

    // 임시저장(첨부서류)
    override suspend fun sendAttachedImage(
        sendAttachImageRequest: SendAttachImageRequest,
        docImageDataList: List<DocImageData>,
        isSendCacheImage: Boolean
    ): Flow<ApiResult<Unit>> {
        val multipartList = mutableListOf<MultipartBody.Part>()
        val attachedImageInfo = remoteDataSource.toMultipartJson(
            TEMP_DATA_API_MULTIPART_JSON_NAME,
            Gson().toJson(sendAttachImageRequest)
        )
        multipartList.add(attachedImageInfo)

        docImageDataList.forEach { docImageData ->
            val sendImageFile =
                if (isSendCacheImage) {
                    File(docImageData.getCacheAvailableImagePath())
                } else {
                    File(docImageData.realImagePath)
                }

            val sendImageFileName =
                if (isSendCacheImage) {
                    File(docImageData.cacheOriginImagePath).name
                } else {
                    File(docImageData.realImagePath).name
                }

            val imagePart = remoteDataSource.toMultipartFile(
                name = "file",
                fileName = sendImageFileName,
                data = sendImageFile.readBytes(),
                contentType = "application/octet-stream"
            )

            multipartList.add(imagePart)
        }

        return remoteDataSource.apiCall {
            edsApiService.tempAttach(multipartList)
        }
    }
}