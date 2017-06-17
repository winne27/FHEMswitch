package de.fehngarten.fhemswitch.widget.listviews;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService.RemoteViewsFactory;

import de.fehngarten.fhemswitch.R;
import de.fehngarten.fhemswitch.data.ConfigWorkBasket;
import de.fehngarten.fhemswitch.widget.WidgetProvider;

import static de.fehngarten.fhemswitch.global.Consts.*;

//import android.util.Log;

class IntValuesFactory implements RemoteViewsFactory {
    //private static final String CLASSNAME = "ValuesFactory.";
    private Context mContext = null;
    private int instSerial;
    //private final String TAG;
    private int widgetId;

    IntValuesFactory(Context context, Intent intent) {
        //if (BuildConfig.DEBUG) Log.d(CLASSNAME, "started");
        mContext = context;
        instSerial = intent.getIntExtra(INSTSERIAL, -1);
        //TAG = "IntValuesFactory-" + instSerial;
        widgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1);
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

        if (ConfigWorkBasket.data.get(instSerial).intValues == null) {
            return (0);
        } else {
            return ConfigWorkBasket.data.get(instSerial).intValues.size();
        }
    }

    @Override
    public RemoteViews getViewAt(int position) {
        //Log.i("values Position: " + position + " of " + values.size(),values.get(position).name);
        RemoteViews mView = new RemoteViews(mContext.getPackageName(), R.layout.intvalue_row);
        int count = getCount();
        if (position >= count || count <= 0 || ConfigWorkBasket.data.get(instSerial).intValues.size() == 0) {
            Intent intent = new Intent(mContext.getApplicationContext(), WidgetProvider.class);
            intent.setAction(NEW_CONFIG);
            mContext.sendBroadcast(intent);
            return mView;
        }

        mView.setTextViewText(R.id.intvalue_name, ConfigWorkBasket.data.get(instSerial).intValues.get(position).name);
        mView.setTextViewText(R.id.intvalue_value, ConfigWorkBasket.data.get(instSerial).intValues.get(position).value);

        if (position == 0) {
            mView.setInt(R.id.intvalue_row, "setBackgroundResource", R.drawable.valuefirst);
        } else if (position == getCount() - 1) {
            mView.setInt(R.id.intvalue_row, "setBackgroundResource", R.drawable.valuelast);
        } else {
            mView.setInt(R.id.intvalue_row, "setBackgroundResource", R.drawable.value);
        }

        Bundle bundle = new Bundle();
        bundle.putString(FHEM_TYPE, "intvalue");
        bundle.putInt(POS, position);
        bundle.putString(SUBACTION, DOWNFAST);
        bundle.putInt(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
        bundle.putInt(INSTSERIAL, instSerial);
        Intent fillInIntent = new Intent();
        fillInIntent.setAction(SEND_FHEM_COMMAND);
        fillInIntent.putExtras(bundle);
        mView.setOnClickFillInIntent(R.id.intvalue_down_fast, fillInIntent);

        bundle = new Bundle();
        bundle.putString(FHEM_TYPE, "intvalue");
        bundle.putInt(POS, position);
        bundle.putString(SUBACTION, DOWN);
        bundle.putInt(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
        bundle.putInt(INSTSERIAL, instSerial);
        fillInIntent = new Intent();
        fillInIntent.setAction(SEND_FHEM_COMMAND);
        fillInIntent.putExtras(bundle);
        mView.setOnClickFillInIntent(R.id.intvalue_down, fillInIntent);

        bundle = new Bundle();
        bundle.putString(FHEM_TYPE, "intvalue");
        bundle.putInt(POS, position);
        bundle.putString(SUBACTION, UP);
        bundle.putInt(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
        bundle.putInt(INSTSERIAL, instSerial);
        fillInIntent = new Intent();
        fillInIntent.setAction(SEND_FHEM_COMMAND);
        fillInIntent.putExtras(bundle);
        mView.setOnClickFillInIntent(R.id.intvalue_up, fillInIntent);

        bundle = new Bundle();
        bundle.putString(FHEM_TYPE, "intvalue");
        bundle.putInt(POS, position);
        bundle.putString(SUBACTION, UPFAST);
        bundle.putInt(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
        bundle.putInt(INSTSERIAL, instSerial);
        fillInIntent = new Intent();
        fillInIntent.setAction(SEND_FHEM_COMMAND);
        fillInIntent.putExtras(bundle);
        mView.setOnClickFillInIntent(R.id.intvalue_up_fast, fillInIntent);

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
