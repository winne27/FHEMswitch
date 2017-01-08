package de.fehngarten.fhemswitch.widget.listviews;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import de.fehngarten.fhemswitch.R;
import static de.fehngarten.fhemswitch.global.Consts.*;
import de.fehngarten.fhemswitch.data.ConfigWorkBasket;
//import android.util.Log;

class LightScenesFactory implements RemoteViewsService.RemoteViewsFactory {
    //private static final String CLASSNAME = "LightScenesFactory.";
    private Context mContext = null;
    private int instSerial;
    private int widgetId;

    LightScenesFactory(Context context, Intent intent) {
        //if (BuildConfig.DEBUG) Log.d(CLASSNAME, "started");
        mContext = context;
        instSerial = intent.getIntExtra(INSTSERIAL, -1);
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
        //if (BuildConfig.DEBUG) Log.d(CLASSNAME + methodname, "lightscenes size: " + Integer.toString(ConfigDataCage.data.get(instSerial).lightScenes.itemsCount));
        if (ConfigWorkBasket.data.get(instSerial).lightScenes == null) {
            return (0);
        } else {
            return ConfigWorkBasket.data.get(instSerial).lightScenes.itemsCount;
        }
    }

    @Override
    public RemoteViews getViewAt(int position) {
        RemoteViews mView = new RemoteViews(mContext.getPackageName(), R.layout.lightscene_row);
        //Log.i("LightScene Position: " + position + " of " + ConfigDataCage.data.get(instSerial).lightScenes.items.size(),ConfigDataCage.data.get(instSerial).lightScenes.items.get(position).name + " " + ConfigDataCage.data.get(instSerial).lightScenes.items.get(position).unit);
        if (position >= getCount()) {
            return mView;
        }
        if (ConfigWorkBasket.data.get(instSerial).lightScenes.items.get(position).header) {
            //Log.i("Factory",ConfigDataCage.data.get(instSerial).lightScenes.items.get(position).toString());
            SpannableString s = new SpannableString(ConfigWorkBasket.data.get(instSerial).lightScenes.items.get(position).name);
            s.setSpan(new StyleSpan(Typeface.BOLD), 0, s.length(), 0);
            mView.setTextViewText(R.id.lightscene_name, s);
            mView.setTextColor(R.id.lightscene_name, 0xFFCCCCCC);
            mView.setFloat(R.id.lightscene_name, "setTextSize", 20);
            if (position == 0) {
                mView.setInt(R.id.lightscene_name, "setBackgroundResource", R.drawable.header);
            } else {
                mView.setInt(R.id.lightscene_name, "setBackgroundResource", R.drawable.header2);
            }

            final Bundle bundle = new Bundle();
            bundle.putString(FHEM_URI, ConfigWorkBasket.urlFhempl + "?detail=" + ConfigWorkBasket.data.get(instSerial).lightScenes.items.get(position).unit);
            bundle.putString(FHEM_TYPE, "lightScene");
            bundle.putString(POS, Integer.toString(position));
            bundle.putInt(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
            bundle.putInt(INSTSERIAL, instSerial);

            final Intent fillInIntent = new Intent();
            fillInIntent.putExtras(bundle);
            mView.setOnClickFillInIntent(R.id.lightscene_name, fillInIntent);
        } else {
            mView.setTextViewText(R.id.lightscene_name, ConfigWorkBasket.data.get(instSerial).lightScenes.items.get(position).name);
            mView.setTextColor(R.id.lightscene_name, 0xFF000088);
            mView.setFloat(R.id.lightscene_name, "setTextSize", 16);

            if (ConfigWorkBasket.data.get(instSerial).lightScenes.items.get(position).activ) {
                if (ConfigWorkBasket.data.get(instSerial).lightScenes.itemsCount == 1) {
                    mView.setInt(R.id.lightscene_name, "setBackgroundResource", R.drawable.activeboth);
                } else if (position == 0) {
                    mView.setInt(R.id.lightscene_name, "setBackgroundResource", R.drawable.activefirst);
                } else if (position == ConfigWorkBasket.data.get(instSerial).lightScenes.itemsCount - 1) {
                    mView.setInt(R.id.lightscene_name, "setBackgroundResource", R.drawable.activelast);
                } else {
                    mView.setInt(R.id.lightscene_name, "setBackgroundResource", R.drawable.active);
                }
            } else {
                if (ConfigWorkBasket.data.get(instSerial).lightScenes.itemsCount == 1) {
                    mView.setInt(R.id.lightscene_name, "setBackgroundResource", R.drawable.inactiveboth);
                } else if (position == 0) {
                    mView.setInt(R.id.lightscene_name, "setBackgroundResource", R.drawable.inactivefirst);
                } else if (position == ConfigWorkBasket.data.get(instSerial).lightScenes.itemsCount - 1) {
                    mView.setInt(R.id.lightscene_name, "setBackgroundResource", R.drawable.inactivelast);
                } else {
                    mView.setInt(R.id.lightscene_name, "setBackgroundResource", R.drawable.inactive);
                }
                final Bundle bundle = new Bundle();
                bundle.putString(FHEM_COMMAND, ConfigWorkBasket.data.get(instSerial).lightScenes.activateCmd(position));
                bundle.putString(FHEM_TYPE, "lightscene");
                bundle.putInt(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
                bundle.putString(POS, Integer.toString(position));

                final Intent fillInIntent = new Intent();
                fillInIntent.setAction(SEND_FHEM_COMMAND);
                fillInIntent.putExtras(bundle);
                mView.setOnClickFillInIntent(R.id.lightscene_name, fillInIntent);
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
