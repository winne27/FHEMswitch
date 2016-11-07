package de.fehngarten.fhemswitch.data;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class ConfigDataOnlyIO {
    public static final String CONFIGFILE = "config.data";

    public ConfigDataOnly configDataOnly;
    private Context mContext;

    public ConfigDataOnlyIO(Context context) {

        mContext = context;
    }

    public ConfigDataOnly read() {
        Object obj = null;
        try {
            FileInputStream f_in = mContext.openFileInput(CONFIGFILE);
            ObjectInputStream obj_in = new ObjectInputStream(f_in);

            obj = obj_in.readObject();
            obj_in.close();

            Log.d("configIO", "config.data found");
            if (obj instanceof ConfigDataOnly) {
                Log.d("configIO", "config.data ok");
                configDataOnly = (ConfigDataOnly) obj;
                configDataOnly.checkNewProps();
            }
        } catch (Exception e) {
            Log.d("configIO", "config.data corrupted");
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
            Log.d("config", "config.data written");
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public ConfigDataOnly checkForOldVersion(Object obj) {
        try {
            if (obj != null && obj instanceof ConfigDataOnly) {
                Log.d("configIO", "config.data old version found");
                configDataOnlyOld = (ConfigDataOnly) obj;
            } else {
                configDataOnly = new ConfigDataOnly();
            }
        } catch (Exception e) {
            Log.d("configIO", "config.data no old version");
            configDataOnly = new ConfigDataOnly();
        }
        return configDataOnly;
    }

}
