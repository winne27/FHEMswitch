package de.fehngarten.fhemswitch.config.listviews;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
//import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import de.fehngarten.fhemswitch.data.MyValue;
import de.fehngarten.fhemswitch.R;
import de.fehngarten.fhemswitch.data.ConfigValueRow;

public class ConfigValuesAdapter extends ConfigAdapter {
    Context mContext;
    //private int layoutResourceId;
    private ArrayList<ConfigValueRow> valueRows = null;

    public ConfigValuesAdapter(Context mContext) {

        //super(mContext, layoutResourceId, data);
        //this.layoutResourceId = layoutResourceId;
        this.mContext = mContext;
        valueRows = new ArrayList<>();
    }

    public void initData(JSONObject obj, List<MyValue> values, List<MyValue> valuesDisabled) {
        valueRows = new ArrayList<>();
        FHEMvalues mFHEMvalues = new FHEMvalues(obj);
        ArrayList<String> valuesConfig = new ArrayList<>();
        ArrayList<String> allUnits = new ArrayList<>();

        for (MyValue myValue : values) {
            ConfigValueRow FHEMrow = mFHEMvalues.getValue(myValue.unit);
            if (FHEMrow != null && !allUnits.contains(myValue.unit)) {
                valueRows.add(new ConfigValueRow(myValue.unit, myValue.name, FHEMrow.value, true));
                valuesConfig.add(myValue.unit);
                allUnits.add(myValue.unit);
            }
        }

        for (MyValue myValue : valuesDisabled) {
            ConfigValueRow FHEMrow = mFHEMvalues.getValue(myValue.unit);
            if (FHEMrow != null && !allUnits.contains(myValue.unit)) {
                valueRows.add(new ConfigValueRow(myValue.unit, myValue.name, FHEMrow.value, false));
                valuesConfig.add(myValue.unit);
                allUnits.add(myValue.unit);
            }
        }
        for (ConfigValueRow FHEMrow : mFHEMvalues.getAllValues()) {
            if (!valuesConfig.contains(FHEMrow.unit) && !allUnits.contains(FHEMrow.unit)) {
                valueRows.add(new ConfigValueRow(FHEMrow.unit, FHEMrow.name, FHEMrow.value, false));
                allUnits.add(FHEMrow.unit);
            }
        }
    }

    public ArrayList<ConfigValueRow> getData() {
        return valueRows;
    }

    public int getCount() {
        return valueRows.size();
    }

    public ConfigValueRow getItem(int position) {
        return valueRows.get(position);
    }

    public long getItemId(int position) {
        return (long) position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView = convertView;
        ConfigValueRow valueRow = getItem(position);
        final ValueHolder valueHolder;

        if (rowView == null) {
            valueHolder = new ValueHolder();
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            rowView = inflater.inflate(R.layout.config_row_value, parent, false);
            rowView.setTag(valueHolder);
            valueHolder.value_unit = (TextView) rowView.findViewById(R.id.config_value_unit);
            valueHolder.value_name = (EditText) rowView.findViewById(R.id.config_value_name);
            valueHolder.value_value = (TextView) rowView.findViewById(R.id.config_value_value);
            valueHolder.value_enabled = (CheckBox) rowView.findViewById(R.id.config_value_enabled);
        } else {
            valueHolder = (ValueHolder) rowView.getTag();
        }

        valueHolder.ref = position;
        valueHolder.value_unit.setText(valueRow.unit);
        valueHolder.value_name.setText(valueRow.name);
        valueHolder.value_value.setText(valueRow.value);
        valueHolder.value_enabled.setChecked(valueRow.enabled);

        //private method of your class

        valueHolder.value_enabled.setOnClickListener(arg0 -> getItem(valueHolder.ref).enabled = valueHolder.value_enabled.isChecked());

        valueHolder.value_name.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
                // TODO Auto-generated method stub

            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
                // TODO Auto-generated method stub

            }

            @Override
            public void afterTextChanged(Editable arg0) {
                // TODO Auto-generated method stub
                getItem(valueHolder.ref).name = arg0.toString();
            }
        });

        return rowView;
    }

    void changeItems(int from, int to) {
        final ArrayList<ConfigValueRow> valueRowsTemp = new ArrayList<>();
        if (from > to) {
            for (int i = 0; i < valueRows.size(); i++) {
                if (i < to) {
                    valueRowsTemp.add(valueRows.get(i));
                } else if (i == to) {
                    valueRowsTemp.add(valueRows.get(from));
                } else if (i <= from) {
                    valueRowsTemp.add(valueRows.get(i - 1));
                } else {
                    valueRowsTemp.add(valueRows.get(i));
                }
            }
        } else if (from < to) {
            for (int i = 0; i < valueRows.size(); i++) {
                if (i < from) {
                    valueRowsTemp.add(valueRows.get(i));
                } else if (i < to) {
                    valueRowsTemp.add(valueRows.get(i + 1));
                } else if (i == to) {
                    valueRowsTemp.add(valueRows.get(from));
                } else {
                    valueRowsTemp.add(valueRows.get(i));
                }
            }
        }
        if (from != to) {
            valueRows = valueRowsTemp;
            notifyDataSetChanged();
        }
    }

    private class ValueHolder {
        CheckBox value_enabled;
        TextView value_unit;
        EditText value_name;
        TextView value_value;
        int ref;
    }

    private class FHEMvalues {
        private ArrayList<ConfigValueRow> valueRows = new ArrayList<>();

        private FHEMvalues(JSONObject obj) {
            Iterator<String> iterator = obj.keys();
            String unit;
            while (iterator.hasNext()) {
                unit = iterator.next();
                String value;
                try {
                    value = obj.getString(unit);
                    valueRows.add(new ConfigValueRow(unit, unit, value, false));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        ConfigValueRow getValue(String unit) {
            for (ConfigValueRow configValue : valueRows) {
                if (configValue.unit.equals(unit)) {
                    return configValue;
                }
            }
            return null;
        }

        ArrayList<ConfigValueRow> getAllValues() {
            return valueRows;
        }
    }
}
