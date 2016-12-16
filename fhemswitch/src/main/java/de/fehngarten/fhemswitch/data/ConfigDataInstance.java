package de.fehngarten.fhemswitch.data;


import java.util.ArrayList;

public class ConfigDataInstance implements java.io.Serializable {

    private static final long serialVersionUID = 1L;
    public ArrayList<ConfigSwitchRow> switchRows;
    public ArrayList<ConfigLightsceneRow> lightsceneRows;
    public ArrayList<ConfigValueRow> valueRows;
    public ArrayList<ConfigIntValueRow> intValueRows;
    public ArrayList<ConfigCommandRow> commandRows;
    public int layoutLandscape;
    public int layoutPortrait;
    public int switchCols;
    public int valueCols;
    public int commandCols;
    public int widgetId;

    public ConfigDataInstance() {
        switchRows = new ArrayList<>();
        lightsceneRows = new ArrayList<>();
        valueRows = new ArrayList<>();
        intValueRows = new ArrayList<>();
        commandRows = new ArrayList<>();
        layoutLandscape = 1;
        layoutPortrait = 0;
        switchCols = 0;
        valueCols = 0;
        commandCols = 0;
        widgetId = -1;
    }
}
