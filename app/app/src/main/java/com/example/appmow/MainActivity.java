package com.example.appmow;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.util.Pair;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private ListView lista;
    private List<String> tareas;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        lista = findViewById(R.id.listTareas);

        TareaHelper th = new TareaHelper(getApplicationContext(), "database_name.db");
        SQLiteDatabase db = th.getReadableDatabase();

        tareas = new ArrayList<>();

        String [] datos = {
                Tarea.DictEntry._ID,
                Tarea.DictEntry.COLUMN_NAME_VAL_ASUNTO,
                Tarea.DictEntry.COLUMN_NAME_VAL_ALARMA
        } ;


        String sortOrder = Tarea.DictEntry.COLUMN_NAME_KEY_FECHA + " ASC";
        Cursor cursor = db.query(Tarea.DictEntry.TABLE_NAME, datos, null, null, null, null, sortOrder);
        try {
            while (cursor.moveToNext()) {
                String id = cursor.getString(cursor.getColumnIndexOrThrow(Tarea.DictEntry._ID));
                String valorAsunto = cursor.getString(cursor.getColumnIndexOrThrow(Tarea.DictEntry.COLUMN_NAME_VAL_ASUNTO));
                String valorAlarma = cursor.getString(cursor.getColumnIndexOrThrow(Tarea.DictEntry.COLUMN_NAME_VAL_ALARMA));
                String aux = "#" + id + " | Asunto: " + valorAsunto + ", - Hora de Alarma: " + valorAlarma;
                tareas.add(aux);


            }
        } finally {
            cursor.close();
        }

        ArrayAdapter<List<String>> arrayAdapter;
        /*
        arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, tareas);
        lista.setAdapter(arrayAdapter);
        */


        lista.setOnItemClickListener((AdapterView.OnItemClickListener) (parent, view, position, id) -> {
            String itemChosen = (String) parent.getItemAtPosition(position);
            Intent intent = new Intent(MainActivity.this, MostrarTarea.class);
            intent.putExtra("tarea", itemChosen);
            startActivity(intent);
        });

        Button crear = (Button) findViewById(R.id.button);

        crear.setOnClickListener((View v) -> {
            Intent intent = new Intent(v.getContext(), NuevaTarea.class);
            startActivity(intent);
        });




    }

}

