package com.shapun.apkaabconverter.convert;

import android.content.Context;

import com.android.apksig.ApkSigner;
import com.android.tools.build.bundletool.androidtools.Aapt2Command;
import com.android.tools.build.bundletool.commands.BuildApksCommand;
import com.android.tools.build.bundletool.model.SigningConfiguration;
import com.google.common.collect.ImmutableList;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.nio.file.Path;

public class AABToApkConverter extends FileConverter {

    private final File AAPT2Binary;
    private final ApkSigner.SignerConfig signerConfig;

    public AABToApkConverter(Builder builder) {
        super(builder);
        AAPT2Binary = new File(getContext().getApplicationInfo().nativeLibraryDir, "libaapt2.so");
        signerConfig = builder.signerConfig;
    }

    public void start() {
        addLog("Starting apk to AAB");
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Aapt2Command aapt2Command = Aapt2Command.createFromExecutablePath(AAPT2Binary.toPath());
        BuildApksCommand.Builder builder = BuildApksCommand.builder()
                .setAapt2Command(aapt2Command)
                .setBundlePath(getInputPath())
                .setOutputFile(getOutputPath())
                .setOverwriteOutput(true)
                .setApkBuildMode(BuildApksCommand.ApkBuildMode.UNIVERSAL)
                .setOutputPrintStream(new PrintStream(outputStream));
        if (signerConfig != null)
            builder.setSigningConfiguration(SigningConfiguration.builder()
                    .setSignerConfig(signerConfig.getPrivateKey(),
                            ImmutableList.copyOf(signerConfig.getCertificates()))
                    .build());
        builder.build().execute();
        addLog(outputStream.toString());
        addLog("Successfully converted AAB to Apk");
    }

    public static class Builder extends FileConverter.Builder<Builder> {
        private ApkSigner.SignerConfig signerConfig;

        public Builder(Context context, Path aabPath, Path outputPath) {
            super(context, aabPath, outputPath);
        }

        public Builder setSignerConfig(ApkSigner.SignerConfig signerConfig) {
            this.signerConfig = signerConfig;
            return this;
        }

        @Override
        public AABToApkConverter build() {
            return new AABToApkConverter(this);
        }

        @Override
        protected Builder self() {
            return this;
        }
    }
}
