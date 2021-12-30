package com.example.appmow;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class Ubicacion extends AppCompatActivity  implements GoogleMap.OnMapClickListener,
        OnMapReadyCallback {

    private GoogleMap googleMap;
    private LatLng origen, destino;
    TextView eOrigen, eDestino;
    private Integer mapCount = 0;
    private Button limpiar, continuar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ubicacion);

        eOrigen = (TextView) findViewById(R.id.eOrigen);
        eDestino = (TextView) findViewById(R.id.eDestino);

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
                }
                else{
                    Intent data = new Intent();
                    data.putExtra("origen", origen);
                    data.putExtra("destino", destino);
                    setResult(RESULT_OK, data);

                    finish();
                }
            }

            }
        );



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
        if(mapCount == 1){
            this.googleMap.addMarker(new MarkerOptions()
                    .position(point)
                    .title("Origen"));
            this.eOrigen.setText(point.latitude + "," + point.longitude);
            this.origen = point;
        }
        else if (mapCount == 2){
            this.googleMap.addMarker(new MarkerOptions()
                    .position(point)
                    .title("Destino"));
            this.eDestino.setText(point.latitude + "," + point.longitude);
            this.destino = point;
        }

    }

}


