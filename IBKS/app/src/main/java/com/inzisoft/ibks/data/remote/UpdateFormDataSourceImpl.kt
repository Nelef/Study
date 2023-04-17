package com.inzisoft.ibks.data.remote

import com.inzisoft.ibks.AppKeySet
import com.inzisoft.ibks.BuildConfig
import com.inzisoft.ibks.PathManager
import com.inzisoft.ibks.data.repository.LocalRepository
import com.inzisoft.ibks.util.log.QLog
import com.inzisoft.paperless.update.FormUpdater
import com.inzisoft.paperless.update.UpdateListener
import com.inzisoft.paperless.update.UpdateSetting
import com.inzisoft.paperless.update.data.UpdateBusinessLogicData
import com.inzisoft.paperless.update.data.UpdateData
import com.inzisoft.paperless.update.data.UpdateFormData
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class UpdateFormDataSourceImpl @Inject constructor(
    private val pathManager: PathManager,
    private val localRepository: LocalRepository
) : UpdateFormDataSource {

    companion object {
        private const val ODS_PRD_CD = "odsPrdCd"
    }

    private var formUpdater: FormUpdater? = null
    private val baseUpdateFormDataList = mutableListOf<UpdateData>()
    private val instructionFormDataList = mutableListOf<UpdateFormData>()

    override suspend fun checkVersion() = callbackFlow {
        trySend(UpdateFormDataSource.UpdateFormState.Loading)

        val token = localRepository.getAccessToken().first()
        val path = "${BuildConfig.MAIN_API}/eds/eform/IS"

        formUpdater = FormUpdater(
            UpdateSetting.UpdateSettingBuilder(pathManager.getFormRootDir())
                .setHeaders(mapOf(AppKeySet.ACCESS_TOKEN to token))
                .setUrlProtocol(BuildConfig.SERVER_PROTOCOL)
                .setHostUrl("${BuildConfig.API_SERVER_URL}:${BuildConfig.API_SERVER_PORT}")
                .setFormUrlPath("$path/form")
                .setBizUrlPath("$path/biz")
                .setDebug(BuildConfig.DEBUG)
                .setManual(true)
                .build()
        ).apply {
            setListener(Listener(this@callbackFlow))
        }.also {
            it.start()
        }

        awaitClose {
            formUpdater?.setListener(null)
        }
    }

    override fun update() = callbackFlow {
        trySend(UpdateFormDataSource.UpdateFormState.Loading)

        if (formUpdater == null) {
            trySend(UpdateFormDataSource.UpdateFormState.OnError("Must call checkVersion before update."))
            close()
            return@callbackFlow
        }

        formUpdater?.apply {
            setListener(Listener(this@callbackFlow))
            update(baseUpdateFormDataList)
        }

        awaitClose {
            formUpdater?.setListener(null)
            formUpdater = null
        }
    }

    override suspend fun updateProductCode(productCode: String) = callbackFlow {
        trySend(UpdateFormDataSource.UpdateFormState.Loading)

        checkVersion().collect { state ->
            when (state) {
                UpdateFormDataSource.UpdateFormState.Loading -> trySend(UpdateFormDataSource.UpdateFormState.Loading)
                is UpdateFormDataSource.UpdateFormState.OnReadyUpdate,
                UpdateFormDataSource.UpdateFormState.OnUpdateComplete -> {
                    formUpdater?.apply {

                        val instructionFormList = localServerFormVersion.list.filter {
                            isProductCodeForm(it as UpdateFormData, productCode)
                        }.map {
                            it.code
                        }

                        trySend(UpdateFormDataSource.UpdateFormState.Instruction(instructionFormList))

                        setListener(Listener(this@callbackFlow))

                        val updateList = instructionFormDataList.filter {
                            isProductCodeForm(it, productCode)
                        }

                        if (updateList.isEmpty()) {
                            trySend(UpdateFormDataSource.UpdateFormState.OnUpdateComplete)
                            close()
                        } else {
                            trySend(UpdateFormDataSource.UpdateFormState.OnReadyUpdate)
                            update(updateList)
                        }
                    }
                }
                else -> {}
            }
        }

        awaitClose {
            formUpdater?.setListener(null)
            formUpdater = null
        }
    }

    private fun divideUpdateFormList(
        formDataList: List<UpdateFormData>,
        bizDataList: List<UpdateBusinessLogicData>
    ) {
        baseUpdateFormDataList.clear()
        instructionFormDataList.clear()

        formDataList.forEach {
            if (isBaseForm(it)) {
                baseUpdateFormDataList.add(it)
            } else {
                instructionFormDataList.add(it)
            }
        }

        baseUpdateFormDataList.addAll(bizDataList)
    }

    private fun isBaseForm(updateFormData: UpdateFormData): Boolean {
        return getProductCode(updateFormData).isEmpty()
    }

    private fun isProductCodeForm(updateFormData: UpdateFormData, productCode: String): Boolean {
        return getProductCode(updateFormData).contains(productCode)
    }

    private fun getProductCode(updateFormData: UpdateFormData): String {
        return updateFormData.attributes[ODS_PRD_CD]?.trim() ?: ""
    }

    private inner class Listener(val scope: ProducerScope<UpdateFormDataSource.UpdateFormState>) :
        UpdateListener {
        override fun onError(message: String) {
            scope.trySend(UpdateFormDataSource.UpdateFormState.OnError(message))
        }

        override fun versionCheckComplete(
            formDataList: List<UpdateFormData>,
            bizDataList: List<UpdateBusinessLogicData>
        ) {
            QLog.d("update form : $formDataList\nupdate biz : $bizDataList")

            divideUpdateFormList(formDataList, bizDataList)

            if (baseUpdateFormDataList.isEmpty()) {
                scope.trySend(UpdateFormDataSource.UpdateFormState.OnUpdateComplete)
                scope.close()
            } else {
                scope.trySend(UpdateFormDataSource.UpdateFormState.OnReadyUpdate)
            }
        }

        override fun onStart(fileName: String, currentCount: Int, totalCount: Int) {
            scope.trySend(UpdateFormDataSource.UpdateFormState.OnStart(currentCount, totalCount))
        }

        override fun onProgress(fileName: String, currentByte: Long, totalByte: Long) {
            scope.trySend(UpdateFormDataSource.UpdateFormState.OnProgress(currentByte.toFloat() / totalByte))
        }

        override fun onComplete(fileName: String, currentCount: Int, totalCount: Int) {
            scope.trySend(UpdateFormDataSource.UpdateFormState.OnComplete(currentCount, totalCount))

            if (currentCount == totalCount) {
                scope.trySend(UpdateFormDataSource.UpdateFormState.OnUpdateComplete)
                scope.close()
            }
        }

    }

    @Suppress("UNCHECKED_CAST")
    override fun getLocalFormDataList(): List<UpdateData> {
        val formUpdater =
            FormUpdater(
                UpdateSetting.UpdateSettingBuilder(pathManager.getFormRootDir())
                    .setLocal(true)
                    .build()
            )
        return (formUpdater.localFormVersion?.list ?: listOf())
    }

    override fun getFormCode(productCode: String): List<String> {
        return getLocalFormDataList().filter {
            isProductCodeForm(it as UpdateFormData, productCode)
        }.map {
            it.code
        }
    }
}