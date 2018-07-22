package de.fehngarten.fhemswitch.modul;

import android.util.Log;

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
        String trace = "";
        try {
            DescValueComparator bvc = new DescValueComparator(blockCounts);
            TreeMap<String, Integer> sortedBlockCounts = new TreeMap<>(bvc);
            sortedBlockCounts.putAll(blockCounts);

            ArrayList<String> leftCol = new ArrayList<>();
            ArrayList<String> rightCol = new ArrayList<>();

            int leftSum = 0;
            int rightSum = 0;

            // first
            Map.Entry<String, Integer> block;
            
            block = sortedBlockCounts.pollFirstEntry();
            
            leftCol.add(block.getKey());
            leftSum = leftSum + block.getValue();

            //second
            block = sortedBlockCounts.pollFirstEntry();
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

            curIndex = 0;
            for (String blockname : rightCol) {
                ArrayList<Integer> listViewIds = new ArrayList<>();
                listViewIds.add(settingMixedListViews[1][curIndex]);
                layout.put(blockname, listViewIds);
                mixedLayout.put(blockname, 1);
                curIndex++;
            }
        } catch (Exception e) {
            Log.e("buildLayoutMixed", e.getMessage());
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
