package com.evolute.survey;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity implements LocationListener {
    Button getloc,getElev;
    Double lat,lan;
    String st="";
    LocationManager locationManager;
    TextView locationText;
    LocationListener locationListener;
    String u = "https://elevation-api.io/api/elevation?points=";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        locationText = findViewById(R.id.selected_location);
        getloc = findViewById(R.id.location_button);
        getElev = findViewById(R.id.from_map);
        if (ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION}, 101);

        }

        getloc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getLocation();
            }
        });


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
            //Toast.makeText(getApplicationContext(),u+lat+","+lan,Toast.LENGTH_SHORT).show();
           new ElevAsyncTask().execute(u+"("+lat+","+lan+")");
            //Toast.makeText(getApplicationContext(),"No internet",Toast.LENGTH_SHORT).show();
        }
        else if ( conMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.DISCONNECTED
                || conMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.DISCONNECTED) {

            Toast.makeText(this,"No Internet",Toast.LENGTH_LONG).show();
        }
    }
    public void getLocation(){
        try {
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 5, this);
        }
        catch(SecurityException e) {
            e.printStackTrace();
        }
    }
    class ElevAsyncTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {
            OkHttpClient client = new OkHttpClient().newBuilder()
                    .readTimeout(20, TimeUnit.SECONDS)
                    .writeTimeout(20, TimeUnit.SECONDS)
                    .connectTimeout(20, TimeUnit.SECONDS).build();
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
            if (s != null)
                locationText.setText(s);
            else
                Toast.makeText(getApplicationContext(),"Timed out low net speed",Toast.LENGTH_SHORT).show();

        }
    }
    @Override
    public void onLocationChanged(Location location) {
        locationText.setText("Latitude: " + location.getLatitude() + "\n Longitude: " + location.getLongitude());
        lat = location.getLatitude();
        lan = location.getLongitude();
        Toast.makeText(this,""+lan+"  "+lat,Toast.LENGTH_SHORT).show();

        try {
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            locationText.setText(locationText.getText() + "\n"+addresses.get(0).getAddressLine(0));
        }catch(Exception e)
        {

        }

    }
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onProviderEnabled(String provider) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onProviderDisabled(String provider) {
        // TODO Auto-generated method stub

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