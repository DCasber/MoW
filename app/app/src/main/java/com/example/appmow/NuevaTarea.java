package com.example.appmow;

import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TimePicker;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class NuevaTarea extends AppCompatActivity {
    EditText fecha, hora, asunto, eOrigen, eDestino;
    private int a, m, d, h, min;
    private String as, s;
    static final int DATE_ID = 0;
    static final int TIME_ID = 1;
    private Spinner sp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nueva_tarea);

        fecha = (EditText) findViewById(R.id.idFecha);
        hora = (EditText) findViewById(R.id.idHora);
        Calendar C = Calendar.getInstance();

        a = C.get(Calendar.YEAR);
        m = C.get(Calendar.MONTH);
        d = C.get(Calendar.DAY_OF_MONTH);

        h = C.get(Calendar.HOUR);
        min = C.get(Calendar.MINUTE);


        fecha.setOnClickListener((View) -> {
            showDialog(DATE_ID);
        });

        hora.setOnClickListener((View) -> {
            showDialog(TIME_ID);
        });

        asunto = (EditText) findViewById(R.id.idAsunto);

        sp = findViewById(R.id.spinner);

        List<String> transportes = new ArrayList<>();
        transportes.add("Andando");
        transportes.add("Vehiculo");
        transportes.add("Bicicleta");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.support_simple_spinner_dropdown_item, transportes);
        sp.setAdapter(adapter);

        s = sp.getSelectedItem().toString();

        //Button bOrigen = (Button) findViewById(R.id.b);


    }



    private void colocar_hora() {
        hora.setText( h + ":" + min + " ");
    }

    private TimePickerDialog.OnTimeSetListener onTimeSetListener =
            new TimePickerDialog.OnTimeSetListener() {
                public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                    h = hourOfDay;
                    min = minute;
                    colocar_hora();
                }
            };

    private void colocar_fecha() {
        fecha.setText( d + "/" + (m + 1) + "/" + a + " ");
    }

    private DatePickerDialog.OnDateSetListener nDateSetListener =
            new DatePickerDialog.OnDateSetListener() {
                public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                    a = year;
                    m = month;
                    d = dayOfMonth;
                    colocar_fecha();
                }
            };

    @Override
    protected Dialog onCreateDialog(int id){
        switch(id) {
            case DATE_ID:
                return new DatePickerDialog(this, nDateSetListener, a, m, d);
            case TIME_ID:
                return new TimePickerDialog(this, onTimeSetListener, h, min, false);
        }

        return null;
    }
}