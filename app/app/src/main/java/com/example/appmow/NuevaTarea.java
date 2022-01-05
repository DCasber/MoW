package com.example.appmow;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

public class NuevaTarea extends AppCompatActivity {
    EditText fecha, hora, asunto, latOrigen, latDestino, lonOrigen, lonDestino;
    private int a, m, d, h, min;
    static final int DATE_ID = 0;
    static final int TIME_ID = 1;
    private Spinner sp;
    private long duracion = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nueva_tarea);

        latOrigen = (EditText) findViewById(R.id.idLatOrigen);
        latDestino = (EditText) findViewById(R.id.idLatDestino);
        lonOrigen = (EditText) findViewById(R.id.idLonOrigen);
        lonDestino = (EditText) findViewById(R.id.idLonDestino);
        fecha = (EditText) findViewById(R.id.idFecha);
        hora = (EditText) findViewById(R.id.idHora);
        asunto = (EditText) findViewById(R.id.idAsunto);
        sp = findViewById(R.id.spinner);

        Intent idInt = getIntent();
        int id = idInt.getIntExtra("id", 0);

        if(id != 0) {
            //Esta función sirve para cuando se accede mediante edit
            //TODO: Obtener tarea por ID y mostrar por pantalla los valores
            latOrigen.setText(0);
            latDestino.setText(0);
            lonOrigen.setText(0);
            lonDestino.setText(0);
            fecha.setText("00/00/0000");
            hora.setText("00:00");
            asunto.setText("00000");
            //TODO: Mostrar valor spinner
        }

        Calendar C = Calendar.getInstance();

        a = C.get(Calendar.YEAR);
        m = C.get(Calendar.MONTH);
        d = C.get(Calendar.DAY_OF_MONTH);

        fecha.setOnClickListener((View) -> {
            showDialog(DATE_ID);
        });

        h = C.get(Calendar.HOUR);
        min = C.get(Calendar.MINUTE);

        hora.setOnClickListener((View) -> {
            showDialog(TIME_ID);
        });


        List<String> transportes = new ArrayList<>();
        transportes.add("Andando");
        transportes.add("Vehiculo");
        transportes.add("Bicicleta");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.support_simple_spinner_dropdown_item, transportes);
        sp.setAdapter(adapter);


        Button bBuscar = (Button) findViewById(R.id.bBuscar);

        bBuscar.setOnClickListener((View v) -> {
            Intent intent = new Intent(v.getContext(), Ubicacion.class);
            buscarUbicacion.launch(intent);
        });

        Button bCrear = (Button) findViewById(R.id.bCrear);

        bCrear.setOnClickListener((View v) -> {
            if(!excepciones()) {
                crear(duracion);
            }
        });
    }


    private void crear(long duracion){
        //TODO: Insetar valores en la base de datos
        Calendar fechaTarea = Calendar.getInstance();
        fechaTarea.set(a, m, d, h, min, 0);
        long timeTarea = fechaTarea.getTimeInMillis();
        timeTarea = timeTarea - duracion - 900000 ;
        Toast.makeText(getApplicationContext(), getString(R.string.changed_to, h + ":" + m), Toast.LENGTH_LONG).show();
        setAlarm(1, timeTarea, NuevaTarea.this);

        insertarBD(timeTarea);

    }

    private void insertarBD(long alarma){

        TareaDBHelper dbHelper = new TareaDBHelper(getApplicationContext());
        SQLiteDatabase db = dbHelper.getWritableDatabase();


        ContentValues values = new ContentValues();
        Calendar fechaTarea = Calendar.getInstance();
        fechaTarea.set(a, m, d, h, min, 0);
        long timeTarea = fechaTarea.getTimeInMillis();

        String origen = "Lat: " + latOrigen + " Lon: " + lonOrigen;
        String destino = "Lat: " + latDestino + " Lon: " + lonDestino;

        values.put(TareaContract.TareaEntry.ASUNTO, asunto.getText().toString());
        values.put(TareaContract.TareaEntry.FECHA, timeTarea + "");
        values.put(TareaContract.TareaEntry.ALARMA, alarma + "");
        values.put(TareaContract.TareaEntry.TRANSPORTE, sp.getSelectedItem().toString());
        values.put(TareaContract.TareaEntry.ORIGEN, origen);
        values.put(TareaContract.TareaEntry.DESTINO, destino);

        db.insert(TareaContract.TareaEntry.TABLE_NAME, null, values);


    }


    ActivityResultLauncher<Intent> buscarUbicacion = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();
                    Bundle extras = data.getExtras();
                    LatLng origen = (LatLng) extras.get("origen");
                    LatLng destino = (LatLng) extras.get("destino");
                    latOrigen.setText(origen.latitude + "");
                    latDestino.setText(destino.latitude + "");
                    lonOrigen.setText(origen.longitude + "");
                    lonDestino.setText(destino.longitude + "");
                    duracion = (long) extras.get("duracion");
                    duracion = duracion * 6000;
                }
            });




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

    private boolean excepciones() {
        AlertDialog.Builder al = new AlertDialog.Builder(this);
        al.setTitle("Error en la aplicacion");

        Calendar cal = Calendar.getInstance();

        if(asunto.getText().toString() == null || asunto.getText().toString().isEmpty()){
            al.setMessage("El asunto no puede ser nulo");
            al.show();
            return true;
        }
        if(fecha.getText().toString() == null || fecha.getText().toString().isEmpty()){
            al.setMessage("La fecha no puede ser nula");
            al.show();
            return true;
        } else if(a < cal.get(Calendar.YEAR)) {
            al.setMessage("El año no puede ser menor al actual");
            al.show();
            return true;
        } else if(a == cal.get(Calendar.YEAR) && m < cal.get(Calendar.MONTH) ) {
            al.setMessage("El mes no puede ser menor al actual");
            al.show();
            return true;
        } else if(a == cal.get(Calendar.YEAR) && m == cal.get(Calendar.MONTH)  && d < cal.get(Calendar.DAY_OF_MONTH) ){
            al.setMessage("El dia no puede ser menor al actual");
            al.show();
            return true;
        }

        if(hora.getText().toString() == null || hora.getText().toString().isEmpty()){
            al.setMessage("La hora no puede ser nulo");
            al.show();
            return true;
        } else if (a == cal.get(Calendar.YEAR) && m == cal.get(Calendar.MONTH) && d == cal.get(Calendar.DAY_OF_MONTH)
                && (h < cal.get(Calendar.HOUR) || min < cal.get(Calendar.MINUTE))){
            al.setMessage("La hora no puede ser menor a la actual");
            al.show();
            return true;
        }

        if(latOrigen.getText().toString() == null || latOrigen.getText().toString().isEmpty()
                || lonOrigen.getText().toString() == null || lonOrigen.getText().toString().isEmpty()) {
            al.setMessage("No se ha seleccionado un punto de origen");
            al.show();
            return true;
        }

        if (latDestino.getText().toString() == null || latDestino.getText().toString().isEmpty()
                || lonDestino.getText().toString() == null || lonDestino.getText().toString().isEmpty()) {
            al.setMessage("No se ha seleccionado un punto de destino");
            al.show();
            return true;
        }

        return false;
    }

    public static void setAlarm(int i, Long timestamp, Context ctx) {
        AlarmManager alarmManager = (AlarmManager) ctx.getSystemService(ALARM_SERVICE);
        Intent alarmIntent = new Intent(ctx, AlarmReceiver.class);
        PendingIntent pendingIntent;
        pendingIntent = PendingIntent.getBroadcast(ctx, i, alarmIntent, PendingIntent.FLAG_ONE_SHOT);
        alarmIntent.setData((Uri.parse("custom://" + System.currentTimeMillis())));
        alarmManager.set(AlarmManager.RTC_WAKEUP, timestamp, pendingIntent);
    }
}