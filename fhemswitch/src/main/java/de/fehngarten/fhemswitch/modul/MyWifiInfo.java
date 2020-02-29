package de.fehngarten.fhemswitch.modul;

//import android.net.NetworkInfo;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.util.Log;

public class MyWifiInfo {
    private WifiInfo wifiInfo;
    private WifiManager wifiMan;
    private Context mContext;
    public MyWifiInfo(Context context) {
        wifiMan = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        wifiInfo = wifiMan.getConnectionInfo();
        mContext = context;
    }

    public boolean isWifi() {
        return wifiMan.getWifiState() == WifiManager.WIFI_STATE_ENABLED;
    }

    public String getWifiName() {
        String wifiName = "";

        //if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {

            WifiManager wifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            if (wifiInfo.getSupplicantState() == SupplicantState.COMPLETED) {
                wifiName = wifiInfo.getSSID();
            }
        //} else {
        //   wifiName = getWifiNameOld();
        //}

        return wifiName;
    }
/*
    @SuppressWarnings("deprecation")
    private String getWifiNameOld() {
        {
            String wifiName = "";
            ConnectivityManager cm = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo info = cm.getActiveNetworkInfo();

            if (info != null && info.isConnected()) {
                wifiName = info.getExtraInfo();
            }
            return wifiName;
        }
    }
*/
    public String getWifiId() {
        String wifiId = wifiInfo.getBSSID();
        if (wifiId == null) {
            wifiId = "";
        }
        return wifiId;
    }

    public Boolean beAtHome(String bssId) {
        if (bssId == null) return false;
        if (!isWifi()) return false;
        String wifiId = getWifiId();
        if (wifiId == null) return false;
        return wifiId.equals(bssId);
    }
}


