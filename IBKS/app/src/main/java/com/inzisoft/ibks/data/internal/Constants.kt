package com.inzisoft.ibks.data.internal

object ElectronicDocConstants {

    object WebData {
        const val DOC_ID = "edocId"             // 전자문서키
        const val PRODUCT_CODE = "prod_no"      // 종목코드
    }

    object IBKConfig {
        const val TX_TIME = "txTime"
        const val INDEX_08 = "index08"
        const val INDEX_09 = "index09"
        const val NXT_DAY_BUY_YN = "nxtDayBuyYn"
        const val CNS_STT_YN = "cnsSttYn"
        const val SEAL_REG_YN = "sealRegYn"
        const val MEMO = "memo"

        const val DOC_INFO = "docInfo"
        const val TERMINAL_INFO = "terminalInfo"
        const val ADD_SCAN_INFO = "addScanInfo"

        const val ODS_ADD_DOC = "ODS_ADD_DOC"               // 지류 서식
        const val ODS_CNS_STT_YN = "ODS_CNS_STT_YN"         // 숙려 여부
        const val ODS_MEMO = "ODS_MEMO"                     // 메모
    }

}