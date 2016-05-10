package com.skyline.hook;

import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import com.skyline.control.Control;
import com.skyline.control.PermissionDeclaration;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XC_MethodHook.MethodHookParam;

public class HookInternet extends HookBase {
	private enum Methods {
		connect
	}
	
	private Methods method = null;
	
	public static ArrayList<HookBase> getInstances(PermissionDeclaration allPermissionInfo, String permissionName) {
		ArrayList<HookBase> listhook = new ArrayList<HookBase>();
		HashMap<String, String> permissionInfo = allPermissionInfo.getPermissionMethodsInfo(permissionName);
		if(permissionInfo != null) {
			for (Entry<String, String> enty : permissionInfo.entrySet()) 
				listhook.add(new HookInternet(permissionName, enty.getValue(), enty.getKey()));
			return listhook;
		}
		else
			return null;
	}
	
	public HookInternet(String permissionName, String className, String methodName) {
		super(permissionName, className, methodName);
		try {
			method = Methods.valueOf(methodName);
		} catch (IllegalArgumentException e) {		
			method = null;
		}
	}

	@Override
	protected void before(MethodHookParam param, Control client) throws Throwable {
		if (method == null) return;
		switch (method) {
		case connect:		
			if (param.args.length > 2 && param.args[1] instanceof InetAddress && param.args[2] instanceof Integer) {
				if(isForbidden(client)) {
					param.setThrowable(new SocketException("Imode"));
				}
			} else {
				XposedBridge.log("hook netAccess error!");
			}
			break;
		default:
		}
	}

	@Override
	protected void after(MethodHookParam param, Control client) throws Throwable {
		// do something
	}
}
