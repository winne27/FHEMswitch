package de.fehngarten.fhemswitch.data;

public class ConfigReadingRow implements java.io.Serializable {
    private static final long serialVersionUID = 1L;
    public String unit;
    public String reading;
    public String name;
    public String value;
    public Boolean enabled;
    public Boolean useIcon = false;

    public ConfigReadingRow(String unit, String reading, String name, String value, Boolean enabled, Boolean useIcon) {
        this.unit = unit;
        this.reading = reading;
        this.name = name;
        this.value = value;
        this.enabled = enabled;
        this.useIcon = useIcon;
    }
}