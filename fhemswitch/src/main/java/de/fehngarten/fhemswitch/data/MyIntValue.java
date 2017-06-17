package de.fehngarten.fhemswitch.data;

import android.support.annotation.NonNull;

public class MyIntValue implements Comparable<MyIntValue> {

    public String name;
    public String unit;
    public String value;
    public String setCommand;
    public Float stepSize;
    public int commandExecDelay;
    public boolean isTime;

    public void transfer(ConfigIntValueRow configIntValueRow) {
        name = configIntValueRow.name;
        unit = configIntValueRow.unit;
        value = configIntValueRow.value;
        setCommand = configIntValueRow.setCommand;
        stepSize = configIntValueRow.stepSize;
        if (configIntValueRow.isTime == null) {
            isTime = false;
        } else {
            isTime = configIntValueRow.isTime;
        }
        commandExecDelay = configIntValueRow.commandExecDelay;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public int compareTo(@NonNull MyIntValue compSwitch) {
        return this.unit.compareToIgnoreCase(compSwitch.unit);
    }
}







