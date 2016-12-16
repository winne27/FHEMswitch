package de.fehngarten.fhemswitch.widget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import java.util.HashMap;

import de.fehngarten.fhemswitch.data.ConfigDataCommon;
import de.fehngarten.fhemswitch.data.ConfigDataIO;

import static de.fehngarten.fhemswitch.global.Consts.*;

import static android.appwidget.AppWidgetManager.ACTION_APPWIDGET_UPDATE;
import static de.fehngarten.fhemswitch.global.Settings.settingServiceClasses;

public class WidgetProvider extends AppWidgetProvider {

    private static final String TAG = "WidgetProvider";
    private HashMap<Integer, MyService> serviceIntents;

    private class MyService {
        private int serial;
        private Intent intent;

        private MyService(int serial, Intent intent) {
            this.serial = serial;
            this.intent = intent;
        }
    }

    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.d(TAG, "onReiceive startet by " + action);

        if (action == null) {
            action = "";
        }
        switch (action) {
            case ACTION_APPWIDGET_UPDATE:
                 checkWidgets(context, "INIT");
                 for (MyService myService  : serviceIntents.values()) {
                    //Log.d(TAG,"start service " + myService.serial);
                    context.stopService(myService.intent);
                    context.startService(myService.intent);
                }
                break;
            case SEND_FHEM_COMMAND:
                int instSerial = intent.getExtras().getInt(INSTSERIAL);
                Intent commandIntent = new Intent(context.getApplicationContext(), settingServiceClasses.get(instSerial));
                commandIntent.setAction(FHEM_COMMAND);
                commandIntent.putExtras(intent.getExtras());
                context.startService(commandIntent);
                break;
            case OPEN_WEBPAGE:
                String urlString = intent.getStringExtra(FHEM_URI);
                Intent webIntent = new Intent(Intent.ACTION_VIEW).setData(Uri.parse(urlString));
                webIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(webIntent);
                break;
        }
        super.onReceive(context, intent);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager,int[] appWidgetIds) {
        //Log.d(TAG, "onUpdate started");
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);
        //Log.d(TAG, "Deleting " + appWidgetIds.length + " widgets");
        int widgetId = appWidgetIds[0];
        ConfigDataIO configDataIO = new ConfigDataIO(context);
        ConfigDataCommon configDataCommon = configDataIO.readCommon(-1);
        int serial = configDataCommon.delete(configDataIO, widgetId);

        if (serial > -1) {
            Intent intent = new Intent(context.getApplicationContext(), settingServiceClasses.get(serial));
            intent.setAction("INIT");
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
            context.stopService(intent);

            Intent stopConfig = new Intent();
            stopConfig.setAction(STOP_CONFIG);
            context.sendBroadcast(stopConfig);
        }
    }

    @Override
    public void onEnabled(Context context) {
        //Log.d(TAG, "onEnabled started");
        super.onEnabled(context);
    }

    private int[] getWidgetIds(Context context) {
        ComponentName thisWidget = new ComponentName(context, this.getClass());
        AppWidgetManager mgr = AppWidgetManager.getInstance(context);
        return mgr.getAppWidgetIds(thisWidget);
    }

    private void checkWidgets(Context context, String action) {
        int[] widgetIds = getWidgetIds(context);

        //Log.d(TAG,"widgetIds: " + widgetIds.toString());
        ConfigDataIO configDataIO = new ConfigDataIO(context);
        int curWidgetId = (widgetIds.length > 0) ? widgetIds[0] : -1;
        //Log.d(TAG,"curWidgetId: " + curWidgetId);
        ConfigDataCommon configDataCommon = configDataIO.readCommon(curWidgetId);

        serviceIntents = new HashMap<>();

        for (int widgetId : widgetIds) {
            int serial = configDataCommon.getInstByWidgetid(widgetId);

            if (serial == -1) {
                serial = 0;

            }

            if (serial > -1) {
                addServiceIntent(serial, widgetId, context, action);
            }
        }

        configDataCommon.removeUnused(configDataIO, widgetIds);
    }

    private void addServiceIntent(int serial, int widgetId, Context context, String action) {
        Intent intent = new Intent(context.getApplicationContext(), settingServiceClasses.get(serial));
        intent.setAction(action);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
        serviceIntents.put(widgetId, new MyService(serial, intent));
    }
}
