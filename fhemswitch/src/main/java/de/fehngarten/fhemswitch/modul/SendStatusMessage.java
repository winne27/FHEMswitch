package de.fehngarten.fhemswitch.modul;

import android.content.Context;

import de.fehngarten.fhemswitch.R;

public class SendStatusMessage extends SendMessage {

    public SendStatusMessage(Context context, String msg) {
        super(context);
        header = context.getString(R.string.hint);
        dialog.setNeutralButton(context.getString(R.string.ok), null);
        doSendMessage(msg);
    }
}
