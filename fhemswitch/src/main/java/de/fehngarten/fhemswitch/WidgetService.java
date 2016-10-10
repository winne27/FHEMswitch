package de.fehngarten.fhemswitch;

import java.io.FileInputStream;
//import java.io.FileNotFoundException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
//import java.util.Set;

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
import android.view.View;
import android.view.Display;
import android.hardware.display.DisplayManager;
import android.widget.RemoteViews;

//import io.socket.emitter.Emitter;
import io.socket.client.Socket;

import de.fehngarten.fhemswitch.MyLightScenes.Item;
import de.fehngarten.fhemswitch.MyLightScenes.MyLightScene;

import android.util.Log;

public class WidgetService extends Service {
    public static final String CONFIGFILE = "config.data";

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
    private Context context;

    public static ConfigData configData;
    private ConfigDataOnly configDataOnly;

    public static PowerManager pm;
    private int layoutId;

    private PendingIntent onClickPendingIntent;
    public static MyLayout myLayout;
    public static HashMap<String, ArrayList<Class<?>>> listViewServices;
    public Boolean valuesRequested = false;
    public static Boolean serviceRunning = false;
    private int waitCount = 0;
    //private Boolean ListviewsInitialized = false;

    public void onCreate() {
        Log.d("WidgetService", "onCreate fired");
        super.onCreate();

        serviceRunning = true;
        context = getApplicationContext();
        appWidgetManager = AppWidgetManager.getInstance(context);
        pm = (PowerManager) getSystemService(Context.POWER_SERVICE);

        icons.put("on", R.drawable.on);
        icons.put("set_on", R.drawable.set_on);
        icons.put("off", R.drawable.off);
        icons.put("set_off", R.drawable.set_off);
        icons.put("set_toggle", R.drawable.set_toggle);
        icons.put("undefined", R.drawable.undefined);
        icons.put("toggle", R.drawable.undefined);

        // Intent conn changed
        BroadcastReceiver connChangeReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d("WidgetService", "connchanged " + intent.getAction());
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

        // Intent new config
        BroadcastReceiver newConfigReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d("WidgetService", "configchange fired");
                readConfig();
                doStart(5);
            }
        };

        IntentFilter newConfigFilter = new IntentFilter(WidgetProvider.NEW_CONFIG);
        registerReceiver(newConfigReceiver, newConfigFilter);

        // intent configuration changed (orientation)
        BroadcastReceiver orientReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d("WidgetService", "orientchange fired");
                //readConfig();
                doStart(4);
                initListviews();
            }
        };

        IntentFilter orientFilter = new IntentFilter(Intent.ACTION_CONFIGURATION_CHANGED);
        registerReceiver(orientReceiver, orientFilter);

        // intent screen on/off
        BroadcastReceiver screenReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                //do something you need when broadcast received
                Log.d("WidgetService", "screenaction " + intent.getAction());

                if (intent.getAction().equals("android.intent.action.SCREEN_ON")) {
                    waitCount = 0;
                }

                handler.postDelayed(checkSocketTimer, 1000);
            }
        };

        IntentFilter screenFilter = new IntentFilter();
        screenFilter.addAction(Intent.ACTION_SCREEN_ON);
        screenFilter.addAction(Intent.ACTION_SCREEN_OFF);
        registerReceiver(screenReceiver, screenFilter);

        readConfig();
        //doStart(1);
    }

    @Override
    public void onDestroy() {
        Log.d("WidgetService", "onDestroy fired");
        mySocket.socket.disconnect();
        mySocket.socket.close();
        mySocket = null;
        handler.removeCallbacks(checkSocketTimer);
        serviceRunning = false;
        super.onDestroy();
    }

    /*
        public void onStartCommand(Intent intent, int startId) {
            //Log.i("trace", "onStartCommand startet by " + intent.getAction());
            if (intent.getStringExtra("newConfig").equals("1")) {
                readConfig();
            }
            doStart(2);
        }
    */
    public void readConfig() {
        Log.d("WidgetService", "readConfig started");
        valuesRequested = false;
        try {
            FileInputStream f_in = openFileInput(WidgetService.CONFIGFILE);
            ObjectInputStream obj_in = new ObjectInputStream(f_in);

            Object obj = obj_in.readObject();
            obj_in.close();

            ////Log.i("config", "config.data found");

            if (obj instanceof ConfigDataOnly) {
                configDataOnly = (ConfigDataOnly) obj;
                websocketUrl = configDataOnly.urljs;
                fhemUrl = configDataOnly.urlpl;
                switchCols = configDataOnly.switchCols;
                valueCols = configDataOnly.valueCols;
                commandCols = configDataOnly.commandCols;
                layouts[Integer.valueOf(getString(R.string.LAYOUT_HORIZONTAL))] = R.layout.main_layout_horizontal;
                layouts[Integer.valueOf(getString(R.string.LAYOUT_VERTICAL))] = R.layout.main_layout_vertical;
                layouts[Integer.valueOf(getString(R.string.LAYOUT_MIXED))] = R.layout.main_layout_mixed;

                setOrientation();
            } else {
                throw new Exception("Config data corrupted");
            }
        } catch (Exception e) {
            e.printStackTrace();
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
        Log.d("WidgetService", "doStart started with " + Integer.toString(nr));

        allWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(context, WidgetProvider.class));

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
        handler.postDelayed(checkSocketTimer, 1000);
    }

    public static void sendCommand(String cmd, int position, String type, int actCol) {
        //Log.i("sendCommand", cmd);
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
                //Log.i("col + pos", Integer.toString(actCol) + " + " + Integer.toString(position));
                configData.commandsCols.get(actCol).get(position).activ = true;
                for (int id : allWidgetIds) {
                    appWidgetManager.notifyAppWidgetViewDataChanged(id, myLayout.layout.get("command").get(actCol));
                }
                Runnable deactCommand = new DeactCommand(actCol, position);
                handler.postDelayed(deactCommand, 500);
                break;
        }
    }

    public static Runnable deactLightscene = new Runnable() {
        @Override
        public void run() {
            //String methodname = "checkSocketTimer";
            ////Log.d(CLASSNAME + methodname, "started");
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
        Log.d("WidgetService", "initListviews started");
        final Intent onItemClick = new Intent(context, WidgetProvider.class);
        onItemClick.setData(Uri.parse(onItemClick.toUri(Intent.URI_INTENT_SCHEME)));
        onClickPendingIntent = PendingIntent.getBroadcast(context, 0, onItemClick, PendingIntent.FLAG_UPDATE_CURRENT);
        RemoteViews mView = new RemoteViews(context.getPackageName(), layoutId);

        mView.setViewVisibility(R.id.noconn, View.GONE);
        for (int viewId : myLayout.goneViews) {
            mView.setViewVisibility(viewId, View.GONE);
        }

        for (int widgetId : allWidgetIds) {

            for (Entry<String, ArrayList<Integer>> entry : myLayout.layout.entrySet()) {
                String type = entry.getKey();
                int actCol = 0;
                for (int listviewId : entry.getValue()) {
                    ////Log.i("initListview",type + " " + Integer.toString(actCol));
                    initListview(widgetId, mView, listviewId, actCol, type);
                    actCol++;
                }
            }

            Intent clickIntent = new Intent(this.getApplicationContext(), WidgetProvider.class);
            clickIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
            clickIntent.putExtra("newConfig", "1");
            PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, clickIntent, 0);
            // PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, clickIntent,
            //         PendingIntent.FLAG_UPDATE_CURRENT);
            mView.setOnClickPendingIntent(R.id.noconn, pendingIntent);

            appWidgetManager.updateAppWidget(widgetId, mView);

        }
    }

    private void initListview(int widgetId, RemoteViews mView, int listviewId, int actCol, String type) {
        Log.d("WidgetService", "initListview started with " + type + " " + Integer.toString(actCol));
        mView.setPendingIntentTemplate(listviewId, onClickPendingIntent);
        Intent switchIntent = new Intent(context, listViewServices.get(type).get(actCol));
        switchIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
        switchIntent.setData(Uri.parse(switchIntent.toUri(Intent.URI_INTENT_SCHEME)));

        mView.setRemoteAdapter(listviewId, switchIntent);
        mView.setViewVisibility(listviewId, View.VISIBLE);

        //appWidgetManager.notifyAppWidgetViewDataChanged(widgetId, listviewId);
    }
/*
    public Runnable initListviewsTimer = new Runnable() {
        @Override
        public void run() {
                initListviews();
        }
    };
*/

    public Runnable checkSocketTimer = new Runnable() {
        @Override
        public void run() {
            //String methodname = "checkSocketTimer";
            //Log.d("WidgetService", methodname + " started");
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
        Log.d("WidgetService", "checkSocket started");
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
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        return pm.isScreenOn();
    }

    @TargetApi(Build.VERSION_CODES.KITKAT_WATCH)
    public boolean isScreenOnNew() {
        DisplayManager dm = (DisplayManager) context.getSystemService(Context.DISPLAY_SERVICE);
        boolean screenOn = false;
        for (Display display : dm.getDisplays()) {
            if (display.getState() != Display.STATE_OFF) {
                screenOn = true;
            }
        }
        return screenOn;
    }

    private void setVisibility(String type) {
        //Log.i("type of setVisibility",type);
        Context context = getApplicationContext();
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        RemoteViews mView = new RemoteViews(context.getPackageName(), layoutId);
        if (type.equals("connected")) {
            mView.setViewVisibility(R.id.noconn, View.GONE);
            for (Entry<String, ArrayList<Integer>> entry : myLayout.layout.entrySet()) {
                for (int listviewId : entry.getValue()) {
                    mView.setViewVisibility(listviewId, View.VISIBLE);
                }
            }
        } else {
            mView.setTextViewText(R.id.noconn, getString(R.string.noconn));
            mView.setViewVisibility(R.id.noconn, View.VISIBLE);

            for (Entry<String, ArrayList<Integer>> entry : myLayout.layout.entrySet()) {
                for (int listviewId : entry.getValue()) {
                    mView.setViewVisibility(listviewId, View.GONE);
                }
            }
        }

        for (int widgetId : allWidgetIds) {
            appWidgetManager.updateAppWidget(widgetId, mView);
        }
    }

    private void requestValues(String from) {
        Log.d("WidgetService", "requestValues started by " + from);

        initListviews();
        //handler.postDelayed(initListviewsTimer, 2000);

        mySocket.requestValues(configData.getSwitchesList(), "once");
        mySocket.requestValues(configData.getValuesList(), "once");
        valuesRequested = true;

        mySocket.requestValues(configData.getSwitchesList(), "onChange");
        mySocket.requestValues(configData.getValuesList(), "onChange");
    }

    private void initSocket() {
        Log.d("WidgetService", "initSocket started");

        mySocket = new MySocket(websocketUrl, context);
        mySocket.socket.off(Socket.EVENT_CONNECT);
        mySocket.socket.on(Socket.EVENT_CONNECT, args -> {
            String pw = configDataOnly.connectionPW;
            if (!pw.equals("")) {
                mySocket.socket.emit("authentication", pw);
            }
            //Log.i("WidgetService", "socket connected");

            try {
                requestValues("initSocket");
                setVisibility("connected");
            } catch (NullPointerException e) {
                //ignore this exception
            }
        });
        mySocket.socket.off(Socket.EVENT_DISCONNECT);
        mySocket.socket.on(Socket.EVENT_DISCONNECT, args -> {
            ////Log.i("socket", "disconnected");
            try {
                setVisibility("disconnected");
            } catch (NullPointerException e) {
                //ignore this exception
            }
        });
        mySocket.socket.off(Socket.EVENT_RECONNECT_FAILED);
        mySocket.socket.on(Socket.EVENT_RECONNECT_FAILED, args -> {
            //Log.i("socket", "reconnect failed");
            try {
                setVisibility("disconnected");
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
                setVisibility("disconnected");
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

        mySocket.socket.off("fhemError");
        mySocket.socket.on("fhemError", args -> {
            ////Log.i("socket", "disconnected");
            setVisibility("fhemError");
        });

        mySocket.socket.off("fhemConn");
        mySocket.socket.on("fhemConn", args -> {
            ////Log.i("socket", "disconnected");
            setVisibility("connected");
        });

    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        return null;
    }

}
