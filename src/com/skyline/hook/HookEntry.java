package com.skyline.hook;

import static de.robv.android.xposed.XposedHelpers.findClass;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import com.skyline.control.Control;
import com.skyline.control.Management;
import com.skyline.control.PermissionDeclaration;
import android.util.Log;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class HookEntry implements IXposedHookLoadPackage, IXposedHookZygoteInit {
	private enum HookTime{
		init,loadPackage
	}
	private class XMethodHook extends XC_MethodHook {
		private HookBase mHook = null;
		private Control client = null;
		public XMethodHook(HookBase hook, Control client) {
			this.mHook = hook;
			this.client = client;
		}		
		@Override
		protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
			mHook.before(param ,client);
		}
		@Override
		protected void afterHookedMethod(MethodHookParam param) throws Throwable {
			mHook.after(param, client);
		}
	}
	
	private Control mClient = null;
	private PermissionDeclaration permissionInfo = new PermissionDeclaration();
	private Management serviceManagement = new Management(permissionInfo);
	
	private void hookAll(HookTime hookTime) {
		switch(hookTime){  
		case init:
			hookPermission(HookEnhanced.getInstances(permissionInfo, "internetAccess"), null);
			hookPermission(HookEnhanced.getInstances(permissionInfo, "storageAccess"), null);			
			hookPermission(HookMessageSend.getInstances(permissionInfo, "smsSendAccess"), null);
			break;
		case loadPackage:
			hookPermission(HookInternet.getInstances(permissionInfo, "internetAccess"), null);
			hookPermission(HookStorage.getInstances(permissionInfo, "storageAccess"), null);
			hookPermission(HookImage.getInstances(permissionInfo, "imageAccess"), null);
			hookPermission(HookVideo.getInstances(permissionInfo, "videoAccess"), null);
			hookPermission(HookAudio.getInstances(permissionInfo, "audioAccess"), null);
			hookPermission(HookLocation.getInstances(permissionInfo, "locationAccess"), null);
			hookPermission(HookContacts.getInstances(permissionInfo, "contactAccess"), null);
			hookPermission(HookCallLog.getInstances(permissionInfo, "callLogAccess"), null);
			hookPermission(HookCall.getInstances(permissionInfo, "callAccess"), null);
			hookPermission(HookMessageRead.getInstances(permissionInfo, "smsReadAccess"), null);
			break;
		default :	
			//do nothing
		}
	}	
	
	private void hookPermission(ArrayList<HookBase> listHook, ClassLoader classLoader) {
		if(listHook != null) 
			for (HookBase hook : listHook) 
				hook(hook, classLoader);
		else
			XposedBridge.log("listHook is null!");
	}
	
	private void hook(final HookBase hook, ClassLoader classLoader) {
		try {
			Class<?> hookClass = null;
			try {
				hookClass = findClass(hook.getClassName(), classLoader);
			} catch (Throwable ex) {
				return;
			}
			List<Member> listMember = new ArrayList<Member>();
			List<Class<?>[]> listParameters = new ArrayList<Class<?>[]>();
			Class<?> clazz = hookClass;
			while (clazz != null && !"android.content.ContentProvider".equals(clazz.getName())) {
				try {
					for (Method method : clazz.getDeclaredMethods()) {
						if (method.getName().equals(hook.getMethodName())
						&& !Modifier.isAbstract(method.getModifiers()) 
						&& (Modifier.isPublic(method.getModifiers()) ? hook.isVisible() : !hook.isVisible())) {
							boolean different = true;
							for (Class<?>[] parameters : listParameters) {
								boolean same = (parameters.length == method.getParameterTypes().length);
								for (int p = 0; same && p < parameters.length; p++)
									if (!parameters[p].equals(method.getParameterTypes()[p])) {
										same = false;
										break;
									}
								if (same) {
									different = false;
									break;
								}
							}
							if (different) {
								listMember.add(method);
								listParameters.add(method.getParameterTypes());
							}
						}
					}
					clazz = clazz.getSuperclass();
				} catch (Throwable ex) {
					if (ex.getClass().equals(ClassNotFoundException.class) || ex.getClass().equals(NoClassDefFoundError.class))
						break;
					else
						throw ex;
				}
			}
			for (Member member : listMember) {
				try {
					XposedBridge.hookMethod(member, new XMethodHook(hook, mClient));
				} catch (NoSuchFieldError ex) {	
					
				} catch (Throwable ex) {
					
				}
			}
		} catch (Throwable ex) {
			
		}
	}
	
	@Override
	public void initZygote(StartupParam startupParam) throws Throwable {
		serviceManagement.registerService();
		hookAll(HookTime.init);
	}

	@Override
	public void handleLoadPackage(LoadPackageParam lpparam) throws Throwable {
		if (mClient == null) mClient = serviceManagement.getService();
		if (mClient == null) {
			 Log.i("Imode","can't find the control service!");
			return;
		}
		hookAll(HookTime.loadPackage);
	}
}
