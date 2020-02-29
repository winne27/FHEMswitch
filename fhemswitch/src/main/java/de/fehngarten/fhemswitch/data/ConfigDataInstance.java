package de.fehngarten.fhemswitch.data;

import java.util.ArrayList;

import static de.fehngarten.fhemswitch.global.Settings.settingsBlockOrder;

public class ConfigDataInstance implements java.io.Serializable {

    private static final long serialVersionUID = 1L;
    public ArrayList<ConfigSwitchRow> switchRows;
    public ArrayList<ConfigLightsceneRow> lightsceneRows;
    public ArrayList<ConfigValueRow> valueRows;
    public ArrayList<ConfigReadingRow> readingRows;
    public ArrayList<ConfigIntValueRow> intValueRows;
    public ArrayList<ConfigCommandRow> commandRows;
    public int layoutLandscape;
    public int layoutPortrait;
    public int switchCols;
    public int valueCols;
    public int commandCols;
    public String[] blockOrder;
    public String widgetName;
    public boolean readingsMigrated = false;
    public boolean confirmCommands = false;

    ConfigDataInstance() {
        switchRows = new ArrayList<>();
        lightsceneRows = new ArrayList<>();
        valueRows = new ArrayList<>();
        readingRows = new ArrayList<>();
        intValueRows = new ArrayList<>();
        commandRows = new ArrayList<>();
        layoutLandscape = 1;
        layoutPortrait = 0;
        switchCols = 0;
        valueCols = 0;
        commandCols = 0;
        blockOrder = settingsBlockOrder;
        widgetName = "";
    }
}
