package com.skyline.hook;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import com.skyline.control.PermissionDeclaration;
import android.content.Intent;

public class HookCall extends HookActivityBase {

	public HookCall(String permissionName, String className, String methodName, String actionName) {
		super(permissionName, className, methodName);
		setAction(actionName);
	}
	
	public static ArrayList<HookBase> getInstances(PermissionDeclaration allPermissionInfo, String permissionName) {
		ArrayList<HookBase> listHook = new ArrayList<HookBase>();
		HashMap<String, String> permissionInfo = allPermissionInfo.getPermissionMethodsInfo(permissionName);
		for (Entry<String, String> enty : permissionInfo.entrySet()) {
			listHook.add(new HookCall(permissionName, enty.getValue(), enty.getKey(), Intent.ACTION_CALL));
		}
		return listHook;
	}

}
