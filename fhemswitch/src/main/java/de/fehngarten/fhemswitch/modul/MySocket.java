package de.fehngarten.fhemswitch.modul;

import java.util.ArrayList;

import android.content.Context;
import android.os.Build;
import android.util.Log;
//import android.util.Log;

import de.fehngarten.fhemswitch.BuildConfig;
import de.fehngarten.fhemswitch.data.ConfigDataCommon;
import de.fehngarten.fhemswitch.data.ConfigWorkBasket;
import io.socket.client.IO;
import io.socket.client.Socket;

import static de.fehngarten.fhemswitch.global.Settings.*;

public class MySocket {
    public Socket socket;
    private final String TAG = "MySocket";

    public MySocket(Context context, ConfigDataCommon configDataCommon, String type) {
        checkLocalWlan(configDataCommon, context);
        String url = ConfigWorkBasket.urlFhemjs;

        try {
            //Log.d(TAG, "URL: " + url);
            IO.Options options = new IO.Options();
            if (type.equals("Config")) {
                options.reconnection = false;
            } else {
                options.reconnection = false;
                //options.reconnectionDelay = 1000;
                //options.reconnectionDelayMax = 30000;
            }

            options.timeout = settingSocketsConnectionTimeout;
            options.query = "client=" + type + "&platform=Android&version=" + Build.VERSION.RELEASE + "&model=" + Build.MODEL + "&appver=" + BuildConfig.VERSION_NAME;
            socket = IO.socket(url, options);

            socket.on(Socket.EVENT_CONNECT, args -> {
                String pw = ConfigWorkBasket.fhemjsPW;
                if (!pw.equals("")) {
                    socket.emit("authentication", pw);
                }
            });

        } catch (Exception e1) {
            //Log.e("socket error", e1.toString());
        }
    }

    private void checkLocalWlan(ConfigDataCommon configDataCommon, Context context) {
        MyWifiInfo myWifiInfo = new MyWifiInfo(context);
        boolean beAtHome = myWifiInfo.beAtHome(configDataCommon.bssId);

        if (!configDataCommon.urlFhemjsLocal.equals("") && beAtHome) {
            ConfigWorkBasket.urlFhemjs = configDataCommon.urlFhemjsLocal;
        } else {
            ConfigWorkBasket.urlFhemjs = configDataCommon.urlFhemjs;
        }

        if (!configDataCommon.urlFhemplLocal.equals("") && beAtHome) {
            ConfigWorkBasket.urlFhempl = configDataCommon.urlFhemplLocal;
        } else {
            ConfigWorkBasket.urlFhempl = configDataCommon.urlFhempl;
        }

        ConfigWorkBasket.fhemjsPW = configDataCommon.fhemjsPW;
    }

    public void doConnect() {
        Log.d(TAG, "doConnect");
        socket.connect();
    }

    public void requestValues(ArrayList<String> unitsList, String type) {
        for (String unit : unitsList) {
            if (type.equals("once")) {
                socket.emit("getValueOnce", unit);
            } else {
                //Log.d(TAG,"getValueOnChange: " + unit);
                socket.emit("getValueOnChange", unit);
            }
        }
    }

    public void sendCommand(String cmd) {
        //if (BuildConfig.DEBUG) Log.d("mySocket command",cmd);
        if (socket != null && cmd != null) {
            socket.emit("commandNoResp", cmd);
        }
    }

    public void destroy() {
        socket.disconnect();
        socket.close();
        socket.off("authenticated");
        socket.off(Socket.EVENT_DISCONNECT);
        socket.off(Socket.EVENT_RECONNECT_FAILED);
        socket.off(Socket.EVENT_CONNECT_ERROR);
        socket.off("value");
        socket.off("version");
        socket.off("fhemError");
        socket.off("fhemConn");
    }



    public void refresh() {
        socket.emit("refreshValues");
    }
}
