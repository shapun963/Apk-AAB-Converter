package com.shapun.apkaabconverter.util

import java.net.URI
import java.nio.file.FileSystem
import java.nio.file.FileSystems
import java.nio.file.Path

object FileUtils {
    @JvmStatic
    fun fileSystemForZip(pathToZip: Path): FileSystem {
        return try {
            FileSystems.getFileSystem(pathToZip.toUri())
        } catch (e: Exception) {
            try {
                FileSystems.getFileSystem(URI.create("zip:" + pathToZip.toUri()))
            } catch (e2: Exception) {
                FileSystems.newFileSystem(
                    URI.create("zip:" + pathToZip.toUri()),
                    HashMap<String,Any>()
                )
            }
        }
    }
}