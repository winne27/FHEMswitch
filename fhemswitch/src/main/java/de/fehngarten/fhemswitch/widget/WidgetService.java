package de.fehngarten.fhemswitch.widget;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import java.util.Map.Entry;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.StrictMode;
import android.view.View;
import android.view.Display;
import android.hardware.display.DisplayManager;
import android.widget.RemoteViews;

import de.fehngarten.fhemswitch.BuildConfig;
import de.fehngarten.fhemswitch.modul.MyBroadcastReceiver;
import de.fehngarten.fhemswitch.modul.MyReceiveListener;
import de.fehngarten.fhemswitch.modul.MySetOnClickPendingIntent;
import de.fehngarten.fhemswitch.modul.VersionChecks;
import de.fehngarten.fhemswitch.data.ConfigData;
import de.fehngarten.fhemswitch.data.ConfigDataOnly;
import de.fehngarten.fhemswitch.data.ConfigDataOnlyIO;
import de.fehngarten.fhemswitch.modul.GetStoreVersion;
import de.fehngarten.fhemswitch.data.MyCommand;
import de.fehngarten.fhemswitch.data.MyLayout;
import de.fehngarten.fhemswitch.data.MySocket;
import de.fehngarten.fhemswitch.data.MySwitch;
import de.fehngarten.fhemswitch.data.MyValue;
import de.fehngarten.fhemswitch.R;
//import de.fehngarten.fhemswitch.widget.listviews.ListviewService;
import de.fehngarten.fhemswitch.widget.listviews.CommonListviewService;
//import de.fehngarten.fhemswitch.widget.listviews.ValuesService;
//import de.fehngarten.fhemswitch.widget.listviews.LightScenesService;
//import de.fehngarten.fhemswitch.widget.listviews.CommandsService;
import de.fehngarten.fhemswitch.data.ConfigCommandRow;
import de.fehngarten.fhemswitch.data.ConfigLightsceneRow;
import de.fehngarten.fhemswitch.data.ConfigSwitchRow;
import de.fehngarten.fhemswitch.data.ConfigValueRow;
import io.socket.client.Socket;

import de.fehngarten.fhemswitch.data.MyLightScenes.MyLightScene;

import android.util.Log;

import static de.fehngarten.fhemswitch.global.Consts.*;
import static de.fehngarten.fhemswitch.global.Settings.*;

public class WidgetService extends Service {
    private static final String TAG = "WidgetService";
    private VersionChecks versionChecks;
    public static MySocket mySocket = null;

    public static String websocketUrl;
    public static String fhemUrl;
    public static Map<String, Integer> icons = new HashMap<>();
    public static int switchCols;
    public static int valueCols;
    public static int commandCols;

    private static AppWidgetManager appWidgetManager;
    private static int iLayout;
    private static int[] allWidgetIds;
    private static int[] layouts = new int[3];
    static Handler handler = new Handler();
    private Context mContext;
    private RemoteViews mView;

    public static ConfigData configData;
    private ConfigDataOnly configDataOnly;
    private ConfigDataOnlyIO configDataOnlyIO;

    public static PowerManager pm;
    private int layoutId;

    private PendingIntent onClickPendingIntent;
    public static MyLayout myLayout;
    public Boolean valuesRequested = false;
    public static Boolean serviceRunning = false;
    private int waitCount = 0;
    private String currentVersionType;
    public ArrayList<BroadcastReceiver> broadcastReceivers;

    public void onCreate() {
        if (BuildConfig.DEBUG) Log.d(TAG, "onCreate fired");

        super.onCreate();

        serviceRunning = true;
        mContext = getApplicationContext();
        appWidgetManager = AppWidgetManager.getInstance(mContext);
        pm = (PowerManager) getSystemService(Context.POWER_SERVICE);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        icons.put("on", R.drawable.on);
        icons.put("set_on", R.drawable.set_on);
        icons.put("off", R.drawable.off);
        icons.put("set_off", R.drawable.set_off);
        icons.put("set_toggle", R.drawable.set_toggle);
        icons.put("undefined", R.drawable.undefined);
        icons.put("toggle", R.drawable.undefined);

        versionChecks = new VersionChecks();
        start();

        checkVersion();
        setBroadcastReceivers();
        handler.postDelayed(checkVersionTimer, settingWaitIntervalVersionCheck);
        handler.postDelayed(checkShowVersionTimer, settingDelayShowVersionCheck);
    }

    private void start() {
        readConfig();
        setOrientation();
        doStart(5);
    }

    @Override
    public void onDestroy() {
        if (BuildConfig.DEBUG) Log.d(TAG, "onDestroy fired");
        if (mySocket != null) {
            mySocket.socket.disconnect();
            mySocket.socket.close();
        }

        handler.removeCallbacks(checkSocketTimer);
        handler.removeCallbacks(checkVersionTimer);
        handler.removeCallbacks(checkShowVersionTimer);
        handler.removeCallbacks(setBroadcastReceiverConnectivity);
        serviceRunning = false;
        for (BroadcastReceiver broadcastReceiver : broadcastReceivers) {
            try {
                unregisterReceiver(broadcastReceiver);
            } catch (java.lang.IllegalArgumentException e) {
                if (BuildConfig.DEBUG) Log.d(TAG, "unregister failed");
            }
        }

        super.onDestroy();
    }

    private void setBroadcastReceivers() {

        broadcastReceivers = new ArrayList<>();

        String[] actions = new String[]{STORE_VERSION_WIDGET};
        broadcastReceivers.add(new MyBroadcastReceiver(this, new OnStoreVersion(), actions));

        actions = new String[]{NEW_VERSION_STORE, NEW_VERSION_SUPPRESS, NEW_VERSION_REMEMBER};
        broadcastReceivers.add(new MyBroadcastReceiver(this, new OnUserIntent(), actions));

        actions = new String[]{Intent.ACTION_CONFIGURATION_CHANGED, NEW_CONFIG};
        broadcastReceivers.add(new MyBroadcastReceiver(this, new OnConfigChange(), actions));

        actions = new String[]{Intent.ACTION_SCREEN_ON, Intent.ACTION_SCREEN_OFF};
        broadcastReceivers.add(new MyBroadcastReceiver(this, new OnScreenOnOff(), actions));

        handler.postDelayed(setBroadcastReceiverConnectivity, settingDelayShowVersionCheck);
    }

    Runnable setBroadcastReceiverConnectivity = new Runnable() {
        @Override
        public void run() {
            String[] actions = new String[]{ConnectivityManager.CONNECTIVITY_ACTION};
            broadcastReceivers.add(new MyBroadcastReceiver(mContext, new OnConnectionChange(), actions));

        }
    };

    class OnScreenOnOff implements MyReceiveListener {
        public void run(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
                waitCount = 0;
            }
            handler.postDelayed(checkSocketTimer, settingDelaySocketCheck);
        }
    }

    class OnConfigChange implements MyReceiveListener {
        public void run(Context context, Intent intent) {
            start();
        }
    }

    class OnStoreVersion implements MyReceiveListener {
        public void run(Context context, Intent intent) {
            versionChecks.setVersions(VERSION_APP, BuildConfig.VERSION_NAME, intent.getExtras().getString(GetStoreVersion.LATEST));
        }
    }

    class OnUserIntent implements MyReceiveListener {
        public void run(Context context, Intent intent) {
            switch (intent.getAction()) {
                case NEW_VERSION_STORE:
                    final String appPackageName = getPackageName();
                    try {
                        Intent storeIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName));
                        storeIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(storeIntent);
                    } catch (android.content.ActivityNotFoundException anfe) {
                        Intent storeIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName));
                        storeIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(storeIntent);
                    }
                    setVisibility(VERSION_CLOSE, null);
                    versionChecks.setDateShown(currentVersionType);
                    break;
                case NEW_VERSION_SUPPRESS:
                    setVisibility(VERSION_CLOSE, null);
                    versionChecks.setSuppressedToLatest(currentVersionType);
                    saveSuppressedVersions(currentVersionType);
                    break;
                case NEW_VERSION_REMEMBER:
                    setVisibility(VERSION_CLOSE, null);
                    versionChecks.setDateShown(currentVersionType);
                    break;
            }
        }
    }

    class OnConnectionChange implements MyReceiveListener {
        public void run(Context context, Intent intent) {
            ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            if (networkInfo != null) {
                NetworkInfo.State state = networkInfo.getState();
                if (state.toString().equals("CONNECTED")) {
                    //doStart(6);
                    checkSocket();
                }
            }
        }
    }

    public void readConfig() {
        if (BuildConfig.DEBUG) Log.d(TAG, "readConfig started");
        valuesRequested = false;
        configDataOnlyIO = new ConfigDataOnlyIO(mContext, null);
        configDataOnly = configDataOnlyIO.read();

        websocketUrl = configDataOnly.urljs;
        fhemUrl = configDataOnly.urlpl;
        switchCols = configDataOnly.switchCols;
        valueCols = configDataOnly.valueCols;
        commandCols = configDataOnly.commandCols;
        layouts[Integer.valueOf(getString(R.string.LAYOUT_HORIZONTAL))] = R.layout.main_layout_horizontal;
        layouts[Integer.valueOf(getString(R.string.LAYOUT_VERTICAL))] = R.layout.main_layout_vertical;
        layouts[Integer.valueOf(getString(R.string.LAYOUT_MIXED))] = R.layout.main_layout_mixed;

        if (configDataOnly.suppressedVersions != null) {
            for (Map.Entry<String, String> entry : configDataOnly.suppressedVersions.entrySet()) {
                String type = entry.getKey();
                String suppressedVersion = entry.getValue();
                versionChecks.setSuppressedVersion(type, suppressedVersion);
            }
        }
    }

    public void setOrientation() {
        Configuration config = getResources().getConfiguration();

        if (config.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            iLayout = configDataOnly.layoutLandscape;
        } else {
            iLayout = configDataOnly.layoutPortrait;
        }

        if (iLayout > 0) {
            switchCols = 0;
            valueCols = 0;
            commandCols = 0;
        }
    }

    public void doStart(int nr) {
        if (BuildConfig.DEBUG) Log.d(TAG, "doStart started with " + Integer.toString(nr));

        handler.postDelayed(checkSocketTimer, settingDelaySocketCheck);

        allWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(mContext, WidgetProvider.class));
        layoutId = layouts[iLayout];
        mView = new RemoteViews(mContext.getPackageName(), layoutId);

        configData = new ConfigData();
        if (myLayout != null) {
            for (int widgetId : allWidgetIds) {
                for (Entry<String, ArrayList<Integer>> entry : myLayout.layout.entrySet()) {
                    for (int listviewId : entry.getValue()) {
                        appWidgetManager.notifyAppWidgetViewDataChanged(widgetId, listviewId);
                    }
                }
            }
        }

        //-- control switches  ------------------------------------------------------------------
        if (configDataOnly.switchRows != null) {
            for (ConfigSwitchRow switchRow : configDataOnly.switchRows) {
                if (switchRow.enabled) {
                    //if (BuildConfig.DEBUG) Log.d(TAG,"unit: " + switchRow.unit + " cmd: " + switchRow.cmd);
                    configData.switches.add(new MySwitch(switchRow.name, switchRow.unit, switchRow.cmd));
                }
            }
        }
        int switchCount = configData.switches.size();

        //-- control lightscenes  ------------------------------------------------------------------
        MyLightScene newLightScene = null;
        if (configDataOnly.lightsceneRows != null) {
            for (ConfigLightsceneRow lightsceneRow : configDataOnly.lightsceneRows) {
                if (lightsceneRow.isHeader) {
                    newLightScene = configData.lightScenes.newLightScene(lightsceneRow.name, lightsceneRow.unit, lightsceneRow.showHeader);
                } else {
                    if (newLightScene != null) {
                        newLightScene.addMember(lightsceneRow.name, lightsceneRow.unit, lightsceneRow.enabled);
                    }
                }
            }
        }
        int lightsceneCount = configData.lightScenes.itemsCount;

        //-- control values  ------------------------------------------------------------------
        if (configDataOnly.valueRows != null) {
            for (ConfigValueRow valueRow : configDataOnly.valueRows) {
                if (valueRow.enabled) {
                    configData.values.add(new MyValue(valueRow.name, valueRow.unit));
                }
            }
        }
        int valueCount = configData.values.size();

        //-- control commands  ------------------------------------------------------------------
        if (configDataOnly.commandRows != null) {
            for (ConfigCommandRow commandRow : configDataOnly.commandRows) {
                if (commandRow.enabled) {
                    configData.commands.add(new MyCommand(commandRow.name, commandRow.command, false));
                }
            }
        }
        int commandCount = configData.commands.size();
        myLayout = new MyLayout(iLayout, switchCols, valueCols, commandCols, switchCount, lightsceneCount, valueCount, commandCount);

        //-- control switches  ------------------------------------------------------------------
        for (int i = 0; i <= switchCols; i++) {
            configData.switchesCols.add(new ArrayList<>());
        }

        int rownum = 0;
        int colnum = 0;
        for (MySwitch switchRow : configData.switches) {
            rownum = rownum + 1;
            configData.switchesCols.get(colnum).add(switchRow);
            if (rownum % myLayout.rowsPerCol.get("switch") == 0) {
                colnum++;
            }
        }

        //-- control values  ------------------------------------------------------------------
        for (int i = 0; i <= valueCols; i++) {
            configData.valuesCols.add(new ArrayList<>());
        }
        rownum = 0;
        colnum = 0;
        for (MyValue valueRow : configData.values) {
            rownum = rownum + 1;
            configData.valuesCols.get(colnum).add(valueRow);
            if (rownum % myLayout.rowsPerCol.get("value") == 0) {
                colnum++;
            }
        }

        //-- control commands  ------------------------------------------------------------------
        for (int i = 0; i <= commandCols; i++) {
            configData.commandsCols.add(new ArrayList<>());
        }
        rownum = 0;
        colnum = 0;
        for (MyCommand commandRow : configData.commands) {
            rownum = rownum + 1;
            configData.commandsCols.get(colnum).add(commandRow);
            if (rownum % myLayout.rowsPerCol.get("command") == 0) {
                colnum++;
            }
        }
        // -------------------------------------------------------------------------------
        initListviews();
    }

    public static void sendCommand(Intent intent) {

        String type = intent.getExtras().getString(FHEM_TYPE);
        String cmd = intent.getExtras().getString(FHEM_COMMAND);

        int actCol = 0;
        int position = -1;
        if (type != null) {
            switch (type) {
                case "switch":
                    position = Integer.parseInt(intent.getExtras().getString(POS));
                    actCol = Integer.parseInt(intent.getExtras().getString(COL));
                    break;
                case "lightscene":
                    position = Integer.parseInt(intent.getExtras().getString(POS));
                    break;
                case "command":
                    position = Integer.parseInt(intent.getExtras().getString(POS));
                    actCol = Integer.parseInt(intent.getExtras().getString(COL));
                    break;
            }

            mySocket.sendCommand(cmd);

            switch (type) {
                case "switch":
                    configData.switchesCols.get(actCol).get(position).setIcon("set_toggle");

                    for (int id : allWidgetIds) {
                        appWidgetManager.notifyAppWidgetViewDataChanged(id, myLayout.layout.get("switch").get(actCol));
                    }
                    break;
                case "lightscene":
                    configData.lightScenes.items.get(position).activ = true;

                    for (int id : allWidgetIds) {
                        appWidgetManager.notifyAppWidgetViewDataChanged(id, myLayout.layout.get("lightscene").get(0));
                    }
                    //handler.postDelayed(deactLightscene, 500);
                    break;
                case "command":
                    configData.commandsCols.get(actCol).get(position).activ = true;
                    for (int id : allWidgetIds) {
                        appWidgetManager.notifyAppWidgetViewDataChanged(id, myLayout.layout.get("command").get(actCol));
                    }
                    Runnable deactCommand = new DeactCommand(actCol, position);
                    handler.postDelayed(deactCommand, 500);
                    break;
            }
        }
    }

    // after touch command button is for 500ms pride
    public static class DeactCommand implements Runnable {
        int actCol;
        int actPos;

        DeactCommand(int actCol, int actPos) {
            this.actCol = actCol;
            this.actPos = actPos;
        }

        public void run() {
            configData.commands.get(actPos).activ = false;

            for (int id : allWidgetIds) {
                appWidgetManager.notifyAppWidgetViewDataChanged(id, myLayout.layout.get("command").get(actCol));
            }
        }
    }

    private void initListviews() {
        if (BuildConfig.DEBUG) Log.d(TAG, "initListviews started");
        final Intent onItemClick = new Intent(mContext, WidgetProvider.class);
        onItemClick.setData(Uri.parse(onItemClick.toUri(Intent.URI_INTENT_SCHEME)));
        onClickPendingIntent = PendingIntent.getBroadcast(mContext, 0, onItemClick, PendingIntent.FLAG_UPDATE_CURRENT);
        mView = new RemoteViews(mContext.getPackageName(), layoutId);

        mView.setViewVisibility(R.id.noconn, View.GONE);
        mView.setViewVisibility(R.id.new_version, View.GONE);
        for (int viewId : myLayout.goneViews) {
            mView.setViewVisibility(viewId, View.GONE);
        }

        for (int widgetId : allWidgetIds) {

            for (Entry<String, ArrayList<Integer>> entry : myLayout.layout.entrySet()) {
                String type = entry.getKey();
                int actCol = 0;
                for (int listviewId : entry.getValue()) {
                    initListview(widgetId, listviewId, actCol, type);
                    mView.setViewVisibility(listviewId, View.VISIBLE);
                    actCol++;
                }
            }
            appWidgetManager.updateAppWidget(widgetId, mView);
        }
    }

    private void initListview(int widgetId, int listviewId, int actCol, String type) {
        mView.setPendingIntentTemplate(listviewId, onClickPendingIntent);
        Class serviceClass = CommonListviewService.class;
        Intent myIntent = new Intent(mContext, serviceClass);
        myIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
        myIntent.putExtra(ACTCOL, actCol);
        myIntent.putExtra(FHEM_TYPE, type);
        String uriString = myIntent.toUri(Intent.URI_INTENT_SCHEME);
        myIntent.setData(Uri.parse(uriString));
        mView.setRemoteAdapter(listviewId, myIntent);
        mView.setViewVisibility(listviewId, View.VISIBLE);
        appWidgetManager.notifyAppWidgetViewDataChanged(widgetId, myLayout.layout.get(type).get(actCol));
    }

    public Runnable checkSocketTimer = new Runnable() {
        @Override
        public void run() {
            handler.removeCallbacks(checkSocketTimer);
            checkSocket();
            Integer wait;
            waitCount++;
            if (mySocket != null && mySocket.socket.connected()) {
                wait = settingWaitIntervalLong;
            } else {
                if (waitCount > settingWaitCyclesShort) {
                    wait = settingWaitIntervalLong;
                } else {
                    wait = settingWaitIntervalShort;
                }
            }

            //handler.removeCallbacks(this);
            handler.postDelayed(this, wait);
        }
    };

    public void checkSocket() {
        //Boolean callInitSocket = false;
        //if (BuildConfig.DEBUG) Log.d(TAG, "checkSocket started");
        //if (BuildConfig.DEBUG) Log.d("trace screen is on", String.valueOf(isScreenOn()));
        if (mySocket == null || !mySocket.socket.connected()) {
            if (isScreenOn()) {
                initSocket();
                valuesRequested = false;
            }
        } else if (!isScreenOn() && mySocket.socket.connected()) {
            //if (BuildConfig.DEBUG) Log.d("trace", "disconnect socket");
            mySocket.socket.disconnect();
            mySocket.socket.close();
            mySocket = null;
            valuesRequested = true;
        }

        if (mySocket != null && !valuesRequested && mySocket.socket.connected()) {
            requestValues("checkSocket");
        }
    }

    public boolean isScreenOn() {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
            return isScreenOnNew();
        } else {
            return isScreenOnOld();
        }
    }

    @SuppressWarnings("deprecation")
    public boolean isScreenOnOld() {
        PowerManager pm = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
        return pm.isScreenOn();
    }

    @TargetApi(Build.VERSION_CODES.KITKAT_WATCH)
    public boolean isScreenOnNew() {
        DisplayManager dm = (DisplayManager) mContext.getSystemService(Context.DISPLAY_SERVICE);
        boolean screenOn = false;
        for (Display display : dm.getDisplays()) {
            if (display.getState() != Display.STATE_OFF) {
                screenOn = true;
            }
        }
        return screenOn;
    }

    private void setVisibility(String type, String text) {
        //Log.i("type of setVisibility",type);
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(mContext);

        switch (type) {
            case VERSION_APP:
                mView.setTextViewText(R.id.new_version_text, text);
                mView.setViewVisibility(R.id.new_version_store_button, View.VISIBLE);
                mView.setViewVisibility(R.id.new_version, View.VISIBLE);
                new MySetOnClickPendingIntent(mContext, mView, NEW_VERSION_STORE, R.id.new_version_store_button);
                new MySetOnClickPendingIntent(mContext, mView, NEW_VERSION_SUPPRESS, R.id.new_version_show_button);
                new MySetOnClickPendingIntent(mContext, mView, NEW_VERSION_REMEMBER, R.id.new_version_remember_button);
                setVisibilityListViews(View.GONE);
                break;
            case VERSION_FHEMJS:
                mView.setTextViewText(R.id.new_version_text, text);
                mView.setViewVisibility(R.id.new_version_store_button, View.GONE);
                mView.setViewVisibility(R.id.new_version, View.VISIBLE);
                new MySetOnClickPendingIntent(mContext, mView, NEW_VERSION_SUPPRESS, R.id.new_version_show_button);
                new MySetOnClickPendingIntent(mContext, mView, NEW_VERSION_REMEMBER, R.id.new_version_remember_button);
                setVisibilityListViews(View.GONE);
                break;
            case SOCKET_CONNECTED:
                mView.setViewVisibility(R.id.noconn, View.GONE);
                mView.setViewVisibility(R.id.new_version, View.GONE);
                setVisibilityListViews(View.VISIBLE);
                break;
            case VERSION_CLOSE:
                mView.setViewVisibility(R.id.new_version, View.GONE);
                setVisibilityListViews(View.VISIBLE);
                break;
            case SOCKET_DISCONNECTED:
                mView.setTextViewText(R.id.noconn, text);
                mView.setViewVisibility(R.id.noconn, View.VISIBLE);
                new MySetOnClickPendingIntent(mContext, mView, NEW_CONFIG, R.id.noconn);
                setVisibilityListViews(View.GONE);
                break;
        }

        for (int widgetId : allWidgetIds) {
            appWidgetManager.updateAppWidget(widgetId, mView);
        }
    }

    private void setVisibilityListViews(int action) {
        for (Entry<String, ArrayList<Integer>> entry : myLayout.layout.entrySet()) {
            for (int listviewId : entry.getValue()) {
                mView.setViewVisibility(listviewId, action);
            }
        }
    }

    private void requestValues(String from) {
        if (BuildConfig.DEBUG) Log.d(TAG, "requestValues started by " + from);

        mySocket.requestValues(configData.getSwitchesList(), "once");
        mySocket.requestValues(configData.getValuesList(), "once");
        mySocket.requestValues(configData.getLightScenesList(), "once");
        valuesRequested = true;

        mySocket.requestValues(configData.getSwitchesList(), "onChange");
        mySocket.requestValues(configData.getValuesList(), "onChange");
        mySocket.requestValues(configData.getLightScenesList(), "onChange");
    }

    private void initSocket() {
        if (BuildConfig.DEBUG) Log.d(TAG, "initSocket started");

        mySocket = new MySocket(websocketUrl, mContext, "Widget");
        mySocket.socket.off(Socket.EVENT_CONNECT);
        mySocket.socket.on(Socket.EVENT_CONNECT, args -> {
            String pw = configDataOnly.connectionPW;
            if (!pw.equals("")) {
                mySocket.socket.emit("authentication", pw);
            }
            requestValues("initSocket");
            setVisibility(SOCKET_CONNECTED, "");


            mySocket.socket.off(Socket.EVENT_DISCONNECT);
            mySocket.socket.on(Socket.EVENT_DISCONNECT, args1 -> setVisibility(SOCKET_DISCONNECTED, getString(R.string.noconn)));

            mySocket.socket.off(Socket.EVENT_RECONNECT_FAILED);
            mySocket.socket.on(Socket.EVENT_RECONNECT_FAILED, args1 -> setVisibility(SOCKET_DISCONNECTED, getString(R.string.noconn)));

            mySocket.socket.off(Socket.EVENT_CONNECT_ERROR);
            mySocket.socket.on(Socket.EVENT_CONNECT_ERROR, args1 -> {
                mySocket.socket.close();
                mySocket.socket.off();
                mySocket = null;
                setVisibility(SOCKET_DISCONNECTED, getString(R.string.noconn));
            });

            mySocket.socket.off("value");
            mySocket.socket.on("value", args1 -> {
                JSONObject obj = (JSONObject) args1[0];
                Iterator<String> iterator = obj.keys();
                String unit = null;
                while (iterator.hasNext()) {
                    unit = iterator.next();
                    String value = null;
                    try {
                        value = obj.getString(unit);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    //if (BuildConfig.DEBUG) Log.d(TAG,"new value: " + unit + ":" + value);
                    int actCol = configData.setSwitchIcon(unit, value);
                    if (actCol > -1) {
                        for (int id : allWidgetIds) {
                            appWidgetManager.notifyAppWidgetViewDataChanged(id, myLayout.layout.get("switch").get(actCol));
                        }
                    }

                    actCol = configData.setValue(unit, value);
                    if (actCol > -1) {
                        for (int id : allWidgetIds) {
                            appWidgetManager.notifyAppWidgetViewDataChanged(id, myLayout.layout.get("value").get(actCol));
                        }
                    }

                    if (configData.setLightscene(unit, value)) {
                        for (int id : allWidgetIds) {
                            appWidgetManager.notifyAppWidgetViewDataChanged(id, myLayout.layout.get("lightscene").get(0));
                        }
                    }


                }
            });

            mySocket.socket.off("version");
            mySocket.socket.on("version", args1 -> {
                if (BuildConfig.DEBUG) Log.d("version", args1[0].toString());
                JSONObject obj = (JSONObject) args1[0];
                try {
                    versionChecks.setVersions(obj.getString("type"), obj.getString("installed"), obj.getString("latest"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            });

            mySocket.socket.off("fhemError");
            mySocket.socket.on("fhemError", args1 -> setVisibility(SOCKET_DISCONNECTED, getString(R.string.noconn)));

            mySocket.socket.off("fhemConn");
            mySocket.socket.on("fhemConn", args1 -> setVisibility(SOCKET_CONNECTED, ""));
        });
    }

    Runnable checkVersionTimer = new Runnable() {
        @Override
        public void run() {
            try {
                handler.removeCallbacks(checkVersionTimer);
                checkVersion();
            } finally {
                handler.postDelayed(checkVersionTimer, settingWaitIntervalVersionCheck);
            }
        }
    };

    Runnable checkShowVersionTimer = new Runnable() {
        @Override
        public void run() {
            handler.removeCallbacks(checkShowVersionTimer);
            try {
                checkShowVersion();
            } finally {
                handler.postDelayed(checkShowVersionTimer, settingWaitIntervalVersionShowCheck);
            }
        }
    };

    private void checkVersion() {
        if (BuildConfig.DEBUG) Log.d(TAG, "checkVersion started");

        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        boolean isWiFi = networkInfo != null && networkInfo.getType() == ConnectivityManager.TYPE_WIFI;
        if (isWiFi) {
            new GetStoreVersion(mContext, STORE_VERSION_WIDGET).execute();
        }
    }

    private void checkShowVersion() {
        if (BuildConfig.DEBUG) Log.d(TAG, "checkShowVersion started");
        if (!isScreenOn()) return;
        //if (BuildConfig.DEBUG) Log.d(TAG, versionChecks.typesToString());
        String type = versionChecks.showVersionHint();

        if (type != null) {
            String hint = "";
            String installedVersion = versionChecks.getInstalledVersion(type);
            String latestVersion = versionChecks.getLatestVersion(type);

            switch (type) {
                case VERSION_APP:
                    hint = getString(R.string.newVersionApp, installedVersion, latestVersion);
                    break;
                case VERSION_FHEMJS:
                    hint = getString(R.string.newVersionFhemjs, installedVersion, latestVersion);
                    break;
                case VERSION_FHEMPL:
                    hint = getString(R.string.newVersionFhemjs, installedVersion, latestVersion);
                    break;
            }

            setVisibility(type, hint);
            currentVersionType = type;
            if (BuildConfig.DEBUG) Log.d(TAG, "show version hint " + type);
        }
    }

    private void saveSuppressedVersions(String type) {
        configDataOnly.suppressedVersions.put(type, versionChecks.getSuppressedVersion(type));
        configDataOnlyIO.save(configDataOnly);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        return null;
    }

}
