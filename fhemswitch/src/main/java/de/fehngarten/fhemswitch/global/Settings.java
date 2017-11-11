package de.fehngarten.fhemswitch.global;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import de.fehngarten.fhemswitch.R;
import de.fehngarten.fhemswitch.widget.WidgetService0;
import de.fehngarten.fhemswitch.widget.WidgetService1;
import de.fehngarten.fhemswitch.widget.WidgetService2;
import de.fehngarten.fhemswitch.widget.WidgetService3;

import static de.fehngarten.fhemswitch.global.Consts.COMMANDS;
import static de.fehngarten.fhemswitch.global.Consts.CONFIG_BLOCK_COMMANDS;
import static de.fehngarten.fhemswitch.global.Consts.CONFIG_BLOCK_INTVALUES;
import static de.fehngarten.fhemswitch.global.Consts.CONFIG_BLOCK_LIGHTSCENES;
import static de.fehngarten.fhemswitch.global.Consts.CONFIG_BLOCK_ORIENT;
import static de.fehngarten.fhemswitch.global.Consts.CONFIG_BLOCK_SWITCHES;
import static de.fehngarten.fhemswitch.global.Consts.CONFIG_BLOCK_VALUES;
import static de.fehngarten.fhemswitch.global.Consts.DOWN;
import static de.fehngarten.fhemswitch.global.Consts.DOWNFAST;
import static de.fehngarten.fhemswitch.global.Consts.INTVALUES;
import static de.fehngarten.fhemswitch.global.Consts.LAYOUT_HORIZONTAL;
import static de.fehngarten.fhemswitch.global.Consts.LAYOUT_MIXED;
import static de.fehngarten.fhemswitch.global.Consts.LAYOUT_VERTICAL;
import static de.fehngarten.fhemswitch.global.Consts.LIGHTSCENES;
import static de.fehngarten.fhemswitch.global.Consts.SWITCHES;
import static de.fehngarten.fhemswitch.global.Consts.UP;
import static de.fehngarten.fhemswitch.global.Consts.UPFAST;
import static de.fehngarten.fhemswitch.global.Consts.VALUES;

public final class Settings {

    public static final String[] settingVersionTypes = {Consts.VERSION_APP, Consts.VERSION_FHEMJS};
    public static final String settingsConfigFileName = "config.data.";

    public static final int settingsMaxInst = 4;
    public static final int settingWaitSocketWifi = 60000;
    public static final int settingWaitSocketLong = 900000;
    public static final int settingWaitSocketShort = 30000;
    public static final int settingSocketsConnectionTimeout = 3000;
    public static final int settingDelaySocketCheck = 500;

    public static final int settingDelayDefineBroadcastReceivers = 5000;
    public static final int settingIntervalVersionCheck = 3600000;
    public static final int settingDelayShowVersionCheck = 20000;
    public static final int settingIntervalShowVersionCheck = 600000;
    public static final int settingPagerFirstItem = 0;

    public static final String settingHelpUrl = "https://forum.fhem.de/index.php?topic=36824.0.html";
    public static final String settingHelpIntvaluesUrl = "https://forum.fhem.de/index.php/topic,62655.0.html";
    public static final String settingHelpIconUrl = "https://forum.fhem.de/index.php/topic,62610.0.html";
    public static final String settingHelpUrlHome = "https://forum.fhem.de/index.php/topic,62716.msg541475.html";
    public static final String settingGoogleStoreUrl = "https://play.google.com/store/apps/details?id=de.fehngarten.fhemswitch";
    public static final String settingLicenceKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAh7D+DlsyIr/qs/nzYQHHITVBXoDn8eSsFKGUgjvlJhINvjFUTwiHBmwrTKBIXye6WozJ4QM7Ov3cUXqeDlIz4m8bHCibXzQsra2kWSZagRhHLcrBwVBy1a3JXB74E1VQO0LbPPgnfeL2Uzv4IIS3QvyAJ2Uo5lHJBoTA+jxUIe/YFPovNvhWhZna2oHZlptc07rNydcTShdMzk/Ujv881jJB0GJMUol5OM5/WG+dHpfyplxlolpS/AXX9312VeU7LkRdOUikQ+bPQMT5gbYyWPXoDAKRkJiU6F5LR+xQqxHxNyedy3yZnlkmXDq0l7u1HYkJaY3Pr2hxOo3hAjX2pQIDAQAB";
    public static final Map<String, Integer> settingIcons = new HashMap<>();
    public static final Map<String, Integer> settingHeaderShapes = new HashMap<>();
    public static final Map<String, Integer> settingDefaultShapes = new HashMap<>();
    public static final Map<String, Integer> settingActiveShapes = new HashMap<>();
    public static final ArrayList<Class<?>> settingServiceClasses = new ArrayList<>(settingsMaxInst);
    public static final int[] settingWidgetSel = new int[settingsMaxInst];
    public static final int[] settingShapes = new int[settingsMaxInst];
    public static final int[] settingShapesSelected = new int[settingsMaxInst];
    public static final int[] settingLayouts = new int[3];
    public static final int[] settingConfigBlocks = new int[6];
    public static final int[] settingTabs = new int[6];
    public static final int[] settingHorizontalListViews = new int[11];
    public static final int[] settingVerticalListViews = new int[5];
    public static final int[][] settingMixedListViews = new int[2][4];
    public static final HashMap<String, Float> settingMultiplier = new HashMap<>();
    public static final HashMap<String, String> settingBlockNames = new HashMap<>();
    public static final String[] settingsBlockOrder;

    static {
        settingIcons.put("v_on", R.drawable.v_on);
        settingIcons.put("v_off", R.drawable.v_off);
        settingIcons.put("v_set_toggle", R.drawable.v_toggle);
        settingIcons.put("v_set_off", R.drawable.v_toggle);
        settingIcons.put("v_set_on", R.drawable.v_toggle);
        settingIcons.put("v_ok", R.drawable.prozent10);
        settingIcons.put("v_low", R.drawable.prozent3);
        settingIcons.put("p_1", R.drawable.prozent1);
        settingIcons.put("p_2", R.drawable.prozent2);
        settingIcons.put("p_3", R.drawable.prozent3);
        settingIcons.put("p_4", R.drawable.prozent4);
        settingIcons.put("p_5", R.drawable.prozent5);
        settingIcons.put("p_6", R.drawable.prozent6);
        settingIcons.put("p_7", R.drawable.prozent7);
        settingIcons.put("p_8", R.drawable.prozent8);
        settingIcons.put("p_9", R.drawable.prozent9);
        settingIcons.put("p_10", R.drawable.prozent10);
        settingIcons.put("on", R.drawable.on);
        settingIcons.put("set_on", R.drawable.set_on);
        settingIcons.put("off", R.drawable.off);
        settingIcons.put("set_off", R.drawable.set_off);
        settingIcons.put("set_toggle", R.drawable.set_toggle);
        settingIcons.put("undefined", R.drawable.undefined);
        settingIcons.put("toggle", R.drawable.set_toggle);

        settingServiceClasses.add(WidgetService0.class);
        settingServiceClasses.add(WidgetService1.class);
        settingServiceClasses.add(WidgetService2.class);
        settingServiceClasses.add(WidgetService3.class);

        settingShapes[0] = R.drawable.widget_shape_0;
        settingShapes[1] = R.drawable.widget_shape_1;
        settingShapes[2] = R.drawable.widget_shape_2;
        settingShapes[3] = R.drawable.widget_shape_3;

        settingShapesSelected[0] = R.drawable.widget_shape_0_selected;
        settingShapesSelected[1] = R.drawable.widget_shape_1_selected;
        settingShapesSelected[2] = R.drawable.widget_shape_2_selected;
        settingShapesSelected[3] = R.drawable.widget_shape_3_selected;

        settingWidgetSel[0] = R.id.widgetsel_0;
        settingWidgetSel[1] = R.id.widgetsel_1;
        settingWidgetSel[2] = R.id.widgetsel_2;
        settingWidgetSel[3] = R.id.widgetsel_3;

        settingLayouts[LAYOUT_HORIZONTAL] = R.layout.widget_layout_horizontal;
        settingLayouts[LAYOUT_VERTICAL] = R.layout.widget_layout_vertical;
        settingLayouts[LAYOUT_MIXED] = R.layout.widget_layout_mixed;

        settingVerticalListViews[0] = R.id.vertical_listview_0;
        settingVerticalListViews[1] = R.id.vertical_listview_1;
        settingVerticalListViews[2] = R.id.vertical_listview_2;
        settingVerticalListViews[3] = R.id.vertical_listview_3;
        settingVerticalListViews[4] = R.id.vertical_listview_4;

        settingHorizontalListViews[0] = R.id.horizontal_listview_0;
        settingHorizontalListViews[1] = R.id.horizontal_listview_1;
        settingHorizontalListViews[2] = R.id.horizontal_listview_2;
        settingHorizontalListViews[3] = R.id.horizontal_listview_3;
        settingHorizontalListViews[4] = R.id.horizontal_listview_4;
        settingHorizontalListViews[5] = R.id.horizontal_listview_5;
        settingHorizontalListViews[6] = R.id.horizontal_listview_6;
        settingHorizontalListViews[7] = R.id.horizontal_listview_7;
        settingHorizontalListViews[8] = R.id.horizontal_listview_8;
        settingHorizontalListViews[9] = R.id.horizontal_listview_9;
        settingHorizontalListViews[10] = R.id.horizontal_listview_10;

        settingMixedListViews[0][0] = R.id.mixed_listview_0_0;
        settingMixedListViews[0][1] = R.id.mixed_listview_0_1;
        settingMixedListViews[0][2] = R.id.mixed_listview_0_2;
        settingMixedListViews[0][3] = R.id.mixed_listview_0_3;
        settingMixedListViews[1][0] = R.id.mixed_listview_1_0;
        settingMixedListViews[1][1] = R.id.mixed_listview_1_1;
        settingMixedListViews[1][2] = R.id.mixed_listview_1_2;
        settingMixedListViews[1][3] = R.id.mixed_listview_1_3;

        settingMultiplier.put(DOWNFAST, (float) -3);
        settingMultiplier.put(DOWN, (float) -1);
        settingMultiplier.put(UP, (float) 1);
        settingMultiplier.put(UPFAST, (float) 3);

        settingConfigBlocks[CONFIG_BLOCK_ORIENT] = R.layout.config_block_orient;
        settingConfigBlocks[CONFIG_BLOCK_SWITCHES] = R.layout.config_block_switches;
        settingConfigBlocks[CONFIG_BLOCK_LIGHTSCENES] = R.layout.config_block_lightscenes;
        settingConfigBlocks[CONFIG_BLOCK_VALUES] = R.layout.config_block_values;
        settingConfigBlocks[CONFIG_BLOCK_INTVALUES] = R.layout.config_block_intvalues;
        settingConfigBlocks[CONFIG_BLOCK_COMMANDS] = R.layout.config_block_commands;

        settingTabs[CONFIG_BLOCK_ORIENT] = R.id.tab0;
        settingTabs[CONFIG_BLOCK_SWITCHES] = R.id.tab1;
        settingTabs[CONFIG_BLOCK_LIGHTSCENES] = R.id.tab2;
        settingTabs[CONFIG_BLOCK_VALUES] = R.id.tab3;
        settingTabs[CONFIG_BLOCK_INTVALUES] = R.id.tab4;
        settingTabs[CONFIG_BLOCK_COMMANDS] = R.id.tab5;

        settingHeaderShapes.put("default", R.drawable.widget_shape_header);
        settingHeaderShapes.put("first", R.drawable.widget_shape_header_first);
        settingHeaderShapes.put("last", R.drawable.widget_shape_header_last);
        settingHeaderShapes.put("both", R.drawable.widget_shape_header_both);

        settingDefaultShapes.put("default", R.drawable.widget_shape_default);
        settingDefaultShapes.put("first", R.drawable.widget_shape_default_first);
        settingDefaultShapes.put("last", R.drawable.widget_shape_default_last);
        settingDefaultShapes.put("both", R.drawable.widget_shape_default_both);

        settingActiveShapes.put("default", R.drawable.widget_shape_active);
        settingActiveShapes.put("first", R.drawable.widget_shape_active_first);
        settingActiveShapes.put("last", R.drawable.widget_shape_active_last);
        settingActiveShapes.put("both", R.drawable.widget_shape_active_both);

        settingsBlockOrder = new  String[] {SWITCHES, LIGHTSCENES, VALUES, INTVALUES, COMMANDS };
    }
}
