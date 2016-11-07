package de.fehngarten.fhemswitch.widget.listviews;

import android.content.Intent;
import android.widget.RemoteViewsService;
//import android.util.Log; 

public class SwitchesService2 extends RemoteViewsService
{
   @Override
   public RemoteViewsFactory onGetViewFactory(Intent intent)
   {
      //Log.i("SwitchesService2","started");
      return new SwitchesFactory(getApplicationContext(), intent, 2);
   } 
}