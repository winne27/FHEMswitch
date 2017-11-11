package de.fehngarten.fhemswitch.widget;

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
import android.widget.PopupMenu;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.firebase.analytics.FirebaseAnalytics;

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
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

import static android.appwidget.AppWidgetManager.ACTION_APPWIDGET_UPDATE;
import static de.fehngarten.fhemswitch.global.Consts.COMMANDS;
import static de.fehngarten.fhemswitch.global.Consts.INTVALUES;
import static de.fehngarten.fhemswitch.global.Consts.LIGHTSCENES;
import static de.fehngarten.fhemswitch.global.Consts.NEW_CONFIG;
import static de.fehngarten.fhemswitch.global.Consts.SEND_DO_COLOR;
import static de.fehngarten.fhemswitch.global.Consts.STOP_CONFIG;
import static de.fehngarten.fhemswitch.global.Consts.SWITCHES;
import static de.fehngarten.fhemswitch.global.Consts.VALUES;
import static de.fehngarten.fhemswitch.global.Settings.settingBlockNames;
import static de.fehngarten.fhemswitch.global.Settings.settingHelpUrl;
import static de.fehngarten.fhemswitch.global.Settings.settingHelpUrlHome;
import static de.fehngarten.fhemswitch.global.Settings.settingPagerFirstItem;
import static de.fehngarten.fhemswitch.global.Settings.settingServiceClasses;
import static de.fehngarten.fhemswitch.global.Settings.settingShapes;
import static de.fehngarten.fhemswitch.global.Settings.settingShapesSelected;
import static de.fehngarten.fhemswitch.global.Settings.settingTabs;
import static de.fehngarten.fhemswitch.global.Settings.settingWidgetSel;
import static de.fehngarten.fhemswitch.global.Settings.settingsMaxInst;

public class Refresh extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Log.d(TAG, "onCreate fired");
        super.onCreate(savedInstanceState);
        moveTaskToBack(true);
        Intent intent = new Intent(getApplicationContext(), WidgetProvider.class);
        intent.setAction(ACTION_APPWIDGET_UPDATE);
        sendBroadcast(intent);
        finish();
    }
}
