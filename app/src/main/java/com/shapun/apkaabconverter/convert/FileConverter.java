package com.shapun.apkaabconverter.convert;

import android.content.Context;
import java.nio.file.Path;

public abstract class FileConverter {

	private final Context mContext;
    private final Path mInputPath;
    private final Path mOutputPath;
    private boolean mVerbose;
	private final Logger mLogger;
    
	public FileConverter(Builder builder){
		mInputPath = builder.inputPath;
        mOutputPath = builder.outputPath;
        mContext = builder.context;
        mVerbose = builder.verbose;
		mLogger = builder.logger;
	}
	
	abstract void start() throws Exception;

	public void addLog(String logText) {
		if (getLogger() != null) getLogger().add(logText);

	}

	public Context getContext() {
		return mContext;

	}

	public Path getInputPath() {
		return mInputPath;

	}

	public Path getOutputPath() {
		return mOutputPath;

	}

	public Logger getLogger() {
		return mLogger;
	}

	public boolean isVerbose() {
		return mVerbose;
	}

	public abstract static class Builder<T extends Builder<T>>{
		private final Context context;
		private final Path inputPath;
		private final Path outputPath;
		private boolean verbose = false;
		private Logger logger;
		public Builder(Context context,Path inputPath,Path outputPath){
			this.context = context;
			this.inputPath = inputPath;
			this.outputPath = outputPath;
		}
		public T verbose(){
			this.verbose = true;
			return self();
		}
		public T setVerbose(boolean verbose){
			this.verbose = verbose;
			return self();
		}
		public T setLogger(Logger logger){
			this.logger = logger;
			return self();
		}
		public abstract FileConverter build();
		
		protected abstract T self();
	}
}