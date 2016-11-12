package de.fehngarten.fhemswitch.data;

import android.util.Log;
import java.util.ArrayList;
import java.util.HashMap;

public class ConfigDataOnly implements java.io.Serializable {
    private static final long serialVersionUID = 1L;
    public String urljs = "https://your-domain.tld:8086";
    public String urlpl = "https://your-domain.tld:8082/fhem";
    public ArrayList<ConfigSwitchRow> switchRows = new ArrayList<>();
    public ArrayList<ConfigLightsceneRow> lightsceneRows = new ArrayList<>();
    public ArrayList<ConfigValueRow> valueRows = new ArrayList<>();
    public ArrayList<ConfigCommandRow> commandRows = new ArrayList<>();
    public String connectionPW = "";
    public int layoutLandscape;
    public int layoutPortrait;
    public int switchCols;
    public int valueCols;
    public int commandCols;
    public HashMap<String, String> suppressedVersions;

    public ConfigDataOnly(String[] types) {

        this.switchRows = new ArrayList<>();
        this.lightsceneRows = new ArrayList<>();
        this.valueRows = new ArrayList<>();
        this.commandRows = new ArrayList<>();

        this.layoutPortrait = 1;
        this.layoutLandscape = 0;
        this.switchCols = 0;
        this.valueCols = 0;
        this.commandCols = 0;
        suppressedVersions = new HashMap<>();
        for (String type : types) {
            suppressedVersions.put(type,"");
        }
    }

    public void checkTypes(String[] types) {
        if (types != null) {
            if (suppressedVersions == null) {
                suppressedVersions = new HashMap<>();
            }
            for (String type : types) {
                if (!suppressedVersions.containsKey(type)) {
                   suppressedVersions.put(type, "");
                }
            }

        }
    }
}
