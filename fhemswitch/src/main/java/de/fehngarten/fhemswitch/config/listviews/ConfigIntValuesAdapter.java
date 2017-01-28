package de.fehngarten.fhemswitch.config.listviews;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import de.fehngarten.fhemswitch.R;
import de.fehngarten.fhemswitch.data.ConfigIntValueRow;
import de.fehngarten.fhemswitch.data.MyIntValue;

public class ConfigIntValuesAdapter extends ConfigAdapter {
    Context mContext;
    //private int layoutResourceId;
    private ArrayList<ConfigIntValueRow> valueRows = null;

    public ConfigIntValuesAdapter(Context mContext) {

        //super(mContext, layoutResourceId, data);
        //this.layoutResourceId = layoutResourceId;
        this.mContext = mContext;
        valueRows = new ArrayList<>();
    }

    public void initData(JSONObject obj, List<MyIntValue> values, List<MyIntValue> valuesDisabled) {
        valueRows = new ArrayList<>();
        FHEMvalues mFHEMvalues = new FHEMvalues(obj);
        ArrayList<String> valuesConfig = new ArrayList<>();
        ArrayList<String> allUnits = new ArrayList<>();

        for (MyIntValue myIntValue : values) {

            ConfigIntValueRow FHEMrow = mFHEMvalues.getValue(myIntValue.unit);
            if (FHEMrow != null && !allUnits.contains(myIntValue.unit)) {
                valueRows.add(new ConfigIntValueRow(myIntValue.unit, myIntValue.name, FHEMrow.value, myIntValue.setCommand, myIntValue.stepSize, myIntValue.commandExecDelay, true));
                valuesConfig.add(myIntValue.unit);
                allUnits.add(myIntValue.unit);
            }
        }

        for (MyIntValue myIntValue : valuesDisabled) {
            ConfigIntValueRow FHEMrow = mFHEMvalues.getValue(myIntValue.unit);
            if (FHEMrow != null && !allUnits.contains(myIntValue.unit)) {
                valueRows.add(new ConfigIntValueRow(myIntValue.unit, myIntValue.name, FHEMrow.value, myIntValue.setCommand, myIntValue.stepSize, myIntValue.commandExecDelay, false));
                valuesConfig.add(myIntValue.unit);
                allUnits.add(myIntValue.unit);
            }
        }
        for (ConfigIntValueRow FHEMrow : mFHEMvalues.getAllValues()) {
            if (!valuesConfig.contains(FHEMrow.unit) && !allUnits.contains(FHEMrow.unit)) {
                valueRows.add(FHEMrow);
                allUnits.add(FHEMrow.unit);
            }
        }
    }

    public ArrayList<ConfigIntValueRow> getData() {
        return valueRows;
    }

    public int getCount() {
        return valueRows.size();
    }

    public ConfigIntValueRow getItem(int position) {
        return valueRows.get(position);
    }

    public long getItemId(int position) {
        return (long) position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView = convertView;
        ConfigIntValueRow valueRow = getItem(position);
        final ValueHolder valueHolder;

        if (rowView == null) {
            valueHolder = new ValueHolder();
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            rowView = inflater.inflate(R.layout.config_row_intvalue, parent, false);
            rowView.setTag(valueHolder);
            valueHolder.value_unit = (TextView) rowView.findViewById(R.id.config_intvalue_unit);
            valueHolder.value_name = (EditText) rowView.findViewById(R.id.config_intvalue_name);
            valueHolder.value_value = (TextView) rowView.findViewById(R.id.config_intvalue_value);
            valueHolder.value_enabled = (CheckBox) rowView.findViewById(R.id.config_intvalue_enabled);
            valueHolder.value_stepSize = (EditText) rowView.findViewById(R.id.config_intvalue_stepsize);
            valueHolder.value_commandExecDelay = (EditText) rowView.findViewById(R.id.config_intvalue_delay);
            valueHolder.value_setCommand = (EditText) rowView.findViewById(R.id.config_intvalue_cmd);
        } else {
            valueHolder = (ValueHolder) rowView.getTag();
        }

        valueHolder.ref = position;
        valueHolder.value_unit.setText(valueRow.unit);
        valueHolder.value_name.setText(valueRow.name);
        valueHolder.value_value.setText(valueRow.value);
        valueHolder.value_enabled.setChecked(valueRow.enabled);
        valueHolder.value_stepSize.setText(valueRow.stepSize.toString());
        valueHolder.value_commandExecDelay.setText(Integer.toString(valueRow.commandExecDelay));
        valueHolder.value_setCommand.setText(valueRow.setCommand);

        setVisible(rowView, valueRow.enabled);
        //private method of your class

        valueHolder.value_enabled.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                ConfigIntValuesAdapter.this.getItem(valueHolder.ref).enabled = valueHolder.value_enabled.isChecked();
                ConfigIntValuesAdapter.this.notifyDataSetChanged();
            }
        });

        valueHolder.value_name.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
            }

            @Override
            public void afterTextChanged(Editable arg0) {
                getItem(valueHolder.ref).name = arg0.toString();
            }
        });

        valueHolder.value_setCommand.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
            }

            @Override
            public void afterTextChanged(Editable arg0) {
                getItem(valueHolder.ref).setCommand = arg0.toString();
            }
        });

        valueHolder.value_stepSize.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
            }

            @Override
            public void afterTextChanged(Editable arg0) {
                getItem(valueHolder.ref).stepSize = Float.parseFloat(arg0.toString());
            }
        });

        valueHolder.value_commandExecDelay.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
            }

            @Override
            public void afterTextChanged(Editable arg0) {
                getItem(valueHolder.ref).commandExecDelay = Integer.valueOf(arg0.toString());
            }
        });

        return rowView;
    }

    void changeItems(int from, int to) {
        final ArrayList<ConfigIntValueRow> valueRowsTemp = new ArrayList<>();
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

    private void setVisible(View rowView, boolean show) {
        int visibility = show ? View.VISIBLE : View.GONE;
        rowView.findViewById(R.id.second_row).setVisibility(visibility);
        rowView.findViewById(R.id.third_row).setVisibility(visibility);
    }

    private class ValueHolder {
        CheckBox value_enabled;
        TextView value_unit;
        EditText value_name;
        TextView value_value;
        EditText value_stepSize;
        EditText value_commandExecDelay;
        EditText value_setCommand;
        int ref;
    }

    private class FHEMvalues {
        private ArrayList<ConfigIntValueRow> valueRows = new ArrayList<>();

        private FHEMvalues(JSONObject obj) {
            Iterator<String> iterator = obj.keys();
            String unit;
            while (iterator.hasNext()) {
                unit = iterator.next();
                String value;
                try {
                    value = obj.getString(unit);
                    if (isNumeric(value)) {
                        valueRows.add(new ConfigIntValueRow(unit, unit, value, "", (float) 1.0, 1000, false));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        ConfigIntValueRow getValue(String unit) {
            for (ConfigIntValueRow configValue : valueRows) {
                if (configValue.unit.equals(unit)) {
                    return configValue;
                }
            }
            return null;
        }

        ArrayList<ConfigIntValueRow> getAllValues() {
            return valueRows;
        }
    }

    private static boolean isNumeric(String inputData) {
        return inputData.matches("[-+]?\\d+(\\.\\d+)?");
    }
}
