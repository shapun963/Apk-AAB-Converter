package com.shapun.apkaabconverter.signing.model

/**
 * Signing config.
 *
 * @param storePassword a password for the key store
 * @param keyPassword a password for the key
 * @param keyAlias the alias under which the key is stored in the store
 *
 * @author qingyu
 * <p>
 * Create on 2023/10/16 15:44
 */
data class SigningConfig(
    val storePassword: String,
    val keyPassword: String,
    val keyAlias: String
)
