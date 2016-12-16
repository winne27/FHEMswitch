package de.fehngarten.fhemswitch.modul;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

public class MyWifiInfo {

    private WifiInfo wifiInfo;
    public MyWifiInfo(Context context) {
        WifiManager wifiMan = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        wifiInfo = wifiMan.getConnectionInfo();
    }

    public boolean isWifi() {
        return wifiInfo.getNetworkId() > 0;
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


