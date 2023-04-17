package com.inzisoft.ibks.data.remote.converter

import com.google.gson.Gson
import com.google.gson.TypeAdapter
import com.google.gson.reflect.TypeToken
import com.inzisoft.ibks.BuildConfig
import com.inzisoft.ibks.util.log.QLog
import okhttp3.MediaType
import okhttp3.RequestBody
import okhttp3.ResponseBody
import okio.Buffer
import retrofit2.Converter
import retrofit2.Retrofit
import java.io.OutputStreamWriter
import java.lang.reflect.Type
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets

class CryptoGsonConverterFactory(
    private val cryptoService: CryptoService,
    private val gson: Gson = Gson()
) : Converter.Factory() {

    override fun requestBodyConverter(
        type: Type,
        parameterAnnotations: Array<out Annotation>,
        methodAnnotations: Array<out Annotation>,
        retrofit: Retrofit
    ): Converter<*, RequestBody> {
        val adapter = gson.getAdapter(TypeToken.get(type))
        return RequestConverter(cryptoService, gson, adapter)
    }

    override fun responseBodyConverter(
        type: Type,
        annotations: Array<out Annotation>,
        retrofit: Retrofit
    ): Converter<ResponseBody, *> {
        val adapter = gson.getAdapter(TypeToken.get(type))
        return ResponseConverter(cryptoService, adapter)
    }
}

private class RequestConverter<T>(
    private val cryptoService: CryptoService,
    private val gson: Gson,
    private val adapter: TypeAdapter<T>
) : Converter<T, RequestBody> {

    companion object {
        private val MEDIA_TYPE = MediaType.parse("application/json")
    }

    override fun convert(value: T): RequestBody? {
        val buffer = Buffer()
        val writer = OutputStreamWriter(buffer.outputStream(), Charset.forName("UTF-8"))
        gson.newJsonWriter(writer).use {
            adapter.write(it, value)
        }

        val encrypted = cryptoService.encrypt(buffer.readByteArray())

        if (BuildConfig.DEBUG) {
            QLog.d("origin : $value\n\nencrypted : ${String(encrypted)}")
            val decrypted = cryptoService.decrypt(encrypted)
            QLog.d("decrypted : ${String(decrypted)}")
        }

        return RequestBody.create(MEDIA_TYPE, encrypted)
    }

}

class ResponseConverter<T>(
    private val cryptoService: CryptoService,
    private val adapter: TypeAdapter<T>
) : Converter<ResponseBody, T> {

    override fun convert(value: ResponseBody): T {
        return value.use {
            val json = String(cryptoService.decrypt(value.bytes()), StandardCharsets.UTF_8)
            adapter.fromJson(json)
        }
    }

}
