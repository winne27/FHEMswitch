package de.fehngarten.fhemswitch.data;

import java.util.ArrayList;
import java.util.List;

//import de.fehngarten.fhemswitch.MyLightScenes.MyLightScene;

public class ConfigData {
    public List<MySwitch> switches = new ArrayList<>();
    public List<MySwitch> switchesDisabled = new ArrayList<>();
    public List<ArrayList<MySwitch>> switchesCols = new ArrayList<>();

    public MyLightScenes lightScenes = new MyLightScenes();

    public List<MyValue> values = new ArrayList<>();
    public List<ArrayList<MyValue>> valuesCols = new ArrayList<>();
    public List<MyValue> valuesDisabled = new ArrayList<>();

    public List<MyCommand> commands = new ArrayList<>();
    public List<ArrayList<MyCommand>> commandsCols = new ArrayList<>();
    //public List<MyCommand> commandsDisabled = new ArrayList<MyCommand>();

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

    /*
    public MySwitch isInSwitchesDisabled(String unit)
    {
       for (MySwitch mySwitch : switchesDisabled)
       {
          if (mySwitch.unit.equals(unit)) { return mySwitch; }
       }
       return null;
    }

    public MyValue isInValues(String unit)
    {
       for (MyValue myValue : values)
       {
          if (myValue.unit.equals(unit)) { return myValue; }
       }
       return null;
    }

    public MyValue isInValuesDisabled(String unit)
    {
       for (MyValue myValue : valuesDisabled)
       {
          if (myValue.unit.equals(unit)) { return myValue; }
       }
       return null;
    }
 */
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

 /*
   public ArrayList<String> getCommandsList()
   {
      ArrayList<String> commandsList = new ArrayList<>();
      for (MyCommand myCommand : commands)
      {
         commandsList.add(myCommand.name);
      }
      return commandsList;
   }

*/
}
