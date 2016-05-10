package com.skyline.hook;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import com.skyline.control.Control;
import com.skyline.control.Management;
import com.skyline.control.PermissionDeclaration;
import de.robv.android.xposed.XC_MethodHook.MethodHookParam;

public class HookEnhanced extends HookBase {
	ArrayList<Integer> gidsForbidden = null;	

	public HookEnhanced(String permissionName, ArrayList<Integer> gidsForbidden) {
		super(permissionName, "android.os.Process", "startViaZygote");
		this.gidsForbidden = gidsForbidden;		
	}
	
	public static ArrayList<HookBase> getInstances(PermissionDeclaration allPermissionInfo, String permissionName) {
		ArrayList<HookBase> listhook = new ArrayList<HookBase>();
		HashMap<String, Integer> permissionInfor = allPermissionInfo.getPermissionGidsInfo(permissionName);
		if(permissionInfor != null) {
			ArrayList<Integer> gids = new ArrayList<Integer>();
			for (Entry<String, Integer> enty : permissionInfor.entrySet()) gids.add(enty.getValue());
			listhook.add(new HookEnhanced(permissionName, gids));
			return listhook;
		}
		else
			return null;
	}
	
	private boolean compareGids(Integer gid, ArrayList<Integer> gids_forbidden){
		for (Integer integer : gids_forbidden) {
			if(gid.equals(integer)) return true;
		}
		return false;
	}

	@Override
	public boolean isVisible() {
		return false;
	}
	
	@Override
	protected void before(MethodHookParam param, Control client) throws Throwable {
		if(client == null) client = new Management().getService();
		if(client == null) return;
		if (param.args.length >= 5 && param.args[2] instanceof Integer && param.args[4] instanceof int[]) {
			int[] gids =  (int[])param.args[4];
			int UID = (int) param.args[2];
			List<Integer> listGids = new ArrayList<Integer>();
			for (int integer : gids) listGids.add(integer);
			for (int i = 0; i < listGids.size(); i++) {
				if (compareGids(listGids.get(i), gidsForbidden))
					if (client.isForbidden(getPermissionName(), UID) && client.isEnhanced(getPermissionName(), UID))
						listGids.remove(i);
			}
			int[] mGids = new int[listGids.size()];
			for (int i = 0; i < listGids.size(); i++) mGids[i] = listGids.get(i);
			param.args[4] = (mGids.length == 0 ? null : mGids);
		}		
	}

	@Override
	protected void after(MethodHookParam param, Control client) throws Throwable {		
	}

}

