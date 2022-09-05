package com.marblevhs.clairsavedimages.utils

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties.*
import com.marblevhs.clairsavedimages.di.AppScope
import java.nio.ByteBuffer
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject

@AppScope
class SecurityUtil
@Inject
constructor() {
    private val provider = "AndroidKeyStore"
    private val cipher by lazy {
        Cipher.getInstance("AES/GCM/NoPadding")
    }
    private val charset by lazy {
        Charsets.UTF_8

    }
    private val keyStore by lazy {
        KeyStore.getInstance(provider).apply {
            load(null)
        }
    }
    private val keyGenerator by lazy {
        KeyGenerator.getInstance(KEY_ALGORITHM_AES, provider)
    }

    fun encryptData(keyAlias: String, text: String): ByteArray {
        cipher.init(Cipher.ENCRYPT_MODE, generateSecretKey(keyAlias))
        val cipherText = cipher.doFinal(text.toByteArray(charset))
        val byteBuffer: ByteBuffer = ByteBuffer.allocate(cipher.iv.size + cipherText.size)
        byteBuffer.put(cipher.iv)
        byteBuffer.put(cipherText)
        return byteBuffer.array()
    }

    fun decryptData(keyAlias: String, encryptedData: ByteArray): String {
        val gcmIv = GCMParameterSpec(128, encryptedData, 0, 12)
        cipher.init(Cipher.DECRYPT_MODE, getSecretKey(keyAlias), gcmIv)
        return cipher.doFinal(encryptedData, 12, encryptedData.size - 12).toString(charset)
    }

    private fun generateSecretKey(keyAlias: String): SecretKey {
        return keyGenerator.apply {
            init(
                KeyGenParameterSpec
                    .Builder(keyAlias, PURPOSE_ENCRYPT or PURPOSE_DECRYPT)
                    .setBlockModes(BLOCK_MODE_GCM)
                    .setEncryptionPaddings(ENCRYPTION_PADDING_NONE)
                    .build()
            )
        }.generateKey()
    }

    private fun getSecretKey(keyAlias: String): SecretKey {
        return ((keyStore.getEntry(keyAlias, null)) as KeyStore.SecretKeyEntry).secretKey
    }
}