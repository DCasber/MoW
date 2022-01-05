package com.example.appmow;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Pair;
import android.view.View;
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

import java.util.ArrayList;
import java.util.List;

public class MostrarTarea extends AppCompatActivity implements OnMapReadyCallback, RoutingListener {

    final TextView tAsunto = (TextView) findViewById(R.id.nombreTarea);
    final TextView tFecha = (TextView) findViewById(R.id.fechaTarea);
    final TextView tAlarma = (TextView) findViewById(R.id.alarmaTarea);
    final TextView tTransporte = (TextView) findViewById(R.id.transporteTarea);

    private GoogleMap googleMap;
    private LatLng origen, destino;
    private List<Polyline> polylines = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mostrar_tarea);

        String asunto = "";
        String transporte = "";
        String fecha = "";
        String alarma = "";
        String ubicacionOrigen = "";
        String ubicacionDestino = "";

        Bundle extras = getIntent().getExtras();
        String tarea = (String) extras.get("tarea");

        Integer id = Integer.parseInt(tarea.split("|")[0].substring(1).trim());
        /*
        TareaHelper th = new TareaHelper(getApplicationContext(), "database_name.db");
        SQLiteDatabase db = th.getReadableDatabase();

        String[] datos = {
                Tarea.DictEntry._ID,
                Tarea.DictEntry.COLUMN_NAME_VAL_ASUNTO,
                Tarea.DictEntry.COLUMN_NAME_VAL_ALARMA,
                Tarea.DictEntry.COLUMN_NAME_VAL_UB_DESTINO,
                Tarea.DictEntry.COLUMN_NAME_VAL_UB_ORIGEN,
                Tarea.DictEntry.COLUMN_NAME_VAL_TRANSPORTE,
                Tarea.DictEntry.COLUMN_NAME_VAL_FECHA,
        };

        Cursor cursor = db.query(Tarea.DictEntry.TABLE_NAME, datos, "_ID = " + id, null, null, null, null);
        try {

                asunto = cursor.getString(cursor.getColumnIndexOrThrow(Tarea.DictEntry.COLUMN_NAME_VAL_ASUNTO));
                alarma = cursor.getString(cursor.getColumnIndexOrThrow(Tarea.DictEntry.COLUMN_NAME_VAL_ALARMA));
                ubicacionDestino = cursor.getString(cursor.getColumnIndexOrThrow(Tarea.DictEntry.COLUMN_NAME_VAL_UB_DESTINO));
                ubicacionOrigen = cursor.getString(cursor.getColumnIndexOrThrow(Tarea.DictEntry.COLUMN_NAME_VAL_UB_ORIGEN));
                transporte = cursor.getString(cursor.getColumnIndexOrThrow(Tarea.DictEntry.COLUMN_NAME_VAL_TRANSPORTE));
                fecha = cursor.getString(cursor.getColumnIndexOrThrow(Tarea.DictEntry.COLUMN_NAME_VAL_FECHA));

        } finally {
            cursor.close();
        }

        tAsunto.setText(asunto);
        tAlarma.setText(alarma);
        tTransporte.setText(transporte);
        tFecha.setText(fecha);

        double latOrigen, latDestino, longOrigen, longDestino;

        String [] ubOrigen = ubicacionOrigen.split(",");
        String [] ubDestino = ubicacionDestino.split(",");

        latOrigen = Double.parseDouble(ubOrigen[0]);
        latDestino = Double.parseDouble(ubDestino[0]);
        longOrigen = Double.parseDouble(ubOrigen[1]);
        longDestino = Double.parseDouble(ubDestino[1]);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        origen = new LatLng(latOrigen, longOrigen);
        destino = new LatLng(latDestino, longDestino);

        */


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

        Findroutes(origen, destino);


    }

    public void Findroutes(LatLng Start, LatLng End) {
        if (Start == null || End == null) {
            Toast.makeText(MostrarTarea.this, "Unable to get location", Toast.LENGTH_LONG).show();
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
        Toast.makeText(MostrarTarea.this, "Finding Route...", Toast.LENGTH_LONG).show();
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

