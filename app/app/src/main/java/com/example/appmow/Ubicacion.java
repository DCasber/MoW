package com.example.appmow;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Ubicacion extends AppCompatActivity  implements GoogleMap.OnMapClickListener,
        OnMapReadyCallback, RoutingListener {

    private GoogleMap googleMap;
    private LatLng origen, destino;
    TextView eOrigen, eDestino, tvDistDurat;
    private Integer mapCount = 0;
    private Button limpiar, continuar;

    private final static int LOCATION_REQUEST_CODE = 23;
    boolean locationPermission = false;

    private List<Polyline> polylines = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ubicacion);


        eOrigen = (TextView) findViewById(R.id.eOrigen);
        eDestino = (TextView) findViewById(R.id.eDestino);
        tvDistDurat = (TextView) findViewById(R.id.tvDistDurat);

        eOrigen.setText("");
        eDestino.setText("");

        limpiar = (Button) findViewById(R.id.limpiar);
        continuar = (Button) findViewById(R.id.continuar);

        limpiar.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                eOrigen.setText("");
                eDestino.setText("");
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


















