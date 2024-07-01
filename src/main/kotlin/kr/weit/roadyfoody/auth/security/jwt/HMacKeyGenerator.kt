package kr.weit.roadyfoody.auth.security.jwt

import java.util.Base64
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

private val hMacKeyGenerator = KeyGenerator.getInstance("HmacSHA256")

fun generateHmac256Key(): String {
    val secretKey: SecretKey = hMacKeyGenerator.generateKey()
    val secretKeyBytes: ByteArray = secretKey.encoded
    val secretKeyBase64: String = Base64.getEncoder().encodeToString(secretKeyBytes)
    return secretKeyBase64
}
