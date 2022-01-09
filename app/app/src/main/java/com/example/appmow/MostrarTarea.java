package com.example.appmow;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Pair;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.directions.route.AbstractRouting;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.MapView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.snackbar.Snackbar;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MostrarTarea extends AppCompatActivity implements OnMapReadyCallback, RoutingListener {

    TextView tAsunto, tFecha, tAlarma, tTransporte;
    Button bEliminar, bEditar;

    private GoogleMap googleMap;
    private LatLng origen, destino;
    private String transporte;
    private List<Polyline> polylines = null;
    private int id;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mostrar_tarea);

        tAsunto = (TextView) findViewById(R.id.nombreTarea);
        tFecha = (TextView) findViewById(R.id.fechaTarea);
        tAlarma = (TextView) findViewById(R.id.alarmaTarea);
        tTransporte = (TextView) findViewById(R.id.transporteTarea);

        bEliminar = (Button) findViewById(R.id.bEliminar);

        String asunto = "";
        transporte = "";
        String fecha = "";
        String alarma = "";
        String ubicacionOrigen = "";
        String ubicacionDestino = "";



        Bundle extras = getIntent().getExtras();
        String strTarea = (String) extras.get("tarea");

        id = Integer.parseInt(strTarea.split("-")[0].substring(1).trim());

        TareaDBHelper th = new TareaDBHelper(getApplicationContext());
        SQLiteDatabase db = th.getReadableDatabase();

        bEliminar.setOnClickListener(v -> {
            String where = TareaContract.TareaEntry._ID + " = ?";
            String[] whereArg = {String.valueOf(id)};
            db.delete(TareaContract.TareaEntry.TABLE_NAME, where, whereArg);

            Intent intent = new Intent(v.getContext(), MainActivity.class);
            startActivity(intent);
        });



        Cursor cursor = db.rawQuery("SELECT * FROM " + TareaContract.TareaEntry.TABLE_NAME + " WHERE _ID = ?", new String[] {id + ""});
        while (cursor.moveToNext()) {

            asunto = cursor.getString(1);
            fecha = cursor.getString(2);
            alarma = cursor.getString(3);
            ubicacionOrigen = cursor.getString(4);
            ubicacionDestino = cursor.getString(5);
            transporte = cursor.getString(6);
            String [] fechaHora = fecha.split(",");
            String [] alarmaHora = alarma.split(",");

            tAsunto.setText(asunto);
            tAlarma.setText(getResources().getString(R.string.dia) + " " + alarmaHora[0] + " " + getResources().getString(R.string.aLas) + " " + alarmaHora[1]);
            tTransporte.setText(transporte);
            tFecha.setText(getResources().getString(R.string.dia) + " " + fechaHora[0] + " " + getResources().getString(R.string.aLas) + " " + fechaHora[1]);


            double latOrigen, latDestino, longOrigen, longDestino;

            String[] ubOrigen = ubicacionOrigen.split(",");
            String[] ubDestino = ubicacionDestino.split(",");

            latOrigen = Double.parseDouble(ubOrigen[0]);
            latDestino = Double.parseDouble(ubDestino[0]);
            longOrigen = Double.parseDouble(ubOrigen[1]);
            longDestino = Double.parseDouble(ubDestino[1]);

            origen = new LatLng(latOrigen, longOrigen);
            destino = new LatLng(latDestino, longDestino);

            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);

        }


    }


    @Override
    public void onMapReady(GoogleMap googleMap) {

        this.googleMap = googleMap;
        this.googleMap.addMarker(new MarkerOptions()
                .position(origen)
                .title("Origen"));

        this.googleMap.addMarker(new MarkerOptions()
                .position(destino)
                .title("Destino"));

        double latmedia = (origen.latitude + destino.latitude) / 2;
        double lonmedia = (origen.longitude + destino.longitude) / 2;

        LatLng latlngM = new LatLng(latmedia, lonmedia);

        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latlngM, 7));

        Findroutes(origen, destino,transporte);


    }

    public void Findroutes(LatLng Start, LatLng End, String mode) {
        if (Start == null || End == null) {
            Toast.makeText(MostrarTarea.this, "Unable to get location", Toast.LENGTH_LONG).show();
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
        Toast.makeText(MostrarTarea.this, getResources().getString(R.string.buscandoRuta), Toast.LENGTH_LONG).show();
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
        Findroutes(origen, destino, transporte);
    }
}

