package com.example.appmow;

import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.directions.route.AbstractRouting;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.HttpEntity;
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.HttpResponse;
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.client.ClientProtocolException;
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.client.HttpClient;
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.client.methods.HttpPost;
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.impl.client.DefaultHttpClient;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Ubicacion extends AppCompatActivity  implements GoogleMap.OnMapClickListener,
        OnMapReadyCallback, RoutingListener {

    private GoogleMap googleMap;
    private LatLng origen, destino;
    private String duracion = "";
    TextView eOrigen, eDestino, tvDuration;
    private Integer mapCount = 0;
    private Button limpiar, continuar;

    private final static int LOCATION_REQUEST_CODE = 23;
    boolean locationPermission = false;

    private List<Polyline> polylines = null;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ubicacion);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);


        eOrigen = (TextView) findViewById(R.id.eOrigen);
        eDestino = (TextView) findViewById(R.id.eDestino);
        tvDuration = (TextView) findViewById(R.id.tvDistDurat);

        eOrigen.setText("");
        eDestino.setText("");

        limpiar = (Button) findViewById(R.id.limpiar);
        continuar = (Button) findViewById(R.id.continuar);

        limpiar.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                eOrigen.setText("");
                eDestino.setText("");
                tvDuration.setText("");
                mapCount = 0;
                googleMap.clear();
                origen = null;
                destino = null;

            }
        });

        continuar.setOnClickListener(new View.OnClickListener() {
                                         public void onClick(View v) {
                                             if (origen == null || destino == null) {
                                                 Toast.makeText(getApplicationContext(), "Faltan puntos por seleccionar", Toast.LENGTH_SHORT).show();
                                             } else {
                                                 Intent data = new Intent();
                                                 data.putExtra("origen", origen);
                                                 data.putExtra("destino", destino);
                                                 long durationLong = parseDuracion(duracion);
                                                 data.putExtra("duracion", durationLong);
                                                 setResult(RESULT_OK, data);

                                                 finish();
                                             }
                                         }

                                     }
        );

        String apiKey = "AIzaSyD6A7Zni9DVryKVro8--jjmGmy8Zq3auxc";
        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), apiKey);
        }

        PlacesClient placesClient = Places.createClient(this);

        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.autoFragment);

        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.PHOTO_METADATAS, Place.Field.LAT_LNG));


        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                LatLng point = place.getLatLng();
                System.out.println(point);
                mapCount++;
                if (mapCount == 1) {
                    googleMap.addMarker(new MarkerOptions()
                            .position(point)
                            .title("Origen"));
                    eOrigen.setText(place.getName());
                    origen = point;

                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(origen, 10));
                } else if (mapCount == 2) {
                    googleMap.addMarker(new MarkerOptions()
                            .position(point)
                            .title("Destino"));
                    eDestino.setText(place.getName());
                    destino = point;

                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(destino, 10));
                    Findroutes(origen, destino);

                    try {
                        duracion = getDuracion(origen, destino);
                    } catch (IOException | JSONException e) {
                        e.printStackTrace();
                    }

                    tvDuration.setText(duracion + "");


                }

            }

            @Override
            public void onError(@NonNull Status status) {
                // TODO: Handle the error.
                Toast.makeText(getApplicationContext(), status.toString(), Toast.LENGTH_SHORT).show();
            }
        });


        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }

    public long parseDuracion(String input) {
        input = input.toLowerCase()
                .replaceAll("mins?", "M")
                .replaceAll("hours?", "H")
                .replaceAll("\\s+", "");
        Duration d = Duration.parse("PT" + input);
        return d.toMinutes();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        LatLng latlng = new LatLng(-33.852, 151.211);
        this.googleMap = googleMap;
        this.googleMap.addMarker(new MarkerOptions()
                .position(latlng)
                .title("Position"));

        this.googleMap.setOnMapClickListener(this);

    }

    @Override
    public void onMapClick(LatLng point) {
        mapCount++;
        if (mapCount == 1) {
            this.googleMap.addMarker(new MarkerOptions()
                    .position(point)
                    .title("Origen"));
            this.eOrigen.setText(point.latitude + "," + point.longitude);
            this.origen = point;

            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(origen, 10));
        } else if (mapCount == 2) {
            this.googleMap.addMarker(new MarkerOptions()
                    .position(point)
                    .title("Destino"));
            this.eDestino.setText(point.latitude + "," + point.longitude);
            this.destino = point;

            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(destino, 10));
            Findroutes(origen, destino);

            try {
                duracion = getDuracion(origen, destino);
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }

            tvDuration.setText(duracion + "");



        }

    }



    private String getDuracion(LatLng origen, LatLng destino) throws IOException, JSONException {


            String stringUrl = "https://maps.googleapis.com/maps/api/distancematrix/json?units=metric&origins="+
                    origen.latitude+","+origen.longitude+
                    "&destinations="+destino.latitude+","+destino.longitude+"&key=AIzaSyD6A7Zni9DVryKVro8--jjmGmy8Zq3auxc&mode=driving";

        String json;
        json = NetworkUtils.getJSONFromAPI(stringUrl);

        String duration = new JSONObject(json)
                .getJSONArray("rows")
                .getJSONObject(0)
                .getJSONArray("elements")
                .getJSONObject(0)
                .getJSONObject("duration")
                .get("text").toString();



        return duration;


    }


    public static class NetworkUtils {

        public static String getJSONFromAPI (String url){
            String output = "";
            try {
                URL apiEnd = new URL(url);
                int responseCode;
                HttpURLConnection connection;
                InputStream is;

                connection = (HttpURLConnection) apiEnd.openConnection();
                connection.setRequestMethod("GET");
                connection.setReadTimeout(15000);
                connection.setConnectTimeout(15000);
                connection.connect();

                responseCode = connection.getResponseCode();
                if(responseCode < HttpURLConnection.HTTP_BAD_REQUEST){
                    is = connection.getInputStream();
                }else {
                    is = connection.getErrorStream();
                }

                output = convertISToString(is);
                is.close();
                connection.disconnect();

            }  catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (ProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }


            return output;
        }

        private static String convertISToString(InputStream is){
            StringBuffer buffer = new StringBuffer();

            try {

                BufferedReader br;
                String row;

                br = new BufferedReader(new InputStreamReader(is));
                while ((row = br.readLine())!= null){
                    buffer.append(row);
                }
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return buffer.toString();
        }
    }




    // function to find Routes.
    public void Findroutes(LatLng Start, LatLng End) {
        if (Start == null || End == null) {
            Toast.makeText(Ubicacion.this, "Unable to get location", Toast.LENGTH_LONG).show();
        } else {

            Routing routing = new Routing.Builder()
                    .travelMode(AbstractRouting.TravelMode.DRIVING)
                    .withListener(this)
                    .alternativeRoutes(true)
                    .waypoints(Start, End)
                    .key("AIzaSyD6A7Zni9DVryKVro8--jjmGmy8Zq3auxc")  //also define your api key here.
                    .build();
            routing.execute();
        }
    }

    //Routing call back functions.
    @Override
    public void onRoutingFailure(RouteException e) {
        View parentLayout = findViewById(android.R.id.content);
        Snackbar snackbar = Snackbar.make(parentLayout, e.toString(), Snackbar.LENGTH_LONG);
        snackbar.show();
//    Findroutes(start,end);
    }


    @Override
    public void onRoutingStart() {
        Toast.makeText(Ubicacion.this, "Finding Route...", Toast.LENGTH_LONG).show();
    }

    //If Route finding success..
    @Override
    public void onRoutingSuccess(ArrayList<Route> route, int shortestRouteIndex) {

        CameraUpdate center = CameraUpdateFactory.newLatLng(origen);
        CameraUpdate zoom = CameraUpdateFactory.zoomTo(16);
        if (polylines != null) {
            polylines.clear();
        }
        PolylineOptions polyOptions = new PolylineOptions();
        LatLng polylineStartLatLng = null;
        LatLng polylineEndLatLng = null;


        polylines = new ArrayList<>();
        //add route(s) to the map using polyline
        for (int i = 0; i < route.size(); i++) {

            if (i == shortestRouteIndex) {
                polyOptions.color(Color.RED);
                polyOptions.width(7);
                polyOptions.addAll(route.get(shortestRouteIndex).getPoints());
                Polyline polyline = googleMap.addPolyline(polyOptions);
                polylineStartLatLng = polyline.getPoints().get(0);
                int k = polyline.getPoints().size();
                polylineEndLatLng = polyline.getPoints().get(k - 1);
                polylines.add(polyline);

            } else {

            }

        }

    }

    @Override
    public void onRoutingCancelled() {
        Findroutes(origen, destino);
    }

}


















