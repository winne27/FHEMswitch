package de.fehngarten.fhemswitch.modul;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

public class MySetOnClickPendingIntent {

    public MySetOnClickPendingIntent(Context context, RemoteViews view, String action, int id) {
        Intent clickIntent = new Intent();
        clickIntent.setAction(action);
        clickIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, clickIntent, 0);
        view.setOnClickPendingIntent(id, pendingIntent);
    }
}
