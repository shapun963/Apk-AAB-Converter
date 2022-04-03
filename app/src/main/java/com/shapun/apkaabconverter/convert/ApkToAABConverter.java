package com.shapun.apkaabconverter.convert;

import android.content.Context;

import com.android.tools.build.bundletool.commands.BuildBundleCommand;
import com.google.common.collect.ImmutableList;
import com.shapun.apkaabconverter.model.MetaData;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class ApkToAABConverter extends FileConverter {
    private static final int BUFFER_SIZE = 1024 * 2;
    private final File AAPT2Binary;
    private final Path mProtoOutput;
    private final Path mBaseZip;
    private final Path mConfigPath;
    private final List<MetaData> mMetaData;

    public ApkToAABConverter(Builder builder) {
        super(builder);
        AAPT2Binary = new File(getContext().getApplicationInfo().nativeLibraryDir, "libaapt2.so");
        String dirPath = getContext().getExternalCacheDir().getAbsolutePath()+File.separator+"temp";
        Path mTempDir = Paths.get(dirPath);
        try {
            Files.createDirectories(mTempDir);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        mProtoOutput = Paths.get(dirPath,"proto.zip");
        mBaseZip = Paths.get(dirPath,"base.zip");
        mConfigPath = builder.configPath;
        mMetaData = builder.metaData;
    }

    @Override
    public void start() throws Exception {
        createProtoFormatZip();
        createBaseZip();
        buildAab();
    }

    private void createProtoFormatZip() throws Exception {
        if(!Files.exists(mProtoOutput))Files.createFile(mProtoOutput);
        addLog("Creating proto formatted zip");
        ProcessBuilder processBuilder = new ProcessBuilder();
        StringWriter stringWriter = new StringWriter();
        List<String> args = new ArrayList<>();
        args.add(AAPT2Binary.getAbsolutePath());
        args.add("convert");
        args.add(getInputPath().toAbsolutePath().toString());
        args.add("-o");
        args.add(mProtoOutput.toAbsolutePath().toString());
        args.add("--output-format");
        args.add("proto");
        processBuilder.command(args);
        Process process = processBuilder.start();
        Scanner scanner = new Scanner(process.getErrorStream());
        boolean hasError = false;
        while (scanner.hasNextLine()) {
            hasError = true;
            String log = scanner.nextLine();
            addLog(log);
            stringWriter.append(log);
            stringWriter.append(System.lineSeparator());
        }
        process.waitFor();
        if (hasError) throw new Exception(stringWriter.toString());
    }

    private void createBaseZip() throws IOException {
        addLog("Creating base.zip");
        try (ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(mProtoOutput.toFile()))) {
            try (ZipOutputStream zipOutputStream = new ZipOutputStream(new FileOutputStream(mBaseZip.toFile()))) {
                ZipEntry entry;
                while ((entry = zipInputStream.getNextEntry()) != null) {
                    if (entry.getName().endsWith(".dex") && entry.getName().startsWith("classes")) {
                        zipOutputStream.putNextEntry(
                                new ZipEntry("dex" + File.separator + entry.getName()));
                    } else if (entry.getName().equals("AndroidManifest.xml")) {
                        zipOutputStream.putNextEntry(
                                new ZipEntry("manifest" + File.separator + entry.getName()));
                    } else if (entry.getName().startsWith("res" + File.separator)) {
                        zipOutputStream.putNextEntry(new ZipEntry(entry));
                    } else if (entry.getName().startsWith("lib" + File.separator)) {
                        zipOutputStream.putNextEntry(new ZipEntry(entry));
                    } else if (entry.getName().equals("resources.pb")) {
                        zipOutputStream.putNextEntry(new ZipEntry(entry));
                    } else if (entry.getName().startsWith("assets" + File.separator)) {
                        zipOutputStream.putNextEntry(new ZipEntry(entry));
                        // the META-INF folder may contain non-signature-related resources
                        // as well, so we check if the entry doesn't point to a signature
                        // file before adding it
                    } else if (!entry.getName().endsWith(".RSA")
                            && !entry.getName().endsWith(".SF")
                            && !entry.getName().endsWith(".MF")) {
                        zipOutputStream.putNextEntry(new ZipEntry("root" + File.separator + entry.getName()));
                    } else {
                        continue;
                    }
                    if(isVerbose())addLog("Adding " + entry.getName() + " to base.zip");
                    byte[] buffer = new byte[BUFFER_SIZE];
                    int len;
                    while ((len = zipInputStream.read(buffer)) != -1) {
                        zipOutputStream.write(buffer, 0, len);
                    }
                }
            }
        }
    }

    private void buildAab(){
        addLog("Creating aab");
        try {
            BuildBundleCommand.Builder builder = BuildBundleCommand.builder()
                    .setModulesPaths(ImmutableList.of(mBaseZip))
                    .setOutputPath(getOutputPath())
                    .setOverwriteOutput(true);
            if (mConfigPath != null) {
                builder.setBundleConfig(mConfigPath);
            }
            for (MetaData metaData : mMetaData) {
                builder.addMetadataFile(metaData.getDirectory(), metaData.getFileName(), metaData.getPath());
            }
            builder.build().execute();
            addLog("Successfully converted Apk to AAB");
        } catch (Exception e) {
            addLog(e.toString());
            throw new RuntimeException(e.toString());
        }
    }

    public static class Builder extends FileConverter.Builder<Builder> {
        private Path configPath;
        private final List<MetaData> metaData;

        public Builder(Context context, Path apkPath, Path outputPath) {
            super(context, apkPath, outputPath);
            metaData = new ArrayList<>();
        }

        public Builder setConfigFile(Path configPath) {
            this.configPath = configPath;
            return this;
        }

        public Builder addMetaData(MetaData metaData) {
            this.metaData.add(metaData);
            return this;
        }

        @Override
        public ApkToAABConverter build() {
            return new ApkToAABConverter(this);
        }

        @Override
        protected Builder self() {
            return this;
        }
    }
}
