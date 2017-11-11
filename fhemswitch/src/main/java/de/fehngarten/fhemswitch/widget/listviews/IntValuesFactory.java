package de.fehngarten.fhemswitch.widget.listviews;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService.RemoteViewsFactory;

import com.google.firebase.crash.FirebaseCrash;

import de.fehngarten.fhemswitch.R;
import de.fehngarten.fhemswitch.data.ConfigWorkBasket;
import de.fehngarten.fhemswitch.data.ConfigWorkInstance;
import de.fehngarten.fhemswitch.data.RowIntValue;

import static android.appwidget.AppWidgetManager.ACTION_APPWIDGET_UPDATE;
import static de.fehngarten.fhemswitch.global.Consts.*;
import static de.fehngarten.fhemswitch.global.Settings.settingDefaultShapes;
import static de.fehngarten.fhemswitch.global.Settings.settingHeaderShapes;

class IntValuesFactory implements RemoteViewsFactory {
    //private static final String CLASSNAME = "ValuesFactory.";
    private Context mContext = null;
    private int instSerial;
    //private final String TAG;
    private int widgetId;
    private ConfigWorkInstance curInstance;

    IntValuesFactory(Context context, Intent intent) {
        //if (BuildConfig.DEBUG) Log.d(CLASSNAME, "started");
        mContext = context;
        instSerial = intent.getIntExtra(INSTSERIAL, -1);
        //TAG = "IntValuesFactory-" + instSerial;
        widgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1);
        curInstance = ConfigWorkBasket.data.get(instSerial);
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
        int size;
        try {
            size = ConfigWorkBasket.data.get(instSerial).intValues.size();
        } catch (Exception e) {
            FirebaseCrash.report(e);
            size = 0;
        }
        return size;
    }

    @Override
    public RemoteViews getViewAt(int position) {
        //Log.i("values Position: " + position + " of " + values.size(),values.get(position).name);
        RemoteViews mView = null;
        int count = getCount();
        try {
            if (position >= count || count <= 0 || ConfigWorkBasket.data.get(instSerial).intValues.size() == 0) {
                //mView = new RemoteViews(mContext.getPackageName(), R.layout.widget_row_intvalue);
                //Intent intent = new Intent(mContext.getApplicationContext(), WidgetProvider.class);
                //intent.setAction(NEW_CONFIG);
                //mContext.sendBroadcast(intent);
            } else {
                RowIntValue curIntValue = curInstance.intValues.get(position);
                String type = ConfigWorkBasket.data.get(instSerial).myRoundedCorners.getType(INTVALUES, 0, position);
                if (curIntValue.unit.equals(HEADER_SEPERATOR)) {
                    if (curIntValue.name.equals("")) {
                        mView = new RemoteViews(mContext.getPackageName(), R.layout.seperator);
                    } else {
                        mView = new RemoteViews(mContext.getPackageName(), R.layout.header);
                        mView.setTextViewText(R.id.header_name, curIntValue.name);
                        mView.setInt(R.id.header, "setBackgroundResource", settingHeaderShapes.get(type));

                        final Intent fillInIntent = new Intent();
                        fillInIntent.setAction(ACTION_APPWIDGET_UPDATE);
                        mView.setOnClickFillInIntent(R.id.header, fillInIntent);
                    }
                } else {
                    mView = new RemoteViews(mContext.getPackageName(), R.layout.widget_row_intvalue);
                    mView.setTextViewText(R.id.intvalue_name, curIntValue.name);
                    mView.setTextViewText(R.id.intvalue_value, curIntValue.value);

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
                    mView.setInt(R.id.intvalue_row, "setBackgroundResource", settingDefaultShapes.get(type));
                }
            }
        } catch (IndexOutOfBoundsException e) {
            FirebaseCrash.log(curInstance.toString());
            FirebaseCrash.report(e);
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
        return 3;
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
