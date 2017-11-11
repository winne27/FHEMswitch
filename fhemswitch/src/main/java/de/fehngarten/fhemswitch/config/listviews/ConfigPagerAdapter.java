package de.fehngarten.fhemswitch.config.listviews;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.view.PagerAdapter;
//import android.util.Log;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;

import com.mobeta.android.dslv.DragSortListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;

import de.fehngarten.fhemswitch.R;
import de.fehngarten.fhemswitch.config.ConfigMain;
import de.fehngarten.fhemswitch.data.ConfigIntValueRow;
import de.fehngarten.fhemswitch.data.ConfigLightsceneRow;
import de.fehngarten.fhemswitch.data.ConfigSwitchRow;
import de.fehngarten.fhemswitch.data.ConfigValueRow;
import de.fehngarten.fhemswitch.data.ConfigWorkInstance;
import de.fehngarten.fhemswitch.data.RowIntValue;
import de.fehngarten.fhemswitch.data.RowLightScenes;
import de.fehngarten.fhemswitch.data.RowSwitch;
import de.fehngarten.fhemswitch.data.RowValue;
import de.fehngarten.fhemswitch.modul.MySocket;
import io.socket.client.Ack;

import static de.fehngarten.fhemswitch.config.ConfigMain.configDataInstance;
import static de.fehngarten.fhemswitch.global.Consts.CONFIG_BLOCK_COMMANDS;
import static de.fehngarten.fhemswitch.global.Consts.CONFIG_BLOCK_INTVALUES;
import static de.fehngarten.fhemswitch.global.Consts.CONFIG_BLOCK_LIGHTSCENES;
import static de.fehngarten.fhemswitch.global.Consts.CONFIG_BLOCK_ORIENT;
import static de.fehngarten.fhemswitch.global.Consts.CONFIG_BLOCK_SWITCHES;
import static de.fehngarten.fhemswitch.global.Consts.CONFIG_BLOCK_VALUES;
import static de.fehngarten.fhemswitch.global.Consts.LAYOUT_HORIZONTAL;
import static de.fehngarten.fhemswitch.global.Settings.settingConfigBlocks;
import static de.fehngarten.fhemswitch.global.Settings.settingHelpIconUrl;
import static de.fehngarten.fhemswitch.global.Settings.settingHelpIntvaluesUrl;

public class ConfigPagerAdapter extends PagerAdapter {
    //private final String TAG = "ConfigPagerAdapter";
    private Context mContext;
    private ConfigWorkInstance configWorkInstance;
    private MySocket mySocket;

    private Spinner spinnerSwitchCols;
    private Spinner spinnerValueCols;
    private Spinner spinnerCommandCols;
    private ConfigSwitchesAdapter configSwitchesAdapter;
    private ConfigLightscenesAdapter configLightscenesAdapter;
    private ConfigValuesAdapter configValuesAdapter;
    private ConfigIntValuesAdapter configIntValuesAdapter;
    private ConfigCommandsAdapter configCommandsAdapter;
    private ConfigBlockorderAdapter configBlockorderAdapter;

    private int lsCounter;
    private ArrayList<View> views;

    public ConfigPagerAdapter(Context context, MySocket mySocket, ConfigMain config) {
        configWorkInstance = new ConfigWorkInstance();
        configWorkInstance.init();
        mContext = context;
        this.mySocket = mySocket;
        views = new ArrayList<>();

        LayoutInflater inflater = LayoutInflater.from(mContext); // 1

        boolean connectionLoss = false;
        for (int i = 0; i < getCount(); i++) {
            int resId = settingConfigBlocks[i];
            views.add(i, inflater.inflate(resId, null));
            if (!initView(i)) {
                connectionLoss = true;
                break;
            }
        }
        if (connectionLoss) {
            config.finishCauseOfConnetionLoss();
        } else {
            visibilityColSpinners();
        }
    }

    private boolean initView(int position) {
        try {
            View view = views.get(position);
            switch (position) {
                case CONFIG_BLOCK_ORIENT:
                    RadioGroup radioLayoutLandscape = (RadioGroup) view.findViewById(R.id.layout_landscape);
                    radioLayoutLandscape.setOnCheckedChangeListener(landscapeSelectorChange);
                    RadioGroup radioLayoutPortrait = (RadioGroup) view.findViewById(R.id.layout_portrait);
                    radioLayoutPortrait.setOnCheckedChangeListener(portraitSelectorChange);
                    ((RadioButton) radioLayoutLandscape.getChildAt(configDataInstance.layoutLandscape)).setChecked(true);
                    ((RadioButton) radioLayoutPortrait.getChildAt(configDataInstance.layoutPortrait)).setChecked(true);
                    setupBlockorder(view);
                    break;
                case CONFIG_BLOCK_SWITCHES:
                    spinnerSwitchCols = view.findViewById(R.id.config_switch_cols);
                    ArrayAdapter<CharSequence> adapterSwitchCols = ArrayAdapter.createFromResource(mContext, R.array.colnum, R.layout.spinner_item);
                    adapterSwitchCols.setDropDownViewResource(R.layout.spinner_dropdown_item);
                    spinnerSwitchCols.setAdapter(adapterSwitchCols);
                    spinnerSwitchCols.setSelection(configDataInstance.switchCols);
                    setupSwitches(view);
                    break;
                case CONFIG_BLOCK_LIGHTSCENES:
                    setupLightscenes(view);
                    break;
                case CONFIG_BLOCK_VALUES:
                    view.findViewById(R.id.help_icon).setOnClickListener(helpIconOnClickListener);
                    spinnerValueCols = view.findViewById(R.id.config_value_cols);
                    ArrayAdapter<CharSequence> adapterValueCols = ArrayAdapter.createFromResource(mContext, R.array.colnum, R.layout.spinner_item);
                    adapterValueCols.setDropDownViewResource(R.layout.spinner_dropdown_item);
                    spinnerValueCols.setAdapter(adapterValueCols);
                    spinnerValueCols.setSelection(configDataInstance.valueCols);
                    setupValues(view);
                    break;
                case CONFIG_BLOCK_INTVALUES:
                    view.findViewById(R.id.help_intvalues).setOnClickListener(helpIntvaluesOnClickListener);
                    setupIntValues(view);
                    break;
                case CONFIG_BLOCK_COMMANDS:
                    spinnerCommandCols = view.findViewById(R.id.config_command_cols);
                    ArrayAdapter<CharSequence> adapterCommandCols = ArrayAdapter.createFromResource(mContext, R.array.colnum, R.layout.spinner_item);
                    adapterCommandCols.setDropDownViewResource(R.layout.spinner_dropdown_item);
                    spinnerCommandCols.setAdapter(adapterCommandCols);
                    spinnerCommandCols.setSelection(configDataInstance.commandCols);
                    setupCommands(view);
                    break;
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public int getCount() {
        return settingConfigBlocks.length;
    }

    public Object instantiateItem(ViewGroup collection, int position) {
        View view = views.get(position);
        collection.addView(view, 0);
        return view;
    }

    @Override
    public void destroyItem(ViewGroup collection, int position, Object view) {
        //Log.d(TAG, "destroyed " + position);
        collection.removeView((View) view);
    }

    private RadioGroup.OnCheckedChangeListener landscapeSelectorChange = (group, checkedId) -> {
        configDataInstance.layoutLandscape = Integer.valueOf(group.findViewById(checkedId).getTag().toString());
        Log.i("array length", Integer.toString(views.size()));
        visibilityColSpinners();
    };

    private RadioGroup.OnCheckedChangeListener portraitSelectorChange = (group, checkedId) -> {
        configDataInstance.layoutPortrait = Integer.valueOf(group.findViewById(checkedId).getTag().toString());
        visibilityColSpinners();
    };

    private void visibilityColSpinners() {
        if (views.size() > 1) {
            if (configDataInstance.layoutLandscape == LAYOUT_HORIZONTAL || configDataInstance.layoutPortrait == LAYOUT_HORIZONTAL) {
                views.get(CONFIG_BLOCK_SWITCHES).findViewById(R.id.config_switch_cols).setVisibility(View.VISIBLE);
                views.get(CONFIG_BLOCK_SWITCHES).findViewById(R.id.config_switch_cols_label).setVisibility(View.VISIBLE);
                views.get(CONFIG_BLOCK_VALUES).findViewById(R.id.config_value_cols).setVisibility(View.VISIBLE);
                views.get(CONFIG_BLOCK_VALUES).findViewById(R.id.config_value_cols_label).setVisibility(View.VISIBLE);
                views.get(CONFIG_BLOCK_COMMANDS).findViewById(R.id.config_command_cols).setVisibility(View.VISIBLE);
                views.get(CONFIG_BLOCK_COMMANDS).findViewById(R.id.config_command_cols_label).setVisibility(View.VISIBLE);
            } else {
                views.get(CONFIG_BLOCK_SWITCHES).findViewById(R.id.config_switch_cols).setVisibility(View.GONE);
                views.get(CONFIG_BLOCK_SWITCHES).findViewById(R.id.config_switch_cols_label).setVisibility(View.GONE);
                views.get(CONFIG_BLOCK_VALUES).findViewById(R.id.config_value_cols).setVisibility(View.GONE);
                views.get(CONFIG_BLOCK_VALUES).findViewById(R.id.config_value_cols_label).setVisibility(View.GONE);
                views.get(CONFIG_BLOCK_COMMANDS).findViewById(R.id.config_command_cols).setVisibility(View.GONE);
                views.get(CONFIG_BLOCK_COMMANDS).findViewById(R.id.config_command_cols_label).setVisibility(View.GONE);
            }
        }
    }

    public void saveItem(int position) {
        //Log.d(TAG, position + " try to save");
        try {
            switch (position) {
                case CONFIG_BLOCK_ORIENT:
                    configDataInstance.blockOrder = configBlockorderAdapter.getData();
                    break;
                case CONFIG_BLOCK_SWITCHES:
                    configDataInstance.switchCols = spinnerSwitchCols.getSelectedItemPosition();
                    configDataInstance.switchRows = configSwitchesAdapter.getData();
                    break;
                case CONFIG_BLOCK_LIGHTSCENES:
                    configDataInstance.lightsceneRows = configLightscenesAdapter.getData();
                    break;
                case CONFIG_BLOCK_VALUES:
                    configDataInstance.valueRows = configValuesAdapter.getData();
                    configDataInstance.valueCols = spinnerValueCols.getSelectedItemPosition();
                    break;
                case CONFIG_BLOCK_INTVALUES:
                    configDataInstance.intValueRows = configIntValuesAdapter.getData();
                    break;
                case CONFIG_BLOCK_COMMANDS:
                    configDataInstance.commandRows = configCommandsAdapter.getData();
                    configDataInstance.commandCols = spinnerCommandCols.getSelectedItemPosition();
                    break;
            }
        } catch (Exception e) {
            //Log.e(TAG, position + " does not exist: " + e.getLocalizedMessage());
        }
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    private Button.OnClickListener helpIntvaluesOnClickListener = arg0 -> {
        Intent webIntent = new Intent(Intent.ACTION_VIEW).setData(Uri.parse(settingHelpIntvaluesUrl));
        webIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(webIntent);
    };

    private Button.OnClickListener helpIconOnClickListener = arg0 -> {
        Intent webIntent = new Intent(Intent.ACTION_VIEW).setData(Uri.parse(settingHelpIconUrl));
        webIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(webIntent);
    };

    private Button.OnClickListener newCommandButtonOnClickListener = new Button.OnClickListener() {
        @Override
        public void onClick(View view) {
            configCommandsAdapter.newLine();
            configCommandsAdapter.setListViewHeightBasedOnChildren((ListView) views.get(CONFIG_BLOCK_COMMANDS).findViewById(R.id.commands));
        }
    };

    private Button.OnClickListener newValuesHeaderButtonOnClickListener = new Button.OnClickListener() {
        @Override
        public void onClick(View view) {
            ListView curView = (ListView) views.get(CONFIG_BLOCK_VALUES).findViewById(R.id.values);
            configValuesAdapter.newLine(curView);
        }
    };
    private Button.OnClickListener newIntValuesHeaderButtonOnClickListener = new Button.OnClickListener() {
        @Override
        public void onClick(View view) {
            ListView curView = (ListView) views.get(CONFIG_BLOCK_INTVALUES).findViewById(R.id.intvalues);
            configIntValuesAdapter.newLine(curView);
        }
    };
    private Button.OnClickListener newSwitchesHeaderButtonOnClickListener = new Button.OnClickListener() {
        @Override
        public void onClick(View view) {
            ListView curView = (ListView) views.get(CONFIG_BLOCK_SWITCHES).findViewById(R.id.switches);
            configSwitchesAdapter.newLine(curView);
        }
    };

    private void setupSwitches(View view) {
        if (configDataInstance.switchRows != null) {
            for (ConfigSwitchRow switchRow : configDataInstance.switchRows) {
                if (switchRow.enabled) {
                    configWorkInstance.switches.add(new RowSwitch(switchRow.name, switchRow.unit, switchRow.cmd));
                } else {
                    configWorkInstance.switchesDisabled.add(new RowSwitch(switchRow.name, switchRow.unit, switchRow.cmd));
                }
            }
            Collections.sort(configWorkInstance.switchesDisabled);
        }

        mySocket.socket.emit("getAllSwitches", new Ack() {
            @Override
            public void call(Object... args) {
                ((Activity) mContext).runOnUiThread(() -> {
                    DragSortListView switchesDSLV = (DragSortListView) view.findViewById(R.id.switches);
                    configSwitchesAdapter = new ConfigSwitchesAdapter(mContext);
                    switchesDSLV.setAdapter(configSwitchesAdapter);
                    ConfigSwitchesController c = new ConfigSwitchesController(switchesDSLV, configSwitchesAdapter, mContext);
                    switchesDSLV.setFloatViewManager(c);
                    //switchesDSLV.setOnTouchListener(c);
                    switchesDSLV.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
                    switchesDSLV.setDivider(null);
                    switchesDSLV.setDividerHeight(0);
                    try {
                        configSwitchesAdapter.initData((JSONArray) args[0], configWorkInstance.switches, configWorkInstance.switchesDisabled);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    configSwitchesAdapter.dataComplete((ListView) view.findViewById(R.id.switches));
                });
            }
        });

        Button newSwitchesHeaderButton = (Button) view.findViewById(R.id.newSwitchesHeaderButton);
        newSwitchesHeaderButton.setOnClickListener(newSwitchesHeaderButtonOnClickListener);
    }

    private void setupBlockorder(View view) {
        DragSortListView blockorderDSLV = (DragSortListView) view.findViewById(R.id.blockorder);
        configBlockorderAdapter = new ConfigBlockorderAdapter(mContext);
        blockorderDSLV.setAdapter(configBlockorderAdapter);
        ConfigBlockorderController c = new ConfigBlockorderController(blockorderDSLV, configBlockorderAdapter, mContext);
        blockorderDSLV.setFloatViewManager(c);
        //blockorderDSLV.setOnTouchListener(c);
        blockorderDSLV.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        blockorderDSLV.setDivider(null);
        blockorderDSLV.setDividerHeight(0);
        configBlockorderAdapter.initData(configDataInstance.blockOrder);
        configBlockorderAdapter.dataComplete((ListView) view.findViewById(R.id.blockorder));
    }

    private void setupLightscenes(View view) {
        RowLightScenes.MyLightScene newLightScene = null;
        if (configDataInstance.lightsceneRows != null) {
            for (ConfigLightsceneRow lightsceneRow : configDataInstance.lightsceneRows) {
                //Log.i("lightscene row",lightsceneRow.isHeader.toString());
                if (lightsceneRow.isHeader) {
                    newLightScene = configWorkInstance.lightScenes.newLightScene(lightsceneRow.name, lightsceneRow.unit, lightsceneRow.showHeader);
                } else {
                    if (newLightScene != null) {
                        newLightScene.addMember(lightsceneRow.name, lightsceneRow.unit, lightsceneRow.enabled);
                    }
                }
            }
        }
        final ArrayList<ConfigLightsceneRow> lightsceneRowsTemp = new ArrayList<>();

        DragSortListView l = (DragSortListView) view.findViewById(R.id.lightscenes);
        configLightscenesAdapter = new ConfigLightscenesAdapter(mContext);
        l.setAdapter(configLightscenesAdapter);
        ConfigLightscenesController c = new ConfigLightscenesController(l, configLightscenesAdapter, mContext);
        l.setFloatViewManager(c);

        mySocket.socket.emit("getAllUnitsOf", "LightScene", (Ack) args -> {
            try {
                lsCounter = 0;
                JSONArray lightscenesFHEM = (JSONArray) args[0];
                int lsSize = lightscenesFHEM.length();
                for (int i = 0; i < lsSize; i++) {
                    String unit = lightscenesFHEM.getString(i);
                    mySocket.socket.emit("command", "get " + unit + " scenes", (Ack) args1 -> {
                        lightsceneRowsTemp.add(new ConfigLightsceneRow(unit, unit, false, true, true));
                        JSONArray lightsceneMember = (JSONArray) args1[0];
                        for (int j = 0; j < lightsceneMember.length(); j++) {
                            String member = null;
                            try {
                                member = lightsceneMember.getString(j);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            if (member != null && !member.equals("") && !member.equals("Bye...")) {
                                lightsceneRowsTemp.add(new ConfigLightsceneRow(member, member, false, false, false));
                            }
                        }
                        lsCounter++;
                        if (lsCounter == lsSize) {
                            ((Activity) mContext).runOnUiThread(() -> {
                                configLightscenesAdapter.initData(configWorkInstance, lightsceneRowsTemp);
                                configLightscenesAdapter.dataComplete((ListView) view.findViewById(R.id.lightscenes));
                            });
                        }
                    });
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });
    }

    private void setupValues(View view) {
        if (configDataInstance.valueRows != null) {
            for (ConfigValueRow valueRow : configDataInstance.valueRows) {
                Boolean useIcon = false;
                if (valueRow.useIcon != null) {
                    useIcon = valueRow.useIcon;
                }
                if (valueRow.enabled) {
                    configWorkInstance.values.add(new RowValue(valueRow.name, valueRow.unit, useIcon));
                } else {
                    configWorkInstance.valuesDisabled.add(new RowValue(valueRow.name, valueRow.unit, useIcon));
                }
            }
            Collections.sort(configWorkInstance.valuesDisabled);
        }

        DragSortListView l = (DragSortListView) view.findViewById(R.id.values);
        configValuesAdapter = new ConfigValuesAdapter(mContext);
        l.setAdapter(configValuesAdapter);
        ConfigValuesController c = new ConfigValuesController(l, configValuesAdapter, mContext);
        l.setFloatViewManager(c);
        l.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        l.setDivider(null);
        l.setDividerHeight(0);

        mySocket.socket.emit("getAllValues", new Ack() {
            @Override
            public void call(Object... args) {
                ((Activity) mContext).runOnUiThread(() -> {
                    //initIntValues((JSONObject) args[0]);
                    configValuesAdapter.initData((JSONObject) args[0], configWorkInstance.values, configWorkInstance.valuesDisabled);
                    configValuesAdapter.dataComplete((ListView) view.findViewById(R.id.values));
                });
            }
        });

        Button newValuesHeaderButton = (Button) view.findViewById(R.id.newValuesHeaderButton);
        newValuesHeaderButton.setOnClickListener(newValuesHeaderButtonOnClickListener);
    }

    private void setupIntValues(View view) {
        if (configDataInstance.intValueRows != null) {
            for (ConfigIntValueRow configIntValueRow : configDataInstance.intValueRows) {
                RowIntValue rowIntValue = new RowIntValue();
                rowIntValue.transfer(configIntValueRow);
                if (configIntValueRow.enabled) {
                    configWorkInstance.intValues.add(rowIntValue);
                } else {
                    configWorkInstance.intValuesDisabled.add(rowIntValue);
                }
            }
            Collections.sort(configWorkInstance.intValuesDisabled);
        }
        DragSortListView l = (DragSortListView) view.findViewById(R.id.intvalues);
        configIntValuesAdapter = new ConfigIntValuesAdapter(mContext);
        l.setAdapter(configIntValuesAdapter);
        ConfigIntValuesController c = new ConfigIntValuesController(l, configIntValuesAdapter, mContext);
        l.setFloatViewManager(c);
        l.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        l.setDivider(null);
        l.setDividerHeight(0);
        mySocket.socket.emit("getAllValues", new Ack() {
            @Override
            public void call(Object... args) {
                ((Activity) mContext).runOnUiThread(() -> {
                    //initIntValues((JSONObject) args[0]);
                    configIntValuesAdapter.initData((JSONObject) args[0], configWorkInstance.intValues, configWorkInstance.intValuesDisabled);
                    configIntValuesAdapter.dataComplete((ListView) view.findViewById(R.id.intvalues));
                });
            }
        });

        Button newIntValuesHeaderButton = (Button) view.findViewById(R.id.newIntValuesHeaderButton);
        newIntValuesHeaderButton.setOnClickListener(newIntValuesHeaderButtonOnClickListener);
    }

    private void setupCommands(View view) {
        DragSortListView l = (DragSortListView) view.findViewById(R.id.commands);
        configCommandsAdapter = new ConfigCommandsAdapter(mContext);
        l.setAdapter(configCommandsAdapter);
        ConfigCommandsController c = new ConfigCommandsController(l, configCommandsAdapter, mContext);
        l.setFloatViewManager(c);
        //l.setOnTouchListener(c);
        l.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        l.setDivider(null);
        l.setDividerHeight(0);
        configCommandsAdapter.initData(configDataInstance.commandRows);
        configCommandsAdapter.dataComplete((ListView) view.findViewById(R.id.commands));

        Button newCommandButton = (Button) view.findViewById(R.id.newcommandline);
        newCommandButton.setOnClickListener(newCommandButtonOnClickListener);
    }
}