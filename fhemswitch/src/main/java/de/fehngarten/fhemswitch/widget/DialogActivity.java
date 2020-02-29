package de.fehngarten.fhemswitch.widget;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import de.fehngarten.fhemswitch.R;

import static de.fehngarten.fhemswitch.global.Consts.FHEM_COMMAND;
import static de.fehngarten.fhemswitch.global.Consts.INSTSERIAL;
import static de.fehngarten.fhemswitch.global.Settings.settingServiceClasses;

public class DialogActivity extends Activity {
    public Context mContext;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_activity);

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();

        String dialogText = "";

        String icon = bundle.getString("icon");
        if (icon.equals("off")) {
            dialogText = getString(R.string.switchOn) + " " + bundle.getString("name");
        } else if (icon.equals("on")) {
            dialogText = getString(R.string.switchOff) + " " + bundle.getString("name");
        } else if (icon.equals("light" +
                "scene")) {
            dialogText = getString(R.string.switchScene) + " " + bundle.getString("name");
        } else if (icon.equals("command")) {
            dialogText = getString(R.string.switchCommand) + " " + bundle.getString("name");
        }

        TextView txt = findViewById(R.id.w_dialog_txt);
        txt.setText(dialogText + "?");

        Bundle cmdBundle = bundle.getBundle("cmdBundle");
        cmdBundle.putBoolean("isConfirmed", true);

        Button doitbutton = findViewById(R.id.doit_btn);
        doitbutton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                int instSerial = intent.getExtras().getInt(INSTSERIAL);
                Intent commandIntent = new Intent(mContext, settingServiceClasses.get(instSerial));
                commandIntent.setAction(FHEM_COMMAND);
                commandIntent.putExtras(cmdBundle);
                mContext.startService(commandIntent);
                DialogActivity.this.finish();
            }
        });

        Button ohnobutton = findViewById(R.id.ohno_btn);
        ohnobutton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogActivity.this.finish();
            }
        });
    }
}
