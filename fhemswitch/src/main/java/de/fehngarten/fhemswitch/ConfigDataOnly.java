package de.fehngarten.fhemswitch;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

class ConfigDataOnly implements java.io.Serializable {
    private static final long serialVersionUID = 1L;
    String urljs = "https://your-domain.tld:8086";
    String urlpl = "https://your-domain.tld:8082/fhem";
    ArrayList<ConfigSwitchRow> switchRows = new ArrayList<>();
    ArrayList<ConfigLightsceneRow> lightsceneRows = new ArrayList<>();
    ArrayList<ConfigValueRow> valueRows = new ArrayList<>();
    ArrayList<ConfigCommandRow> commandRows = new ArrayList<>();
    String connectionPW = "";
    int layoutLandscape;
    int layoutPortrait;
    int switchCols;
    int valueCols;
    int commandCols;

    int leftColWeight;
    int switchColWeight;
    int lightsceneColWeight;
    int valueColWeight;
    int commandColWeight;

    String suppressNewAppVersion;
    String suppressNewNodeVersion;
    String suppressNewFhemVersion;

    ConfigDataOnly() {

        this.switchRows = new ArrayList<>();
        this.lightsceneRows = new ArrayList<>();
        this.valueRows = new ArrayList<>();
        this.commandRows = new ArrayList<>();

        this.layoutPortrait = 1;
        this.layoutLandscape = 0;
        this.switchCols = 0;
        this.valueCols = 0;
        this.commandCols = 0;

        initNewProps();
        initNewVersions();
    }

    private void initNewVersions() {
        this.suppressNewAppVersion = "";
        this.suppressNewNodeVersion = "";
        this.suppressNewFhemVersion = "";
    }

    private void initNewProps() {
        Log.d("ConfigDataOnly", "initNewProps startet");
        this.leftColWeight = 50;
        this.switchColWeight = 10;
        this.lightsceneColWeight = 10;
        this.valueColWeight = 10;
        this.commandColWeight = 10;
    }

    public void checkNewProps() {
        if (this.leftColWeight == 0) {
            initNewProps();
        }

        if (this.suppressNewFhemVersion == null) {
            initNewVersions();
        }
    }
}
