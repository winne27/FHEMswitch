package de.fehngarten.fhemswitch.data;

import android.content.Context;
//import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import de.fehngarten.fhemswitch.BuildConfig;

import static de.fehngarten.fhemswitch.global.Settings.settingsConfigFileName;

public class ConfigDataIO {
    private Context mContext;
    private String[] types;
    private final String TAG = "ConfigDataIO";
    
    public ConfigDataIO(Context context) {
        mContext = context;
    }

    public ConfigDataCommon readCommon(int widgetId) {
        ConfigDataCommon configDataCommon;
        Object obj;
        try {
            String filename = settingsConfigFileName + "common";
            FileInputStream f_in = mContext.openFileInput(filename);
            ObjectInputStream obj_in = new ObjectInputStream(f_in);

            obj = obj_in.readObject();
            obj_in.close();

            //if (BuildConfig.DEBUG) Log.d(TAG, "config common data found");
            if (obj instanceof ConfigDataCommon) {
                configDataCommon = (ConfigDataCommon) obj;
                configDataCommon.checkTypes(types);
            } else {
                //if (BuildConfig.DEBUG) Log.d(TAG, "config data not correct ");
                throw new Exception("config data corrupted");
            }
        } catch (Exception e) {
            //if (BuildConfig.DEBUG) Log.d(TAG, "config data with exception");
            //if (BuildConfig.DEBUG) Log.d(TAG, e.getMessage());


            ConfigDataMigrate configDataMigrate = new ConfigDataMigrate(mContext, this);
            configDataCommon = configDataMigrate.doMigrate(widgetId);
            if (configDataCommon == null) {
                configDataCommon = new ConfigDataCommon();
                configDataCommon.init();
            }
        }

        return configDataCommon;
    }

    public ConfigDataInstance readInstance(int instSerial) {
        ConfigDataInstance configDataInstance = new ConfigDataInstance();
        Object obj;
        try {
            String instSerialString = Integer.toString(instSerial);
            String filename = settingsConfigFileName + instSerialString;
            FileInputStream f_in = mContext.openFileInput(filename);
            ObjectInputStream obj_in = new ObjectInputStream(f_in);

            obj = obj_in.readObject();
            obj_in.close();

            //if (BuildConfig.DEBUG) Log.d(TAG, "config " + instSerialString + " data found");
            if (obj instanceof ConfigDataInstance) {
                //if (BuildConfig.DEBUG) Log.d(TAG, "config data ok");
                configDataInstance = (ConfigDataInstance) obj;
            } else {
                //if (BuildConfig.DEBUG) Log.d(TAG, "config data not correct ");
                throw new Exception("config data corrupted");
            }
        } catch (Exception e) {
            //if (BuildConfig.DEBUG) Log.d(TAG, "config data with exception");
            //if (BuildConfig.DEBUG) Log.d(TAG, e.getMessage());
        }

        return configDataInstance;
    }

    public boolean configInstanceExists(int instSerial) {
        String instSerialString = Integer.toString(instSerial);
        String filename = settingsConfigFileName + instSerialString;
        File file = new File(filename);
        return file.exists();
    }

    public Boolean saveCommon(ConfigDataCommon configDataCommon) {
        try {
            String dir = mContext.getFilesDir().getAbsolutePath();
            String filename = settingsConfigFileName + "common";
            File f0 = new File(dir, filename);
            f0.delete();
            FileOutputStream f_out = mContext.openFileOutput(filename, Context.MODE_PRIVATE);
            ObjectOutputStream obj_out = new ObjectOutputStream(f_out);
            obj_out.writeObject(configDataCommon);
            obj_out.close();
            //if (BuildConfig.DEBUG) Log.d("config", "config data written, common");
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public Boolean saveInstance(ConfigDataInstance configDataInstance, int instSerial) {
        try {
            String instSerialString = Integer.toString(instSerial);
            String dir = mContext.getFilesDir().getAbsolutePath();
            String filename = settingsConfigFileName + instSerialString;
            File f0 = new File(dir, filename);
            f0.delete();
            FileOutputStream f_out = mContext.openFileOutput(filename, Context.MODE_PRIVATE);
            ObjectOutputStream obj_out = new ObjectOutputStream(f_out);
            obj_out.writeObject(configDataInstance);
            obj_out.close();
            //if (BuildConfig.DEBUG) Log.d("config", "config data written: " + filename);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    void deleteInstance(int instSerial) {
        String instSerialString = Integer.toString(instSerial);
        String dir = mContext.getFilesDir().getAbsolutePath();
        String filename = settingsConfigFileName + instSerialString;
        File f0 = new File(dir, filename);
        f0.delete();
    }
/*
    public void deleteCommon() {
        Log.d(TAG, "deleteCommon started");
        String dir = mContext.getFilesDir().getAbsolutePath();
        String filename = settingsConfigFileName + "common";
        File f0 = new File(dir, filename);
        f0.delete();
    }
*/
}
