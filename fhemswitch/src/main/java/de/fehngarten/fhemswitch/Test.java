package de.fehngarten.fhemswitch;

import android.os.Bundle;
import android.app.Activity;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

public class Test extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test);
        Log.i("Testcommand", "eins");

        try {
            //URL url = new URL("http://raspi.fritz.box:8081/fhem?XHR=1&fwcsrf=csrf_233137639685367&cmd=" + URLEncoder.encode("set Ventilator on", "UTF-8"));
            URL url = new URL("http://raspi.fritz.box:8081/fhem?XHR=1&fwcsrf=csrf_233137639685367&cmd=" + URLEncoder.encode("list", "UTF-8"));
            HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
            Log.i("Testcommand", "zwei");
            InputStream inputStream = httpConn.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String line = bufferedReader.readLine();
            Log.i("Testcommand", line);
            httpConn.disconnect();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
