package com.inzisoft.ibks.di

import android.content.Context
import android.util.Base64
import com.inzisoft.ibks.data.internal.AuthInfoDataSource
import com.inzisoft.ibks.data.internal.PreferenceDataSource
import com.inzisoft.ibks.data.remote.converter.CryptoService
import com.inzisoft.ibks.util.CryptoUtil
import com.inzisoft.ibks.data.repository.LocalRepository
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

    @Singleton
    @Provides
    fun provideAuthInfoDataSource(@ApplicationContext context: Context): AuthInfoDataSource =
        AuthInfoDataSource(context)

    @Singleton
    @Provides
    fun providePreferenceDataSource(@ApplicationContext context: Context): PreferenceDataSource =
        PreferenceDataSource(context)

    @Singleton
    @Provides
    fun provideLocalRepository(
        preferenceDataSource: PreferenceDataSource,
        authInfoDataSource: AuthInfoDataSource
    ): LocalRepository =
        LocalRepository(
            preferenceDataSource,
            authInfoDataSource
        )
}