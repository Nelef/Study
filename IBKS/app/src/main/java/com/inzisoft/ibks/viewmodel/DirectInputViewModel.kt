package com.inzisoft.ibks.viewmodel

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import com.inzisoft.ibks.AuthType
import com.inzisoft.ibks.data.repository.LocalRepository
import com.inzisoft.ibks.data.repository.CameraRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

@HiltViewModel
class DirectInputViewModel @Inject constructor(
    @ApplicationContext context: Context,
    savedStateHandle: SavedStateHandle,
    localRepository: LocalRepository,
    cameraRepository: CameraRepository
) : AuthViewModel(context, savedStateHandle, cameraRepository, localRepository) {

    var directInputUiState by mutableStateOf<DirectInputUiState>(DirectInputUiState.IDCARD)

    init {
        initDataMap(authDataState)
        if(authCameraData.cameraType == AuthType.OVERSEA.type) {
            directInputUiState = DirectInputUiState.OVERSEA
            authDataState = AuthData.OverSea()
        }
    }

    fun selectChoiceButton(selected: DirectInputUiState) {
        directInputUiState = selected
        when (authDataState) {
            is AuthData.IdCradData -> {
                if (selected is DirectInputUiState.DRIVE_LICENSE) {
                    authDataState = AuthData.DriveLicenseData()
                }
            }
            else -> {
                if (selected is DirectInputUiState.IDCARD) {
                    authDataState = AuthData.IdCradData()
                }
            }
        }

        initDataMap(authDataState)
    }

    private fun initDataMap(authData: AuthData) {
        when (authData) {
            is AuthData.DriveLicenseData -> {
                authDataState.dataMap[AuthData.NAME] = ""
                authDataState.dataMap[AuthData.LICNUM0_1] = ""
                authDataState.dataMap[AuthData.LICNUM2_3] = ""
                authDataState.dataMap[AuthData.LICNUM4_9] = ""
                authDataState.dataMap[AuthData.LICNUM10_11] = ""
                authDataState.dataMap[AuthData.ISSUE_DATE] = ""
                authDataState.dataMap[AuthData.ISSUE_OFFICE] = ""
                authDataState.dataMap[AuthData.FRONT_IDNUM] = ""
                authDataState.dataMap[AuthData.LAST_IDNUM] = ""
            }
            else -> {
                authDataState.dataMap[AuthData.NAME] = ""
                authDataState.dataMap[AuthData.ISSUE_DATE] = ""
                authDataState.dataMap[AuthData.ISSUE_OFFICE] = ""
                authDataState.dataMap[AuthData.FRONT_IDNUM] = ""
                authDataState.dataMap[AuthData.LAST_IDNUM] = ""
            }
        }
    }
}

sealed class DirectInputUiState {
    object IDCARD: DirectInputUiState()
    object OVERSEA: DirectInputUiState()
    object DRIVE_LICENSE: DirectInputUiState()
}