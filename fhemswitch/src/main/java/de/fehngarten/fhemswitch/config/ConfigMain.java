package de.fehngarten.fhemswitch.config;

import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.view.inputmethod.InputMethodManager;
import android.widget.RadioGroup;
import android.widget.RadioButton;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;

import android.widget.ArrayAdapter;
import android.widget.ScrollView;
import android.widget.Spinner;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
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
import android.content.pm.ActivityInfo;

import de.fehngarten.fhemswitch.BuildConfig;
import de.fehngarten.fhemswitch.data.ConfigData;
import de.fehngarten.fhemswitch.data.ConfigDataOnly;
import de.fehngarten.fhemswitch.data.ConfigDataOnlyIO;
import de.fehngarten.fhemswitch.data.MyLightScenes.MyLightScene;
import de.fehngarten.fhemswitch.data.MySocket;
import de.fehngarten.fhemswitch.data.MySwitch;
import de.fehngarten.fhemswitch.data.MyValue;
import de.fehngarten.fhemswitch.data.ConfigLightsceneRow;
import de.fehngarten.fhemswitch.data.ConfigSwitchRow;
import de.fehngarten.fhemswitch.data.ConfigValueRow;

import de.fehngarten.fhemswitch.config.listviews.*;
import de.fehngarten.fhemswitch.widget.WidgetService;
import de.fehngarten.fhemswitch.R;

import de.fehngarten.fhemswitch.modul.GetStoreVersion;
import de.fehngarten.fhemswitch.modul.MyBroadcastReceiver;
import de.fehngarten.fhemswitch.modul.MyReceiveListener;

import io.socket.emitter.Emitter;
import io.socket.client.Socket;
import io.socket.client.Ack;

import com.mobeta.android.dslv.DragSortListView;
import android.util.Log;
import static de.fehngarten.fhemswitch.global.Settings.*;

import static android.appwidget.AppWidgetManager.ACTION_APPWIDGET_UPDATE;

public class ConfigMain extends Activity {
    int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
    private EditText urlpl, urljs, connectionPW;
    public static ConfigData configData;
    private ConfigDataOnly configDataOnly;
    public static MySocket mySocket;

    public ConfigSwitchesAdapter configSwitchesAdapter;
    public ConfigLightscenesAdapter configLightscenesAdapter;
    public ConfigValuesAdapter configValuesAdapter;
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
    public ConfigDataOnlyIO configDataOnlyIO;
    static final String STORE_VERSION_CONFIG = "de.fehngarten.fhemswitch.STORE_VERSION_CONFIG";
    private ArrayList<BroadcastReceiver> broadcastReceivers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (BuildConfig.DEBUG) Log.d("ConfigMain", "onCreate fired");
        super.onCreate(savedInstanceState);

        mContext = this;
        setResult(RESULT_CANCELED);

        //int height = size.y; a
        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        float density = getResources().getDisplayMetrics().density;
        float dpWidth = screenWidth / density;

        if (dpWidth < 600) {
            this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            screenWidth = getResources().getDisplayMetrics().widthPixels;
            density = getResources().getDisplayMetrics().density;
            dpWidth = screenWidth / density;
        }
        if (dpWidth < 600) {
            setContentView(R.layout.config__s);
        } else if (dpWidth < 725) {
            setContentView(R.layout.config__m);
        } else {
            setContentView(R.layout.config__l);
        }

        String installedText = BuildConfig.VERSION_NAME;
        TextView installedView = (TextView) findViewById(R.id.installedView);
        installedView.setText(installedText);

        class OnStoreVersion implements MyReceiveListener {
            public void run(Context context, Intent intent) {
                String latest = intent.getExtras().getString(GetStoreVersion.LATEST);
                if (BuildConfig.DEBUG) Log.d("ConfigMain", "get store version: " + latest);
                TextView latestView = (TextView) findViewById(R.id.latestView);
                latestView.setText(latest);
            }
        }


        broadcastReceivers = new ArrayList<>();

        String[] actions = new String[]{STORE_VERSION_CONFIG};
        broadcastReceivers.add(new MyBroadcastReceiver(this, new OnStoreVersion(), actions));

        new GetStoreVersion(mContext, STORE_VERSION_CONFIG).execute();

        urlpl = (EditText) findViewById(R.id.urlpl);
        urljs = (EditText) findViewById(R.id.urljs);
        connectionPW = (EditText) findViewById(R.id.connection_pw);

        configDataOnlyIO = new ConfigDataOnlyIO(mContext, settingVersionTypes);
        configDataOnly = configDataOnlyIO.read();

        // Read object using ObjectInputStream

        urljs.setText(configDataOnly.urljs, TextView.BufferType.EDITABLE);
        urlpl.setText(configDataOnly.urlpl, TextView.BufferType.EDITABLE);
        connectionPW.setText(configDataOnly.connectionPW, TextView.BufferType.EDITABLE);

        Button callDonateButton = (Button) findViewById(R.id.callDonateButton);
        callDonateButton.setOnClickListener(callDonateButtonOnClickListener);

        Button getConfigButton = (Button) findViewById(R.id.get_config);
        getConfigButton.setOnClickListener(getConfigButtonOnClickListener);

        Button saveConfigButton = (Button) findViewById(R.id.save_config);
        saveConfigButton.setOnClickListener(saveConfigButtonOnClickListener);

        Button cancelConfigButton = (Button) findViewById(R.id.cancel_config);
        cancelConfigButton.setOnClickListener(cancelConfigButtonOnClickListener);

        Button cancel2ConfigButton = (Button) findViewById(R.id.cancel2_config);
        cancel2ConfigButton.setOnClickListener(cancelConfigButtonOnClickListener);

        spinnerSwitchCols = (Spinner) this.findViewById(R.id.config_switch_cols);
        ArrayAdapter<CharSequence> adapterSwitchCols = ArrayAdapter.createFromResource(this, R.array.colnum, R.layout.spinner_item);
        adapterSwitchCols.setDropDownViewResource(R.layout.spinner_dropdown_item);
        spinnerSwitchCols.setAdapter(adapterSwitchCols);
        spinnerSwitchCols.setSelection(configDataOnly.switchCols);

        spinnerValueCols = (Spinner) this.findViewById(R.id.config_value_cols);
        ArrayAdapter<CharSequence> adapterValueCols = ArrayAdapter.createFromResource(this, R.array.colnum, R.layout.spinner_item);
        adapterValueCols.setDropDownViewResource(R.layout.spinner_dropdown_item);
        spinnerValueCols.setAdapter(adapterValueCols);
        spinnerValueCols.setSelection(configDataOnly.valueCols);

        spinnerCommandCols = (Spinner) this.findViewById(R.id.config_command_cols);
        ArrayAdapter<CharSequence> adapterCommandCols = ArrayAdapter.createFromResource(this, R.array.colnum, R.layout.spinner_item);
        adapterCommandCols.setDropDownViewResource(R.layout.spinner_dropdown_item);
        spinnerCommandCols.setAdapter(adapterCommandCols);
        spinnerCommandCols.setSelection(configDataOnly.commandCols);

        configData = new ConfigData();
        if (configDataOnly.switchRows != null) {
            for (ConfigSwitchRow switchRow : configDataOnly.switchRows) {
                if (switchRow.enabled) {
                    configData.switches.add(new MySwitch(switchRow.name, switchRow.unit, switchRow.cmd));
                } else {
                    configData.switchesDisabled.add(new MySwitch(switchRow.name, switchRow.unit, switchRow.cmd));
                }
            }
            Collections.sort(configData.switchesDisabled);
        }

        MyLightScene newLightScene = null;
        if (configDataOnly.lightsceneRows != null) {
            for (ConfigLightsceneRow lightsceneRow : configDataOnly.lightsceneRows) {
                //Log.i("lightscene row",lightsceneRow.isHeader.toString());
                if (lightsceneRow.isHeader) {
                    newLightScene = configData.lightScenes.newLightScene(lightsceneRow.name, lightsceneRow.unit, lightsceneRow.showHeader);
                } else {
                    newLightScene.addMember(lightsceneRow.name, lightsceneRow.unit, lightsceneRow.enabled);
                }
            }
        }

        if (configDataOnly.valueRows != null) {
            for (ConfigValueRow valueRow : configDataOnly.valueRows) {
                if (valueRow.enabled) {
                    configData.values.add(new MyValue(valueRow.name, valueRow.unit));
                } else {
                    configData.valuesDisabled.add(new MyValue(valueRow.name, valueRow.unit));
                }
            }
            Collections.sort(configData.valuesDisabled);
        }

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            mAppWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        radioLayoutLandscape = (RadioGroup) findViewById(R.id.layout_landscape);
        radioLayoutPortrait = (RadioGroup) findViewById(R.id.layout_portrait);

        ((RadioButton) radioLayoutLandscape.getChildAt(configDataOnly.layoutLandscape)).setChecked(true);
        ((RadioButton) radioLayoutPortrait.getChildAt(configDataOnly.layoutPortrait)).setChecked(true);
    }

    @Override
    public void onDestroy() {
        if (BuildConfig.DEBUG) Log.d("ConfigMain", "onDestroy fired");
        if (mySocket != null) {
            mySocket.socket.disconnect();
            mySocket.socket.close();
        }

        for (BroadcastReceiver broadcastReceiver : broadcastReceivers) {
            unregisterReceiver(broadcastReceiver);
        }

        super.onDestroy();
    }

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

    private Button.OnClickListener getConfigButtonOnClickListener = arg0 -> showFHEMunits();

    private Button.OnClickListener saveConfigButtonOnClickListener = arg0 -> saveConfig();

    private Button.OnClickListener cancelConfigButtonOnClickListener = arg0 -> finish();

    private void showFHEMunits() {
        // hide soft keyboard
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }

        try {
            URL url = new URL(urljs.getText().toString());
            url.toURI();
            try {
                String pw = connectionPW.getText().toString();

                mySocket = new MySocket(urljs.getText().toString(), mContext, "Config");
                mySocket.socket.on("authenticated", authListener);

                if (!pw.equals("")) {
                    //Log.i("send pw",pw);
                    mySocket.socket.emit("authentication", pw);
                    waitAuth.postDelayed(runnableWaitAuth, 2000);
                }
            } catch (Exception e) {
                waitAuth.removeCallbacks(runnableWaitAuth);
                sendAlertMessage(getString(R.string.noconn) + ":\n- " + getString(R.string.urlcheck) + "!\n- " + getString(R.string.onlinecheck) + "?\n" + e);
            }

            mySocket.socket.on(Socket.EVENT_CONNECT_ERROR, args -> runOnUiThread(() -> {
                waitAuth.removeCallbacks(runnableWaitAuth);
                sendAlertMessage(getString(R.string.noconn) + ":\n- " + getString(R.string.urlcheck) + ".\n- " + getString(R.string.onlinecheck) + "?");
            }));
        } catch (MalformedURLException | URISyntaxException e) {
            sendAlertMessage(getString(R.string.urlerr) + ":\n " + e);
        }
    }

    private Runnable runnableWaitAuth = new Runnable() {
        @Override
        public void run() {
            sendAlertMessage(getString(R.string.checkpw));
            mySocket.socket.off("authenticated");
            mySocket.socket.close();
            mySocket = null;
        }
    };

    private Emitter.Listener authListener = args -> runOnUiThread(this::buildOutput);

    public void buildOutput() {
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
        ScrollView mainScrollView = (ScrollView) findViewById(R.id.config_scroll_block);
        mainScrollView.fullScroll(ScrollView.FOCUS_UP);
        mainScrollView.smoothScrollTo(0, 0);
    }

    public void sendAlertMessage(final String msg) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
        dialog.setTitle(mContext.getString(R.string.error_header));
        //dialog.setIcon(R.drawable.error_icon);
        dialog.setMessage(msg);
        dialog.setNeutralButton(mContext.getString(R.string.ok), null);
        dialog.create().show();
    }

    private void allCommands() {
        DragSortListView l = (DragSortListView) findViewById(R.id.commands);
        configCommandsAdapter = new ConfigCommandsAdapter(mContext);
        l.setAdapter(configCommandsAdapter);
        ConfigCommandsController c = new ConfigCommandsController(l, configCommandsAdapter, mContext);
        l.setFloatViewManager(c);
        l.setOnTouchListener(c);
        l.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        configCommandsAdapter.initData(configDataOnly.commandRows);
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
                        configSwitchesAdapter.initData((JSONArray) args[0], configData.switches, configData.switchesDisabled);
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
                                configLightscenesAdapter.initData(configData, lightsceneRowsTemp);
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
                    configValuesAdapter.initData((JSONObject) args[0], configData.values, configData.valuesDisabled);
                    configValuesAdapter.dataComplete((ListView) findViewById(R.id.values));
                });
            }
        });
    }

    private void saveConfig() {
        configDataOnly.urljs = urljs.getText().toString();
        configDataOnly.urlpl = urlpl.getText().toString();
        configDataOnly.connectionPW = connectionPW.getText().toString();
        configDataOnly.switchRows = configSwitchesAdapter.getData();
        configDataOnly.lightsceneRows = configLightscenesAdapter.getData();
        configDataOnly.valueRows = configValuesAdapter.getData();
        configDataOnly.commandRows = configCommandsAdapter.getData();
        configDataOnly.switchCols = spinnerSwitchCols.getSelectedItemPosition();
        configDataOnly.valueCols = spinnerValueCols.getSelectedItemPosition();
        configDataOnly.commandCols = spinnerCommandCols.getSelectedItemPosition();

        RadioButton radioLayoutPortraitButton = (RadioButton) findViewById(radioLayoutPortrait.getCheckedRadioButtonId());
        RadioButton radioLayoutLandscapeButton = (RadioButton) findViewById(radioLayoutLandscape.getCheckedRadioButtonId());
        configDataOnly.layoutPortrait = Integer.valueOf(radioLayoutPortraitButton.getTag().toString());
        configDataOnly.layoutLandscape = Integer.valueOf(radioLayoutLandscapeButton.getTag().toString());

        configDataOnlyIO.save(configDataOnly);

        // send Intent "new config" to widget
        Intent resultValue = new Intent();
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
        setResult(RESULT_OK, resultValue);

        Intent restartIntent = new Intent();
        restartIntent.setAction(ACTION_APPWIDGET_UPDATE);
        mContext.sendBroadcast(restartIntent);

        //Intent updateIntent = new Intent();
        //updateIntent.setAction(WidgetService.NEW_CONFIG);
        //mContext.sendBroadcast(updateIntent);

        finish();
    }
}
