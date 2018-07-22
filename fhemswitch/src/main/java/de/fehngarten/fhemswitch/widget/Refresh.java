package de.fehngarten.fhemswitch.widget;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import static android.appwidget.AppWidgetManager.ACTION_APPWIDGET_UPDATE;

public class Refresh extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Log.d(TAG, "onCreate fired");
        super.onCreate(savedInstanceState);
        moveTaskToBack(true);
        Intent intent = new Intent(getApplicationContext(), WidgetProvider.class);
        intent.setAction(ACTION_APPWIDGET_UPDATE);
        sendBroadcast(intent);
        finish();
    }
}
