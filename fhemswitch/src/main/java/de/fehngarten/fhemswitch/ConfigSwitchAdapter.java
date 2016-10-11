package de.fehngarten.fhemswitch;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
//import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
//import android.util.Log;

class ConfigSwitchAdapter extends BaseAdapter
{
   Context mContext;
   //private int layoutResourceId;
   private ArrayList<ConfigSwitchRow> switchRows = null;

   ConfigSwitchAdapter(Context mContext)
   {
      //super(mContext, layoutResourceId, data);
      //this.layoutResourceId = layoutResourceId;
      this.mContext = mContext;
      switchRows = new ArrayList<>();
   }

   void initData(JSONArray JSONswitches, List<MySwitch> switches, List<MySwitch> switchesDisabled)
   {
      switchRows = new ArrayList<>();
      ArrayList<String> switchesFHEM = ConfigMain.convertJSONarray(JSONswitches);
      ArrayList<String> switchesConfig = new ArrayList<>();
      ArrayList<String> allUnits = new ArrayList<>();

      for (MySwitch mySwitch : switches)
      {
         if (switchesFHEM.contains(mySwitch.unit) && !allUnits.contains(mySwitch.unit))
         {
            switchRows.add(new ConfigSwitchRow(mySwitch.unit, mySwitch.name, true, mySwitch.cmd));
            switchesConfig.add(mySwitch.unit);
            allUnits.add(mySwitch.unit);
         }
      }
      for (MySwitch mySwitch : switchesDisabled)
      {
         if (switchesFHEM.contains(mySwitch.unit) && !allUnits.contains(mySwitch.unit))
         {
            switchRows.add(new ConfigSwitchRow(mySwitch.unit, mySwitch.name, false, mySwitch.cmd));
            switchesConfig.add(mySwitch.unit);
            allUnits.add(mySwitch.unit);
         }
      }
      for (String unit : switchesFHEM)
      {
         if (!switchesConfig.contains(unit) && !allUnits.contains(unit))
         {
            switchRows.add(new ConfigSwitchRow(unit, unit, false, "toggle"));
            allUnits.add(unit);
         }
      }
   }
   
   public ArrayList<ConfigSwitchRow> getData()
   {
      return switchRows;
   }
   
   public int getCount()
   {
      return switchRows.size();
   }

   public ConfigSwitchRow getItem(int position)
   {
      return switchRows.get(position);
   }

   public long getItemId(int position)
   {
      return (long) position;
   }

   public View getView(int position, View convertView, ViewGroup parent)
   {
      View rowView = convertView;
      ConfigSwitchRow switchRow = getItem(position);
      final SwitchHolder switchHolder;

      if (rowView == null)
      {
         switchHolder = new SwitchHolder();
         LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
         rowView = inflater.inflate(R.layout.config_switch_row, parent, false);
         rowView.setTag(switchHolder);
         switchHolder.switch_unit = (TextView) rowView.findViewById(R.id.config_switch_unit);
         //switchHolder.switch_unit = (TextView) rowView.findViewById(R.id.drag_handle);
         switchHolder.switch_name = (EditText) rowView.findViewById(R.id.config_switch_name);
         switchHolder.switch_enabled = (CheckBox) rowView.findViewById(R.id.config_switch_enabled);
         switchHolder.switch_cmd = (Spinner) rowView.findViewById(R.id.config_switch_cmd);
      }
      else
      {
         switchHolder = (SwitchHolder) rowView.getTag();
      }

      switchHolder.ref = position;
      switchHolder.switch_unit.setText(switchRow.unit);
      switchHolder.switch_name.setText(switchRow.name);
      switchHolder.switch_enabled.setChecked(switchRow.enabled);

      switchHolder.switch_cmd.setSelection(getSpinnerIndex(switchHolder.switch_cmd, switchRow.cmd));

      //private method of your class     

      switchHolder.switch_enabled.setOnClickListener(arg0 -> getItem(switchHolder.ref).enabled = switchHolder.switch_enabled.isChecked());

      switchHolder.switch_name.addTextChangedListener(new TextWatcher()
      {
         @Override
         public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3)
         {
            // TODO Auto-generated method stub

         }

         @Override
         public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3)
         {
            // TODO Auto-generated method stub

         }

         @Override
         public void afterTextChanged(Editable arg0)
         {
            // TODO Auto-generated method stub
            getItem(switchHolder.ref).name = arg0.toString();
         }
      });

      switchHolder.switch_cmd.setOnItemSelectedListener(new OnItemSelectedListener()
      {
         @Override
         public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id)
         {
            getItem(switchHolder.ref).cmd = parentView.getItemAtPosition(position).toString();
         }

         @Override
         public void onNothingSelected(AdapterView<?> parentView)
         {
            // your code here
         }

      });

      return rowView;
   }

   void changeItems(int from, int to)
   {
      //Log.i("change switch", Integer.toString(from) + " " + Integer.toString(to));
      final ArrayList<ConfigSwitchRow> switchRowsTemp = new ArrayList<>();
      if (from > to)
      {
         for (int i = 0; i < switchRows.size(); i++)
         {
            if (i < to)
            {
               switchRowsTemp.add(switchRows.get(i));
            }
            else if (i == to)
            {
               switchRowsTemp.add(switchRows.get(from));
            }
            else if (i <= from)
            {
               switchRowsTemp.add(switchRows.get(i - 1));
            }
            else
            {
               switchRowsTemp.add(switchRows.get(i));
            }
         }
      }
      else if (from < to)
      {
         for (int i = 0; i < switchRows.size(); i++)
         {
            if (i < from)
            {
               switchRowsTemp.add(switchRows.get(i));
            }
            else if (i < to)
            {
               switchRowsTemp.add(switchRows.get(i + 1));
            }
            else if (i == to)
            {
               switchRowsTemp.add(switchRows.get(from));
            }
            else
            {
               switchRowsTemp.add(switchRows.get(i));
            }
         }
      }
      if (from != to)
      {
         switchRows = switchRowsTemp;
         notifyDataSetChanged();
      }
   }

   private int getSpinnerIndex(Spinner spinner, String myString)
   {
      int index = 0;

      for (int i = 0; i < spinner.getCount(); i++)
      {
         if (spinner.getItemAtPosition(i).toString().equalsIgnoreCase(myString))
         {
            index = i;
            break;
         }
      }
      return index;
   }

   private class SwitchHolder
   {
      CheckBox switch_enabled;
      TextView switch_unit;
      EditText switch_name;
      Spinner switch_cmd;
      int ref;
   }
}
