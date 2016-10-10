package de.fehngarten.fhemswitch;

import android.content.Intent;
import android.widget.RemoteViewsService;
//import android.util.Log; 

public class SwitchesService0 extends RemoteViewsService
{
   @Override
   public RemoteViewsFactory onGetViewFactory(Intent intent)
   {
      //Log.i("SwitchesService0","started");
      return new SwitchesFactory(getApplicationContext(), intent, 0);
   } 
}