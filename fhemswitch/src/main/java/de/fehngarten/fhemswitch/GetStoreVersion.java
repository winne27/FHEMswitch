package de.fehngarten.fhemswitch;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import org.jsoup.Jsoup;
import java.io.IOException;

class GetStoreVersion extends AsyncTask<String, Void, String> {
    Context mContext;
    String mAction;
    static final String LATEST = "LATEST";

    GetStoreVersion(Context context, String action) {
        mContext = context;
        mAction = action;
    }

    @Override
    protected String doInBackground(String... params) {
        String url = mContext.getResources().getString(R.string.googleStoreUrl);
        String latest = "";
        try {
            latest = Jsoup.connect(url)
                    .timeout(30000)
                    .userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
                    .referrer("http://www.google.com")
                    .get()
                    .select("div[itemprop=softwareVersion]")
                    .first()
                    .ownText();

        } catch (IOException e) {
            Log.i("ConfigMain", "read app version failed");
        }
        return latest;
    }
    @Override
    public void onPostExecute(String latest) {
        super.onPostExecute(latest);

        Intent intent = new Intent();
        intent.setAction(mAction);
        intent.putExtra(LATEST, latest);
        mContext.sendBroadcast(intent);
    }
}
