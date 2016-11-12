package de.fehngarten.fhemswitch.widget.listviews;

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
import de.fehngarten.fhemswitch.widget.WidgetService;
//import android.util.Log;

class LightScenesFactory implements RemoteViewsService.RemoteViewsFactory {
    //private static final String CLASSNAME = "LightScenesFactory.";
    private Context mContext = null;

    LightScenesFactory(Context context, Intent intent) {
        //if (BuildConfig.DEBUG) Log.d(CLASSNAME, "started");
        mContext = context;
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
        //if (BuildConfig.DEBUG) Log.d(CLASSNAME + methodname, "lightscenes size: " + Integer.toString(WidgetService.configData.lightScenes.itemsCount));
        if (WidgetService.configData == null || WidgetService.configData.lightScenes == null) {
            return (0);
        } else {
            return WidgetService.configData.lightScenes.itemsCount;
        }
    }

    @Override
    public RemoteViews getViewAt(int position) {
        RemoteViews mView = new RemoteViews(mContext.getPackageName(), R.layout.lightscene_row);
        //Log.i("LightScene Position: " + position + " of " + WidgetService.configData.lightScenes.items.size(),WidgetService.configData.lightScenes.items.get(position).name + " " + WidgetService.configData.lightScenes.items.get(position).unit);
        if (position >= getCount()) {
            return mView;
        }
        if (WidgetService.configData.lightScenes.items.get(position).header) {
            //Log.i("Factory",WidgetService.configData.lightScenes.items.get(position).toString());
            SpannableString s = new SpannableString(WidgetService.configData.lightScenes.items.get(position).name);
            s.setSpan(new StyleSpan(Typeface.BOLD), 0, s.length(), 0);
            mView.setTextViewText(R.id.lightscene_name, s);
            mView.setTextColor(R.id.lightscene_name, 0xFFCCCCCC);
            mView.setFloat(R.id.lightscene_name, "setTextSize", 20);
            if (position == 0) {
                mView.setInt(R.id.lightscene_name, "setBackgroundResource", R.drawable.header);
            } else {
                mView.setInt(R.id.lightscene_name, "setBackgroundResource", R.drawable.header2);
            }

            final Intent fillInIntent = new Intent();
            fillInIntent.setAction(OPEN_FHEM_HOMEPAGE);
            final Bundle bundle = new Bundle();
            bundle.putString(FHEM_URI, WidgetService.fhemUrl + "?detail=" + WidgetService.configData.lightScenes.items.get(position).unit);
            bundle.putString(FHEM_TYPE, "lightScene");
            bundle.putString(POS, Integer.toString(position));
            fillInIntent.putExtras(bundle);
            mView.setOnClickFillInIntent(R.id.lightscene_name, fillInIntent);
        } else {
            mView.setTextViewText(R.id.lightscene_name, WidgetService.configData.lightScenes.items.get(position).name);
            mView.setTextColor(R.id.lightscene_name, 0xFF000088);
            mView.setFloat(R.id.lightscene_name, "setTextSize", 16);

            if (WidgetService.configData.lightScenes.items.get(position).activ) {
                if (WidgetService.configData.lightScenes.itemsCount == 1) {
                    mView.setInt(R.id.lightscene_name, "setBackgroundResource", R.drawable.activeboth);
                } else if (position == 0) {
                    mView.setInt(R.id.lightscene_name, "setBackgroundResource", R.drawable.activefirst);
                } else if (position == WidgetService.configData.lightScenes.itemsCount - 1) {
                    mView.setInt(R.id.lightscene_name, "setBackgroundResource", R.drawable.activelast);
                } else {
                    mView.setInt(R.id.lightscene_name, "setBackgroundResource", R.drawable.active);
                }
            } else {
                if (WidgetService.configData.lightScenes.itemsCount == 1) {
                    mView.setInt(R.id.lightscene_name, "setBackgroundResource", R.drawable.inactiveboth);
                } else if (position == 0) {
                    mView.setInt(R.id.lightscene_name, "setBackgroundResource", R.drawable.inactivefirst);
                } else if (position == WidgetService.configData.lightScenes.itemsCount - 1) {
                    mView.setInt(R.id.lightscene_name, "setBackgroundResource", R.drawable.inactivelast);
                } else {
                    mView.setInt(R.id.lightscene_name, "setBackgroundResource", R.drawable.inactive);
                }
                final Intent fillInIntent = new Intent();
                fillInIntent.setAction(SEND_FHEM_COMMAND);
                final Bundle bundle = new Bundle();
                bundle.putString(FHEM_COMMAND, WidgetService.configData.lightScenes.activateCmd(position));
                bundle.putString(FHEM_TYPE, "lightscene");
                bundle.putString(POS, Integer.toString(position));
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
