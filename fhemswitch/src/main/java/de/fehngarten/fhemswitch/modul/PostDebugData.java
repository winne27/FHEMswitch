package de.fehngarten.fhemswitch.modul;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by Werner on 10.09.2017.
 */

public class PostDebugData {

    public PostDebugData(String data) {
        try {
            //Log.d
            // ("mixed data", data);
            URL url = new URL("https://wernerschaeffer.de/fhemswitch/debug.php");
            //Open the connection here, and remember to close it when job its done.
            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
            conn.setReadTimeout(10000);
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            conn.setConnectTimeout(15000);
            conn.setRequestMethod("POST");
            conn.setDoInput(true);
            conn.setDoOutput(true);
            OutputStream os = conn.getOutputStream();
            os.write(data.getBytes("UTF-8"));
            os.flush();
            //Log.d("mixed data", "debug finished");

            if (conn.getResponseCode() != 200) {
                throw new RuntimeException("Failed : HTTP error code : " + conn.getResponseCode());
            }
            BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));

            String output;
            //Log.d("mixed data response", "Output from Server ....");
            while ((output = br.readLine()) != null) {
                //Log.d("mixed data response", output);
            }

            conn.disconnect();


        } catch (MalformedURLException e) {
            Log.d("mixed data", "MalformedURLException");
            e.printStackTrace();

        } catch (IOException e) {
            Log.d("mixed data", "IOException");
            e.printStackTrace();
        }
    }
}
