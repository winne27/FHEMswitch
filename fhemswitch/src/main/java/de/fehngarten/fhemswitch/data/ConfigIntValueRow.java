package de.fehngarten.fhemswitch.data;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class ConfigIntValueRow implements java.io.Serializable {
    private static final long serialVersionUID = 1L;

    public String name;
    public String unit;
    public String value;
    public String setCommand = "";
    public Float stepSize = (float) 1.0;
    public int commandExecDelay = 1000;
    public Boolean enabled;
    public Boolean isTime;

    public ConfigIntValueRow(String unit, String name, String value, String setCommand, Float stepSize, int commandExecDelay, Boolean enabled) {
        this.unit = unit;
        this.name = name;
        this.value = value;
        this.setCommand =       setCommand;
        this.stepSize =         stepSize;
        this.commandExecDelay = commandExecDelay;
        this.enabled = enabled;
        DateFormat df = new SimpleDateFormat( "H:m", Locale.GERMAN);
        try {
            df.parse(value);
            this.isTime = true;
        } catch ( ParseException exc ) {
            this.isTime = false;
        }
    }
}