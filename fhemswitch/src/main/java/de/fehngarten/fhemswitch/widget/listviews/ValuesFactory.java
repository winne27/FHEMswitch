package de.fehngarten.fhemswitch.widget.listviews;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService.RemoteViewsFactory;

import de.fehngarten.fhemswitch.R;
import static de.fehngarten.fhemswitch.global.Consts.*;
import de.fehngarten.fhemswitch.data.ConfigWorkBasket;
import de.fehngarten.fhemswitch.widget.WidgetService;

//import android.util.Log;

class ValuesFactory implements RemoteViewsFactory {
    //private static final String CLASSNAME = "ValuesFactory.";
    private Context mContext = null;
    private int colnum;
    private int instSerial;

    ValuesFactory(Context context, Intent intent, int colnum) {
        //if (BuildConfig.DEBUG) Log.d(CLASSNAME, "started");
        mContext = context;
        this.colnum = colnum;
        instSerial = intent.getIntExtra(INSTSERIAL, -1);
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

        if (ConfigWorkBasket.data.get(instSerial).valuesCols == null || ConfigWorkBasket.data.get(instSerial).valuesCols.size() <= colnum) {
            return (0);
        } else {
            return ConfigWorkBasket.data.get(instSerial).valuesCols.get(colnum).size();
        }
    }

    @Override
    public RemoteViews getViewAt(int position) {
        //Log.i("values Position: " + position + " of " + values.size(),values.get(position).name);
        RemoteViews mView = new RemoteViews(mContext.getPackageName(), R.layout.value_row);
        if (position >= getCount()) {
            return mView;
        }

        mView.setTextViewText(R.id.value_name, ConfigWorkBasket.data.get(instSerial).valuesCols.get(colnum).get(position).name);
        mView.setTextViewText(R.id.value_value, ConfigWorkBasket.data.get(instSerial).valuesCols.get(colnum).get(position).value);

        if (position == 0) {
            mView.setInt(R.id.value_row, "setBackgroundResource", R.drawable.valuefirst);
        } else if (position == ConfigWorkBasket.data.get(instSerial).valuesCols.get(colnum).size() - 1) {
            mView.setInt(R.id.value_row, "setBackgroundResource", R.drawable.valuelast);
        } else {
            mView.setInt(R.id.value_row, "setBackgroundResource", R.drawable.value);
        }

        final Bundle bundle = new Bundle();
        bundle.putString(FHEM_URI, ConfigWorkBasket.urlFhempl + "?detail=" + ConfigWorkBasket.data.get(instSerial).valuesCols.get(colnum).get(position).unit);

        final Intent fillInIntent = new Intent();
        fillInIntent.setAction(OPEN_FHEM_HOMEPAGE);
        fillInIntent.putExtras(bundle);
        mView.setOnClickFillInIntent(R.id.value_name, fillInIntent);

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
