package de.fehngarten.fhemswitch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
//import android.util.Log;

class MyLayout {
    HashMap<String, ArrayList<Integer>> layout;
    HashMap<String, Integer> rowsPerCol;
    ArrayList<Integer> goneViews;

    MyLayout(int layoutId, int switchCols, int valueCols, int commandCols, int switchCount, int lightsceneCount,
             int valueCount, int commandCount) {
        layout = new HashMap<>();
        rowsPerCol = new HashMap<>();
        goneViews = new ArrayList<>();

        if (layoutId == 0) {
            buildLayoutHorizontal(switchCols, valueCols, commandCols, switchCount, lightsceneCount, valueCount, commandCount);
        }
        if (layoutId == 1) {
            buildLayoutVertical(switchCount, lightsceneCount, valueCount, commandCount);
            initRowsPerCol();
        }
        if (layoutId == 2) {
            buildLayoutMixed(switchCount, lightsceneCount, valueCount, commandCount);
            initRowsPerCol();
        }
    }

    private void buildLayoutVertical(int switchCount, int lightsceneCount, int valueCount, int commandCount) {
        if (switchCount > 0) {
            ArrayList<Integer> listViewIds = new ArrayList<>();
            listViewIds.add(R.id.switches0);
            layout.put("switch", listViewIds);
        } else {
            goneViews.add(R.id.switches0);
        }

        if (valueCount > 0) {
            ArrayList<Integer> listViewIds = new ArrayList<>();
            listViewIds.add(R.id.values0);
            layout.put("value", listViewIds);
        } else {
            goneViews.add(R.id.values0);
        }

        if (commandCount > 0) {
            ArrayList<Integer> listViewIds = new ArrayList<>();
            listViewIds.add(R.id.commands0);
            layout.put("command", listViewIds);
        } else {
            goneViews.add(R.id.commands0);
        }

        if (lightsceneCount > 0) {
            ArrayList<Integer> listViewIds = new ArrayList<>();
            listViewIds.add(R.id.lightscenes);
            layout.put("lightscene", listViewIds);
        } else {
            goneViews.add(R.id.lightscenes);
        }
    }

    private void buildLayoutMixed(int switchCount, int lightsceneCount, int valueCount, int commandCount) {
        ArrayList<MyCounter> counter = new ArrayList<>();
        counter.add(new MyCounter("switch", switchCount * 10));
        counter.add(new MyCounter("lightscene", lightsceneCount * 8));
        counter.add(new MyCounter("value", valueCount * 7));
        counter.add(new MyCounter("command", commandCount * 10));
        Collections.sort(counter);

        if (counter.get(0).count >= counter.get(1).count + counter.get(2).count + counter.get(3).count) {
            ArrayList<Integer> listViewIds0 = new ArrayList<>();
            listViewIds0.add(R.id.mixed00);
            layout.put(counter.get(0).type, listViewIds0);
            goneViews.add(R.id.mixed01);

            if (counter.get(1).count > 0) {
                ArrayList<Integer> listViewIds1 = new ArrayList<>();
                listViewIds1.add(R.id.mixed10);
                layout.put(counter.get(1).type, listViewIds1);
            } else {
                goneViews.add(R.id.mixed10);
            }

            if (counter.get(2).count > 0) {
                ArrayList<Integer> listViewIds2 = new ArrayList<>();
                listViewIds2.add(R.id.mixed11);
                layout.put(counter.get(2).type, listViewIds2);
            } else {
                goneViews.add(R.id.mixed11);
            }

            if (counter.get(3).count > 0) {
                ArrayList<Integer> listViewIds3 = new ArrayList<>();
                listViewIds3.add(R.id.mixed12);
                layout.put(counter.get(3).type, listViewIds3);
            } else {
                goneViews.add(R.id.mixed12);
            }
        } else {
            ArrayList<Integer> listViewIds0 = new ArrayList<>();
            listViewIds0.add(R.id.mixed00);
            layout.put(counter.get(0).type, listViewIds0);

            if (counter.get(3).count > 0) {
                ArrayList<Integer> listViewIds3 = new ArrayList<>();
                listViewIds3.add(R.id.mixed01);
                layout.put(counter.get(3).type, listViewIds3);
            } else {
                goneViews.add(R.id.mixed01);
            }

            if (counter.get(1).count > 0) {
                ArrayList<Integer> listViewIds1 = new ArrayList<>();
                listViewIds1.add(R.id.mixed10);
                layout.put(counter.get(1).type, listViewIds1);
            } else {
                goneViews.add(R.id.mixed10);
            }

            if (counter.get(2).count > 0) {
                ArrayList<Integer> listViewIds2 = new ArrayList<>();
                listViewIds2.add(R.id.mixed11);
                layout.put(counter.get(2).type, listViewIds2);
            } else {
                goneViews.add(R.id.mixed11);
            }
            goneViews.add(R.id.mixed12);
        }
    }

    private void buildLayoutHorizontal(int switchCols, int valueCols, int commandCols, int switchCount, int lightsceneCount,
                                       int valueCount, int commandCount) {
        if (switchCount > 0) {
            rowsPerCol.put("switch", (int) Math.ceil((double) switchCount / (double) (switchCols + 1)));

            ArrayList<Integer> listViewIds = new ArrayList<>();
            listViewIds.add(R.id.switches0);
            if (switchCols > 0) {
                listViewIds.add(R.id.switches1);
            } else {
                goneViews.add(R.id.switches1);
            }

            if (switchCols > 1) {
                listViewIds.add(R.id.switches2);
            } else {
                goneViews.add(R.id.switches2);
            }

            layout.put("switch", listViewIds);
        } else {
            goneViews.add(R.id.switches0);
            goneViews.add(R.id.switches1);
            goneViews.add(R.id.switches2);
        }

        if (valueCount > 0) {
            rowsPerCol.put("value", (int) Math.ceil((double) valueCount / (double) (valueCols + 1)));

            ArrayList<Integer> listViewIds = new ArrayList<>();
            listViewIds.add(R.id.values0);
            if (valueCols > 0) {
                listViewIds.add(R.id.values1);
            } else {
                goneViews.add(R.id.values1);
            }

            if (valueCols > 1) {
                listViewIds.add(R.id.values2);
            } else {
                goneViews.add(R.id.values2);
            }

            layout.put("value", listViewIds);
        } else {
            goneViews.add(R.id.values0);
            goneViews.add(R.id.values1);
            goneViews.add(R.id.values2);
        }

        if (commandCount > 0) {
            rowsPerCol.put("command", (int) Math.ceil((double) commandCount / (double) (commandCols + 1)));

            ArrayList<Integer> listViewIds = new ArrayList<>();
            listViewIds.add(R.id.commands0);
            if (commandCols > 0) {
                listViewIds.add(R.id.commands1);
            } else {
                goneViews.add(R.id.commands1);
            }

            if (commandCols > 1) {
                listViewIds.add(R.id.commands2);
            } else {
                goneViews.add(R.id.commands2);
            }

            layout.put("command", listViewIds);
        } else {
            goneViews.add(R.id.commands0);
            goneViews.add(R.id.commands1);
            goneViews.add(R.id.commands2);
        }

        if (lightsceneCount > 0) {
            ArrayList<Integer> listViewIds = new ArrayList<>();
            listViewIds.add(R.id.lightscenes);
            layout.put("lightscene", listViewIds);
        } else {
            goneViews.add(R.id.lightscenes);
        }
    }

    private void initRowsPerCol() {
        rowsPerCol.put("value", 99);
        rowsPerCol.put("switch", 99);
        rowsPerCol.put("command", 99);
        rowsPerCol.put("lightscene", 99);
    }
}
