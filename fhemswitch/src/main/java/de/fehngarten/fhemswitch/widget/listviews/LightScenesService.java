package de.fehngarten.fhemswitch.widget.listviews;

import android.content.Intent;
import android.widget.RemoteViewsService;
//import android.util.Log;

public class LightScenesService extends RemoteViewsService
{
   @Override
   public RemoteViewsFactory onGetViewFactory(Intent intent)
   {
      //Log.i("SwitchesService","started");
      return new LightScenesFactory(this.getApplicationContext(), intent);
   }
}