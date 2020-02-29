package de.fehngarten.fhemswitch.data;

//import android.util.Log; 

public class RowSwitch implements Comparable<RowSwitch> {
    public String name;
    public String unit;
    public String cmd;
    public String icon;
    public Integer confirm;

    public RowSwitch(String name, String unit, String cmd, Integer confirm) {
        this.name = name;
        this.unit = unit;
        this.cmd = cmd;
        icon = "off";
        this.confirm = confirm;
    }

    public void setIcon(String icon) {
        //Log.i("icon",icon);
        if (icon.equals("on") || icon.equals("off") || icon.equals("set_on") || icon.equals("set_off") || icon.equals("set_toggle")) {
            this.icon = icon;
        } /*else { //ignore fucking else states like RSSI and so on
            this.icon = "undefined";
        }*/
    }

    public String activateCmd() {
        return "set " + this.unit + " " + this.cmd;
    }

    @Override
    public int compareTo(RowSwitch compSwitch) {
        return this.unit.compareToIgnoreCase(compSwitch.unit);
    }
}
