package com.inzisoft.ibks.data.internal

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class PaperlessData(
    val businessLogic: String = DEFAULT_ID,
    val loadOptions: LoadOptions = LoadOptions.Write(),
) : Parcelable {
    companion object {
        const val DEFAULT_ID = "DEFAULT_ID"
    }
}

sealed class LoadOptions : Parcelable {

    /**
     * 기본 옵션
     *
     * @property formList 비즈로직 외에 외부에서 전달받는 서식 목록
     *              1. 비즈로직 openForms 목록이 있을경우 해당 서식 목록이 먼저 열린다.
     *              2. 비즈로직 openForms 목록이 없을경우 해당 서식 목록만 열린다.
     * @property data 서식에 세팅할 데이터
     * @property image 서식에 외부에서 생성한 이미지로 세팅할 fieldId 목록 (펜,인감)
     */
    @Parcelize
    data class Write(
        val formList: List<LoadForm>? = listOf(),
        val data: Map<String, String>? = mapOf(),
        val image: List<String>? = listOf(),
    ) : LoadOptions()

    @Parcelize
    data class Restore(val resultXmlDirPathList: List<String> = listOf()) : LoadOptions()

}

@Parcelize
data class LoadForm(val code: String, val name: String = "") : Parcelable