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
import de.fehngarten.fhemswitch.data.ConfigWorkBasket;
import de.fehngarten.fhemswitch.data.ConfigWorkInstance;
import de.fehngarten.fhemswitch.modul.GetStoreVersion;
import de.fehngarten.fhemswitch.modul.MyBroadcastReceiver;
import de.fehngarten.fhemswitch.modul.MyReceiveListener;
import de.fehngarten.fhemswitch.modul.MySocket;
import de.fehngarten.fhemswitch.modul.MyWifiInfo;
import de.fehngarten.fhemswitch.modul.SendAlertMessage;
import de.fehngarten.fhemswitch.modul.SendStatusMessage;
import de.fehngarten.fhemswitch.widget.WidgetProvider;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

import static android.appwidget.AppWidgetManager.ACTION_APPWIDGET_UPDATE;
import static de.fehngarten.fhemswitch.global.Consts.*;
import static de.fehngarten.fhemswitch.global.Settings.*;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Log.d(TAG, "onCreate fired");
        super.onCreate(savedInstanceState);

        mContext = this;
        setResult(RESULT_CANCELED);

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
        configDataCommon = configDataIO.readCommon();

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

    @Override
    public void onDestroy() {

        Intent intent = new Intent(mContext.getApplicationContext(), WidgetProvider.class);
        intent.setAction(ACTION_APPWIDGET_UPDATE);
        sendBroadcast(intent);

        //if (BuildConfig.DEBUG) Log.d(TAG, "onDestroy fired");
        if (mySocket != null) {
            mySocket.destroy();
        }

        for (BroadcastReceiver broadcastReceiver : broadcastReceivers) {
            unregisterReceiver(broadcastReceiver);
        }

        super.onDestroy();
    }


    private void showFHEMunits() {
        // hide soft keyboard
        sendDoColor();
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
        if (saveConfigCommon()) {
            showFHEMunits();
        }
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
        //Log.d(TAG, "refresh fired");
        doConnect(true);
        new SendStatusMessage(mContext, getResources().getString(R.string.serverRefresh));
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
            //Log.d(TAG, "refresh connect fired");
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


        for (int settingTab : settingTabs) {
            findViewById(settingTab).setOnClickListener(tabOnClickListener);
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
                //Log.d(TAG, "currentPage: " + currentPage);
                if (lastPage != currentPage) {
                    configPagerAdapter.saveItem(lastPage);
                    lastPage = currentPage;
                }
                hilightTab(currentPage);
            }

        });
    }

    private void hilightTab(int pos) {
        for (int settingTab : settingTabs) {
            TextView tabView = (TextView) findViewById(settingTab);
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

            HashSet<String> protocols = new HashSet<>(Arrays.asList(new String[]{"http", "https"}));

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
            } catch (Exception ex) {}
        }
        return rc;
    }
}
