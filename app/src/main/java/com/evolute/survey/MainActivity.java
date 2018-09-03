package com.evolute.survey;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    Button getElev;
    String u = "https://api.open-elevation.com/api/v1/lookup?locations=41.161758,-8.583933";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getElev = findViewById(R.id.location_button);
    }
//    public boolean hostAvailable(String host, int port) {
//        try (Socket socket = new Socket()) {
//            socket.connect(new InetSocketAddress(host, port), 2000);
//            return true;
//        } catch (IOException e) {
//            // Either we have a timeout or unreachable host or failed DNS lookup
//            System.out.println(e);
//            return false;
//        }
//    }

    public void setGetElev(View v){
        //boolean on =  hostAvailable("www.google.com",80);
        ConnectivityManager conMgr = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        if ( conMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED
                || conMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED ){
           new ElevAsyncTask().execute(u);
            //Toast.makeText(getApplicationContext(),"No internet",Toast.LENGTH_SHORT).show();
        }
        else if ( conMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.DISCONNECTED
                || conMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.DISCONNECTED) {

            Toast.makeText(this,"No Internet",Toast.LENGTH_LONG).show();
        }
    }
    class ElevAsyncTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {
            OkHttpClient client = new OkHttpClient().newBuilder()
                    .readTimeout(6, TimeUnit.SECONDS)
                    .writeTimeout(6, TimeUnit.SECONDS)
                    .connectTimeout(6, TimeUnit.SECONDS).build();
            Request request = new Request.Builder()
                    .url(strings[0])
                    .build();

            Response response = null;
            try {
                response = client.newCall(request).execute();
                return response.body().string();
            } catch (IOException e) {
                e.printStackTrace();
                Log.d("request", "timedout");
            }
            return null;
        }
        @Override
        protected void onPostExecute(String s){
            super.onPostExecute(s);
            Toast.makeText(getApplicationContext(),s,Toast.LENGTH_SHORT).show();
        }
    }
    public static String executeCommand(String command) {
        StringBuilder output = new StringBuilder();
        try {
            Process proc = Runtime.getRuntime().exec(new String[] { "sh", "-c", command });
            BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()));

            String line;
            while ((line = reader.readLine())!= null) {
                output.append(line + "\n");
            }
            proc.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return output.toString();
    }

}