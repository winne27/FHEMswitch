package de.fehngarten.fhemswitch.modul;

import android.app.AlertDialog;
import android.content.Context;

import de.fehngarten.fhemswitch.R;

public class SendMessage {

    protected String header;

    protected void doSendMessage(Context context, String msg) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(context);
        dialog.setTitle(header);
        dialog.setMessage(msg);
        dialog.setNeutralButton(context.getString(R.string.ok), null);
        dialog.create().show();
    }
}
