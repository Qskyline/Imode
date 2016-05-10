package com.skyline.hook;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import com.skyline.control.Control;
import com.skyline.control.PermissionDeclaration;
import android.hardware.Camera;
import de.robv.android.xposed.XC_MethodHook.MethodHookParam;

@SuppressWarnings("deprecation")
public class HookImage extends HookBase {
	private enum Methods {
		takePicture
	}
	private class hh implements Camera.PictureCallback {
		private Camera.PictureCallback cp;
		public hh(Camera.PictureCallback cp) {
			this.cp = cp;
		}		
		@Override
		public void onPictureTaken(byte[] data, Camera camera) {
			cp.onPictureTaken(fakeImage, camera);
		}
	}
	
	private Methods method = null;
	public static final byte[] fakeImage = getFakeImage();
	
	public static final byte[] getFakeImage() {
		byte[] data = null;
		FileInputStream input = null;
		try {
			input = new FileInputStream(new File("/data/data/com.skyline.imode/files/fake.jpg"));
		    ByteArrayOutputStream output = new ByteArrayOutputStream();
		    byte[] buf = new byte[1024];
		    int numBytesRead = 0;
		    while ((numBytesRead = input.read(buf)) != -1) {
		    	output.write(buf, 0, numBytesRead);
		    }
		    data = output.toByteArray();
		    output.close();
		    input.close();
		}
	    catch (IOException ex1) {
	    	ex1.printStackTrace();
		}
		return data;
	}
	
	public static ArrayList<HookBase> getInstances(PermissionDeclaration allPermissionInfo, String permissionName) {
		ArrayList<HookBase> listHook = new ArrayList<HookBase>();
		HashMap<String, String> permissionInfo = allPermissionInfo.getPermissionMethodsInfo(permissionName);
		for (Entry<String, String> enty : permissionInfo.entrySet()) {
			listHook.add(new HookImage(permissionName, enty.getValue(), enty.getKey()));
		}
		return listHook;	
	}
	
	public HookImage(String permissionName, String className, String methodName) {
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
		case takePicture:
			if (isForbidden(client)) {
				if (param.args.length == 4 || param.args.length == 3) {
					if (param.args[1] != null && param.args[1] instanceof Camera.PictureCallback) {
						param.args[1] = new hh((Camera.PictureCallback)param.args[1]);
					}
					if (param.args[2] != null && param.args[2] instanceof Camera.PictureCallback) {
						param.args[2] = new hh((Camera.PictureCallback)param.args[2]);
					}
					if (param.args.length == 4) {
						if (param.args[3] != null && param.args[3] instanceof Camera.PictureCallback) {
							param.args[3] = new hh((Camera.PictureCallback)param.args[3]);
						}
					}
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
