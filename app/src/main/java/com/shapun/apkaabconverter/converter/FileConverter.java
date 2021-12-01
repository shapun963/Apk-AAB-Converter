package com.shapun.apkaabconverter.converter;

import android.content.Context;
import java.io.File;

public abstract class FileConverter {
    private final Context mContext;
    private final File mInputFile;
    private final File mOutputFile;
	private final Logger mLogger;
    
	public FileConverter(Builder builder){
		mInputFile = builder.inputFile;
        mOutputFile = builder.outputFile;
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
	protected File getInputFile(){
		return mInputFile;
	}
	protected File getOutputFile(){
		return mOutputFile;
	}
	protected Logger getLogger(){
		return mLogger;
	}
	
	public abstract static class Builder<T extends Builder<T>>{
		private final Context context;
		private final File inputFile;
		private final File outputFile;
		private Logger logger;
		public Builder(Context context,File inputFile,File outputFile){
			this.context = context;
			this.inputFile = inputFile;
			this.outputFile = outputFile;
		}
		public T setLogger(Logger logger){
			this.logger = logger;
			return self();
		}
		public abstract FileConverter build();
		
		protected abstract T self();
	}
}