package de.fehngarten.fhemswitch.modul;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

//import android.support.compat.BuildConfig;
//import android.util.Log;

public class MyBroadcastReceiver extends BroadcastReceiver {

    private MyReceiveListener myReceiveListener;
    private Context mContext;

    public MyBroadcastReceiver(Context context, MyReceiveListener myReceiveListener, String[] actions) {
        mContext = context;
        this.myReceiveListener = myReceiveListener;

        IntentFilter myfilter = new IntentFilter();

        for (String action : actions) {
            myfilter.addAction(action);
        }
        mContext.registerReceiver(this, myfilter);
    }

    public void unregister() {
        mContext.unregisterReceiver(this);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        myReceiveListener.run(context, intent);
    }


}
