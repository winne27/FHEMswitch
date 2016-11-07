package de.fehngarten.fhemswitch.config.listviews;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.MotionEvent;
import android.view.View;

import com.mobeta.android.dslv.DragSortController;
import com.mobeta.android.dslv.DragSortListView;

import de.fehngarten.fhemswitch.R;

public class ConfigCommandsController extends DragSortController {

    ConfigCommandsAdapter mAdapter;
    Context mContext;
    DragSortListView mDslv;

    public ConfigCommandsController(DragSortListView dslv, ConfigCommandsAdapter adapter, Context context) {
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
        return v;
    }

    @Override
    public void onDestroyFloatView(View floatView) {
        //do nothing; block super from crashing
        //Log.d("ConfigCommandsContro","******* destroyed");
        //mAdapter.notifyDataSetChanged();
    }

    private DragSortListView.DropListener onDrop = new DragSortListView.DropListener() {
        @Override
        public void drop(int from, int to) {
            mAdapter.changeItems(from, to);
        }
    };
}