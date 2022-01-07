package com.example.appmow;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    private ListView lista;
    private List<String> listaTareas;
    private List<Tarea> tareas;
    private static final int ID = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        lista = findViewById(R.id.listTareas);

        consultarListaTareas();


        ArrayAdapter<List<String>> arrayAdapter;

        arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, listaTareas);
        lista.setAdapter(arrayAdapter);
        /*

        lista.setOnItemClickListener((AdapterView.OnItemClickListener) (parent, view, position, id) -> {
            String itemChosen = (String) parent.getItemAtPosition(position);
            Intent intent = new Intent(MainActivity.this, MostrarTarea.class);
            intent.putExtra("tarea", itemChosen);
            startActivity(intent);
        });

         */

        Button crear = (Button) findViewById(R.id.button);

        crear.setOnClickListener((View v) -> {
            Intent intent = new Intent(v.getContext(), NuevaTarea.class);
            startActivity(intent);
        });

    }



    private void consultarListaTareas() {
        TareaDBHelper th = new TareaDBHelper(getApplicationContext());
        SQLiteDatabase db = th.getReadableDatabase();

        Tarea tarea = null;
        tareas = new ArrayList<>();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TareaContract.TareaEntry.TABLE_NAME, null);
        while (cursor.moveToNext()) {
            System.out.println("Entro");
            String asunto = cursor.getString(1);
            String fecha = cursor.getString(2);
            String alarma = cursor.getString(3);
            String origen = cursor.getString(4);
            String destino = cursor.getString(5);
            String transporte = cursor.getString(6);

            tarea = new Tarea(asunto, fecha, alarma, origen, destino, transporte);
            tareas.add(tarea);
        }
        obtenerLista();
    }

    private void obtenerLista(){
        listaTareas = new ArrayList<>();

        for(Tarea t : tareas){

            /* long alarma = Long.parseLong(t.getAlarma());
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(alarma * 1000);
            String date = DateFormat.format("dd-MM-yyyy HH:mm", cal).toString(); */

            listaTareas.add(t.getAsunto());

        }
    }

}

