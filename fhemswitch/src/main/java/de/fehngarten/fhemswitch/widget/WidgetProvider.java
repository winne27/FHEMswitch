package de.fehngarten.fhemswitch.widget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.util.SparseArray;

import de.fehngarten.fhemswitch.data.ConfigDataCommon;
import de.fehngarten.fhemswitch.data.ConfigDataIO;

import static de.fehngarten.fhemswitch.global.Consts.*;

import static android.appwidget.AppWidgetManager.ACTION_APPWIDGET_UPDATE;
import static de.fehngarten.fhemswitch.global.Settings.settingServiceClasses;

public class WidgetProvider extends AppWidgetProvider {

    //private static final String TAG = "WidgetProvider";
    public SparseArray<Intent> serviceIntents;

    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        //Log.d(this.getClass().getSimpleName(), "onReiceive startet by " + action);

        if (action == null) {
            action = "";
        }
        switch (action) {
            case ACTION_APPWIDGET_UPDATE:
                checkWidgets(context);
                for(int i = 0, nsize = serviceIntents.size(); i < nsize; i++) {
                    try {
                        serviceIntents.valueAt(i).putExtra("ISFOREGROUND", false);
                        context.startService(serviceIntents.valueAt(i));
                    } catch (Exception e) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            serviceIntents.valueAt(i).putExtra("ISFOREGROUND", true);
                            context.startForegroundService(serviceIntents.valueAt(i));
                        }
                    }
                 }
                break;
            case SEND_FHEM_COMMAND:
                int instSerial = intent.getExtras().getInt(INSTSERIAL);
                //Log.i(this.getClass().getSimpleName(),"instserial: " + Integer.toString(instSerial));
                Intent commandIntent = new Intent(context.getApplicationContext(), settingServiceClasses.get(instSerial));
                commandIntent.setAction(FHEM_COMMAND);
                commandIntent.putExtras(intent.getExtras());
                try {
                    commandIntent.putExtra("ISFOREGROUND", false);
                    context.startService(commandIntent);
                } catch (Exception e) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        commandIntent.putExtra("ISFOREGROUND", true);
                        context.startForegroundService(commandIntent);
                    }
                }
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
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);
        //Log.d(TAG, "Deleting " + appWidgetIds.length + " widgets");
        int widgetId = appWidgetIds[0];
        ConfigDataIO configDataIO = new ConfigDataIO(context);
        ConfigDataCommon configDataCommon = configDataIO.readCommon();
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

    private int[] getWidgetIds(Context context) {
        ComponentName thisWidget = new ComponentName(context, this.getClass());
        AppWidgetManager mgr = AppWidgetManager.getInstance(context);
        return mgr.getAppWidgetIds(thisWidget);
    }

    private void checkWidgets(Context context) {
        int[] widgetIds = getWidgetIds(context);

        //Log.d(TAG,"widgetIds: " + widgetIds.toString());
        ConfigDataIO configDataIO = new ConfigDataIO(context);
        ConfigDataCommon configDataCommon = configDataIO.readCommon();

        serviceIntents = new SparseArray<>();

        for (int widgetId : widgetIds) {
            int serial = configDataCommon.getInstByWidgetid(widgetId);

            if (serial == -1) {  //shit happens
                serial = 0;
            }

            if (serial > -1) {
                addServiceIntent(serial, widgetId, context);
            }
        }

        configDataCommon.removeUnused(configDataIO, widgetIds);
    }

    private void addServiceIntent(int serial, int widgetId, Context context) {
        Intent intent = new Intent(context.getApplicationContext(), settingServiceClasses.get(serial));
        intent.setAction("INIT");
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);

        serviceIntents.put(widgetId, intent);
    }
}
