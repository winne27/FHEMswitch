package de.fehngarten.fhemswitch.modul;

import android.content.Context;

import de.fehngarten.fhemswitch.R;

public class SendStatusMessage extends SendMessage {

    public SendStatusMessage(Context context, String msg) {
        header = context.getString(R.string.hint);
        doSendMessage(context, msg);
    }
}
