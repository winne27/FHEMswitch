package de.fehngarten.fhemswitch;

import android.content.Context;

import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;

class VersionData  implements java.io.Serializable{

    String storeVersion;
    Boolean newStoreVersionShown;
    String serverVersion;
    Boolean newServerVersionShown;

    VersionData() {
        this.storeVersion = "";
        this.newStoreVersionShown = false;
        this.serverVersion = "";
        this.newServerVersionShown = false;
    }
/*
    void saveVersionData() {
        String dir = getFilesDir().getAbsolutePath();
        File f0 = new File(dir, WidgetService.CONFIGFILE);
        f0.delete();
        FileOutputStream f_out = openFileOutput(WidgetService.CONFIGFILE, Context.MODE_PRIVATE);
        ObjectOutputStream obj_out = new ObjectOutputStream(f_out);
        obj_out.writeObject(this);
    }
*/
}
