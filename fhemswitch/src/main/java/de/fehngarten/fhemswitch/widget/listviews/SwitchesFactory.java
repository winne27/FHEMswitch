package de.fehngarten.fhemswitch.widget.listviews;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService.RemoteViewsFactory;

import de.fehngarten.fhemswitch.R;
import static de.fehngarten.fhemswitch.global.Consts.*;
import de.fehngarten.fhemswitch.widget.WidgetService;
//import android.util.Log;

class SwitchesFactory implements RemoteViewsFactory {
    //private static final String CLASSNAME = "SwitchesFactory.";
    private Context mContext = null;
    int colnum;
    //private List<MySwitch> switches = new ArrayList<MySwitch>();

    SwitchesFactory(Context context, Intent intent, int colnum) {
        //if (BuildConfig.DEBUG) Log.d(CLASSNAME, "started");
        mContext = context;
        this.colnum = colnum;
        //Log.i("colnum in factory",intent.getExtras().getString("colnum"));
    }

    public void initData() {
        //String methodname = "initData";
        //if (BuildConfig.DEBUG) Log.d(CLASSNAME + methodname, "started");
    }

    @Override
    public void onCreate() {
        //String methodname = "onCreate";
        //if (BuildConfig.DEBUG) Log.d(CLASSNAME + methodname, "started");
        //initData();
    }

    @Override
    public void onDataSetChanged() {
        //String methodname = "onDataSetChanged";
        //if (BuildConfig.DEBUG) Log.d(CLASSNAME + methodname, "started");
        //initData();
    }

    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub

    }

    public int getCount() {
        //String methodname = "getCount";
        //if (BuildConfig.DEBUG) Log.d(CLASSNAME + methodname, "switches size: " + Integer.toString(switches.size()));

        if (WidgetService.configData == null || WidgetService.configData.switchesCols == null || WidgetService.configData.switchesCols.size() <= colnum) {
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
        fillInIntent.setAction(SEND_FHEM_COMMAND);
        final Bundle bundle = new Bundle();
        bundle.putString(FHEM_COMMAND, WidgetService.configData.switchesCols.get(colnum).get(position).activateCmd());
        //if (BuildConfig.DEBUG) Log.d("SwitchesFactory","cmd: " + WidgetService.configData.switchesCols.get(colnum).get(position).activateCmd());
        bundle.putString(FHEM_TYPE, "switch");
        bundle.putString(POS, Integer.toString(position));
        bundle.putString(COL, Integer.toString(colnum));
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
