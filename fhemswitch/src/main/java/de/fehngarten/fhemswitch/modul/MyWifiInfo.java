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
        return wifiInfo.getSSID();
    }

    public String getWifiId() {
        return wifiInfo.getBSSID();
    }

    public Boolean beAtHome(String bssId) {
        return isWifi() && getWifiId().equals(bssId);
    }
}


