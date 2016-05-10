package com.skyline.hook;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import com.skyline.control.Control;
import com.skyline.control.PermissionDeclaration;
import android.util.Log;
import de.robv.android.xposed.XC_MethodHook.MethodHookParam;

public class HookMessageSend extends HookBase {
	private enum Methods {
		sendTextMessage,sendDataMessage,sendMultipartTextMessage
	};
	
	private Methods method;
	
	public static ArrayList<HookBase> getInstances(PermissionDeclaration allPermissionInfo, String permissionName) {
		ArrayList<HookBase> listHook = new ArrayList<HookBase>();
		HashMap<String, String> permissionInfo = allPermissionInfo.getPermissionMethodsInfo(permissionName);
		for (Entry<String, String> enty : permissionInfo.entrySet()) {
			listHook.add(new HookMessageSend(permissionName, enty.getValue(), enty.getKey()));
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

	@Override
	protected void before(MethodHookParam param, Control client) throws Throwable {
		if (method == null) return;
		switch (method) {
		case sendDataMessage:
		case sendMultipartTextMessage:
		case sendTextMessage:
			if (isForbidden(client)) {
				Log.i("Imode", "111111111111111111111111111");
				param.setResult(null);
			}
			break;
		default:			
		}
	}

	@Override
	protected void after(MethodHookParam param, Control client) throws Throwable {
		// TODO Auto-generated method stub
	}
}
