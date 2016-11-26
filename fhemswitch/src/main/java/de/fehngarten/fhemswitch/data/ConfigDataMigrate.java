package de.fehngarten.fhemswitch.data;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.util.Log;

import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;

import de.fehngarten.fhemswitch.BuildConfig;
import de.fehngarten.fhemswitch.ConfigDataOnly;
import de.fehngarten.fhemswitch.widget.WidgetProvider;


public class ConfigDataMigrate {

    Context mContext;
    ConfigDataIO configDataIO;
    private final String TAG = "ConfigDataMigrate";

    public ConfigDataMigrate(Context context, ConfigDataIO configDataIO) {
        mContext = context;
        this.configDataIO = configDataIO;
    }

    public ConfigDataCommon doMigrate() {
        Log.d(TAG, "gestartet");
        Object obj = null;
        try {
            String filename = "config.data";
            FileInputStream f_in = mContext.openFileInput(filename);
            ObjectInputStream obj_in = new ObjectInputStream(f_in);

            obj = obj_in.readObject();
            obj_in.close();

            if (obj instanceof ConfigDataOnly) {
                if (BuildConfig.DEBUG) Log.d(TAG, "old config found");
                ConfigDataOnly configDataOnly = (ConfigDataOnly) obj;
                return doRealMigrate(configDataOnly);
            } else {
                if (BuildConfig.DEBUG) Log.d(TAG, "old config data not correct ");
                return null;
            }
        } catch (Exception e) {
            if (BuildConfig.DEBUG) Log.d(TAG, "old config data with exception");
            if (BuildConfig.DEBUG) Log.d(TAG, e.getMessage());
            return null;
        }
    }

    private ConfigDataCommon doRealMigrate(ConfigDataOnly configDataOnly) {
        if (BuildConfig.DEBUG) Log.d(TAG, "old version takeover started");
        ConfigDataCommon configDataCommon = new ConfigDataCommon();
        configDataCommon.init();
        configDataCommon.urlFhemjs = configDataOnly.urljs;
        configDataCommon.urlFhempl = configDataOnly.urlpl;
        configDataCommon.fhemjsPW = configDataOnly.connectionPW;

        ComponentName thisWidget = new ComponentName(mContext, WidgetProvider.class);
        AppWidgetManager mgr = AppWidgetManager.getInstance(mContext);
        int[] widgetIds = mgr.getAppWidgetIds(thisWidget);

        if (widgetIds.length > 0) {
            configDataCommon.instances[0] = widgetIds[0];
        }

        configDataIO.saveCommon(configDataCommon);
        ConfigDataInstance configDataInstance = new ConfigDataInstance();

        configDataInstance.switchRows = new ArrayList<>();
        for (de.fehngarten.fhemswitch.ConfigSwitchRow configSwitchRow : configDataOnly.switchRows) {
            configDataInstance.switchRows.add(new ConfigSwitchRow(configSwitchRow.unit, configSwitchRow.name, configSwitchRow.enabled, configSwitchRow.cmd));
        }

        configDataInstance.valueRows = new ArrayList<>();
        for (de.fehngarten.fhemswitch.ConfigValueRow configValueRow : configDataOnly.valueRows) {
            configDataInstance.valueRows.add(new ConfigValueRow(configValueRow.unit, configValueRow.name, configValueRow.value, configValueRow.enabled));
        }

        configDataInstance.commandRows = new ArrayList<>();
        for (de.fehngarten.fhemswitch.ConfigCommandRow configCommandRow : configDataOnly.commandRows) {
            configDataInstance.commandRows.add(new ConfigCommandRow(configCommandRow.name, configCommandRow.command, configCommandRow.enabled));
        }

        configDataInstance.lightsceneRows = new ArrayList<>();
        for (de.fehngarten.fhemswitch.ConfigLightsceneRow configLightsceneRow : configDataOnly.lightsceneRows) {
            configDataInstance.lightsceneRows.add(new ConfigLightsceneRow(configLightsceneRow.unit, configLightsceneRow.name, configLightsceneRow.enabled, configLightsceneRow.isHeader, configLightsceneRow.showHeader));
        }

        configDataInstance.layoutLandscape = configDataOnly.layoutLandscape;
        configDataInstance.layoutPortrait  = configDataOnly.layoutPortrait ;
        configDataInstance.switchCols      = configDataOnly.switchCols     ;
        configDataInstance.valueCols       = configDataOnly.valueCols      ;
        configDataInstance.commandCols     = configDataOnly.commandCols    ;

        configDataIO.saveInstance(configDataInstance, 0);

        return configDataCommon;
    }
}