package de.fehngarten.fhemswitch.config.listviews;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;

import de.fehngarten.fhemswitch.R;
import de.fehngarten.fhemswitch.data.ConfigCommandRow;

class ConfigCommandsAdapter extends ConfigAdapter
{
   private Context mContext;
   private ArrayList<ConfigCommandRow> commandRows = null;

   ConfigCommandsAdapter(Context mContext)
   {
      this.mContext = mContext;
      commandRows = new ArrayList<>();
   }

   public void initData(List<ConfigCommandRow> commands)
   {
      commandRows = (ArrayList<ConfigCommandRow>) commands;
      //commandRows.add(new ConfigCommandRow("","",false));
      notifyDataSetChanged();
   }

   void newLine()
   {
      commandRows.add(new ConfigCommandRow());
      notifyDataSetChanged();
   }

   public ArrayList<ConfigCommandRow> getData()
   {
      ArrayList<ConfigCommandRow> commandRowsTemp = new ArrayList<>();

      for (ConfigCommandRow commandRow : commandRows)
      {
         if (!commandRow.name.equals(""))
         {
            commandRowsTemp.add(commandRow);
         }
      }
      return commandRowsTemp;
   }
   
   public int getCount()
   {
      return commandRows.size();
   }

   public ConfigCommandRow getItem(int position)
   {
      return commandRows.get(position);
   }

   public long getItemId(int position)
   {
      return (long) position;
   }

   public View getView(int position, View convertView, ViewGroup parent)
   {
      //if (BuildConfig.DEBUG) Log.d("command pos",Integer.toString(position) + " from " + Integer.toString(getCount()));
      View rowView = convertView;
      ConfigCommandRow commandRow = getItem(position);
      final CommandHolder commandHolder;

      if (rowView == null)
      {
         commandHolder = new CommandHolder();
         LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
         rowView = inflater.inflate(R.layout.config_row_command, parent, false);
         rowView.setTag(commandHolder);
         commandHolder.command_name = (EditText) rowView.findViewById(R.id.config_command_name);
         commandHolder.command_command = (EditText) rowView.findViewById(R.id.config_command_command);
         commandHolder.command_enabled = (CheckBox) rowView.findViewById(R.id.config_command_enabled);
      }
      else
      {
         commandHolder = (CommandHolder) rowView.getTag();
      }

      commandHolder.ref = position;
      commandHolder.command_name.setText(commandRow.name);
      commandHolder.command_command.setText(commandRow.command);
      commandHolder.command_enabled.setChecked(commandRow.enabled);

      //private method of your class     

      commandHolder.command_enabled.setOnClickListener((View arg0) -> getItem(commandHolder.ref).enabled = commandHolder.command_enabled.isChecked());

      commandHolder.command_name.addTextChangedListener(new TextWatcher()
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
            getItem(commandHolder.ref).name = arg0.toString();
         }
      });
      commandHolder.command_command.addTextChangedListener(new TextWatcher()
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
            getItem(commandHolder.ref).command = arg0.toString();
         }
      });

      return rowView;
   }

   public void changeItems(int from, int to)
   {
      final ArrayList<ConfigCommandRow> commandRowsTemp = new ArrayList<>();
      if (from > to)
      {
         for (int i = 0; i < commandRows.size(); i++)
         {
            if (i < to)
            {
               commandRowsTemp.add(commandRows.get(i));
            }
            else if (i == to)
            {
               commandRowsTemp.add(commandRows.get(from));
            }
            else if (i <= from)
            {
               commandRowsTemp.add(commandRows.get(i - 1));
            }
            else
            {
               commandRowsTemp.add(commandRows.get(i));
            }
         }
      }
      else if (from < to)
      {
         for (int i = 0; i < commandRows.size(); i++)
         {
            if (i < from)
            {
               commandRowsTemp.add(commandRows.get(i));
            }
            else if (i < to)
            {
               commandRowsTemp.add(commandRows.get(i + 1));
            }
            else if (i == to)
            {
               commandRowsTemp.add(commandRows.get(from));
            }
            else
            {
               commandRowsTemp.add(commandRows.get(i));
            }
         }
      }
      if (from != to)
      {
         commandRows = commandRowsTemp;
         notifyDataSetChanged();
      }
   }

   private class CommandHolder
   {
      CheckBox command_enabled;
      EditText command_name;
      EditText command_command;
      int ref;
   }
}
