package de.fehngarten.fhemswitch.config;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v13.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.view.menu.MenuBuilder;
import android.support.v7.view.menu.MenuPopupHelper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.ScrollView;
import android.widget.TextView;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import de.fehngarten.fhemswitch.BuildConfig;
import de.fehngarten.fhemswitch.R;
import de.fehngarten.fhemswitch.config.listviews.ConfigPagerAdapter;
import de.fehngarten.fhemswitch.data.ConfigDataCommon;
import de.fehngarten.fhemswitch.data.ConfigDataIO;
import de.fehngarten.fhemswitch.data.ConfigDataInstance;
import de.fehngarten.fhemswitch.data.ConfigWorkInstance;
import de.fehngarten.fhemswitch.modul.GetStoreVersion;
import de.fehngarten.fhemswitch.modul.MyBroadcastReceiver;
import de.fehngarten.fhemswitch.modul.MyReceiveListener;
import de.fehngarten.fhemswitch.modul.MySocket;
import de.fehngarten.fhemswitch.modul.MyWifiInfo;
import de.fehngarten.fhemswitch.modul.SendAlertMessage;
import de.fehngarten.fhemswitch.widget.WidgetProvider;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

import static android.appwidget.AppWidgetManager.ACTION_APPWIDGET_UPDATE;
import static de.fehngarten.fhemswitch.global.Consts.*;
import static de.fehngarten.fhemswitch.global.Settings.*;

//import android.util.Log;

public class ConfigMain extends Activity {
    //private final String TAG = "ConfigMain";
    int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
    private EditText urlpl, urljs, connectionPW, urlplLocal, urljsLocal;
    private CheckBox isHomeNet;
    private MyWifiInfo myWifiInfo;
    public static ConfigWorkInstance configWorkInstance;
    private ConfigDataCommon configDataCommon;
    public static ConfigDataInstance configDataInstance;
    public static MySocket mySocket;

    public Context mContext;
    public Handler waitAuth = new Handler();

    public RadioGroup radioWidgetSelector;
    public ConfigDataIO configDataIO;
    static final String STORE_VERSION_CONFIG = "de.fehngarten.fhemswitch.STORE_VERSION_CONFIG";
    private ArrayList<BroadcastReceiver> broadcastReceivers;
    private int instSerial;
    private ViewPager myPager;
    private ConfigPagerAdapter configPagerAdapter;
    private int lastPage;
    private boolean isFirstConfigPage = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Log.d(TAG, "onCreate fired");
        super.onCreate(savedInstanceState);

        mContext = this;
        setResult(RESULT_CANCELED);

        settingBlockNames.put(SWITCHES, this.getString(R.string.switches));
        settingBlockNames.put(LIGHTSCENES, this.getString(R.string.lightscenes));
        settingBlockNames.put(VALUES, this.getString(R.string.values));
        settingBlockNames.put(INTVALUES, this.getString(R.string.values2));
        settingBlockNames.put(COMMANDS, this.getString(R.string.commandsShort));

        //int height = size.y; a
        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        float density = getResources().getDisplayMetrics().density;
        float dpWidth = screenWidth / density;

        if (dpWidth < 750) {
            setContentView(R.layout.config__s);
        } else {
            setContentView(R.layout.config__l);
        }

        String installedText = BuildConfig.VERSION_NAME;
        TextView installedView = findViewById(R.id.installedView);
        installedView.setText(installedText);

        class OnStoreVersion implements MyReceiveListener {
            public void run(Context context, Intent intent) {
                try {
                    String latest = intent.getExtras().getString(GetStoreVersion.LATEST);
                    TextView latestView = findViewById(R.id.latestView);
                    latestView.setText(latest);
                } catch (NullPointerException e) {
                    // ignore
                }
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

        urlpl = findViewById(R.id.urlpl);
        urljs = findViewById(R.id.urljs);
        urlplLocal = findViewById(R.id.urlpl_local);
        urljsLocal = findViewById(R.id.urljs_local);

        isHomeNet = findViewById(R.id.is_home_net);
        TextView isHomeNetLabel = findViewById(R.id.is_home_net_label);
        connectionPW = findViewById(R.id.connection_pw);

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            mAppWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        } else {
            mAppWidgetId = -1;
        }

        configDataIO = new ConfigDataIO(mContext);
        configDataCommon = configDataIO.readCommon();

        if (Build.VERSION.SDK_INT >= 23 && !configDataCommon.writePermissionAsked) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            configDataCommon.writePermissionAsked = true;
        }

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

        if (mAppWidgetId > 0) {
            instSerial = configDataCommon.getFreeInstance(mAppWidgetId);
        } else {
            instSerial = configDataCommon.getFirstInstance();
        }

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

            TextView t = findViewById(R.id.message);
            t.setText(hint);

            findViewById(R.id.actions_config).setVisibility(View.GONE);
            findViewById(R.id.url_block).setVisibility(View.GONE);
            findViewById(R.id.message_block).setVisibility(View.VISIBLE);
        } else if (configDataCommon.getWidgetCount() > 1) {

            RadioGroup radioGroup = findViewById(R.id.widgetsel);

            findViewById(R.id.widgetSelector).setVisibility(View.VISIBLE);
            int i = 0;
            for (int widgetId : configDataCommon.instances) {
                if (widgetId > 0) {
                    findViewById(settingWidgetSel[i]).setVisibility(View.VISIBLE);
                } else {
                    findViewById(settingWidgetSel[i]).setVisibility(View.GONE);
                }

                if (configDataCommon.getWidgetCount() > 2 && dpWidth < 600) {
                    String w = "W" + i;
                    ((RadioButton) radioGroup.getChildAt(i)).setText(w);
                }
                i++;
            }
        } else {
            findViewById(R.id.widgetSelector).setVisibility(View.GONE);
        }
        handleButtons();
    }

    private void showFHEMunits() {
        sendDoColor();
        isFirstConfigPage = false;
        //Log.d("Instanz lesen config", Integer.toString(instSerial));
        configDataInstance = configDataIO.readInstance(instSerial);

        ((RadioButton) radioWidgetSelector.getChildAt(instSerial)).setChecked(true);
        radioWidgetSelector.setOnCheckedChangeListener(widgetSelectorChange);
        hideSoftKeyboard();

        for (int i = 0; i < settingsMaxInst; i++) {
            findViewById(settingWidgetSel[i]).setBackgroundResource(settingShapes[i]);
        }
        findViewById(settingWidgetSel[instSerial]).setBackgroundResource(settingShapesSelected[instSerial]);

        // open websocket connection
        doConnect(false);
    }

    private void doConnect(boolean isRefresh) {
        if (mySocket == null || mySocket.socket == null || !mySocket.socket.connected()) {
            //Log.d("Instanz doconnect", "fired");
            mySocket = new MySocket(mContext, configDataCommon, "Config");
            if (isRefresh) {
                mySocket.socket.on("authenticated", authListenerRefresh);
            } else {
                mySocket.socket.on("authenticated", authListener);
            }

            mySocket.socket.on(Socket.EVENT_CONNECT_ERROR, args -> runOnUiThread(() -> {
                waitAuth.removeCallbacks(runnableWaitAuth);
                new SendAlertMessage(mContext, getString(R.string.noconnjs) + ":\n- " + getString(R.string.urlcheck) + ".\n- " + getString(R.string.onlinecheck) + "?");
            }));

            mySocket.socket.on("unauthorized", args -> runOnUiThread(() -> new SendAlertMessage(mContext, getString(R.string.checkpw))));

            mySocket.doConnect();
        } else {
            if (isRefresh) {
                mySocket.refresh();
            } else {
                buildOutput();
            }
        }
    }

    public void buildOutput() {

        configWorkInstance = new ConfigWorkInstance();
        configWorkInstance.init();

        configPagerAdapter = new ConfigPagerAdapter(mContext, mySocket, this);
        myPager = findViewById(R.id.configpager);
        myPager.setAdapter(configPagerAdapter);
        myPager.setCurrentItem(settingPagerFirstItem);
        lastPage = settingPagerFirstItem;
        hilightTab(settingPagerFirstItem);
        waitAuth.removeCallbacks(runnableWaitAuth);

        findViewById(R.id.url_block).setVisibility(View.GONE);
        findViewById(R.id.config_layout_block).setVisibility(View.VISIBLE);
        findViewById(R.id.config_layout_block_inner).setVisibility(View.VISIBLE);

        hideSoftKeyboard();

        for (int settingTab : settingTabs) {
            findViewById(settingTab).setOnClickListener(tabOnClickListener);
        }
        myPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            public void onPageScrollStateChanged(int arg0) {
            }

            public void onPageScrolled(int arg0, float arg1, int arg2) {
                ScrollView scrollView = findViewById(R.id.scrollView);
                scrollView.fullScroll(ScrollView.FOCUS_UP);
                //scrollView.scrollTo(0, 0);
            }

            public void onPageSelected(int currentPage) {
                //Log.d(TAG, "currentPage: " + currentPage);
                if (lastPage != currentPage) {
                    configPagerAdapter.saveItem(lastPage);
                    lastPage = currentPage;
                }
                hilightTab(currentPage);
            }
        });
    }

    private void handleButtons() {
        findViewById(R.id.actions_config).setOnClickListener(callActionsButtonOnClickListener);

        findViewById(R.id.get_config).setOnClickListener(getConfigButtonOnClickListener);
        findViewById(R.id.save_config).setOnClickListener(saveConfigButtonOnClickListener);
        findViewById(R.id.cancel_config).setOnClickListener(cancelConfigButtonOnClickListener);
        findViewById(R.id.cancel2_config).setOnClickListener(cancelConfigButtonOnClickListener);
        findViewById(R.id.cancel3_config).setOnClickListener(cancelConfigButtonOnClickListener);
        findViewById(R.id.help_homenet).setOnClickListener(callHelpHomenetOnClickListener);
        radioWidgetSelector = findViewById(R.id.widgetsel);
    }

    @Override
    public void onDestroy() {
        //Log.d("Instanz", "onDestroy fired");
        Intent intent = new Intent(mContext.getApplicationContext(), WidgetProvider.class);
        intent.setAction(ACTION_APPWIDGET_UPDATE);
        sendBroadcast(intent);

        if (mySocket != null) {
            mySocket.destroy();
            mySocket = null;
        }

        for (BroadcastReceiver broadcastReceiver : broadcastReceivers) {
            unregisterReceiver(broadcastReceiver);
        }

        super.onDestroy();
    }

    // ---------------------------------------------------------------------------------------------
    // --- diverse listeners
    // ---------------------------------------------------------------------------------------------

    private Emitter.Listener authListener = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            ConfigMain.this.runOnUiThread(ConfigMain.this::buildOutput);
        }
    };

    private Emitter.Listener authListenerRefresh = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            //Log.d(TAG, "refresh connect fired");
            if (mySocket != null) {
                mySocket.refresh();
                mySocket.destroy();
            }
        }
    };

    private OnCheckedChangeListener widgetSelectorChange = new OnCheckedChangeListener() {

        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            findViewById(R.id.config_layout_block_inner).setVisibility(View.GONE);
            View radioButton = group.findViewById(checkedId);
            saveConfig(false);
            Intent intent = new Intent(mContext.getApplicationContext(), settingServiceClasses.get(instSerial));
            intent.setAction(NEW_CONFIG);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, configDataCommon.instances[instSerial]);
            mContext.stopService(intent);
            mContext.startService(intent);

            instSerial = Integer.valueOf(radioButton.getTag().toString());
            Handler handler = new Handler();
            handler.postDelayed(delayOutput, 200);  //calls showFHEMunits();
        }
    };

    // ---------------------------------------------------------------------------------------------
    // --- onclick listener
    // ---------------------------------------------------------------------------------------------

    private Button.OnClickListener callActionsButtonOnClickListener = new Button.OnClickListener() {
        @SuppressLint("RestrictedApi")
        @Override
        public void onClick(View arg0) {
            //PopupMenu popup = new PopupMenu(mContext, findViewById(R.id.actions_config));
            //popup.getMenuInflater().inflate(R.menu.config_action_menu, popup.getMenu());

            MenuBuilder menuBuilder = new MenuBuilder(mContext);
            MenuInflater inflater = new MenuInflater(mContext);
            inflater.inflate(R.menu.config_action_menu, menuBuilder);
            MenuPopupHelper optionsMenu = new MenuPopupHelper(mContext, menuBuilder, findViewById(R.id.actions_config));
            optionsMenu.setForceShowIcon(true);

            // Set Item Click Listener
            menuBuilder.setCallback(new MenuBuilder.Callback() {
                @Override
                public boolean onMenuItemSelected(MenuBuilder menu, MenuItem item) {
                    switch (item.getItemId()) {
                         case R.id.callRefreshServer:
                            callRefreshServer();
                            return true;
                        case R.id.callExport:
                            doExport();
                            return true;
                        case R.id.callImport:
                            if (isFirstConfigPage) {
                                doImportCommon();
                            } else {
                                doImportInstance();
                            }
                            return true;
                        case R.id.callHelp:
                            callHelp();
                            return true;
                        default:
                            return false;
                    }
                }

                @Override
                public void onMenuModeChange(MenuBuilder menu) {}
            });

            // Display the menu
            optionsMenu.show();
        }
    };

    private Button.OnClickListener getConfigButtonOnClickListener = arg0 -> {
        if (saveConfigCommon()) {
            showFHEMunits();
        }
    };

    private Button.OnClickListener callHelpHomenetOnClickListener = arg0 -> {
        Intent webIntent = new Intent(Intent.ACTION_VIEW).setData(Uri.parse(settingHelpUrlHome));
        webIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(webIntent);
    };

    private Button.OnClickListener saveConfigButtonOnClickListener = arg0 -> saveConfig(true);

    private Button.OnClickListener cancelConfigButtonOnClickListener = arg0 -> finish();

    private TextView.OnClickListener tabOnClickListener = arg0 -> {
        String pos = arg0.getTag().toString();
        myPager.setCurrentItem(Integer.parseInt(pos));
    };

    // ---------------------------------------------------------------------------------------------
    // --- save routines
    // ---------------------------------------------------------------------------------------------

    private void saveConfig(boolean doFinish) {
        configPagerAdapter.saveItem(lastPage);
        //if (configDataInstance.widgetName == null || configDataInstance.widgetName.equals("")) {
            configDataInstance.widgetName = "Widget " + Integer.toString(instSerial + 1);
        //}
        configDataIO.saveInstance(configDataInstance, instSerial);

        if (doFinish) {
            Intent resultValue = new Intent();
            resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
            setResult(RESULT_OK, resultValue);
            finish();
        }
    }

    private boolean saveConfigCommon() {

        String url;

        url = urljs.getText().toString().trim();
        if (!checkUrl(url, getString(R.string.url_fhem_js), true)) {
            return false;
        }
        configDataCommon.urlFhemjs = url;

        url = urljsLocal.getText().toString().trim();
        if (!checkUrl(url, getString(R.string.url_fhem_js_local), false)) {
            return false;
        }
        configDataCommon.urlFhemjsLocal = url;

        url = urlpl.getText().toString().trim();
        if (!checkUrl(url, getString(R.string.url_fhem_pl), false)) {
            return false;
        }
        configDataCommon.urlFhempl = url;

        url = urlplLocal.getText().toString().trim();
        if (!checkUrl(url, getString(R.string.url_fhem_pl_local), false)) {
            return false;
        }
        configDataCommon.urlFhemplLocal = url;

        configDataCommon.fhemjsPW = connectionPW.getText().toString();

        if (isHomeNet.isChecked()) {
            configDataCommon.bssId = myWifiInfo.getWifiId();
        } else if (configDataCommon.bssId.equals(myWifiInfo.getWifiId())) {
            configDataCommon.bssId = "";
        }
        configDataIO.saveCommon(configDataCommon);
        return true;
    }

    // ---------------------------------------------------------------------------------------------
    // --- helper, checker, waiter ...
    // ---------------------------------------------------------------------------------------------

    private void hideSoftKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public Runnable delayOutput = this::showFHEMunits;

    private void sendDoColor() {
        int widgetId;
        for (int i = 0; i < configDataCommon.instances.length; i++) {
            widgetId = configDataCommon.instances[i];
            if (widgetId > 0) {
                Intent intent = new Intent(mContext.getApplicationContext(), settingServiceClasses.get(i));
                intent.setAction(SEND_DO_COLOR);
                intent.putExtra("COLOR", true);
                intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
                mContext.startService(intent);
            }
        }
    }

    private Runnable runnableWaitAuth = new Runnable() {
        @Override
        public void run() {
            new SendAlertMessage(mContext, getString(R.string.checkpw));
            mySocket.socket.off("authenticated");
            mySocket.socket.close();
            mySocket = null;
        }
    };

    private void hilightTab(int pos) {
        for (int settingTab : settingTabs) {
            TextView tabView = findViewById(settingTab);
            tabView.setBackgroundResource(R.drawable.config_shape_tabs);
            tabView.setTextColor(ContextCompat.getColor(mContext, R.color.text_hinweis));
        }
        TextView tabView = findViewById(settingTabs[pos]);
        tabView.setBackgroundResource(R.drawable.config_shape_tabssel);
        tabView.setTextColor(ContextCompat.getColor(mContext, R.color.conf_text_header));
    }

    private boolean checkUrl(String url, String fieldname, boolean mandatory) {
        if (checkUrl2(url, mandatory)) {
            return true;
        } else {
            new SendAlertMessage(mContext, fieldname + ':' + getResources().getString(R.string.urlerr));
            return false;
        }
    }

    private boolean checkUrl2(String url, boolean mandatory) {

        boolean rc = false;
        if (!mandatory && url.equals("")) {
            rc = true;
        } else {

            HashSet<String> protocols = new HashSet<>(Arrays.asList("http", "https"));

            int colon = url.indexOf(':');

            if (colon < 3) return false;

            String proto = url.substring(0, colon).toLowerCase();

            if (!protocols.contains(proto)) return false;

            try {

                URI uri = new URI(url);
                if (uri.getHost() == null) return false;

                String path = uri.getPath();
                if (path != null) {
                    for (int i = path.length() - 1; i >= 0; i--) {
                        if ("?<>:*|\"".indexOf(path.charAt(i)) > -1)
                            return false;
                    }
                }
                rc = true;
            } catch (Exception ignored) {
            }
        }
        return rc;
    }

    public void finishCauseOfConnetionLoss() {
        //String popupText = mContext.getString(R.string.export_hint, configDataIO.getBackupPath());
        String popupText = "Shit happens - cause of connection loss to FHEM server, config will be closed";
        PopupHolder popupHolder = openPopupHint(popupText, false, true);

        if (popupHolder != null) {
            popupHolder.okButton.setOnClickListener(v -> {
                popupHolder.popupWindow.dismiss();
                finish();
            });
        }
    }

    // ---------------------------------------------------------------------------------------------
    // --- Popup handler for messages
    // ---------------------------------------------------------------------------------------------

    private class PopupHolder {
        PopupWindow popupWindow;
        Button okButton;
        Button cancelButton;
    }

    private PopupHolder openPopupHint(String popupText, boolean showCancelButton, boolean isErrorMessage) {
        //LayoutInflater layoutInflater = (LayoutInflater) getBaseContext().getSystemService(LAYOUT_INFLATER_SERVICE);
        try {
            if (!isFinishing()) {
                LayoutInflater layoutInflater = (LayoutInflater) mContext.getSystemService(LAYOUT_INFLATER_SERVICE);
                View popupView = layoutInflater.inflate(R.layout.two_button_popup, null);

                PopupHolder popupHolder = new PopupHolder();
                popupHolder.popupWindow = new PopupWindow(
                        popupView,
                        ViewPager.LayoutParams.WRAP_CONTENT,
                        ViewPager.LayoutParams.WRAP_CONTENT);

                TextView popupTextView = popupView.findViewById(R.id.popup_text);
                popupTextView.setText(popupText);
                //popupView.setBackgroundResource(R.drawable.widget_shape_active_both);

                popupHolder.okButton = popupView.findViewById(R.id.popup_ok);

                popupHolder.cancelButton = popupView.findViewById(R.id.popup_cancel);
                if (showCancelButton) {
                    popupHolder.cancelButton.setVisibility(View.VISIBLE);
                    popupHolder.cancelButton.setOnClickListener(v -> popupHolder.popupWindow.dismiss());
                } else {
                    popupHolder.cancelButton.setVisibility(View.GONE);
                    popupHolder.okButton.setOnClickListener(v -> popupHolder.popupWindow.dismiss());
                    popupHolder.cancelButton = null;
                    if (isErrorMessage) {
                        popupTextView.setTextColor(ContextCompat.getColor(mContext, R.color.error));
                        popupHolder.okButton.setBackgroundResource(R.drawable.widget_shape_0);
                    } else {
                        popupTextView.setTextColor(ContextCompat.getColor(mContext, R.color.conf_bg_header_2));
                    }
                }

                int top = showCancelButton ? 110 : 120;
                popupHolder.popupWindow.showAtLocation(popupView, Gravity.TOP, 0, top);
                return popupHolder;
            } else {
                return null;
            }
        } catch (Exception e) {
            return null;
        }

    }

    // ---------------------------------------------------------------------------------------------
    // --- Export config
    // ---------------------------------------------------------------------------------------------

    private void doExport() {
        String popupText = mContext.getString(R.string.export_hint, configDataIO.getBackupPath());
        PopupHolder popupHolder = openPopupHint(popupText, true, false);

        if (popupHolder != null) {
            popupHolder.okButton.setOnClickListener(v -> {
                String responseText;
                if (configDataIO.doExport(configDataCommon)) {
                    responseText = mContext.getString(R.string.export_success, configDataIO.getBackupPath());
                    openPopupHint(responseText, false, false);
                } else {
                    responseText = mContext.getString(R.string.export_error);
                    openPopupHint(responseText, false, true);
                }
                popupHolder.popupWindow.dismiss();
            });
        }
    }

    // ---------------------------------------------------------------------------------------------
    // --- Import config
    // ---------------------------------------------------------------------------------------------

    private void doImportCommon() {
        int textId = isFirstConfigPage ? R.string.import_hint_urls : R.string.import_hint_widget;
        String popupText = mContext.getString(textId, configDataIO.getBackupPath());
        PopupHolder popupHolder = openPopupHint(popupText, true, false);

        if (popupHolder != null) {
            popupHolder.okButton.setOnClickListener(v -> {
                doImportCommonReal();
                popupHolder.popupWindow.dismiss();
            });
        }
    }

    private void doImportCommonReal() {
        String responseText;
        ConfigDataCommon configDataCommonImport = configDataIO.doImportCommon();
        if (configDataCommonImport == null) {
            responseText =  mContext.getString(R.string.import_error);
            openPopupHint(responseText, false, true);
        } else {
            configDataCommon.urlFhemjs = configDataCommonImport.urlFhemjs;
            configDataCommon.urlFhemjsLocal = configDataCommonImport.urlFhemjsLocal;
            configDataCommon.urlFhempl = configDataCommonImport.urlFhempl;
            configDataCommon.urlFhemplLocal = configDataCommonImport.urlFhemplLocal;
            urljs.setText(configDataCommon.urlFhemjs, TextView.BufferType.EDITABLE);
            urljsLocal.setText(configDataCommon.urlFhemjsLocal, TextView.BufferType.EDITABLE);
            urlpl.setText(configDataCommon.urlFhempl, TextView.BufferType.EDITABLE);
            urlplLocal.setText(configDataCommon.urlFhemplLocal, TextView.BufferType.EDITABLE);
            responseText =  mContext.getString(R.string.import_success);
            openPopupHint(responseText, false, false);
        }
    }

    private void doImportInstance() {
        String responseText;
        CharSequence fileNames[] = configDataIO.getAvailableInstanceFiles();
        if (fileNames == null || fileNames.length == 0) {
            responseText =  mContext.getString(R.string.import_error);
            openPopupHint(responseText, false, true);
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
            builder.setTitle(mContext.getString(R.string.select_widget));
            builder.setItems(fileNames, (dialog, which) -> doImportInstanceReal(fileNames[which].toString()));
            builder.setNegativeButton(mContext.getString(R.string.cancel), null);
            builder.show();
        }
    }

    private void doImportInstanceReal(String fileName) {
        String responseText;
        ConfigDataInstance configDataInstanceImport = configDataIO.doImportInstance(fileName);
        if (configDataInstanceImport != null) {
            configDataInstance = configDataInstanceImport;
            configDataIO.saveInstance(configDataInstance, instSerial);
            responseText =  mContext.getString(R.string.import_success);
            openPopupHint(responseText, false, false);
            buildOutput();
        } else {
            responseText =  mContext.getString(R.string.import_error);
            openPopupHint(responseText, false, true);
        }
    }

    // ---------------------------------------------------------------------------------------------
    // --- Donate, help and refresh handler
    // ---------------------------------------------------------------------------------------------

    private void callHelp() {
        Intent webIntent = new Intent(Intent.ACTION_VIEW).setData(Uri.parse(settingHelpUrl));
        webIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(webIntent);
    }

    private void callRefreshServer() {
        doConnect(true);
        openPopupHint(getResources().getString(R.string.serverRefresh), false, false);
    }
}
