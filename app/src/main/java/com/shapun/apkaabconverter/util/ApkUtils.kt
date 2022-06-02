package com.shapun.apkaabconverter.util

import com.android.apksig.apk.ApkUtils
import com.android.apksig.util.DataSources
import java.io.RandomAccessFile
import java.nio.file.Path

object ApkUtils {

    @JvmStatic
    fun getMinimumSdkVersion(apkPath: Path): Int {
        RandomAccessFile(apkPath.toFile(), "r").use {
            val inputApk = DataSources.asDataSource(it)
            val manifest = ApkUtils.getAndroidManifest(inputApk)
            return ApkUtils.getMinSdkVersionFromBinaryAndroidManifest(manifest)
        }
    }
}