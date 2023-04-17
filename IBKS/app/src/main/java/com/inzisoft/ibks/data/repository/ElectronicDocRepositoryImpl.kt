package com.inzisoft.ibks.data.repository

import android.graphics.Bitmap
import com.google.gson.Gson
import com.inzisoft.ibks.AuthType
import com.inzisoft.ibks.Constants.TEMP_DATA_API_MULTIPART_JSON_NAME
import com.inzisoft.ibks.PathManager
import com.inzisoft.ibks.data.internal.*
import com.inzisoft.ibks.data.remote.BaseRemoteDataSource
import com.inzisoft.ibks.data.remote.UpdateFormDataSource
import com.inzisoft.ibks.data.remote.api.EdsApiService
import com.inzisoft.ibks.data.remote.model.ApiResult
import com.inzisoft.ibks.data.remote.model.DocsResponse
import com.inzisoft.ibks.data.remote.model.RequestResultInfo
import com.inzisoft.ibks.data.web.*
import com.inzisoft.ibks.util.FileUtils
import com.inzisoft.paperless.util.ImageController
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import okhttp3.MultipartBody
import java.io.File
import java.io.FileNotFoundException
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class ElectronicDocRepositoryImpl @Inject constructor(
    private val pathManager: PathManager,
    private val edsApiService: EdsApiService,
    private val remoteDataSource: BaseRemoteDataSource,
    private val updateFormDataSource: UpdateFormDataSource,
) : ElectronicDocRepository {

    override suspend fun makeResultZipFile(
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
    ): Flow<ApiResult<Unit>> = flow {
        emit(ApiResult.Loading())

        FileUtils.delete(pathManager.getResultRoot(entryId))

        // 1. 파일 복사
        copyResultForms(entryId, resultData)
        copyEvidenceDocs(entryId)
        saveSendImages(entryId, sendImageDataInfoList)
        val records = copyVoiceOfRecords(entryId)

        // 2. Data.cfg 생성
        val dataConfig = makeDataConfig(resultData.resultFormList)
        saveDataConfigFile(entryId, dataConfig)

        // 3. ibks.cfg 생성
        val defaultMap = makeDefaultValue(entryId)
        val iBKSConfig = makeIBKSConfig(
            entryId,
            info + defaultMap,
            resultData,
            resultDocs,
            records,
            addedDocs,
            sendImageDataInfoList,
            autoMailList,
            memo
        )
        saveDataConfigFile(entryId, iBKSConfig)

        // 4. exdata cfg 생성
        if (exData?.isNotEmpty() == true) {
            if (exFileName == null || exFileName.isEmpty()) throw IllegalStateException("exFileName is empty.")
            saveExDataFile(entryId, exFileName, exData)
        }

        // 5. zip
        zip(entryId)

        emit(ApiResult.Success(Unit))
    }.catch {
        emit(ApiResult.Error(-9, "${it.message}", it))
    }

    private fun copyResultForms(entryId: String, resultData: ResultData) {
        resultData.resultFormList.forEachIndexed { index, resultForm ->
            FileUtils.copy(
                resultForm.resultXmlDirPath,
                pathManager.getResultXmlDir(entryId, index + 1)
            )
        }
    }

    private fun copyEvidenceDocs(entryId: String) {
        File(pathManager.getEvidenceDocDir(entryId)).listFiles { _, name ->
            FileUtils.copy(
                pathManager.getEvidenceDocDir(entryId, name),
                pathManager.getResultImageDir(entryId, name)
            )

            true
        }
    }

    private fun saveSendImages(entryId: String, sendImageDataInfoList: List<SendImageDataInfo>) {
        sendImageDataInfoList.forEach { sendImageDataInfo ->
            val imageStr = sendImageDataInfo.base64Image

            if (imageStr.isNullOrBlank()) return@forEach

            ImageController.convertBase64PngStringToBitmap(imageStr)?.let {
                val path =
                    pathManager.getSendImageFilePath(entryId, sendImageDataInfo.tFieldId, "01")
                sendImageDataInfo.path = path
                FileUtils.saveBitmap(path, it, Bitmap.CompressFormat.PNG, 100)
            } ?: throw NullPointerException("Base64를 이미지 변환에 실패했습니다.")
        }
    }

    private fun copyVoiceOfRecords(entryId: String): List<RecordOfVoiceEx> {
        return File(pathManager.getRecordDir(entryId)).listFiles { _, name ->
            FileUtils.copy(
                pathManager.getRecordDir(entryId, name),
                pathManager.getResultImageDir(entryId, name)
            )
            true
        }?.map {
            RecordOfVoiceEx(
                RecordOfVoice(it.name, "녹취"),
                pathManager.getRecordCount(entryId, it.name)
            )
        } ?: listOf()
    }

    private fun makeDataConfig(resultFormDataList: List<ResultFormInfo>): List<FlDrDataExt> {
        return mutableListOf<FlDrDataExt>().apply {
            resultFormDataList.forEachIndexed { index, resultForm ->
                val resultXmlDir = File(resultForm.resultXmlDirPath)
                val resultXmlName =
                    resultXmlDir.list { _, name -> name.endsWith(PathManager.EXT_XML) }?.get(0)
                        ?: throw FileNotFoundException("result xml not found in ${resultForm.resultXmlDirPath}")
                val imageCount =
                    resultXmlDir.list { _, name -> name.endsWith(PathManager.EXT_PNG) }?.size ?: 0

                add(
                    FlDrDataExt(
                        flDrData = FlDrData(
                            fLdrName = "${index + 1}",
                            formCode = resultForm.formCode,
                            formName = resultForm.formName ?: "",
                            formVersion = resultForm.formVersion,
                            imgCount = imageCount,
                            xmlName = resultXmlName
                        ),
                        dirPath = resultForm.resultXmlDirPath
                    )
                )
            }
        }
    }

    private fun makeDefaultValue(entryId: String): Map<String, String> {
        return mutableMapOf<String, String>().apply {
            val date = Date()
            this[ElectronicDocConstants.WebData.DOC_ID] = entryId
            this[ElectronicDocConstants.IBKConfig.TX_TIME] =
                SimpleDateFormat("yyyyMMddHHmmss").format(date)
            this[ElectronicDocConstants.IBKConfig.INDEX_08] =
                SimpleDateFormat("yyyyMMdd").format(date)
            this[ElectronicDocConstants.IBKConfig.NXT_DAY_BUY_YN] = "N"
            this[ElectronicDocConstants.IBKConfig.SEAL_REG_YN] = "N"
        }
    }

    private fun makeIBKSConfig(
        entryId: String,
        info: Map<String, String>,
        resultData: ResultData,
        resultDocs: List<EvidenceDocument>,
        recordExs: List<RecordOfVoiceEx>,
        addedDocs: List<AddedDocument>,
        sendImageDataInfoList: List<SendImageDataInfo>,
        autoMailList: List<DocsResponse>,
        memo: String?
    ): Map<String, Any> {
        return hashMapOf<String, Any>().apply {
            putAll(info)

            var order = 1
            var hasSendSeal = false
            this[ElectronicDocConstants.IBKConfig.DOC_INFO] = mutableListOf<FormInfo>().apply {

                // 1. 전자문서
                addAll(resultData.resultFormList.map { resultForm ->
                    FormInfo(
                        code = resultForm.formCode,
                        name = resultForm.formName,
                        pages = "${resultForm.formPageCount}",
                        order = "${order++}",
                        autoMail = autoMailList.firstOrNull { it.formCd == resultForm.formCode }?.autoMail
                            ?: "N"
                    )
                })

                // 2. 신분증
                File(pathManager.getResultImageDir(entryId)).listFiles()?.filter { dir ->
                    (AuthType.values().any { authType -> authType.code == dir.name }) && (dir.list()
                        ?.isNotEmpty() ?: false)
                }?.map { dir ->
                    val type = AuthType.getAuthType(dir.name)!!
                    val dirPath = pathManager.getResultImageDir(entryId, type.code)
                    addOrderChildFileName(dirPath, type.code, order)
                    FormInfo(
                        code = type.code,
                        name = type.title,
                        pages = "${dir.list()?.size ?: 0}",
                        order = "${order++}"
                    )
                }?.let {
                    addAll(it)
                }

                // 3. 증빙서류
                addAll(resultDocs.filter {
                    val dirPath = pathManager.getResultImageDir(entryId, it.code)
                    existFiles(dirPath)
                }.map {
                    val dirPath = pathManager.getResultImageDir(entryId, it.code)
                    addOrderChildFileName(dirPath, it.code, order)

                    FormInfo(
                        code = it.code,
                        name = it.name,
                        pages = "${it.count}",
                        order = "${order++}"
                    )
                })

                // 4. 녹취파일
                addAll(recordExs.filter {
                    val dirPath = pathManager.getResultImageDir(entryId, it.record.code)
                    existFiles(dirPath)
                }.map {
                    val dirPath = pathManager.getResultImageDir(entryId, it.record.code)
                    addOrderChildFileName(dirPath, it.record.code, order)

                    FormInfo(
                        code = it.record.code,
                        name = it.record.name,
                        pages = "${it.count}",
                        order = "${order++}"
                    )
                })

                // 5. 인감/서명 등록
                addAll(sendImageDataInfoList.filter {
                    it.path?.let { path -> File(path).exists() } ?: false
                }.map {
                    val dirPath = pathManager.getResultImageDir(entryId, it.tFieldId)
                    addOrderChildFileName(dirPath, it.tFieldId, order)

                    hasSendSeal = true
                    FormInfo(
                        code = it.tFieldId,
                        name = it.name,
                        pages = "1",
                        order = "${order++}"
                    )
                })

                addAll(addedDocs.map {
                    File(
                        pathManager.getResultDummyImageDir(
                            entryId,
                            it.type,
                            it.code,
                            order
                        )
                    ).mkdirs()

                    FormInfo(
                        code = it.code,
                        name = it.name,
                        pages = "1",//"${it.count}",        // 페이지별로 이미지화 하지 않아 파일이 1개 이기 때문에 고정함
                        order = "${order++}"
                    )
                })
            }

            var scanCodeStr: String? = null
            var cnsSttYn = "N"
            var odsMemo = ""

            this[ElectronicDocConstants.IBKConfig.TERMINAL_INFO] = resultData.terminalInfo
                .filter { map ->
                    if (map.key.endsWith(
                            suffix = ElectronicDocConstants.IBKConfig.ODS_ADD_DOC,
                            ignoreCase = true
                        )
                    ) {         // 지류서식 목록
                        scanCodeStr = map.value
                        false
                    } else if (map.key.endsWith(
                            suffix = ElectronicDocConstants.IBKConfig.ODS_CNS_STT_YN,
                            ignoreCase = true
                        )
                    ) {  // 숙려 여부
                        if (map.value.isNotEmpty()) {
                            cnsSttYn = map.value
                        }
                        false
                    } else if (map.key.endsWith(
                            suffix = ElectronicDocConstants.IBKConfig.ODS_MEMO,
                            ignoreCase = true
                        )
                    ) {
                        odsMemo = map.value
                        false
                    } else true
                }
                .map { TerminalInfo(it.key, it.value) }

            scanCodeStr?.let {
                this[ElectronicDocConstants.IBKConfig.ADD_SCAN_INFO] = getScanInfo(it)
            }

            if (hasSendSeal) {
                this[ElectronicDocConstants.IBKConfig.SEAL_REG_YN] = "Y"
            }
            this[ElectronicDocConstants.IBKConfig.CNS_STT_YN] = cnsSttYn
            this[ElectronicDocConstants.IBKConfig.MEMO] = if (odsMemo.isBlank()) {
                memo ?: ""
            } else {
                memo?.let { "$odsMemo\n$memo" } ?: odsMemo
            }
        }
    }

    private fun existFiles(dirPath: String): Boolean {
        val dir = File(dirPath)

        if (!dir.exists()) return false

        return dir.list()?.isNotEmpty() ?: false
    }

    private fun addOrderChildFileName(dirPath: String, code: String, order: Int) {
        File(dirPath).listFiles()?.forEach { file ->
            val orderedName = file.name.replace(code, "${code}_${String.format("%03d", order)}")
            val renameFile = File(file.parentFile, orderedName)
            if (!file.renameTo(renameFile)) throw IllegalStateException("파일명 변경 실패. (${file.name} > $orderedName)")
        }
    }

    private fun getScanInfo(scanCodeStr: String): List<AddScanInfo> {
        val scanCodes = scanCodeStr.split("^")
        val formInfo = updateFormDataSource.getLocalFormDataList()

        return formInfo.filter {
            scanCodes.contains(it.code)
        }.map {
            AddScanInfo(code = it.code, name = it.title)
        }
    }

    private fun copyFiles(entryId: String, dataConfig: List<FlDrDataExt>) {

        val dir = File(dataConfig[0].dirPath).parentFile
            ?: throw NullPointerException("copy fail : parent directory is null")
        val dest = pathManager.getResultRoot(entryId)

        FileUtils.delete(dest)

        FileUtils.copy(
            dir.absolutePath,
            dest
        )
    }

    private fun saveDataConfigFile(entryId: String, dataConfig: List<FlDrDataExt>) {
        val json = Gson().toJson(DataConfig(
            entryId = entryId,
            fLDRCnt = dataConfig.size,
            fLDRData = dataConfig.map { it.flDrData }
        ))
        File(pathManager.getResultConfig(entryId)).writeText(json)
    }

    private fun saveDataConfigFile(entryId: String, iBKSConfig: Map<String, Any>) {
        val json = Gson().toJson(iBKSConfig)
        File(pathManager.getIBKSConfig(entryId)).writeText(json)
    }

    private fun saveExDataFile(entryId: String, fileName: String, exData: Map<String, Any>) {
        val json = Gson().toJson(exData)
        File(pathManager.getExDataPath(entryId, fileName)).writeText(json)
    }

    private fun zip(entryId: String) {
        val resultRoot = pathManager.getResultRoot(entryId)
        val resultZip = pathManager.getResultZip(entryId)

        FileUtils.zipFile(resultRoot, resultZip)
    }

    override suspend fun transmitResultFile(entryId: String, memo: String): Flow<ApiResult<Unit>> {
        return remoteDataSource.apiCall {

            val resultZip = pathManager.getResultZip(entryId)

            val file = FileUtils.readFile(resultZip)
            val zipFilePart = remoteDataSource.toMultipartFile(
                name = "file",
                fileName = "$entryId.zip",
                data = file,
                contentType = "application/octet-stream"
            )

            val json = Gson().toJson(RequestResultInfo(entryId = entryId, memo = memo))
            val dataPart = remoteDataSource.toMultipartJson(TEMP_DATA_API_MULTIPART_JSON_NAME, json)

            MultipartBody.Builder()

            edsApiService.uploadResult(zipFilePart, dataPart)
        }
    }
}