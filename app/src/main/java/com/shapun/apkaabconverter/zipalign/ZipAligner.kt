package com.shapun.apkaabconverter.zipalign

import com.google.common.collect.ImmutableList
import com.shapun.apkaabconverter.App
import java.io.File
import java.io.StringWriter
import java.nio.file.Path
import java.time.Duration
import java.util.*
import java.util.stream.Collectors
import kotlin.io.path.absolutePathString

class ZipAligner(inputPath: Path, outputPath: Path) {

    private val mInputPath: Path = inputPath
    private val mOutputPath:Path = outputPath
    private var mVerbose = false

    fun setVerbose(verbose: Boolean) {
        mVerbose = verbose
    }

    fun align() {
        val processBuilder = ProcessBuilder()
        val args = ImmutableList.of(
            File(App.context.applicationInfo.nativeLibraryDir, "libzipalign.so").toString(),
            if (mVerbose) "-v" else "",
            "-f",
            "4",
            mInputPath.absolutePathString(),
            mOutputPath.absolutePathString()
        )
        processBuilder.command(args)
        val process: Process = processBuilder.start()
        val scanner = Scanner(process.errorStream)
        val errorList = mutableListOf<String>()
        while (scanner.hasNextLine()) {
            errorList.add(scanner.nextLine())
        }
        process.waitFor()
        if(errorList.stream().anyMatch{it.startsWith("ERROR:")}) {
            throw Exception(errorList.stream().collect(Collectors.joining("\n")))
        }
    }
}