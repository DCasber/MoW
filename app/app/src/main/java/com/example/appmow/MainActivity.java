package com.example.appmow;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.util.Pair;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
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
    private List<Pair<String, String>> tareas;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        lista = findViewById(R.id.listTareas);

        // CONSEGUIR TAREAS DE LA BD
        // tareas = getResources().getStringArray(R.array.array_technology);

        TareaHelper th = new TareaHelper(getApplicationContext(), "database_name.db");
        SQLiteDatabase db = th.getReadableDatabase();

        Map<Integer, List<Pair<String, String>>> listBD = new HashMap<>();

        String [] datos = {
                Tarea.DictEntry._ID,
                Tarea.DictEntry.COLUMN_NAME_KEY_ASUNTO,
                Tarea.DictEntry.COLUMN_NAME_VAL_ASUNTO,
                Tarea.DictEntry.COLUMN_NAME_KEY_UB_DESTINO,
                Tarea.DictEntry.COLUMN_NAME_VAL_UB_DESTINO,
                Tarea.DictEntry.COLUMN_NAME_KEY_ALARMA,
                Tarea.DictEntry.COLUMN_NAME_VAL_ALARMA
        } ;

        String sortOrder = Tarea.DictEntry.COLUMN_NAME_KEY_FECHA + " ASC";
        Cursor cursor = db.query(Tarea.DictEntry.TABLE_NAME, datos, null, null, null, null, sortOrder);
        try {
            while (cursor.moveToNext()) {
                List<Pair<String, String>> aux = new ArrayList<>();
                Integer id = cursor.getInt(cursor.getColumnIndexOrThrow(Tarea.DictEntry._ID));

                String claveAsunto = cursor.getString(cursor.getColumnIndexOrThrow(Tarea.DictEntry.COLUMN_NAME_KEY_ASUNTO));
                String valorAsunto = cursor.getString(cursor.getColumnIndexOrThrow(Tarea.DictEntry.COLUMN_NAME_VAL_ASUNTO));
                Pair<String, String> asunto = new Pair<>(claveAsunto, valorAsunto);
                aux.add(asunto);

                String claveUbicacion = cursor.getString(cursor.getColumnIndexOrThrow(Tarea.DictEntry.COLUMN_NAME_KEY_UB_DESTINO));
                String valorUbicacion = cursor.getString(cursor.getColumnIndexOrThrow(Tarea.DictEntry.COLUMN_NAME_VAL_UB_DESTINO));
                Pair<String, String> ubicacion = new Pair<>(claveUbicacion, valorUbicacion);
                aux.add(ubicacion);

                String claveAlarma = cursor.getString(cursor.getColumnIndexOrThrow(Tarea.DictEntry.COLUMN_NAME_KEY_ALARMA));
                String valorAlarma = cursor.getString(cursor.getColumnIndexOrThrow(Tarea.DictEntry.COLUMN_NAME_VAL_ALARMA));
                Pair<String, String> alarma = new Pair<>(claveAlarma, valorAlarma);
                aux.add(alarma);

                listBD.put(id, aux);
            }
        } finally {
            cursor.close();
        }

        tareas = new ArrayList<>();

        for (List<Pair<String, String>> t : listBD.values()){
            //t = key_asunto, val_asunto, key_ubicadst, val_ubicadst, key_alarma, val_alarma
            for (ListIterator<Pair<String, String>> it = t.listIterator(); it.hasNext(); ) {
                Pair<String, String> v = it.next();
                tareas.add(v);

            }
        }


        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, android.R.id.text1, tareas);
        lista.setAdapter(adapter);

        lista.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                // TODO Auto-generated method stub

                Intent intent = new Intent(MainActivity.this, MostrarTarea.class);
                // Tarea tarea = COGER TAREA SELECCIONADA
                // intent.putExtra(tarea, tarea);
                startActivity(intent);

            }
        });


    }

}

