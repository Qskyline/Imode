package com.skyline.control;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import android.os.RemoteException;

public class Service extends Control.Stub  {	
	private enum EnhancedStatus{
		noEnhanced,noControl,control
	}
	private class Judge{
		private boolean flag = false;
		private EnhancedStatus enhancedStatus = null;
		
		public Judge() {
			this.flag = false;
			this.enhancedStatus = EnhancedStatus.noEnhanced;
		}
		
		public Judge(EnhancedStatus enhancedStatus) {
			this.flag = false;
			this.enhancedStatus = enhancedStatus;
		}
		
		public EnhancedStatus getEnhancedStatus(){
			return enhancedStatus;
		}
		
		public void setEnhancedStatus(EnhancedStatus enhancedStatus){
			this.enhancedStatus = enhancedStatus;
		}
		
		public boolean getFlag(){
			return this.flag;
		}
		
		public void setFlag(boolean flag){
			this.flag = flag;
		}
	}
	
	private HashSet<Integer> whiteList = new HashSet<Integer>();
	private HashMap<String, Judge> allControledPermission = new HashMap<String, Judge>();
	
	public Service(PermissionDeclaration permissionInfo)
	{
		Set<String> all_temp = permissionInfo.getAllPermissionName();
		Set<String> enhanced_temp = permissionInfo.getAllEnhancedPermissionName();
		for(String key : all_temp){
			if(enhanced_temp.contains(key)) this.allControledPermission.put(key, new Judge(EnhancedStatus.noControl));
			else this.allControledPermission.put(key, new Judge());
		}
	}
	
	@Override
	public int getForbiddenStatus(String permissionName) throws RemoteException {
		if(allControledPermission.containsKey(permissionName)) {
			if(allControledPermission.get(permissionName).getFlag()) return 1;
			else return 0;
		}
		else{
			return -1;
		}
	}

	@Override
	public boolean setForbiden(String permissionName) throws RemoteException {
		return setPermissionStatus(permissionName, true);
	}
		
	@Override
	public int setPermission(String permissionName) throws RemoteException {
		int enhanced_temp = setNoEnhanced(permissionName);
		if(enhanced_temp == 0 || enhanced_temp == 1 || enhanced_temp == 2){
			setPermissionStatus(permissionName, false);
		}
		return enhanced_temp;
	}
	
	@Override
	public int getEnhancedStatus(String permissionName) throws RemoteException{
		if(allControledPermission.containsKey(permissionName)) {
			EnhancedStatus temp = allControledPermission.get(permissionName).getEnhancedStatus();
			switch(temp){
			case noEnhanced:
				return -1;
			case noControl:
				return 0;
			case control:
				return 1;
			default:	
				return -3;
			}
		}
		else{
			return -2;
		}
	}
	
	@Override
	public int setEnhanced(String permissionName) throws RemoteException{
		return setEnhancedStatus(permissionName, EnhancedStatus.control);	
	}
		
	@Override
	public int setNoEnhanced(String permissionName) throws RemoteException{
		return setEnhancedStatus(permissionName, EnhancedStatus.noControl);
	}
		
	private boolean setPermissionStatus(String permissionName, boolean status){
		if(allControledPermission.containsKey(permissionName)) {
			allControledPermission.get(permissionName).setFlag(status);
			return true;
		}
		else{
			return false;
		}
	}
	
	private int setEnhancedStatus(String permissionName, EnhancedStatus enhancedStatus){
		if(enhancedStatus.equals(EnhancedStatus.noEnhanced)) return -2;
		if(allControledPermission.containsKey(permissionName)) {
			Judge judge_temp = allControledPermission.get(permissionName);
			if(!judge_temp.getEnhancedStatus().equals(EnhancedStatus.noEnhanced)){
				if(!judge_temp.getEnhancedStatus().equals(enhancedStatus)){
					judge_temp.setEnhancedStatus(enhancedStatus);
					return 1;	
				}
				else {
					return 2;
				}		
			}
			else{
				return 0;
			}
		}
		else{
			return -1;
		}
	}

	@Override
	public boolean isForbidden(String permissionName, int UID) throws RemoteException {
	    if (whiteList.contains(UID)) return false;
	    if (getForbiddenStatus(permissionName) == 1) return true;
	    else return false;
	}

	@Override
	public boolean isEnhanced(String permissionName, int UID) throws RemoteException {
		 if (whiteList.contains(UID)) return false;
		 if (getEnhancedStatus(permissionName) == 1) return true;
		 else return false;
	}

	@Override
	public void addToWhiteList(int UID) throws RemoteException {
		whiteList.add(UID);		
	}
}
