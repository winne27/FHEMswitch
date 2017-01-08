package de.fehngarten.fhemswitch.config.listviews;

import java.util.ArrayList;

import de.fehngarten.fhemswitch.data.ConfigWorkInstance;
import de.fehngarten.fhemswitch.data.MyLightScenes.MyLightScene;
import de.fehngarten.fhemswitch.R;
import de.fehngarten.fhemswitch.data.ConfigLightsceneRow;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
//import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.support.v4.content.ContextCompat;

import android.util.Log;

public class ConfigLightscenesAdapter extends ConfigAdapter {
    Context mContext;
    private static ArrayList<ConfigLightsceneRow> lightsceneRows;

    public ConfigLightscenesAdapter(Context mContext) {
        //super(mContext, layoutResourceId, data);
        this.mContext = mContext;
        lightsceneRows = new ArrayList<>();
    }

    public void initData(ConfigWorkInstance configWorkInstance, ArrayList<ConfigLightsceneRow> FHEMlightsceneRows) {
        lightsceneRows = new ArrayList<>();
        for (MyLightScene lightScene : configWorkInstance.lightScenes.lightScenes) {
            if (isInFhem(FHEMlightsceneRows, lightScene.unit, true)) {
                lightsceneRows.add(new ConfigLightsceneRow(lightScene.unit, lightScene.name, false, true, lightScene.showHeader));
                for (MyLightScene.Member member : lightScene.members) {
                    if (isInFhem(FHEMlightsceneRows, member.unit, false)) {
                        lightsceneRows.add(new ConfigLightsceneRow(member.unit, member.name, member.enabled, false, false));
                    }
                }
                // check for new members
                Boolean isCurrent = false;
                for (ConfigLightsceneRow lightsceneRow : FHEMlightsceneRows) {
                    if (lightsceneRow.isHeader) {
                        isCurrent = lightsceneRow.unit.equals(lightScene.unit);
                    } else if (isCurrent) {
                        if (!lightsceneRow.unit.equals("") && !lightScene.isMember(lightsceneRow.unit)) {
                            lightsceneRows.add(new ConfigLightsceneRow(lightsceneRow.unit, lightsceneRow.unit, false, false, false));
                        }
                    }
                }
            }
        }

        // check for new lightscenes
        Boolean isNew = false;
        for (ConfigLightsceneRow lightsceneRow : FHEMlightsceneRows) {
            if (lightsceneRow.isHeader) {
                if (!lightsceneRow.unit.equals("") && !configWorkInstance.lightScenes.isLightScene(lightsceneRow.unit)) {
                    isNew = true;
                    lightsceneRows.add(new ConfigLightsceneRow(lightsceneRow.unit, lightsceneRow.unit, false, true, true));
                } else {
                    isNew = false;
                }
            } else if (isNew) {
                lightsceneRows.add(new ConfigLightsceneRow(lightsceneRow.unit, lightsceneRow.unit, false, false, false));
            }
        }
    }

    private Boolean isInFhem(ArrayList<ConfigLightsceneRow> FHEMlightsceneRows, String unit, Boolean isHeader) {
        for (ConfigLightsceneRow configLightsceneRow : FHEMlightsceneRows) {
            if (configLightsceneRow.unit.equals(unit) && configLightsceneRow.isHeader == isHeader) {
                return true;
            }
        }
        return false;
    }

    public ArrayList<ConfigLightsceneRow> getData() {
        return lightsceneRows;
    }

    public int getCount() {
        return lightsceneRows.size();
    }

    public ConfigLightsceneRow getItem(int position) {
        return lightsceneRows.get(position);
    }

    public long getItemId(int position) {
        return (long) position;
    }

    Boolean isDragable(int res) {
        //if (BuildConfig.DEBUG) Log.d("isDragable res", Integer.toString(res));
        return !getItem(res).isHeader;
    }

    int[] getBounds(int pos) {
        int[] bounds = new int[2];

        int startPos = 1;
        int endPos = getCount() - 1;

        for (int i = 1; i < getCount(); i++) {
            if (getItem(i).isHeader) {
                if (i < pos) {
                    startPos = i + 1;
                } else {
                    endPos = i - 1;
                    break;
                }
            }
        }

        bounds[0] = startPos;
        bounds[1] = endPos;
        return bounds;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView = convertView;
        ConfigLightsceneRow lightsceneRow = getItem(position);
        final LightsceneHolder lightsceneHolder;

        if (rowView == null) {
            lightsceneHolder = new LightsceneHolder();
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            rowView = inflater.inflate(R.layout.config_row_lightscene, parent, false);
            rowView.setTag(lightsceneHolder);
            lightsceneHolder.lightscene_unit = (TextView) rowView.findViewById(R.id.config_lightscene_unit);
            lightsceneHolder.lightscene_name = (EditText) rowView.findViewById(R.id.config_lightscene_name);
            lightsceneHolder.lightscene_enabled = (CheckBox) rowView.findViewById(R.id.config_lightscene_enabled);
            lightsceneHolder.lightscene_header = (CheckBox) rowView.findViewById(R.id.config_lightscene_header);
        } else {
            lightsceneHolder = (LightsceneHolder) rowView.getTag();
        }

        lightsceneHolder.ref = position;
        lightsceneHolder.lightscene_unit.setText(lightsceneRow.unit);
        lightsceneHolder.lightscene_name.setText(lightsceneRow.name);
        if (lightsceneRow.isHeader) {
            lightsceneHolder.lightscene_enabled.setVisibility(View.INVISIBLE);
            lightsceneHolder.lightscene_header.setVisibility(View.VISIBLE);
            lightsceneHolder.lightscene_header.setChecked(lightsceneRow.showHeader);
            //rowView.setBackgroundColor(mContext.getResources().getColor(R.color.conf_bg_header_3));
            rowView.setBackgroundColor(ContextCompat.getColor(mContext, R.color.conf_bg_header_3));

        } else {
            lightsceneHolder.lightscene_enabled.setChecked(lightsceneRow.enabled);
        }

        //private method of your class

        lightsceneHolder.lightscene_enabled.setOnClickListener(arg0 -> getItem(lightsceneHolder.ref).enabled = lightsceneHolder.lightscene_enabled.isChecked());

        lightsceneHolder.lightscene_header.setOnClickListener(arg0 -> {
            //Log.i("isChecked",Boolean.toString(lightsceneHolder.lightscene_header.isChecked()));
            getItem(lightsceneHolder.ref).showHeader = lightsceneHolder.lightscene_header.isChecked();
        });

        lightsceneHolder.lightscene_name.addTextChangedListener(new TextWatcher() {
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
                getItem(lightsceneHolder.ref).name = arg0.toString();
            }
        });

        return rowView;
    }

    void changeItems(int from, int to) {
        Log.i("change switch", Integer.toString(from) + " " + Integer.toString(to));
        final ArrayList<ConfigLightsceneRow> lightsceneRowsTemp = new ArrayList<>();
        if (from > to) {
            for (int i = 0; i < lightsceneRows.size(); i++) {
                if (i < to) {
                    lightsceneRowsTemp.add(lightsceneRows.get(i));
                } else if (i == to) {
                    lightsceneRowsTemp.add(lightsceneRows.get(from));
                } else if (i <= from) {
                    lightsceneRowsTemp.add(lightsceneRows.get(i - 1));
                } else {
                    lightsceneRowsTemp.add(lightsceneRows.get(i));
                }
            }
        } else if (from < to) {
            for (int i = 0; i < lightsceneRows.size(); i++) {
                if (i < from) {
                    lightsceneRowsTemp.add(lightsceneRows.get(i));
                } else if (i < to) {
                    lightsceneRowsTemp.add(lightsceneRows.get(i + 1));
                } else if (i == to) {
                    lightsceneRowsTemp.add(lightsceneRows.get(from));
                } else {
                    lightsceneRowsTemp.add(lightsceneRows.get(i));
                }
            }
        }
        if (from != to) {
            lightsceneRows = lightsceneRowsTemp;
            notifyDataSetChanged();
        }
    }

    private class LightsceneHolder {
        CheckBox lightscene_header;
        CheckBox lightscene_enabled;
        TextView lightscene_unit;
        EditText lightscene_name;
        int ref;
    }
}
