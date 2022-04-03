package com.shapun.apkaabconverter.util

import android.content.Context
import com.android.tools.build.bundletool.model.SigningConfiguration
import java.io.InputStream
import java.security.PrivateKey
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.util.*

object SignUtils {

    fun getDebugSigningConfiguration(context: Context): SigningConfiguration{
        val assets = context.assets
        val key = assets.open("testkey.pk8").use {
            (Class.forName("sun.security.pkcs.PKCS8Key").newInstance() as PrivateKey).apply {
                this.javaClass.getMethod("decode", InputStream::class.java).invoke(this, it)
            }
        }
        val cert = assets.open("testkey.x509.pem").use {
            (CertificateFactory.getInstance("X.509").generateCertificate(it) as X509Certificate)
        }
        return SigningConfiguration.builder().setSignerConfig(key,cert).build();
    }

    fun extractFromKeystore(
        keystore: InputStream,
        keyAlias: String,
        optionalKeystorePassword: Optional<String>,
        optionalKeyPassword: Optional<String>
    ): SigningConfiguration? {
        return null
    }
}