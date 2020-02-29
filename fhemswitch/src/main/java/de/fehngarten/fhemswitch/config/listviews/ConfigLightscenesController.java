package de.fehngarten.fhemswitch.config.listviews;

import android.content.Context;
import android.graphics.Point;
import androidx.core.content.ContextCompat;
import android.view.MotionEvent;
import android.view.View;

import com.mobeta.android.dslv.DragSortController;
import com.mobeta.android.dslv.DragSortListView;

import de.fehngarten.fhemswitch.R;

class ConfigLightscenesController extends DragSortController {
    private int mPos;
    private Boolean curViewIsHeader = false;
    private DragSortListView mDslv;
    Context mContext;
    private ConfigLightscenesAdapter mAdapter;

    ConfigLightscenesController(DragSortListView dslv, ConfigLightscenesAdapter adapter, Context context) {
        super(dslv, R.id.config_lightscene_unit, DragSortController.ON_DOWN, 0);
        setRemoveEnabled(false);
        mDslv = dslv;
        mContext = context;
        mAdapter = adapter;
        DragSortListView.DropListener onDrop = (from, to) -> mAdapter.changeItems(from, to);
        mDslv.setDropListener(onDrop);
        mAdapter = adapter;
    }

    @Override
    public int startDragPosition(MotionEvent ev) {
        int res = super.startDragPosition(ev);

        if (res < 0) {
            return DragSortController.MISS;
        }

        if (mAdapter.isDragable(res)) {
            return res;
        } else {
            return DragSortController.MISS;
        }
    }

    @Override
    public View onCreateFloatView(int position) {
        mPos = position;
        curViewIsHeader = mAdapter.getItem(position).isHeader;

        View v = mAdapter.getView(position, null, mDslv);
        if (!curViewIsHeader) {
            //v.setBackgroundColor(mContext.getResources().getColor(R.color.conf_bg_handle_pressed));
            v.setBackgroundColor(ContextCompat.getColor(mContext, R.color.conf_bg_handle_pressed));
        }
        return v;
    }

    private int origHeight = -1;

    @Override
    public void onDragFloatView(View floatView, Point floatPoint, Point touchPoint) {
        //final int first = mDslv.getFirstVisiblePosition();
        //final int lvDivHeight = mDslv.getDividerHeight();

        if (origHeight == -1) {
            origHeight = floatView.getHeight();
        }

        if (curViewIsHeader) {
            floatPoint.y = mDslv.getChildAt(mPos).getTop();
        } else {
            int[] bounds = mAdapter.getBounds(mPos);

            View top = mDslv.getChildAt(bounds[0]);
            View bottom = mDslv.getChildAt(bounds[1]);

            final int limitTop = top.getTop();
            final int limitBottom = bottom.getBottom() - floatView.getHeight();

            if (floatPoint.y < limitTop) {
                floatPoint.y = limitTop;
            } else if (floatPoint.y > limitBottom) {
                floatPoint.y = limitBottom;
            }
        }
    }

    @Override
    public void onDestroyFloatView(View floatView) {
        //do nothing; block super from crashing
        //if (BuildConfig.DEBUG) Log.d("ConfigLightscenesContro","******* destroyed");
        //mAdapter.notifyDataSetChanged();
    }

}
