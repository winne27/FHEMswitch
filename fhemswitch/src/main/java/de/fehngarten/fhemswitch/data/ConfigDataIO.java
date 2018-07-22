package de.fehngarten.fhemswitch.data;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

import static de.fehngarten.fhemswitch.global.Settings.settingsBlockOrder;
import static de.fehngarten.fhemswitch.global.Settings.settingsConfigFileName;

public class ConfigDataIO {
    private Context mContext;

    public ConfigDataIO(Context context) {
        mContext = context;
    }

    // -----------------------------------------------
    // ---- common config stuff
    // -----------------------------------------------

    public ConfigDataCommon readCommon() {
        ConfigDataCommon configDataCommon;
        try {
            String filename = settingsConfigFileName + "common";
            File file = new File(mContext.getFilesDir(), filename);
            configDataCommon = physicalReadCommon(file);
        } catch (Exception e) {
            configDataCommon = new ConfigDataCommon();
            configDataCommon.init();
        }
        return configDataCommon;
    }

    public void saveCommon(ConfigDataCommon configDataCommon) {
        try {
            String filename = settingsConfigFileName + "common";
            File file = new File(mContext.getFilesDir(), filename);
            physicalWriteCommon(configDataCommon, file);
        } catch (Exception ignored) {
        }
    }

    // -----------------------------------------------
    // ---- instance config stuff
    // -----------------------------------------------

    public ConfigDataInstance readInstance(int instSerial, boolean checkMigrateReadings) {
        ConfigDataInstance configDataInstance;
        try {
            String instSerialString = Integer.toString(instSerial);
            String filename = settingsConfigFileName + instSerialString;
            File file = new File(mContext.getFilesDir(), filename);
            configDataInstance = physicalReadInstance(file);

            if (configDataInstance.blockOrder == null) {
                configDataInstance.blockOrder = settingsBlockOrder;
            }
            if (configDataInstance.widgetName == null || configDataInstance.widgetName.equals("")) {
                configDataInstance.widgetName = "Widget " + Integer.toString(instSerial + 1);
            }

            if (checkMigrateReadings && !configDataInstance.readingsMigrated && configDataInstance.valueRows.size() > 0) {

            }

        } catch (Exception e) {
            return null;
        }
        return configDataInstance;
    }

    public boolean configInstanceExists(int instSerial) {
        String instSerialString = Integer.toString(instSerial);
        String filename = settingsConfigFileName + instSerialString;
        File file = new File(filename);
        return file.exists();
    }

    public void saveInstance(ConfigDataInstance configDataInstance, int instSerial) {
        try {
            String instSerialString = Integer.toString(instSerial);
            String filename = settingsConfigFileName + instSerialString;
            File file = new File(mContext.getFilesDir(), filename);
            physicalWriteInstance(configDataInstance, file);
        } catch (Exception ignored) {
        }
    }

    void deleteInstance(int instSerial) {
        String instSerialString = Integer.toString(instSerial);
        String dir = mContext.getFilesDir().getAbsolutePath();
        String filename = settingsConfigFileName + instSerialString;
        File f0 = new File(dir, filename);
        f0.delete();
    }

    // -----------------------------------------------
    // ---- export stuff
    // -----------------------------------------------

    public boolean doExport(ConfigDataCommon configDataCommon) {
        if (isExternalStorageWritable()) {
            try {
                File myDir = new File(getBackupPath());
                if (!myDir.exists()) {
                    myDir.mkdirs();
                }

                doExportCommon(configDataCommon, myDir);

                List<Integer> instSerials = configDataCommon.getAllInstSerials();
                for (int instSerial : instSerials) {
                    doExportInstance(instSerial, myDir);
                }

                return true;
            } catch (Exception e) {
                return false;
            }
        } else {
            return false;
        }
    }

    private void doExportCommon(ConfigDataCommon configDataCommon, File myDir) {
        try {
            File file = new File(myDir, "urls.conf");
            // do not save password
            configDataCommon.fhemjsPW = "";
            physicalWriteCommon(configDataCommon, file);
        } catch (Exception e) {
            Log.e("save external common", e.getMessage());
        }
    }

    private void doExportInstance(Integer instSerial, File myDir) {
        try {
            ConfigDataInstance configDataInstance = readInstance(instSerial, false);
            if (configDataInstance != null) {
                File file = new File(myDir, configDataInstance.widgetName.replace(" ", "_") + ".conf");
                physicalWriteInstance(configDataInstance, file);
            }
        } catch (Exception e) {
            Log.e("save external instance", e.getMessage());
        }
    }

    // -----------------------------------------------
    // ---- import stuff
    // -----------------------------------------------

    public ConfigDataCommon doImportCommon() {
        if (isExternalStorageWritable()) {
            try {
                File file = new File(getBackupPath(), "urls.conf");
                return physicalReadCommon(file);
            } catch (Exception e) {
                return null;
            }
        } else {
            return null;
        }
    }

    public ConfigDataInstance doImportInstance(String fileName) {
        if (isExternalStorageWritable()) {
            try {
                File file = new File(getBackupPath(), fileName);
                return physicalReadInstance(file);
            } catch (Exception e) {
                return null;
            }
        } else {
            return null;
        }
    }

    private boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    public String getBackupPath() {
        return Environment.getExternalStorageDirectory().toString() + "/fhemswitch";
    }

    public CharSequence[] getAvailableInstanceFiles() {
        String fileName;
        String path = getBackupPath();
        File directory = new File(path);
        File[] files = directory.listFiles();
        if (files == null) {
            return null;
        }
        CharSequence[] instanceFiles = new CharSequence[files.length];
        int j = 0;
        for (File file : files) {
            fileName = file.getName();
            if (!fileName.equals("urls.conf")) {
                instanceFiles[j] = fileName;
                j++;
            }
        }
        CharSequence[] instanceFilesSel = new CharSequence[j];
        System.arraycopy(instanceFiles, 0, instanceFilesSel, 0, j);
        return instanceFilesSel;
    }
    // -----------------------------------------------
    // ---- physical IOs
    // -----------------------------------------------

    private ConfigDataCommon physicalReadCommon(File file) {
        ConfigDataCommon configDataCommon;
        Object obj;
        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            ObjectInputStream obj_in = new ObjectInputStream(fileInputStream);

            obj = obj_in.readObject();
            obj_in.close();

            if (obj instanceof ConfigDataCommon) {
                configDataCommon = (ConfigDataCommon) obj;
            } else {
                throw new Exception("config data corrupted");
            }
        } catch (Exception e) {
            configDataCommon = new ConfigDataCommon();
            configDataCommon.init();
        }
        return configDataCommon;
    }

    private void physicalWriteCommon(ConfigDataCommon configDataCommon, File file) {
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(file, false);
            ObjectOutputStream obj_out = new ObjectOutputStream(fileOutputStream);
            obj_out.writeObject(configDataCommon);
            obj_out.close();
        } catch (Exception e) {
            Log.e("write instance", Log.getStackTraceString(e));
        }
    }

    private ConfigDataInstance physicalReadInstance(File file) {
        ConfigDataInstance configDataInstance = new ConfigDataInstance();
        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);

            Object obj = objectInputStream.readObject();
            objectInputStream.close();

            if (obj instanceof ConfigDataInstance) {
                configDataInstance = (ConfigDataInstance) obj;
            }
        } catch (Exception e) {}
        return configDataInstance;
    }

    private void physicalWriteInstance(ConfigDataInstance configDataInstance, File file) {
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(file, false);
            ObjectOutputStream obj_out = new ObjectOutputStream(fileOutputStream);
            obj_out.writeObject(configDataInstance);
            obj_out.close();
        } catch (Exception e) {
            Log.e("write instance", Log.getStackTraceString(e));
        }
    }
}
