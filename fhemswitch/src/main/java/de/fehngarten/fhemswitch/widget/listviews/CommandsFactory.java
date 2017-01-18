package de.fehngarten.fhemswitch.widget.listviews;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService.RemoteViewsFactory;

import de.fehngarten.fhemswitch.R;

import static de.fehngarten.fhemswitch.global.Consts.*;

import de.fehngarten.fhemswitch.data.ConfigWorkBasket;

//import android.util.Log;
class CommandsFactory implements RemoteViewsFactory {
    private static final String TAG = "CommandsFactory.";
    private Context mContext = null;
    private int colnum;
    private int instSerial;
    private int widgetId;

    CommandsFactory(Context context, Intent intent, int colnum) {
        //if (BuildConfig.DEBUG) Log.d(TAG, "started");
        mContext = context;
        this.colnum = colnum;
        instSerial = intent.getIntExtra(INSTSERIAL, -1);
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
        if (ConfigWorkBasket.data.get(instSerial).commandsCols == null || ConfigWorkBasket.data.get(instSerial).commandsCols.size() <= colnum) {
            return (0);
        } else {
            return ConfigWorkBasket.data.get(instSerial).commandsCols.get(colnum).size();
        }
    }

    @Override
    public RemoteViews getViewAt(int position) {
        //Log.i("values Position: " + position + " of " + values.size(),values.get(position).name);
        RemoteViews mView = new RemoteViews(mContext.getPackageName(), R.layout.command_row);
        if (position >= getCount()) {
            return mView;
        }
        mView.setTextViewText(R.id.command_name, ConfigWorkBasket.data.get(instSerial).commandsCols.get(colnum).get(position).name);

        if (ConfigWorkBasket.data.get(instSerial).commandsCols.get(colnum).get(position).activ) {
            mView.setInt(R.id.command_row, "setBackgroundResource", R.drawable.activecommand);
        } else {
            mView.setInt(R.id.command_row, "setBackgroundResource", R.drawable.widget_shape_command);
        }

        Bundle bundle = new Bundle();
        bundle.putString(FHEM_COMMAND, ConfigWorkBasket.data.get(instSerial).commandsCols.get(colnum).get(position).command);
        bundle.putString(FHEM_TYPE, "command");
        bundle.putString(POS, Integer.toString(position));
        bundle.putString(COL, Integer.toString(colnum));
        bundle.putInt(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
        bundle.putInt(INSTSERIAL, instSerial);

        final Intent fillInIntent = new Intent();
        fillInIntent.setAction(SEND_FHEM_COMMAND);
        fillInIntent.putExtras(bundle);
        mView.setOnClickFillInIntent(R.id.command_name, fillInIntent);

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
