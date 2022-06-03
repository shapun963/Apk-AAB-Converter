package com.shapun.apkaabconverter.util

import android.content.Context
import com.android.apksig.ApkSigner
import com.android.tools.build.bundletool.model.exceptions.CommandExecutionException
import com.google.common.collect.ImmutableList
import java.io.InputStream
import java.security.KeyFactory
import java.security.KeyStore
import java.security.NoSuchAlgorithmException
import java.security.PrivateKey
import java.security.cert.Certificate
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.security.spec.InvalidKeySpecException
import java.security.spec.PKCS8EncodedKeySpec
import java.util.*
import java.util.stream.Collectors


object SignUtils {
    private const val SIGNER_NAME = "Apk-AAB-Converter"

    fun getDebugSignerConfig(context: Context): ApkSigner.SignerConfig {
        val assets = context.assets
        val privateKey: PrivateKey
        assets.open("testkey.pk8").use {
            val privateKeyBlob = it.readBytes()
            val keySpec = PKCS8EncodedKeySpec(privateKeyBlob)
            // Load the private key from its PKCS #8 encoded form.
            try {
                 privateKey = loadPkcs8EncodedPrivateKey(keySpec)!!
            } catch (e: InvalidKeySpecException) {
                throw InvalidKeySpecException(
                    "Failed to load PKCS #8 encoded private key ", e
                )
            }
        }
        val certs = assets.open("testkey.x509.pem").use { inputStream ->
            CertificateFactory.getInstance("X.509").generateCertificates(inputStream)
                .stream()
                .map { it as X509Certificate }
                .collect(Collectors.toList())
        }
        return ApkSigner.SignerConfig.Builder(SIGNER_NAME,privateKey,certs).build()
    }

    @Throws(InvalidKeySpecException::class, NoSuchAlgorithmException::class)
    private fun loadPkcs8EncodedPrivateKey(spec: PKCS8EncodedKeySpec): PrivateKey? {
        try {
            return KeyFactory.getInstance("RSA").generatePrivate(spec)
        } catch (expected: InvalidKeySpecException) {
        }
        try {
            return KeyFactory.getInstance("EC").generatePrivate(spec)
        } catch (expected: InvalidKeySpecException) {
        }
        try {
            return KeyFactory.getInstance("DSA").generatePrivate(spec)
        } catch (expected: InvalidKeySpecException) {
        }
        throw InvalidKeySpecException("Not an RSA, EC, or DSA private key")
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