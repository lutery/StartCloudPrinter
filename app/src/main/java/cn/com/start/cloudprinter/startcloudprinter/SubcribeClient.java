package cn.com.start.cloudprinter.startcloudprinter;

import android.util.Log;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

public class SubcribeClient extends WebSocketClient {

    private final static String TAG = SubcribeClient.class.getSimpleName();

//    public static void main(String[] argc) throws URISyntaxException {
//        SubcribeClient subcribeClient = new SubcribeClient(new URI("ws://10.0.0.8:8080/printer/subscribe"));
//        subcribeClient.connect();
//    }


    public SubcribeClient(URI serverUri) {
        super(serverUri);
    }

    public SubcribeClient(URI serverUri, Map<String, String> headers){
        super(serverUri, headers);
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        Log.d(TAG, "connect server success");
    }

    @Override
    public void onMessage(String message) {
        Log.d(TAG, "***************recevice server message***************");
        Log.d(TAG, message);
        Log.d(TAG, "+++++++++++++++recevice server message+++++++++++++++");
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        Log.d(TAG, "close connect");
        Log.d(TAG, "code is " + code + " reason is " + reason);
    }

    @Override
    public void onError(Exception ex) {
        ex.printStackTrace();
    }
}
