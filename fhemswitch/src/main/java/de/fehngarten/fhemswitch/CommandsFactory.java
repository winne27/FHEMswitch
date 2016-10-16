package de.fehngarten.fhemswitch;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService.RemoteViewsFactory;

//import android.util.Log;

class CommandsFactory implements RemoteViewsFactory {
    private static final String CLASSNAME = "CommandsFactory.";
    private Context mContext = null;
    private int colnum;

    CommandsFactory(Context context, Intent intent, int colnum) {
        //Log.d(CLASSNAME, "started");
        mContext = context;
        this.colnum = colnum;
    }

    public void initData() {
        //String methodname = "initData";
        //Log.d(CLASSNAME + methodname, "started");
    }

    @Override
    public void onCreate() {
        //String methodname = "onCreate";
        //Log.d(CLASSNAME + methodname, "started");
        //initData();
    }

    @Override
    public void onDataSetChanged() {
        //String methodname = "onDataSetChanged";
        //Log.d(CLASSNAME + methodname, "started");
        //initData();
    }

    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub

    }

    @Override
    public int getCount() {
        //String methodname = "getCount";
        //Log.d(CLASSNAME + methodname, "values size: " + Integer.toString(values.size()));

        if (WidgetService.configData == null || WidgetService.configData.commandsCols == null || WidgetService.configData.commandsCols.size() == 0) {
            return (0);
        } else {
            return WidgetService.configData.commandsCols.get(colnum).size();
        }
    }

    @Override
    public RemoteViews getViewAt(int position) {
        //Log.i("values Position: " + position + " of " + values.size(),values.get(position).name);
        RemoteViews mView = new RemoteViews(mContext.getPackageName(), R.layout.command_row);
        if (position >= getCount()) {
            return mView;
        }
        mView.setTextViewText(R.id.command_name, WidgetService.configData.commandsCols.get(colnum).get(position).name);

        if (WidgetService.configData.commandsCols.get(colnum).get(position).activ) {
            mView.setInt(R.id.command_row, "setBackgroundResource", R.drawable.activecommand);
        } else {
            mView.setInt(R.id.command_row, "setBackgroundResource", R.drawable.command);
        }

        final Intent fillInIntent = new Intent();
        fillInIntent.setAction(WidgetProvider.SEND_FHEM_COMMAND);
        final Bundle bundle = new Bundle();
        bundle.putString(WidgetProvider.COMMAND, WidgetService.configData.commandsCols.get(colnum).get(position).command);
        bundle.putString(WidgetProvider.TYPE, "command");
        bundle.putString(WidgetProvider.POS, Integer.toString(position));
        bundle.putString(WidgetProvider.COL, Integer.toString(colnum));
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
