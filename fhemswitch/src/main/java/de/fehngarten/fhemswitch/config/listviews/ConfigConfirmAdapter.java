package de.fehngarten.fhemswitch.config.listviews;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.List;

import de.fehngarten.fhemswitch.R;

import static android.graphics.Color.GREEN;

public class ConfigConfirmAdapter extends ArrayAdapter<String> {

        private String[] items;
        private Activity activity;
        private Spinner spinner;
        private LayoutInflater inflater;
        private Integer[] icons = {R.drawable.confirm_no, R.drawable.confirm_off, R.drawable.confirm_on, R.drawable.confirm_full};
        private int selPos;

        public ConfigConfirmAdapter(Context context, String[] items, Spinner spinner) {
            super(context, android.R.layout.simple_list_item_1, items);
            this.items = items;
            this.activity = activity;
            this.spinner = spinner;
            this.inflater = LayoutInflater.from(context);
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            //TextView v = (TextView) super.getView(position, convertView, parent);

            View view = inflater.inflate(R.layout.config_confirm_layout, null);
            ImageView icon = (ImageView) view.findViewById(R.id.confirmIcon);
            TextView names = (TextView) view.findViewById(R.id.confirmText);
            icon.setImageResource(icons[position]);
            names.setText(items[position]);
            if (position == selPos) {
                view.setBackgroundResource(R.drawable.config_shape_header);
            }
            return view;
        }

        @Override
        public String getItem(int position) {
            return items[position];
        }

        @Override
        public View getView(int position, View view, ViewGroup parent) {
            view = inflater.inflate(R.layout.config_confirm_layout, null);
            ImageView icon = (ImageView) view.findViewById(R.id.confirmIcon);
            TextView names = (TextView) view.findViewById(R.id.confirmText);
            icon.setVisibility(View.GONE);
            names.setVisibility(View.GONE);
            this.spinner.setBackgroundResource(icons[position]);
            selPos = position;
            return view;
        }
    }