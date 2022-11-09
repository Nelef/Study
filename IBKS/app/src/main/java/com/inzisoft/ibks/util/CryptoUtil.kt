package com.inzisoft.ibks.util

import android.content.Context
import android.util.Base64
import com.inzisoft.mobile.data.CryptoManager
import com.inzisoft.mobile.data.CryptoParameter
import com.inzisoft.mobile.data.MIDReaderProfile

object CryptoUtil {
    /**
     * Input Normal String -> Encrypt -> Return Base64 String
     */
    fun encrypt(context : Context, value : String) : String {
        return Base64.encodeToString(
            encrypt(context, value.toByteArray(Charsets.UTF_8)),
            Base64.NO_WRAP)
    }

    /**
     * Input ByteArray -> Encrypt -> return Encrypted ByteArray
     */
    fun encrypt(context : Context, value : ByteArray) : ByteArray {
        setConfig()
        val cryptoManager = CryptoManager.newInstance(context)
        val encryptValue = cryptoManager.getEncryptMem(value)
        cryptoManager.destroy()
        clearConfig()

        return encryptValue
    }

    /**
     * Input Base64 String -> Decrypt -> return Decrypted String
     */
    fun decrypt(context : Context, value : String) : String {
        return String(decrypt(context, Base64.decode(value, Base64.NO_WRAP)))
    }

    /**
     * Input ByteArray -> Decrypt -> return Decrypted ByteArray
     */
    fun decrypt(context : Context, value : ByteArray) : ByteArray {
        setConfig()
        val cryptoManager = CryptoManager.newInstance(context)
        val decryptValue = cryptoManager.getDecryptoMem(value)
        cryptoManager.destroy()
        clearConfig()

        return decryptValue
    }

    /**
     * CryptManager Configuration Setting
     */
    private fun setConfig() {
        // 암호화 관련 설정
        MIDReaderProfile.getInstance().ENCRYPT_MODE_TYPE = MIDReaderProfile.ENC_MODE_AES256 // Default
        MIDReaderProfile.getInstance().ENC_FILE_NAME = "inzi_enc_qa.dat"
        val param = CryptoParameter() // AES 암호화 옵션 설정
        param.setBlockSize(CryptoParameter.BLOCK_SIZE_256_BIT); // default
        param.setBlockPaddingMode(CryptoParameter.BLOCK_PADDING_MODE_BLANKS); // default
        param.setChaningOperMode(CryptoParameter.CHANING_OPERATION_MODE_ECB); // default
        MIDReaderProfile.getInstance().ENC_PARAM = param;
    }

    /**
     * CryptoManager Configuration clear (Encrypt/Decrypt Key)
     */
    private fun clearConfig() {
        MIDReaderProfile.getInstance().clearKey()
    }
}