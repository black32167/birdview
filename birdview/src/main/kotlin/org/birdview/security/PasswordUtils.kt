package org.birdview.security

import org.springframework.security.crypto.codec.Hex
import java.security.MessageDigest


object PasswordUtils {
    fun hash(password:String): String =
            String(Hex.encode(MessageDigest.getInstance("SHA-256").digest(password.toByteArray())))
}