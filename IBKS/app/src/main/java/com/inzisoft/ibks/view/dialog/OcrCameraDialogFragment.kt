package com.inzisoft.ibks.view.dialog

import android.icu.text.SimpleDateFormat
import android.view.View
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.fragment.app.viewModels
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.fragment.findNavController
import com.inzisoft.ibks.FragmentRequest
import com.inzisoft.ibks.FragmentResult
import com.inzisoft.ibks.TakeType
import com.inzisoft.ibks.data.internal.AuthDialogData
import com.inzisoft.ibks.setFragmentResult
import com.inzisoft.ibks.view.compose.theme.IBKSTheme
import com.inzisoft.ibks.view.compose.theme.OcrImageBackgroundColor
import com.inzisoft.ibks.viewmodel.CameraState
import com.inzisoft.ibks.viewmodel.OcrCameraViewModel
import com.inzisoft.mobile.data.MIDReaderProfile
import com.inzisoft.ibks.viewmodel.CameraViewModel
import com.inzisoft.ibks.R
import com.inzisoft.ibks.base.BaseDialogFragmentViewModel
import com.inzisoft.ibks.data.web.AuthCameraData
import com.inzisoft.ibks.view.compose.*
import com.inzisoft.ibks.view.compose.theme.*
import com.inzisoft.ibks.viewmodel.AuthData
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class OcrCameraDialogFragment: CameraDialogFragment() {
    private val CAMERA_GUIDE = "CAMERA_GUIDE"
    private val RECOG_RESULT = "RECOG_RESULT"

    private val ocrCameraViewModel by viewModels<OcrCameraViewModel>()

    init {
        MIDReaderProfile.getInstance().SET_USER_SCREEN_PORTRAIT = false
    }

    override fun initComposeLayout() {
        binding.lyCameraOptions.visibility = View.GONE
        OcrCameraScreen(
            topComposeView = binding.topComposeView,
            leftComposeView = binding.leftComposeView,
            rightComposeView = binding.rightComposeView,
            popupComposeView = binding.popupComposeView
        )
    }

    override fun getViewModel(): CameraViewModel {
        return ocrCameraViewModel
    }

    /**
     * 신분증 인식 촬영
     */
    private fun OcrCameraScreen(
        topComposeView: ComposeView,
        leftComposeView: ComposeView,
        rightComposeView: ComposeView,
        popupComposeView: ComposeView
    ) {
        topComposeView.apply {
            setContent {
                IBKSTheme {
                    val cameraState = ocrCameraViewModel.cameraState

                    OcrCameraTopBar(
                        title = ocrCameraViewModel.config.title,
                        cameraState = cameraState,
                        onRetake = { ocrCameraViewModel.retake() },
                        onCancel = {
                            setFragmentResult(FragmentRequest.OcrCamera, FragmentResult.Cancel())
                            findNavController().navigateUp()
                        },
                        onAuth = {
                            ocrCameraViewModel.auth(TakeType.OCR) { result ->
                                if (result) {
                                    setFragmentResult(FragmentRequest.OcrCamera, FragmentResult.OK(null))
                                    findNavController().navigateUp()
                                }
                            }
                        }
                    )
                }
            }
        }
        
        leftComposeView.apply {
            setContent {
                IBKSTheme {
                    visibility = when (ocrCameraViewModel.cameraState) {
                        is CameraState.CameraOcrResultState -> {
                            binding.tvOcrGuide.visibility = View.GONE
                            Row(
                                modifier = Modifier
                                    .width(516.dp)
                                    .height(328.dp)
                                    .background(OcrImageBackgroundColor),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                ocrCameraViewModel.authDataState.idCardBitmap?.let {
                                    Image(
                                        bitmap = it.asImageBitmap(),
                                        contentDescription = ""
                                    )
                                }
                            }
                            View.VISIBLE
                        }
                        else -> {
                            binding.tvOcrGuide.visibility = View.VISIBLE
                            View.GONE
                        }
                    }
                }
            }
        }

        rightComposeView.apply {
            setContent {
                IBKSTheme {
                    val cameraState = ocrCameraViewModel.cameraState
                    val navController = rememberNavController()

                    NavHost(navController = navController, startDestination = CAMERA_GUIDE) {
                        composable(CAMERA_GUIDE) {
                            CameraGuide()
                        }
                        composable(RECOG_RESULT) {
                            CameraRecogResult(
                                authData = ocrCameraViewModel.authDataState,
                                authCameraData = ocrCameraViewModel.authCameraData
                            ) {
                                ocrCameraViewModel.showAuthGuideDialog()
                            }
                        }
                    }

                    when (cameraState) {
                        is CameraState.CameraPreviewState -> navController.navigate(CAMERA_GUIDE)
                        is CameraState.CameraOcrResultState -> navController.navigate(RECOG_RESULT)
                    }
                }
            }
        }

        popupComposeView.apply {
            setContent {
                IBKSTheme {
                    when(val dialogState = ocrCameraViewModel.dialogState) {
                        is AuthDialogData.ShowAuthGuidePopup ->
                            CameraAuthGuidePopup {
                                ocrCameraViewModel.closeAuthGuideDialog()
                            }
                        is AuthDialogData.AuthFailedPopup -> {
                            AlertDialog(
                                contentText = buildAnnotatedString {
                                    append(dialogState.message)
                                },
                                onRightBtnClick = {
                                    ocrCameraViewModel.dialogState = AuthDialogData.None
                                })
                        }
                        is AuthDialogData.Loading -> { Loading() }
                        is AuthDialogData.ShowOcrFailedPopup -> {
                            AlertDialog(
                                contentText = buildAnnotatedString {
                                    append(stringResource(id = R.string.ocr_failed_popup))
                                },
                                leftBtnText = stringResource(id = R.string.no),
                                rightBtnText = stringResource(id = R.string.yes),
                                onLeftBtnClick = {
                                    setFragmentResult(FragmentRequest.OcrCamera, FragmentResult.Cancel())
                                    findNavController().navigateUp()
                                },
                                onRightBtnClick = {
                                    ocrCameraViewModel.dialogState = AuthDialogData.None
                                    ocrCameraViewModel.retake()
                                })
                        }
                        is AuthDialogData.None -> {}
                    }
                }
            }
        }
    }

    /**
     * 인감촬영 UI
     */
    private fun sealScanCameraViewInit() {

    }

    /**
     * 문서촬영 UI
     */
    private fun paperScanCameraViewInit() {

    }

    /**
     * 여권촬영 UI
     */
    private fun passportOcrCameraViewInit() {

    }

    @Composable
    private fun CameraGuide() {
        Column(
            Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_img_snap_guide1),
                    contentDescription = "",
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = stringResource(id = R.string.camera_guide1),
                    style = MaterialTheme.typography.subtitle1,
                    color = MaterialTheme.colors.textColor,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(48.dp))
                Image(
                    painter = painterResource(id = R.drawable.ic_img_snap_guide2),
                    contentDescription = ""
                )

                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = stringResource(id = R.string.camera_guide2),
                    style = MaterialTheme.typography.subtitle1,
                    color = MaterialTheme.colors.textColor,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(48.dp))
                Image(
                    painter = painterResource(id = R.drawable.ic_img_snap_guide3),
                    contentDescription = ""
                )
                Text(
                    text = stringResource(id = R.string.camera_guide3),
                    style = MaterialTheme.typography.subtitle1,
                    color = MaterialTheme.colors.textColor,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(92.dp))

            Column {
                Text(
                    text = stringResource(id = R.string.camera_guide4),
                    style = MaterialTheme.typography.body1,
                    color = MaterialTheme.colors.textColor
                )
                Text(
                    text = stringResource(id = R.string.camera_guide5),
                    modifier = Modifier.padding(8.dp, 0.dp, 0.dp, 0.dp),
                    style = MaterialTheme.typography.body1,
                    color = MaterialTheme.colors.textColor
                )
                Text(
                    text = stringResource(id = R.string.camera_guide6),
                    modifier = Modifier.padding(8.dp, 0.dp, 0.dp, 0.dp),
                    style = MaterialTheme.typography.body1,
                    color = MaterialTheme.colors.point4Color
                )
                Text(
                    text = stringResource(id = R.string.camera_guide7),
                    modifier = Modifier.padding(8.dp, 0.dp, 0.dp, 0.dp),
                    style = MaterialTheme.typography.body1,
                    color = MaterialTheme.colors.textColor
                )
            }
        }
    }

    @Composable
    private fun CameraRecogResult(
        authData: AuthData,
        authCameraData: AuthCameraData,
        onShowAuthGuidePopupClick: () -> Unit = {}
    ) {
        Column(
            Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Column {
//                Text(
//                    text = stringResource(id = R.string.idcard_recog_content),
//                    style = MaterialTheme.typography.body1,
//                    color = MaterialTheme.colors.sub1Color
//                )
//
//                Spacer(modifier = Modifier.height(56.dp))

                IdCardDetail(
                    authData = authData,
                    authCameraData = authCameraData,
                    onDatePickerShow = { key ->
                        showDatePicker { date ->
                            authData.dataMap[key] =
                                SimpleDateFormat("yyyyMMdd").format(date)
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(95.dp))

            Text(
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                text = stringResource(id = R.string.idcard_recog_content),
                style = MaterialTheme.typography.body1,
                color = MaterialTheme.colors.point4Color
            )

            when (authData) {
//                    is AuthData.DriveLicenseData -> {
//                        Spacer(modifier = Modifier.height(95.dp))
//
//                        Text(
//                            textAlign = TextAlign.Center,
//                            text = stringResource(id = R.string.retry_camera_take),
//                            style = MaterialTheme.typography.body1,
//                            color = MaterialTheme.colors.point4Color
//                        )
//                    }
                is AuthData.OverSea,
                is AuthData.IdCradData,
                is AuthData.ForeignData -> {
                    Spacer(modifier = Modifier.height(73.dp))

                    Row {
                        Spacer(modifier = Modifier.width(132.dp))
                        RoundImageButton(
                            onClick = onShowAuthGuidePopupClick,
                            text = stringResource(R.string.button_text_show_auth_guide_popup)
                        )
                    }
                }
            }
        }
    }

    override fun getBaseViewModel(): BaseDialogFragmentViewModel? {
        return ocrCameraViewModel
    }
}