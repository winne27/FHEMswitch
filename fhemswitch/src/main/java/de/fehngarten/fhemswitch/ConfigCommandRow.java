package de.fehngarten.fhemswitch;

class ConfigCommandRow implements java.io.Serializable {
    private static final long serialVersionUID = 1L;
    public String name;
    public String command;
    public Boolean enabled;

    ConfigCommandRow(String name, String command, Boolean enabled) {
        this.name = name;
        this.command = command;
        this.enabled = enabled;
    }
}