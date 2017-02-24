package de.fehngarten.fhemswitch.widget.listviews;

import android.content.Intent;
import android.widget.RemoteViewsService;

import static de.fehngarten.fhemswitch.global.Consts.ACTCOL;
import static de.fehngarten.fhemswitch.global.Consts.FHEM_TYPE;

//import android.util.Log;

public class CommonListviewService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        int col = intent.getIntExtra(ACTCOL, -1);
        String type = intent.getStringExtra(FHEM_TYPE);

        switch (type) {
            case "switch":
                return new SwitchesFactory(getApplicationContext(), intent, col);
            case "value":
                return new ValuesFactory(getApplicationContext(), intent, col);
            case "lightscene":
                return new LightScenesFactory(getApplicationContext(), intent);
            case "command":
                return new CommandsFactory(getApplicationContext(), intent, col);
           case "intvalue":
                return new IntValuesFactory(getApplicationContext(), intent);
        }
        return null;
    }
}