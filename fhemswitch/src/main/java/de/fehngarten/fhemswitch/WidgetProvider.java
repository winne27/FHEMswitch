package de.fehngarten.fhemswitch;

import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;

import java.util.Arrays;

import static android.appwidget.AppWidgetManager.ACTION_APPWIDGET_DELETED;
import static android.appwidget.AppWidgetManager.ACTION_APPWIDGET_UPDATE;

public class WidgetProvider extends AppWidgetProvider {

    private static final String TAG = "WidgetProvider";
    public static final String SEND_FHEM_COMMAND = "de.fehngarten.fhemswitch.SEND_FHEM_COMMAND";
    public static final String COMMAND = "de.fehngarten.fhemswitch.COMMAND";
    public static final String TYPE = "de.fehngarten.fhemswitch.TYPE";
    public static final String POS = "de.fehngarten.fhemswitch.POS";
    public static final String COL = "de.fehngarten.fhemswitch.COL";
    public static final String URL = "de.fehngarten.fhemswitch.URL";
    public static final String OPEN_URL = "de.fehngarten.fhemswitch.OPEN_URL";

    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReiceive startet by " + intent.getAction());
        //Log.d(TAG, bundleToString(intent.getExtras()));

        switch (intent.getAction()) {
            case ACTION_APPWIDGET_UPDATE:
                context.startService(new Intent(context.getApplicationContext(), WidgetService.class));
                break;
            case ACTION_APPWIDGET_DELETED:
                context.stopService(new Intent(context.getApplicationContext(), WidgetService.class));
                break;
            case SEND_FHEM_COMMAND:
                WidgetService.sendCommand(intent);
                break;
            case OPEN_URL:
                String urlString = intent.getExtras().getString(URL);
                Intent webIntent = new Intent(Intent.ACTION_VIEW).setData(Uri.parse(urlString));
                webIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(webIntent);
                break;
        }
        super.onReceive(context, intent);
    }

    @Override
    public void onDisabled(Context context) {
        Intent intent = new Intent(context, WidgetService.class);
        context.stopService(intent);
        intent = new Intent(context, WidgetService.class);
        context.stopService(intent);
        super.onDisabled(context);
    }

    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
    }

}
