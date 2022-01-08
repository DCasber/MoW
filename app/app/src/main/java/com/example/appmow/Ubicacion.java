package com.example.appmow;

import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
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
    private String modoTransporte;
    private String duracion = "";
    TextView eOrigen, eDestino, tvDuration;
    private Integer mapCount = 0;
    private Button limpiar, continuar;
    private Spinner transportes;

    private final static int LOCATION_REQUEST_CODE = 23;
    boolean locationPermission = false;

    private List<Polyline> polylines = null;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ubicacion);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        transportes = findViewById(R.id.spinner);
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
                transportes.setEnabled(false);


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
                                                 data.putExtra("transporte", modoTransporte);
                                                 setResult(RESULT_OK, data);

                                                 finish();
                                             }
                                         }

                                     }
        );

        List<String> listaTransportes = new ArrayList<>();
        listaTransportes.add("Andando");
        listaTransportes.add("Vehiculo");
        listaTransportes.add("Bicicleta");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.support_simple_spinner_dropdown_item, listaTransportes);
        transportes.setAdapter(adapter);

        transportes.setEnabled(false);

        transportes.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                if(origen != null && destino != null){

                    transportes.setEnabled(true);

                    modoTransporte = transportes.getSelectedItem().toString();

                    googleMap.clear();

                    Findroutes(origen, destino,modoTransporte);

                    try {
                        duracion = getDuracion(origen, destino,modoTransporte);
                    } catch (IOException | JSONException e) {
                        e.printStackTrace();
                    }

                    tvDuration.setText(duracion + "");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {

            }

        });

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

                    modoTransporte = transportes.getSelectedItem().toString();

                    transportes.setEnabled(true);

                    Findroutes(origen, destino,modoTransporte);

                    try {
                        duracion = getDuracion(origen, destino,modoTransporte);
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

        Bundle extras = getIntent().getExtras();
        Integer id = (Integer) extras.get("id");

        if (id != 0){
            double latOrigen = (Double) extras.get("latOrigen");
            double latDestino = (Double) extras.get("latDestino");
            double lonOrigen = (Double) extras.get("lonOrigen");
            double lonDestino = (Double) extras.get("lonDestino");

            modoTransporte = (String) extras.get("modoTransporte");

            origen = new LatLng(latOrigen, lonOrigen);
            destino = new LatLng(latDestino, lonDestino);

            mapCount = 2;

            googleMap.addMarker(new MarkerOptions()
                    .position(origen)
                    .title("Origen"));

            googleMap.addMarker(new MarkerOptions()
                    .position(destino)
                    .title("Destino"));

            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(destino, 10));

            transportes.setEnabled(true);

            Findroutes(origen, destino,modoTransporte);

            try {
                duracion = getDuracion(origen, destino,modoTransporte);
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }

            tvDuration.setText(duracion + "");

        }



    }

    public long parseDuracion(String input) {
        input = input.toLowerCase()
                .replaceAll("days?", "DT")
                .replaceAll("mins?", "M")
                .replaceAll("hours?", "H")
                .replaceAll("\\s+", "");

        input = "PT" + input;
        if(input.contains("D")) {
            input = input.replaceFirst("T", "");
        }


        Duration d = Duration.parse(input);
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

            modoTransporte = transportes.getSelectedItem().toString();

            transportes.setEnabled(true);

            Findroutes(origen, destino,modoTransporte);

            try {
                duracion = getDuracion(origen, destino, modoTransporte);
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }

            tvDuration.setText(duracion + "");



        }

    }



    private String getDuracion(LatLng origen, LatLng destino, String mode) throws IOException, JSONException {


        mode = parseMode(mode);

        String stringUrl = "https://maps.googleapis.com/maps/api/distancematrix/json?units=metric&origins="+
                            origen.latitude+","+origen.longitude+
                            "&destinations="+destino.latitude+","+destino.longitude+"&key=AIzaSyD6A7Zni9DVryKVro8--jjmGmy8Zq3auxc&mode="
                            + mode;

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

    private String parseMode(String mode) {

        if(mode.equals("Andando")){
            mode = "walking";
        } else if (mode.equals("Vehiculo")){
            mode = "driving";
        }
        else if(mode.equals("Bicicleta")){
            mode = "bicycling";
        }
        return mode;

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
    public void Findroutes(LatLng Start, LatLng End, String mode) {
        if (Start == null || End == null) {
            Toast.makeText(Ubicacion.this, "Unable to get location", Toast.LENGTH_LONG).show();
        } else {

            AbstractRouting.TravelMode transporte;

            if (mode.equals("Andando")){
                transporte = AbstractRouting.TravelMode.WALKING;

            } else if (mode.equals("Vehiculo")){
                transporte = AbstractRouting.TravelMode.DRIVING;
            } else{
                transporte = AbstractRouting.TravelMode.BIKING;
            }


            Routing routing = new Routing.Builder()
                    .travelMode(transporte)
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
        Findroutes(origen, destino, modoTransporte);
    }

}


















