package org.birdview.utils

import org.springframework.beans.factory.annotation.Value
import java.security.spec.KeySpec
import java.util.*
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec
import javax.inject.Named

@Named
class CryptoMapper(
    private val jsonMapper:JsonMapper,
    @Value("\${document.secret}") private val secret: String
) {
    companion object {
       private const val FORMAT_VERSION = "01"
    }

    private val secretKey:SecretKey

    init {
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val spec: KeySpec = PBEKeySpec(secret.toCharArray(), byteArrayOf(1), 65536, 256)
        secretKey = SecretKeySpec(
            factory.generateSecret(spec)
                .encoded, "AES"
        )
    }

    fun serialize(input: Any): String =
        jsonMapper.serializeToString(input)
            .toByteArray(Charsets.UTF_8)
            .let { bytes ->
                Cipher.getInstance("AES")
                    .also { it.init(Cipher.ENCRYPT_MODE, secretKey) }
                    .doFinal(bytes)
            }
            .let { encoded->
                Base64.getEncoder().encodeToString(encoded)
            }
            .let { base64 -> "${FORMAT_VERSION}${base64}" }

    fun <T> deserialize(input:String, targetClass:Class<T>) =
        if (input.take(FORMAT_VERSION.length) != FORMAT_VERSION) {
            throw IllegalArgumentException("Unsupported format")
        } else {
            Base64.getDecoder().decode(input.substring(FORMAT_VERSION.length))
                .let { crypted ->
                    Cipher.getInstance("AES")
                        .also { it.init(Cipher.DECRYPT_MODE, secretKey) }
                        .doFinal(crypted)
                }
                .let { decrypted ->
                    jsonMapper.deserializeString(
                        decrypted.toString(Charsets.UTF_8),
                        targetClass
                    )
                }
        }
}
