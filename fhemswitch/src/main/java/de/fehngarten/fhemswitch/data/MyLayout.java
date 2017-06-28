package de.fehngarten.fhemswitch.data;

import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import de.fehngarten.fhemswitch.R;
//import android.util.Log;

public class MyLayout {
    public HashMap<String, ArrayList<Integer>> layout;
    public HashMap<String, Integer> rowsPerCol;
    public ArrayList<Integer> goneViews;

    public MyLayout(int layoutId, int switchCols, int valueCols, int commandCols, int switchCount, int lightsceneCount,
                    int valueCount, int commandCount, int intValueCount) {
        layout = new HashMap<>();
        rowsPerCol = new HashMap<>();
        goneViews = new ArrayList<>();

        if (layoutId == 0) {
            buildLayoutHorizontal(switchCols, valueCols, commandCols, switchCount, lightsceneCount, valueCount, commandCount, intValueCount);
        }
        if (layoutId == 1) {
            buildLayoutVertical(switchCount, lightsceneCount, valueCount, commandCount, intValueCount);
            initRowsPerCol();
        }
        if (layoutId == 2) {
            buildLayoutMixed(switchCount, lightsceneCount, valueCount, commandCount, intValueCount);
            initRowsPerCol();
        }
    }

    private void buildLayoutVertical(int switchCount, int lightsceneCount, int valueCount, int commandCount, int intValueCount) {
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

        if (intValueCount > 0) {
            ArrayList<Integer> listViewIds = new ArrayList<>();
            listViewIds.add(R.id.intvalues);
            layout.put("intvalue", listViewIds);
        } else {
            goneViews.add(R.id.intvalues);
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

    private void buildLayoutMixed(int switchCount, int lightsceneCount, int valueCount, int commandCount, int intValueCount) {
        ArrayList<MyCounter> counter = new ArrayList<>();
        counter.add(new MyCounter("switch", switchCount * 9));
        counter.add(new MyCounter("lightscene", lightsceneCount * 8));
        counter.add(new MyCounter("value", valueCount * 8));
        counter.add(new MyCounter("command", commandCount * 8));
        counter.add(new MyCounter("intvalue", intValueCount * 11));
        Collections.sort(counter);

        ArrayList<Integer> listViewIds = new ArrayList<>();
        if (counter.get(0).count >= counter.get(1).count + counter.get(2).count + counter.get(3).count + counter.get(4).count) {
            listViewIds.add(R.id.mixed00);
            layout.put(counter.get(0).type, listViewIds);
            goneViews.add(R.id.mixed01);

            if (counter.get(1).count > 0) {
                listViewIds = new ArrayList<>();
                listViewIds.add(R.id.mixed10);
                layout.put(counter.get(1).type, listViewIds);
            } else {
                goneViews.add(R.id.mixed10);
            }

            if (counter.get(2).count > 0) {
                listViewIds = new ArrayList<>();
                listViewIds.add(R.id.mixed11);
                layout.put(counter.get(2).type, listViewIds);
            } else {
                goneViews.add(R.id.mixed11);
            }

            if (counter.get(3).count > 0) {
                listViewIds = new ArrayList<>();
                listViewIds.add(R.id.mixed12);
                layout.put(counter.get(3).type, listViewIds);
            } else {
                goneViews.add(R.id.mixed12);
            }

            if (counter.get(4).count > 0) {
                listViewIds = new ArrayList<>();
                listViewIds.add(R.id.mixed13);
                layout.put(counter.get(4).type, listViewIds);
            } else {
                goneViews.add(R.id.mixed13);
            }
        } else {
            listViewIds.add(R.id.mixed00);
            layout.put(counter.get(0).type, listViewIds);

            int lastIndex = counter.size() - 1;
            int curIndex = 1;
            for (int i = lastIndex; i > 0; i--) {
                if (counter.get(i).count > 0) {
                    curIndex = i;
                    break;
                }
            }

            if (counter.get(curIndex).count > 0) {
                listViewIds = new ArrayList<>();
                listViewIds.add(R.id.mixed01);
                layout.put(counter.get(curIndex).type, listViewIds);
                counter.get(curIndex).count = 0;
            } else {
                goneViews.add(R.id.mixed01);
            }

            if (counter.get(1).count > 0) {
                listViewIds = new ArrayList<>();
                listViewIds.add(R.id.mixed10);
                layout.put(counter.get(1).type, listViewIds);
            } else {
                goneViews.add(R.id.mixed10);
            }

            if (counter.get(2).count > 0) {
                listViewIds = new ArrayList<>();
                listViewIds.add(R.id.mixed11);
                layout.put(counter.get(2).type, listViewIds);
            } else {
                goneViews.add(R.id.mixed11);
            }

            if (counter.get(3).count > 0) {
                listViewIds = new ArrayList<>();
                listViewIds.add(R.id.mixed12);
                layout.put(counter.get(3).type, listViewIds);
            } else {
                goneViews.add(R.id.mixed12);
            }

            if (counter.get(4).count > 0) {
                listViewIds = new ArrayList<>();
                listViewIds.add(R.id.mixed13);
                layout.put(counter.get(4).type, listViewIds);
            } else {
                goneViews.add(R.id.mixed13);
            }
        }
    }

    private void buildLayoutHorizontal(int switchCols, int valueCols, int commandCols, int switchCount, int lightsceneCount,
                                       int valueCount, int commandCount, int intValueCount) {
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

        if (intValueCount > 0) {
            ArrayList<Integer> listViewIds = new ArrayList<>();
            listViewIds.add(R.id.intvalues);
            layout.put("intvalue", listViewIds);
        } else {
            goneViews.add(R.id.intvalues);
        }
    }

    private void initRowsPerCol() {
        rowsPerCol.put("value", 99);
        rowsPerCol.put("switch", 99);
        rowsPerCol.put("command", 99);
        rowsPerCol.put("lightscene", 99);
    }
}
