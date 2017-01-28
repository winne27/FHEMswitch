package de.fehngarten.fhemswitch.data;

//import android.util.Log;

import java.util.HashMap;

import static de.fehngarten.fhemswitch.global.Settings.settingVersionTypes;
import static de.fehngarten.fhemswitch.global.Settings.settingsMaxInst;

public class ConfigDataCommon implements java.io.Serializable {
    private static final long serialVersionUID = 1L;
    public String urlFhemjs = "https://your-domain.tld:8086";
    public String urlFhempl = "https://your-domain.tld:8082/fhem";
    public String fhemjsPW = "";
    public String urlFhemjsLocal = "http://local_ip_addr:8087";
    public String urlFhemplLocal = "http://local_ip_addr:8083/fhem";
    public String bssId = "";
    public HashMap<String, String> suppressedVersions;
    public int[] instances = new int[settingsMaxInst];

    public void init() {
        suppressedVersions = new HashMap<>();
        for (String type : settingVersionTypes) {
            suppressedVersions.put(type, "");
        }

        for (int j = 0; j < settingsMaxInst; j++) {
            instances[j] = 0;
        }
    }

    public int getInstByWidgetid(int widgetId) {
        for (int j = 0; j < settingsMaxInst; j++) {
            if (instances[j] == widgetId) {
                return j;
            }
        }
        return -1;
    }

    public int getFreeInstance(int widgetId) {
        for (int j = 0; j < settingsMaxInst; j++) {
            if (instances[j] == 0) {
                instances[j] = widgetId;
                return j;
            }
        }
        return -1;
    }

    public int getFirstInstance() {
        for (int j = 0; j < settingsMaxInst; j++) {
            if (instances[j] > 0) {
                return j;
            }
        }

        return -1;
    }

    public void removeUnused(ConfigDataIO configDataIO, int[] widgetIds) {
        for (int j = 0; j < settingsMaxInst; j++) {
            if (instances[j] > 0) {
                boolean found = false;
                for (int widgetId : widgetIds) {
                    if (widgetId == instances[j]) {
                        found = true;
                    }
                }
                if (!found) {
                    instances[j] = 0;
                    configDataIO.saveCommon(this);
                }
            }
        }
    }

    public int delete(ConfigDataIO configDataIO, int widgetId) {
        for (int j = 0; j < settingsMaxInst; j++) {
            if (instances[j] == widgetId) {
                instances[j] = 0;
                configDataIO.saveCommon(this);
                configDataIO.deleteInstance(j);
                return j;
            }
        }
        return -1;
    }

    public int getWidgetCount() {
        int i = 0;
        for (int j = 0; j < settingsMaxInst; j++) {
            if (instances[j] > 0) {
                i++;
            }
        }
        return i;
    }
}
