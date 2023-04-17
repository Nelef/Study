package com.inzisoft.ibks.data.repository

import com.inzisoft.ibks.data.internal.DocImageData
import com.inzisoft.ibks.data.remote.model.ApiResult
import com.inzisoft.ibks.data.remote.model.AuthRequest
import com.inzisoft.ibks.data.remote.model.SendAttachImageRequest
import kotlinx.coroutines.flow.Flow

interface CameraRepository {
    // 신분증 진위확인(주민등록증/운전면허증)
    suspend fun verifyIdcard(authRequest: AuthRequest): Flow<ApiResult<Unit>>

    // 첨부문서 이미지(JPG 파일) 전송
    suspend fun sendAttachedImage(
        sendAttachImageRequest: SendAttachImageRequest,
        docImageDataList: List<DocImageData>,
        isSendCacheImage: Boolean
    ): Flow<ApiResult<Unit>>
}