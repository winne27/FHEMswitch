package de.fehngarten.fhemswitch.modul;

import com.google.firebase.crash.FirebaseCrash;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.fehngarten.fhemswitch.data.ConfigWorkInstance;
import de.fehngarten.fhemswitch.data.RowCommand;
import de.fehngarten.fhemswitch.data.RowIntValue;
import de.fehngarten.fhemswitch.data.RowLightScenes;
import de.fehngarten.fhemswitch.data.RowSwitch;
import de.fehngarten.fhemswitch.data.RowValue;

import static de.fehngarten.fhemswitch.global.Consts.BOTH;
import static de.fehngarten.fhemswitch.global.Consts.COMMANDS;
import static de.fehngarten.fhemswitch.global.Consts.DEFAULT;
import static de.fehngarten.fhemswitch.global.Consts.FIRST;
import static de.fehngarten.fhemswitch.global.Consts.HEADER_SEPERATOR;
import static de.fehngarten.fhemswitch.global.Consts.INTVALUES;
import static de.fehngarten.fhemswitch.global.Consts.LAST;
import static de.fehngarten.fhemswitch.global.Consts.LAYOUT_HORIZONTAL;
import static de.fehngarten.fhemswitch.global.Consts.LAYOUT_MIXED;
import static de.fehngarten.fhemswitch.global.Consts.LAYOUT_VERTICAL;
import static de.fehngarten.fhemswitch.global.Consts.LIGHTSCENES;
import static de.fehngarten.fhemswitch.global.Consts.SWITCHES;
import static de.fehngarten.fhemswitch.global.Consts.VALUES;

public class MyRoundedCorners {

    private HashMap<String, ArrayList<ArrayList<Item>>> items;
    private boolean setNextFirst;
    private Pointer lastPos;
    private HashMap<String, Integer> mixedLayout;

    public MyRoundedCorners(ConfigWorkInstance curInstance, Map<String, Integer> blockCounts, HashMap<String, Integer> mixedLayout) {
        try {
            items = new HashMap<>();
            this.mixedLayout = mixedLayout;
            lastPos = new Pointer("", 0, 0);
            init(curInstance, blockCounts);
        } catch (Exception e) {
            FirebaseCrash.log("blockCounts: " + blockCounts.toString());
            FirebaseCrash.log("mixedLayout: " + mixedLayout.toString());
            FirebaseCrash.report(e);
        }
    }

    public String getType(String block, int col, int pos) {
        try {
            Pointer pointer = new Pointer(block, col, pos);
            return getItem(pointer).getType();
        } catch (Exception e) {
            return DEFAULT;
        }
    }

    private void setFirst(Pointer pointer) {
        try {
            if (!pointer.block.equals("")) {
                getItem(pointer).setIsFirst();
            }
        } catch (Exception e) {
        }
    }

    private void setLast(Pointer pointer) {
        try {
            if (!pointer.block.equals("")) {
                getItem(pointer).setIsLast();
            }
        } catch (Exception e) {
        }
    }

    private Item initItem(Pointer pointer) {
        if (!items.containsKey(pointer.block)) {
            items.put(pointer.block, new ArrayList<>());
        }

        if (items.get(pointer.block).size() <= pointer.col) {
            items.get(pointer.block).add(new ArrayList<>());
        }

        if (items.get(pointer.block).get(pointer.col).size() <= pointer.pos) {
            items.get(pointer.block).get(pointer.col).add(new Item());
        }
        return items.get(pointer.block).get(pointer.col).get(pointer.pos);
    }


    private void init(ConfigWorkInstance curInstance, Map<String, Integer> blockCounts) {
        switch (curInstance.curLayout) {
            case LAYOUT_HORIZONTAL:
                initHorizontal(curInstance, blockCounts);
                break;
            case LAYOUT_VERTICAL:
                initVertical(curInstance, blockCounts);
                break;
            case LAYOUT_MIXED:
                initMixed(curInstance);
                break;
        }
    }

    private void initHorizontal(ConfigWorkInstance curInstance, Map<String, Integer> blockCounts) {
        if (blockCounts.get(SWITCHES) > 0) {
            for (int i = 0; i < curInstance.switchesCols.size(); i++) {
                setNextFirst = true;
                walkThrough(curInstance, SWITCHES, i);
                if (!lastPos.block.equals("")) {
                    setLast(lastPos);
                }
            }
        }

        if (blockCounts.get(VALUES) > 0) {
            for (int i = 0; i < curInstance.valuesCols.size(); i++) {
                setNextFirst = true;
                walkThrough(curInstance, VALUES, i);
                if (!lastPos.block.equals("")) {
                    setLast(lastPos);
                }
            }
        }

        if (blockCounts.get(COMMANDS) > 0) {
            for (int i = 0; i < curInstance.commandsCols.size(); i++) {
                setNextFirst = true;
                walkThrough(curInstance, COMMANDS, i);
                if (!lastPos.block.equals("")) {
                    setLast(lastPos);
                }
            }
        }

        if (blockCounts.get(INTVALUES) > 0) {
            setNextFirst = true;
            walkThrough(curInstance, INTVALUES, 0);
            if (!lastPos.block.equals("")) {
                setLast(lastPos);
            }
        }

        if (blockCounts.get(LIGHTSCENES) > 0) {
            setNextFirst = true;
            walkThrough(curInstance, LIGHTSCENES, 0);
            if (!lastPos.block.equals("")) {
                setLast(lastPos);
            }
        }
    }

    private void initVertical(ConfigWorkInstance curInstance, Map<String, Integer> blockCounts) {
        setNextFirst = true;
        for (String block : curInstance.blockOrder) {
            if (blockCounts.get(block) > 0) {
                walkThrough(curInstance, block, 0);
            }
        }
        setLast(lastPos);
    }

    private void initMixed(ConfigWorkInstance curInstance) {
        setNextFirst = true;
        int oldCol = 0;

        for (Map.Entry<String, Integer> entry : mixedLayout.entrySet()) {
            String block = entry.getKey();
            int col = entry.getValue();
            if (oldCol != col) {
                if (!lastPos.block.equals("")) {
                    setLast(lastPos);
                }
                setNextFirst = true;
                oldCol = col;
            }
            walkThrough(curInstance, block, 0);
        }
        if (!lastPos.block.equals("")) {
            setLast(lastPos);
        }
    }

    private void walkThrough(ConfigWorkInstance curInstance, String block, int col) {
        switch (block) {
            case SWITCHES:
                walkThroughSwitches(curInstance.switchesCols.get(col), col);
                break;
            case VALUES:
                walkThroughValues(curInstance.valuesCols.get(col), col);
                break;
            case INTVALUES:
                walkThroughIntValues(curInstance.intValues, 0);
                break;
            case LIGHTSCENES:
                walkThroughLightscenes(curInstance.lightScenes, 0);
                break;
            case COMMANDS:
                walkThroughCommands(curInstance.commandsCols.get(col), col);
                break;
        }
    }

    private void walkThroughSwitches(ArrayList<RowSwitch> switches, int col) {
        for (int i = 0; i < switches.size(); i++) {
            Pointer pointer = new Pointer(SWITCHES, col, i);
            initItem(pointer);
            if (setNextFirst) {
                setFirst(pointer);
                setNextFirst = false;
            }

            if (switches.get(i).unit.equals(HEADER_SEPERATOR) && switches.get(i).name.equals("")) {
                if (!lastPos.block.equals("")) {
                    getItem(lastPos).setIsLast();
                    setLast(lastPos);
                }
                setNextFirst = true;
            }
            lastPos.set(SWITCHES, col, i);
        }
    }

    private void walkThroughValues(ArrayList<RowValue> values, int col) {
        for (int i = 0; i < values.size(); i++) {
            Pointer pointer = new Pointer(VALUES, col, i);
            initItem(pointer);
            if (setNextFirst) {
                setFirst(pointer);
                setNextFirst = false;
            }

            if (values.get(i).unit.equals(HEADER_SEPERATOR) && values.get(i).name.equals("")) {
                if (!lastPos.block.equals("")) {
                    setLast(lastPos);
                }
                setNextFirst = true;
            }
            lastPos.set(VALUES, col, i);
        }
    }

    private void walkThroughIntValues(List<RowIntValue> intValues, int col) {
        for (int i = 0; i < intValues.size(); i++) {
            Pointer pointer = new Pointer(INTVALUES, col, i);
            initItem(pointer);
            if (setNextFirst) {
                setFirst(pointer);
                setNextFirst = false;
            }

            if (intValues.get(i).unit.equals(HEADER_SEPERATOR) && intValues.get(i).name.equals("")) {
                if (!lastPos.block.equals("")) {
                    setLast(lastPos);
                }
                setNextFirst = true;
            }
            lastPos.set(INTVALUES, col, i);
        }
    }

    private void walkThroughCommands(ArrayList<RowCommand> commands, int col) {
        for (int i = 0; i < commands.size(); i++) {
            Pointer pointer = new Pointer(COMMANDS, col, i);
            initItem(pointer);
            if (setNextFirst) {
                setFirst(pointer);
                setNextFirst = false;
            }
            lastPos.set(COMMANDS, col, i);
        }
    }

    private void walkThroughLightscenes(RowLightScenes rowLightScenes, int col) {
        for (int i = 0; i < rowLightScenes.items.size(); i++) {
            Pointer pointer = new Pointer(LIGHTSCENES, col, i);
            initItem(pointer);
            if (setNextFirst) {
                setFirst(pointer);
                setNextFirst = false;
            }

            lastPos.set(LIGHTSCENES, col, i);
        }
    }

    private Item getItem(Pointer pointer) {
        return items.get(pointer.block).get(pointer.col).get(pointer.pos);
    }

    private class Pointer {
        private String block;
        private Integer col;
        private Integer pos;

        private Pointer(String block, int col, int pos) {
            this.block = block;
            this.col = col;
            this.pos = pos;
        }

        private void set(String block, int col, int pos) {
            this.block = block;
            this.col = col;
            this.pos = pos;
        }
    }

    private class Item {
        private boolean isFirst;
        private boolean isLast;
        private boolean isBoth;

        private Item() {
            isFirst = false;
            isLast = false;
            isBoth = false;
        }

        private void setIsFirst() {
            isFirst = true;
            isBoth = isLast;
        }

        private void setIsLast() {
            isLast = true;
            isBoth = isFirst;
        }

        private String getType() {
            if (isBoth) {
                return BOTH;
            } else if (isFirst) {
                return FIRST;
            } else if (isLast) {
                return LAST;
            } else {
                return DEFAULT;
            }
        }
    }
}
