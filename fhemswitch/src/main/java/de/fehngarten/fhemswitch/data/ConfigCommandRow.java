package de.fehngarten.fhemswitch.data;

public class ConfigCommandRow implements java.io.Serializable {
    private static final long serialVersionUID = 1L;
    public String name;
    public String command;
    public Boolean enabled;

    public ConfigCommandRow() {
        this.name = "";
        this.command = "";
        this.enabled = false;
    }
}