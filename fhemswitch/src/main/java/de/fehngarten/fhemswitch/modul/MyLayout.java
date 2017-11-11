package de.fehngarten.fhemswitch.modul;

import com.google.firebase.crash.FirebaseCrash;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

import static de.fehngarten.fhemswitch.global.Consts.COMMANDS;
import static de.fehngarten.fhemswitch.global.Consts.INTVALUES;
import static de.fehngarten.fhemswitch.global.Consts.LAYOUT_HORIZONTAL;
import static de.fehngarten.fhemswitch.global.Consts.LAYOUT_MIXED;
import static de.fehngarten.fhemswitch.global.Consts.LAYOUT_VERTICAL;
import static de.fehngarten.fhemswitch.global.Consts.LIGHTSCENES;
import static de.fehngarten.fhemswitch.global.Consts.SWITCHES;
import static de.fehngarten.fhemswitch.global.Consts.VALUES;
import static de.fehngarten.fhemswitch.global.Settings.settingHorizontalListViews;
import static de.fehngarten.fhemswitch.global.Settings.settingMixedListViews;
import static de.fehngarten.fhemswitch.global.Settings.settingVerticalListViews;

public class MyLayout {
    public HashMap<String, ArrayList<Integer>> layout;
    public HashMap<String, Integer> rowsPerCol;
    public LinkedHashMap<String, Integer> mixedLayout;

    public MyLayout(int layoutId, Map<String, Integer> blockCols, Map<String, Integer> blockCounts, String[] blockOrder) {
        layout = new HashMap<>();
        rowsPerCol = new HashMap<>();

        if (layoutId == LAYOUT_HORIZONTAL) {
            buildLayoutHorizontal(blockCols, blockCounts, blockOrder);
        }
        if (layoutId == LAYOUT_VERTICAL) {
            buildLayoutVertical(blockCounts, blockOrder);
            initRowsPerCol();
        }
        if (layoutId == LAYOUT_MIXED) {
            buildLayoutMixed(blockCounts);
            initRowsPerCol();
        }
    }

    private void buildLayoutVertical(Map<String, Integer> blockCounts, String[] blockOrder) {
        int curIndex = 0;
        for (String block : blockOrder) {
            if (blockCounts.get(block) > 0) {
                ArrayList<Integer> listViewIds = new ArrayList<>();
                listViewIds.add(settingVerticalListViews[curIndex]);
                curIndex++;
                layout.put(block, listViewIds);
            }
        }
    }

    private void buildLayoutMixed(Map<String, Integer> blockCounts) {
        Integer debugCount = 0;
        String trace = "";
        try {
            DescValueComparator bvc = new DescValueComparator(blockCounts);
            TreeMap<String, Integer> sortedBlockCounts = new TreeMap<>(bvc);
            sortedBlockCounts.putAll(blockCounts);
            debugCount++;   //1
            debugCount++;   //2
            debugCount++;   //3

            ArrayList<String> leftCol = new ArrayList<>();
            ArrayList<String> rightCol = new ArrayList<>();
            debugCount++;   //4

            int leftSum = 0;
            int rightSum = 0;
            debugCount++;   //5

            // first
            Map.Entry<String, Integer> block;
            
            block = sortedBlockCounts.pollFirstEntry();
            
            leftCol.add(block.getKey());
            leftSum = leftSum + block.getValue();
            debugCount++;   //6

            //second
            block = sortedBlockCounts.pollFirstEntry();
            debugCount++;   //7
            if (block.getValue() > 0) {
                trace += "a";
                rightCol.add(block.getKey());
                rightSum = rightSum + block.getValue();

                // third
                block = sortedBlockCounts.pollFirstEntry();
                if (block.getValue() > 0) {
                    trace += "b";
                    rightCol.add(block.getKey());
                    rightSum = rightSum + block.getValue();

                    // forth
                    block = sortedBlockCounts.pollFirstEntry();
                    if (block.getValue() > 0) {
                        trace += "c";
                        if (rightSum > leftSum) {
                            leftCol.add(block.getKey());
                            leftSum = leftSum + block.getValue();
                        } else {
                            rightCol.add(block.getKey());
                            rightSum = rightSum + block.getValue();
                        }
                        // fifth
                        block = sortedBlockCounts.pollFirstEntry();
                        if (block.getValue() > 0) {
                            trace += "d";
                            if (rightSum > leftSum) {
                                leftCol.add(block.getKey());
                                leftSum = leftSum + block.getValue();
                            } else {
                                rightCol.add(block.getKey());
                                rightSum = rightSum + block.getValue();
                            }
                        }
                    }
                }
            }

            // switch cols if right is taller when left
            debugCount++;   //8
            if (rightSum > leftSum) {
                trace += "e";
                ArrayList<String> leftColTemp = new ArrayList<>();

                for (String blockname : leftCol) {
                    leftColTemp.add(blockname);
                }

                leftCol = new ArrayList<>();
                for (String blockname : rightCol) {
                    leftCol.add(blockname);
                }

                rightCol = new ArrayList<>();
                for (String blockname : leftColTemp) {
                    rightCol.add(blockname);
                }
            }
            debugCount++;   //9
            int curIndex = 0;
            mixedLayout = new LinkedHashMap<>();

            for (String blockname : leftCol) {
                trace += "f";
                ArrayList<Integer> listViewIds = new ArrayList<>();
                listViewIds.add(settingMixedListViews[0][curIndex]);
                layout.put(blockname, listViewIds);
                mixedLayout.put(blockname, 0);
                curIndex++;
            }
            debugCount++;   //10

            curIndex = 0;
            for (String blockname : rightCol) {
                ArrayList<Integer> listViewIds = new ArrayList<>();
                listViewIds.add(settingMixedListViews[1][curIndex]);
                layout.put(blockname, listViewIds);
                mixedLayout.put(blockname, 1);
                curIndex++;
            }
        } catch (Exception e) {
            FirebaseCrash.log(blockCounts.toString());
            FirebaseCrash.log("debugCount-a: " + debugCount.toString());
            FirebaseCrash.log("debugTrace-a: " + trace);
            FirebaseCrash.report(e);
        } catch (NoClassDefFoundError e) {
            FirebaseCrash.log(blockCounts.toString());
            FirebaseCrash.log("debugCount-b: " + debugCount.toString());
            FirebaseCrash.log("debugTrace-b: " + trace);
            FirebaseCrash.report(e);
        }
    }

    private void buildLayoutHorizontal(Map<String, Integer> blockCols, Map<String, Integer> blockCounts, String[] blockOrder) {

        int curIndex = 0;

        for (String block : blockOrder) {
            int blockCount = blockCounts.get(block);
            if (blockCount > 0) {
                rowsPerCol.put(block, (int) Math.ceil((double) blockCount / (double) (blockCols.get(block) + 1)));
                ArrayList<Integer> listViewIds = new ArrayList<>();
                for (int i = 0; i <= blockCols.get(block); i++) {
                    listViewIds.add(settingHorizontalListViews[curIndex]);
                    curIndex++;
                }
                layout.put(block, listViewIds);
            }
        }
    }

    private void initRowsPerCol() {
        rowsPerCol.put(VALUES, 99);
        rowsPerCol.put(SWITCHES, 99);
        rowsPerCol.put(COMMANDS, 99);
        rowsPerCol.put(LIGHTSCENES, 99);
        rowsPerCol.put(INTVALUES, 99);
    }

    class DescValueComparator implements Comparator<String> {
        Map<String, Integer> base;

        public DescValueComparator(Map<String, Integer> base) {
            this.base = base;
        }

        // Note: this comparator imposes orderings that are inconsistent with
        // equals.
        public int compare(String a, String b) {
            if (base.get(a) >= base.get(b)) {
                return -1;
            } else {
                return 1;
            } // returning 0 would merge keys
        }
    }
}
