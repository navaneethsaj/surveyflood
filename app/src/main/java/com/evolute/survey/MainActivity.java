package com.evolute.survey;

import android.content.Context;
import android.content.DialogInterface;
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
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity implements LocationListener {
    Button getloc,nextbutton;
    Double lat,lan;
    String st="";
    LinearLayout locationlayout,altitudelayout;
    AlertDialog alertDialog, reviewDialog;
    AlertDialog.Builder builder, reviewBuilder;
    LocationManager locationManager;
    TextView locationText, altitudetext;
    EditText nameED,emailED,heightED,pincodeED;
    LocationListener locationListener;
    String url = "https://elevation-api.io/api/elevation?points=";
    String urltak = "http://takshak.in/survey/?";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        locationText = findViewById(R.id.selected_location);
        getloc = findViewById(R.id.location_button);
        altitudetext = findViewById(R.id.altitudetextview);
        locationlayout = findViewById(R.id.locationlayout);
        altitudelayout = findViewById(R.id.altitudelayout);
        nextbutton = findViewById(R.id.nextbutton);
        nameED = findViewById(R.id.name);
        heightED = findViewById(R.id.height);
        emailED = findViewById(R.id.email);
        pincodeED = findViewById(R.id.pincode);
        reviewBuilder = new AlertDialog.Builder(this);
        reviewBuilder.setTitle("Confirm the Information").setCancelable(false)
        .setPositiveButton("Send", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                sendToTakshak();
            }
        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        reviewDialog = reviewBuilder.create();
        builder = new AlertDialog.Builder(this);
        builder.setTitle("Fetching location").setMessage("This may take some time").setCancelable(false);
        alertDialog = builder.create();
        if (ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION}, 101);

        }

        getloc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!alertDialog.isShowing()){
                    alertDialog.show();
                }
                getLocation();
            }
        });

        nextbutton.setEnabled(false);
        nextbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (emailED.getText().toString().length() < 5){
                    Toast.makeText(getApplicationContext(),"Enter Details",Toast.LENGTH_SHORT).show();
                    return;
                }
                else if (nameED.getText().toString().length() < 3){
                    Toast.makeText(getApplicationContext(),"Enter Details",Toast.LENGTH_SHORT).show();
                    return;
                }
                else if (heightED.getText().toString().length() == 0){
                    Toast.makeText(getApplicationContext(),"Enter Details",Toast.LENGTH_SHORT).show();
                    return;
                }
                else if (pincodeED.getText().toString().length() < 6){
                    Toast.makeText(getApplicationContext(),"Enter Details",Toast.LENGTH_SHORT).show();
                    return;
                }
                reviewBuilder.setMessage("Email : " + emailED.getText().toString()
                        +"\nName : "+nameED.getText().toString()
                        +"\nHeight Affected : "+heightED.getText().toString()
                        +"\nPincode : "+pincodeED.getText().toString()
                        +"\nAddress : "+locationText.getText().toString());
                reviewDialog = reviewBuilder.create();
                if (!reviewDialog.isShowing()){
                    reviewDialog.show();
                }

            }
        });
    }

    private void sendToTakshak() {

        String email = emailED.getText().toString();
        email = email.replace("@","%40");
        String name = nameED.getText().toString();
        String height = heightED.getText().toString();
        String latitude = String.valueOf(lat);
        String longitude = String.valueOf(lan);
       // email=djkd@gmailc.clm&name=alf&place=lsfj&pincode=aljf&longitude=34&latitude=25&elevation=35&area=345&area=524&hight_effected=23";
        String URL = urltak + "email="+email+
                "&name="+name+
                "&place="+locationText.getText().toString()+
                "&pincode="+"000"+
                "&longitude="+longitude+
                "&latitude="+latitude+
                "&elevation="+altitudetext.getText().toString()+
                "&area="+"54"+
                "&hight_effected="+height;
        new UploadAsyncTask().execute(URL);
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



    public void setGetElev(){
        //boolean on =  hostAvailable("www.google.com",80);
        ConnectivityManager conMgr = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        if ( conMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED
                || conMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED ){
            //Toast.makeText(getApplicationContext(),url+lat+","+lan,Toast.LENGTH_SHORT).show();
           new ElevAsyncTask().execute(url +"("+lat+","+lan+")");
            //Toast.makeText(getApplicationContext(),"No internet",Toast.LENGTH_SHORT).show();
        }
        else if ( conMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.DISCONNECTED
                || conMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.DISCONNECTED) {

            Toast.makeText(this,"No Internet",Toast.LENGTH_LONG).show();
        }
    }
    public void getLocation(){
        try {
            /*builder = new AlertDialog.Builder(getApplicationContext());
            builder.setTitle("Updating");
            builder.setMessage("Getting new Location ..");
            alertDialog = builder.create();
            if(!alertDialog.isShowing()){
                alertDialog.show();
            }*/
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
            if (alertDialog.isShowing()){
                alertDialog.dismiss();
            }
            if (s != null) {
                try {
                    JSONObject jsonObject = new JSONObject(s);
                    JSONArray jsonArray = jsonObject.getJSONArray("elevations");
                    JSONObject result = jsonArray.getJSONObject(0);
                    String altitude = String.valueOf(result.getDouble("elevation"));
                    altitudetext.setText(altitude);
                    altitudelayout.setVisibility(View.VISIBLE);
                    nextbutton.setEnabled(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            else
                Toast.makeText(getApplicationContext(),"Timed out low net speed",Toast.LENGTH_SHORT).show();

        }
    }
    @Override
    public void onLocationChanged(Location location) {
        /*if(alertDialog.isShowing())
            alertDialog.dismiss();*/
        locationText.setText("Latitude: " + location.getLatitude() + "\n Longitude: " + location.getLongitude());
        lat = location.getLatitude();
        lan = location.getLongitude();
        //Toast.makeText(this,""+lan+"  "+lat,Toast.LENGTH_SHORT).show();

        try {
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            pincodeED.setText(addresses.get(0).getPostalCode());
            locationlayout.setVisibility(View.VISIBLE);
            locationText.setText(addresses.get(0).getAddressLine(0));
            setGetElev();
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

    class UploadAsyncTask extends AsyncTask<String, Void, String> {

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
            if (s!=null){
                try {
                    JSONObject jsonObject = new JSONObject(s);
                    String code = jsonObject.getString("status");
                    if (code.equals("400")){
                        Toast.makeText(getApplicationContext(),"Success",Toast.LENGTH_SHORT).show();
                    }else {
                        Toast.makeText(getApplicationContext(),"Failed",Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }
    }
}