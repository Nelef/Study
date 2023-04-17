package com.inzisoft.ibks.view.fragment.paperelss

import android.graphics.Bitmap
import com.inzisoft.ibks.data.internal.AlertData
import com.inzisoft.paperless.data.PaperlessSaveData
import com.inzisoft.paperless.ods.data.TFieldInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlin.coroutines.CoroutineContext


interface PaperlessInterface {

    /**
     * 쓰기 모드로 변경
     *
     */
    fun onWriteMode()

    /**
     * 미리보기모드로 변경
     *
     */
    fun onPreviewMode()

    /**
     * 페이지 이동
     *
     * @param formCode 이동할 PDF code
     */
    fun goPage(formCode: String)

    /**
     * 형관펜 모드
     *
     */
    fun onHighlighter()

    /**
     * 지우개 모드
     *
     */
    fun onEraser()

    /**
     * 형광펜(지우개) 모드 해제
     *
     */
    fun turnOffHighlighter()

    /**
     * 인감 이미지 세팅 요청
     *
     * @param sealImage 인감이미지
     */
    fun setSealImage(sealImage: Bitmap?)

    /**
     * 작성이 완료 되었는지 여부 리턴
     *
     * @return true: 모든 필수 입력 작성완료
     */
    fun isFillOutComplete(result: (isComplete: Boolean) -> Unit)

    fun fillOutComplete(): Flow<Pair<Boolean, String>>

    /**
     * 작성하지 않은 필수 입력 필드로 포커싱 한다.
     *
     */
    fun fieldCallUpAtEmptyMustEntry()

    /**
     * 비즈로직으로 데이터 셋
     *
     * @param tFieldInfo key: tFieldId, value : String
     */
    fun setTFieldData(tFieldInfo: TFieldInfo)

    /**
     * 비즈로직에 구현된 모든 TerminalMethod 값을 리턴한다.
     *
     * @param result 터미널에 전달할 정보
     * @receiver
     */
    fun getTerminalInfo(result: (terminalInfo: Map<String, String>) -> Unit)

    /**
     * 비즈로직에 구현된 일부 key 값들에 해당하는 TerminalMethod 값을 리턴한다.
     *
     * @param tFieldIdList 배열 key: tFieldId
     * @param result 터미널에 전달할 정보
     * @receiver
     */
    fun getTerminalInfo(context: CoroutineContext = Dispatchers.Default, tFieldIdList: List<String>, result: (terminalInfo: Map<String, String>) -> Unit)

    /**
     * 결과 xml 생성
     *
     * @param savePath xml을 저장할 경로
     */
    fun saveResultXml(saveDirPath: String)

    /**
     * 전자문서 API Listener
     *
     */
    interface Listener {

        fun updateThumbnailState(isShow: Boolean)

        /**
         * 전자문서 오픈 완료
         *
         * @param businessLogic 비즈로직명
         * @param isSuccess true: 오픈 성공, false 오픈 실패
         */
        fun onLoadComplete(
            businessLogic: String,
            isSuccess: Boolean,
            formCount: Int,
            throwable: Throwable? = null
        )

        /**
         * 페이지 정보 전달
         *
         * @param businessLogic 비즈로직명
         * @param current 현재 페이지
         * @param total 전체 페이지
         */
        fun updatePage(businessLogic: String, current: Int, total: Int)

        /**
         * 인감 촬영 요청
         *
         */
        fun showSealCamera()

        /**
         * 알림 팝업 요청
         *
         * @param businessLogic 비즈로직명
         * @param alertData 팝업 데이터
         */
        fun alert(businessLogic: String, alertData: AlertData)

        /**
         * 에러 발생
         *
         * @param businessLogic 비즈로직명
         * @param throwable 에러
         *
         */
        fun onError(businessLogic: String, throwable: Throwable)

    }

    interface SaveResultListener {
        /**
         * 결과 xml 저장 결과 리턴
         *
         * @param businessLogic 비즈로직명
         * @param isSuccess true : 저장 성공, false : 저장 실패
         * @param saveDataList 저장한 서식 목록
         * @param throwable 저장 실패시 사유
         *
         */
        fun onSaveResultXml(
            businessLogic: String,
            isSuccess: Boolean,
            saveDataList: List<PaperlessSaveData> = listOf(),
            throwable: Throwable? = null
        )
    }
}