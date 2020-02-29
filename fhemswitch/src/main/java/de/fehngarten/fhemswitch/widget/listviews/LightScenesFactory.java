package de.fehngarten.fhemswitch.widget.listviews;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import androidx.core.content.ContextCompat;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;
import de.fehngarten.fhemswitch.R;

import static android.appwidget.AppWidgetManager.ACTION_APPWIDGET_UPDATE;
import static de.fehngarten.fhemswitch.global.Consts.*;
import static de.fehngarten.fhemswitch.global.Settings.settingActiveShapes;
import static de.fehngarten.fhemswitch.global.Settings.settingDefaultShapes;
import static de.fehngarten.fhemswitch.global.Settings.settingHeaderShapes;

import de.fehngarten.fhemswitch.data.ConfigWorkBasket;
import de.fehngarten.fhemswitch.data.ConfigWorkInstance;

class LightScenesFactory implements RemoteViewsService.RemoteViewsFactory {
    private Context mContext = null;
    private int instSerial;
    private int widgetId;
    private ConfigWorkInstance curInstance;

    LightScenesFactory(Context context, Intent intent) {
        mContext = context;
        instSerial = intent.getIntExtra(INSTSERIAL, -1);
        widgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1);
        curInstance = ConfigWorkBasket.data.get(instSerial);
    }

    public void initData() {
        //String methodname = "initData";
    }

    @Override
    public void onCreate() {
        //String methodname = "onCreate";
    }

    @Override
    public void onDataSetChanged() {
        //String methodname = "onDataSetChanged";
    }

    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
    }

    @Override
    public int getCount() {
        int size;
        try {
            size = curInstance.lightScenes.items.size();
        } catch (Exception e) {
            size = 0;
        }
        return size;
    }

    @Override
    public RemoteViews getViewAt(int position) {
        RemoteViews mView = null;
        //Log.i("LightScene Position: " + position + " of " + ConfigDataCage.data.get(instSerial).lightScenes.items.size(),ConfigDataCage.data.get(instSerial).lightScenes.items.get(position).name + " " + ConfigDataCage.data.get(instSerial).lightScenes.items.get(position).unit);
        try {
            int count = getCount();
            if (position < count && count > 0) {
                mView = new RemoteViews(mContext.getPackageName(), R.layout.widget_row_lightscene);
                String type = ConfigWorkBasket.data.get(instSerial).myRoundedCorners.getType(LIGHTSCENES, 0, position);
                if (curInstance.lightScenes.items.get(position).header) {
                    //Log.i("Factory",ConfigDataCage.data.get(instSerial).lightScenes.items.get(position).toString());
                    SpannableString s = new SpannableString(curInstance.lightScenes.items.get(position).name);
                    s.setSpan(new StyleSpan(Typeface.BOLD), 0, s.length(), 0);
                    mView.setTextViewText(R.id.lightscene_name, s);
                    mView.setTextColor(R.id.lightscene_name, ContextCompat.getColor(mContext, R.color.widget_header_color));
                    mView.setInt(R.id.lightscene_row, "setBackgroundResource", settingHeaderShapes.get(type));

                    final Intent fillInIntent = new Intent();
                    fillInIntent.setAction(ACTION_APPWIDGET_UPDATE);
                    mView.setOnClickFillInIntent(R.id.lightscene_row, fillInIntent);
                } else {
                    mView.setTextViewText(R.id.lightscene_name, curInstance.lightScenes.items.get(position).name);
                    mView.setTextColor(R.id.lightscene_name, 0xFF000088);
                    mView.setFloat(R.id.lightscene_name, "setTextSize", 16);

                    if (curInstance.lightScenes.items.get(position).activ) {
                        mView.setInt(R.id.lightscene_row, "setBackgroundResource", settingActiveShapes.get(type));
                    } else {
                        mView.setInt(R.id.lightscene_row, "setBackgroundResource", settingDefaultShapes.get(type));
                        final Bundle bundle = new Bundle();
                        bundle.putString(FHEM_COMMAND, curInstance.lightScenes.activateCmd(position));
                        bundle.putString(FHEM_TYPE, "lightscene");
                        bundle.putInt(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
                        bundle.putInt(INSTSERIAL, instSerial);
                        bundle.putString(POS, Integer.toString(position));

                        final Intent fillInIntent = new Intent();
                        fillInIntent.setAction(SEND_FHEM_COMMAND);
                        fillInIntent.putExtras(bundle);
                        mView.setOnClickFillInIntent(R.id.lightscene_name, fillInIntent);

                        if (type.equals("first")) {
                            final Intent fhemIntent = new Intent();
                            fhemIntent.setAction(OPEN_WEBPAGE);
                            fhemIntent.putExtra(FHEM_URI, ConfigWorkBasket.urlFhempl + "?room=all");
                            mView.setOnClickFillInIntent(R.id.fhemhome, fhemIntent);
                            mView.setViewVisibility(R.id.fhemhome, View.VISIBLE);
                        } else {
                            mView.setViewVisibility(R.id.fhemhome, View.GONE);
                        }
                    }
                }
            }
        } catch (Exception e) {
            Log.e("getViewAt", e.getMessage());
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
