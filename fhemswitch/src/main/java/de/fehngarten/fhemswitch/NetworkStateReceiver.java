package de.fehngarten.fhemswitch;

import android.content.Context;
import android.content.Intent;
import android.content.BroadcastReceiver;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
//import android.util.Log;

public class NetworkStateReceiver extends BroadcastReceiver
{
   //private static final String TAG = "NetworkStateReceiver";

   @Override
   public void onReceive(final Context context, final Intent intent)
   {

      //Log.d(TAG, "Network connectivity change");

      if (intent.getExtras() != null)
      {
         final ConnectivityManager connectivityManager = (ConnectivityManager) context
         .getSystemService(Context.CONNECTIVITY_SERVICE);
         final NetworkInfo ni = connectivityManager.getActiveNetworkInfo();

         if (ni != null && ni.isConnectedOrConnecting())
         {
            //Log.i(TAG, "Network " + ni.getTypeName() + " connected");
            Intent serviceIntent = new Intent(context.getApplicationContext(), WidgetService.class);
            context.startService(serviceIntent);
         }
      }
   }
}