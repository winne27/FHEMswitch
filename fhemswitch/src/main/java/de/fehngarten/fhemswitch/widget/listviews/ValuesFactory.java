package de.fehngarten.fhemswitch.widget.listviews;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService.RemoteViewsFactory;
import java.util.ArrayList;

import de.fehngarten.fhemswitch.R;

import static android.appwidget.AppWidgetManager.ACTION_APPWIDGET_UPDATE;
import static de.fehngarten.fhemswitch.global.Consts.*;
import static de.fehngarten.fhemswitch.global.Settings.settingDefaultShapes;
import static de.fehngarten.fhemswitch.global.Settings.settingHeaderShapes;

import de.fehngarten.fhemswitch.data.ConfigWorkBasket;
import de.fehngarten.fhemswitch.data.ConfigWorkInstance;
import de.fehngarten.fhemswitch.data.RowValue;
import de.fehngarten.fhemswitch.global.Settings;

class ValuesFactory implements RemoteViewsFactory {
    //private static final String CLASSNAME = "ValuesFactory.";
    private Context mContext = null;
    private int colnum;
    private int instSerial;
    //private final String TAG;
    private ConfigWorkInstance curInstance;

    ValuesFactory(Context context, Intent intent, int colnum) {
        //if (BuildConfig.DEBUG) Log.d(CLASSNAME, "started");
        mContext = context;
        this.colnum = colnum;
        instSerial = intent.getIntExtra(INSTSERIAL, -1);
        curInstance = ConfigWorkBasket.data.get(instSerial);
        //TAG = "ValuesFactory-" + instSerial;
    }

    public void initData() {
        //String methodname = "initData";
    }

    @Override
    public void onCreate() {
        //String methodname = "onCreate";
        //initData();
    }

    @Override
    public void onDataSetChanged() {
        //String methodname = "onDataSetChanged";
        //initData();
    }

    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub

    }

    @Override
    public int getCount() {
        int size = 0;
        try {
            if (curInstance.valuesCols.size() > 0) {
                ArrayList<RowValue> rowValuesCols = curInstance.valuesCols.get(colnum);
                size = rowValuesCols.size();
            }
        } catch (Exception e) {
            size = 0;
        }
        return size;
    }

    @Override
    public RemoteViews getViewAt(int position) {
        //Log.i("values Position: " + position + " of " + values.size(),values.get(position).name);
        RemoteViews mView = null;
        try {
            int count = getCount();
            if (position < count && count > 0) {
                ArrayList<RowValue> rowValuesCols = curInstance.valuesCols.get(colnum);
                RowValue curValue = rowValuesCols.get(position);
                String type = ConfigWorkBasket.data.get(instSerial).myRoundedCorners.getType(VALUES, colnum, position);
                if (curValue.unit.equals(HEADER_SEPERATOR)) {
                    if (curValue.name.equals("")) {
                        mView = new RemoteViews(mContext.getPackageName(), R.layout.seperator);
                    } else {
                        mView = new RemoteViews(mContext.getPackageName(), R.layout.header);
                        mView.setTextViewText(R.id.header_name, curValue.name);
                        mView.setInt(R.id.header, "setBackgroundResource", settingHeaderShapes.get(type));
                        final Intent fillInIntent = new Intent();
                        fillInIntent.setAction(ACTION_APPWIDGET_UPDATE);
                        mView.setOnClickFillInIntent(R.id.header, fillInIntent);

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
                } else {
                    mView = new RemoteViews(mContext.getPackageName(), R.layout.widget_row_value);
                    mView.setTextViewText(R.id.value_name, curValue.name);

                    String value = curValue.value;
                    boolean useIcon = curValue.useIcon;

                    boolean valueIsProz = false;
                    String lastSign = "";
                    String value1 = "";
                    if (value.length() > 0) {
                        lastSign = value.substring(value.length() - 1);
                        value1 = value.substring(0, value.length() - 1);

                        if (lastSign.equals("%")) {
                            try {
                                Double proz = Double.parseDouble(value1);
                                if (proz < 0 || proz > 100) {
                                    valueIsProz = false;
                                } else {
                                    valueIsProz = true;
                                }
                            } catch (Exception e) {
                                valueIsProz = false;
                            }
                        }
                    }

                    if (useIcon && valueIsProz) {
                        int val = Integer.parseInt(value1);
                        val = Math.round(val / 10);
                        mView.setImageViewResource(R.id.prozent_icon, Settings.settingIcons.get("p_" + Integer.toString(val)));
                        mView.setViewVisibility(R.id.prozent_icon, View.VISIBLE);
                        mView.setViewVisibility(R.id.value_icon, View.GONE);
                        mView.setViewVisibility(R.id.value_value, View.GONE);
                        mView.setInt(R.id.header, "setBackgroundResource", settingHeaderShapes.get(type));
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
                    mView.setInt(R.id.value_row, "setBackgroundResource", settingDefaultShapes.get(type));
                }

                final Intent fillInIntent = new Intent();
                fillInIntent.setAction(OPEN_WEBPAGE);


                fillInIntent.putExtra(FHEM_URI, ConfigWorkBasket.urlFhempl + "?detail=" + curValue.unit);
                mView.setOnClickFillInIntent(R.id.value_name, fillInIntent);
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
