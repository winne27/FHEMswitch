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
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import de.fehngarten.fhemswitch.data.RowValue;
import de.fehngarten.fhemswitch.R;
import de.fehngarten.fhemswitch.data.ConfigValueRow;

import static de.fehngarten.fhemswitch.global.Consts.HEADER_SEPERATOR;

class ConfigValuesAdapter extends ConfigAdapter {
    Context mContext;
    private ArrayList<ConfigValueRow> valueRows = null;

    ConfigValuesAdapter(Context mContext) {
        this.mContext = mContext;
        valueRows = new ArrayList<>();
    }

    public void initData(JSONObject obj, List<RowValue> values, List<RowValue> valuesDisabled) {
        valueRows = new ArrayList<>();
        FHEMvalues mFHEMvalues = new FHEMvalues(obj);
        ArrayList<String> valuesConfig = new ArrayList<>();
        ArrayList<String> allUnits = new ArrayList<>();

        for (RowValue rowValue : values) {
            ConfigValueRow FHEMrow = mFHEMvalues.getValue(rowValue.unit);
            if ((FHEMrow != null && !allUnits.contains(rowValue.unit)) || rowValue.unit.equals(HEADER_SEPERATOR)) {
                String value = rowValue.unit.equals(HEADER_SEPERATOR) ? "" : FHEMrow.value;
                valueRows.add(new ConfigValueRow(rowValue.unit, rowValue.name, value, true, rowValue.useIcon));
                valuesConfig.add(rowValue.unit);
                allUnits.add(rowValue.unit);
            }
        }

        for (RowValue rowValue : valuesDisabled) {
            ConfigValueRow FHEMrow = mFHEMvalues.getValue(rowValue.unit);
            if ((FHEMrow != null && !allUnits.contains(rowValue.unit)) || rowValue.unit.equals(HEADER_SEPERATOR)) {
                String value = rowValue.unit.equals(HEADER_SEPERATOR) ? "" : FHEMrow.value;
                valueRows.add(new ConfigValueRow(rowValue.unit, rowValue.name, value, false, rowValue.useIcon));
                valuesConfig.add(rowValue.unit);
                allUnits.add(rowValue.unit);
            }
        }
        for (ConfigValueRow FHEMrow : mFHEMvalues.getAllValues()) {
            if (!valuesConfig.contains(FHEMrow.unit) && !allUnits.contains(FHEMrow.unit)) {
                valueRows.add(new ConfigValueRow(FHEMrow.unit, FHEMrow.name, FHEMrow.value, false, false));
                allUnits.add(FHEMrow.unit);
            }
        }
    }

    void newLine(ListView listView) {
        ArrayList<ConfigValueRow> tempValueRows = new ArrayList<>();
        tempValueRows.add(new ConfigValueRow(HEADER_SEPERATOR, "", "", false, false));
        for (ConfigValueRow valueRow : valueRows) {
            tempValueRows.add(valueRow);
        }
        valueRows.clear();
        notifyDataSetChanged();

        for (ConfigValueRow valueRow : tempValueRows) {
            valueRows.add(valueRow);
        }
        notifyDataSetChanged();
        setListViewHeightBasedOnChildren(listView);
    }

    private void removeItem(int pos) {
        ArrayList<ConfigValueRow> tempValueRows = new ArrayList<>();
        for (ConfigValueRow valueRow : valueRows) {
            tempValueRows.add(valueRow);
        }
        tempValueRows.remove(pos);
        valueRows.clear();
        notifyDataSetChanged();

        for (ConfigValueRow valueRow : tempValueRows) {
            valueRows.add(valueRow);
        }
        notifyDataSetChanged();
        //setListViewHeightBasedOnChildren(listView);
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
        ConfigValueRow valueRow = getItem(position);
        final ValueHolder valueHolder;

        valueHolder = new ValueHolder();
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.config_row_value, parent, false);
        rowView.setTag(valueHolder);
        valueHolder.value_unit = (TextView) rowView.findViewById(R.id.config_value_unit);
        valueHolder.value_name = (EditText) rowView.findViewById(R.id.config_value_name);
        valueHolder.value_value = (TextView) rowView.findViewById(R.id.config_value_value);
        valueHolder.value_enabled = (CheckBox) rowView.findViewById(R.id.config_value_enabled);
        valueHolder.value_useicon = (CheckBox) rowView.findViewById(R.id.config_value_useicon);
        valueHolder.value_remove_button = (Button) rowView.findViewById(R.id.config_value_remove);

        if (valueRow.unit.equals(HEADER_SEPERATOR)) {
            rowView.findViewById(R.id.config_value_value).setVisibility(View.VISIBLE);
            rowView.findViewById(R.id.config_value_useicon).setVisibility(View.GONE);
            rowView.findViewById(R.id.config_value_remove).setVisibility(View.VISIBLE);
            valueHolder.value_remove_button.setOnClickListener(arg0 -> removeItem(valueHolder.ref));
            if (valueRow.name.equals("")) {
                rowView.setBackgroundResource(R.drawable.config_shape_seperator);
                valueHolder.value_unit.setText(R.string.seperator_label);
            } else {
                rowView.setBackgroundResource(R.drawable.config_shape_header);
                valueHolder.value_unit.setText(R.string.header_label);
            }
        } else {
            rowView.findViewById(R.id.config_value_unit).setVisibility(View.VISIBLE);
            rowView.findViewById(R.id.config_value_value).setVisibility(View.VISIBLE);
            rowView.findViewById(R.id.config_value_useicon).setVisibility(View.VISIBLE);
            rowView.findViewById(R.id.config_value_remove).setVisibility(View.GONE);
            rowView.setBackgroundResource(R.drawable.config_shape_default);
            valueHolder.value_unit.setText(valueRow.unit);
        }

        valueHolder.ref = position;
        valueHolder.value_name.setText(valueRow.name);
        valueHolder.value_value.setText(valueRow.value);
        valueHolder.value_enabled.setChecked(valueRow.enabled);
        valueHolder.value_useicon.setChecked(valueRow.useIcon);

        valueHolder.value_enabled.setOnClickListener(arg0 -> {
            ConfigValuesAdapter.this.getItem(valueHolder.ref).enabled = valueHolder.value_enabled.isChecked();
        });

        valueHolder.value_useicon.setOnClickListener(arg0 -> {
            ConfigValuesAdapter.this.getItem(valueHolder.ref).useIcon = valueHolder.value_useicon.isChecked();
        });

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

                if (getItem(valueHolder.ref).unit.equals(HEADER_SEPERATOR)) {
                    //rowView.findViewById(R.id.config_switch_unit).setVisibility(View.GONE);
                    if (valueRow.name.equals("")) {
                        valueHolder.rowView.setBackgroundResource(R.drawable.config_shape_seperator);
                        valueHolder.value_unit.setText(R.string.seperator_label);
                    } else {
                        valueHolder.rowView.setBackgroundResource(R.drawable.config_shape_header);
                        valueHolder.value_unit.setText(R.string.header_label);
                    }
                }

            }
        });
        valueHolder.rowView = rowView;
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
        CheckBox value_useicon;
        TextView value_unit;
        EditText value_name;
        TextView value_value;
        int ref;
        Button value_remove_button;
        View rowView;
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
                    valueRows.add(new ConfigValueRow(unit, unit, value, false, false));
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
