package de.fehngarten.fhemswitch.data;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

import de.fehngarten.fhemswitch.*;
import de.fehngarten.fhemswitch.ConfigCommandRow;
import de.fehngarten.fhemswitch.ConfigLightsceneRow;
import de.fehngarten.fhemswitch.ConfigSwitchRow;
import de.fehngarten.fhemswitch.ConfigValueRow;

public class ConfigDataOnlyIO {
    public static final String CONFIGFILE = "config.data";

    public ConfigDataOnly configDataOnly;
    private Context mContext;
    private String[] types;

    public ConfigDataOnlyIO(Context context, String[] types) {

        mContext = context;
        this.types = types;
    }

    public ConfigDataOnly read() {
        Object obj = null;
        try {
            FileInputStream f_in = mContext.openFileInput(CONFIGFILE);
            ObjectInputStream obj_in = new ObjectInputStream(f_in);

            obj = obj_in.readObject();
            obj_in.close();

            if (BuildConfig.DEBUG) Log.d("configIO", "config.data found");
            if (obj instanceof ConfigDataOnly) {
                if (BuildConfig.DEBUG) Log.d("configIO", "config.data ok");
                configDataOnly = (ConfigDataOnly) obj;
                configDataOnly.checkTypes(types);
            } else {
                if (BuildConfig.DEBUG) Log.d("configIO", "config.data not correct ");
                configDataOnly = checkForOldVersion(obj);
            }
        } catch (Exception e) {
            if (BuildConfig.DEBUG) Log.d("configIO", "config.data with exception");
            //if (BuildConfig.DEBUG) Log.d("configIO", e.getMessage());
            configDataOnly = checkForOldVersion(obj);
        }

        return configDataOnly;
    }

    public Boolean save(ConfigDataOnly configDataOnly) {
        try {
            String dir = mContext.getFilesDir().getAbsolutePath();
            File f0 = new File(dir, CONFIGFILE);
            f0.delete();
            FileOutputStream f_out = mContext.openFileOutput(CONFIGFILE, Context.MODE_PRIVATE);
            ObjectOutputStream obj_out = new ObjectOutputStream(f_out);
            obj_out.writeObject(configDataOnly);
            obj_out.close();
            if (BuildConfig.DEBUG) Log.d("config", "config.data written");
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public ConfigDataOnly checkForOldVersion(Object obj) {
        if (BuildConfig.DEBUG) Log.d("configIO", "check for old version started");
        configDataOnly = new ConfigDataOnly(types);
        try {
            if (obj != null && obj instanceof de.fehngarten.fhemswitch.ConfigDataOnly) {
                if (BuildConfig.DEBUG) Log.d("configIO", "config.data old version found");
                de.fehngarten.fhemswitch.ConfigDataOnly configDataOnlyOld = (de.fehngarten.fhemswitch.ConfigDataOnly) obj;
                configDataOnly.urljs = configDataOnlyOld.urljs;
                configDataOnly.urlpl = configDataOnlyOld.urlpl;
                for (de.fehngarten.fhemswitch.ConfigSwitchRow oldRow : configDataOnlyOld.switchRows) {
                    configDataOnly.switchRows.add(new de.fehngarten.fhemswitch.data.ConfigSwitchRow(oldRow.unit, oldRow.name, oldRow.enabled, oldRow.cmd));
                }

                for (de.fehngarten.fhemswitch.ConfigValueRow oldRow : configDataOnlyOld.valueRows) {
                    configDataOnly.valueRows.add(new de.fehngarten.fhemswitch.data.ConfigValueRow(oldRow.unit, oldRow.name, oldRow.value, oldRow.enabled));
                }

                for (de.fehngarten.fhemswitch.ConfigCommandRow oldRow : configDataOnlyOld.commandRows) {
                    configDataOnly.commandRows.add(new de.fehngarten.fhemswitch.data.ConfigCommandRow(oldRow.name, oldRow.command, oldRow.enabled));
                }

                for (de.fehngarten.fhemswitch.ConfigLightsceneRow oldRow : configDataOnlyOld.lightsceneRows) {
                    configDataOnly.lightsceneRows.add(new de.fehngarten.fhemswitch.data.ConfigLightsceneRow(oldRow.unit, oldRow.name, oldRow.enabled, oldRow.isHeader, oldRow.showHeader));
                }

                configDataOnly.connectionPW = configDataOnlyOld.connectionPW;
                configDataOnly.layoutLandscape = configDataOnlyOld.layoutLandscape;
                configDataOnly.layoutPortrait = configDataOnlyOld.layoutPortrait;
                configDataOnly.switchCols = configDataOnlyOld.switchCols;
                configDataOnly.valueCols = configDataOnlyOld.valueCols;
                configDataOnly.commandCols = configDataOnlyOld.commandCols;

            }
        } catch (Exception e) {
            if (BuildConfig.DEBUG) Log.d("configIO", "config.data no old version");
            //configDataOnly = new ConfigDataOnly();
        }
        return configDataOnly;
    }

}
