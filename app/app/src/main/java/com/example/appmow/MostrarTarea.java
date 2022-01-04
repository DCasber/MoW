package com.example.appmow;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Pair;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.MapView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MostrarTarea extends AppCompatActivity{

    final TextView tAsunto = (TextView) findViewById(R.id.nombreTarea);
    final TextView tFecha = (TextView) findViewById(R.id.fechaTarea);
    final TextView tAlarma = (TextView) findViewById(R.id.alarmaTarea);
    final TextView tTransporte = (TextView) findViewById(R.id.transporteTarea);


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

        latOrigen = Integer.parseInt(ubOrigen[0]);
        latDestino = Integer.parseInt(ubDestino[0]);
        longOrigen = Integer.parseInt(ubOrigen[1]);
        longDestino = Integer.parseInt(ubDestino[1]);

        com.google.android.gms.maps.MapFragment mapOrigen = (MapFragment) getFragmentManager().findFragmentById(R.id.ubicacionPersona);
        mapOrigen.getMapAsync(onMapReadyCallback1(latOrigen, longOrigen));

        com.google.android.gms.maps.MapFragment mapDestino = (MapFragment) getFragmentManager().findFragmentById(R.id.ubicacionTarea);
        mapDestino.getMapAsync(onMapReadyCallback2(latDestino, longDestino));

        */

    }

    public OnMapReadyCallback onMapReadyCallback1(double lat, double lon){
        return googleMap -> {
            GoogleMap mMap = googleMap;
            LatLng latlng = new LatLng(lat, lon);
            mMap.addMarker(new MarkerOptions().position(latlng).title("Origen"));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latlng));
        };
    }

    public OnMapReadyCallback onMapReadyCallback2(double lat, double lon){
        return googleMap -> {
            GoogleMap mMap = googleMap;
            LatLng latlng = new LatLng(lat, lon);
            mMap.addMarker(new MarkerOptions().position(latlng).title("Destino"));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latlng));

        };
    }

}