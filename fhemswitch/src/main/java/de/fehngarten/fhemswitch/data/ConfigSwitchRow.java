package de.fehngarten.fhemswitch.data;

public class ConfigSwitchRow implements java.io.Serializable {
    private static final long serialVersionUID = 1L;
    public String unit;
    public String name;
    public Boolean enabled;
    public String cmd;
    public Integer confirm;

    public ConfigSwitchRow(String unit, String name, Boolean enabled, String cmd, Integer confirm) {
        this.unit = unit;
        this.name = name;
        this.enabled = enabled;
        this.cmd = cmd;
        if (confirm == null) {
            this.confirm = 0;
        } else {
            this.confirm = confirm;
        }
    }
}
