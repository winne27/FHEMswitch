package de.fehngarten.fhemswitch.widget.listviews;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService.RemoteViewsFactory;

import java.util.ArrayList;

import de.fehngarten.fhemswitch.R;

import static android.appwidget.AppWidgetManager.ACTION_APPWIDGET_UPDATE;
import static de.fehngarten.fhemswitch.global.Consts.*;

import de.fehngarten.fhemswitch.data.ConfigWorkBasket;
import de.fehngarten.fhemswitch.data.ConfigWorkInstance;
import de.fehngarten.fhemswitch.data.MyValue;
import de.fehngarten.fhemswitch.global.Settings;
import de.fehngarten.fhemswitch.widget.WidgetProvider;
import de.fehngarten.fhemswitch.widget.WidgetService;

//import android.util.Log;

class ValuesFactory implements RemoteViewsFactory {
    //private static final String CLASSNAME = "ValuesFactory.";
    private Context mContext = null;
    private int colnum;
    private int instSerial;
    @SuppressWarnings("FieldCanBeLocal")
    private final String TAG;
    private ConfigWorkInstance curInstance;

    ValuesFactory(Context context, Intent intent, int colnum) {
        //if (BuildConfig.DEBUG) Log.d(CLASSNAME, "started");
        mContext = context;
        this.colnum = colnum;
        instSerial = intent.getIntExtra(INSTSERIAL, -1);
        curInstance = ConfigWorkBasket.data.get(instSerial);
        TAG = "ValuesFactory-" + instSerial;
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

    @Override
    public int getCount() {
        //String methodname = "getCount";
        //if (BuildConfig.DEBUG) Log.d(CLASSNAME + methodname, "values size: " + Integer.toString(values.size()));

        if (curInstance.valuesCols == null || curInstance.valuesCols.size() <= colnum) {
            return (0);
        } else {
            return curInstance.valuesCols.get(colnum).size();
        }
    }

    @Override
    public RemoteViews getViewAt(int position) {
        //Log.i("values Position: " + position + " of " + values.size(),values.get(position).name);
        RemoteViews mView = new RemoteViews(mContext.getPackageName(), R.layout.value_row);
        int count = getCount();
        if (position >= count || count <= 0 ) {
            Intent intent = new Intent(mContext.getApplicationContext(), WidgetProvider.class);
            intent.setAction(NEW_CONFIG);
            mContext.sendBroadcast(intent);
        } else {
            ArrayList<MyValue> myValuesCols = curInstance.valuesCols.get(colnum);
            MyValue curValue = myValuesCols.get(position);

            mView.setTextViewText(R.id.value_name, curValue.name);

            String value = curValue.value;
            boolean useIcon = curValue.useIcon;

            if (useIcon && value.length() > 0 && value.substring(value.length() - 1).equals("%")) {
                String value1 = value.substring(0, value.length() - 1);
                int val = Integer.parseInt(value1);
                val = val / 10;
                String vals = Integer.toString(val);
                mView.setImageViewResource(R.id.prozent_icon, Settings.settingIcons.get("p_" + vals));
                mView.setViewVisibility(R.id.prozent_icon, View.VISIBLE);
                mView.setViewVisibility(R.id.value_icon, View.GONE);
                mView.setViewVisibility(R.id.value_value, View.GONE);
            } else if (useIcon && Settings.settingIcons.containsKey("v_" + value)) {
                mView.setImageViewResource(R.id.value_icon, Settings.settingIcons.get("v_" + value));
                mView.setViewVisibility(R.id.prozent_icon, View.GONE);
                mView.setViewVisibility(R.id.value_icon, View.VISIBLE);
                mView.setViewVisibility(R.id.value_value, View.GONE);
            } else {
                mView.setTextViewText(R.id.value_value, value);
                mView.setViewVisibility(R.id.prozent_icon, View.GONE);
                mView.setViewVisibility(R.id.value_icon, View.GONE);
                mView.setViewVisibility(R.id.value_value, View.VISIBLE);
            }

            if (position == 0) {
                mView.setInt(R.id.value_row, "setBackgroundResource", R.drawable.valuefirst);
            } else if (position == ConfigWorkBasket.data.get(instSerial).valuesCols.get(colnum).size() - 1) {
                mView.setInt(R.id.value_row, "setBackgroundResource", R.drawable.valuelast);
            } else {
                mView.setInt(R.id.value_row, "setBackgroundResource", R.drawable.value);
            }

            final Intent fillInIntent = new Intent();
            fillInIntent.setAction(OPEN_WEBPAGE);


            fillInIntent.putExtra(FHEM_URI, ConfigWorkBasket.urlFhempl + "?detail=" + curValue.unit);
            mView.setOnClickFillInIntent(R.id.value_name, fillInIntent);
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
