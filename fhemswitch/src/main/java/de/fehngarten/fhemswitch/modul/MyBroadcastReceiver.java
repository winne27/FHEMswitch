package de.fehngarten.fhemswitch.modul;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

public class MyBroadcastReceiver extends BroadcastReceiver {

    private MyReceiveListener myReceiveListener;
    private IntentFilter myfilter;

    public MyBroadcastReceiver(Context myContext, MyReceiveListener myReceiveListener, String[] actions) {

        this.myReceiveListener = myReceiveListener;

        myfilter = new IntentFilter();

        for (String action : actions) {
            myfilter.addAction(action);
        }
        myContext.registerReceiver(this, myfilter);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("BroadcastListener", myReceiveListener.getClass().getName() + " - " + intent.getAction());
        myReceiveListener.run(context, intent);
    }


}
