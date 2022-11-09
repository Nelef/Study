package com.inzisoft.ibks.di

import android.content.Context
import android.os.Environment
import com.inzisoft.ibks.BuildConfig
import com.inzisoft.ibks.PathManager
import com.inzisoft.ibks.data.remote.BaseRemoteDataSource
import com.inzisoft.ibks.data.remote.BaseRemoteDataSourceImpl
import com.inzisoft.ibks.data.remote.converter.CryptoGsonConverterFactory
import com.inzisoft.ibks.data.remote.converter.CryptoService
import com.inzisoft.ibks.data.remote.converter.EnumConverterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)

object RemoteRepositoryModule {
    @Provides
    @Singleton
    fun providePathManager(@ApplicationContext context: Context): PathManager {
        return PathManager(
            filesPath = context.filesDir.absolutePath,
            cachePath = context.cacheDir.absolutePath,
            externalFilesPath = context.getExternalFilesDir(null)?.absolutePath,
            externalDownLoadsPath = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)?.absolutePath,
            externalDocumentsPath = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)?.absolutePath,
            externalCachePath = context.externalCacheDir?.absolutePath
        )
    }
}