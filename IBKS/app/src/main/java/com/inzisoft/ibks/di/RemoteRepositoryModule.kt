package com.inzisoft.ibks.di

import android.content.Context
import android.os.Environment
import com.inzisoft.ibks.BuildConfig
import com.inzisoft.ibks.PathManager
import com.inzisoft.ibks.data.remote.*
import com.inzisoft.ibks.data.remote.api.*
import com.inzisoft.ibks.data.remote.converter.CryptoGsonConverterFactory
import com.inzisoft.ibks.data.remote.converter.CryptoService
import com.inzisoft.ibks.data.remote.converter.EnumConverterFactory
import com.inzisoft.ibks.data.remote.interceptor.HeaderInterceptor
import com.inzisoft.ibks.data.repository.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Qualifier
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)

object RemoteRepositoryModule {

    @Singleton
    @Provides
    fun provideLoginRepository(
        authApiService: AuthApiService,
        userApiService: UserApiService,
        baseRemoteDataSource: BaseRemoteDataSource
    ): LoginRepository =
        LoginRepositoryImpl(authApiService, userApiService, baseRemoteDataSource)

    @Singleton
    @Provides
    fun provideApplicationUpdateRepository(
        apkApiService: ApkApiService,
        baseRemoteDataSource: BaseRemoteDataSource
    ): ApplicationUpdateRepository =
        ApplicationUpdateRepositoryImpl(apkApiService, baseRemoteDataSource)

    @Singleton
    @Provides
    fun provideFormUpdateRepository(
        pathManager: PathManager,
        localRepository: LocalRepository
    ): UpdateFormDataSource =
        UpdateFormDataSourceImpl(pathManager, localRepository)

    @Singleton
    @Provides
    fun provideMainRepository(
        localRepository: LocalRepository,
        edsApiService: EdsApiService,
        productApiService: ProductApiService,
        baseRemoteDataSource: BaseRemoteDataSource
    ): MainRepository = MainRepositoryImpl(localRepository, edsApiService, productApiService, baseRemoteDataSource)

    @Singleton
    @Provides
    fun provideCameraRepository(
        identityApiService: IdentityApiService,
        edsApiService: EdsApiService,
        baseRemoteDataSource: BaseRemoteDataSource
    ): CameraRepository =
        CameraRepositoryImpl(identityApiService, edsApiService, baseRemoteDataSource)

    @Singleton
    @Provides
    fun provideBaseRemoteDataSource(
        cryptoService: CryptoService,
        localRepository: LocalRepository
    ): BaseRemoteDataSource =
        BaseRemoteDataSourceImpl(cryptoService, localRepository)

    @Provides
    fun providePreviewRepository(
        pathManager: PathManager,
        edsApiService: EdsApiService,
        baseRemoteDataSource: BaseRemoteDataSource,
        updateFormDataSource: UpdateFormDataSource,
    ): ElectronicDocRepository =
        ElectronicDocRepositoryImpl(pathManager, edsApiService, baseRemoteDataSource, updateFormDataSource)

    @Qualifier
    @Retention(AnnotationRetention.BINARY)
    annotation class CryptoRetrofit

    @CryptoRetrofit
    @Singleton
    @Provides
    fun provideCryptoRetrofit(okHttpClient: OkHttpClient, cryptoService: CryptoService): Retrofit =
        Retrofit.Builder()
            .addConverterFactory(
                if (BuildConfig.ENCRYPT_API) {
                    CryptoGsonConverterFactory(cryptoService)
                } else {
                    GsonConverterFactory.create()
                }
            )
            .addConverterFactory(EnumConverterFactory)
            .baseUrl(
                String.format(
                    "${BuildConfig.SERVER_PROTOCOL}://${BuildConfig.API_SERVER_URL}:${BuildConfig.API_SERVER_PORT}"
                )
            )
            .client(okHttpClient)
            .build()

    @Singleton
    @Provides
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit = Retrofit.Builder()
        .addConverterFactory(GsonConverterFactory.create())
        .addConverterFactory(EnumConverterFactory)
        .baseUrl(
            String.format(
                "${BuildConfig.SERVER_PROTOCOL}://${BuildConfig.API_SERVER_URL}:${BuildConfig.API_SERVER_PORT}"
            )
        )
        .client(okHttpClient)
        .build()

    @Singleton
    @Provides
    fun provideOkHttpClient(localRepository: LocalRepository): OkHttpClient {
        val httpClientBuilder = OkHttpClient.Builder()
            .connectTimeout(160, TimeUnit.SECONDS)
            .readTimeout(160, TimeUnit.SECONDS)
            .addInterceptor(HeaderInterceptor(localRepository))

        if (BuildConfig.DEBUG) {
            val httpLoggingInterceptor = HttpLoggingInterceptor()
            httpLoggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
            httpClientBuilder.addInterceptor(httpLoggingInterceptor)
        }

        return httpClientBuilder.build()
    }

    @Provides
    @Singleton
    fun provideIdentityApiService(@CryptoRetrofit retrofit: Retrofit): IdentityApiService =
        retrofit.create(IdentityApiService::class.java)

    @Provides
    @Singleton
    fun provideApplicationService(@CryptoRetrofit retrofit: Retrofit): ApplicationService =
        retrofit.create(ApplicationService::class.java)

    @Provides
    @Singleton
    fun provideAuthApiService(@CryptoRetrofit retrofit: Retrofit): AuthApiService =
        retrofit.create(AuthApiService::class.java)

    @Provides
    @Singleton
    fun provideUserApiService(@CryptoRetrofit retrofit: Retrofit): UserApiService =
        retrofit.create(UserApiService::class.java)

    @Provides
    @Singleton
    fun provideEdsApiService(@CryptoRetrofit retrofit: Retrofit): EdsApiService =
        retrofit.create(EdsApiService::class.java)

    @Provides
    @Singleton
    fun provideApkApiService(@CryptoRetrofit retrofit: Retrofit): ApkApiService =
        retrofit.create(ApkApiService::class.java)

    @Provides
    @Singleton
    fun provideProductApiService(@CryptoRetrofit retrofit: Retrofit): ProductApiService =
        retrofit.create(ProductApiService::class.java)

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

    @Singleton
    @Provides
    fun provideApplicationRemoteDataSource(
        applicationService: ApplicationService,
        baseRemoteDataSource: BaseRemoteDataSource
    ): ApplicationRemoteDataSource =
        ApplicationRemoteDataSource(applicationService, baseRemoteDataSource)
}