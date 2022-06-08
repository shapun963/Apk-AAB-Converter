package com.shapun.apkaabconverter.convert;

public class Logger {
	
	private final StringBuilder mLogs ;
	private LogListener mLogListener;
	
	public Logger(){
		mLogs = new StringBuilder();
	}
	
	public void add(String logText){
		if(mLogListener != null){
			mLogListener.onLogAdded(logText);
		}
		if(mLogs.length() != 0){
			mLogs.append(System.lineSeparator());
		}
		mLogs.append(logText);
	}
	
	public void setLogListener(LogListener listener){
		mLogListener = listener;
	}
	
	public String getLogs(){
		return toString();
	}
	
	@Override
	public String toString(){
		return mLogs.toString();
	}
	
	@FunctionalInterface
	public interface LogListener{
		void onLogAdded(String log);
	}
}