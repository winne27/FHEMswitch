package de.fehngarten.fhemswitch;

//import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
//import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

public class WidgetProvider extends AppWidgetProvider {

    //private static final String LOG = "WetterstationWidgetProvider";
    public static final String SEND_FHEM_COMMAND = "de.fehngarten.fhemswitch.SEND_FHEM_COMMAND";
    public static final String COMMAND = "de.fehngarten.fhemswitch.COMMAND";
    //public static final String UNIT = "de.fehngarten.fhemswitch.UNIT";
    public static final String TYPE = "de.fehngarten.fhemswitch.TYPE";
    public static final String POS = "de.fehngarten.fhemswitch.POS";
    public static final String COL = "de.fehngarten.fhemswitch.COL";
    public static final String URL = "de.fehngarten.fhemswitch.URL";
    public static final String OPEN_URL = "de.fehngarten.fhemswitch.OPEN_URL";
    public static final String NEW_CONFIG = "de.fehngarten.fhemswitch.NEW_CONFIG";
    //private static Boolean startService = true;

    public void onReceive(Context context, Intent intent) {
        Log.d("WidgetProvider", "onReiceive startet by " + intent.getAction());
        //Log.d("trace", "extra newConfig " + intent.getStringExtra("newConfig"));
        // super.onReceive(context, intent);
        // make sure the user has actually installed a widget
        // before starting the update service
        //if (widgetsInstalledLength(context) != 0 && intent.getAction().equals("android.appwidget.action.APPWIDGET_UPDATE"))
        if (intent.getAction().equals("android.appwidget.action.APPWIDGET_UPDATE")) {
            //if (startService)
            //{
            ////Log.d("trace", "starten Service");

            if (!WidgetService.serviceRunning) {
                Intent serviceIntent = new Intent(context.getApplicationContext(), WidgetService.class);
                context.startService(serviceIntent);
            } else if (intent.getExtras() != null && intent.getExtras().containsKey("newConfig") && intent.getStringExtra("newConfig").equals("1")) {
                Intent newConfig = new Intent(NEW_CONFIG);
                context.sendBroadcast(newConfig);
            }
            //}
            //else
            //{
            //   startService = true;
            //}
        } else if (intent.getAction().equals("android.appwidget.action.APPWIDGET_DELETED")) {
            ////Log.i("trace", "stop service");
            Intent serviceIntent = new Intent(context.getApplicationContext(), WidgetService.class);
            context.stopService(serviceIntent);
        } else if (intent.getAction().equals(SEND_FHEM_COMMAND)) {
            String type = intent.getExtras().getString(TYPE);
            String cmd = intent.getExtras().getString(COMMAND);
            int actcol = 0;
            int position = -1;
            if (type != null) {
                switch (type) {
                    case "switch":
                        position = Integer.parseInt(intent.getExtras().getString(POS));
                        actcol = Integer.parseInt(intent.getExtras().getString(COL));
                        break;
                    case "lightscene":
                        position = Integer.parseInt(intent.getExtras().getString(POS));
                        break;
                    case "command":
                        position = Integer.parseInt(intent.getExtras().getString(POS));
                        actcol = Integer.parseInt(intent.getExtras().getString(COL));
                        break;
                }
            }
            WidgetService.sendCommand(cmd, position, type, actcol);
        } else if (intent.getAction().equals(OPEN_URL)) {
            ////Log.i("trace", "switch pressed");
            String urlString = intent.getExtras().getString(URL);

            Intent webIntent = new Intent(Intent.ACTION_VIEW).setData(Uri.parse(urlString));
            webIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(webIntent);
        }
        super.onReceive(context, intent);
    }

    @Override
    public void onDisabled(Context context) {
        ////Log.i("trace", "onDisabled startet");
        Intent intent = new Intent(context, WidgetService.class);
        context.stopService(intent);
        intent = new Intent(context, WidgetService.class);
        context.stopService(intent);
        super.onDisabled(context);
    }

    @Override
    public void onEnabled(Context context) {
        ////Log.i("trace", "onEnabled startet");
        //startService = false;
        super.onEnabled(context);
    }

    // convenience method to count the number of installed widgets
    /*
    private int widgetsInstalledLength(Context context) {
        ComponentName thisWidget = new ComponentName(context, WidgetProvider.class);
        AppWidgetManager mgr = AppWidgetManager.getInstance(context);
        return mgr.getAppWidgetIds(thisWidget).length;
    }
    */
}
