package de.fehngarten.fhemswitch;

//import android.util.Log; 

class MySwitch {
    String name;
    String unit;
    String cmd;
    String icon;

    MySwitch(String name, String unit, String cmd) {
        this.name = name;
        this.unit = unit;
        this.cmd = cmd;
        icon = "off";
    }

    void setIcon(String icon) {
        //Log.i("icon",icon);
        if (icon.equals("on") || icon.equals("off") || icon.equals("set_on") || icon.equals("set_off") || icon.equals("set_toggle")) {
            this.icon = icon;
        } else {
            this.icon = "undefined";
        }

    }

    String activateCmd() {
        return "set " + this.unit + " " + this.cmd;
    }

}
