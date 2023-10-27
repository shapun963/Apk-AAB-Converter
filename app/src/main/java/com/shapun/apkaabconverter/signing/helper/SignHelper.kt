package com.shapun.apkaabconverter.signing.helper

import com.shapun.apkaabconverter.App
import com.shapun.apkaabconverter.signing.model.CertificateInfo
import com.shapun.apkaabconverter.signing.model.SigningConfig
import java.io.File
import java.util.Locale
import java.util.Properties
import java.util.zip.Deflater

/**
 * Helper for sign.
 *
 * @author qingyu
 * <p>
 * Create on 2023/10/16 16:07
 */
object SignHelper {

    /**
     * Returns the built-in debug signing certificate information.
     *
     * @return the certificate info if it could be loaded
     */
    fun getDebugSigningCertInfo(): CertificateInfo {
        // default debug keystore config
        val debugSigningConfig = SigningConfig(
            storePassword = "android",
            keyPassword = "android",
            keyAlias = "androiddebugkey",
        )
        val keyStore = KeystoreHelper.loadKeyStore(
            App.context.assets.open("debug.keystore"),
            debugSigningConfig.storePassword
        )
        return KeystoreHelper.getKeystoreCertInfo(keyStore, debugSigningConfig)
    }

    /**
     * Use flinger to sign archive.
     *
     * @param certificateInfo the certificate info
     * @param src archive to sign
     * @param dest output file for signed
     */
    fun signWithFlinger(certificateInfo: CertificateInfo, src: File, dest: File) {
        AabFlinger(
            outputFile = dest,
            signerName = getSignerName(certificateInfo),
            privateKey = certificateInfo.key,
            certificates = listOf(certificateInfo.certificate),
            minSdkVersion = 18 // So that RSA + SHA256 are used
        ).use { aabFlinger ->
            aabFlinger.writeZip(
                src,
                Deflater.DEFAULT_COMPRESSION
            )
        }
    }

    /**
     * Return Signer Name based on certificate information for [AabFlinger].
     *
     * @param certificateInfo the certificate info
     *
     * @return If the subject distinguished name can be obtained from the certificate,
     * and the 'CN' attribute can be resolved, then it is returned.
     * otherwise the certificate's alias is returned.
     */
    private fun getSignerName(certificateInfo: CertificateInfo) = kotlin.runCatching {
        Properties().let { prop ->
            prop.load(certificateInfo.certificate.subjectX500Principal.name.replace(',', '\n').reader())
            prop.getProperty("CN") ?: throw NullPointerException()
        }
    }.getOrDefault(certificateInfo.keyAlias.uppercase(Locale.US))
}
