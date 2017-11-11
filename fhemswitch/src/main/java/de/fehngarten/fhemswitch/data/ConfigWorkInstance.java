package de.fehngarten.fhemswitch.data;

import java.util.ArrayList;
import java.util.List;

import de.fehngarten.fhemswitch.modul.MyRoundedCorners;

//import de.fehngarten.fhemswitch.MyLightScenes.MyLightScene;

public class ConfigWorkInstance {
    public List<RowSwitch> switches;
    public List<RowSwitch> switchesDisabled;
    public List<ArrayList<RowSwitch>> switchesCols;

    public RowLightScenes lightScenes;

    public List<RowValue> values;
    public List<RowValue> valuesDisabled;
    public List<ArrayList<RowValue>> valuesCols;

    public List<RowIntValue> intValues;
    public List<RowIntValue> intValuesDisabled;

    public List<RowCommand> commands;
    public List<ArrayList<RowCommand>> commandsCols;

    public MyRoundedCorners myRoundedCorners;
    public String[] blockOrder;
    public String widgetName;

    public int curLayout;

    public void init() {
        switches = new ArrayList<>();
        switchesDisabled = new ArrayList<>();
        switchesCols = new ArrayList<>();

        lightScenes = new RowLightScenes();

        values = new ArrayList<>();
        valuesCols = new ArrayList<>();
        valuesDisabled = new ArrayList<>();

        intValues = new ArrayList<>();
        intValuesDisabled = new ArrayList<>();

        commands = new ArrayList<>();
        commandsCols = new ArrayList<>();
    }

    public int setSwitchIcon(String unit, String value) {
        for (int actCol = 0; actCol < switchesCols.size(); actCol++) {
            for (RowSwitch rowSwitch : switchesCols.get(actCol)) {
                if (rowSwitch.unit.equals(unit)) {
                    rowSwitch.setIcon(value);
                    return actCol;
                }
            }
        }
        return -1;
    }

    public int setValue(String unit, String value) {
        for (int actCol = 0; actCol < valuesCols.size(); actCol++) {
            for (RowValue rowValue : valuesCols.get(actCol)) {
                if (rowValue.unit.equals(unit)) {
                    rowValue.setValue(value);
                    return actCol;
                }
            }
        }
        return -1;
    }

    public boolean setIntValue(String unit, String value) {
        for (RowIntValue rowIntValue : intValues) {
            if (rowIntValue.unit.equals(unit)) {
                rowIntValue.setValue(value);
                return true;
            }
        }
        return false;
    }

    public boolean setLightscene(String unit, String member) {
        return lightScenes.setMemberActive(unit, member);
    }

    public ArrayList<String> getSwitchesList() {
        ArrayList<String> switchesList = new ArrayList<>();
        for (RowSwitch rowSwitch : switches) {
            switchesList.add(rowSwitch.unit);
        }
        return switchesList;
    }

    public ArrayList<String> getValuesList() {
        ArrayList<String> valuesList = new ArrayList<>();
        for (RowValue rowValue : values) {
            valuesList.add(rowValue.unit);
        }
        return valuesList;
    }

    public ArrayList<String> getIntValuesList() {
        ArrayList<String> intValuesList = new ArrayList<>();
        for (RowIntValue rowIntValue : intValues) {
            intValuesList.add(rowIntValue.unit);
        }
        return intValuesList;
    }

    public ArrayList<String> getLightScenesList()
    {
        ArrayList<String> lightScenesList = new ArrayList<>();
        for (RowLightScenes.MyLightScene myLightScene : lightScenes.lightScenes)
        {
            if (myLightScene.enabled)
            {
                lightScenesList.add(myLightScene.unit);
            }
        }
        return lightScenesList;
    }
}
