package com.shapun.apkaabconverter.convert;

import android.content.Context;
import java.io.File;
import java.nio.file.Path;

public abstract class FileConverter {

	private final Context mContext;
    private final Path mInputPath;
    private final Path mOutputPath;
	private final Logger mLogger;
    
	public FileConverter(Builder builder){
		mInputPath = builder.inputPath;
        mOutputPath = builder.outputPath;
        mContext = builder.context;
		mLogger = builder.logger;
	}
	
	abstract void start() throws Exception;
	
	protected void addLog(String logText){
		if(getLogger() != null)getLogger().add(logText);
	}
	protected Context getContext(){
		return mContext;
	}
	protected Path getInputPath(){
		return mInputPath;
	}
	protected Path getOutputPath(){
		return mOutputPath;
	}
	protected Logger getLogger(){
		return mLogger;
	}
	
	public abstract static class Builder<T extends Builder<T>>{
		private final Context context;
		private final Path inputPath;
		private final Path outputPath;
		private Logger logger;
		public Builder(Context context,Path inputPath,Path outputPath){
			this.context = context;
			this.inputPath = inputPath;
			this.outputPath = outputPath;
		}
		public T setLogger(Logger logger){
			this.logger = logger;
			return self();
		}
		public abstract FileConverter build();
		
		protected abstract T self();
	}
}