package de.fehngarten.fhemswitch.widget;

import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.compat.BuildConfig;
import android.util.Log;

import static de.fehngarten.fhemswitch.global.Consts.*;

import static android.appwidget.AppWidgetManager.ACTION_APPWIDGET_DELETED;
import static android.appwidget.AppWidgetManager.ACTION_APPWIDGET_UPDATE;

public class WidgetProvider extends AppWidgetProvider {

    private static final String TAG = "WidgetProvider";

    public void onReceive(Context context, Intent intent) {
        if (BuildConfig.DEBUG) Log.d(TAG, "onReiceive startet by " + intent.getAction());
        //if (BuildConfig.DEBUG) Log.d(TAG, bundleToString(intent.getExtras()));
        switch (intent.getAction()) {
            case ACTION_APPWIDGET_UPDATE:
                context.stopService(new Intent(context.getApplicationContext(), WidgetService.class));
                context.startService(new Intent(context.getApplicationContext(), WidgetService.class));
                break;
            case ACTION_APPWIDGET_DELETED:
                context.stopService(new Intent(context.getApplicationContext(), WidgetService.class));
                break;
            case SEND_FHEM_COMMAND:
                WidgetService.sendCommand(intent);
                break;
            case OPEN_FHEM_HOMEPAGE:
                String urlString = intent.getExtras().getString(FHEM_URI);
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
