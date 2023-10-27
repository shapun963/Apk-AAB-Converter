package com.shapun.apkaabconverter.signing.helper

import com.shapun.apkaabconverter.signing.jks.JavaKeyStore
import com.shapun.apkaabconverter.signing.model.CertificateInfo
import com.shapun.apkaabconverter.signing.model.SigningConfig
import java.io.ByteArrayInputStream
import java.io.IOException
import java.io.InputStream
import java.nio.ByteOrder
import java.security.KeyStore
import java.security.cert.X509Certificate


/**
 * Helper for keystore.
 *
 * @author qingyu
 * <p>
 * Create on 2023/10/16 11:03
 */
object KeystoreHelper {

    /**
     * Returns the [KeyStore] loaded from input stream.
     *
     * @param storeInputStream input stream of the keystore
     * @param storePassword password of the keystore
     *
     * @return the loaded keystore object if it could be loaded
     */
    fun loadKeyStore(storeInputStream: InputStream, storePassword: String): KeyStore {
        val bais = ByteArrayInputStream(storeInputStream.readBytes())
        storeInputStream.close()
        val ks = if (isJKS(bais)) JavaKeyStore() else KeyStore.getInstance("PKCS12")
        ks.load(bais, storePassword.toCharArray())
        return ks
    }

    /**
     * Returns the [CertificateInfo] for the given signing configuration.
     *
     * @param keyStore keystore loaded
     * @param signingConfig a sign config
     *
     * @return the certificate info if it could be loaded
     */
    fun getKeystoreCertInfo(keyStore: KeyStore, signingConfig: SigningConfig): CertificateInfo {
        val keyPasswordArray = signingConfig.keyPassword.toCharArray()
        val entry = keyStore.getEntry(
            signingConfig.keyAlias, KeyStore.PasswordProtection(keyPasswordArray)
        ) as? KeyStore.PrivateKeyEntry ?: throw IOException("No key with alias '${signingConfig.keyAlias}' found in keystore")

        return CertificateInfo(entry.privateKey, signingConfig.keyAlias, entry.certificate as X509Certificate)
    }

    /**
     * Check the input keystore is jks type.
     *
     * @param input the byte array input stream of keystore
     *
     * @return true if is jks, otherwise false
     */
    private fun isJKS(input: ByteArrayInputStream) = try {
        val bytes = ByteArray(4)
        input.read(bytes, 0, bytes.size)
        peekInt(bytes, 0, ByteOrder.BIG_ENDIAN) == 0XFEEDFEED.toInt()
    } catch (_: Throwable) {
        false
    } finally {
        input.reset() // ensure reset before load
    }

    /**
     * Gets int value from a byte buffer `src` at offset `offset` using
     * `order` byte order.
     *
     * @param src    source byte buffer
     * @param offset offset in `src` to get bytes from
     * @param order  byte order
     * @return int value
     */
    @Suppress("SameParameterValue")
    private fun peekInt(src: ByteArray, offset: Int, order: ByteOrder): Int {
        var pos = offset
        return if (order == ByteOrder.BIG_ENDIAN) {
            src[pos++].toInt() and 0xff shl 24 or
                    (src[pos++].toInt() and 0xff shl 16) or
                    (src[pos++].toInt() and 0xff shl 8) or
                    (src[pos].toInt() and 0xff shl 0)
        } else {
            src[pos++].toInt() and 0xff shl 0 or
                    (src[pos++].toInt() and 0xff shl 8) or
                    (src[pos++].toInt() and 0xff shl 16) or
                    (src[pos].toInt() and 0xff shl 24)
        }
    }
}
