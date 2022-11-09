package com.inzisoft.ibks.data.remote.converter

interface CryptoService {

    fun encrypt(data: ByteArray, wrapBase64: Boolean = false): ByteArray

    fun decrypt(data: ByteArray): ByteArray

}