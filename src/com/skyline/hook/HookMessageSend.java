package com.skyline.hook;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import com.skyline.control.Control;
import com.skyline.control.PermissionDeclaration;
import de.robv.android.xposed.XC_MethodHook.MethodHookParam;

public class HookMessageSend extends HookBase {
	private enum Methods {
		sendMultimediaMessage,getISmsService
	};
	
	private Methods method;
	private boolean isVisible = true; 
	
	public static ArrayList<HookBase> getInstances(PermissionDeclaration allPermissionInfo, String permissionName) {
		ArrayList<HookBase> listHook = new ArrayList<HookBase>();
		HashMap<String, String> permissionInfo = allPermissionInfo.getPermissionMethodsInfo(permissionName);
		for (Entry<String, String> enty : permissionInfo.entrySet()) {
			if (enty.getKey().equals(Methods.getISmsService.name())) {
				listHook.add(new HookMessageSend(permissionName, enty.getValue(), enty.getKey(), false));
			}
			else {
				listHook.add(new HookMessageSend(permissionName, enty.getValue(), enty.getKey()));
			}
		}
		return listHook;
	}
	
	public HookMessageSend(String permissionName, String className, String methodName) {
		super(permissionName, className, methodName);
		try {
			method = Methods.valueOf(methodName);
		} catch (IllegalArgumentException e) {		
			method = null;
		}
	}
	
	public HookMessageSend(String permissionName, String className, String methodName, boolean isVisible) {
		super(permissionName, className, methodName);
		try {
			method = Methods.valueOf(methodName);
		} catch (IllegalArgumentException e) {		
			method = null;
		}
		this.isVisible = isVisible;
	}

	@Override
	protected void before(MethodHookParam param, Control client) throws Throwable {
		if (method == null) return;
		switch (method) {
		case getISmsService:
		case sendMultimediaMessage:
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
	
	@Override
	public boolean isVisible() {
		if (isVisible) return true;
		else return false;
	}
}
