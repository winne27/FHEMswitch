package de.fehngarten.fhemswitch;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * Created by Werner on 20.10.2016.
 */

public class ConfigDataOnlyIO {
    public static final String CONFIGFILE = "config.data";

    private ConfigDataOnly configDataOnly;
    private Context mContext;

    ConfigDataOnlyIO (Context context) {
        mContext = context;
    }

    public ConfigDataOnly read() {
        try {
            FileInputStream f_in = mContext.openFileInput(CONFIGFILE);
            ObjectInputStream obj_in = new ObjectInputStream(f_in);

            Object obj = obj_in.readObject();
            obj_in.close();

            //Log.i("config", "config.data found");
            if (obj instanceof ConfigDataOnly) {
                configDataOnly = (ConfigDataOnly) obj;
                configDataOnly.checkNewProps();
            }
        } catch (Exception e) {
            configDataOnly = new ConfigDataOnly();
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
}
