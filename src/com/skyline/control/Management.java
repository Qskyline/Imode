package com.skyline.control;

import java.lang.reflect.Method;

import android.os.Build;
import android.os.IBinder;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;

public class Management {
	private String serviceName = "serviceControl";
	private Service controlService = null;
	
	public Management(PermissionDeclaration permissionInfo){
		controlService = new Service(permissionInfo);
	}
	
	public Management(){
	}
		
	private String getServiceName() {
		return (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ? "user." : "") + serviceName;
	}
		
	public void registerService() throws ClassNotFoundException, NoSuchMethodException {
		if(controlService == null) return;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			Class<?> at = Class.forName("android.app.ActivityThread");
			XposedBridge.hookAllMethods(at, "systemMain", new XC_MethodHook() {
				@Override
				protected void afterHookedMethod(MethodHookParam param) throws Throwable {
					try {
						final ClassLoader loader = Thread.currentThread().getContextClassLoader();
						Class<?> am = Class.forName("com.android.server.am.ActivityManagerService", false, loader);
						XposedBridge.hookAllConstructors(am, new XC_MethodHook() {
							@Override
							protected void afterHookedMethod(MethodHookParam param) throws Throwable {
								try {
									Class<?> cServiceManager = Class.forName("android.os.ServiceManager", false, loader);
									Method mAddService = cServiceManager.getDeclaredMethod("addService", String.class, IBinder.class, boolean.class);
									mAddService.invoke(null, getServiceName(), controlService, true);
								} catch (Throwable ex) {
									//do something
								}
							}
						});
					} catch (Throwable ex) {
						//do something
					}
				}
			});

		} else {		
			Class<?> cSystemServer = Class.forName("com.android.server.SystemServer");
			Method mMain = cSystemServer.getDeclaredMethod("main", String[].class);
			XposedBridge.hookMethod(mMain, new XC_MethodHook() {
				@Override
				protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
					try {
						Class<?> cServiceManager = Class.forName("android.os.ServiceManager", false, null);
						Method mAddService = cServiceManager.getDeclaredMethod("addService", String.class, IBinder.class);
						mAddService.invoke(null, getServiceName(), controlService);
					} catch (Throwable ex) {
						//do something
					}
				}
			});
		}
	}
	
	public Control getService() {
		try {
			Class<?> cServiceManager = Class.forName("android.os.ServiceManager");
			Method mGetService = cServiceManager.getDeclaredMethod("getService", String.class);
			Object temp = mGetService.invoke(null, getServiceName());
			if(temp == null) return null;
			return Control.Stub.asInterface((IBinder)temp);
		} catch (Throwable ex) {
			return null;
		}
	}	
}
