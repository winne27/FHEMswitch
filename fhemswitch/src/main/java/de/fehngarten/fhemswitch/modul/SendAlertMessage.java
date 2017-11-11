package de.fehngarten.fhemswitch.modul;

import android.content.Context;
import de.fehngarten.fhemswitch.R;

public class SendAlertMessage extends SendMessage {

    public SendAlertMessage(Context context, String msg) {
        super(context);
        header = context.getString(R.string.error_header);
        dialog.setNeutralButton(context.getString(R.string.ok), null);
        doSendMessage(msg);
    }
}
