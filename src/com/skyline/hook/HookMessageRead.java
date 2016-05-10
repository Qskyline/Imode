package com.skyline.hook;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import com.skyline.control.Control;
import com.skyline.control.PermissionDeclaration;
import android.annotation.SuppressLint;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import de.robv.android.xposed.XC_MethodHook.MethodHookParam;

public class HookMessageRead extends HookBase {
	private enum Methods {
		query
	};
	
	private Methods method;
	
	@SuppressLint("DefaultLocale")
	private void processUri(MethodHookParam param, Control client) throws Throwable {
		if (param.args.length > 2 && param.args[0] instanceof Uri && param.getResult() != null) {
			String uri = ((Uri) param.args[0]).toString().toLowerCase();
			Cursor cursor = (Cursor) param.getResult();		
			if (uri.startsWith("content://mms") || uri.startsWith("content://mms-sms")
			|| uri.startsWith("content://sms") || uri.startsWith("content://com.android.voicemail")) { 
				if (isForbidden(client)) {
					MatrixCursor result = new MatrixCursor(cursor.getColumnNames());
					result.respond(cursor.getExtras());
					param.setResult(result);
					cursor.close();
				}
			}
		}
	}
	
	public static ArrayList<HookBase> getInstances(PermissionDeclaration allPermissionInfo, String permissionName) {
		ArrayList<HookBase> listHook = new ArrayList<HookBase>();
		HashMap<String, String> permissionInfo = allPermissionInfo.getPermissionMethodsInfo(permissionName);
		for (Entry<String, String> enty : permissionInfo.entrySet()) {
			listHook.add(new HookMessageRead(permissionName, enty.getValue(), enty.getKey()));
		}
		return listHook;
	}	
	
	public HookMessageRead(String permissionName, String className, String methodName) {
		super(permissionName, className, methodName);
		try {
			method = Methods.valueOf(methodName);
		} catch (IllegalArgumentException e) {		
			method = null;
		}
	}

	@Override
	protected void before(MethodHookParam param, Control client) throws Throwable {
	}

	@Override
	protected void after(MethodHookParam param, Control client) throws Throwable {
		if (method == null) return;
		switch (method) {
		case query:
			processUri(param, client);
			break;
		default:
		}
	}
}
