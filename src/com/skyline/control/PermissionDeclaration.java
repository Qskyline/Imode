package com.skyline.control;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PermissionDeclaration {
	private class PermissionInfo {
		private HashMap<String, String> permissionMethod = null;
		private HashMap<String, Integer> permissionGids = null;		
		public HashMap<String, String> getPermissionMethodsInfo(){
			return this.permissionMethod;
		}		
		public HashMap<String, Integer> getPermissionGidsInfo(){
			return this.permissionGids;
		}		
		public void setPermissionMethodsInfo(HashMap<String, String> permissionMethod){
			this.permissionMethod = permissionMethod;
		}		
		public void setPermissionGidsInfo(HashMap<String, Integer> permissionGids){
			this.permissionGids = permissionGids;
		}
	}
	
	private HashMap<String, PermissionInfo> allPermissionInfo = new HashMap<String, PermissionInfo>();
	
	private String[] activity = new String[] {
		"android.app.Activity:startActivities",
		"android.app.Activity:startActivity",
		"android.app.Activity:startActivityForResult",
		"android.app.Activity:startActivityFromChild",
		"android.app.Activity:startActivityFromFragment",
		"android.app.Activity:startActivityIfNeeded",
		"android.app.Activity:startNextMatchingActivity"
	};	
	private String[] internet = new String[] {
		"libcore.io.IoBridge:connect"
	};
	private String[] storage = new String[] {
		"libcore.io.IoBridge:open"
	};
	private String[] image = new String[] {
		"android.hardware.Camera:takePicture"
	};			
	private String[] video = new String[] {
		"android.hardware.Camera:unlock",
		"android.hardware.Camera:setPreviewCallback",
		"android.hardware.Camera:setPreviewCallbackWithBuffer",
		"android.hardware.Camera:setOneShotPreviewCallback"
	};
	private String[] audio = new String[] {
		"android.media.AudioRecord:startRecording",
		"android.media.AudioRecord:stop",
		"android.media.MediaRecorder:setAudioEncoder",
		"android.media.MediaRecorder:setAudioSource"
	};
	private String[] location = new String[] {
		"android.location.LocationManager:requestLocationUpdates",
		"android.location.LocationManager:requestSingleUpdate",
		"android.location.LocationManager:getLastKnownLocation",
		"android.location.LocationManager:removeUpdates",
		"android.net.wifi.WifiManager:getScanResults",
		"android.telephony.TelephonyManager:getAllCellInfo",
		"android.telephony.TelephonyManager:getCellLocation",
		"android.telephony.TelephonyManager:getNeighboringCellInfo",
		"android.telephony.TelephonyManager:listen"
	};
	private String[] contact = new String[] {
		"android.content.ContentResolver:query"
	};
	private String[] callLog = new String[] {
		"android.content.ContentResolver:query"
	};
	private String[] smsRead = new String[] {
		"android.content.ContentResolver:query"
	};
	private String[] smsSend = new String[] {
		"android.telephony.SmsManager:sendTextMessage",
		"android.telephony.SmsManager:sendDataMessage",
		"android.telephony.SmsManager:sendMultipartTextMessage"
	};
	private String[] internet_enhance = new String[] {
		"inet:3003",
		"inet_raw:3004",
		"inet_admin:3005",
		"inet_bw_stats:3006",
		"inet_bw_acct:3007",
		"inet_vpn:1016",
		"inet_mesh:1030"	
	};
	private String[] storage_enhance = new String[] {
		"sdcard_r:1028",
		"sdcard_rw:1015",
		"sdcard_pics:1033",
		"sdcard_av:1034",
		"sdcard_all:1035"
	};
	
	public PermissionDeclaration(){
		addPermissionByMethods("internetAccess", internet);
		addPermissionByMethods("storageAccess", storage);
		addPermissionByMethods("imageAccess", image);
		addPermissionByMethods("videoAccess", video);		
		addPermissionByMethods("audioAccess", audio);	
		addPermissionByMethods("locationAccess", location);
		addPermissionByMethods("contactAccess", contact);
		addPermissionByMethods("callLogAccess", callLog);
		addPermissionByMethods("callAccess", activity);
		addPermissionByMethods("smsReadAccess", smsRead);
		addPermissionByMethods("smsSendAccess", smsSend);
	
		addPermissionByGids("internetAccess", internet_enhance);
		addPermissionByGids("storageAccess", storage_enhance);
	}
	
	public HashMap<String, String> getPermissionMethodsInfo(String permissionName){
		if(allPermissionInfo.containsKey(permissionName)) return allPermissionInfo.get(permissionName).getPermissionMethodsInfo();
		return null;
	}
	
	public HashMap<String, Integer> getPermissionGidsInfo(String permissionName){
		if(allPermissionInfo.containsKey(permissionName)) return allPermissionInfo.get(permissionName).getPermissionGidsInfo();
		return null;
	}
	
	public Set<String> getAllPermissionName(){
		return allPermissionInfo.keySet();
	}
	
	public Set<String> getAllEnhancedPermissionName(){
		HashSet<String> temp = new HashSet<String>();
		for (Entry<String, PermissionInfo> entry : allPermissionInfo.entrySet())
			if(entry.getValue().getPermissionGidsInfo() != null)
				temp.add(entry.getKey());
		return temp;
	}
	
	private void addPermissionByMethods(String permissionName, Object str_classes_methods){
		if (permissionName == null || str_classes_methods == null) return;
		String[] arr_classes_methods = null;
		if (str_classes_methods instanceof String) arr_classes_methods = ((String)str_classes_methods).split(",");
		else if (str_classes_methods instanceof String[]) arr_classes_methods = (String[])str_classes_methods;
		else return;
		PermissionInfo temp = null;
		if (allPermissionInfo.containsKey(permissionName)) temp = allPermissionInfo.get(permissionName);
		else temp = new PermissionInfo();
		for (String str_class_method : arr_classes_methods) {
			String[] arr_class_method = str_class_method.split(":");
			if(arr_class_method.length != 2) continue;
			String methodName = arr_class_method[1];
			String className = arr_class_method[0];
			if(methodName == null || className == null) continue;
			if(temp.getPermissionMethodsInfo() == null){
				HashMap<String, String> hash_temp = new HashMap<String, String>();
				hash_temp.put(methodName, className);
				temp.setPermissionMethodsInfo(hash_temp);
			}
			else {
				temp.getPermissionMethodsInfo().put(methodName, className);
			}
		}
		allPermissionInfo.put(permissionName, temp);
	}
	
	private void addPermissionByGids(String permissionName, Object Gids){
		if(permissionName == null || Gids == null) return;
		String[] arr_gids = null;
		if (Gids instanceof String) arr_gids = ((String)Gids).split(",");
		else if (Gids instanceof String[]) arr_gids = (String[])Gids;
		else return;
		
		PermissionInfo temp = null;
		if(allPermissionInfo.containsKey(permissionName)) temp = allPermissionInfo.get(permissionName);
		else temp = new PermissionInfo();
		for (String str_gid : arr_gids) {
			String[] arr_gid = str_gid.split(":");
			if(arr_gid.length != 2) continue;
			Pattern pattern = Pattern.compile("^[0-9]+");
			Matcher matcher = pattern.matcher(arr_gid[1]);
			if(!matcher.matches()) continue;
			String gidName = arr_gid[0];
			int gidId = Integer.parseInt(arr_gid[1]); 
			if(gidName == null) continue;
			if(temp.getPermissionGidsInfo() == null){
				HashMap<String, Integer> hash_temp = new HashMap<String, Integer>();
				hash_temp.put(gidName, gidId);
				temp.setPermissionGidsInfo(hash_temp);
			}
			else{
				temp.getPermissionGidsInfo().put(gidName, gidId);
			}			
		}
		allPermissionInfo.put(permissionName, temp);		
	}
}
