package com.huewu.pla.sample.util;

import java.util.List;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.Settings.Secure;
import android.telephony.TelephonyManager;
import android.text.TextUtils;


public class DeviceUtil {
    
    public final static String NETWORK_STATE_NOTHING = "network_state_nothing";
	public final static String NETWORK_STATE_MOBILE = "network_state_mobile";
	public final static String NETWORK_STATE_WIFI = "network_state_wifi";
	public final static String NETWORK_STATE_OTHER = "network_state_other";
    
	public static String getIMEI(Context context) 
	{
		String imei = "";
		TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		if (telephonyManager != null) {
			imei = telephonyManager.getDeviceId();
			if (TextUtils.isEmpty(imei)) {
				imei = Secure.getString(context.getContentResolver(), Secure.ANDROID_ID);
			}
		}
		return imei;
	}
	
	public static boolean hasApp(Context context, String packagename) 
	{
		PackageInfo packageInfo = null;

		try {
			packageInfo = context.getPackageManager().getPackageInfo(packagename, 0);

		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		
		return packageInfo == null ? false:true;
	}
	
    public static boolean isNetworkEnable(Context context)
    {
        ConnectivityManager conManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = conManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isAvailable();
	}	
    
    public static boolean isNetworkDisable(Context context)
    {
        ConnectivityManager conManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = conManager.getActiveNetworkInfo();
        return networkInfo == null || !networkInfo.isAvailable();
	}
    
	public static String getNetworkState(Context context) 
	{
		ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = cm.getActiveNetworkInfo();
		if (info == null || !info.isAvailable()) {
			return NETWORK_STATE_NOTHING;
		} else {
			if (info.getType() == ConnectivityManager.TYPE_MOBILE) {
				return NETWORK_STATE_MOBILE;
			} else if(info.getType() == ConnectivityManager.TYPE_WIFI){
				return NETWORK_STATE_WIFI;
			} else {
				return NETWORK_STATE_OTHER;
			}
		}
	}
	
	public static boolean isWifiNetWork(Context context)
	{
		return NETWORK_STATE_WIFI.equals(DeviceUtil.getNetworkState(context));
	}
	
	public static boolean isMobileNetWork(Context context)
	{
		return NETWORK_STATE_MOBILE.equals(DeviceUtil.getNetworkState(context));
	}
    
    public static int getScreenWidth(Context context)
    {
        return context.getResources().getDisplayMetrics().widthPixels;
    }
    
    public static int getScreenHeight(Context context)
    {
        return context.getResources().getDisplayMetrics().heightPixels;
    }
    
    public static int getHeight169ByScreenMinus(Context context, int widthOffset)
    {
    	return (int)((getScreenWidth(context)-widthOffset)/1.77);
    }
    
    public static int getHeight169ByScreen(Context context)
    {
    	return (int)(getScreenWidth(context)/1.77);
    }
    
    public static boolean hasSinaWeiboClient(Context context)
    {
        try {
        	
        	PackageInfo packageInfo = context.getPackageManager().getPackageInfo("com.sina.weibo", 0);
        	if(packageInfo == null)
        		return false;
        	
            int highBit = packageInfo.versionName.charAt(0);
            return highBit > 50 ? true : false;//50 = 2

        } catch (NameNotFoundException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public static boolean checkGoogleMap(Context context) {
        boolean isInstallGMap = false;
        List<PackageInfo> packs = context.getPackageManager().getInstalledPackages(0);
        for (int i = 0; i < packs.size(); i++) {
            PackageInfo p = packs.get(i);
            if (p.versionName == null) { // system packages
                continue;
            }
            if ("com.google.android.apps.maps".equals(p.packageName)) {
                isInstallGMap = true;
                break;
            }
        }
        return isInstallGMap;
    }
}
