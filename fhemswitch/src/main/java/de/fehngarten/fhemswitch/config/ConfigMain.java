package de.fehngarten.fhemswitch.config;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;

import de.fehngarten.fhemswitch.BuildConfig;
import de.fehngarten.fhemswitch.R;
import de.fehngarten.fhemswitch.config.listviews.ConfigPagerAdapter;
import de.fehngarten.fhemswitch.data.ConfigDataCommon;
import de.fehngarten.fhemswitch.data.ConfigDataIO;
import de.fehngarten.fhemswitch.data.ConfigDataInstance;
import de.fehngarten.fhemswitch.data.ConfigWorkBasket;
import de.fehngarten.fhemswitch.data.ConfigWorkInstance;
import de.fehngarten.fhemswitch.modul.GetStoreVersion;
import de.fehngarten.fhemswitch.modul.MyBroadcastReceiver;
import de.fehngarten.fhemswitch.modul.MyReceiveListener;
import de.fehngarten.fhemswitch.modul.MySocket;
import de.fehngarten.fhemswitch.modul.MyWifiInfo;
import de.fehngarten.fhemswitch.modul.SendAlertMessage;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

import static android.appwidget.AppWidgetManager.ACTION_APPWIDGET_UPDATE;
import static de.fehngarten.fhemswitch.global.Consts.NEW_CONFIG;
import static de.fehngarten.fhemswitch.global.Consts.SEND_DO_COLOR;
import static de.fehngarten.fhemswitch.global.Consts.STOP_CONFIG;
import static de.fehngarten.fhemswitch.global.Settings.settingHelpUrl;
import static de.fehngarten.fhemswitch.global.Settings.settingHelpUrlHome;
import static de.fehngarten.fhemswitch.global.Settings.settingPagerFirstItem;
import static de.fehngarten.fhemswitch.global.Settings.settingServiceClasses;
import static de.fehngarten.fhemswitch.global.Settings.settingTabs;
import static de.fehngarten.fhemswitch.global.Settings.settingWidgetSel;
import static de.fehngarten.fhemswitch.global.Settings.settingsMaxInst;

//import android.util.Log;

public class ConfigMain extends Activity {
    private final String TAG = "ConfigMain";
    int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
    private EditText urlpl, urljs, connectionPW, urlplLocal, urljsLocal;
    private CheckBox isHomeNet;
    private MyWifiInfo myWifiInfo;
    public static ConfigWorkInstance configWorkInstance;
    private ConfigDataCommon configDataCommon;
    public static ConfigDataInstance configDataInstance;
    public static MySocket mySocket;

    public int lsCounter = 0;
    public int lsSize = 0;
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

    public ConfigMain() {
        Log.d(TAG, "class started");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate fired");
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

        if (dpWidth < 800) {
            setContentView(R.layout.config__s);
        } else {
            setContentView(R.layout.config__l);
        }

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

            RadioGroup radioGroup = (RadioGroup) findViewById(R.id.widgetsel);

            findViewById(R.id.widgetSelector).setVisibility(View.VISIBLE);
            int i = 0;
            for (int widgetId : configDataCommon.instances) {
                if (widgetId > 0) {
                    findViewById(settingWidgetSel[i]).setVisibility(View.VISIBLE);
                } else {
                    findViewById(settingWidgetSel[i]).setVisibility(View.GONE);
                }

                if (configDataCommon.getWidgetCount() > 2 && dpWidth < 600) {
                    ((RadioButton) radioGroup.getChildAt(i)).setText("W" + i);
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

        ((RadioButton) radioWidgetSelector.getChildAt(instSerial)).setChecked(true);
        radioWidgetSelector.setOnCheckedChangeListener(widgetSelectorChange);
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }

        // open websocket connection
        doConnect(false);
    }

    private void doConnect(boolean isRefresh) {
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

        mySocket.socket.on("unauthorized", args -> runOnUiThread(() -> {
            new SendAlertMessage(mContext, getString(R.string.checkpw));
        }));

        mySocket.doConnect();
    }

    private void handleButtons() {
        findViewById(R.id.callDonateButton).setOnClickListener(callDonateButtonOnClickListener);
        findViewById(R.id.get_config).setOnClickListener(getConfigButtonOnClickListener);
        findViewById(R.id.save_config).setOnClickListener(saveConfigButtonOnClickListener);
        findViewById(R.id.cancel_config).setOnClickListener(cancelConfigButtonOnClickListener);
        findViewById(R.id.cancel2_config).setOnClickListener(cancelConfigButtonOnClickListener);
        findViewById(R.id.cancel3_config).setOnClickListener(cancelConfigButtonOnClickListener);
        findViewById(R.id.refresh_config).setOnClickListener(refreshConfigButtonOnClickListener);
        findViewById(R.id.help_layout).setOnClickListener(callHelpOnClickListener);
        findViewById(R.id.help_url).setOnClickListener(callHelpOnClickListener);
        findViewById(R.id.help_homenet).setOnClickListener(callHelpHomenetOnClickListener);

        radioWidgetSelector = (RadioGroup) findViewById(R.id.widgetsel);
    }

    private OnCheckedChangeListener widgetSelectorChange = new OnCheckedChangeListener() {

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

    private Button.OnClickListener getConfigButtonOnClickListener = arg0 -> {
        saveConfigCommon();
        showFHEMunits();
    };

    private Button.OnClickListener callHelpOnClickListener = arg0 -> {
        Intent webIntent = new Intent(Intent.ACTION_VIEW).setData(Uri.parse(settingHelpUrl));
        webIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(webIntent);
    };

    private Button.OnClickListener callHelpHomenetOnClickListener = arg0 -> {
        Intent webIntent = new Intent(Intent.ACTION_VIEW).setData(Uri.parse(settingHelpUrlHome));
        webIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(webIntent);
    };


    private Button.OnClickListener saveConfigButtonOnClickListener = arg0 -> saveConfig(true);

    private Button.OnClickListener refreshConfigButtonOnClickListener = arg0 -> {
        Log.d(TAG, "refresh fired");
        doConnect(true);
        Toast.makeText(mContext, getResources().getString(R.string.serverRefresh), Toast.LENGTH_LONG).show();
    };

    private Button.OnClickListener cancelConfigButtonOnClickListener = arg0 -> {

        //new ConfigDataIO(mContext).deleteCommon();

        finish();
    };
    private TextView.OnClickListener tabOnClickListener = arg0 -> {
        String pos = arg0.getTag().toString();
        myPager.setCurrentItem(Integer.parseInt(pos));
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

    private Emitter.Listener authListener = args -> {
        runOnUiThread(this::buildOutput);
    };

    private Emitter.Listener authListenerRefresh = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.d(TAG, "refresh connect fired");
            mySocket.refresh();
            mySocket.destroy();
        }
    };

    public void buildOutput() {

        configWorkInstance = new ConfigWorkInstance();
        configWorkInstance.init();

        configPagerAdapter = new ConfigPagerAdapter(mContext, mySocket);
        myPager = (ViewPager) findViewById(R.id.configpager);
        myPager.setAdapter(configPagerAdapter);
        myPager.setCurrentItem(settingPagerFirstItem);
        lastPage = settingPagerFirstItem;
        hilightTab(settingPagerFirstItem);
        waitAuth.removeCallbacks(runnableWaitAuth);

        findViewById(R.id.url_block).setVisibility(View.GONE);
        findViewById(R.id.config_layout_block).setVisibility(View.VISIBLE);

        // hide soft keyboard
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }


        for (int i = 0; i < settingTabs.length; i++) {
            findViewById(settingTabs[i]).setOnClickListener(tabOnClickListener);
        }

        myPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            public void onPageScrollStateChanged(int arg0) {
            }

            public void onPageScrolled(int arg0, float arg1, int arg2) {
                ScrollView scrollView = (ScrollView) findViewById(R.id.scrollView);
                scrollView.fullScroll(ScrollView.FOCUS_UP);
                //scrollView.scrollTo(0, 0);
            }

            public void onPageSelected(int currentPage) {
                Log.d(TAG, "currentPage: " + currentPage);
                if (lastPage != currentPage) {
                    configPagerAdapter.saveItem(lastPage);
                    lastPage = currentPage;
                }
                hilightTab(currentPage);
            }

        });
    }

    private void hilightTab(int pos) {
        for (int i = 0; i < settingTabs.length; i++) {
            TextView tabView = (TextView) findViewById(settingTabs[i]);
            tabView.setBackgroundResource(R.drawable.config_shape_tabs);
            tabView.setTextColor(ContextCompat.getColor(mContext, R.color.text_hinweis));
        }
        TextView tabView = (TextView) findViewById(settingTabs[pos]);
        tabView.setBackgroundResource(R.drawable.config_shape_tabssel);
        tabView.setTextColor(ContextCompat.getColor(mContext, R.color.conf_text_header_1));
    }

    private void saveConfig(boolean doFinish) {
        configPagerAdapter.saveItem(lastPage);
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
