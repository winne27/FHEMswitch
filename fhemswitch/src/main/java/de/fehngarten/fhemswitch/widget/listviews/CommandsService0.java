package de.fehngarten.fhemswitch.widget.listviews;

import android.content.Intent;
import android.widget.RemoteViewsService;
//import android.util.Log;

public class CommandsService0 extends RemoteViewsService
{
   @Override
   public RemoteViewsFactory onGetViewFactory(Intent intent)
   {
      //Log.i("ValuesService","started");
      return new CommandsFactory(getApplicationContext(), intent, 0);
   } 
}