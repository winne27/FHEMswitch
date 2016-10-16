package de.fehngarten.fhemswitch;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService.RemoteViewsFactory;
//import android.util.Log;

class SwitchesFactory implements RemoteViewsFactory {
    //private static final String CLASSNAME = "SwitchesFactory.";
    private Context mContext = null;
    int colnum;
    //private List<MySwitch> switches = new ArrayList<MySwitch>();

    SwitchesFactory(Context context, Intent intent, int colnum) {
        //Log.d(CLASSNAME, "started");
        mContext = context;
        this.colnum = colnum;
        //Log.i("colnum in factory",intent.getExtras().getString("colnum"));
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

    public int getCount() {
        //String methodname = "getCount";
        //Log.d(CLASSNAME + methodname, "switches size: " + Integer.toString(switches.size()));

        if (WidgetService.configData == null || WidgetService.configData.switchesCols == null || WidgetService.configData.switchesCols.size() == 0) {
            return (0);
        } else {
            return WidgetService.configData.switchesCols.get(colnum).size();
        }
    }

    @Override
    public RemoteViews getViewAt(int position) {
        //Log.i("switches Position: " + position + " in col " + String.valueOf(colnum),WidgetService.configData.switchesCols.get(colnum).get(position).name);
        RemoteViews mView = new RemoteViews(mContext.getPackageName(), R.layout.switch_row);
        if (position >= getCount()) {
            return mView;
        }
        mView.setTextViewText(R.id.switch_name, WidgetService.configData.switchesCols.get(colnum).get(position).name);
        mView.setImageViewResource(R.id.switch_icon, WidgetService.icons.get(WidgetService.configData.switchesCols.get(colnum).get(position).icon));

        final Intent fillInIntent = new Intent();
        fillInIntent.setAction(WidgetProvider.SEND_FHEM_COMMAND);
        final Bundle bundle = new Bundle();
        bundle.putString(WidgetProvider.COMMAND, WidgetService.configData.switchesCols.get(colnum).get(position).activateCmd());
        //Log.d("SwitchesFactory","cmd: " + WidgetService.configData.switchesCols.get(colnum).get(position).activateCmd());
        bundle.putString(WidgetProvider.TYPE, "switch");
        bundle.putString(WidgetProvider.POS, Integer.toString(position));
        bundle.putString(WidgetProvider.COL, Integer.toString(colnum));
        fillInIntent.putExtras(bundle);
        mView.setOnClickFillInIntent(R.id.switch_row, fillInIntent);

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
