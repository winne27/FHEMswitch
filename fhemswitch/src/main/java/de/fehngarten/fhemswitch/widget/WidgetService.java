package de.fehngarten.fhemswitch.widget;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

import java.util.Map.Entry;

import org.json.JSONObject;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.StrictMode;
import androidx.core.app.NotificationCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Display;
import android.hardware.display.DisplayManager;
import android.widget.RemoteViews;
import android.widget.Toast;

import de.fehngarten.fhemswitch.BuildConfig;
import de.fehngarten.fhemswitch.config.ConfigMain;
import de.fehngarten.fhemswitch.data.ConfigDataCommon;
import de.fehngarten.fhemswitch.data.ConfigDataIO;
import de.fehngarten.fhemswitch.data.ConfigDataInstance;
import de.fehngarten.fhemswitch.data.ConfigIntValueRow;
import de.fehngarten.fhemswitch.data.ConfigWorkBasket;
import de.fehngarten.fhemswitch.data.ConfigWorkInstance;
import de.fehngarten.fhemswitch.data.RowIntValue;
import de.fehngarten.fhemswitch.modul.MyRoundedCorners;
import de.fehngarten.fhemswitch.modul.MyBroadcastReceiver;
import de.fehngarten.fhemswitch.modul.MyReceiveListener;
import de.fehngarten.fhemswitch.modul.MySetOnClickPendingIntent;
import de.fehngarten.fhemswitch.modul.MyWifiInfo;
import de.fehngarten.fhemswitch.modul.VersionChecks;
import de.fehngarten.fhemswitch.modul.GetStoreVersion;
import de.fehngarten.fhemswitch.data.RowCommand;
import de.fehngarten.fhemswitch.modul.MyLayout;
import de.fehngarten.fhemswitch.modul.MySocket;
import de.fehngarten.fhemswitch.data.RowSwitch;
import de.fehngarten.fhemswitch.data.RowValue;
import de.fehngarten.fhemswitch.R;
import de.fehngarten.fhemswitch.widget.listviews.CommonListviewService;
import de.fehngarten.fhemswitch.data.ConfigCommandRow;
import de.fehngarten.fhemswitch.data.ConfigLightsceneRow;
import de.fehngarten.fhemswitch.data.ConfigSwitchRow;
import de.fehngarten.fhemswitch.data.ConfigValueRow;
import io.socket.client.Socket;
import de.fehngarten.fhemswitch.data.RowLightScenes.MyLightScene;

import static de.fehngarten.fhemswitch.global.Consts.*;
import static de.fehngarten.fhemswitch.global.Settings.*;

public class WidgetService extends Service {
    private VersionChecks versionChecks;
    private MySocket mySocket = null;

    private int layoutId;
    private int iLayout;
    private MyLayout myLayout;
    private Map<String, Integer> blockCols = new HashMap<>();

    private AppWidgetManager appWidgetManager;
    private Handler handler;
    private ArrayList<MyBroadcastReceiver> myBroadcastReceivers;
    private ArrayList<DoSendCommand> doSendCommands = new ArrayList<>();
    private Context mContext;
    private RemoteViews mView;
    private MyWifiInfo myWifiInfo;

    private ConfigDataCommon configDataCommon;
    private ConfigDataInstance configDataInstance;

    public Boolean valuesRequested = false;
    private String currentVersionType;
    private int widgetId;
    private boolean listenConnChange = false;

    protected Integer instSerial;
    private boolean screenIsOn = true;
    private boolean keepDisconnected = false;
    private int waitCheckSocket = settingWaitSocketShort;

    public WidgetService() {
        versionChecks = new VersionChecks();
        myBroadcastReceivers = new ArrayList<>();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {

            String action = intent.getAction();

            widgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1);
            switch (action != null ? action : "default") {
                case FHEM_COMMAND:
                    if (configDataCommon == null || configDataCommon.urlFhemjsLocal == null) {
                        start();
                    }

                    Bundle extras = intent.getExtras();

                    if (configDataInstance.confirmCommands && !extras.getBoolean("isConfirmed", false)) {
                        ItemHolder itemHolder = getItemName(intent);
                        if (itemHolder.name != "") {
                            Bundle bundle = new Bundle();
                            bundle.putString("name", itemHolder.name);
                            bundle.putString("icon", itemHolder.icon);
                            bundle.putInt(INSTSERIAL, instSerial);
                            bundle.putBundle("cmdBundle", intent.getExtras());

                            Intent confirm = new Intent(mContext, DialogActivity.class);
                            confirm.putExtras(bundle);
                            mContext.startActivity(confirm);
                        } else {
                            sendCommand(intent);
                        }
                    } else {
                        sendCommand(intent);
                    }
                    break;
                case SEND_DO_COLOR:
                    doColoring(intent.getBooleanExtra("COLOR", false));
                    break;
                case NEW_CONFIG:
                    start();
                    break;
                default:
                    startInForeground();
                    start();
                    doColoring(false);
                    setBroadcastReceivers();
                    checkVersion();
                    handler.postDelayed(setBroadcastReceiversTimer, settingDelayDefineBroadcastReceivers);
                    handler.postDelayed(checkVersionTimer, settingIntervalVersionCheck);
                    handler.postDelayed(checkShowVersionTimer, settingDelayShowVersionCheck);
                    break;
            }
        }
        super.onStartCommand(intent, flags, startId);
        return START_REDELIVER_INTENT;
    }

    private void startInForeground() {
        if (Build.VERSION_CODES.O <= Build.VERSION.SDK_INT) {

            Intent notificationIntent = new Intent(this, ConfigMain.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, PRIMARY_CHANNEL_ID)
                    .setSmallIcon(R.drawable.set_on)
                    .setContentTitle(CHANNEL_NAME)
                    .setContentText(NOWAYNOTIFICATION)
//                    .setTicker("TICKER")
                    .setContentIntent(pendingIntent);
            Notification notification = builder.build();

            NotificationChannel channel = new NotificationChannel(PRIMARY_CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription(CHANNEL_DESC);
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(channel);
            startForeground(1, notification);
        }
    }

    public void onCreate() {
        //TAG = "WidgetService-" + instSerial;
        mContext = getApplicationContext();
        appWidgetManager = AppWidgetManager.getInstance(mContext);
        handler = new Handler();
        myBroadcastReceivers = new ArrayList<>();
        myWifiInfo = new MyWifiInfo(mContext);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        super.onCreate();
    }

    private void start() {
        screenIsOn = isScreenOn();
        setVisibility("start", SOCKET_DISCONNECTED, getString(R.string.noconn));
        if (mySocket != null) {
            mySocket.destroy();
        }
        readConfig();
        setOrientation();
        doStart();
    }

    @Override
    public void onDestroy() {
        if (mySocket != null) {
            mySocket.destroy();
        }

        handler.removeCallbacks(checkSocketTimer);
        handler.removeCallbacks(checkVersionTimer);
        handler.removeCallbacks(checkShowVersionTimer);
        //handler.removeCallbacks(setBroadcastReceiversTimer);

        for (DoSendCommand doSendCommand : doSendCommands) {
            doSendCommand.kill();
        }

        unregisterBroadcastReceivers();

        super.onDestroy();
    }

    private void doColoring(boolean doit) {
        mView = new RemoteViews(mContext.getPackageName(), layoutId);
        int shape = doit ? settingShapes[instSerial] : R.drawable.widget_shape;
        mView.setInt(R.id.main_layout, "setBackgroundResource", shape);
        appWidgetManager.updateAppWidget(widgetId, mView);

        if (doit) {
            if (mySocket != null) {
                mySocket.destroy();
            }
            keepDisconnected = true;
        } else {
            keepDisconnected = false;
            checkSocket();
        }
    }

    private void unregisterBroadcastReceivers() {
        for (MyBroadcastReceiver myBroadcastReceiver : myBroadcastReceivers) {
            try {
                myBroadcastReceiver.unregister();
            } catch (java.lang.IllegalArgumentException e) {
                //if (BuildConfig.DEBUG) Log.d(TAG, "unregister failed");
            }
        }
    }

    private void setBroadcastReceivers() {
        //Log.d(TAG, "setBroadcastReceivers started");
        unregisterBroadcastReceivers();

        String[] actions = new String[]{STORE_VERSION_WIDGET};
        myBroadcastReceivers.add(new MyBroadcastReceiver(this, new OnStoreVersion(), actions));

        actions = new String[]{NEW_VERSION_STORE, NEW_VERSION_SUPPRESS, NEW_VERSION_REMEMBER};
        myBroadcastReceivers.add(new MyBroadcastReceiver(this, new OnUserIntent(), actions));
    }

    Runnable setBroadcastReceiversTimer = new Runnable() {
        @Override
        public void run() {
            listenConnChange = false;

            String[] actions = new String[]{Intent.ACTION_CONFIGURATION_CHANGED, NEW_CONFIG};
            myBroadcastReceivers.add(new MyBroadcastReceiver(mContext, new OnConfigChange(), actions));

            actions = new String[]{Intent.ACTION_SCREEN_ON, Intent.ACTION_SCREEN_OFF};
            myBroadcastReceivers.add(new MyBroadcastReceiver(mContext, new OnScreenOnOff(), actions));

            actions = new String[]{ConnectivityManager.CONNECTIVITY_ACTION};
            myBroadcastReceivers.add(new MyBroadcastReceiver(mContext, new OnConnectionChange(), actions));
/*
            ConnectivityManager connectivityManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
            connectivityManager.registerNetworkCallback(networkRequest , this);
            actions = new String[]{connectivityManager.NetworkCallback};
            myBroadcastReceivers.add(new MyBroadcastReceiver(mContext, new OnConnectionChange(), actions));
 */
        }
    };

    private class OnScreenOnOff implements MyReceiveListener {
        public void run(Context context, Intent intent) {
            //Log.d(TAG,"on/off fired");
            try {
                String action = intent.getAction();
                screenIsOn = action != null && action.equals(Intent.ACTION_SCREEN_ON);
                if (screenIsOn) {
                    start();
                }
            } catch (Exception e) {
                // do nothing
            }
        }
    }

    private class OnConfigChange implements MyReceiveListener {
        public void run(Context context, Intent intent) {
            //Log.d(TAG, "config change fired");
            start();
        }
    }

    private class OnStoreVersion implements MyReceiveListener {
        public void run(Context context, Intent intent) {
            Bundle extras = intent.getExtras();
            String type = extras != null ? extras.getString(GetStoreVersion.LATEST, null) : null;
            if (type != null) {
                versionChecks.setVersions(VERSION_APP, BuildConfig.VERSION_NAME, type);
            }
        }
    }

    private class OnUserIntent implements MyReceiveListener {
        public void run(Context context, Intent intent) {
            String action = intent.getAction();
            switch (action != null ? action : "") {
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
                    setVisibility("", VERSION_CLOSE, null);
                    versionChecks.setDateShown(currentVersionType);
                    break;
                case NEW_VERSION_SUPPRESS:
                    setVisibility("", VERSION_CLOSE, null);
                    versionChecks.setSuppressedToLatest(currentVersionType);
                    saveSuppressedVersions(currentVersionType);
                    break;
                case NEW_VERSION_REMEMBER:
                    setVisibility("", VERSION_CLOSE, null);
                    versionChecks.setDateShown(currentVersionType);
                    break;
            }
        }
    }

    private class OnConnectionChange implements MyReceiveListener {
        public void run(Context context, Intent intent) {
            //Log.d(TAG,"connection change fired");

            if (listenConnChange) {
                ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo = null;
                if (connectivityManager != null) {
                    networkInfo = connectivityManager.getActiveNetworkInfo();
                }
                if (networkInfo != null && networkInfo.isConnected() && screenIsOn) {
                    start();
                }
            } else {
                listenConnChange = true;
            }
        }
    }

    public void sendCommand(Intent intent) {

        if (keepDisconnected) {
            Toast toast = Toast.makeText(mContext, getString(R.string.noCommandsAllowed), Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
            toast.show();
            return;
        }

        if (mySocket == null) {
            checkSocket();
            return;
        }

        Bundle extras = intent.getExtras();
        String type = extras != null ? extras.getString(FHEM_TYPE, "") : "";

        try {
            if (type.equals("intvalue")) {
                setNewValue(intent);
            } else if (!type.equals("")) {
                String cmd = intent.getExtras().getString(FHEM_COMMAND);
                int actCol = 0;
                int position = -1;
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
                        ConfigWorkBasket.data.get(instSerial).switchesCols.get(actCol).get(position).setIcon("set_toggle");
                        appWidgetManager.notifyAppWidgetViewDataChanged(widgetId, myLayout.layout.get("switch").get(actCol));
                        break;
                    case "lightscene":
                        ConfigWorkBasket.data.get(instSerial).lightScenes.items.get(position).activ = true;
                        appWidgetManager.notifyAppWidgetViewDataChanged(widgetId, myLayout.layout.get("lightscene").get(0));
                        break;
                    case "command":
                        ConfigWorkBasket.data.get(instSerial).commandsCols.get(actCol).get(position).activ = true;
                        appWidgetManager.notifyAppWidgetViewDataChanged(widgetId, myLayout.layout.get("command").get(actCol));
                        Runnable deactCommand = new DeactCommand(myLayout.layout.get("command").get(actCol), position, widgetId, instSerial, appWidgetManager);
                        handler.postDelayed(deactCommand, 500);
                        break;
                }
            }
        } catch (Exception e) {
            start();
        }
    }

    public ItemHolder getItemName(Intent intent) {

        Bundle extras = intent.getExtras();
        String type = extras != null ? extras.getString(FHEM_TYPE, "") : "";

        String name = "";
        String icon = "";
        if (!type.equals("")) {
            String cmd = intent.getExtras().getString(FHEM_COMMAND);
            int actCol = 0;
            int position = -1;
            switch (type) {
                case "switch":
                    position = Integer.parseInt(intent.getExtras().getString(POS));
                    actCol = Integer.parseInt(intent.getExtras().getString(COL));
                    name = ConfigWorkBasket.data.get(instSerial).switchesCols.get(actCol).get(position).name;
                    ConfigWorkInstance curInstance = ConfigWorkBasket.data.get(instSerial);
                    icon = curInstance.switchesCols.get(actCol).get(position).icon;
                    break;
                case "lightscene":
                    position = Integer.parseInt(intent.getExtras().getString(POS));
                    name = ConfigWorkBasket.data.get(instSerial).lightScenes.items.get(position).name;
                    icon = "lightscene";
                    break;
                case "command":
                    position = Integer.parseInt(intent.getExtras().getString(POS));
                    actCol = Integer.parseInt(intent.getExtras().getString(COL));
                    name = ConfigWorkBasket.data.get(instSerial).commandsCols.get(actCol).get(position).name;
                    icon = "command";
                    break;
            }
        }
        ItemHolder itemHolder = new ItemHolder();
        itemHolder.name = name;
        itemHolder.icon = icon;
        return itemHolder;
    }

    private class ItemHolder {
        String name;
        String icon;
    }

    private void setNewValue(Intent intent) {
        //Log.i(TAG,intent.getStringExtra(SUBACTION));
        int pos = intent.getIntExtra(POS, -1);
        RowIntValue rowIntValue = ConfigWorkBasket.data.get(instSerial).intValues.get(pos);
        String newValueString;
        if (rowIntValue.isTime) {
            newValueString = calcNewTime(rowIntValue.value, intent.getStringExtra(SUBACTION));
        } else {
            Float delta = rowIntValue.stepSize * settingMultiplier.get(intent.getStringExtra(SUBACTION));
            Float newValue = Float.valueOf(rowIntValue.value) + delta;
            newValueString = newValue.toString();
        }

        if (rowIntValue.setCommand.equals("")) {
            rowIntValue.setCommand = rowIntValue.unit;
        }

        String cmd = "set " + rowIntValue.setCommand + " " + newValueString;

        ConfigWorkBasket.data.get(instSerial).intValues.get(pos).setValue(newValueString);
        appWidgetManager.notifyAppWidgetViewDataChanged(widgetId, myLayout.layout.get("intvalue").get(0));
        doSendCommands.get(pos).fire(cmd, rowIntValue.commandExecDelay);
    }

    private String calcNewTime(String value, String subaction) {
        String hour = value.substring(0, 2);
        String min = value.substring(3, 5);
        int minInt = Integer.parseInt(min);
        int hourInt = Integer.parseInt(hour);
        switch (subaction) {
            case DOWN:
                minInt = minInt - 1;
                if (minInt == 0) {
                    minInt = 59;
                }
                break;
            case UP:
                minInt = minInt + 1;
                if (minInt == 60) {
                    minInt = 0;
                }
                break;
            case DOWNFAST:
                hourInt = hourInt - 1;
                if (hourInt == -1) {
                    hourInt = 23;
                }
                break;
            case UPFAST:
                hourInt = hourInt + 1;
                if (hourInt == 24) {
                    hourInt = 0;
                }
                break;
        }
        hour = String.format(Locale.getDefault(), "%02d", hourInt);
        min = String.format(Locale.getDefault(), "%02d", minInt);
        return hour + ":" + min;
    }

    private class DoSendCommand implements Runnable {
        String cmd;
        Handler handler;

        DoSendCommand(Handler handler) {
            this.handler = handler;
        }

        void fire(String cmd, int delay) {
            this.cmd = cmd;
            kill();
            handler.postDelayed(this, delay);
        }

        void kill() {
            handler.removeCallbacks(this);
        }

        public void run() {
            //Log.d(TAG, "cmd: " + cmd);
            if (cmd != null && mySocket != null) {
                mySocket.sendCommand(cmd);
            }
        }
    }

    // after touch command button is for 500ms pride
    private static class DeactCommand implements Runnable {
        int actPos;
        int widgetId;
        int viewId;
        int instSerial;
        AppWidgetManager appWidgetManager;

        DeactCommand(int viewId, int actPos, int widgetId, int instSerial, AppWidgetManager appWidgetManager) {
            this.viewId = viewId;
            this.actPos = actPos;
            this.widgetId = widgetId;
            this.instSerial = instSerial;
            this.appWidgetManager = appWidgetManager;
        }

        public void run() {
            ConfigWorkBasket.data.get(instSerial).commands.get(actPos).activ = false;
            appWidgetManager.notifyAppWidgetViewDataChanged(widgetId, viewId);
        }
    }

    public void readConfig() {
        //if (BuildConfig.DEBUG) //Log.d(TAG, "readConfig started");
        valuesRequested = false;

        ConfigDataIO configDataIO = new ConfigDataIO(mContext);

        configDataCommon = configDataIO.readCommon();
        ConfigWorkBasket.fhemjsPW = configDataCommon.fhemjsPW;

        if (configDataCommon.suppressedVersions != null) {
            for (Map.Entry<String, String> entry : configDataCommon.suppressedVersions.entrySet()) {
                String type = entry.getKey();
                String suppressedVersion = entry.getValue();
                versionChecks.setSuppressedVersion(type, suppressedVersion);
            }
        }
        //Log.d("Instanz lesen widget", Integer.toString(instSerial));
        configDataInstance = configDataIO.readInstance(instSerial, false);
    }

    public void setOrientation() {
        Configuration config = getResources().getConfiguration();

        if (config.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            iLayout = configDataInstance.layoutLandscape;
        } else {
            iLayout = configDataInstance.layoutPortrait;
        }

        blockCols = new HashMap<>();

        blockCols.put(LIGHTSCENES, 0);
        blockCols.put(INTVALUES, 0);

        if (iLayout == LAYOUT_HORIZONTAL) {
            blockCols.put(SWITCHES, configDataInstance.switchCols);
            blockCols.put(VALUES, configDataInstance.valueCols);
            blockCols.put(COMMANDS, configDataInstance.commandCols);
        } else {
            blockCols.put(SWITCHES, 0);
            blockCols.put(VALUES, 0);
            blockCols.put(COMMANDS, 0);
        }
    }

    public void doStart() {
        Map<String, Integer> blockCounts = new HashMap<>();
        try {
            ConfigWorkBasket.data.get(instSerial).init();
            if (myLayout != null) {
                for (Entry<String, ArrayList<Integer>> entry : myLayout.layout.entrySet()) {
                    for (int listviewId : entry.getValue()) {
                        appWidgetManager.notifyAppWidgetViewDataChanged(widgetId, listviewId);
                    }
                }
            }

            if (configDataInstance.blockOrder == null) {
                ConfigWorkBasket.data.get(instSerial).blockOrder = settingsBlockOrder;
            } else {
                ConfigWorkBasket.data.get(instSerial).blockOrder = configDataInstance.blockOrder;
            }

            //-- control switches  ------------------------------------------------------------------
            if (configDataInstance.switchRows != null) {
                for (ConfigSwitchRow switchRow : configDataInstance.switchRows) {
                    if (switchRow.enabled) {
                        ConfigWorkBasket.data.get(instSerial).switches.add(new RowSwitch(switchRow.name, switchRow.unit, switchRow.cmd, switchRow.confirm));
                    }
                }
            }
            blockCounts.put(SWITCHES, ConfigWorkBasket.data.get(instSerial).switches.size());

            //-- control lightscenes  ------------------------------------------------------------------
            MyLightScene newLightScene = null;
            if (configDataInstance.lightsceneRows != null) {
                for (ConfigLightsceneRow lightsceneRow : configDataInstance.lightsceneRows) {
                    if (lightsceneRow.isHeader) {
                        newLightScene = ConfigWorkBasket.data.get(instSerial).lightScenes.newLightScene(lightsceneRow.name, lightsceneRow.unit, lightsceneRow.showHeader);
                    } else {
                        if (newLightScene != null) {
                            newLightScene.addMember(lightsceneRow.name, lightsceneRow.unit, lightsceneRow.enabled);
                        }
                    }
                }
            }
            blockCounts.put(LIGHTSCENES, ConfigWorkBasket.data.get(instSerial).lightScenes.itemsCount);

            //-- control values  ------------------------------------------------------------------
            if (configDataInstance.valueRows != null) {
                for (ConfigValueRow valueRow : configDataInstance.valueRows) {
                    if (valueRow.enabled) {
                        ConfigWorkBasket.data.get(instSerial).values.add(new RowValue(valueRow.name, valueRow.unit, valueRow.useIcon));
                    }
                }
            }
            blockCounts.put(VALUES, ConfigWorkBasket.data.get(instSerial).values.size());

            //-- control intvalues  ------------------------------------------------------------------
            if (configDataInstance.intValueRows != null) {
                doSendCommands = new ArrayList<>();
                for (ConfigIntValueRow configIntValueRow : configDataInstance.intValueRows) {
                    if (configIntValueRow.enabled) {
                        RowIntValue rowIntValue = new RowIntValue();
                        rowIntValue.transfer(configIntValueRow);
                        ConfigWorkBasket.data.get(instSerial).intValues.add(rowIntValue);
                        doSendCommands.add(new DoSendCommand(handler));
                    }
                }
            }
            blockCounts.put(INTVALUES, ConfigWorkBasket.data.get(instSerial).intValues.size());

            //-- control commands  ------------------------------------------------------------------
            if (configDataInstance.commandRows != null) {
                for (ConfigCommandRow commandRow : configDataInstance.commandRows) {
                    if (commandRow.enabled) {
                        ConfigWorkBasket.data.get(instSerial).commands.add(new RowCommand(commandRow.name, commandRow.command));
                    }
                }
            }
            blockCounts.put(COMMANDS, ConfigWorkBasket.data.get(instSerial).commands.size());

            int sumCounts = 0;
            Integer numBlocks = 0;
            for (Map.Entry<String, Integer> entry : blockCounts.entrySet()) {
                sumCounts += entry.getValue();
                if (entry.getValue() > 0) {
                    numBlocks++;
                }
            }

            if (sumCounts == 0) {
                throw new NoFuckingEntries("sum of block entries is null");
            }

            if (iLayout == LAYOUT_MIXED && numBlocks <= 1) {
                iLayout = LAYOUT_HORIZONTAL;
            }
            layoutId = settingLayouts[iLayout];
            mView = new RemoteViews(mContext.getPackageName(), layoutId);
            myLayout = new MyLayout(iLayout, blockCols, blockCounts, ConfigWorkBasket.data.get(instSerial).blockOrder);

            //-- control switches  ------------------------------------------------------------------
            for (int i = 0; i <= blockCols.get(SWITCHES); i++) {
                ConfigWorkBasket.data.get(instSerial).switchesCols.add(new ArrayList<>());
            }

            int rownum = 0;
            int colnum = 0;

            for (RowSwitch switchRow : ConfigWorkBasket.data.get(instSerial).switches) {
                rownum = rownum + 1;
                ConfigWorkBasket.data.get(instSerial).switchesCols.get(colnum).add(switchRow);
                if (rownum % myLayout.rowsPerCol.get(SWITCHES) == 0) {
                    colnum++;
                }
            }

            //-- control values  ------------------------------------------------------------------
            for (int i = 0; i <= blockCols.get(VALUES); i++) {
                ConfigWorkBasket.data.get(instSerial).valuesCols.add(new ArrayList<>());
            }
            rownum = 0;
            colnum = 0;
            for (RowValue valueRow : ConfigWorkBasket.data.get(instSerial).values) {
                rownum = rownum + 1;
                ConfigWorkBasket.data.get(instSerial).valuesCols.get(colnum).add(valueRow);
                if (rownum % myLayout.rowsPerCol.get(VALUES) == 0) {
                    colnum++;
                }
            }

            //-- control commands  ------------------------------------------------------------------
            for (int i = 0; i <= blockCols.get(COMMANDS); i++) {
                ConfigWorkBasket.data.get(instSerial).commandsCols.add(new ArrayList<>());
            }
            rownum = 0;
            colnum = 0;
            for (RowCommand commandRow : ConfigWorkBasket.data.get(instSerial).commands) {
                rownum = rownum + 1;
                ConfigWorkBasket.data.get(instSerial).commandsCols.get(colnum).add(commandRow);
                if (rownum % myLayout.rowsPerCol.get(COMMANDS) == 0) {
                    colnum++;
                }
            }
            // -------------------------------------------------------------------------------

            ConfigWorkBasket.data.get(instSerial).curLayout = iLayout;
            ConfigWorkBasket.data.get(instSerial).myRoundedCorners = new MyRoundedCorners(ConfigWorkBasket.data.get(instSerial), blockCounts, myLayout.mixedLayout);

            initListviews();

            waitCheckSocket = settingDelaySocketCheck;
            handler.postDelayed(checkSocketTimer, waitCheckSocket);
        } catch (NoFuckingEntries e) {
            layoutId = settingLayouts[LAYOUT_HORIZONTAL];
            mView = new RemoteViews(mContext.getPackageName(), layoutId);
            setVisibility("NoFuckingEntries", EMPTY_WIDGET, getString(R.string.empty_widget));
        }
    }

    private void initListviews() {
        //if (BuildConfig.DEBUG) //Log.d(TAG, "initListviews started");
        mView = new RemoteViews(mContext.getPackageName(), layoutId);

        mView.setViewVisibility(R.id.message, View.GONE);
        mView.setViewVisibility(R.id.new_version, View.GONE);

        for (Entry<String, ArrayList<Integer>> entry : myLayout.layout.entrySet()) {
            String type = entry.getKey();
            int actCol = 0;
            for (int listviewId : entry.getValue()) {
                if (colTypeIsValid(actCol, type)) {
                    initListview(listviewId, actCol, type);
                }
                actCol++;
            }
        }

        Intent intentSync = new Intent(mContext, WidgetProvider.class);
        intentSync.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        PendingIntent pendingSync = PendingIntent.getBroadcast(mContext, 0, intentSync, PendingIntent.FLAG_UPDATE_CURRENT);
        mView.setOnClickPendingIntent(R.id.mainshape, pendingSync);

        appWidgetManager.updateAppWidget(widgetId, mView);
    }

    private void initListview(int listviewId, int actCol, String type) {
        final Intent onItemClick = new Intent(mContext, WidgetProvider.class);
        //onItemClick.setData(Uri.parse(onItemClick.toUri(Intent.URI_INTENT_SCHEME)));
        PendingIntent onClickPendingIntent = PendingIntent.getBroadcast(mContext, 0, onItemClick, PendingIntent.FLAG_UPDATE_CURRENT);
        mView.setPendingIntentTemplate(listviewId, onClickPendingIntent);

        Class<?> serviceClass = CommonListviewService.class;
        Intent myIntent = new Intent(mContext, serviceClass);
        myIntent.putExtra(ACTCOL, actCol);
        myIntent.putExtra(FHEM_TYPE, type);
        myIntent.putExtra(INSTSERIAL, instSerial);
        myIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
        String uriString = myIntent.toUri(Intent.URI_INTENT_SCHEME);
        myIntent.setData(Uri.parse(uriString));
        mView.setRemoteAdapter(listviewId, myIntent);
        mView.setViewVisibility(listviewId, View.VISIBLE);
        appWidgetManager.notifyAppWidgetViewDataChanged(widgetId, myLayout.layout.get(type).get(actCol));
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
        boolean screenOn = false;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT_WATCH) {
            PowerManager pm = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
            screenOn = pm != null && pm.isScreenOn();
        }
        return screenOn;
    }

    public boolean isScreenOnNew() {
        boolean screenOn = false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
            DisplayManager dm = (DisplayManager) mContext.getSystemService(Context.DISPLAY_SERVICE);
            if (dm != null) {
                for (Display display : dm.getDisplays()) {
                    if (display.getState() != Display.STATE_OFF) {
                        screenOn = true;
                    }
                }
            }
        }
        return screenOn;
    }

    private void setVisibility(String reason, String type, String text) {
        if (mView == null) return;
        try {
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
                    mView.setViewVisibility(R.id.message, View.GONE);
                    mView.setViewVisibility(R.id.new_version, View.GONE);
                    setVisibilityListViews(View.VISIBLE);
                    break;
                case VERSION_CLOSE:
                    mView.setViewVisibility(R.id.new_version, View.GONE);
                    setVisibilityListViews(View.VISIBLE);
                    break;
                case EMPTY_WIDGET:
                    mView.setTextViewText(R.id.message, text);
                    mView.setViewVisibility(R.id.message, View.VISIBLE);
                    //new MySetOnClickPendingIntent(mContext, mView, NEW_CONFIG, R.id.message);
                    //setVisibilityListViews(View.GONE);
                    break;
                case SOCKET_DISCONNECTED:
                    if (screenIsOn) {
                        mView.setTextViewText(R.id.message, text);
                        mView.setViewVisibility(R.id.message, View.VISIBLE);
                        new MySetOnClickPendingIntent(mContext, mView, NEW_CONFIG, R.id.message);
                        setVisibilityListViews(View.GONE);
                    }
                    break;
            }

            appWidgetManager.updateAppWidget(widgetId, mView);
        } catch (Exception e) {
            // ignore
        }
    }

    private void setVisibilityListViews(int action) {
        if (myLayout == null) {
            return;
        }
        for (Entry<String, ArrayList<Integer>> entry : myLayout.layout.entrySet()) {
            for (int listviewId : entry.getValue()) {
                mView.setViewVisibility(listviewId, action);
            }
        }
    }

    private void requestValues() {
        try {
            mySocket.requestValues(ConfigWorkBasket.data.get(instSerial).getSwitchesList(), "once");
            mySocket.requestValues(ConfigWorkBasket.data.get(instSerial).getValuesList(), "once");
            mySocket.requestValues(ConfigWorkBasket.data.get(instSerial).getIntValuesList(), "once");
            mySocket.requestValues(ConfigWorkBasket.data.get(instSerial).getLightScenesList(), "once");

            mySocket.requestValues(ConfigWorkBasket.data.get(instSerial).getSwitchesList(), "onChange");
            mySocket.requestValues(ConfigWorkBasket.data.get(instSerial).getValuesList(), "onChange");
            mySocket.requestValues(ConfigWorkBasket.data.get(instSerial).getIntValuesList(), "onChange");
            mySocket.requestValues(ConfigWorkBasket.data.get(instSerial).getLightScenesList(), "onChange");
        } catch (NullPointerException e) {
            start();
        }
    }

    public Runnable checkSocketTimer = new Runnable() {
        @Override
        public void run() {
            handler.removeCallbacks(checkSocketTimer);
            checkSocket();
            if (screenIsOn) {
                if (waitCheckSocket == settingDelaySocketCheck) {
                    waitCheckSocket = settingWaitSocketShort;
                }
                handler.postDelayed(this, waitCheckSocket);
            }
        }
    };

    public void checkSocket() {
        if (screenIsOn) {
            if (mySocket == null) {
                initSocket();
            } else if (mySocket.socket == null) {
                initSocket();
            } else if (!mySocket.socket.connected()) {
                mySocket.destroy();
                initSocket();
            }
        } else {
            if (mySocket != null) {
                mySocket.destroy();
                mySocket = null;
            }
        }
    }

    private void initSocket() {
        //if (BuildConfig.DEBUG) Log.d(TAG, "initSocket started");
        if (!keepDisconnected) {
            try {
                mySocket = new MySocket(mContext, configDataCommon, "Widget");
                defineSocketListeners();
                mySocket.doConnect();
            } catch (Exception e) {
                setVisibility("connectError", SOCKET_DISCONNECTED, getString(R.string.noconn));
            }
        }
    }

    private void defineSocketListeners() {
        if (mySocket == null || mySocket.socket == null) {
            return;
        }
        mySocket.socket.on("authenticated", args -> {
            requestValues();
            waitCheckSocket = myWifiInfo.isWifi() ? settingWaitSocketWifi : settingWaitSocketLong;
            setVisibility("authenticated", SOCKET_CONNECTED, "");
        });

        mySocket.socket.on(Socket.EVENT_DISCONNECT, args1 -> {
            mySocket = null;
            waitCheckSocket = settingWaitSocketShort;
            setVisibility("EVENT_DISCONNECT", SOCKET_DISCONNECTED, getString(R.string.noconn));
            if (screenIsOn) {
                handler.postDelayed(checkSocketTimer, waitCheckSocket);
            }
        });

        mySocket.socket.on(Socket.EVENT_CONNECT_ERROR, args1 -> {
            if (mySocket != null) {
                mySocket.destroy();
                mySocket = null;
            }
            waitCheckSocket = settingWaitSocketShort;
            setVisibility("EVENT_DISCONNECT", SOCKET_DISCONNECTED, getString(R.string.noconn));
            if (screenIsOn) {
                handler.postDelayed(checkSocketTimer, waitCheckSocket);
            }
        });

        mySocket.socket.on("value", args1 -> {
            try {
                if (args1 == null) return;
                JSONObject obj = (JSONObject) args1[0];
                Iterator<String> iterator = obj.keys();
                String unit;
                while (iterator.hasNext()) {
                    unit = iterator.next();
                    String value = null;
                    try {
                        value = obj.getString(unit);
                    } catch (Exception e) {
                        Log.e("mySocket", e.getMessage());
                    }

                    //Log.d(TAG, "new value: " + unit + ":" + value + " - widgetId: " + widgetId);
                    int actColSwitch = ConfigWorkBasket.data.get(instSerial).setSwitchIcon(unit, value);
                    if (actColSwitch > -1) {
                        appWidgetManager.notifyAppWidgetViewDataChanged(widgetId, myLayout.layout.get("switch").get(actColSwitch));
                    }

                    int actColValue = ConfigWorkBasket.data.get(instSerial).setValue(unit, value);
                    if (actColValue > -1) {
                        appWidgetManager.notifyAppWidgetViewDataChanged(widgetId, myLayout.layout.get("value").get(actColValue));
                    }

                    if (ConfigWorkBasket.data.get(instSerial).setLightscene(unit, value)) {
                        appWidgetManager.notifyAppWidgetViewDataChanged(widgetId, myLayout.layout.get("lightscene").get(0));
                    }

                    if (ConfigWorkBasket.data.get(instSerial).setIntValue(unit, value)) {
                        if (myLayout.layout.containsKey("intvalue")) {
                            appWidgetManager.notifyAppWidgetViewDataChanged(widgetId, myLayout.layout.get("intvalue").get(0));
                        }
                    }
                }
            } catch (Exception e) {
                Log.e("mySocket2", e.getMessage());
            }
        });

        mySocket.socket.on("version", args1 -> {
            if (args1 == null) return;
            try {
                JSONObject obj = (JSONObject) args1[0];
                String type = (obj.has("type")) ? obj.getString("type") : "fhemjs";
                versionChecks.setVersions(type, obj.getString("installed"), obj.getString("latest"));
            } catch (Exception e) {
                Log.e("mySocket3", e.getMessage());
            }

        });

        mySocket.socket.on("fhemError", args1 -> setVisibility("fhemError", SOCKET_DISCONNECTED, getString(R.string.noconn)));

        mySocket.socket.on("fhemConn", args1 -> setVisibility("fhemConn", SOCKET_CONNECTED, ""));
    }

    Runnable checkVersionTimer = new Runnable() {
        @Override
        public void run() {
            try {
                handler.removeCallbacks(checkVersionTimer);
                checkVersion();
            } finally {
                handler.postDelayed(checkVersionTimer, settingIntervalVersionCheck);
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
                handler.postDelayed(checkShowVersionTimer, settingIntervalShowVersionCheck);
            }
        }
    };

    private void checkVersion() {
        if (myWifiInfo.isWifi()) {
            new GetStoreVersion(mContext, STORE_VERSION_WIDGET).execute();
        }
    }

    private void checkShowVersion() {
        //if (BuildConfig.DEBUG) //Log.d(TAG, "checkShowVersion started");
        if (!screenIsOn) return;
        //if (BuildConfig.DEBUG) //Log.d(TAG, versionChecks.typesToString());
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

            setVisibility("", type, hint);
            currentVersionType = type;
            //if (BuildConfig.DEBUG) //Log.d(TAG, "show version hint " + type);
        }
    }

    private void saveSuppressedVersions(String type) {
        try {
            configDataCommon.suppressedVersions.put(type, versionChecks.getSuppressedVersion(type));
            ConfigDataIO configDataIO = new ConfigDataIO(mContext);
            configDataIO.saveCommon(configDataCommon);
        } catch (Exception e) {
            // ignore
        }
    }

    private class NoFuckingEntries extends Exception {
        NoFuckingEntries(String exc) {
            super(exc);
        }

        public String getMessage() {
            return super.getMessage();
        }
    }

    private boolean colTypeIsValid(int col, String type) {
        boolean isOK = false;
        ConfigWorkInstance curInstance = ConfigWorkBasket.data.get(instSerial);
        switch (type) {
            case "switch":
                if (curInstance.switchesCols.size() > col) {
                    if (curInstance.switchesCols.get(col).size() > 0) {
                        isOK = true;
                    }
                }
            case "value":
                if (curInstance.valuesCols.size() > col) {
                    if (curInstance.valuesCols.get(col).size() > 0) {
                        isOK = true;
                    }
                }
            case "lightscene":
                if (curInstance.lightScenes.items.size() > 0) {
                    isOK = true;
                }
            case "command":
                if (curInstance.commandsCols.size() > col) {
                    if (curInstance.commandsCols.get(col).size() > 0) {
                        isOK = true;
                    }
                }
            case "intvalue":
                if (curInstance.intValues.size() > 0) {
                    isOK = true;
                }
        }
        return isOK;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}

