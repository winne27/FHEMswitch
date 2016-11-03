package de.fehngarten.fhemswitch;

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
import android.content.IntentFilter;
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

import io.socket.client.Socket;

import de.fehngarten.fhemswitch.MyLightScenes.Item;
import de.fehngarten.fhemswitch.MyLightScenes.MyLightScene;

import android.util.Log;


public class WidgetService extends Service {
    static final String NEW_CONFIG = "de.fehngarten.fhemswitch.NEW_CONFIG";
    static final String NEW_VERSION_STORE = "de.fehngarten.fhemswitch.NEW_VERSION_STORE";
    static final String NEW_VERSION_SHOW = "de.fehngarten.fhemswitch.NEW_VERSION_SHOW";
    static final String NEW_VERSION_REMEMBER = "de.fehngarten.fhemswitch.NEW_VERSION_REMEMBER";
    static final String TAG = "WidgetService";
    static final String VERSION_APP = "app";
    static final String VERSION_FHEMJS = "fhemjs";
    static final String VERSION_FHEMPL = "fhempl";
    static final String VERSION_CLOSE = "closeversion";
    static final String SOCKET_CONNECTED = "connected";
    static final String SOCKET_DISCONNECTED = "disconnected";
    static final String STORE_VERSION_WIDGET = "de.fehngarten.fhemswitch.STORE_VERSION_WIDGET";
    private String newVersionType;
    private String versionLatest;
    private String versionType;
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
    private static Handler handler = new Handler();
    private Context mContext;
    private RemoteViews mView;

    public static ConfigData configData;
    private ConfigDataOnly configDataOnly;
    private ConfigDataOnlyIO configDataOnlyIO;

    public static PowerManager pm;
    private int layoutId;

    private PendingIntent onClickPendingIntent;
    public static MyLayout myLayout;
    public static HashMap<String, ArrayList<Class<?>>> listViewServices;
    public Boolean valuesRequested = false;
    public static Boolean serviceRunning = false;
    private int waitCount = 0;
    private String storeVersion;
    private BroadcastReceiver newConfigReceiver;
    private BroadcastReceiver connChangeReceiver;
    private BroadcastReceiver userIntentReceiver;
    private BroadcastReceiver screenReceiver;
    private BroadcastReceiver storeVersionReceiver;
    private HashMap<String, VersionCheck> versionChecks;

    public void onCreate() {
        Log.d(TAG, "onCreate fired");

        super.onCreate();

        serviceRunning = true;
        mContext = getApplicationContext();
        appWidgetManager = AppWidgetManager.getInstance(mContext);
        pm = (PowerManager) getSystemService(Context.POWER_SERVICE);

        icons.put("on", R.drawable.on);
        icons.put("set_on", R.drawable.set_on);
        icons.put("off", R.drawable.off);
        icons.put("set_off", R.drawable.set_off);
        icons.put("set_toggle", R.drawable.set_toggle);
        icons.put("undefined", R.drawable.undefined);
        icons.put("toggle", R.drawable.undefined);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        versionChecks = new HashMap<>();

        versionChecks.put(VERSION_APP, new VersionCheck(VERSION_APP));
        versionChecks.put(VERSION_FHEMJS, new VersionCheck(VERSION_FHEMJS));
        versionChecks.put(VERSION_FHEMPL, new VersionCheck(VERSION_FHEMPL));

        // intent get store version
        storeVersionReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                storeVersion = intent.getExtras().getString(GetStoreVersion.LATEST);
                Log.d(TAG, "get store version: " + storeVersion);
                String localVersion = BuildConfig.VERSION_NAME;

                VersionCheck versionCheck = versionChecks.get(VERSION_APP);
                versionCheck.setLatest(storeVersion);
                versionCheck.setInstalled(localVersion);
                versionChecks.put(VERSION_APP, versionCheck);

            }
        };

        IntentFilter storeVersionFilter = new IntentFilter();
        storeVersionFilter.addAction(STORE_VERSION_WIDGET);
        registerReceiver(storeVersionReceiver, storeVersionFilter);

        handler.postDelayed(checkVersionTimer, getResources().getInteger(R.integer.delayVersionCheck));
        handler.postDelayed(checkShowVersionTimer, getResources().getInteger(R.integer.delayShowVersionCheck));

        // Intent conn changed
        connChangeReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d(TAG, "connchanged " + intent.getAction());
                ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
                if (networkInfo != null) {
                    NetworkInfo.State state = networkInfo.getState();
                    //Log.d("netState", state.toString());
                    if (state.toString().equals("CONNECTED")) {
                        doStart(6);
                    }
                }
            }
        };

        IntentFilter connChangeFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(connChangeReceiver, connChangeFilter);

        // --- Intent user --------------------------------------------------------------------------------
        userIntentReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                Log.d(TAG, "userIntent fired by " + action);
                VersionCheck versionCheck = versionChecks.get(newVersionType);
                switch (action) {
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
                        versionCheck.setDateShown();
                        versionChecks.put(VERSION_APP, versionCheck);
                        break;
                    case NEW_VERSION_SHOW:
                        String latestSuppress = stopVersionRemember();
                        setVisibility(VERSION_CLOSE, null);
                        versionCheck.setSuppress(latestSuppress);
                        versionChecks.put(VERSION_APP, versionCheck);
                        break;
                    case NEW_VERSION_REMEMBER:
                        setVisibility(VERSION_CLOSE, null);
                        versionCheck.setDateShown();
                        versionChecks.put(VERSION_APP, versionCheck);
                        break;
                }
            }
        };

        IntentFilter newVersionStoreFilter = new IntentFilter();
        newVersionStoreFilter.addAction(NEW_VERSION_STORE);
        newVersionStoreFilter.addAction(NEW_VERSION_SHOW);
        newVersionStoreFilter.addAction(NEW_VERSION_REMEMBER);
        registerReceiver(userIntentReceiver, newVersionStoreFilter);

        // --- intent configuration changed (orientation) -------------------------------------------------
        newConfigReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d(TAG, "orientchange fired");
                readConfig();
            }
        };

        IntentFilter orientFilter = new IntentFilter();
        orientFilter.addAction(Intent.ACTION_CONFIGURATION_CHANGED);
        orientFilter.addAction(NEW_CONFIG);
        registerReceiver(newConfigReceiver, orientFilter);

        // intent screen on/off
        screenReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d(TAG, "screenaction " + intent.getAction());

                if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
                    waitCount = 0;
                }
                handler.postDelayed(checkSocketTimer, getResources().getInteger(R.integer.delaySocketCheck));
            }
        };

        IntentFilter screenFilter = new IntentFilter();
        screenFilter.addAction(Intent.ACTION_SCREEN_ON);
        screenFilter.addAction(Intent.ACTION_SCREEN_OFF);
        registerReceiver(screenReceiver, screenFilter);
        // ---------------------------------------

        readConfig();
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy fired");
        mySocket.socket.disconnect();
        mySocket.socket.close();
        mySocket = null;
        handler.removeCallbacks(checkSocketTimer);
        serviceRunning = false;
        unregisterReceiver(newConfigReceiver);
        unregisterReceiver(connChangeReceiver);
        unregisterReceiver(userIntentReceiver);
        unregisterReceiver(screenReceiver);
        unregisterReceiver(storeVersionReceiver);

        super.onDestroy();
    }

    public void readConfig() {
        Log.d(TAG, "readConfig started");
        valuesRequested = false;
        configDataOnlyIO = new ConfigDataOnlyIO(mContext);
        configDataOnly = configDataOnlyIO.read();

        websocketUrl = configDataOnly.urljs;
        fhemUrl = configDataOnly.urlpl;
        switchCols = configDataOnly.switchCols;
        valueCols = configDataOnly.valueCols;
        commandCols = configDataOnly.commandCols;
        layouts[Integer.valueOf(getString(R.string.LAYOUT_HORIZONTAL))] = R.layout.main_layout_horizontal;
        layouts[Integer.valueOf(getString(R.string.LAYOUT_VERTICAL))] = R.layout.main_layout_vertical;
        layouts[Integer.valueOf(getString(R.string.LAYOUT_MIXED))] = R.layout.main_layout_mixed;

        saveSuppressed(VERSION_APP, configDataOnly.suppressNewAppVersion);
        saveSuppressed(VERSION_FHEMJS, configDataOnly.suppressNewNodeVersion);
        saveSuppressed(VERSION_FHEMPL, configDataOnly.suppressNewFhemVersion);

        setOrientation();
        doStart(5);
        initListviews();
    }

    private void saveSuppressed(String type, String version) {
        VersionCheck versionCheck = versionChecks.get(type);
        versionCheck.setSuppress(version);
        versionChecks.put(type, versionCheck);
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
        Log.d(TAG, "doStart started with " + Integer.toString(nr));

        allWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(mContext, WidgetProvider.class));

        listViewServices = new HashMap<>();

        ArrayList<Class<?>> classes = new ArrayList<>();
        classes.add(SwitchesService0.class);
        classes.add(SwitchesService1.class);
        classes.add(SwitchesService2.class);
        listViewServices.put("switch", classes);

        classes = new ArrayList<>();
        classes.add(ValuesService0.class);
        classes.add(ValuesService1.class);
        classes.add(ValuesService2.class);
        listViewServices.put("value", classes);

        classes = new ArrayList<>();
        classes.add(CommandsService0.class);
        classes.add(CommandsService1.class);
        classes.add(CommandsService2.class);
        listViewServices.put("command", classes);

        classes = new ArrayList<>();
        classes.add(LightScenesService.class);
        listViewServices.put("lightscene", classes);
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
                    //Log.d(TAG,"unit: " + switchRow.unit + " cmd: " + switchRow.cmd);
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

        layoutId = layouts[iLayout];

        //handler.postDelayed(checkSocketTimer, R.integer.delaySocketCheck);
        handler.postDelayed(checkSocketTimer, getResources().getInteger(R.integer.delaySocketCheck));
        mView = new RemoteViews(mContext.getPackageName(), layoutId);

        Intent clickIntent = new Intent();
        clickIntent.setAction(NEW_CONFIG);
        clickIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent1 = PendingIntent.getBroadcast(mContext, 0, clickIntent, 0);
        mView.setOnClickPendingIntent(R.id.noconn, pendingIntent1);

        Intent newVersionStore = new Intent();
        newVersionStore.setAction(NEW_VERSION_STORE);
        newVersionStore.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent2 = PendingIntent.getBroadcast(mContext, 0, newVersionStore, 0);
        mView.setOnClickPendingIntent(R.id.new_version_store_button, pendingIntent2);

        Intent newVersionShow = new Intent();
        newVersionShow.setAction(NEW_VERSION_SHOW);
        newVersionShow.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent3 = PendingIntent.getBroadcast(mContext, 0, newVersionShow, 0);
        mView.setOnClickPendingIntent(R.id.new_version_show_button, pendingIntent3);

        Intent newVersionRemember = new Intent();
        newVersionRemember.setAction(NEW_VERSION_REMEMBER);
        newVersionRemember.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent4 = PendingIntent.getBroadcast(mContext, 0, newVersionRemember, 0);
        mView.setOnClickPendingIntent(R.id.new_version_remember_button, pendingIntent4);
    }

    public static void sendCommand(Intent intent) {

        String type = intent.getExtras().getString(WidgetProvider.TYPE);
        String cmd = intent.getExtras().getString(WidgetProvider.COMMAND);

        int actCol = 0;
        int position = -1;
        if (type != null) {
            switch (type) {
                case "switch":
                    position = Integer.parseInt(intent.getExtras().getString(WidgetProvider.POS));
                    actCol = Integer.parseInt(intent.getExtras().getString(WidgetProvider.COL));
                    break;
                case "lightscene":
                    position = Integer.parseInt(intent.getExtras().getString(WidgetProvider.POS));
                    break;
                case "command":
                    position = Integer.parseInt(intent.getExtras().getString(WidgetProvider.POS));
                    actCol = Integer.parseInt(intent.getExtras().getString(WidgetProvider.COL));
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
                    handler.postDelayed(deactLightscene, 500);
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

    public static Runnable deactLightscene = new Runnable() {
        @Override
        public void run() {
            for (Item item : configData.lightScenes.items) {
                item.activ = false;
            }

            for (int id : allWidgetIds) {
                appWidgetManager.notifyAppWidgetViewDataChanged(id, myLayout.layout.get("lightscene").get(0));
            }
        }
    };

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
        Log.d(TAG, "initListviews started");
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
        //Log.d(TAG, "initListview started with " + type + " " + Integer.toString(actCol));
        mView.setPendingIntentTemplate(listviewId, onClickPendingIntent);
        Intent switchIntent = new Intent(mContext, listViewServices.get(type).get(actCol));
        switchIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
        switchIntent.setData(Uri.parse(switchIntent.toUri(Intent.URI_INTENT_SCHEME)));

        mView.setRemoteAdapter(listviewId, switchIntent);
        mView.setViewVisibility(listviewId, View.VISIBLE);
    }

    public Runnable checkSocketTimer = new Runnable() {
        @Override
        public void run() {
            checkSocket();
            Integer wait;
            waitCount++;
            if (mySocket != null && mySocket.socket.connected()) {
                wait = getResources().getInteger(R.integer.waitIntervalLong);
            } else {
                if (waitCount > getResources().getInteger(R.integer.waitCyclesShort)) {
                    wait = getResources().getInteger(R.integer.waitIntervalLong);
                } else {
                    wait = getResources().getInteger(R.integer.waitIntervalShort);
                }
            }

            handler.removeCallbacks(this);
            handler.postDelayed(this, wait);
        }
    };

    public void checkSocket() {
        //Boolean callInitSocket = false;
        //Log.d(TAG, "checkSocket started");
        //Log.d("trace screen is on", String.valueOf(isScreenOn()));
        if (mySocket == null || !mySocket.socket.connected()) {
            if (isScreenOn()) {
                initSocket();
                valuesRequested = false;
            }
        } else if (!isScreenOn() && mySocket.socket.connected()) {
            //Log.d("trace", "disconnect socket");
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
                setVisibilityListViews(View.GONE);
                break;
            case VERSION_FHEMJS:
                mView.setTextViewText(R.id.new_version_text, text);
                mView.setViewVisibility(R.id.new_version_store_button, View.GONE);
                mView.setViewVisibility(R.id.new_version, View.VISIBLE);
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
        Log.d(TAG, "requestValues started by " + from);

        mySocket.requestValues(configData.getSwitchesList(), "once");
        mySocket.requestValues(configData.getValuesList(), "once");
        valuesRequested = true;

        mySocket.requestValues(configData.getSwitchesList(), "onChange");
        mySocket.requestValues(configData.getValuesList(), "onChange");
    }

    private void initSocket() {
        Log.d(TAG, "initSocket started");

        mySocket = new MySocket(websocketUrl, mContext, "Widget");
        mySocket.socket.off(Socket.EVENT_CONNECT);
        mySocket.socket.on(Socket.EVENT_CONNECT, args -> {
            String pw = configDataOnly.connectionPW;
            if (!pw.equals("")) {
                mySocket.socket.emit("authentication", pw);
            }
            //Log.i(TAG, "socket connected");

            try {
                requestValues("initSocket");
                setVisibility(SOCKET_CONNECTED, "");
            } catch (NullPointerException e) {
                //ignore this exception
            }
        });
        mySocket.socket.off(Socket.EVENT_DISCONNECT);
        mySocket.socket.on(Socket.EVENT_DISCONNECT, args -> {
            try {
                setVisibility(SOCKET_DISCONNECTED, getString(R.string.noconn));
            } catch (NullPointerException e) {
                //ignore this exception
            }
        });
        mySocket.socket.off(Socket.EVENT_RECONNECT_FAILED);
        mySocket.socket.on(Socket.EVENT_RECONNECT_FAILED, args -> {
            //Log.i("socket", "reconnect failed");
            try {
                setVisibility(SOCKET_DISCONNECTED, getString(R.string.noconn));
            } catch (NullPointerException e) {
                //ignore this exception
            }
        });
        mySocket.socket.off(Socket.EVENT_CONNECT_ERROR);
        mySocket.socket.on(Socket.EVENT_CONNECT_ERROR, args -> {
            //Log.i("socket", "connect error");
            try {
                mySocket.socket.close();
                mySocket.socket.off();
                mySocket = null;
                setVisibility(SOCKET_DISCONNECTED, getString(R.string.noconn));
            } catch (NullPointerException e) {
                //ignore this exception
            }
        });

        mySocket.socket.off("value");
        mySocket.socket.on("value", args -> {
            //Log.i("get value", args[0].toString());
            JSONObject obj = (JSONObject) args[0];
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
            }
        });

        mySocket.socket.off("version");
        mySocket.socket.on("version", args -> {
            Log.d("version", args[0].toString());
            JSONObject obj = (JSONObject) args[0];
            try {
                versionLatest = obj.getString("latest");
                versionType = obj.getString("type");

                VersionCheck versionCheck = versionChecks.get(versionType);
                versionCheck.setLatest(obj.getString("latest"));
                versionCheck.setInstalled(obj.getString("installed"));
                versionChecks.put(versionType, versionCheck);
            } catch (JSONException e) {
                e.printStackTrace();
            }

        });

        mySocket.socket.off("fhemError");
        mySocket.socket.on("fhemError", args -> setVisibility(SOCKET_DISCONNECTED, getString(R.string.noconn)));

        mySocket.socket.off("fhemConn");
        mySocket.socket.on("fhemConn", args -> setVisibility(SOCKET_CONNECTED, ""));
    }

    Runnable checkVersionTimer = new Runnable() {
        @Override
        public void run() {
            try {
                checkVersion(); //this function can change value of mInterval.
            } finally {
                handler.postDelayed(checkVersionTimer, getResources().getInteger(R.integer.waitIntervalVersionCheck));
            }
        }
    };

    Runnable checkShowVersionTimer = new Runnable() {
        @Override
        public void run() {
            try {
                checkShowVersion();
            } finally {
                handler.postDelayed(checkShowVersionTimer, getResources().getInteger(R.integer.waitIntervalVersionShowCheck));
            }
        }
    };

    private void checkVersion() {
        Log.d(TAG, "checkVersion started");

        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        boolean isWiFi = networkInfo != null && networkInfo.getType() == ConnectivityManager.TYPE_WIFI;
        if (isWiFi) {
            new GetStoreVersion(mContext, STORE_VERSION_WIDGET).execute();
        }
    }

    private void checkShowVersion() {
        Log.d(TAG, "checkShowVersion started");
        if (!isScreenOn()) return;
        for (VersionCheck versionCheck : versionChecks.values()) {
            Log.d(TAG, versionCheck.toString());

            if (versionCheck.showVersionHint()) {
                String hint = "";
                switch (versionCheck.type) {
                    case VERSION_APP:
                        hint = getString(R.string.newVersionApp, versionCheck.installed, versionCheck.latest);
                        break;
                    case VERSION_FHEMJS:
                        hint = getString(R.string.newVersionFhemjs, versionCheck.installed, versionCheck.latest);
                        break;
                    case VERSION_FHEMPL:
                        hint = getString(R.string.newVersionFhemjs, versionCheck.installed, versionCheck.latest);
                        break;
                }
                newVersionType = versionCheck.type;
                setVisibility(versionCheck.type, hint);
                Log.d(TAG, "show version hint " + versionCheck.type);
                break;
            }
        }
    }

    private String stopVersionRemember() {
        String latestSuppress = "";
        switch (newVersionType) {
            case VERSION_FHEMJS:
                latestSuppress = configDataOnly.suppressNewNodeVersion = versionLatest;
                break;
            case VERSION_FHEMPL:
                latestSuppress = configDataOnly.suppressNewFhemVersion = versionLatest;
                break;
            case VERSION_APP:
                latestSuppress = configDataOnly.suppressNewAppVersion = storeVersion;
                break;
        }
        configDataOnlyIO.save(configDataOnly);
        return latestSuppress;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        return null;
    }

}
