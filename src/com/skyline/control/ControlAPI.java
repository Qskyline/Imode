package com.skyline.control;

import java.util.ArrayList;
import java.util.Set;
import com.skyline.control.CurrentProcesses.CallBack;
import android.content.Context;
import android.os.Process;
import android.os.RemoteException;

/**
 * @author skyline
 */

public class ControlAPI {
	public static enum ControlLevel{
		normal,high
	}
	public static enum EnhancedStatus{
		noEnhanced,noControl,control
	}	
	private enum Operation{
		setPermission,setEnhanced,setNoEnhanced,setForbiden
	}	
	public static class ControlException extends Exception {
		private static final long serialVersionUID = -4702021711831245775L;
		public void printReason(){
			System.out.println("Getting controlService failed!");
		}		
	}
	
	private Control client = null;
	private PermissionDeclaration permissionInfo = null;
	private CurrentProcesses currentProcesses = null;
	private FakeEnvironment fakeEnvironment = null;
	
	public ControlAPI(Context context) throws ControlException{
		if (client == null) client = new Management().getService();
		if (permissionInfo == null) permissionInfo = new PermissionDeclaration();
		if (currentProcesses == null) currentProcesses = new CurrentProcesses();
		if (fakeEnvironment == null) fakeEnvironment = new FakeEnvironment(context);
		if (client == null) {
			throw new ControlException();
		}
		if (!addToWhiteList(Process.myUid())) throw new ControlException();
	}
	
	private  ArrayList<String> operationHelp(ArrayList<String> permissionNames, Operation operation){
		ArrayList<String> result = new ArrayList<String>();
		boolean b_temp =false;
		int i_temp;
		for (String permissionName : permissionNames) {
			try {
				switch (operation) {
				case setPermission:
					i_temp = client.setPermission(permissionName);
					break;
				case setEnhanced:
					i_temp = client.setEnhanced(permissionName);
					break;
				case setNoEnhanced:
					i_temp = client.setNoEnhanced(permissionName);
					break;
				case setForbiden:
					if (client.setForbiden(permissionName)) i_temp = 2;
					else i_temp = -1;
				default:
					i_temp = -7;
				}
				if (i_temp == 1) b_temp = true;
				else if(i_temp == -1) result.add(permissionName);
			} catch (RemoteException e) {
				result.add(permissionName);
				e.printStackTrace();
			}
		}
		if (b_temp) killCurrentProcesses();
		if (result.isEmpty()) return null;
		else return result;
	}
	
	private int operationHelp(String permissionName, Operation operation){
		int result;
		try {
			switch (operation) {
			case setPermission:
				result = client.setPermission(permissionName);
				break;
			case setEnhanced:
				result = client.setEnhanced(permissionName);
				break;
			case setNoEnhanced:
				result = client.setNoEnhanced(permissionName);
				break;
			case setForbiden:
				if (client.setForbiden(permissionName)) result = 2;
				else result = -1;
				break;
			default:
				result = -7;
			}
		} catch (RemoteException e) {
			e.printStackTrace();
			result = -2;
		}
		if (result == 1) killCurrentProcesses();		
		if (result == 2) result = 1;
		return result;
	}
	
	
	/**
	 * get all controlled permission names.
	 * @return a set that contains all controlled permission names.
	 */
	public Set<String> getALLPermissionName(){
		return permissionInfo.getAllPermissionName();
	}
	
	/**
	 * get all enhance-controlled permission names.
	 * @return a set that contains all enhance-controlled permission names.
	 */
	public Set<String> getALLEnhancedPermissionName(){
		return permissionInfo.getAllEnhancedPermissionName();
	}

	/**
	 * get the current enhance-controlled status of the permission.
	 * @param permissionName is the permission name.
	 * @return null if the permission is not be controlled or RemoteException happened, 
	 * EnhancedStatus.noEnhanced means that the permission control is not be enhanced,
	 * EnhancedStatus.noControl means that the permission enhance-controlled status is close,
	 * EnhancedStatus.control means that the permission enhance-controlled status is open.
	 */
	public EnhancedStatus getEnhancedStatus(String permissionName){
		try {
			int enhanced_temp = client.getEnhancedStatus(permissionName);
			switch(enhanced_temp){
			case -3:
			case -2:
				return null;
			case -1:
				return EnhancedStatus.noEnhanced;
			case 0:
				return EnhancedStatus.noControl;
			case 1:
				return EnhancedStatus.control;
			default:
				return null;
			}
		} catch (RemoteException e) {
			e.printStackTrace();
			return null;
		}
	}
		
	/**
	 * set the permission current enhance-controlled status open if it could be.
	 * @param permissionName is the permission name.
	 * @return -2 if RemoteException happened, -1 if the permission is not controlled, 0 if the
	 * permission can not be enhanced, 1 if success.
	 */
	public int setEnhanced(String permissionName){
		return operationHelp(permissionName, Operation.setEnhanced);
	}	
	
	/**
	 * set the permissions's current enhance-controlled statuses open if they could be.
	 * @param permissionNames are names of the permissions.
	 * @return the names of the permissions that they failed to be setEnhanced.
	 */
	public ArrayList<String> setEnhanced(ArrayList<String> permissionNames){
		return operationHelp(permissionNames, Operation.setEnhanced);
	}
	
	/**
	 * set the permission current enhance-controlled status close if it could be.
	 * @param permissionName is the permission name.
	 * @return -2 if RemoteException happened, -1 if the permission is not controlled, 0 if the
	 * permission can not be enhanced, 1 if success.
	 */
	public int setNoEnhanced(String permissionName){
		return operationHelp(permissionName, Operation.setNoEnhanced);
	}	
	
	/**
	 * set the permissions's current enhance-controlled statuses close if they could be.
	 * @param permissionNames are names of the permissions.
	 * @return the names of the permissions that they failed to be setNoEnhanced.
	 */
	public ArrayList<String> setNoEnhanced(ArrayList<String> permissionNames){
		return operationHelp(permissionNames, Operation.setNoEnhanced);		
	}
	
	/**
	 * get the current normal-controlled status of the permission.
	 * @param permissionName is the permission.
	 * @return -2 if RemoteException happened, -1 if the permission is not controlled, 0 if the
	 * permission normal-controlled status is close, 1 if the permission normal-controlled status is open.
	 */
	public int getPermissionStatus(String permissionName){
		try {
			return client.getForbiddenStatus(permissionName);
		} catch (RemoteException e) {
			e.printStackTrace();
			return -2;
		}
	}
	
	/**
	 * set the permission normal-controlled status open and don't change the permission enhance-controlled status.
	 * @param permissionName is the permission name.
	 * @return -2 if RemoteException happened, -1 if the permission is not controlled, 1 if success.
	 */
	public int setPermissionForbiden(String permissionName){
		return operationHelp(permissionName, Operation.setForbiden);
	}
	
	/**
	 * set the permissions's normal-controlled statuses open and don't change the permissions's enhance-controlled statuses.
	 * @param permissionNames are the names of the permissions.
	 * @return the names of the permissions that they failed to setPermissionForbiden.
	 */
	public ArrayList<String> setPermissionForbiden(ArrayList<String> permissionNames){
		return operationHelp(permissionNames, Operation.setForbiden);		
	}
	
	/**
	 * set the permission normal-controlled status open and if change the permission enhance-controlled status
	 * is belong to the value of parameter controlLevel. if the value of controlLevel is ControlLevel.high,
	 * it will set the permission enhance-controlled status open, and if the value of controlLevel is
	 * ControlLevel.normal, it would't do that.
	 * @param permissionName is the permission name.
	 * @param controlLevel is a enum object and it contains two values ！！ normal and high.
	 * @return -2 if RemoteException happened, -1 if the permission is not controlled, 0 if the permission
	 * can not be enhanced(but now the operation has succeeded), i if success.
	 * note:it will not fail if the permission can't be enhance-controlled when controlLevel is ControlLevel.high.
	 */
	public int setPermissionForbiden(String permissionName, ControlLevel controlLevel){
		int result = operationHelp(permissionName, Operation.setForbiden);
		switch(controlLevel){
		case normal:
			break;
		case high:
			if (result == 1) result = operationHelp(permissionName, Operation.setEnhanced);
			break;
		default:
		}
		return result;
	}
	
	/**
	 * set the permissions's normal-controlled statuses open and if change the permissions's enhance-controlled statuses
	 * is belong to the value of parameter controlLevel. if the value of controlLevel is ControlLevel.high,
	 * it will set the permissions's enhance-controlled statuses open, and if the value of controlLevel is
	 * ControlLevel.normal, it would't do that.
	 * @param permissionName are the permissions's names.
	 * @param controlLevel is a enum object and it contains two values ！！ normal and high.
	 * @return the names of the permissions that they failed to setPermissionForbiden.
	 * note:it will not fail if the permission can't be enhance-controlled when controlLevel is ControlLevel.high.
	 */
	public ArrayList<String> setPermissionForbiden(ArrayList<String> permissionNames, ControlLevel controlLevel){
		ArrayList<String> result = operationHelp(permissionNames, Operation.setForbiden);
		switch(controlLevel){
		case normal:
			break;
		case high:
			if (result == null) result = operationHelp(permissionNames, Operation.setEnhanced);
			else {
				for (int i = 0; i < result.size(); i++) 
					for (int j = 0; j < permissionNames.size(); j++)
						if(result.get(i).equals(permissionNames.get(j)))
							permissionNames.remove(j);
				ArrayList<String> temp = operationHelp(permissionNames, Operation.setEnhanced);
				if (temp != null) {
					for (int i = 0; i < temp.size(); i++)
						result.add(temp.get(i));
//					operationHelp(temp, Operation.setPermission);
				}
			}
			break;
		default:
		}
		return result;	
	}
	
	/**
	 * close the permission control, not only the normal-control, but also the enhanced-control(if it can be enhance-controlled).
	 * @param permissionName is the permission name.
	 * @return -2 if RemoteException happened, -1 if the permission is not controlled, 1 if success.
	 */
	public int setPermissionAllow(String permissionName){
		return operationHelp(permissionName, Operation.setPermission);
	}
	
	/**
	 * close the permissions's control, not only the normal-control, but also the enhanced-control(if it can be enhance-controlled).
	 * @param permissionName are the names of the permissions.
	 * @return the names of the permissions that they failed to setPermissionAllow.
	 */
	public ArrayList<String> setPermissionAllow(ArrayList<String> permissionNames){
		return operationHelp(permissionNames, Operation.setPermission);
	}	
	
	/**
	 * kill current processes.
	 */
	public void killCurrentProcesses() {
		currentProcesses.killCurrentPeocesses();
	}
	
	/**
	 * get the informations of the current processes. cause that this method will start a asynchronous task(thread),
	 * we can only get the result with a callback method. And the class ClassBack is a encapsulation about the method.
	 * you must create a object of CallBack, and implement the method named doSomething, and then you can do something
	 * with the data in the variable currentProcessesInfo.
	 * @param callBack is a Class containing the data and a callback  method.
	 */
	public void getCurrentProcesses(CallBack callBack) {
		currentProcesses.getCurrentPeocesses(callBack);
	}
	
	/**
	 * add a user to WhiteList.
	 * @param UID is the User ID.
	 * @return true if succeeded, false if failed.
	 */
	public boolean addToWhiteList(int UID) {
		try {
			client.addToWhiteList(UID);
			return true;
		} catch (RemoteException e) {
			e.printStackTrace();
			return false;
		}
	}
}
