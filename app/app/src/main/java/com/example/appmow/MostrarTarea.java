package com.example.appmow;

import androidx.appcompat.app.AppCompatActivity;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Pair;
import android.widget.TextView;

import com.google.android.gms.maps.MapView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MostrarTarea extends AppCompatActivity {

    final TextView tAsunto = (TextView) findViewById(R.id.nombreTarea);
    final TextView tFecha = (TextView) findViewById(R.id.fechaTarea);
    final TextView tAlarma = (TextView) findViewById(R.id.alarmaTarea);
    final TextView tTransporte = (TextView) findViewById(R.id.transporteTarea);
    final MapView mOrigen = (MapView) findViewById(R.id.ubicacionPersona);
    final MapView mDestino = (MapView) findViewById(R.id.ubicacionTarea);

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
        Integer tarea = (Integer) extras.get("ID");

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

        Cursor cursor = db.query(Tarea.DictEntry.TABLE_NAME, datos, "_ID = tarea", null, null, null, null);
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





    }

}