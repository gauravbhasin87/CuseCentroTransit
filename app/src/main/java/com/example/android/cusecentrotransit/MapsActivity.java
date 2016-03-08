package com.example.android.cusecentrotransit;

import android.app.ProgressDialog;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class MapsActivity extends FragmentActivity {

    private ArrayList<Pattern> patterns;
    private ArrayList<LatLng> ltlnListW;
    private ArrayList<LatLng> ltlnListS;
    private GoogleMap mMap; // Might be null if Google Play services APK is not available.

    private Polyline polyLine;
    private PolylineOptions rectOptions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        ltlnListW = new ArrayList<LatLng>();
        ltlnListS = new ArrayList<LatLng>();


        Intent intent = getIntent();

        String routeNo = intent.getStringExtra("routeId");
        FetchPattern fetchPattern = new FetchPattern();
        fetchPattern.execute(routeNo);


    }

    @Override
    protected void onResume() {
        super.onResume();
        //setUpMapIfNeeded();
        //call fetch from here also
    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p/>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p/>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
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

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {
        //mMap.addMarker(new MarkerOptions().position(new LatLng(0, 0)).title("Marker"));
        mMap.setMyLocationEnabled(true);//enable mylocation layer- draw indication of current location
        LocationManager locationManager=(LocationManager)getSystemService(LOCATION_SERVICE);//to retrive
        Criteria criteria=new Criteria();//indicates the criteria of location provide- power , accuracy
        String provider=locationManager.getBestProvider(criteria,true);
        Location myLocation=locationManager.getLastKnownLocation(provider);
        mMap.setMapType(mMap.MAP_TYPE_HYBRID);
        double lat=myLocation.getLatitude();
        double lng=myLocation.getLongitude();
        LatLng latlng=new LatLng(lat,lng);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(ltlnListW.get(0)));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
        mMap.addMarker(new MarkerOptions().position(new LatLng(lat, lng)).title("you r here"));
        rectOptions = new PolylineOptions();

        for(LatLng ltln: ltlnListW){
            rectOptions.add(ltln);
        }
        polyLine = mMap.addPolyline(rectOptions);
        //mMap.addMarker(new MarkerOptions().position(new LatLng(43.037273814705, -76.13142907619499)).title("my stop"));
        //rectOptions.add(latlng);
        //rectOptions.add(new LatLng(43.037273814705, -76.13142907619499));
        //polyLine = mMap.addPolyline(rectOptions);
        for (LatLng ltln : ltlnListS) {
            mMap.addMarker(new MarkerOptions().position(ltln));
        }
    }

    public class FetchPattern extends AsyncTask<String, Void, ArrayList<Pattern>> {

        public final String LOG_TAG = FetchPattern.class.getName();
        //ProgressDialog pd = new ProgressDialog(getParent());
        /*@Override
        protected void onPreExecute ( )
        {
            //starting the progress dialogue
            pd.show();
        }*/


        @Override
        public ArrayList<Pattern> doInBackground(String... params) {

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            String patternsXML;

            try {
                final String DIRECTION_BASE_URL =
                        "http://bus-time.centro.org/bustime/api/v1/getpatterns?key=gbiC5GSAESipXuzsF6hemn6Hq";
                Uri builtUri = Uri.parse(DIRECTION_BASE_URL).buildUpon()
                        .appendQueryParameter("rt", params[0]).build();

                URL url = new URL(builtUri.toString());
                Log.v(LOG_TAG, "Built URI " + builtUri.toString());
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();
                Log.v(LOG_TAG, " urlConnection.connect()");
                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                Log.v(LOG_TAG, "inputstream is not null");
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                patternsXML = buffer.toString();
                Log.v(LOG_TAG, patternsXML);
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error in URL connection", e);
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }

            try {
                return getPatternsFromXML(patternsXML);
            } catch (Exception e) {
                Log.e(LOG_TAG, "Error in getRoutesFromXML ", e);

            }
            return null;
        }

        public ArrayList<Pattern> getPatternsFromXML(String xml) {
            XmlPullParserFactory pullParserFactory;
            try {
                pullParserFactory = XmlPullParserFactory.newInstance();
                XmlPullParser parser = pullParserFactory.newPullParser();

                // InputStream in_s = getApplicationContext().getAssets().open("temp.xml");

                parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
                parser.setInput(new StringReader(xml));

                return parseXMLforPatterns(parser);

            } catch (XmlPullParserException e) {
                Log.e(LOG_TAG, "Error", e);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                Log.e(LOG_TAG, "Error", e);
            }
            return null;

        }

        private ArrayList<Pattern> parseXMLforPatterns(XmlPullParser parser) throws XmlPullParserException, IOException {
            patterns = null;
            int eventType = parser.getEventType();
            Pattern currentPattern = null;
            Pt point = null;
            while (eventType != XmlPullParser.END_DOCUMENT) {
                String name;
                switch (eventType) {
                    case XmlPullParser.START_DOCUMENT:
                        patterns = new ArrayList<Pattern>();

                        break;
                    case XmlPullParser.START_TAG:
                        name = parser.getName();
                        if (name.equalsIgnoreCase("ptr")) {
                            currentPattern = new Pattern();

                        } else if (currentPattern != null) {
                            if (name.equalsIgnoreCase("pid"))
                                currentPattern.setPid(parser.nextText());
                            if (name.equalsIgnoreCase("ln"))
                                currentPattern.setRtdir(parser.nextText());
                            if (name.equalsIgnoreCase("pt")) {
                                point = new Pt();
                                point.stpid = "";
                                point.stpnm = "";
                                point.pdist = "";

                            }
                            if (name.equalsIgnoreCase("seq") && point != null)
                                point.seq = parser.nextText();
                            if (name.equalsIgnoreCase("lat") && point != null)
                                point.lat = parser.nextText();
                            if (name.equalsIgnoreCase("lon") && point != null)
                                point.lon = parser.nextText();
                            if (name.equalsIgnoreCase("typ") && point != null)
                                point.typ = parser.nextText();
                            if (name.equalsIgnoreCase("stpid") && point != null)
                                point.stpid = parser.nextText();
                            if (name.equalsIgnoreCase("stpnm") && point != null)
                                point.stpnm = parser.nextText();
                            if (name.equalsIgnoreCase("pdist"))
                                point.pdist = parser.nextText();

                        }
                        break;
                    case XmlPullParser.END_TAG:
                        name = parser.getName();
                        if (name.equalsIgnoreCase("pt") && point != null)
                            currentPattern.getPts().add(point);
                        else if (name.equalsIgnoreCase("ptr") && currentPattern != null) {
                           // System.out.println("HElloo !!!!" + currentPattern.getPts().size());
                            patterns.add(currentPattern);
                        }


                }
                eventType = parser.next();
            }
            patterns = patterns;
            return patterns;
        }

        protected void onPostExecute(ArrayList<Pattern> result) {
            if (result != null) {
                if (result.size() != 0) {
                    for (Pattern pattern : result) {

                        for (Pt point : pattern.getPts()) {
                            if (point.typ.equalsIgnoreCase("S")) {
                                LatLng ltln = new LatLng(Double.parseDouble(point.lat), Double.parseDouble(point.lon));
                                ltlnListS.add(ltln);
                            }

                            LatLng ltln = new LatLng(Double.parseDouble(point.lat), Double.parseDouble(point.lon));
                            ltlnListW.add(ltln);
                        }
                    }
                    //int i=10;
                    setUpMapIfNeeded();
                }
                else{
                    Toast.makeText(getBaseContext(), "NO ROUTE AVAILABLE TO SHOW AT THIS TIME!!!",
                            Toast.LENGTH_LONG).show();
                }
             }
        }
    }
}
