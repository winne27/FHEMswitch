package de.fehngarten.fhemswitch.data;

public class RowReading implements Comparable<RowReading> {
    public String name;
    public String unit;
    public String reading;
    public String value;
    public Boolean useIcon;

    public RowReading(String name, String reading, String unit, Boolean useIcon) {
        this.name = name;
        this.unit = unit;
        this.reading = reading;
        this.value = "";
        this.useIcon = useIcon;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public int compareTo(RowReading compSwitch) {
        return this.unit.compareToIgnoreCase(compSwitch.unit);
    }
}