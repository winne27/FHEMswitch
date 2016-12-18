package de.fehngarten.fhemswitch.config;

import android.content.BroadcastReceiver;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.RadioButton;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;

import android.widget.ArrayAdapter;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.ScrollView;
import android.widget.Spinner;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import de.fehngarten.fhemswitch.BuildConfig;
import de.fehngarten.fhemswitch.data.ConfigDataCommon;
import de.fehngarten.fhemswitch.data.ConfigDataIO;
import de.fehngarten.fhemswitch.data.ConfigIntValueRow;
import de.fehngarten.fhemswitch.data.ConfigWorkBasket;
import de.fehngarten.fhemswitch.data.ConfigWorkInstance;
import de.fehngarten.fhemswitch.data.ConfigDataInstance;
import de.fehngarten.fhemswitch.data.MyIntValue;
import de.fehngarten.fhemswitch.data.MyLightScenes.MyLightScene;
import de.fehngarten.fhemswitch.modul.MySocket;
import de.fehngarten.fhemswitch.data.MySwitch;
import de.fehngarten.fhemswitch.data.MyValue;
import de.fehngarten.fhemswitch.data.ConfigLightsceneRow;
import de.fehngarten.fhemswitch.data.ConfigSwitchRow;
import de.fehngarten.fhemswitch.data.ConfigValueRow;

import de.fehngarten.fhemswitch.config.listviews.*;
import de.fehngarten.fhemswitch.R;

import de.fehngarten.fhemswitch.modul.GetStoreVersion;
import de.fehngarten.fhemswitch.modul.MyBroadcastReceiver;
import de.fehngarten.fhemswitch.modul.MyReceiveListener;

import de.fehngarten.fhemswitch.modul.MyWifiInfo;
import de.fehngarten.fhemswitch.modul.SendAlertMessage;
import io.socket.emitter.Emitter;
import io.socket.client.Socket;
import io.socket.client.Ack;

import com.mobeta.android.dslv.DragSortListView;

//import android.util.Log;

import static de.fehngarten.fhemswitch.global.Consts.NEW_CONFIG;
import static de.fehngarten.fhemswitch.global.Consts.SEND_DO_COLOR;
import static de.fehngarten.fhemswitch.global.Consts.STOP_CONFIG;
import static de.fehngarten.fhemswitch.global.Settings.*;

import static android.appwidget.AppWidgetManager.ACTION_APPWIDGET_UPDATE;

public class ConfigMain extends Activity {
    private final String TAG = "ConfigMain";
    int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
    private EditText urlpl, urljs, connectionPW, urlplLocal, urljsLocal;
    private CheckBox isHomeNet;
    private MyWifiInfo myWifiInfo;
    public static ConfigWorkInstance configWorkInstance;
    private ConfigDataCommon configDataCommon;
    private ConfigDataInstance configDataInstance;
    public static MySocket mySocket;

    public ConfigSwitchesAdapter configSwitchesAdapter;
    public ConfigLightscenesAdapter configLightscenesAdapter;
    public ConfigValuesAdapter configValuesAdapter;
    public ConfigIntValuesAdapter configIntValuesAdapter;
    public ConfigCommandsAdapter configCommandsAdapter;
    public int lsCounter = 0;
    public int lsSize = 0;
    public Context mContext;
    public Handler waitAuth = new Handler();
    public Spinner spinnerSwitchCols;
    public Spinner spinnerValueCols;
    public Spinner spinnerCommandCols;
    public RadioGroup radioLayoutLandscape;
    public RadioGroup radioLayoutPortrait;
    public RadioGroup radioWidgetSelector;
    public ConfigDataIO configDataIO;
    static final String STORE_VERSION_CONFIG = "de.fehngarten.fhemswitch.STORE_VERSION_CONFIG";
    private ArrayList<BroadcastReceiver> broadcastReceivers;
    private int instSerial;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //if (BuildConfig.DEBUG) Log.d(TAG, "onCreate fired");
        super.onCreate(savedInstanceState);

        mContext = this;
        setResult(RESULT_CANCELED);

        //int height = size.y; a
        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        float density = getResources().getDisplayMetrics().density;
        float dpWidth = screenWidth / density;
/*
        if (dpWidth < 600) {
            this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            screenWidth = getResources().getDisplayMetrics().widthPixels;
            density = getResources().getDisplayMetrics().density;
            dpWidth = screenWidth / density;
        }
*/
        if (dpWidth < 600) {
            //Log.d(TAG, "config__s");
            setContentView(R.layout.config__s);
        } else if (dpWidth < 725) {
            //Log.d(TAG, "config__m");
            setContentView(R.layout.config__m);
        } else {
            //Log.d(TAG, "config__l");
            setContentView(R.layout.config__l);
        }
/*
        setContentView(R.layout.config);
        LinearLayout config = (LinearLayout) findViewById(R.id.configView);
        ViewGroup.LayoutParams layoutParams = config.getLayoutParams();
        layoutParams.width = Math.round(725 * density);
        config.setLayoutParams(layoutParams);
*/
        String installedText = BuildConfig.VERSION_NAME;
        TextView installedView = (TextView) findViewById(R.id.installedView);
        installedView.setText(installedText);

        class OnStoreVersion implements MyReceiveListener {
            public void run(Context context, Intent intent) {
                String latest = intent.getExtras().getString(GetStoreVersion.LATEST);
                //if (BuildConfig.DEBUG) Log.d("ConfigMain", "latest store version: " + latest);
                TextView latestView = (TextView) findViewById(R.id.latestView);
                latestView.setText(latest);
            }
        }

        class OnStopConfig implements MyReceiveListener {
            public void run(Context context, Intent intent) {
                //Log.d(TAG, "OnStopConfig fired");
                finish();
            }
        }

        broadcastReceivers = new ArrayList<>();

        String[] actions;
        actions = new String[]{STORE_VERSION_CONFIG};
        broadcastReceivers.add(new MyBroadcastReceiver(this, new OnStoreVersion(), actions));

        actions = new String[]{STOP_CONFIG};
        broadcastReceivers.add(new MyBroadcastReceiver(this, new OnStopConfig(), actions));

        new GetStoreVersion(mContext, STORE_VERSION_CONFIG).execute();

        urlpl = (EditText) findViewById(R.id.urlpl);
        urljs = (EditText) findViewById(R.id.urljs);
        urlplLocal = (EditText) findViewById(R.id.urlpl_local);
        urljsLocal = (EditText) findViewById(R.id.urljs_local);
        isHomeNet = (CheckBox) findViewById(R.id.is_home_net);
        TextView isHomeNetLabel = (TextView) findViewById(R.id.is_home_net_label);
        connectionPW = (EditText) findViewById(R.id.connection_pw);

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            mAppWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        } else {
            mAppWidgetId = -1;
        }

        configDataIO = new ConfigDataIO(mContext);
        configDataCommon = configDataIO.readCommon(mAppWidgetId);

        // Read object using ObjectInputStream

        urljs.setText(configDataCommon.urlFhemjs, TextView.BufferType.EDITABLE);
        urljsLocal.setText(configDataCommon.urlFhemjsLocal, TextView.BufferType.EDITABLE);
        urlpl.setText(configDataCommon.urlFhempl, TextView.BufferType.EDITABLE);
        urlplLocal.setText(configDataCommon.urlFhemplLocal, TextView.BufferType.EDITABLE);
        connectionPW.setText(configDataCommon.fhemjsPW, TextView.BufferType.EDITABLE);

        myWifiInfo = new MyWifiInfo(mContext);
        if (myWifiInfo.isWifi()) {
            String isHomeNetText = getString(R.string.is_home_net, myWifiInfo.getWifiName());
            isHomeNetLabel.setText(isHomeNetText);
            isHomeNet.setChecked(myWifiInfo.getWifiId().equals(configDataCommon.bssId));
            findViewById(R.id.is_home_net_row).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.is_home_net_row).setVisibility(View.GONE);
        }

        if (mAppWidgetId > 0 && !ConfigWorkBasket.justMigrated) {
            instSerial = configDataCommon.getFreeInstance(mAppWidgetId);
        } else {
            instSerial = configDataCommon.getFirstInstance();
        }
        ConfigWorkBasket.justMigrated = false;
        //Log.d(TAG, "instSerial: " + instSerial);

        // Migration stuff to 3.0.0
        if (instSerial == 0 && configDataCommon.instances[0] == 0 && mAppWidgetId > 0 && configDataIO.configInstanceExists(0)) {
            configDataCommon.instances[0] = mAppWidgetId;
        }

        if (instSerial < 0) {
            String hint;
            if (mAppWidgetId > 0) {
                hint = getString(R.string.maxWidgetMessage, Integer.toString(settingsMaxInst));
            } else {
                hint = getString(R.string.noWidgetMessage);
            }

            TextView t = (TextView) findViewById(R.id.message);
            t.setText(hint);

            findViewById(R.id.url_block).setVisibility(View.GONE);
            findViewById(R.id.message_block).setVisibility(View.VISIBLE);
        } else if (configDataCommon.getWidgetCount() > 1) {
            findViewById(R.id.widgetSelector).setVisibility(View.VISIBLE);
            int i = 0;
            for (int widgetId : configDataCommon.instances) {
                if (widgetId > 0) {
                    findViewById(settingWidgetSel[i]).setVisibility(View.VISIBLE);
                } else {
                    findViewById(settingWidgetSel[i]).setVisibility(View.GONE);
                }
                i++;
            }
        } else {
            findViewById(R.id.widgetSelector).setVisibility(View.GONE);
        }

        handleButtons();
    }

    private void sendDoColor(boolean setColor) {
        int widgetId;
        for (int i = 0; i < configDataCommon.instances.length; i++) {
            widgetId = configDataCommon.instances[i];
            if (widgetId > 0) {
                Intent intent = new Intent(mContext.getApplicationContext(), settingServiceClasses.get(i));
                intent.setAction(SEND_DO_COLOR);
                intent.putExtra("COLOR", setColor);
                intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
                mContext.startService(intent);
            }
        }
    }

    @Override
    public void onDestroy() {
        //if (BuildConfig.DEBUG) Log.d(TAG, "onDestroy fired");
        if (mySocket != null) {
            mySocket.destroy();
        }

        for (BroadcastReceiver broadcastReceiver : broadcastReceivers) {
            unregisterReceiver(broadcastReceiver);
        }

        sendDoColor(false);

        int widgetId;
        for (int i = 0; i < configDataCommon.instances.length; i++) {
            widgetId = configDataCommon.instances[i];
            if (widgetId > 0) {
                Intent intent = new Intent(mContext.getApplicationContext(), settingServiceClasses.get(i));
                intent.setAction(ACTION_APPWIDGET_UPDATE);
                intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
                mContext.stopService(intent);
                mContext.startService(intent);
            }
        }

        super.onDestroy();
    }


    private void showFHEMunits() {
        // hide soft keyboard
        sendDoColor(true);
        configDataInstance = configDataIO.readInstance(instSerial);

        buildSpinnerRadio();
        radioWidgetSelector.setOnCheckedChangeListener(widgetSelectorChange);
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }

        // open websocket connection

        mySocket = new MySocket(mContext, configDataCommon, "Config");
        mySocket.socket.on("authenticated", authListener);

        mySocket.socket.on(Socket.EVENT_CONNECT_ERROR, args -> runOnUiThread(() -> {
            waitAuth.removeCallbacks(runnableWaitAuth);
            new SendAlertMessage(mContext, getString(R.string.noconnjs) + ":\n- " + getString(R.string.urlcheck) + ".\n- " + getString(R.string.onlinecheck) + "?");
        }));

        mySocket.socket.on("unauthorized", args -> runOnUiThread(() -> {
            new SendAlertMessage(mContext, getString(R.string.checkpw));
        }));

        mySocket.doConnect();
    }

    private void buildSpinnerRadio() {
        ((RadioButton) radioLayoutLandscape.getChildAt(configDataInstance.layoutLandscape)).setChecked(true);
        ((RadioButton) radioLayoutPortrait.getChildAt(configDataInstance.layoutPortrait)).setChecked(true);
        ((RadioButton) radioWidgetSelector.getChildAt(instSerial)).setChecked(true);

        spinnerSwitchCols = (Spinner) this.findViewById(R.id.config_switch_cols);
        ArrayAdapter<CharSequence> adapterSwitchCols = ArrayAdapter.createFromResource(this, R.array.colnum, R.layout.spinner_item);
        adapterSwitchCols.setDropDownViewResource(R.layout.spinner_dropdown_item);
        spinnerSwitchCols.setAdapter(adapterSwitchCols);
        spinnerSwitchCols.setSelection(configDataInstance.switchCols);

        spinnerValueCols = (Spinner) this.findViewById(R.id.config_value_cols);
        ArrayAdapter<CharSequence> adapterValueCols = ArrayAdapter.createFromResource(this, R.array.colnum, R.layout.spinner_item);
        adapterValueCols.setDropDownViewResource(R.layout.spinner_dropdown_item);
        spinnerValueCols.setAdapter(adapterValueCols);
        spinnerValueCols.setSelection(configDataInstance.valueCols);

        spinnerCommandCols = (Spinner) this.findViewById(R.id.config_command_cols);
        ArrayAdapter<CharSequence> adapterCommandCols = ArrayAdapter.createFromResource(this, R.array.colnum, R.layout.spinner_item);
        adapterCommandCols.setDropDownViewResource(R.layout.spinner_dropdown_item);
        spinnerCommandCols.setAdapter(adapterCommandCols);
        spinnerCommandCols.setSelection(configDataInstance.commandCols);
    }

    private void handleButtons() {
        findViewById(R.id.callDonateButton).setOnClickListener(callDonateButtonOnClickListener);
        findViewById(R.id.get_config).setOnClickListener(getConfigButtonOnClickListener);
        findViewById(R.id.save_config).setOnClickListener(saveConfigButtonOnClickListener);
        findViewById(R.id.cancel_config).setOnClickListener(cancelConfigButtonOnClickListener);
        findViewById(R.id.cancel2_config).setOnClickListener(cancelConfigButtonOnClickListener);
        findViewById(R.id.cancel3_config).setOnClickListener(cancelConfigButtonOnClickListener);
        findViewById(R.id.help_layout).setOnClickListener(callHelpOnClickListener);
        findViewById(R.id.help_url).setOnClickListener(callHelpOnClickListener);
        findViewById(R.id.help_icon).setOnClickListener(callHelpIconOnClickListener);
        findViewById(R.id.help_intvalues).setOnClickListener(callHelpIntvaluesOnClickListener);

        radioLayoutLandscape = (RadioGroup) findViewById(R.id.layout_landscape);
        radioLayoutPortrait = (RadioGroup) findViewById(R.id.layout_portrait);
        radioWidgetSelector = (RadioGroup) findViewById(R.id.widgetsel);
    }

    private RadioGroup.OnCheckedChangeListener widgetSelectorChange = new OnCheckedChangeListener() {

        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            View radioButton = group.findViewById(checkedId);
            saveConfig(false);

            Intent intent = new Intent(mContext.getApplicationContext(), settingServiceClasses.get(instSerial));
            intent.setAction(NEW_CONFIG);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, configDataCommon.instances[instSerial]);
            mContext.stopService(intent);
            mContext.startService(intent);

            instSerial = Integer.valueOf(radioButton.getTag().toString());
            showFHEMunits();
        }
    };

    private Button.OnClickListener callDonateButtonOnClickListener = new Button.OnClickListener() {
        @Override
        public void onClick(View arg0) {
            Intent donateIntent = new Intent(mContext, ConfigDonate.class);
            startActivity(donateIntent);
        }
    };

    private Button.OnClickListener newCommandButtonOnClickListener = new Button.OnClickListener() {
        @Override
        public void onClick(View arg0) {
            configCommandsAdapter.newLine();
            configCommandsAdapter.setListViewHeightBasedOnChildren((ListView) findViewById(R.id.commands));
        }
    };

    private Button.OnClickListener getConfigButtonOnClickListener = arg0 -> {
        saveConfigCommon();
        showFHEMunits();
    };

    private Button.OnClickListener callHelpOnClickListener = arg0 -> {
        Intent webIntent = new Intent(Intent.ACTION_VIEW).setData(Uri.parse(settingHelpUrl));
        webIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(webIntent);
    };

    private Button.OnClickListener callHelpIconOnClickListener = arg0 -> {
        Intent webIntent = new Intent(Intent.ACTION_VIEW).setData(Uri.parse(settingHelpIconUrl));
        webIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(webIntent);
    };

    private Button.OnClickListener callHelpIntvaluesOnClickListener = arg0 -> {
        Intent webIntent = new Intent(Intent.ACTION_VIEW).setData(Uri.parse(settingHelpIntvaluesUrl));
        webIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(webIntent);
    };

    private Button.OnClickListener saveConfigButtonOnClickListener = arg0 -> saveConfig(true);

    private Button.OnClickListener cancelConfigButtonOnClickListener = arg0 -> {

        //new ConfigDataIO(mContext).deleteCommon();

        finish();
    };

    private Runnable runnableWaitAuth = new Runnable() {
        @Override
        public void run() {
            new SendAlertMessage(mContext, getString(R.string.checkpw));
            mySocket.socket.off("authenticated");
            mySocket.socket.close();
            mySocket = null;
        }
    };

    private Emitter.Listener authListener = args -> runOnUiThread(this::buildOutput);

    public void buildOutput() {

        configWorkInstance = new ConfigWorkInstance();
        configWorkInstance.init();

        if (configDataInstance.switchRows != null) {
            for (ConfigSwitchRow switchRow : configDataInstance.switchRows) {
                if (switchRow.enabled) {
                    configWorkInstance.switches.add(new MySwitch(switchRow.name, switchRow.unit, switchRow.cmd));
                } else {
                    configWorkInstance.switchesDisabled.add(new MySwitch(switchRow.name, switchRow.unit, switchRow.cmd));
                }
            }
            Collections.sort(configWorkInstance.switchesDisabled);
        }

        MyLightScene newLightScene = null;
        if (configDataInstance.lightsceneRows != null) {
            for (ConfigLightsceneRow lightsceneRow : configDataInstance.lightsceneRows) {
                //Log.i("lightscene row",lightsceneRow.isHeader.toString());
                if (lightsceneRow.isHeader) {
                    newLightScene = configWorkInstance.lightScenes.newLightScene(lightsceneRow.name, lightsceneRow.unit, lightsceneRow.showHeader);
                } else {
                    newLightScene.addMember(lightsceneRow.name, lightsceneRow.unit, lightsceneRow.enabled);
                }
            }
        }

        if (configDataInstance.valueRows != null) {
            for (ConfigValueRow valueRow : configDataInstance.valueRows) {
                Boolean useIcon = false;
                if (valueRow.useIcon != null) {
                    useIcon = valueRow.useIcon;
                }
                if (valueRow.enabled) {
                    configWorkInstance.values.add(new MyValue(valueRow.name, valueRow.unit, useIcon));
                } else {
                    configWorkInstance.valuesDisabled.add(new MyValue(valueRow.name, valueRow.unit, useIcon));
                }
            }
            Collections.sort(configWorkInstance.valuesDisabled);
        }

        if (configDataInstance.intValueRows != null) {
            for (ConfigIntValueRow intValueRow : configDataInstance.intValueRows) {
                MyIntValue myIntValue = new MyIntValue();
                myIntValue.transfer(intValueRow);
                if (intValueRow.enabled) {
                    configWorkInstance.intValues.add(myIntValue);
                } else {
                    configWorkInstance.intValuesDisabled.add(myIntValue);
                }
            }
            Collections.sort(configWorkInstance.intValuesDisabled);
        }

        waitAuth.removeCallbacks(runnableWaitAuth);

        getAllSwitches(mySocket);
        getAllLightscenes(mySocket);
        getAllValues(mySocket);
        allCommands();

        findViewById(R.id.url_block).setVisibility(View.GONE);
        findViewById(R.id.config_layout_block).setVisibility(View.VISIBLE);

        // hide soft keyboard
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }

        // scroll nach oben
        //ScrollView mainScrollView = (ScrollView) findViewById(R.id.config_scroll_block);
        //mainScrollView.fullScroll(ScrollView.FOCUS_UP);
        //mainScrollView.smoothScrollTo(0, 0);
    }

    private void allCommands() {
        DragSortListView l = (DragSortListView) findViewById(R.id.commands);
        configCommandsAdapter = new ConfigCommandsAdapter(mContext);
        l.setAdapter(configCommandsAdapter);
        ConfigCommandsController c = new ConfigCommandsController(l, configCommandsAdapter, mContext);
        l.setFloatViewManager(c);
        l.setOnTouchListener(c);
        l.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        configCommandsAdapter.initData(configDataInstance.commandRows);
        configCommandsAdapter.dataComplete((ListView) findViewById(R.id.commands));

        Button newCommandButton = (Button) findViewById(R.id.newcommandline);
        newCommandButton.setOnClickListener(newCommandButtonOnClickListener);

    }

    private void getAllSwitches(MySocket mySocket) {
        DragSortListView switchesDSLV = (DragSortListView) findViewById(R.id.switches);
        configSwitchesAdapter = new ConfigSwitchesAdapter(this);
        switchesDSLV.setAdapter(configSwitchesAdapter);
        ConfigSwitchesController c = new ConfigSwitchesController(switchesDSLV, configSwitchesAdapter, mContext);
        switchesDSLV.setFloatViewManager(c);
        switchesDSLV.setOnTouchListener(c);
        switchesDSLV.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        // read switches from FHEM server
        mySocket.socket.emit("getAllSwitches", new Ack() {
            @Override
            public void call(Object... args) {
                runOnUiThread(() -> {
                    try {
                        configSwitchesAdapter.initData((JSONArray) args[0], configWorkInstance.switches, configWorkInstance.switchesDisabled);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    configSwitchesAdapter.dataComplete((ListView) findViewById(R.id.switches));
                });
            }
        });
    }

    private void getAllLightscenes(final MySocket mySocket) {
        final ArrayList<ConfigLightsceneRow> lightsceneRowsTemp = new ArrayList<>();

        DragSortListView l = (DragSortListView) findViewById(R.id.lightscenes);
        configLightscenesAdapter = new ConfigLightscenesAdapter(this);
        l.setAdapter(configLightscenesAdapter);
        ConfigLightscenesController c = new ConfigLightscenesController(l, configLightscenesAdapter, mContext);
        l.setFloatViewManager(c);
        lsCounter = 0;

        mySocket.socket.emit("getAllUnitsOf", "LightScene", (Ack) args -> {
            try {
                JSONArray lightscenesFHEM = (JSONArray) args[0];
                lsSize = lightscenesFHEM.length();
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
                            if (!member.equals("") && !member.equals("Bye...")) {
                                lightsceneRowsTemp.add(new ConfigLightsceneRow(member, member, false, false, false));
                            }
                        }
                        lsCounter++;
                        if (lsCounter == lsSize) {
                            runOnUiThread(() -> {
                                configLightscenesAdapter.initData(configWorkInstance, lightsceneRowsTemp);
                                configLightscenesAdapter.dataComplete((ListView) findViewById(R.id.lightscenes));
                            });
                        }
                    });
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });
    }

    private void getAllValues(final MySocket mySocket) {
        DragSortListView l = (DragSortListView) findViewById(R.id.values);
        configValuesAdapter = new ConfigValuesAdapter(this);
        l.setAdapter(configValuesAdapter);
        ConfigValuesController c = new ConfigValuesController(l, configValuesAdapter, mContext);
        l.setFloatViewManager(c);
        l.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        // read values from FHEM server
        mySocket.socket.emit("getAllValues", new Ack() {
            @Override
            public void call(Object... args) {
                runOnUiThread(() -> {
                    initIntValues((JSONObject) args[0]);
                    configValuesAdapter.initData((JSONObject) args[0], configWorkInstance.values, configWorkInstance.valuesDisabled);
                    configValuesAdapter.dataComplete((ListView) findViewById(R.id.values));
                });
            }
        });
    }

    private void initIntValues(JSONObject intValues) {
        DragSortListView l = (DragSortListView) findViewById(R.id.intvalues);
        configIntValuesAdapter = new ConfigIntValuesAdapter(this);
        l.setAdapter(configIntValuesAdapter);
        ConfigIntValuesController c = new ConfigIntValuesController(l, configIntValuesAdapter, mContext);
        l.setFloatViewManager(c);
        l.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        configIntValuesAdapter.initData(intValues, configWorkInstance.intValues, configWorkInstance.intValuesDisabled);
        configIntValuesAdapter.dataComplete((ListView) findViewById(R.id.intvalues));
    }

    private void saveConfig(boolean doFinish) {
        configDataInstance.switchRows = configSwitchesAdapter.getData();
        configDataInstance.lightsceneRows = configLightscenesAdapter.getData();
        configDataInstance.valueRows = configValuesAdapter.getData();
        configDataInstance.intValueRows = configIntValuesAdapter.getData();
        configDataInstance.commandRows = configCommandsAdapter.getData();
        configDataInstance.switchCols = spinnerSwitchCols.getSelectedItemPosition();
        configDataInstance.valueCols = spinnerValueCols.getSelectedItemPosition();
        configDataInstance.commandCols = spinnerCommandCols.getSelectedItemPosition();

        RadioButton radioLayoutPortraitButton = (RadioButton) findViewById(radioLayoutPortrait.getCheckedRadioButtonId());
        RadioButton radioLayoutLandscapeButton = (RadioButton) findViewById(radioLayoutLandscape.getCheckedRadioButtonId());
        configDataInstance.layoutPortrait = Integer.valueOf(radioLayoutPortraitButton.getTag().toString());
        configDataInstance.layoutLandscape = Integer.valueOf(radioLayoutLandscapeButton.getTag().toString());

        configDataIO.saveInstance(configDataInstance, instSerial);

        if (doFinish) {
            Intent resultValue = new Intent();
            resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
            setResult(RESULT_OK, resultValue);
            finish();
        }
    }

    private void saveConfigCommon() {

        // check syntax of url
        String urlString = urljs.getText().toString();
        try {
            URL url = new URL(urlString);
            url.toURI();
        } catch (MalformedURLException | URISyntaxException e) {
            new SendAlertMessage(mContext, getString(R.string.urlerr) + ":\n" + e.getMessage());
            return;
        }

        configDataCommon.urlFhemjs = urljs.getText().toString();
        configDataCommon.urlFhemjsLocal = urljsLocal.getText().toString();
        configDataCommon.urlFhempl = urlpl.getText().toString();
        configDataCommon.urlFhemplLocal = urlplLocal.getText().toString();
        configDataCommon.fhemjsPW = connectionPW.getText().toString();

        if (isHomeNet.isChecked()) {
            configDataCommon.bssId = myWifiInfo.getWifiId();
        } else if (configDataCommon.bssId.equals(myWifiInfo.getWifiId())) {
            configDataCommon.bssId = "";
        }
        configDataIO.saveCommon(configDataCommon);
    }
}
