package com.shapun.apkaabconverter.converter;

import android.content.Context;
import com.android.tools.build.bundletool.BundleToolMain;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;

public class AabToApkConverter extends FileConverter{
	
	private final File AAPT2Binary;
    
    public AabToApkConverter(Builder builder) {
        super(builder);
		AAPT2Binary = new File(getContext().getApplicationInfo().nativeLibraryDir, "libaapt2.so");
    }

    public void start() {	
		addLog("Starting apk to AAB");		
		PrintStream oldErrorStream = System.err;
        PrintStream oldOutputStream = System.out;
        ByteArrayOutputStream errorStream = new ByteArrayOutputStream();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		addLog("Setting up streams");
        System.setErr(new PrintStream(errorStream));
        System.setOut(new PrintStream(outputStream));

        ArrayList<String> args = new ArrayList<>();
        args.add("build-apks");
        args.add("--bundle=" + getInputFile().getAbsolutePath());
        args.add("--overwrite");
        args.add("--output=" + getOutputFile().getAbsolutePath());
        args.add("--mode=universal");
        args.add("--aapt2=" + AAPT2Binary.getAbsolutePath());
        BundleToolMain.main(args.toArray(new String[args.size()]));
		addLog(outputStream.toString());
		if (!errorStream.toString().isEmpty()) {
			addLog(errorStream.toString());
            throw new RuntimeException(errorStream.toString());
        }else{
			addLog("Successfully converted AAB to Apk");
		}
        System.out.flush();
        System.err.flush();
        System.setErr(oldErrorStream);
        System.setOut(oldOutputStream);
    }

	
    public static class Builder extends FileConverter.Builder<Builder>{
			
		public Builder(Context context,File aabFile,File outputPath){
			super(context,aabFile,outputPath);
		}
		@Override
		public AabToApkConverter build(){
			return new AabToApkConverter(this);
		}
        @Override
        protected Builder self() {
            return this;
        }
	}
}
