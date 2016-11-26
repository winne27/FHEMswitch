package de.fehngarten.fhemswitch.global;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import de.fehngarten.fhemswitch.R;
import de.fehngarten.fhemswitch.widget.WidgetService0;
import de.fehngarten.fhemswitch.widget.WidgetService1;
import de.fehngarten.fhemswitch.widget.WidgetService2;
import de.fehngarten.fhemswitch.widget.WidgetService3;

import static de.fehngarten.fhemswitch.global.Consts.LAYOUT_HORIZONTAL;
import static de.fehngarten.fhemswitch.global.Consts.LAYOUT_MIXED;
import static de.fehngarten.fhemswitch.global.Consts.LAYOUT_VERTICAL;

public final class Settings {

    public static final String[] settingVersionTypes = {Consts.VERSION_APP, Consts.VERSION_FHEMJS};
    public static final String settingsConfigFileName = "config.data.";

    public static final int settingsMaxInst = 4;
    public static final int settingWaitCyclesShort = 15;
    public static final int settingWaitIntervalLong = 91000;
    public static final int settingWaitIntervalShort = 4000;
    public static final int settingSocketsConnectionTimeout = 15000;
    public static final int settingDelaySocketCheck = 1000;
    public static final int settingDelayVersionCheck = 10000;
    public static final int settingDelayShowVersionCheck = 20000;
    public static final int settingWaitIntervalVersionCheck = 3600000;
    public static final int settingWaitIntervalVersionShowCheck = 600000;

    public static final String settingGoogleStoreUrl = "https://play.google.com/store/apps/details?id=de.fehngarten.fhemswitch";
    public static final String settingLicenceKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAh7D+DlsyIr/qs/nzYQHHITVBXoDn8eSsFKGUgjvlJhINvjFUTwiHBmwrTKBIXye6WozJ4QM7Ov3cUXqeDlIz4m8bHCibXzQsra2kWSZagRhHLcrBwVBy1a3JXB74E1VQO0LbPPgnfeL2Uzv4IIS3QvyAJ2Uo5lHJBoTA+jxUIe/YFPovNvhWhZna2oHZlptc07rNydcTShdMzk/Ujv881jJB0GJMUol5OM5/WG+dHpfyplxlolpS/AXX9312VeU7LkRdOUikQ+bPQMT5gbYyWPXoDAKRkJiU6F5LR+xQqxHxNyedy3yZnlkmXDq0l7u1HYkJaY3Pr2hxOo3hAjX2pQIDAQAB";
    public static final Map<String, Integer> settingIcons = new HashMap<>();
    public static final ArrayList<Class> settingServiceClasses = new ArrayList<>(settingsMaxInst);
    public static final int[] settingWidgetSel = new int[settingsMaxInst];
    public static final int[] settingShapes = new int[settingsMaxInst];
    public static final int[] settingLayouts = new int[3];


    static {
        settingIcons.put("on", R.drawable.on);
        settingIcons.put("set_on", R.drawable.set_on);
        settingIcons.put("off", R.drawable.off);
        settingIcons.put("set_off", R.drawable.set_off);
        settingIcons.put("set_toggle", R.drawable.set_toggle);
        settingIcons.put("undefined", R.drawable.undefined);
        settingIcons.put("toggle", R.drawable.undefined);

        settingServiceClasses.add(WidgetService0.class);
        settingServiceClasses.add(WidgetService1.class);
        settingServiceClasses.add(WidgetService2.class);
        settingServiceClasses.add(WidgetService3.class);

        settingShapes[0] = R.drawable.myshape0;
        settingShapes[1] = R.drawable.myshape1;
        settingShapes[2] = R.drawable.myshape2;
        settingShapes[3] = R.drawable.myshape3;

        settingWidgetSel[0] = R.id.widgetsel_0;
        settingWidgetSel[1] = R.id.widgetsel_1;
        settingWidgetSel[2] = R.id.widgetsel_2;
        settingWidgetSel[3] = R.id.widgetsel_3;

        settingLayouts[LAYOUT_HORIZONTAL] = R.layout.main_layout_horizontal;
        settingLayouts[LAYOUT_VERTICAL] = R.layout.main_layout_vertical;
        settingLayouts[LAYOUT_MIXED] = R.layout.main_layout_mixed;
    }
}
