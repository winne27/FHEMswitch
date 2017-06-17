package de.fehngarten.fhemswitch.widget.listviews;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService.RemoteViewsFactory;
import java.util.ArrayList;
import de.fehngarten.fhemswitch.R;

import static de.fehngarten.fhemswitch.global.Consts.*;

import de.fehngarten.fhemswitch.data.ConfigWorkBasket;
import de.fehngarten.fhemswitch.data.ConfigWorkInstance;
import de.fehngarten.fhemswitch.data.MyCommand;
import de.fehngarten.fhemswitch.widget.WidgetProvider;

//import android.util.Log;
class CommandsFactory implements RemoteViewsFactory {
    //private static final String TAG = "CommandsFactory.";
    private Context mContext = null;
    private int colnum;
    private int instSerial;
    private int widgetId;
    private ConfigWorkInstance curInstance;

    CommandsFactory(Context context, Intent intent, int colnum) {
        //if (BuildConfig.DEBUG) Log.d(TAG, "started");
        mContext = context;
        this.colnum = colnum;
        instSerial = intent.getIntExtra(INSTSERIAL, -1);
        curInstance = ConfigWorkBasket.data.get(instSerial);
        widgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1);
    }

    public void initData() {
    }

    @Override
    public void onCreate() {

    }

    @Override
    public void onDataSetChanged() {

    }

    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub

    }

    @Override
    public int getCount() {
        //if (BuildConfig.DEBUG) Log.d(TAG, "values size: " + Integer.toString(values.size()));
        if (curInstance.commandsCols == null || curInstance.commandsCols.size() == 0 || curInstance.commandsCols.size() <= colnum) {
            return (0);
        } else {
            return curInstance.commandsCols.get(colnum).size();
        }
    }

    @Override
    public RemoteViews getViewAt(int position) {
        //Log.i("values Position: " + position + " of " + values.size(),values.get(position).name);
        RemoteViews mView = new RemoteViews(mContext.getPackageName(), R.layout.command_row);
        int count = getCount();
        if (position >= count || count <= 0 ) {
            Intent intent = new Intent(mContext.getApplicationContext(), WidgetProvider.class);
            intent.setAction(NEW_CONFIG);
            mContext.sendBroadcast(intent);
        } else {
            ArrayList<MyCommand> myCommandsCols = curInstance.commandsCols.get(colnum);
            MyCommand curCommand = myCommandsCols.get(position);

            mView.setTextViewText(R.id.command_name, curCommand.name);

            if (curCommand.activ) {
                mView.setInt(R.id.command_row, "setBackgroundResource", R.drawable.activecommand);
            } else {
                mView.setInt(R.id.command_row, "setBackgroundResource", R.drawable.widget_shape_command);
            }

            Bundle bundle = new Bundle();
            bundle.putString(FHEM_COMMAND, curCommand.command);
            //bundle.putString(FHEM_COMMAND, ConfigWorkBasket.data.get(instSerial).commandsCols.get(colnum).get(position).command);
            bundle.putString(FHEM_TYPE, "command");
            bundle.putString(POS, Integer.toString(position));
            bundle.putString(COL, Integer.toString(colnum));
            bundle.putInt(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
            bundle.putInt(INSTSERIAL, instSerial);

            final Intent fillInIntent = new Intent();
            fillInIntent.setAction(SEND_FHEM_COMMAND);
            fillInIntent.putExtras(bundle);
            mView.setOnClickFillInIntent(R.id.command_name, fillInIntent);
        }
        return mView;
    }

    @Override
    public RemoteViews getLoadingView() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int getViewTypeCount() {
        // TODO Auto-generated method stub
        return 1;
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    @Override
    public boolean hasStableIds() {
        // TODO Auto-generated method stub
        return false;
    }
}
