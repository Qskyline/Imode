package com.skyline.hook;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.Map.Entry;
import com.skyline.control.Control;
import com.skyline.control.PermissionDeclaration;
import android.app.PendingIntent;
import android.location.Location;
import android.location.LocationListener;
import android.net.wifi.ScanResult;
import android.os.Bundle;
import android.telephony.CellInfo;
import android.telephony.CellLocation;
import android.telephony.NeighboringCellInfo;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.SignalStrength;
import android.telephony.gsm.GsmCellLocation;
import de.robv.android.xposed.XC_MethodHook.MethodHookParam;

public class HookLocation extends HookBase {
	private enum Methods {
		requestLocationUpdates,requestSingleUpdate,getLastKnownLocation,removeUpdates,
		getScanResults,getAllCellInfo,getCellLocation,getNeighboringCellInfo,listen
	}
	private class ProxyPhoneStateListener extends PhoneStateListener {
		private PhoneStateListener mListener;
		public ProxyPhoneStateListener(PhoneStateListener listener) {
			mListener = listener;
		}
		@Override
		public void onCallForwardingIndicatorChanged(boolean cfi) {
			mListener.onCallForwardingIndicatorChanged(cfi);
		}
		@Override
		public void onCallStateChanged(int state, String incomingNumber) {
			mListener.onCallStateChanged(state, incomingNumber);
		}
		@Override
		public void onCellInfoChanged(List<CellInfo> cellInfo) {
			mListener.onCellInfoChanged(new ArrayList<CellInfo>());
		}
		@Override
		public void onCellLocationChanged(CellLocation location) {
			mListener.onCellLocationChanged(getFakeCellLocation());
		}
		@Override
		public void onDataActivity(int direction) {
			mListener.onDataActivity(direction);
		}
		@Override
		public void onDataConnectionStateChanged(int state) {
			mListener.onDataConnectionStateChanged(state);
		}
		@Override
		public void onDataConnectionStateChanged(int state, int networkType) {
			mListener.onDataConnectionStateChanged(state, networkType);
		}
		@Override
		public void onMessageWaitingIndicatorChanged(boolean mwi) {
			mListener.onMessageWaitingIndicatorChanged(mwi);
		}
		@Override
		public void onServiceStateChanged(ServiceState serviceState) {
			mListener.onServiceStateChanged(serviceState);
		}
		@Override
		@SuppressWarnings("deprecation")
		public void onSignalStrengthChanged(int asu) {
			mListener.onSignalStrengthChanged(asu);
		}
		@Override
		public void onSignalStrengthsChanged(SignalStrength signalStrength) {
			mListener.onSignalStrengthsChanged(signalStrength);
		}
	}
	private class ProxyLocationListener implements LocationListener {
		private LocationListener mListener;
		public ProxyLocationListener(LocationListener listener) {
			mListener = listener;
		}
		@Override
		public void onLocationChanged(Location location) {
			Location fakeLocation = getFakeLocation(location);
			mListener.onLocationChanged(fakeLocation);
		}
		@Override
		public void onProviderDisabled(String provider) {
			mListener.onProviderDisabled(provider);
		}
		@Override
		public void onProviderEnabled(String provider) {
			mListener.onProviderEnabled(provider);
		}
		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
			mListener.onStatusChanged(provider, status, extras);
		}
	}
	
	private int lac = 0;
	private int cid = 0;
	private float latitude = -10.5f;
	private float longitude = 105.667f;
	private float altitude = 686f;
	
	private Methods mMethod;	
	private final Map<Object, Object> mMapProxy = new WeakHashMap<Object, Object>();
	private final Map<PhoneStateListener, ProxyPhoneStateListener> mListener = new WeakHashMap<PhoneStateListener, ProxyPhoneStateListener>();
	
	private void addProxyPhoneStateListener(MethodHookParam param, int arg) {
		if (param.args.length > arg && param.args[arg] != null && param.args[arg] instanceof PhoneStateListener) {
			PhoneStateListener listener = (PhoneStateListener) param.args[arg];
			try {
				ProxyPhoneStateListener xListener;
				synchronized (mListener) {
					xListener = mListener.get(listener);
					if (xListener == null) {
						xListener = new ProxyPhoneStateListener(listener);
						mListener.put(listener, xListener);
					}
				}
				param.args[arg] = xListener;
			} catch (Throwable ignored) {}				
		}
	}
	
	private void removeProxyPhoneStateListener(MethodHookParam param, int arg) {
		if (param.args.length > arg && param.args[arg] != null && param.args[arg] instanceof PhoneStateListener) {
			PhoneStateListener listener = (PhoneStateListener) param.args[arg];
			synchronized (mListener) {
				ProxyPhoneStateListener xListener = mListener.get(listener);
				if (xListener != null) param.args[arg] = xListener;
			}
		}
	}
	
	private void addProxyLocationListener(MethodHookParam param, int arg) throws Throwable {
		if (param.args.length > arg) {
			if (param.args[arg] instanceof PendingIntent) {
				param.setResult(null);
			}
			else if (param.args[arg] != null && param.thisObject != null) {
				Object key = param.args[arg];
				synchronized (mMapProxy) {
					if (mMapProxy.containsKey(key)) {
						param.args[arg] = mMapProxy.get(key);
						return;
					}
					if (mMapProxy.containsValue(key)) {
						return;
					}
				}
				Object proxy = new ProxyLocationListener((LocationListener)param.args[arg]);
				synchronized (mMapProxy) {
					mMapProxy.put(key, proxy);
				}
				param.args[arg] = proxy;
			}
		}
	}
		
	private void removeProxyLocationListener(MethodHookParam param, int arg) {
		if (param.args.length > arg) {
			if (param.args[arg] instanceof PendingIntent) {
				param.setResult(null);
			}
			else if (param.args[arg] != null) {
				Object key = param.args[arg];
				synchronized (mMapProxy) {
					if (mMapProxy.containsKey(key)) {
						param.args[arg] = mMapProxy.get(key);
					}
				}
			}
		}
	}
	
	private Location getFakeLocation(Location location) {
		location.setLatitude(latitude + (Math.random() * 2.0 - 1.0) * location.getAccuracy() * 9e-6);
		location.setLongitude(longitude + (Math.random() * 2.0 - 1.0) * location.getAccuracy() * 9e-6);
		location.setAltitude(altitude + (Math.random() * 2.0 - 1.0) * location.getAccuracy());
		return location;
	}
	
	private CellLocation getFakeCellLocation() {
		if (cid > 0 && lac > 0) {
			GsmCellLocation cellLocation = new GsmCellLocation();
			cellLocation.setLacAndCid(lac, cid);
			return cellLocation;
		} else {
			return CellLocation.getEmpty();
		}
	}

	public static ArrayList<HookBase> getInstances(PermissionDeclaration allPermissionInfo, String permissionName) {
		ArrayList<HookBase> listhook = new ArrayList<HookBase>();
		HashMap<String, String> permissionInfo = allPermissionInfo.getPermissionMethodsInfo(permissionName);
		if(permissionInfo != null) {
			for (Entry<String, String> enty : permissionInfo.entrySet()) 
				listhook.add(new HookLocation(permissionName, enty.getValue(), enty.getKey()));
			return listhook;
		}
		else  {
			return null;
		}
	}

	public HookLocation(String permissionName, String className, String methodName) {
		super(permissionName, className, methodName);
		try {
			mMethod = Methods.valueOf(methodName);
		} catch (IllegalArgumentException e) {		
			mMethod = null;
		}
	}

	@Override
	protected void before(MethodHookParam param, Control client) throws Throwable {
		switch (mMethod) {
		case requestLocationUpdates:
			if (isForbidden(client)) {
				addProxyLocationListener(param, 3);
			}
			break;
		case requestSingleUpdate:
			if (isForbidden(client)) {
				addProxyLocationListener(param, 1);
			}
			break;
		case removeUpdates:
			if (isForbidden(client)) {
				removeProxyLocationListener(param, 0);
			}
			break;
		case listen:
			if (param.args.length > 1 && param.args[0] instanceof PhoneStateListener && param.args[1] instanceof Integer) {
				int event = (Integer) param.args[1];
				if (event == PhoneStateListener.LISTEN_NONE) {
					removeProxyPhoneStateListener(param, 0);
				} else if (isForbidden(client)) {
					addProxyPhoneStateListener(param, 0);
				}					
			}
			break;
		default:
		}
	}

	@Override
	protected void after(MethodHookParam param, Control client) throws Throwable {
		switch (mMethod) {
		case getLastKnownLocation:
			if (param.args.length > 0 && param.getResult() instanceof Location && isForbidden(client)) {
				param.setResult(getFakeLocation((Location)param.getResult()));
			}
			break;
		case getScanResults:
			if (param.getResult() != null && isForbidden(client)) {
				param.setResult(new ArrayList<ScanResult>());
			}
			break;
		case getAllCellInfo:
			if (param.getResult() != null && isForbidden(client)) {
				param.setResult(new ArrayList<CellInfo>());
			}
			break;
		case getCellLocation:
			if (param.getResult() != null && isForbidden(client)) { 
				param.setResult(getFakeCellLocation());
			}
			break;
		case getNeighboringCellInfo:
			if (param.getResult() != null && isForbidden(client)) {
				param.setResult(new ArrayList<NeighboringCellInfo>());
			}
			break;
		default:
		}
	}
}
