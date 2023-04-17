package com.inzisoft.ibks.di

import android.content.Context
import android.util.Base64
import com.inzisoft.ibks.BuildConfig
import com.inzisoft.ibks.PathManager
import com.inzisoft.ibks.data.internal.AuthInfoDataSource
import com.inzisoft.ibks.data.internal.PaperlessSaveInfoDataSource
import com.inzisoft.ibks.data.internal.PreferenceDataSource
import com.inzisoft.ibks.data.internal.UserInfoDataSource
import com.inzisoft.ibks.data.remote.converter.CryptoService
import com.inzisoft.ibks.data.repository.LocalRepository
import com.inzisoft.ibks.util.CryptoUtil
import com.inzisoft.ibks.util.PdfRenderer
import com.inzisoft.paperless.pdf.PdfManager
import com.inzisoft.paperless.util.MsgLog
import com.inzisoft.paperless.xml.builder.EmptyXmlBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object LocalRepositoryModule {

    @Singleton
    @Provides
    fun providePreferenceDataSource(@ApplicationContext context: Context): PreferenceDataSource =
        PreferenceDataSource(context)

    @Singleton
    @Provides
    fun provideUserInfoDataSource(@ApplicationContext context: Context): UserInfoDataSource =
        UserInfoDataSource(context)

    @Singleton
    @Provides
    fun provideAuthInfoDataSource(@ApplicationContext context: Context): AuthInfoDataSource =
        AuthInfoDataSource(context)

    @Singleton
    @Provides
    fun providePaperlessSaveInfoDataSource(@ApplicationContext context: Context): PaperlessSaveInfoDataSource =
        PaperlessSaveInfoDataSource(context)

    @Singleton
    @Provides
    fun provideLocalRepository(
        preferenceDataSource: PreferenceDataSource,
        userInfoDataSource: UserInfoDataSource,
        authInfoDataSource: AuthInfoDataSource,
        paperlessSaveInfoDataSource: PaperlessSaveInfoDataSource,
        pathManager: PathManager
    ): LocalRepository =
        LocalRepository(
            preferenceDataSource,
            userInfoDataSource,
            authInfoDataSource,
            paperlessSaveInfoDataSource,
            pathManager
        )

    @Singleton
    @Provides
    fun provideCryptoService(@ApplicationContext context: Context): CryptoService =
        object : CryptoService {

            override fun encrypt(data: ByteArray, wrapBase64: Boolean): ByteArray {
                return CryptoUtil.encrypt(context, data).let { encData ->
                    if (wrapBase64) {
                        Base64.encode(encData, Base64.NO_WRAP)
                    } else {
                        encData
                    }
                }
            }

            override fun decrypt(data: ByteArray): ByteArray {
                return CryptoUtil.decrypt(context, data)
            }
        }

    @Provides
    @Singleton
    fun providePdfRenderer(
        @ApplicationContext context: Context,
        pathManager: PathManager
    ): PdfRenderer? {
        if (BuildConfig.DEBUG) {
            MsgLog.setDebug(true)
        }

        val ret = PdfManager.getInstance().checkPDFLicense(context)
        return if (ret == 0) {
            PdfRenderer(pathManager)
        } else {
            null
        }
    }

    @Provides
    @Singleton
    fun provideEmptyXmlBuilder(@ApplicationContext context: Context): EmptyXmlBuilder? {
        if (BuildConfig.DEBUG) {
            MsgLog.setDebug(true)
        }

        val ret = PdfManager.getInstance().checkPDFLicense(context)
        return if (ret == 0) {
            EmptyXmlBuilder.getInstance()
        } else {
            null
        }
    }
}