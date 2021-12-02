package com.shapun.apkaabconverter.converter;

import android.content.Context;
import com.android.tools.build.bundletool.BundleToolMain;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class ApkToAABConverter extends FileConverter {
    private static final int BUFFER_SIZE = 1024 * 2;
    private final File AAPT2Binary;
    private final File mTempDir;
    private final File mProtoOutput;
    private final File mBaseZip;

    public ApkToAABConverter(Builder builder) {
        super(builder);
        AAPT2Binary = new File(getContext().getApplicationInfo().nativeLibraryDir, "libaapt2.so");
        mTempDir = new File(getContext().getExternalFilesDir(null), "temp");
        mTempDir.mkdirs();
        mProtoOutput = new File(mTempDir, "proto.zip");
        mBaseZip = new File(mTempDir, "base.zip");
    }

    @Override
    public void start() throws Exception {
        createProtoFormatZip();
        createBaseZip();
        buildAab();
    }

    private void createProtoFormatZip() throws Exception {
        mProtoOutput.delete();
        mProtoOutput.createNewFile();
        addLog("Creating proto format zip");
        ProcessBuilder processBuilder = new ProcessBuilder();
        StringWriter stringWriter = new StringWriter();
        List<String> args = new ArrayList<>();
        args.add(AAPT2Binary.getAbsolutePath());
        args.add("convert");
        args.add(getInputFile().getAbsolutePath());
        args.add("-o");
        args.add(mProtoOutput.getAbsolutePath());
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

        try (ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(mProtoOutput))) {
            try (ZipOutputStream zipOutputStream = new ZipOutputStream(new FileOutputStream(mBaseZip))) {
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
					addLog("Adding "+entry.getName()+" to base.zip");
					
                    byte[] buffer = new byte[BUFFER_SIZE];
                    int len;
                    while ((len = zipInputStream.read(buffer)) != -1) {
                        zipOutputStream.write(buffer, 0, len);
                    }
                }
            }
        }
    }

    private void buildAab() throws IOException {
        addLog("Creating aab");
        PrintStream oldErrorStream = System.err;
        PrintStream oldOutputStream = System.out;
        ByteArrayOutputStream errorStream = new ByteArrayOutputStream();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        System.setErr(new PrintStream(errorStream));
        System.setOut(new PrintStream(outputStream));
        try {
            getOutputFile().createNewFile();
            ArrayList<String> args = new ArrayList<>();
            args.add("build-bundle");
            args.add("--modules=" + mBaseZip.getAbsolutePath());
            args.add("--overwrite");
            args.add("--output=" + getOutputFile().getAbsolutePath());
            BundleToolMain.main(args.toArray(new String[0]));

            if (!errorStream.toString().isEmpty()) {
                addLog(errorStream.toString());
                throw new RuntimeException(errorStream.toString());
            }
            addLog("Successfully converted Apk to AAB");
        } finally {
            System.out.flush();
            System.err.flush();
            System.setErr(oldErrorStream);
            System.setOut(oldOutputStream);
        }
    }

    public static class Builder extends FileConverter.Builder<Builder> {

        public Builder(Context context, File apkFile, File outputPath) {
            super(context, apkFile, outputPath);
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
