package de.fehngarten.fhemswitch.data;

import java.util.ArrayList;

public class MyLightScenes {
    public ArrayList<MyLightScene> lightScenes = null;
    public ArrayList<Item> items = new ArrayList<>();
    public int itemsCount = 0;

    MyLightScenes() {
        lightScenes = new ArrayList<>();
        items = new ArrayList<>();
        itemsCount = 0;
    }

    public MyLightScene newLightScene(String name, String unit, Boolean showHeader) {
        MyLightScene myLightScene = new MyLightScene(name, unit, false, showHeader);
        lightScenes.add(myLightScene);
        return myLightScene;
    }

    private void aggregate() {
        items = new ArrayList<>();
        itemsCount = 0;
        for (MyLightScene lightScene : lightScenes) {
            if (lightScene.enabled) {
                String lightSceneUnit = lightScene.unit;
                if (lightScene.showHeader) {
                    items.add(new Item(lightScene.unit, lightScene.name, lightScene.unit, true, false, true));
                    itemsCount++;
                }
                for (MyLightScene.Member member : lightScene.members) {
                    if (member.enabled) {
                        lightScene.enabled = true;
                        items.add(new Item(lightSceneUnit, member.name, member.unit, false, false, false));
                        itemsCount++;
                    }
                }
            }
        }
    }

    boolean setMemberActive(String lightSceneUnit, String memberUnit) {
        boolean lightSceneFound = false;
        for (Item item : items) {
            if (lightSceneFound && item.header) {
                break;
            } else if (lightSceneFound) {
                item.activ = item.unit.equals(memberUnit);
            } else if (item.unit.equals(lightSceneUnit)  && item.header) {
                lightSceneFound = true;
            }
        }
        return lightSceneFound;
    }

    public class Item {
        String lightSceneName;
        public String name;
        public String unit;
        public Boolean header;
        public Boolean activ;
        Boolean showHeader;

        Item(String lightSceneName, String name, String unit, Boolean header, Boolean activ, Boolean showHeader) {
            this.lightSceneName = lightSceneName;
            this.name = name;
            this.unit = unit;
            this.header = header;
            this.activ = activ;
            this.showHeader = showHeader;
        }
    }

    public String activateCmd(int pos) {
        return "set " + items.get(pos).lightSceneName + " scene " + items.get(pos).unit;
    }

    public Boolean isLightScene(String unit) {
        for (MyLightScene lightScene : lightScenes) {
            if (lightScene.unit.equals(unit)) {
                return true;
            }
        }
        return false;
    }

    public class MyLightScene {
        public String name;
        public String unit;
        public Boolean enabled;
        public Boolean showHeader;

        public ArrayList<Member> members = new ArrayList<>();

        MyLightScene(String name, String unit, Boolean enabled, Boolean showHeader) {
            this.name = name;
            this.unit = unit;
            this.enabled = enabled;
            this.showHeader = showHeader;
        }

        public void addMember(String name, String unit, Boolean enabled) {
            members.add(new Member(name, unit, enabled));
            if (enabled) {
                this.enabled = true;
            }
            aggregate();
        }

        public Boolean isMember(String unit) {
            for (Member member : members) {
                if (member.unit.equals(unit)) {
                    return true;
                }
            }
            return false;
        }

        public class Member {
            public String name;
            public String unit;
            public Boolean enabled;

            Member(String name, String unit, Boolean enabled) {
                this.name = name;
                this.unit = unit;
                this.enabled = enabled;
            }
        }
    }
}
