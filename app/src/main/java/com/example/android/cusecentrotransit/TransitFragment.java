package com.example.android.cusecentrotransit;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.support.v4.app.Fragment;
//import android.app.Fragment;
import android.os.AsyncTask;
import android.os.Bundle;
//import android.support.v7.internal.widget.AdapterViewCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;

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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Created by Gaurav on 4/19/2015.
 */
public class TransitFragment extends Fragment {

    //Adapters for each spinner
    private ArrayAdapter<String> routeAdapter;
    private ArrayAdapter<String> dirAdapter;
    private ArrayAdapter<String> stopAdapter;

    public TransitFragment() {
    }

    void showToast(CharSequence msg) {
        Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();
    }
    View rootView;


    FetchRoutes fetchRoutes;

    //display text for Route spinner
    private String[] routesInfo;
    //list of Route class
    private ArrayList<Route> routes;
    //list of Stop class
    private ArrayList<Stop> stops;
    //displat text for Stop spinner
    private String[] stopInfo;
    //list of Prediction class
    private ArrayList<Prediction> predictions;
    //list of Pattern class
    private ArrayList<Pattern> patterns;
    //selected route in Route spinner
    private Route selectedRoute;

    ListView predlist;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final String LOG_TAG = TransitFragment.class.getName();
        rootView = inflater.inflate(R.layout.fragment_main, container, false);

        Button btn_navigate = (Button)rootView.findViewById(R.id.btn_navigt);
        final Button btn_route = (Button)rootView.findViewById(R.id.btn_route);

        //click listener for Button "See Route"
        btn_route.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //launch MapsActivity and pass Route Number to plot the given Route
                Intent intent = new Intent(getActivity(), MapsActivity.class);
                intent.putExtra("routeId",selectedRoute.getRouteNo());
                startActivity(intent);

            }
        });
        btn_route.setVisibility(View.INVISIBLE);
        btn_navigate.setVisibility(View.INVISIBLE);
        Spinner routeSpinner = (Spinner) rootView.findViewById(R.id.routeSpinner);
       // Spinner dirSpinner = (Spinner)rootView.findViewById(R.id.dirSpinner);

        String[] testRoute = {};
        List<String> listRoutes = new ArrayList<String>(Arrays.asList(testRoute));

        fetchRoutes = new FetchRoutes();
        routeAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, listRoutes);
        routeAdapter = routeAdapter;
        routeSpinner.setAdapter(routeAdapter);
        routeSpinner.setSelection(0);

        //execute the AsyncTask for fetching the list of available routes from Centro API
        fetchRoutes.execute();
      //  routes = fetchRoutes.routes;

        List<String> listDirections = new ArrayList<String>();
        dirAdapter = new ArrayAdapter<String>(getActivity(),android.R.layout.simple_spinner_item,listDirections);

        final Spinner dirSpinner = (Spinner)rootView.findViewById(R.id.dirSpinner);
        dirSpinner.setAdapter(dirAdapter);

        List<String> listStops = new ArrayList<String>();
        stopAdapter = new ArrayAdapter<String>(getActivity(),android.R.layout.simple_spinner_item,listStops);
        final Spinner stopSpinner = (Spinner)rootView.findViewById(R.id.stopSpinner);
        stopSpinner.setAdapter(stopAdapter);

        predlist = (ListView)rootView.findViewById(R.id.predlist);



     //item selection listener for Route Spinner
        routeSpinner.setOnItemSelectedListener(
                new OnItemSelectedListener() {
                    @Override
                    public void onItemSelected( AdapterView<?> parent, View view, int position, long id) {
                        //Log.v(LOG_TAG,"1.INSIDE OnItemSelectedListener ");
                        //showToast("Spinner1: position=" + position + " id=" + id);
                            if(position != 0) {

                                dirAdapter.clear();
                                stopAdapter.clear();
                                predlist.setAdapter(null);
                                Button btn_navigate = (Button)rootView.findViewById(R.id.btn_navigt);
                                btn_navigate.setVisibility(View.INVISIBLE);
                                btn_route.setVisibility(View.VISIBLE);

                                Log.v(LOG_TAG,"INSIDE OnItemSelectedListener ");

                                selectedRoute = routes.get(position-1);
                                //execute AsyncTask for fetching directions for the given Route
                                FetchDir fetchDir = new FetchDir();
                                fetchDir.execute((String) selectedRoute.getRouteNo());
                            }
                    }
                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                        showToast("Spinner1: unselected");
                    }
                });

        //item selection listener for Direction Spinner
        dirSpinner.setOnItemSelectedListener(
                new OnItemSelectedListener(){
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                        //showToast("Spinner1: position=" + position + " id=" + id);
                        if(position != 0) {
                            //showToast("Spinner1: position=" + position + " id=" + id);
                            //List<String> listDirections = new ArrayList<String>();
                            //clear stop's spinner and prediction's listview
                            stopAdapter.clear();
                            predlist.setAdapter(null);
                            Log.v(LOG_TAG, "INSIDE dirSpinner OnItemSelectedListener ");

                            Button btn_navigate = (Button)rootView.findViewById(R.id.btn_navigt);
                            btn_navigate.setVisibility(View.INVISIBLE);
                            //execute AsyncTask for fetching all the stops for selected route and direction
                            FetchStops fetchStop = new FetchStops();
                            fetchStop.execute((String) selectedRoute.getRouteNo(),parent.getItemAtPosition(position).toString());
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });

                //item selection listener for Stop's spinner
                stopSpinner.setOnItemSelectedListener(new OnItemSelectedListener( ) {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        //showToast("Spinner1: position=" + position + " id=" + id);

                        if(position !=0){
                            Log.v(LOG_TAG,"inside stopspoinner onItemSelected");
                            //clear
                            predlist.setAdapter(null);
                            final Stop selectedStop = stops.get(position-1);

                            //execute AsyncTask for fetching the arrival time predictions for the selected stop
                            //this will predict all the buses for the given stop irrespective of route coz we are just passing Stop id
                            FetchPredictions fetchPredictions = new FetchPredictions();
                            fetchPredictions.execute(selectedStop.getStpid());

                            Button btn_navigate = (Button)rootView.findViewById(R.id.btn_navigt);
                            btn_navigate.setVisibility(View.VISIBLE);

                            //navigate to bus stop button listener
                            btn_navigate.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {

                                    //fetch current location of the device
                                    LocationManager locationManager=(LocationManager)getActivity().getSystemService(Context.LOCATION_SERVICE);
                                    //indicates the criteria of location provide- power , accuracy
                                    Criteria criteria=new Criteria();
                                    String provider=locationManager.getBestProvider(criteria,true);
                                    Location location=locationManager.getLastKnownLocation(provider);
                                    Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
                                            Uri.parse("http://maps.google.com/maps?saddr=" + location.getLatitude() + "," + location.getLongitude() + "&daddr=" + selectedStop.getLat() + "," + selectedStop.getLon()));
                                    intent.setPackage("com.google.android.apps.maps");
                                    startActivity(intent);

                                }
                            });
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });

        return rootView;
    }

    //AsyncTask to fetch route's infomation
    public class FetchRoutes extends AsyncTask<Void, Void, String[]> {

        private final String LOG_TAG = FetchRoutes.class.getName();
        ProgressDialog pd = new ProgressDialog(getActivity());

        @Override
        protected void onPreExecute ( )
        {
            //starting the progress dialogue
            pd.show();
        }


        @Override
        public String[] doInBackground(Void... params) {

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            String routeXML;

            try {
                //create Centro API url to fetch routes
                URL url = new URL("http://bus-time.centro.org/bustime/api/v1/getroutes?key=gbiC5GSAESipXuzsF6hemn6Hq");
                urlConnection = (HttpURLConnection) url.openConnection();
                Log.v(LOG_TAG," urlConnection.openConnection()");
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();
                Log.v(LOG_TAG," urlConnection.connect()");
                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                Log.v(LOG_TAG,"inputstream is not null");
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                routeXML = buffer.toString();
                Log.v(LOG_TAG, routeXML);
            } catch (IOException e) {
                pd.dismiss();
                Log.e(LOG_TAG, "Error in URL connection", e);

                Toast.makeText(getActivity(), "NO INTERNET CONNECTION OR THE SERVER IS DOWN!!!",
                        Toast.LENGTH_LONG).show();
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
                return getRoutesFromXML(routeXML);
            } catch (Exception e) {
                Log.e(LOG_TAG, "Error in getRoutesFromXML ", e);

            }
            return null;
        }
        //Parse response XML from Centro Server
        private String[] getRoutesFromXML(String xml) {

            XmlPullParserFactory pullParserFactory;
            try {
                pullParserFactory = XmlPullParserFactory.newInstance();
               // pullParserFactory.setNamespaceAware(true);
                XmlPullParser parser = pullParserFactory.newPullParser();
                parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
                parser.setInput(new StringReader(xml));
                return parseXMLforRoutes(parser);
            } catch (XmlPullParserException e) {
                Log.e(LOG_TAG, "Error", e);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                Log.e(LOG_TAG, "Error", e);
            }
            return null;
        }

        private String[] parseXMLforRoutes(XmlPullParser parser) throws XmlPullParserException, IOException {
            routes = null;
            int eventType = parser.getEventType();
            Route currentRoute = null;

            while (eventType != XmlPullParser.END_DOCUMENT) {
                String name = null;
                switch (eventType) {
                    case XmlPullParser.START_DOCUMENT:
                        routes = new ArrayList();

                        break;
                    case XmlPullParser.START_TAG:
                        name = parser.getName();
                        if (name.equalsIgnoreCase("route")) {
                            currentRoute = new Route();
                        } else if (currentRoute != null) {
                            if (name.equalsIgnoreCase("rt")) {
                                currentRoute.setRouteNo(parser.nextText());
                            } else if (name.equalsIgnoreCase("rtnm")) {
                                currentRoute.setRouteName(parser.nextText());
                            } else if (name.equalsIgnoreCase("rtclr")) {
                                currentRoute.setRouteColor(parser.nextText());
                            }
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        name = parser.getName();
                        if (name.equalsIgnoreCase("route") && currentRoute != null) {
                            routes.add(currentRoute);
                        }
                }
                eventType = parser.next();
            }
            routesInfo = new String[routes.size()+1];
            int i=1;
            routesInfo[0]="";
            for (Route r : routes) {
                String route = r.getRouteNo() + "-" + r.getRouteName();
                routesInfo[i] = route;
                i++;
                Log.v(LOG_TAG, route);
            }
            return routesInfo;

        }

        @Override
        protected void onPostExecute(String[] result) {
            if (result != null) {
                routeAdapter.clear();
                for (String str : result) {
                    Log.v(LOG_TAG, str);
                    routeAdapter.add(str);
                }
                pd.dismiss();
                // New data is back from the server.  Hooray!
            }
        }
    }

    //AsyncTask to fetch directions for the selected Route
    public class FetchDir extends AsyncTask<String,Void,String[]>{
        private final String LOG_TAG = FetchDir.class.getName();
        //Spinner routeSpinner = (Spinner) rootView.findViewById(R.id.routeSpinner);
        ProgressDialog pd = new ProgressDialog(getActivity());
        @Override
        protected void onPreExecute ( )
        {
            //starting the progress dialogue
            pd.show();
        }

        @Override
        public String[] doInBackground(String... params) {

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            String dirXML;

            try {
                final String DIRECTION_BASE_URL =
                        "http://bus-time.centro.org/bustime/api/v1/getdirections?key=gbiC5GSAESipXuzsF6hemn6Hq";
                Uri builtUri = Uri.parse(DIRECTION_BASE_URL).buildUpon()
                        .appendQueryParameter("rt", params[0]).build();

                URL url = new URL(builtUri.toString());
                Log.v(LOG_TAG, "Built URI " + builtUri.toString());

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                dirXML = buffer.toString();
                Log.v(LOG_TAG, dirXML);
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
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
                return getDirFromXML(dirXML);
            } catch (Exception e) {
                Log.e(LOG_TAG, "Error", e);

            }


            return null;
        }

        private String[] getDirFromXML(String xml) {

            XmlPullParserFactory pullParserFactory;
            try {
                pullParserFactory = XmlPullParserFactory.newInstance();
                XmlPullParser parser = pullParserFactory.newPullParser();

                // InputStream in_s = getApplicationContext().getAssets().open("temp.xml");

                parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
                parser.setInput(new StringReader(xml));

                return parseXMLforDir(parser);

            } catch (XmlPullParserException e) {

                Log.e(LOG_TAG, "Error", e);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                Log.e(LOG_TAG, "Error", e);
            }
            return null;

        }

        private String[] parseXMLforDir(XmlPullParser parser) throws XmlPullParserException, IOException {
            Directions dirs = null;
            int eventType = parser.getEventType();

            while (eventType != XmlPullParser.END_DOCUMENT) {
                String name = null;
                switch (eventType) {
                    case XmlPullParser.START_DOCUMENT:
                        dirs = new Directions();
                        break;
                    case XmlPullParser.START_TAG:
                        name = parser.getName();
                        if (name.equalsIgnoreCase("dir")){
                            String direction = parser.nextText();
                            if(direction.contains("FROM"))
                                  dirs.setFromDir(direction);
                            else if(direction.contains("TO"))
                                    dirs.setToDir(direction);

                        }
                        /*if (name.equalsIgnoreCase("dir") && direction.contains("FROM"))
                            dirs.fromDir = direction;
                        else if(name.equalsIgnoreCase("dir") && direction.contains("TO") )
                            dirs.toDir = direction;
                        if (parser.getEventType() != XmlPullParser.END_TAG)
                        parser.nextTag();*/
                        break;
                    case XmlPullParser.END_TAG:
                        break;
                        }
                eventType = parser.next();
                }

                String[] directions = new String[3];
                directions[0] = "";
                directions[1] = dirs.getFromDir();
                directions[2] = dirs.getToDir();
                Log.v(LOG_TAG,directions[1]);
                Log.v(LOG_TAG,directions[2]);
                return directions;
            }

        //display stops after processing the response XML from the server
        @Override
        protected void onPostExecute(String[] result) {
            if (result != null) {
                //Spinner dirSpinner = (Spinner)rootView.findViewById(R.id.dirSpinner);
                //dirSpinner.setAdapter(dirAdapter);
                dirAdapter.clear();
                for (String str : result) {
                    Log.v(LOG_TAG, str);
                    dirAdapter.add(str);
                }
                pd.dismiss();
                // New data is back from the server.  Hooray!
            }
        }
    }

    //AsysnTask to fetch stops for selected route and selected direction
    public class FetchStops extends AsyncTask<String,Void,String[]>{

        private final String LOG_TAG = FetchStops.class.getName();
        ProgressDialog pd = new ProgressDialog(getActivity());

        @Override
        public String[] doInBackground(String... params){
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            String stopXml;

            try {

                final String DIRECTION_BASE_URL =
                        "http://bus-time.centro.org/bustime/api/v1/getstops?key=gbiC5GSAESipXuzsF6hemn6Hq";
                Uri builtUri = Uri.parse(DIRECTION_BASE_URL).buildUpon()
                        .appendQueryParameter("rt", params[0])
                        .appendQueryParameter("dir",params[1]).build();

                URL url = new URL(builtUri.toString());
                Log.v(LOG_TAG, "Built URI " + builtUri.toString());
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                stopXml = buffer.toString();
                Log.v(LOG_TAG,stopXml);

            }
            catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                return null;
            }

            finally {
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
                return getStopsFromXML(stopXml);
            } catch (Exception e) {
                Log.e(LOG_TAG, "Error", e);

            }


            return null;
        }

        private String[] getStopsFromXML(String xml){
            XmlPullParserFactory pullParserFactory;
            try {
                pullParserFactory = XmlPullParserFactory.newInstance();
                XmlPullParser parser = pullParserFactory.newPullParser();

                // InputStream in_s = getApplicationContext().getAssets().open("temp.xml");

                parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
                parser.setInput(new StringReader(xml));

                return parseXMLforStops(parser);

            } catch (XmlPullParserException e) {

                Log.e(LOG_TAG, "Error", e);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                Log.e(LOG_TAG, "Error", e);
            }
            return null;

        }

        private String[] parseXMLforStops(XmlPullParser parser) throws XmlPullParserException, IOException{

            stops = null;
            int eventType = parser.getEventType();
            Stop currentStop = null;

            while (eventType != XmlPullParser.END_DOCUMENT) {
                String name = null;
                switch (eventType) {
                    case XmlPullParser.START_DOCUMENT:
                        stops = new ArrayList();
                        break;
                    case XmlPullParser.START_TAG:
                        name = parser.getName();
                        if (name.equalsIgnoreCase("stop")) {
                            currentStop = new Stop();
                        } else if (currentStop != null) {
                            if (name.equalsIgnoreCase("stpid")) {
                                currentStop.setStpid(parser.nextText());
                            } else if (name.equalsIgnoreCase("stpnm")) {
                                currentStop.setStpnm(parser.nextText());
                            } else if (name.equalsIgnoreCase("lat")) {
                                currentStop.setLat(parser.nextText());
                            }else if (name.equalsIgnoreCase("lon")) {
                                currentStop.setLon(parser.nextText());
                            }
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        name = parser.getName();
                        if (name.equalsIgnoreCase("stop") && currentStop != null) {
                            stops.add(currentStop);
                        }
                }
                eventType = parser.next();
            }

            stopInfo = new String[stops.size()+1];
            stopInfo[0] = "";
            int i=1;
            for(Stop stp : stops){
                stopInfo[i] = stp.getStpnm();
                i++;
            }
            return stopInfo;

        }


        @Override
        protected void onPostExecute(String[] result) {
            if (result != null) {

                stopAdapter.clear();
                for (String str : result) {
                    Log.v(LOG_TAG, str);
                    stopAdapter.add(str);
                }
                pd.dismiss();
                // New data is back from the server.  Hooray!
            }

        }


    }

    //AsysnTask to fetch bus arrival predictions at a given stop
    public class FetchPredictions extends AsyncTask<String,Void,ArrayList<Prediction>>{

        private final String LOG_TAG = FetchPredictions.class.getName();
        ProgressDialog pd = new ProgressDialog(getActivity());
        @Override
        protected void onPreExecute ( )
        {
            //starting the progress dialogue
            pd.show();
        }
        @Override
        public ArrayList<Prediction> doInBackground(String... params) {

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            String predXML;

            try {
                final String DIRECTION_BASE_URL =
                        "http://bus-time.centro.org/bustime/api/v1/getpredictions?key=gbiC5GSAESipXuzsF6hemn6Hq";
                Uri builtUri = Uri.parse(DIRECTION_BASE_URL).buildUpon()
                        .appendQueryParameter("stpid", params[0]).build();

                URL url = new URL(builtUri.toString());
                Log.v(LOG_TAG, "Built URI " + builtUri.toString());

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                predXML = buffer.toString();
                Log.v(LOG_TAG, predXML);
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
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
                return getPredFromXML(predXML);
            } catch (Exception e) {
                Log.e(LOG_TAG, "Error", e);

            }


            return null;
        }

        private ArrayList<Prediction> getPredFromXML(String xml){
            XmlPullParserFactory pullParserFactory;
            try {
                pullParserFactory = XmlPullParserFactory.newInstance();
                XmlPullParser parser = pullParserFactory.newPullParser();

                // InputStream in_s = getApplicationContext().getAssets().open("temp.xml");

                parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
                parser.setInput(new StringReader(xml));

                return parseXMLforPred(parser);

            } catch (XmlPullParserException e) {

                Log.e(LOG_TAG, "Error", e);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                Log.e(LOG_TAG, "Error", e);
            }
            return null;

        }

        private ArrayList<Prediction> parseXMLforPred(XmlPullParser parser) throws XmlPullParserException, IOException{
            predictions = null;
            int eventType = parser.getEventType();
            Prediction currentPred = null;

            while(eventType != XmlPullParser.END_DOCUMENT){
                String name;
                switch(eventType){
                    case XmlPullParser.START_DOCUMENT:
                        predictions = new ArrayList<Prediction>();
                        break;
                    case XmlPullParser.START_TAG:
                        name = parser.getName();
                        if(name.equalsIgnoreCase("prd")){
                            currentPred = new Prediction();
                        }
                        else if(currentPred != null){
                            if(name.equalsIgnoreCase("tmstmp"))
                               currentPred.setTmstmp(parser.nextText());
                            if(name.equalsIgnoreCase("typ"))
                                currentPred.setTyp(parser.nextText());
                            if(name.equalsIgnoreCase("stpnm"))
                                currentPred.setStpnm(parser.nextText());
                            if(name.equalsIgnoreCase("stpid"))
                                currentPred.setStpid(parser.nextText());
                            if(name.equalsIgnoreCase("vid"))
                                currentPred.setVid(parser.nextText());
                            if(name.equalsIgnoreCase("dstp"))
                                currentPred.setDstp(parser.nextText());
                            if(name.equalsIgnoreCase("rt"))
                                currentPred.setRt(parser.nextText());
                            if(name.equalsIgnoreCase("rtdir"))
                                currentPred.setRtdir(parser.nextText());
                            if(name.equalsIgnoreCase("des"))
                                currentPred.setDes(parser.nextText());
                            if(name.equalsIgnoreCase("prdtm"))
                                currentPred.setPrdtm(parser.nextText());
                            if(name.equalsIgnoreCase("tablockid"))
                                currentPred.setTablockid(parser.nextText());
                            if(name.equalsIgnoreCase("tatripid"))
                                currentPred.setTatripid(parser.nextText());
                            if(name.equalsIgnoreCase("zone"))
                                currentPred.setZone(parser.nextText());
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        name = parser.getName();
                        if(name.equalsIgnoreCase("prd") && currentPred != null)
                            predictions.add(currentPred);

                }
                eventType = parser.next();
            }
            return predictions;
        }

        @Override
        protected void onPostExecute(ArrayList<Prediction> result){
            if(result !=null){
                //predlist = (ListView)rootView.findViewById(R.id.predlist);
                if(result.size()==0) {
                    Toast.makeText(getActivity(), "NO SERVICE AVAILABLE AT THIS TIME!!!",
                            Toast.LENGTH_LONG).show();
                }
                else{
                    PredListAdapter adapter = new PredListAdapter(getActivity(), R.layout.listview_layput, predictions);
                    predlist.setAdapter(adapter);
                }
            }
            pd.dismiss();
        }

    }
}

 //listview adapter
 class PredListAdapter extends BaseAdapter{

    private LayoutInflater mInflator;
    private List<Prediction> predictions;
    Context context;
    int resourceLayoutId;
    private final String LOG_TAG = PredListAdapter.class.getName();

    public PredListAdapter(Context context, int resourceLayoutId, ArrayList<Prediction> objects){
        //super(context, resourceLayoutId, objects);
        this.resourceLayoutId = resourceLayoutId;
        this.context = context;
        this.predictions = objects;

    }

     @Override
     public int getCount() {
         return predictions.size();
     }

     @Override
     public Object getItem(int position) {
         return predictions.get(position);
     }

     @Override
     public long getItemId(int position) {
         return position;
     }

    @Override
    public View getView(int  position, View convertView, ViewGroup parent) {


        View row = convertView;
        Holder holder = null;

        if(row == null){
            LayoutInflater inflater = ((Activity)context).getLayoutInflater();
            row = inflater.inflate(resourceLayoutId, parent, false);
            holder = new Holder();
            holder.txtAtime = (TextView)row.findViewById(R.id.txtAtime);
            holder.txtbusno = (TextView)row.findViewById(R.id.txtbusno);
            holder.txtdestination = (TextView)row.findViewById(R.id.txtdestination);
            holder.txtrouteNo = (TextView)row.findViewById(R.id.txtrouteNo);
            row.setTag(holder);
        }
        else{
            holder = (Holder)row.getTag();
        }

        Prediction prediction = predictions.get(position);
        holder.txtrouteNo.setText(prediction.getRt());
        holder.txtdestination.setText(prediction.getDes());
        holder.txtbusno.setText(prediction.getVid());
        //String[] time = prediction.prdtm.split(" ");
        DateFormat df = new SimpleDateFormat("yyyymmdd kk:mm");
        long diffMin=0;
        try{
            Date prdtime = df.parse(prediction.getPrdtm());
            Log.v(LOG_TAG,prdtime.toString());
            Date systime = df.parse(prediction.getTmstmp());
            Log.v(LOG_TAG,systime.toString());
            long t = prdtime.getTime();
            long diff = prdtime.getTime()-systime.getTime();
            //Log.v(LOG_TAG, diff.toString());
             diffMin = (diff / (60 * 1000) % 60);
            //Log.v(LOG_TAG,diffMinutes.toString());
            System.out.println(diffMin);
        }
        catch(Exception e){
            Log.e(LOG_TAG,"PARSERROR",e);
            holder.txtAtime.setText(prediction.getPrdtm());
        }


        holder.txtAtime.setText(Long.toString(diffMin)+" MINUTES");

        return row;
    }

        static class Holder{

            TextView txtrouteNo;
            TextView txtAtime;
            TextView txtdestination;
            TextView txtbusno;

        }

}