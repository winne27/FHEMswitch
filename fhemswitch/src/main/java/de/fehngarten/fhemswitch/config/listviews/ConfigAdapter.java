package de.fehngarten.fhemswitch.config.listviews;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;

abstract class ConfigAdapter extends BaseAdapter {

    void dataComplete(ListView listView) {
        this.notifyDataSetChanged();
        setListViewHeightBasedOnChildren(listView);
    }

    void setListViewHeightBasedOnChildren(ListView listView) {

        Integer totalHeight = listView.getPaddingTop() + listView.getPaddingBottom();
        //int desiredWidth = View.MeasureSpec.makeMeasureSpec(listView.getWidth(), View.MeasureSpec.AT_MOST);
        //Integer count = getCount();
        for (int i = 0; i < getCount(); i++) {
            View listItem = getView(i, null, listView);
         //   if (listItem instanceof ViewGroup) {
         //       listItem.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
         //   }
            listItem.measure(0, 0);
            //listItem.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);
            totalHeight += listItem.getMeasuredHeight() + 25;
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (getCount() - 1));
        listView.setLayoutParams(params);
    }
}
