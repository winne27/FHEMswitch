package de.fehngarten.fhemswitch.data;

public class ConfigValueRow implements java.io.Serializable {
    private static final long serialVersionUID = 1L;
    public String unit;
    public String name;
    public String value;
    public Boolean enabled;
    public Boolean useIcon = false;

    public ConfigValueRow(String unit, String name, String value, Boolean enabled, Boolean useIcon) {
        this.unit = unit;
        this.name = name;
        this.value = value;
        this.enabled = enabled;
        this.useIcon = useIcon;
    }
}