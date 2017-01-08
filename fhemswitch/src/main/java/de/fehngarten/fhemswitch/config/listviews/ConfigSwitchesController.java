package de.fehngarten.fhemswitch.config.listviews;

import android.content.Context;
import android.graphics.Point;
import android.support.v4.content.ContextCompat;
//import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ScrollView;

import com.mobeta.android.dslv.DragSortController;
import com.mobeta.android.dslv.DragSortListView;

import de.fehngarten.fhemswitch.R;

public class ConfigSwitchesController extends DragSortController {

    ConfigSwitchesAdapter mAdapter;
    Context mContext;
    DragSortListView mDslv;

    public ConfigSwitchesController(DragSortListView dslv, ConfigSwitchesAdapter adapter, Context context) {
        super(dslv, R.id.config_switch_unit, DragSortController.ON_DOWN, 0);
        mAdapter = adapter;
        mContext = context;
        mDslv = dslv;
        mDslv.setDropListener(onDrop);
        setRemoveEnabled(false);
    }

    @Override
    public int startDragPosition(MotionEvent ev) {
        int res = super.dragHandleHitPosition(ev);
        if (res == 0) {
            return DragSortController.MISS;
        } else {
            return res;
        }
    }

    @Override
    public View onCreateFloatView(int position) {
        View v = mAdapter.getView(position, null, mDslv);
        v.setBackgroundColor(ContextCompat.getColor(mContext, R.color.conf_bg_handle_pressed));
        //ScrollView mainScrollView = (ScrollView) mDslv.findViewById(R.id.scrollView);
        //mainScrollView.fullScroll(ScrollView.FOCUS_UP);
        //mainScrollView.scrollTo(0, 0);
        return v;
    }

    @Override
    public void onDragFloatView(View floatView, Point floatPoint, Point touchPoint) {

    }

    @Override
    public void onDestroyFloatView(View floatView) {
        //do nothing; block super from crashing
        //if (BuildConfig.DEBUG) Log.d("ConfigSwitchesControl","******* destroyed");
        //mAdapter.notifyDataSetChanged();
    }

    private DragSortListView.DropListener onDrop = new DragSortListView.DropListener()
    {
        @Override
        public void drop(int from, int to)
        {
            mAdapter.changeItems(from, to);
        }
    };
}