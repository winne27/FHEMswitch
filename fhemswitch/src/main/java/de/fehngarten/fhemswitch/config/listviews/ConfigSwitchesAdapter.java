package de.fehngarten.fhemswitch.config.listviews;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;

import android.content.Context;
import android.graphics.Color;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import de.fehngarten.fhemswitch.data.RowSwitch;
import de.fehngarten.fhemswitch.R;
import de.fehngarten.fhemswitch.data.ConfigSwitchRow;

import static de.fehngarten.fhemswitch.global.Consts.HEADER_SEPERATOR;

class ConfigSwitchesAdapter extends ConfigAdapter {
    Context mContext;
    private ArrayList<ConfigSwitchRow> switchRows = null;

    ConfigSwitchesAdapter(Context mContext) {
        this.mContext = mContext;
        switchRows = new ArrayList<>();
    }

    public void initData(JSONArray JSONswitches, List<RowSwitch> switches, List<RowSwitch> switchesDisabled) throws JSONException {
        switchRows = new ArrayList<>();
        ArrayList<String> switchesFHEM = new ArrayList<>();
        for (int j = 0; j < JSONswitches.length(); j++) {
            switchesFHEM.add(JSONswitches.getString(j));
        }

        ArrayList<String> switchesConfig = new ArrayList<>();
        ArrayList<String> allUnits = new ArrayList<>();

        //switchRows.add(new ConfigSwitchRow(mContext.getString(R.string.unit), mContext.getString(R.string.name), false, mContext.getString(R.string.command)));
        for (RowSwitch rowSwitch : switches) {
            if ((switchesFHEM.contains(rowSwitch.unit) && !allUnits.contains(rowSwitch.unit)) || rowSwitch.unit.equals(HEADER_SEPERATOR)) {
                switchRows.add(new ConfigSwitchRow(rowSwitch.unit, rowSwitch.name, true, rowSwitch.cmd, rowSwitch.confirm));
                switchesConfig.add(rowSwitch.unit);
                allUnits.add(rowSwitch.unit);
            }
        }
        for (RowSwitch rowSwitch : switchesDisabled) {
            if ((switchesFHEM.contains(rowSwitch.unit) && !allUnits.contains(rowSwitch.unit)) || rowSwitch.unit.equals(HEADER_SEPERATOR)) {
                switchRows.add(new ConfigSwitchRow(rowSwitch.unit, rowSwitch.name, false, rowSwitch.cmd, rowSwitch.confirm));
                switchesConfig.add(rowSwitch.unit);
                allUnits.add(rowSwitch.unit);
            }
        }
        for (String unit : switchesFHEM) {
            if (!switchesConfig.contains(unit) && !allUnits.contains(unit)) {
                switchRows.add(new ConfigSwitchRow(unit, unit, false, "toggle", 0));
                allUnits.add(unit);
            }
        }
    }

    public ArrayList<ConfigSwitchRow> getData() {
        return switchRows;
    }

    public int getCount() {
        return switchRows.size();
    }

    void newLine(ListView listView) {
        ArrayList<ConfigSwitchRow> tempSwitchRows = new ArrayList<>();
        tempSwitchRows.add(new ConfigSwitchRow(HEADER_SEPERATOR, "", false, "", 0));
        for (ConfigSwitchRow switchRow : switchRows) {
            tempSwitchRows.add(switchRow);
        }

        switchRows.clear();

        for (ConfigSwitchRow switchRow : tempSwitchRows) {
            switchRows.add(switchRow);
        }
        notifyDataSetChanged();
        setListViewHeightBasedOnChildren(listView);
    }

    private void removeItem(int pos) {
        ArrayList<ConfigSwitchRow> tempSwitchRows = new ArrayList<>();
        for (ConfigSwitchRow switchRow : switchRows) {
            tempSwitchRows.add(switchRow);
        }
        tempSwitchRows.remove(pos);
        switchRows.clear();
        notifyDataSetChanged();

        for (ConfigSwitchRow switchRow : tempSwitchRows) {
            switchRows.add(switchRow);
        }
        notifyDataSetChanged();
    }

    public ConfigSwitchRow getItem(int position) {
        return switchRows.get(position);
    }

    public long getItemId(int position) {
        return (long) position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        //View rowView = convertView;
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        final SwitchHolder switchHolder;
        ConfigSwitchRow switchRow = getItem(position);

        switchHolder = new SwitchHolder();
        View rowView = inflater.inflate(R.layout.config_row_switch, parent, false);
        rowView.setTag(switchHolder);
        switchHolder.switch_unit = (TextView) rowView.findViewById(R.id.config_switch_unit);
        switchHolder.switch_name = (EditText) rowView.findViewById(R.id.config_switch_name);
        switchHolder.switch_enabled = (CheckBox) rowView.findViewById(R.id.config_switch_enabled);
        switchHolder.switch_cmd = (Spinner) rowView.findViewById(R.id.config_switch_cmd);
        switchHolder.switch_confirm = (Spinner) rowView.findViewById(R.id.config_switch_confirm);
        switchHolder.switch_remove_button = (Button) rowView.findViewById(R.id.config_switch_remove);
        switchHolder.ref = position;

        String[] items = this.mContext.getResources().getStringArray(R.array.confirmTypes);
        ConfigConfirmAdapter configConfirmSpinnerArrayAdapter = new ConfigConfirmAdapter(this.mContext, items, switchHolder.switch_confirm);
        switchHolder.switch_confirm.setAdapter(configConfirmSpinnerArrayAdapter);


        if (switchRow.unit.equals(HEADER_SEPERATOR)) {
            //rowView.findViewById(R.id.config_switch_unit).setVisibility(View.GONE);
            rowView.findViewById(R.id.config_switch_cmd).setVisibility(View.GONE);
            rowView.findViewById(R.id.config_switch_confirm).setVisibility(View.GONE);
            rowView.findViewById(R.id.config_switch_remove).setVisibility(View.VISIBLE);
            switchHolder.switch_remove_button.setOnClickListener(arg0 -> {
                removeItem(switchHolder.ref);
            });
            if (switchRow.name.equals("")) {
                rowView.setBackgroundResource(R.drawable.config_shape_seperator);
                switchHolder.switch_unit.setText(R.string.seperator_label);
            } else {
                rowView.setBackgroundResource(R.drawable.config_shape_header);
                switchHolder.switch_unit.setText(R.string.header_label);
            }
        } else {
            rowView.findViewById(R.id.config_switch_cmd).setVisibility(View.VISIBLE);
            rowView.findViewById(R.id.config_switch_remove).setVisibility(View.GONE);
            rowView.setBackgroundResource(R.drawable.config_shape_default);
            switchHolder.switch_unit.setText(switchRow.unit);
            switchHolder.switch_cmd.setSelection(getSpinnerIndex(switchHolder.switch_cmd, switchRow.cmd));
            switchHolder.switch_confirm.setSelection(switchRow.confirm, true);
            String prompt = mContext.getString(R.string.confirmsPrompt, switchRow.unit);
            switchHolder.switch_confirm.setPrompt(prompt);
        }

        switchHolder.switch_name.setText(switchRow.name);
        switchHolder.switch_enabled.setChecked(switchRow.enabled);

        switchHolder.switch_enabled.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                ConfigSwitchesAdapter.this.getItem(switchHolder.ref).enabled = switchHolder.switch_enabled.isChecked();
            }
        });

        switchHolder.switch_name.addTextChangedListener(new TextWatcher() {
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
                getItem(switchHolder.ref).name = arg0.toString();

                if (getItem(switchHolder.ref).unit.equals(HEADER_SEPERATOR)) {
                    //rowView.findViewById(R.id.config_switch_unit).setVisibility(View.GONE);
                    if (switchRow.name.equals("")) {
                        switchHolder.rowView.setBackgroundResource(R.drawable.config_shape_seperator);
                        switchHolder.switch_unit.setText(R.string.seperator_label);
                    } else {
                        switchHolder.rowView.setBackgroundResource(R.drawable.config_shape_header);
                        switchHolder.switch_unit.setText(R.string.header_label);
                    }
                }
            }
        });

        switchHolder.switch_cmd.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int selPos, long id) {
                getItem(switchHolder.ref).cmd = parentView.getItemAtPosition(selPos).toString();
                //((TextView) parentView.getChildAt(0)).setTextColor(Color.BLUE);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }
        });
        switchHolder.switch_confirm.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int selPos, long id) {
                getItem(switchHolder.ref).confirm = selPos;
                //((TextView) parentView.getChildAt(0)).setTextColor(Color.BLUE);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }
        });

        switchHolder.rowView = rowView;
        return rowView;
    }

    void changeItems(int from, int to) {
        //Log.i("change switch", Integer.toString(from) + " " + Integer.toString(to));
        final ArrayList<ConfigSwitchRow> switchRowsTemp = new ArrayList<>();
        if (from > to) {
            for (int i = 0; i < switchRows.size(); i++) {
                if (i < to) {
                    switchRowsTemp.add(switchRows.get(i));
                } else if (i == to) {
                    switchRowsTemp.add(switchRows.get(from));
                } else if (i <= from) {
                    switchRowsTemp.add(switchRows.get(i - 1));
                } else {
                    switchRowsTemp.add(switchRows.get(i));
                }
            }
        } else if (from < to) {
            for (int i = 0; i < switchRows.size(); i++) {
                if (i < from) {
                    switchRowsTemp.add(switchRows.get(i));
                } else if (i < to) {
                    switchRowsTemp.add(switchRows.get(i + 1));
                } else if (i == to) {
                    switchRowsTemp.add(switchRows.get(from));
                } else {
                    switchRowsTemp.add(switchRows.get(i));
                }
            }
        }
        if (from != to) {
            switchRows = switchRowsTemp;
            notifyDataSetChanged();
        }
    }

    private int getSpinnerIndex(Spinner spinner, String myString) {
        int index = 0;

        for (int i = 0; i < spinner.getCount(); i++) {
            if (spinner.getItemAtPosition(i).toString().equalsIgnoreCase(myString)) {
                index = i;
                break;
            }
        }
        return index;
    }

    private class SwitchHolder {
        CheckBox switch_enabled;
        TextView switch_unit;
        EditText switch_name;
        Spinner switch_cmd;
        Spinner switch_confirm;
        int ref;
        Button switch_remove_button;
        View rowView;
    }
}
