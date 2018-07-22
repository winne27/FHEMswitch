package de.fehngarten.fhemswitch.modul;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

//import org.jsoup.Jsoup;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import static de.fehngarten.fhemswitch.global.Settings.*;

public class GetStoreVersion extends AsyncTask<String, Void, String> {
    private final String TAG;
    private Context mContext;
    private String mAction;
    public static final String LATEST = "LATEST";

    public GetStoreVersion(Context context, String action) {
        TAG = getClass().getName();
        //Log.d(TAG, "started");
        mContext = context;
        mAction = action;
    }

    @Override
    protected String doInBackground(String... params) {
        String latest = "";
        try {
            Document doc = Jsoup.connect(settingGoogleStoreUrl).get();
            Elements zwei = doc.selectFirst("div.xyOfqd").children();
            latest = zwei.get(3).selectFirst("span").text();
        } catch (Exception e) {
            Log.d(TAG, "read app version failed: " + e);
        }
        return latest;
    }

    @Override
    public void onPostExecute(String latest) {
        //super.onPostExecute(latest);
        Intent intent = new Intent();
        intent.setAction(mAction);
        intent.putExtra(LATEST, latest);
        mContext.sendBroadcast(intent);
    }
}
