package com.skyline.control;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import android.os.AsyncTask;
import android.os.Build;

public class CurrentProcesses {
	private class StreamClear extends Thread{
		private BufferedReader reader = null;
		private List<String> outputList = null;
		public StreamClear(InputStream inputStream) {
			this.reader = new BufferedReader(new InputStreamReader(inputStream));
		    this.outputList = Collections.synchronizedList(new ArrayList<String>());
		}
		@Override
	    public void run() {
			try {
				String line;
				if(outputList != null) {
					while((line = reader.readLine()) != null)
						outputList.add(line);
				} 
				else {
					while((line = reader.readLine()) != null);
				}
				reader.close();
		    } catch (IOException e) {		        
		    }
		}	
		public List<String> getOutputList() {
			return this.outputList;
		}
	}
	private class StartTask extends AsyncTask<CallBackStyle, Void, Void> {
		@Override
		protected Void doInBackground(CallBackStyle... params) {
			if (params[0].getOperationStyle().equals("kill")) {
				killCurrentProcesses();
			} else if (params[0].getOperationStyle().equals("get")) {
				params[0].getCallBack().currentProcessesInfo = getCurrentProcessesInfo();
				params[0].getCallBack().doSomething();
			}			
			return null;
		}
		private String getAppUserPattern() {
			String app_user_pattern = null;
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
				app_user_pattern = "u\\d+_a\\d+";
		    } else {
		    	app_user_pattern = "app_\\d+";
		    }
			return app_user_pattern;
		}
		private HashMap<String, ArrayList<ProcessInfo>> getCurrentProcessesInfo() {
			List<List<String>> __temp = shell("sh", "ps");
			if(__temp == null || __temp.size() != 2) return null;
			List<String> outList_temp = __temp.get(0);
			String app_user_pattern_temp = getAppUserPattern();
			if (outList_temp != null && !outList_temp.isEmpty() && app_user_pattern_temp != null) {
				HashMap<String, ArrayList<ProcessInfo>> currentProcessesInfo= new HashMap<String, ArrayList<ProcessInfo>>();
				String skyline_user  = null;
				for (String string : outList_temp) {
					String[] fields = string.split("\\s+");
					if (fields[0].matches(app_user_pattern_temp) && !fields[8].contains("android")) {
						if (currentProcessesInfo.containsKey(fields[0])) {
							currentProcessesInfo.get(fields[0]).add(new ProcessInfo(fields[0], Integer.valueOf(fields[1]), Integer.valueOf(fields[2]), fields[8]));
		           		} else {
		           			ArrayList<ProcessInfo> _temp = new ArrayList<ProcessInfo>();
		           			_temp.add(new ProcessInfo(fields[0], Integer.valueOf(fields[1]), Integer.valueOf(fields[2]), fields[8]));
		           			currentProcessesInfo.put(fields[0], _temp);
		           		}
						if(fields[8].contains("skyline")) skyline_user = fields[0];
					}
				}
				if (skyline_user != null) {
					currentProcessesInfo.remove(skyline_user);
				}				
				return  currentProcessesInfo;
			}
			return null;
		}
		private void killCurrentProcesses() {
			HashMap<String, ArrayList<ProcessInfo>> currentProcessesInfo = getCurrentProcessesInfo();
			if (currentProcessesInfo != null) {
				ArrayList<String> commands = new ArrayList<String>();
				for (Entry<String, ArrayList<ProcessInfo>> enty : currentProcessesInfo.entrySet()) {
					for (ProcessInfo _val : enty.getValue()) {
						if(_val.getPackageName() != null)
							commands.add("am force-stop " + _val.getPackageName());
						else 
							commands.add("kill -9 " + _val.getPID());
					}     	
				}
				shell("su", commands);
			}
		}	
		private List<List<String>> shell(String shell, String command) {
			ArrayList<String> temp = new ArrayList<String>();
			temp.add(command);
			return shell(shell, temp);
		}
		private List<List<String>> shell(String shell, ArrayList<String> commands) {
			Process process = null;
			List<List<String>> results = new ArrayList<List<String>>();
			try {
				if (shell.equals("sh")) {
					process = Runtime.getRuntime().exec("sh");
				} else if(shell.equals("su")) {
					process = Runtime.getRuntime().exec("su");
				} else {
					return null;
				}
				DataOutputStream std_in = new DataOutputStream(process.getOutputStream());
				StreamClear std_out = new StreamClear(process.getInputStream());
				StreamClear std_err = new StreamClear(process.getErrorStream());
				std_out.start();
				std_err.start();
				for (String string : commands) {
					std_in.write((string + "\n").getBytes("UTF-8"));
					std_in.flush();
				}
				std_in.write("exit\n".getBytes("UTF-8"));
				std_in.flush();
				process.waitFor();				
				std_in.close();				
				std_out.join();
				std_err.join();				
				process.destroy();	
				results.add(std_out.getOutputList());
				results.add(std_err.getOutputList());
				return results;
			} catch (IOException | InterruptedException e1) {
				e1.printStackTrace();
				return null;
			}			
		}
	}
	public static class ProcessInfo {
		private String user = null;
		private int PID = -1;
		private int PPID = -2;
		private String name = null;
		private String packageName = null;
		public ProcessInfo(String user, int PID, int PPID, String name) {
			this.user = user;
			this.PID = PID;
			this.PPID = PPID;
			this.name = name;
			if (name.contains("/") || !name.contains(".")) {
				this.packageName = null;
			} else if (name.contains(":")) {		
				this.packageName = name.split(":")[0];
			} else {
				this.packageName = name;
			}
		}
		public String getUser() {
			return this.user;
		}
		public int getPID() {
			return this.PID;
		}
		public int getPPID() {
			return this.PPID;
		}
		public String getName() {
			return this.name;
		}
		public String getPackageName() {
			return this.packageName;
		}
	}
	private class CallBackStyle {
		private String operationStyle = null;
		private CallBack callBack = null;
		public CallBackStyle(String operationStyle, CallBack callBack) {
			this.operationStyle = operationStyle;
			this.callBack = callBack;
		}
		public CallBackStyle(String operationStyle) {
			this.operationStyle = operationStyle;
		}
		public String getOperationStyle() {
    		return this.operationStyle;
    	}
		public CallBack getCallBack() {
			return this.callBack;
		}
	}
    public static abstract class CallBack{
    	public HashMap<String, ArrayList<ProcessInfo>> currentProcessesInfo = null;
    	public abstract void doSomething();
    }
	
	
	public void killCurrentPeocesses() {
		new StartTask().execute(new CallBackStyle("kill"));
	}
	
	public void getCurrentPeocesses(CallBack callBack) {
		new StartTask().execute(new CallBackStyle("get", callBack));
	}
}
