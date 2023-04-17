package com.inzisoft.ibks.view.fragment.paperelss

import com.inzisoft.ibks.data.internal.Thumbnail

interface DocInterface {
    /**
     * 현재 열려있는 서식들의 썸네일 정보를 추출한다.
     *
     * @param result 썸네일 정보
     * @receiver
     */
    fun loadThumbnail(result: (thumbnailList: List<Thumbnail>) -> Unit)

    /**
     * 썸네일 열기
     *
     */
    fun openThumbnail()

    /**
     * 썸네일 닫기
     *
     */
    fun closeThumbnail()

    /**
     *
     *
     * @return 썸네일이 열려있는지 여부
     */
    fun isOpenThumbnail() : Boolean

    /**
     * 페이지 정보 요청
     *
     */
    fun getPageInfo()

    /**
     * 페이지 이동
     *
     * @param index page index 0 ~
     */
    fun goPage(index: Int)
}