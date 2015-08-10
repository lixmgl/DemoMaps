package com.example.fangyichen.demomaps;

import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MapsActivity extends FragmentActivity {

    private GoogleMap mMap;
    ProgressBar progressBar;
    EditText fromAddress;
    EditText toAddress;
    TextView errorDisplay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        setUpMapIfNeeded();
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        fromAddress = (EditText) findViewById(R.id.fromIP);
        toAddress = (EditText) findViewById(R.id.toIP);
        errorDisplay = (TextView) findViewById(R.id.errorMessage);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    public void onSearch(View view) {
        mMap.clear();
        errorDisplay.setText("Loading...");
        new RetrieveFeedTask().execute(fromAddress.getText().toString(), toAddress.getText().toString());
    }

    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    private void setUpMap() {
        mMap.setMyLocationEnabled(true);
    }

    /**
     * This class is used to run IP search on back thread
     */
    private class RetrieveFeedTask extends AsyncTask<String, LatLng, String> {

        private Exception exception;


        protected void onPreExecute() {
            progressBar.setVisibility(View.VISIBLE);

        }

        /**
         *
         * @param ipAddress two IPv4 address as a range
         * @return message which will be display on the screen in onPostExcute
         */
        protected String doInBackground(String... ipAddress) {

            if(!IpConverter.IsValidIpAddress(ipAddress[0]) || !IpConverter.IsValidIpAddress(ipAddress[1])) return "Invalid IP Address";
            long from = IpConverter.ipToLong(ipAddress[0]);
            long to = IpConverter.ipToLong(ipAddress[1]);
            if(from > to){
                long temp = to;
                to = from;
                from = temp;
            }

            BinaryCheck(from,to);
            return "Search Finished";
        }

        /**
         * assumption: if two IP address have same GeoLocation, all ip address between them have the same address
         * @param from  smaller IP address
         * @param to    larger IP address
         */
        private void BinaryCheck(long from, long to){
            LatLng start = checkIp(IpConverter.longToIp(from));
            LatLng end = checkIp(IpConverter.longToIp(to));
            if(start.equals(end)){
                publishProgress(start);
            } else{
                BinaryCheck(to/2+from/2+1,to);
                BinaryCheck(from,to/2+from/2);


            }
        }

        /**
         *
         * @param ip IPv4 address
         * @return its location info
         */
        private LatLng checkIp(String ip){
            ObjectMapper MAPPER = new ObjectMapper();
            try {
                URL url = new URL("http://api.ipinfodb.com/v3/" + IpInfoDbClient.MODE + "/?format=json&key=" + IpInfoDbClient.APIKEY + "&ip=" + ip);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                try {
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                    StringBuilder stringBuilder = new StringBuilder();
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        stringBuilder.append(line).append("\n");
                    }
                    bufferedReader.close();
                    IpInfoDbClient.IpCityResponse ipCityResponse = MAPPER.readValue(stringBuilder.toString(), IpInfoDbClient.IpCityResponse.class);
                    LatLng latLng = new LatLng(Double.parseDouble(ipCityResponse.getLatitude()), Double.parseDouble(ipCityResponse.getLongitude()));
                    return latLng;
                } finally {
                    urlConnection.disconnect();
                }
            } catch (IOException e) {
                Log.e("ERROR", e.getMessage(), e);
                return null;
            }

        }

        /**
         * make a marker on map
         * @param str location info
         */
        protected  void onProgressUpdate(LatLng ... str){
            mMap.addMarker(new MarkerOptions().position(str[0]).title(str[0].toString()));
        }

        /**
         * display message
         * @param response
         */
        protected void onPostExecute(String response) {
            if(response != null ) {
                errorDisplay.setText(response);
            }

            progressBar.setVisibility(View.GONE);
            Log.i("INFO", response.toString());

        }
    }
}
