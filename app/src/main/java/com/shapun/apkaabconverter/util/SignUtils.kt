package com.shapun.apkaabconverter.util

import android.content.Context
import com.android.apksig.ApkSigner
import com.android.tools.build.bundletool.model.exceptions.CommandExecutionException
import com.google.common.collect.ImmutableList
import java.io.InputStream
import java.security.KeyStore
import java.security.PrivateKey
import java.security.cert.Certificate
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.util.*


object SignUtils {
    private const val SIGNER_NAME = "Apk-AAB-Converter"
    fun getDebugSignerConfig(context: Context): ApkSigner.SignerConfig {
        val assets = context.assets
        val key = assets.open("testkey.pk8").use {
            (Class.forName("sun.security.pkcs.PKCS8Key").newInstance() as PrivateKey).apply {
                this.javaClass.getMethod("decode", InputStream::class.java).invoke(this, it)
            }
        }
        val cert = assets.open("testkey.x509.pem").use {
            (CertificateFactory.getInstance("X.509").generateCertificate(it) as X509Certificate)
        }
        return ApkSigner.SignerConfig.Builder(SIGNER_NAME,key,ImmutableList.of(cert)).build()
    }

    fun getKeyStore(inputStream: InputStream, password: String): KeyStore {
        val keystore: KeyStore = KeyStore.getInstance("PKCS12")
        keystore.load(inputStream, password.toCharArray())
        return keystore
    }

    fun getSignerConfig(
        inputStream: InputStream,
        keyAlias: String,
        keystorePassword: String,
        keyPassword: String
    ): ApkSigner.SignerConfig {
        return getSignerConfig(
            getKeyStore(inputStream, keystorePassword), keyAlias, keyPassword
        )
    }

    fun getSignerConfig(
        keystore: KeyStore, keyAlias: String, keyPassword: String
    ): ApkSigner.SignerConfig {
        val privateKey = keystore.getKey(keyAlias, keyPassword.toCharArray()) as PrivateKey
        val certChain = keystore.getCertificateChain(keyAlias)
            ?: throw CommandExecutionException.builder()
                .withInternalMessage("No key found with alias '%s' in keystore.", keyAlias)
                .build()
        val certificates = Arrays.stream(certChain).map { c: Certificate? -> c as X509Certificate? }
            .collect(ImmutableList.toImmutableList())
        return ApkSigner.SignerConfig.Builder(SIGNER_NAME, privateKey, certificates)
            .build()
    }
}