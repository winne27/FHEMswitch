package de.fehngarten.fhemswitch.widget.listviews;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService.RemoteViewsFactory;

import de.fehngarten.fhemswitch.R;
import static de.fehngarten.fhemswitch.global.Consts.*;
import de.fehngarten.fhemswitch.data.ConfigWorkBasket;
import de.fehngarten.fhemswitch.global.Settings;

class SwitchesFactory implements RemoteViewsFactory {
    private final String TAG;
    private Context mContext = null;
    private int colnum;
    private int instSerial;

    SwitchesFactory(Context context, Intent intent, int colnum) {
        mContext = context;
        this.colnum = colnum;
        instSerial = intent.getIntExtra(INSTSERIAL, -1);
        TAG = "SwitchesFactory-" + instSerial;
    }

    public void initData() {
        //String methodname = "initData";
        //Log.d("SwitchesFactory init ", "started");
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
        //if (BuildConfig.DEBUG) Log.d(TAG, "onDataSetChanged");
        //initData();
    }

    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub

    }

    public int getCount() {
        //String methodname = "getCount";
        //if (BuildConfig.DEBUG) Log.d(CLASSNAME + methodname, "switches size: " + Integer.toString(switches.size()));

        if (ConfigWorkBasket.data.get(instSerial) == null || ConfigWorkBasket.data.get(instSerial).switchesCols == null || ConfigWorkBasket.data.get(instSerial).switchesCols.size() <= colnum) {
            return (0);
        } else {
            return ConfigWorkBasket.data.get(instSerial).switchesCols.get(colnum).size();
        }
    }

    @Override
    public RemoteViews getViewAt(int position) {
        //Log.i("switches Position: " + position + " in col " + String.valueOf(colnum),WidgetService.configData.switchesCols.get(colnum).get(position).name);
        RemoteViews mView = new RemoteViews(mContext.getPackageName(), R.layout.switch_row);
        if (position >= getCount()) {
            return mView;
        }
        mView.setTextViewText(R.id.switch_name, ConfigWorkBasket.data.get(instSerial).switchesCols.get(colnum).get(position).name);
        mView.setImageViewResource(R.id.switch_icon, Settings.settingIcons.get(ConfigWorkBasket.data.get(instSerial).switchesCols.get(colnum).get(position).icon));

        final Bundle bundle = new Bundle();
        bundle.putString(FHEM_COMMAND, ConfigWorkBasket.data.get(instSerial).switchesCols.get(colnum).get(position).activateCmd());
        bundle.putString(FHEM_TYPE, "switch");
        bundle.putString(POS, Integer.toString(position));
        bundle.putString(COL, Integer.toString(colnum));
        bundle.putInt(INSTSERIAL, instSerial);

        final Intent fillInIntent = new Intent();
        fillInIntent.setAction(SEND_FHEM_COMMAND);
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
