package com.inzisoft.ibks.data.repository

import com.inzisoft.ibks.data.internal.ResultData
import com.inzisoft.ibks.data.remote.model.ApiResult
import com.inzisoft.ibks.data.remote.model.DocsResponse
import com.inzisoft.ibks.data.web.AddedDocument
import com.inzisoft.ibks.data.web.EvidenceDocument
import com.inzisoft.ibks.data.web.SendImageDataInfo
import kotlinx.coroutines.flow.Flow

interface ElectronicDocRepository {

    /**
     * Make result zip file
     *
     * 1. 파일 복사
     *      1-1. 전자문서 결과 파일복사 및 정렬
     *      1-2. 증빙서류 복사
     *      1-3. 녹취파일 복사
     * 2. Data.cfg 생성   (전자문서 변환용 메타 파일)
     * 3. IBKS.cfg 생성   (IBKs 전달 메타 파일)
     * 4. exData.cfg 생성 (별도 메타 파일)
     * 5. zip
     *
     * 전자문서키.zip
     *
     *  1
     *  |- formCode_result.xml
     *  2
     *  |- formCode_result.xml
     *  ...
     *
     *  IMG
     *  |- IDV00001
     *        |- IDV00001_001_01.jpg
     *  |- ROV00001
     *        |- ROV00001_001_01.mp3
     *  ...
     *
     * Data.cfg
     * Ibks.cfg
     * {exFileName}.cfg
     *
     * @param entryId       전자문서키
     * @param resultData    전자문서 결과 정보
     * @param info          기본 정보
     * @param resultDocs    촬영완료한 증빙서류 목록
     * @param addedDocs     추가 서류
     * @param exFileName    별도 메타 파일명
     * @param exData        별도 메타 데이터
     * @param memo          메모
     * @return
     */
    suspend fun makeResultZipFile(
        entryId: String,
        resultData: ResultData,
        info: Map<String, String>,
        resultDocs: List<EvidenceDocument>,
        addedDocs: List<AddedDocument>,
        exFileName: String?,
        exData: Map<String, String>?,
        memo: String?,
        sendImageDataInfoList: List<SendImageDataInfo>,
        autoMailList: List<DocsResponse>
    ): Flow<ApiResult<Unit>>

    suspend fun transmitResultFile(entryId: String, memo: String): Flow<ApiResult<Unit>>

}