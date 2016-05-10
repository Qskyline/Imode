package com.skyline.hook;

import com.skyline.control.Control;
import com.skyline.control.Management;

import android.os.Process;
import android.os.RemoteException;
import de.robv.android.xposed.XC_MethodHook.MethodHookParam;

public abstract class HookBase {
	private String permissionName = null;
	private String className = null;
	private String methodName = null;
	
	public HookBase(String permissionName, String className, String methodName) {
		this.permissionName = permissionName;
		this.className = className;
		this.methodName = methodName;
	}
	
	protected String getPermissionName() {
		return permissionName;
	}
	
	protected String getClassName() {
		return className;
	}
	
	protected String getMethodName() {
		return methodName;
	}
	
	protected boolean isForbidden(Control client) throws RemoteException {
		if (client == null) client = new Management().getService();
		if (client != null && client.isForbidden(getPermissionName(), Process.myUid())) return true;
		else return false;
	}
	
	abstract protected void before(MethodHookParam param, Control client) throws Throwable;

	abstract protected void after(MethodHookParam param, Control client) throws Throwable;
	
	public boolean isVisible() {
		return true;
	}
}
