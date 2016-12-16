package de.fehngarten.fhemswitch.data;

public class MyValue implements Comparable<MyValue> {
    public String name;
    public String unit;
    public String value;
    public Boolean useIcon;

    public MyValue(String name, String unit, Boolean useIcon) {
        this.name = name;
        this.unit = unit;
        this.value = "";
        this.useIcon = useIcon;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public int compareTo(MyValue compSwitch) {
        return this.unit.compareToIgnoreCase(compSwitch.unit);
    }
}