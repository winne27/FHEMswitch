package de.fehngarten.fhemswitch.modul;

import android.app.AlertDialog;
import android.content.Context;

class SendMessage {

    protected String header;
    public AlertDialog.Builder dialog;
    private Context mContext;

    public SendMessage(Context context) {
        mContext = context;
        dialog = new AlertDialog.Builder(context);
    }

    void doSendMessage(String msg) {
        try {
            dialog.setTitle(header);
            dialog.setMessage(msg);
            dialog.create().show();
        } catch (Exception e) {
            // ignore
        }
    }
}
