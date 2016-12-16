package de.fehngarten.fhemswitch.data;

public class MyIntValue implements Comparable<MyIntValue> {

    public String name;
    public String unit;
    public String value;
    public String setCommand;
    public Float stepSize;
    public int commandExecDelay;

    public void transfer(ConfigIntValueRow configIntValueRow) {
        name = configIntValueRow.name;
        unit = configIntValueRow.unit;
        value = configIntValueRow.value;
        setCommand = configIntValueRow.setCommand;
        stepSize = configIntValueRow.stepSize;
        commandExecDelay = configIntValueRow.commandExecDelay;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public int compareTo(MyIntValue compSwitch) {
        return this.unit.compareToIgnoreCase(compSwitch.unit);
    }
}







