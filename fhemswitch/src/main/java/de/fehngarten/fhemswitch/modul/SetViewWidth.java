package de.fehngarten.fhemswitch.modul;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import de.fehngarten.fhemswitch.R;

public class SetViewWidth {
    public SetViewWidth(View view, int width) {
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.width = width;
        view.setLayoutParams(layoutParams);
    }

}
