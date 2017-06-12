package de.fehngarten.fhemswitch.modul;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

public class MyWifiInfo {

    private WifiInfo wifiInfo;
    private WifiManager wifiMan;
    public MyWifiInfo(Context context) {
        wifiMan = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        wifiInfo = wifiMan.getConnectionInfo();
    }

    public boolean isWifi() {
        return wifiMan.getWifiState() == WifiManager.WIFI_STATE_ENABLED;
    }

    public String getWifiName() {
        String wifiName = wifiInfo.getSSID();
        if (wifiName == null) {
            wifiName = "";
        }
        return wifiName;
    }

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


