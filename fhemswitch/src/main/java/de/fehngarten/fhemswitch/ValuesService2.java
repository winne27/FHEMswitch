package de.fehngarten.fhemswitch;

import android.content.Intent;
import android.widget.RemoteViewsService;
//import android.util.Log;

public class ValuesService2 extends RemoteViewsService
{
   @Override
   public RemoteViewsFactory onGetViewFactory(Intent intent)
   {
      //Log.i("ValuesService","started");
      return new ValuesFactory(getApplicationContext(), intent, 2);
   } 
}