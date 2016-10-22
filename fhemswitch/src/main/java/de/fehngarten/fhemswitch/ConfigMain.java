package de.fehngarten.fhemswitch;

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
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.content.pm.ActivityInfo;

import io.socket.emitter.Emitter;
import io.socket.client.Socket;
import io.socket.client.Ack;

import com.mobeta.android.dslv.DragSortListView;

import de.fehngarten.fhemswitch.MyLightScenes.MyLightScene;


import android.util.Log;

public class ConfigMain extends Activity {
    Button getConfigButton;
    Button saveConfigButton;
    Button newCommandButton;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("ConfigMain", "onCreate fired");
        super.onCreate(savedInstanceState);

        mContext = this;
        setResult(RESULT_CANCELED);

        //int height = size.y;
        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        float density = getResources().getDisplayMetrics().density;
        float dpWidth = screenWidth / density;

        if (dpWidth < 596) {
            this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
        setContentView(R.layout.config);

        urlpl = (EditText) findViewById(R.id.urlpl);
        urljs = (EditText) findViewById(R.id.urljs);
        connectionPW = (EditText) findViewById(R.id.connection_pw);

        configDataOnlyIO = new ConfigDataOnlyIO(mContext);
        configDataOnly = configDataOnlyIO.read();

        // Read object using ObjectInputStream

        urljs.setText(configDataOnly.urljs, TextView.BufferType.EDITABLE);
        urlpl.setText(configDataOnly.urlpl, TextView.BufferType.EDITABLE);
        connectionPW.setText(configDataOnly.connectionPW, TextView.BufferType.EDITABLE);

        Button callDonateButton = (Button) findViewById(R.id.callDonateButton);
        callDonateButton.setOnClickListener(callDonateButtonOnClickListener);

        getConfigButton = (Button) findViewById(R.id.get_config);
        getConfigButton.setOnClickListener(getConfigButtonOnClickListener);

        saveConfigButton = (Button) findViewById(R.id.save_config);
        saveConfigButton.setOnClickListener(saveConfigButtonOnClickListener);

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

    private Button.OnClickListener callDonateButtonOnClickListener = new Button.OnClickListener() {
        @Override
        public void onClick(View arg0) {
            Intent donateIntent = new Intent(mContext, Donate.class);
            startActivity(donateIntent);
        }
    };

    private Button.OnClickListener newCommandButtonOnClickListener = new Button.OnClickListener() {
        @Override
        public void onClick(View arg0) {
            configCommandsAdapter.newLine();
            setListViewHeightBasedOnChildren((ListView) findViewById(R.id.commands));
        }
    };

    private Button.OnClickListener getConfigButtonOnClickListener = arg0 -> showFHEMunits();

    private Button.OnClickListener saveConfigButtonOnClickListener = arg0 -> saveConfig();

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

                mySocket = new MySocket(urljs.getText().toString(), mContext);
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

    private Emitter.Listener authListener = args -> runOnUiThread(this::buildOutput
    );

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
        configCommandsAdapter.notifyDataSetChanged();
        setListViewHeightBasedOnChildren((ListView) findViewById(R.id.commands));

        newCommandButton = (Button) findViewById(R.id.newcommandline);
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
                //Log.i("get allSwitches", args[0].toString());
                runOnUiThread(() -> {
                    configSwitchesAdapter.initData((JSONArray) args[0], configData.switches, configData.switchesDisabled);
                    configSwitchesAdapter.notifyDataSetChanged();
                    setListViewHeightBasedOnChildren((ListView) findViewById(R.id.switches));
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
            ArrayList<String> lightscenesFHEM = convertJSONarray((JSONArray) args[0]);

            lsSize = lightscenesFHEM.size();
            for (int i = 0; i < lsSize; i++) {
                final String unit = lightscenesFHEM.get(i);

                mySocket.socket.emit("command", "get " + unit + " scenes", (Ack) args1 -> {
                    //Log.i("get allLightscenes", args[0].toString());
                    lightsceneRowsTemp.add(new ConfigLightsceneRow(unit, unit, false, true, true));
                    ArrayList<String> lightscenesMember = convertJSONarray((JSONArray) args1[0]);
                    for (String unit1 : lightscenesMember) {
                        if (!unit1.equals("") && !unit1.equals("Bye...")) {
                            lightsceneRowsTemp.add(new ConfigLightsceneRow(unit1, unit1, false, false, false));
                        }
                    }
                    lsCounter++;
                    if (lsCounter == lsSize) {
                        runOnUiThread(() -> {
                            configLightscenesAdapter.initData(configData, lightsceneRowsTemp);
                            configLightscenesAdapter.notifyDataSetChanged();
                            setListViewHeightBasedOnChildren((ListView) findViewById(R.id.lightscenes));
                        });
                    }
                });
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
                    configValuesAdapter.notifyDataSetChanged();
                    setListViewHeightBasedOnChildren((ListView) findViewById(R.id.values));
                });
            }
        });
    }

    private void saveConfig() {
        mySocket.socket.close();

        //configDataOnly = new ConfigDataOnly();
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


        Intent resultValue = new Intent();
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
        setResult(RESULT_OK, resultValue);
        Intent updateIntent = new Intent();
        updateIntent.setAction(WidgetService.NEW_CONFIG);
        mContext.sendBroadcast(updateIntent);
        finish();
    }

    public void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) {
            return;
        }

        int totalHeight = listView.getPaddingTop() + listView.getPaddingBottom();
        for (int i = 0; i < listAdapter.getCount(); i++) {
            View listItem = listAdapter.getView(i, null, listView);
            if (listItem instanceof ViewGroup)
                listItem.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
            listItem.measure(0, 0);
            totalHeight += listItem.getMeasuredHeight();
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
    }

    public static ArrayList<String> convertJSONarray(JSONArray jsonArray) {
        ArrayList<String> arrayList = new ArrayList<>();
        for (int i = 0, size = jsonArray.length(); i < size; i++) {
            try {
                arrayList.add(jsonArray.getString(i));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return arrayList;
    }
}
