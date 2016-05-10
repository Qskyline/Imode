package com.skyline.hook;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import com.skyline.control.Control;
import com.skyline.control.PermissionDeclaration;
import android.annotation.SuppressLint;
import android.text.TextUtils;
import de.robv.android.xposed.XC_MethodHook.MethodHookParam;

public class HookStorage extends HookBase {
	private enum Methods {
		open
	}
	
	private Methods method;
	private String mExternalStorage = null;
	private String mEmulatedSource = null;
	private String mEmulatedTarget = null;
	private String mMediaStorage = null;
	private String mSecondaryStorage = null;

	public static ArrayList<HookBase> getInstances(PermissionDeclaration allPermissionInfo, String permissionName) {
		ArrayList<HookBase> listhook = new ArrayList<HookBase>();
		HashMap<String, String> permissionInfo = allPermissionInfo.getPermissionMethodsInfo(permissionName);
		if(permissionInfo != null) {
			for (Entry<String, String> enty : permissionInfo.entrySet()) 
				listhook.add(new HookStorage(permissionName, enty.getValue(), enty.getKey()));
			return listhook;
		}
		else
			return null;
	}
	
	public HookStorage(String permissionName, String className, String methodName) {
		super(permissionName, className, methodName);
		try {
			method = Methods.valueOf(methodName);
		} catch (IllegalArgumentException e) {		
			method = null;
		}
	}

	@SuppressLint("SdCardPath")
	@Override
	protected void before(MethodHookParam param, Control client) throws Throwable {
		if (method == null) return;
		switch (method) {
		case open:
			if (param.args.length > 0) {
				String fileName = (String)param.args[0];
				if (fileName != null) {
					if (mExternalStorage == null) {
						mExternalStorage = System.getenv("EXTERNAL_STORAGE");
						mEmulatedSource = System.getenv("EMULATED_STORAGE_SOURCE");
						mEmulatedTarget = System.getenv("EMULATED_STORAGE_TARGET");
						mMediaStorage = System.getenv("MEDIA_STORAGE");
						mSecondaryStorage = System.getenv("SECONDARY_STORAGE");
						if (TextUtils.isEmpty(mMediaStorage)) mMediaStorage = "/data/media";
					}
					if (fileName.startsWith("/sdcard")
					|| (mExternalStorage != null && fileName.startsWith(mExternalStorage))
					|| (mEmulatedSource != null && fileName.startsWith(mEmulatedSource))
					|| (mEmulatedTarget != null && fileName.startsWith(mEmulatedTarget))
					|| (mMediaStorage != null && fileName.startsWith(mMediaStorage))
					|| (mSecondaryStorage != null && fileName.startsWith(mSecondaryStorage)))
						if (isForbidden(client))
							param.setThrowable(new FileNotFoundException("Imode"));
				}
			}
			break;
		default:
		}
	}

	@Override
	protected void after(MethodHookParam param, Control client) throws Throwable {	
	}
}
