package de.fehngarten.fhemswitch.widget.listviews;

import android.content.Intent;
import android.widget.RemoteViewsService;
//import android.util.Log; 

public class SwitchesService1 extends RemoteViewsService
{
   @Override
   public RemoteViewsFactory onGetViewFactory(Intent intent)
   {
      //Log.i("SwitchesService1","started");
      return new SwitchesFactory(getApplicationContext(), intent, 1);
   } 
}