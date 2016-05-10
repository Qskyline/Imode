package com.skyline.hook;

import com.skyline.control.Control;
import android.content.Intent;
import de.robv.android.xposed.XC_MethodHook.MethodHookParam;

public class HookActivityBase extends HookBase {
	private enum Methods {
		startActivities, startActivity, startActivityForResult, startActivityFromChild, startActivityFromFragment, startActivityIfNeeded, startNextMatchingActivity
	};
	
	private Methods method;
	private String action = null;
		
	public void setAction(String actionName) {
		this.action = actionName;
	}
	
	public HookActivityBase(String permissionName, String className, String methodName) {
		super(permissionName, className, methodName);
		try {
			method = Methods.valueOf(methodName);
		} catch (IllegalArgumentException e) {		
			method = null;
		}
	}

	@Override
	protected void before(MethodHookParam param, Control client) throws Throwable {
		if (method == null || action == null) return;
		Intent[] intents = null;
		switch (method) {
		case startActivity:
		case startActivityForResult:
		case startActivityIfNeeded:
		case startNextMatchingActivity:
			if (param.args.length > 0 && param.args[0] instanceof Intent)
				intents = new Intent[] { (Intent) param.args[0] };
			break;
		case startActivityFromChild:
		case startActivityFromFragment:
			if (param.args.length > 1 && param.args[1] instanceof Intent)
				intents = new Intent[] { (Intent) param.args[1] };
			break;
		case startActivities:
			if (param.args.length > 0 && param.args[0] instanceof Intent[])
				intents = (Intent[]) param.args[0];
			break;
		default:		
		}
		if (intents != null) {
			for (Intent intent : intents) {
				if (action.equals(intent.getAction()) && isForbidden(client)) {
					if (method == Methods.startActivityIfNeeded) param.setResult(true);
					else param.setResult(null);
					return;
				}
			}
		}
	}

	@Override
	protected void after(MethodHookParam param, Control client) throws Throwable {
	}
}
