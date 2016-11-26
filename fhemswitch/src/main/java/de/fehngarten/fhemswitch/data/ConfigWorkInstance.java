package de.fehngarten.fhemswitch.data;

import java.util.ArrayList;
import java.util.List;

//import de.fehngarten.fhemswitch.MyLightScenes.MyLightScene;

public class ConfigWorkInstance {
    public List<MySwitch> switches;
    public List<MySwitch> switchesDisabled;
    public List<ArrayList<MySwitch>> switchesCols;

    public MyLightScenes lightScenes;

    public List<MyValue> values;
    public List<ArrayList<MyValue>> valuesCols;
    public List<MyValue> valuesDisabled;

    public List<MyCommand> commands;
    public List<ArrayList<MyCommand>> commandsCols;

    public void init() {
        switches = new ArrayList<>();
        switchesDisabled = new ArrayList<>();
        switchesCols = new ArrayList<>();

        lightScenes = new MyLightScenes();

        values = new ArrayList<>();
        valuesCols = new ArrayList<>();
        valuesDisabled = new ArrayList<>();

        commands = new ArrayList<>();
        commandsCols = new ArrayList<>();
    }

    public int setSwitchIcon(String unit, String value) {
        for (int actCol = 0; actCol < switchesCols.size(); actCol++) {
            for (MySwitch mySwitch : switchesCols.get(actCol)) {
                if (mySwitch.unit.equals(unit)) {
                    mySwitch.setIcon(value);
                    return actCol;
                }
            }
        }
        return -1;
    }

    public int setValue(String unit, String value) {
        for (int actCol = 0; actCol < valuesCols.size(); actCol++) {
            for (MyValue myValue : valuesCols.get(actCol)) {
                if (myValue.unit.equals(unit)) {
                    myValue.setValue(value);
                    return actCol;
                }
            }
        }
        return -1;
    }

    public boolean setLightscene(String unit, String member) {
        return lightScenes.setMemberActive(unit, member);
    }

    public ArrayList<String> getSwitchesList() {
        ArrayList<String> switchesList = new ArrayList<>();
        for (MySwitch mySwitch : switches) {
            switchesList.add(mySwitch.unit);
        }
        return switchesList;
    }

    public ArrayList<String> getValuesList() {
        ArrayList<String> valuesList = new ArrayList<>();
        for (MyValue myValue : values) {
            valuesList.add(myValue.unit);
        }
        return valuesList;
    }

    public ArrayList<String> getLightScenesList()
    {
        ArrayList<String> lightScenesList = new ArrayList<>();
        for (MyLightScenes.MyLightScene myLightScene : lightScenes.lightScenes)
        {
            if (myLightScene.enabled)
            {
                lightScenesList.add(myLightScene.unit);
            }
        }
        return lightScenesList;
    }
}
