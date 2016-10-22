package de.fehngarten.fhemswitch;

import java.util.ArrayList;

import android.util.Log;

//import io.socket.emitter.Emitter;
import io.socket.client.IO;
import io.socket.client.Socket;

import android.content.Context;

class MySocket {
    Socket socket = null;
    public String url;

    MySocket(String url, Context context) {
        //Log.d("MySocket", "started");

        try {
            socket = null;
            IO.Options options = new IO.Options();
            options.reconnection = false;
            options.timeout = context.getResources().getInteger(R.integer.socketsConnectionTimeout);
            socket = IO.socket(url, options);
            socket.connect();
            this.url = url;
        } catch (Exception e1) {
            Log.e("socket error", e1.toString());
        }

        socket.on(Socket.EVENT_ERROR, args -> Log.d("socket.io", "lost connection to server"));

        socket.on(Socket.EVENT_CONNECT_ERROR, args -> {
            Log.d("connection error", args[0].toString());
            socket.close();
        });

        socket.on(Socket.EVENT_CONNECT, args -> Log.d("connection established", ""));
    }

    void requestValues(ArrayList<String> unitsList, String type) {
        for (String unit : unitsList) {
            if (type.equals("once")) {
                socket.emit("getValueOnce", unit);
            } else {
                socket.emit("getValueOnChange", unit);
            }
        }
    }

    void sendCommand(String cmd) {
        //Log.d("mySocket command",cmd);
        if (socket != null && cmd != null) {
            socket.emit("commandNoResp", cmd);
        }
    }
}
