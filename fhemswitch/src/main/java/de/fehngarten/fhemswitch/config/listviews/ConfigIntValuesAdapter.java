package de.fehngarten.fhemswitch.config.listviews;

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

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import de.fehngarten.fhemswitch.R;
import de.fehngarten.fhemswitch.data.ConfigIntValueRow;
import de.fehngarten.fhemswitch.data.RowIntValue;

import static de.fehngarten.fhemswitch.global.Consts.HEADER_SEPERATOR;

class ConfigIntValuesAdapter extends ConfigAdapter {
    Context mContext;
    private ArrayList<ConfigIntValueRow> intValueRows = null;

    ConfigIntValuesAdapter(Context mContext) {

        //super(mContext, layoutResourceId, data);
        //this.layoutResourceId = layoutResourceId;
        this.mContext = mContext;
        intValueRows = new ArrayList<>();
    }

    public void initData(JSONObject obj, List<RowIntValue> values, List<RowIntValue> valuesDisabled) {
        intValueRows = new ArrayList<>();
        FHEMvalues mFHEMvalues = new FHEMvalues(obj);
        ArrayList<String> valuesConfig = new ArrayList<>();
        ArrayList<String> allUnits = new ArrayList<>();

        for (RowIntValue rowIntValue : values) {

            ConfigIntValueRow FHEMrow = mFHEMvalues.getValue(rowIntValue.unit);
            if ((FHEMrow != null && !allUnits.contains(rowIntValue.unit)) || rowIntValue.unit.equals(HEADER_SEPERATOR)) {
                String intValue = rowIntValue.unit.equals(HEADER_SEPERATOR) ? "" : FHEMrow.value;
                intValueRows.add(new ConfigIntValueRow(rowIntValue.unit, rowIntValue.name, intValue, rowIntValue.setCommand, rowIntValue.stepSize, rowIntValue.commandExecDelay, true));
                valuesConfig.add(rowIntValue.unit);
                allUnits.add(rowIntValue.unit);
            }
        }

        for (RowIntValue rowIntValue : valuesDisabled) {
            ConfigIntValueRow FHEMrow = mFHEMvalues.getValue(rowIntValue.unit);
            if ((FHEMrow != null && !allUnits.contains(rowIntValue.unit)) || rowIntValue.unit.equals(HEADER_SEPERATOR)) {
                String intValue = rowIntValue.unit.equals(HEADER_SEPERATOR) ? "" : FHEMrow.value;
                intValueRows.add(new ConfigIntValueRow(rowIntValue.unit, rowIntValue.name, intValue, rowIntValue.setCommand, rowIntValue.stepSize, rowIntValue.commandExecDelay, false));
                valuesConfig.add(rowIntValue.unit);
                allUnits.add(rowIntValue.unit);
            }
        }
        for (ConfigIntValueRow FHEMrow : mFHEMvalues.getAllValues()) {
            if (!valuesConfig.contains(FHEMrow.unit) && !allUnits.contains(FHEMrow.unit)) {
                intValueRows.add(FHEMrow);
                allUnits.add(FHEMrow.unit);
            }
        }
    }

    public ArrayList<ConfigIntValueRow> getData() {
        return intValueRows;
    }

    public int getCount() {
        return intValueRows.size();
    }

    public ConfigIntValueRow getItem(int position) {
        return intValueRows.get(position);
    }

    public long getItemId(int position) {
        return (long) position;
    }

    void newLine(ListView listView) {
        ArrayList<ConfigIntValueRow> tempIntValueRows = new ArrayList<>();
        tempIntValueRows.add(new ConfigIntValueRow(HEADER_SEPERATOR, "", "", "", (float) 0, 0, false));
        for (ConfigIntValueRow intValueRow : intValueRows) {
            tempIntValueRows.add(intValueRow);
        }
        intValueRows.clear();
        notifyDataSetChanged();

        for (ConfigIntValueRow intValueRow : tempIntValueRows) {
            intValueRows.add(intValueRow);
        }
        notifyDataSetChanged();
        setListViewHeightBasedOnChildren(listView);
    }


    private void removeItem(int pos) {
        ArrayList<ConfigIntValueRow> tempIntValueRows = new ArrayList<>();
        for (ConfigIntValueRow intValueRow : intValueRows) {
            tempIntValueRows.add(intValueRow);
        }
        tempIntValueRows.remove(pos);
        intValueRows.clear();
        notifyDataSetChanged();

        for (ConfigIntValueRow intValueRow : tempIntValueRows) {
            intValueRows.add(intValueRow);
        }
        notifyDataSetChanged();
        //setListViewHeightBasedOnChildren(listView);
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ConfigIntValueRow intValueRow = getItem(position);
        ValueHolder intValueHolder;

        intValueHolder = new ValueHolder();
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.config_row_intvalue, parent, false);
        rowView.setTag(intValueHolder);
        intValueHolder.value_unit = (TextView) rowView.findViewById(R.id.config_intvalue_unit);
        intValueHolder.value_name = (EditText) rowView.findViewById(R.id.config_intvalue_name);
        intValueHolder.value_value = (TextView) rowView.findViewById(R.id.config_intvalue_value);
        intValueHolder.value_enabled = (CheckBox) rowView.findViewById(R.id.config_intvalue_enabled);
        intValueHolder.value_stepSize = (EditText) rowView.findViewById(R.id.config_intvalue_stepsize);
        intValueHolder.value_commandExecDelay = (EditText) rowView.findViewById(R.id.config_intvalue_delay);
        intValueHolder.value_setCommand = (EditText) rowView.findViewById(R.id.config_intvalue_cmd);
        intValueHolder.intvalue_remove_button = (Button) rowView.findViewById(R.id.config_intvalue_remove);

        if (intValueRow.unit.equals(HEADER_SEPERATOR)) {
            rowView.findViewById(R.id.config_intvalue_cmd).setVisibility(View.GONE);
            rowView.findViewById(R.id.config_intvalue_remove).setVisibility(View.VISIBLE);
            intValueHolder.intvalue_remove_button.setOnClickListener(arg0 -> removeItem(intValueHolder.ref));
            setVisible(rowView, false, false);
            if (intValueRow.name.equals("")) {
                rowView.setBackgroundResource(R.drawable.config_shape_seperator);
                intValueHolder.value_unit.setText(R.string.seperator_label);
            } else {
                rowView.setBackgroundResource(R.drawable.config_shape_header);
                intValueHolder.value_unit.setText(R.string.header_label);
            }
        } else {
            rowView.findViewById(R.id.config_intvalue_cmd).setVisibility(View.VISIBLE);
            rowView.findViewById(R.id.config_intvalue_remove).setVisibility(View.GONE);
            setVisible(rowView, intValueRow.enabled, !intValueRow.isTime);
            rowView.setBackgroundResource(R.drawable.config_shape_default);
            intValueHolder.value_unit.setText(intValueRow.unit);
        }

        intValueHolder.ref = position;
        intValueHolder.value_name.setText(intValueRow.name);
        intValueHolder.value_value.setText(intValueRow.value);
        intValueHolder.value_enabled.setChecked(intValueRow.enabled);
        //intValueHolder.value_stepSize.setText(intValueRow.stepSize.toString());
        intValueHolder.value_stepSize.setText(String.format(Locale.getDefault(), "%s", intValueRow.stepSize));
        intValueHolder.value_commandExecDelay.setText(String.format(Locale.getDefault(), "%d", intValueRow.commandExecDelay));
        String setCommandText;
        if (intValueRow.setCommand.equals("")) {
            setCommandText = intValueRow.unit;
        } else {
            setCommandText = intValueRow.setCommand;
        }
        intValueHolder.value_setCommand.setText(setCommandText);

        intValueHolder.value_enabled.setOnClickListener(view -> {
            ConfigIntValueRow intValueRow1 = getItem(intValueHolder.ref);
            intValueRow1.enabled = intValueHolder.value_enabled.isChecked();
            View rootView = view.getRootView();
            setListViewHeightBasedOnChildren((ListView) rootView.findViewById(R.id.intvalues));
            notifyDataSetChanged();
        });

        intValueHolder.value_name.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
            }

            @Override
            public void afterTextChanged(Editable arg0) {
                getItem(intValueHolder.ref).name = arg0.toString();
            }
        });

        intValueHolder.value_setCommand.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
            }

            @Override
            public void afterTextChanged(Editable arg0) {
                getItem(intValueHolder.ref).setCommand = arg0.toString();

                if (getItem(intValueHolder.ref).unit.equals(HEADER_SEPERATOR)) {
                    //rowView.findViewById(R.id.config_switch_unit).setVisibility(View.GONE);
                    if (intValueRow.name.equals("")) {
                        intValueHolder.rowView.setBackgroundResource(R.drawable.config_shape_seperator);
                        intValueHolder.value_unit.setText(R.string.seperator_label);
                    } else {
                        intValueHolder.rowView.setBackgroundResource(R.drawable.config_shape_header);
                        intValueHolder.value_unit.setText(R.string.header_label);
                    }
                }

            }
        });

        intValueHolder.value_stepSize.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
            }

            @Override
            public void afterTextChanged(Editable arg0) {
                getItem(intValueHolder.ref).stepSize = Float.parseFloat(arg0.toString());
            }
        });

        intValueHolder.value_commandExecDelay.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
            }

            @Override
            public void afterTextChanged(Editable arg0) {
                getItem(intValueHolder.ref).commandExecDelay = Integer.valueOf(arg0.toString());
            }
        });
        intValueHolder.rowView = rowView;
        return rowView;
    }

    void changeItems(int from, int to) {
        final ArrayList<ConfigIntValueRow> intValueRowsTemp = new ArrayList<>();
        if (from > to) {
            for (int i = 0; i < intValueRows.size(); i++) {
                if (i < to) {
                    intValueRowsTemp.add(intValueRows.get(i));
                } else if (i == to) {
                    intValueRowsTemp.add(intValueRows.get(from));
                } else if (i <= from) {
                    intValueRowsTemp.add(intValueRows.get(i - 1));
                } else {
                    intValueRowsTemp.add(intValueRows.get(i));
                }
            }
        } else if (from < to) {
            for (int i = 0; i < intValueRows.size(); i++) {
                if (i < from) {
                    intValueRowsTemp.add(intValueRows.get(i));
                } else if (i < to) {
                    intValueRowsTemp.add(intValueRows.get(i + 1));
                } else if (i == to) {
                    intValueRowsTemp.add(intValueRows.get(from));
                } else {
                    intValueRowsTemp.add(intValueRows.get(i));
                }
            }
        }
        if (from != to) {
            intValueRows = intValueRowsTemp;
            notifyDataSetChanged();
        }
    }

    private void setVisible(View rowView, boolean show, boolean showStepsize) {
        int visibility = show ? View.VISIBLE : View.GONE;
        if (showStepsize) {
            rowView.findViewById(R.id.second_row).setVisibility(visibility);
        } else {
            rowView.findViewById(R.id.second_row).setVisibility(View.GONE);
        }
        rowView.findViewById(R.id.seconda_row).setVisibility(visibility);
        rowView.findViewById(R.id.third_row).setVisibility(visibility);
        rowView.findViewById(R.id.thirda_row).setVisibility(visibility);
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
        Button intvalue_remove_button;
        View rowView;
    }

    private class FHEMvalues {
        private ArrayList<ConfigIntValueRow> intValueRows = new ArrayList<>();

        private FHEMvalues(JSONObject obj) {
            Iterator<String> iterator = obj.keys();
            String unit;
            DateFormat df = new SimpleDateFormat("H:m", Locale.GERMAN);
            while (iterator.hasNext()) {
                unit = iterator.next();
                String value;
                try {
                    value = obj.getString(unit);
                    if (isNumeric(value)) {
                        intValueRows.add(new ConfigIntValueRow(unit, unit, value, "", (float) 1.0, 1000, false));
                    } else {
                        try {
                            df.parse(value);
                            intValueRows.add(new ConfigIntValueRow(unit, unit, value, "", (float) 0, 1000, false));
                        } catch (ParseException ignored) {
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        ConfigIntValueRow getValue(String unit) {
            for (ConfigIntValueRow configValue : intValueRows) {
                if (configValue.unit.equals(unit)) {
                    return configValue;
                }
            }
            return null;
        }

        ArrayList<ConfigIntValueRow> getAllValues() {
            return intValueRows;
        }
    }

    private static boolean isNumeric(String inputData) {
        return inputData.matches("[-+]?\\d+(\\.\\d+)?");
    }
}
