package com.skyline.hook;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import com.skyline.control.Control;
import com.skyline.control.PermissionDeclaration;

import de.robv.android.xposed.XC_MethodHook.MethodHookParam;

public class HookNFC extends HookBase {
	private enum Methods {
		getDefaultAdapter, getNfcAdapter
	};
	
	private Methods method;	
	
	public static ArrayList<HookBase> getInstances(PermissionDeclaration allPermissionInfo, String permissionName) {
		ArrayList<HookBase> listHook = new ArrayList<HookBase>();
		HashMap<String, String> permissionInfo = allPermissionInfo.getPermissionMethodsInfo(permissionName);
		for (Entry<String, String> enty : permissionInfo.entrySet()) {
			listHook.add(new HookNFC(permissionName, enty.getValue(), enty.getKey()));
		}
		return listHook;
	}	
	
	public HookNFC(String permissionName, String className, String methodName) {
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
		case getDefaultAdapter:
		case getNfcAdapter:
			if (isForbidden(client)) {
				param.setResult(null);
			}
			break;
		default:
		}
	}

	@Override
	protected void after(MethodHookParam param, Control client) throws Throwable {
	}

}
