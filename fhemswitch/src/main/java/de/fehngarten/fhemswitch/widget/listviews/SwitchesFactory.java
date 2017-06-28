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
import static de.fehngarten.fhemswitch.global.Settings.settingDefaultShapes;
import static de.fehngarten.fhemswitch.global.Settings.settingHeaderShapes;

import de.fehngarten.fhemswitch.data.ConfigWorkBasket;
import de.fehngarten.fhemswitch.data.ConfigWorkInstance;
import de.fehngarten.fhemswitch.data.MySwitch;
import de.fehngarten.fhemswitch.global.Settings;
import de.fehngarten.fhemswitch.widget.WidgetProvider;

class SwitchesFactory implements RemoteViewsFactory {
    //private final String TAG;
    private Context mContext = null;
    private int colnum;
    private int instSerial;
    private int widgetId;
    private ConfigWorkInstance curInstance;

    SwitchesFactory(Context context, Intent intent, int colnum) {
        mContext = context;
        this.colnum = colnum;
        instSerial = intent.getIntExtra(INSTSERIAL, -1);
        curInstance = ConfigWorkBasket.data.get(instSerial);
        widgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1);
        //TAG = "SwitchesFactory-" + instSerial;
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

        if (curInstance == null || curInstance.switchesCols == null || curInstance.switchesCols.size() == 0 || curInstance.switchesCols.size() <= colnum) {
            return (0);
        } else {
            return curInstance.switchesCols.get(colnum).size();
        }
    }

    @Override
    public RemoteViews getViewAt(int position) {
        RemoteViews mView;
        int count = getCount();
        if (position >= count || count <= 0) {
            mView = new RemoteViews(mContext.getPackageName(), R.layout.widget_row_switch);
            Intent intent = new Intent(mContext.getApplicationContext(), WidgetProvider.class);
            intent.setAction(NEW_CONFIG);
            mContext.sendBroadcast(intent);
        } else {
            ArrayList<MySwitch> mySwitchesCols = curInstance.switchesCols.get(colnum);
            MySwitch curSwitch = mySwitchesCols.get(position);
            String type = checkPosition(mySwitchesCols, position);
            if (curSwitch.unit.equals(HEADER_SEPERATOR)) {
                if (curSwitch.name.equals("")) {
                    mView = new RemoteViews(mContext.getPackageName(), R.layout.seperator);
                } else {
                    mView = new RemoteViews(mContext.getPackageName(), R.layout.header);
                    mView.setInt(R.id.header, "setBackgroundResource", settingHeaderShapes.get(type));
                    mView.setTextViewText(R.id.header_name, curSwitch.name);
                }
            } else {
                mView = new RemoteViews(mContext.getPackageName(), R.layout.widget_row_switch);
                mView.setInt(R.id.switch_row, "setBackgroundResource", settingDefaultShapes.get(type));

                mView.setTextViewText(R.id.switch_name, curSwitch.name);
                mView.setImageViewResource(R.id.switch_icon, Settings.settingIcons.get(curSwitch.icon));

                final Bundle bundle = new Bundle();
                bundle.putString(FHEM_COMMAND, curSwitch.activateCmd());
                bundle.putString(FHEM_TYPE, "switch");
                bundle.putString(POS, Integer.toString(position));
                bundle.putString(COL, Integer.toString(colnum));
                bundle.putInt(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
                bundle.putInt(INSTSERIAL, instSerial);

                final Intent fillInIntent = new Intent();
                fillInIntent.setAction(SEND_FHEM_COMMAND);
                fillInIntent.putExtras(bundle);
                mView.setOnClickFillInIntent(R.id.switch_row, fillInIntent);
            }
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

    private String checkPosition(ArrayList<MySwitch> mySwitchesCols, int position) {
        String type = "default";
        boolean isFirst = false;
        boolean isLast = false;

        if (position == 0) {
            isFirst = true;
        }

        if (position == curInstance.switchesCols.get(colnum).size() - 1) {
            isLast = true;
        }

        if (!isLast) {
            MySwitch nextSwitch = mySwitchesCols.get(position + 1);
            if (nextSwitch.unit.equals(HEADER_SEPERATOR) && nextSwitch.name.equals("")) {
                isLast = true;
            }
        }

        if (!isFirst) {
            MySwitch prevSwitch = mySwitchesCols.get(position - 1);
            if (prevSwitch.unit.equals(HEADER_SEPERATOR) && prevSwitch.name.equals("")) {
                isFirst = true;
            }
        }

        if (isFirst && isLast) {
            type = "both";
        } else if (isFirst) {
            type = "first";
        } else if (isLast) {
            type = "last";
        }

        return type;
    }
}
