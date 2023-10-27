package com.shapun.apkaabconverter.signing.model

import java.security.PrivateKey
import java.security.cert.X509Certificate

/**
 * Signing information.
 *
 * Both the [PrivateKey] and the [X509Certificate] are guaranteed to be non-null.
 *
 * @author qingyu
 * <p>
 * Create on 2023/10/16 11:10
 */
data class CertificateInfo(
    val key: PrivateKey,
    val keyAlias: String,
    val certificate: X509Certificate
)
