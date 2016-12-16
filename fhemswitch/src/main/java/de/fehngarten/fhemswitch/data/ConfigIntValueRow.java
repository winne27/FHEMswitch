package de.fehngarten.fhemswitch.data;

public class ConfigIntValueRow implements java.io.Serializable {
    private static final long serialVersionUID = 1L;

    public String name;
    public String unit;
    public String value;
    public String setCommand = "";
    public Float stepSize = (float) 1.0;
    public int commandExecDelay = 1000;
    public Boolean enabled;

    public ConfigIntValueRow(String unit, String name, String value, String setCommand, Float stepSize, int commandExecDelay, Boolean enabled) {
        this.unit = unit;
        this.name = name;
        this.value = value;
        this.setCommand =       setCommand;
        this.stepSize =         stepSize;
        this.commandExecDelay = commandExecDelay;
        this.enabled = enabled;
    }
}